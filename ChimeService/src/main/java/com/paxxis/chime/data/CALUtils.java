/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.chime.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlparser.Parser;
import org.htmlparser.visitors.TextExtractingVisitor;

import com.paxxis.chime.cal.parser.CALRuleParser;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.ClauseOperator;
import com.paxxis.chime.client.common.DataInstanceRequest.Operator;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Parameter;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.cal.ExtensionHelper;
import com.paxxis.chime.client.common.cal.IValue;
import com.paxxis.chime.client.common.cal.QueryParameter;
import com.paxxis.chime.client.common.cal.QueryProvider;
import com.paxxis.chime.client.common.cal.Rule;
import com.paxxis.chime.client.common.cal.RuleSet;
import com.paxxis.chime.client.common.cal.Runtime;
import com.paxxis.chime.client.common.extension.ChimeExtension;
import com.paxxis.chime.data.DataInstanceUtils.FetchType;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.extension.CALExtensionHelper;
import com.paxxis.chime.extension.ChimeExtensionManager;

/**
 *
 * @author Robert Englander
 */
class CALRuntime extends Runtime implements Serializable {
    transient private DatabaseConnection database;
    transient private User user;

    public CALRuntime() {

    }

    public CALRuntime(User user, DatabaseConnection database) {
        this.user = user;
        this.database = database;
    }

    @Override
    public QueryProvider getQueryProvider() {
        return new QueryProvider() {
            public double getStockPrice(String symbol) {
                return CALUtils.getStockPrice(symbol);
            }

            public List<DataInstance> getDataInstances(List<QueryParameter> params) {
                return CALUtils.getDataInstances(params, user, database);
            }

            public List<String> getInstances(List<QueryParameter> params) {
                return CALUtils.getInstances(params, user, database);
            }

            public DataInstance getDataInstanceById(InstanceId id) {
                return CALUtils.getDataInstanceById(id, user, database);
            }

            public Shape getShapeById(InstanceId id) {
                return CALUtils.getShapeById(id, user, database);
            }

            public ChimeExtension getExtension(String id) {
                return CALUtils.getExtension(id);
            }

            public ExtensionHelper createExtensionHelper() {
                return new CALExtensionHelper();
            }
        };
    }
}


class StockQuoter implements Serializable
{
    private final static String QUOTEURL =
            "http://finance.yahoo.com/d/quotes.csv?f=sl1d1t1c1ohgv&e=.csv&s=";

    private final static String HIST1 =
            "http://ichart.finance.yahoo.com/table.csv?s=";
    private final static String HIST2 =
            "&a=02&b=11&c=2006&d=02&e=10&f=2007&g=d&ignore=.csv";

    public StockQuoter() {

    }

    public double getQuote(String symbol) throws Exception
    {
        String compositeURL = QUOTEURL + symbol;
        URL url = new URL(compositeURL);
        InputStream is = url.openStream();
        Reader reader = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer st = new StreamTokenizer(reader);
        // grab the first token - should be the symbol
        st.nextToken();
        String returnedSymbol = st.sval;
        if (!returnedSymbol.equals(symbol))
                throw new Exception("A problem occurred with the stock service");
        else
        {
                st.nextToken(); // skip the comma
                st.nextToken(); // get the quote
                double quote = (double)st.nval;
                return quote;
        }
    }

    public String getHistoricalRaw(String symbol) throws Exception
    {
        String compositeURL = HIST1 + symbol + HIST2;
        URL url = new URL(compositeURL);
        InputStream is = url.openStream();
        Reader reader = new BufferedReader(new InputStreamReader(is));
        BufferedReader bir = new BufferedReader(reader);

        // get past the header
        StringBuffer buf = new StringBuffer();
        String line = bir.readLine();
        while (line != null)
        {
            buf.append(line + "\n");
            line = bir.readLine();
        }

        return buf.toString();
    }

    public List<Double> getHistoricalClose(String symbol) throws Exception
    {
        String compositeURL = HIST1 + symbol + HIST2;
        URL url = new URL(compositeURL);
        InputStream is = url.openStream();
        Reader reader = new BufferedReader(new InputStreamReader(is));
        BufferedReader bir = new BufferedReader(reader);

        // get past the header
        String line = bir.readLine();

        List<Double> prices = new ArrayList<Double>();

        // read the data
        while (line != null)
        {
            line = bir.readLine();
            if (line == null)
            {
                break;
            }

            // there are 7 columns.  the 5th column is the close price
            StringTokenizer tok = new StringTokenizer(line, ",");

            for (int i = 0; i < 4; i++)
            {
                tok.nextToken();
            }

            // they come in reverse date order, so insert each entry at the head
            Double closePrice = Double.valueOf(tok.nextToken());
            prices.add(0, closePrice);

            for (int i = 0; i < 2; i++)
            {
                tok.nextToken();
            }
        }

        return prices;
    }
}


/**
 *
 * @author Robert Englander
 */
public class CALUtils {
    private static final Object monitor = new Object();

    private CALUtils()
    {}

    public static IValue execute(DataInstance instance, final User user, final DatabaseConnection database) throws Exception {

        Shape scriptType = instance.getShapes().get(0);

        if (!scriptType.getId().equals(Shape.ANALYTIC_ID)) {
            throw new Exception("Attempt to execute wrong Shape: " + scriptType.getName());
        }

        // get the CAL script
        DataField scriptField = scriptType.getField("Script");
        List<DataFieldValue> values = instance.getFieldValues(scriptType, scriptField);
        if (values.size() == 0) {
            throw new Exception("No Script to execute.");
        }

        String script = values.get(0).getValue().toString();

        // replace html line breaks
        script = script.replaceAll("<br>", "\n");

        // extract the text from the html
        Parser htmlparser = Parser.createParser(script, null);
        TextExtractingVisitor visitor = new TextExtractingVisitor();
        htmlparser.visitAllNodesWith(visitor);
        script = visitor.getExtractedText();

        Rule rule = null;
        synchronized(monitor) {
            CALRuleParser parser = CALRuleParser.create(script);
            RuleSet ruleSet = new RuleSet("CAL", parser.getSourceCode(), new CALRuntime(user, database));

            parser.parseRuleSet(ruleSet);
            rule = ruleSet.getRule("execute");
        }

        boolean result = rule.process(null);

        return rule.getReturnValue();
    }

    public static List<String> getInstances(List<QueryParameter> params, final User user, final DatabaseConnection database) {
        List<String> result = new ArrayList<String>();
        try {
            result.addAll(getHitsList(params, user, database));
        } catch (Exception ex) {
            Logger.getLogger(CALUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static DataInstance getDataInstanceById(InstanceId id, User user, DatabaseConnection database) {
        DataInstance instance = null;
        try {
            instance = DataInstanceUtils.getInstance(id, user, database, true, true);
        } catch (Exception ex) {
            Logger.getLogger(CALUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return instance;
    }

    public static ChimeExtension getExtension(String id) {
        List<ChimeExtension> list = ChimeExtensionManager.instance().getExtensions();
        for (ChimeExtension ext : list) {
            if (ext.getId().equals(id)) {
                return ext;
            }
        }

        return null;
    }

    public static Shape getShapeById(InstanceId id, User user, DatabaseConnection database) {
        Shape shape = null;
        try {
            shape = ShapeUtils.getInstanceById(id, database, true);
        } catch (Exception ex) {
            Logger.getLogger(CALUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return shape;
    }

    public static double getStockPrice(String symbol) {
        StockQuoter quoter = new StockQuoter();
        double price = 0.0;
        try {
            price = quoter.getQuote(symbol);
        } catch (Exception e) {

        }

        return price;
    }

    public static List<DataInstance> getDataInstances(List<QueryParameter> params, User user, DatabaseConnection database) {
        List<DataInstance> results = new ArrayList<DataInstance>();

        try {
            List<String> ids = getHitsList(params, user, database);
            for (String id : ids) {
                InstanceId instId = InstanceId.create(id);
                DataInstance inst = DataInstanceUtils.getInstance(instId, user, database, FetchType.Deep, true);
                results.add(inst);
            }
        } catch (Exception e) {

        }

        return results;
    }

    private static List<String> getHitsList(List<QueryParameter> params, User user, DatabaseConnection database) throws Exception {
        // turn the query parameters into indexing parameters
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (QueryParameter qparam : params) {
            Parameter p = new Parameter();
            p.dataShape = null;
            IValue shapeVal = qparam.getShapeExpression();
            if (shapeVal != null) {
                Shape shape = ShapeUtils.getInstanceById(InstanceId.create(shapeVal.valueAsString()), database, true);
                p.dataShape = shape;
            }

            p.fieldName = qparam.getFieldExpression().valueAsString();
            p.fieldValue = qparam.getValueExpression().valueAsString();

            switch (qparam.getOperator()) {
                case STREQUALS:
                    p.operator = Operator.Contains;
                    break;
                case VALEQUALS:
                    p.operator = Operator.Equals;
                    break;
                case ON:
                    p.operator = Operator.OnDate;
                    p.fieldValue = (Serializable)qparam.getValueExpression().valueAsObject();
                    break;
                case BEFORE:
                    p.operator = Operator.BeforeDate;
                    p.fieldValue = (Serializable)qparam.getValueExpression().valueAsObject();
                    break;
                case AFTER:
                    p.operator = Operator.AfterDate;
                    p.fieldValue = (Serializable)qparam.getValueExpression().valueAsObject();
                    break;
                case IS:
                    p.operator = Operator.Reference;
                    break;
                case ISNOT:
                    p.operator = Operator.NotReference;
                    break;
                case GREATERTHAN:
                    p.operator = Operator.GreaterThan;
                    break;
                case GREATERTHANEQ:
                    p.operator = Operator.GreaterThanOrEquals;
                    break;
                case LESSTHAN:
                    p.operator = Operator.LessThan;
                    break;
                case LESSTHANEQ:
                    p.operator = Operator.LessThanOrEquals;
                    break;
                case NOTEQUALS:
                    p.operator = Operator.NotEquals;
                    break;
            }

            parameters.add(p);
        }

        List<String> hits = SearchUtils.getInstanceHitIds(null, parameters, ClauseOperator.MatchAll, user, false, false);
        return hits;
    }
}
