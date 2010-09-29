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

package com.paxxis.chime.indexing;

import org.apache.tika.Tika;
import com.paxxis.chime.client.common.BackReferencingDataInstance;
import com.paxxis.chime.client.common.Comment;
import com.paxxis.chime.client.common.CommentsBundle;
import com.paxxis.chime.client.common.Community;
import com.paxxis.chime.client.common.DataField;
import com.paxxis.chime.client.common.DataFieldValue;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest.SortOrder;
import com.paxxis.chime.client.common.DataSocialContext;
import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.Discussion;
import com.paxxis.chime.client.common.Dashboard;
import com.paxxis.chime.client.common.InstanceId;
import com.paxxis.chime.client.common.Scope;
import com.paxxis.chime.client.common.Tag;
import com.paxxis.chime.client.common.TagContext;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.portal.PortalColumn;
import com.paxxis.chime.client.common.portal.PortalTemplate;
import com.paxxis.chime.client.common.portal.PortletSpecification;
import com.paxxis.chime.client.common.portal.PortletSpecification.PortletType;
import com.paxxis.chime.database.DataSet;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import com.paxxis.chime.database.IDataValue;
import com.paxxis.chime.data.CacheManager;
import com.paxxis.chime.data.CommentUtils;
import com.paxxis.chime.data.DataInstanceUtils;
import com.paxxis.chime.data.ShapeUtils;
import com.paxxis.chime.data.TagUtils;
import com.paxxis.chime.service.Tools;
import com.paxxis.chime.indexing.IndexUpdater.Type;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.htmlparser.Parser;
import org.htmlparser.visitors.TextExtractingVisitor;
 
/**
 *
 * @author Robert Englander
 */
public class Indexer {
    private static final Logger _logger = Logger.getLogger(Indexer.class);

    private static final String PREFIX = "Index-";

    // this should come from a config
    protected static final int SPINDLES = 1;

    private static String[] INDEXES;

    private static Indexer instance = null;
    
    static ExecutorService _executor = null;

    static {
        INDEXES = new String[SPINDLES + 1]; // the extra index for for events
        for (int i = 0; i < (SPINDLES + 1); i++) {
            INDEXES[i] = PREFIX + (i + 1);
        }
    }

    /** this is a thread synchronization latch that users of the index must .await() on
     * before accessing the index.  This gives the startup indexer a chance to finish
     * its work before any other threads attempt access to the index
     */
    private CountDownLatch readyLatch = new CountDownLatch(1);

    public static Indexer instance()
    {
        if (instance == null)
        {
            instance = new Indexer();
            instance.setup();
        }
        
        return instance;
    }

    public void await() {
        try {
            readyLatch.await();
        } catch (InterruptedException e) {
        }
    }

    public void setReady() {
        readyLatch.countDown();
    }

    public static String[] getIndexNames() {
        return INDEXES;
    }

    public static String indexNameByInstance(DataInstance instance) {
        int idx = 1 + indexByInstance(instance);
        return PREFIX + idx;
    }

    public static int indexByInstance(DataInstance instance) {
        int idx;
        if (instance.getShapes().get(0).getId().equals(Shape.EVENT_ID)) {
            idx = SPINDLES;
        } else {
            idx = (instance.getId().hashCode() % SPINDLES);
        }

        return idx;
    }

    /*
    public static String indexNameByInstance(String typeId) {
        long idx = 1 + (typeId.hashCode() % SPINDLES);
        return PREFIX + idx;
    }
    */

    private void setup()
    {
        _executor = Executors.newFixedThreadPool(1);
    }
    
    public void rebuildIndex(boolean optimize, User user, DatabaseConnectionPool pool)
    {
        if (user.isAdmin()) {
            user.setIndexing(true);
            _executor.submit(new IndexBuilder(optimize, user, pool));
        }
    }

    public void rebuildDiffs(DatabaseConnection database)
    {
       // _executor.submit(new DiffUpdater(database));
    }

    public void optimize(int spindle) {
        _executor.submit(new OptimizerTask(spindle));
    }

    public void createIndex(Shape type, DatabaseConnectionPool pool)
    {
        IndexBuilder builder = new IndexBuilder(type, pool);
        builder.run();
        
        IndexUpdater updater = new IndexUpdater(type, Type.Create, pool);
        updater.run();
    }
    
    public void typeCreated(DataInstance type, DatabaseConnectionPool pool)
    {
        _executor.submit(new IndexBuilder(type, pool));
    }
    
    public void dataCreated(DataInstance data, DatabaseConnectionPool pool)
    {
        _executor.submit(new IndexUpdater(data, Type.Create, pool));
    }

    public void dataModified(DataInstance data, DatabaseConnectionPool pool)
    {
        _executor.submit(new IndexUpdater(data, Type.Modify, pool));
    }
    
    public void dataDeleted(DataInstance data, DatabaseConnectionPool pool)
    {
        _executor.submit(new IndexUpdater(data, Type.Delete, pool));
    }
    
    public void dataDeleted(List<DataInstance> list, DatabaseConnectionPool pool)
    {
        _executor.submit(new IndexEventDeleter(list, pool));
    }

    public void nameChanged(DataInstance data)
    {
        //_executor.submit(new NameChanger(data, Type.Delete));
    }
}

abstract class IndexerBase implements Runnable {
    private static final Logger _logger = Logger.getLogger(IndexerBase.class);

    DatabaseConnectionPool _pool;

    public IndexerBase(DatabaseConnectionPool pool) {
        _pool = pool;
    }

    protected void indexFileContents(Document doc, DataInstance instance) {

        try {
            // we need the name of the file, which is in the File ID field from the File type
            Shape fileType = instance.getShapes().get(0);
            DataField field = fileType.getField("File ID");
            List<DataFieldValue> values = instance.getFieldValues(fileType, field);
            if (values.size() > 0) {
                _logger.info("Indexing file contents");
                String fileName = "./filestore/" + values.get(0).getName();
                
                Tika tika = new Tika();
                File file = new File(fileName);
                String results = tika.parseToString(file);
                field = fileType.getField("File");
                String fieldName = "field" + field.getId();
                doc.add(new Field(fieldName, results, Field.Store.NO, Field.Index.TOKENIZED));
                doc.add(new Field("content", results, Field.Store.NO, Field.Index.TOKENIZED));
            }
        } catch (Exception e) {
            int x = 1;
        }
    }

    protected Document buildDocument(DataInstance instance, User indexingUser, DatabaseConnection database) throws Exception
    {
        Document doc = new Document();
        String isTransient = "N";
        if (instance.isTransient()) {
            isTransient = "Y";
        }
        doc.add(new Field("transient", isTransient, Field.Store.YES, Field.Index.UN_TOKENIZED));

        doc.add(new Field("instanceid", instance.getId().getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("marker", "marker", Field.Store.NO, Field.Index.UN_TOKENIZED));

        Field f = new Field("name", instance.getName(), Field.Store.YES, Field.Index.TOKENIZED);
        f.setBoost(5);
        doc.add(f);

        f = new Field("fullName", instance.getName().toUpperCase(), Field.Store.NO, Field.Index.UN_TOKENIZED);
        f.setBoost(5);
        doc.add(f);

        String description = instance.getDescription();
        if (description != null) {
            f = new Field("description", description, Field.Store.YES, Field.Index.TOKENIZED);
            f.setBoost(3);
            doc.add(f);
        }

        Date latestActivity = null;
        
        Date date = instance.getCreated();
        doc.add(new Field("created", Tools.longToNumeric(date.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        User user = instance.getCreatedBy();
        doc.add(new Field("createdById", user.getId().getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("createdByName", user.getName(), Field.Store.YES, Field.Index.NO));
        if (latestActivity == null || date.after(latestActivity)) {
            latestActivity = date;
        }

        date = instance.getUpdated();
        doc.add(new Field("updated", Tools.longToNumeric(date.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        user = instance.getUpdatedBy();
        doc.add(new Field("updatedById", user.getId().getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("refId", user.getId().getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        doc.add(new Field("updatedByName", user.getName(), Field.Store.YES, Field.Index.NO));
        if (latestActivity == null || date.after(latestActivity)) {
            latestActivity = date;
        }

        date = instance.getExpiration();
        if (date != null) {
            doc.add(new Field("expiration", Tools.longToNumeric(date.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }

        // get the full user instance
        Shape userType = ShapeUtils.getInstance("User", database, true);

        User theUser = (User)CacheManager.instance().get(user.getId());
        if (theUser == null) {
            theUser = (User)DataInstanceUtils.getInstance(user.getId(), indexingUser, database, true, true);
        }

        List<Community> communities = theUser.getCommunities();
        for (Community community : communities) {
            doc.add(new Field("updatedByCommunityId", community.getId().getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }

        theUser = (User)CacheManager.instance().get(instance.getCreatedBy().getId());
        if (theUser == null) {
            theUser = (User)DataInstanceUtils.getInstance(instance.getCreatedBy().getId(), indexingUser, database, true, true);
        }

        communities = theUser.getCommunities();
        for (Community community : communities) {
            doc.add(new Field("createdByCommunityId", community.getId().getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }

        date = instance.getTagged();
        if (date != null)
        {
            doc.add(new Field("tagged", Tools.longToNumeric(date.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            user = instance.getTaggedBy();
            doc.add(new Field("taggedById", user.getId().getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("taggedByName", user.getName(), Field.Store.YES, Field.Index.NO));
            doc.add(new Field("taggedAction", instance.getTaggedAction().toString(), Field.Store.YES, Field.Index.NO));
            
            if (latestActivity == null || date.after(latestActivity))
            {
                latestActivity = date;
            }
        }
        
        date = instance.getReviewed();
        if (date != null)
        {
            doc.add(new Field("reviewed", Tools.longToNumeric(date.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            user = instance.getReviewedBy();
            doc.add(new Field("reviewedById", user.getId().getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("reviewedByName", user.getName(), Field.Store.YES, Field.Index.NO));
            doc.add(new Field("reviewedAction", instance.getReviewedAction().toString(), Field.Store.YES, Field.Index.NO));
            
            if (latestActivity == null || date.after(latestActivity))
            {
                latestActivity = date;
            }
        }
        
        date = instance.getCommented();
        if (date != null)
        {
            doc.add(new Field("commented", Tools.longToNumeric(date.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            user = instance.getCommentedBy();
            doc.add(new Field("commentedById", user.getId().getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("commentedByName", user.getName(), Field.Store.YES, Field.Index.NO));
            
            if (latestActivity == null || date.after(latestActivity))
            {
                latestActivity = date;
            }
        }

        if (latestActivity != null)
        {
            doc.add(new Field("latestActivity", Tools.longToNumeric(latestActivity.getTime()), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }
        
        DataSocialContext context = instance.getSocialContext();
        doc.add(new Field("ratingCount", String.valueOf(context.getRatingCount()), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("averageRating", Tools.floatToNumeric(context.getAverageRating()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("tagCount", String.valueOf(instance.getSocialContext().getTagContexts().size()), Field.Store.YES, Field.Index.NO));

        doc.add(new Field("commentCount", "0", Field.Store.YES, Field.Index.NO));

        doc.add(new Field("positiveCount", String.valueOf(instance.getPositiveCount()), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("negativeCount", String.valueOf(instance.getNegativeCount()), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("ranking", Tools.longToNumeric(instance.getRanking()), Field.Store.YES, Field.Index.UN_TOKENIZED));

        List<TagContext> tagContexts = context.getTagContexts();
        for (TagContext tagContext : tagContexts)
        {
            Tag tag = tagContext.getTag();
            String id = tag.getId().getValue();
            doc.add(new Field("tagId", id, Field.Store.NO, Field.Index.UN_TOKENIZED));
            doc.add(new Field("refId", id, Field.Store.NO, Field.Index.UN_TOKENIZED));

            StringBuilder buffer = new StringBuilder();
            List<String> userIds = TagUtils.getAppliedUserIds(tag, instance, database);
            for (String userId : userIds) {
                buffer.append(userId).append(" ");
            }

            String fieldName = "tagUser" + tag.getId() + "Id";
            doc.add(new Field(fieldName, buffer.toString().trim(), Field.Store.NO, Field.Index.TOKENIZED));
        }

        for (Shape shape : instance.getShapes()) {
            doc.add(new Field("dataTypeId", String.valueOf(shape.getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("refId", String.valueOf(shape.getId()), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }

        if (instance instanceof Shape)
        {
            Shape thisShape = (Shape)instance;
            if (thisShape.isPrimitive() || !thisShape.isVisible()) {
                doc.add(new Field("internalType", "1", Field.Store.NO, Field.Index.UN_TOKENIZED));
            } else {
                doc.add(new Field("internalType", "0", Field.Store.NO, Field.Index.UN_TOKENIZED));
            }

            if (thisShape.isDirectCreatable()) {
                doc.add(new Field("direct", "1", Field.Store.NO, Field.Index.UN_TOKENIZED));
            } else {
                doc.add(new Field("direct", "0", Field.Store.NO, Field.Index.UN_TOKENIZED));
            }
        }

        if (instance instanceof BackReferencingDataInstance) {
            BackReferencingDataInstance br = (BackReferencingDataInstance)instance;
            InstanceId refId = br.getBackRefId();
            doc.add(new Field("backrefId", refId.getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
            doc.add(new Field("refId", refId.getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));

            DataInstance brInst = DataInstanceUtils.getInstance(refId, indexingUser, database, true, true);
            for (Shape type : brInst.getShapes()) {
                doc.add(new Field("refId", type.getId().getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
            }
        }

        // the contents of the posts in a discussion is searchable content.  posts are actually just comments
        // on the discussion instance.
        if (instance instanceof Discussion) {
            Discussion disc = (Discussion)instance;

            CommentsBundle bundle = context.getCommentsBundle();
            boolean more = bundle.getComments().size() > 0;
            while (more) {
                List<Comment> comments = bundle.getComments();
                for (Comment comment : comments) {
                    doc.add(new Field("postUserId", comment.getUpdatedBy().getId().getValue(), Field.Store.NO, Field.Index.TOKENIZED));
                    doc.add(new Field("postContent", comment.getDescription(), Field.Store.NO, Field.Index.TOKENIZED));
                    doc.add(new Field("content", comment.getDescription(), Field.Store.NO, Field.Index.TOKENIZED));
                }

                more = bundle.getCursor().prepareNext();
                if (more) {
                    bundle = CommentUtils.getComments(disc.getId(), null, bundle.getCursor(), SortOrder.ByName, database);
                }
            }
        }

        List<Scope> scopes = context.getScopes();
        for (Scope scope : scopes)
        {
            String id = scope.getCommunity().getId().getValue();
            doc.add(new Field("communityId", id, Field.Store.NO, Field.Index.UN_TOKENIZED));
        }

        for (DataInstance inst : instance.getFiles()) {
            doc.add(new Field("refId", inst.getId().getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }

        for (DataInstance inst : instance.getImages()) {
            doc.add(new Field("refId", inst.getId().getValue(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }

        if (instance instanceof Dashboard) {
            Dashboard page = (Dashboard)instance;
            PortalTemplate template = page.getPageTemplate();
            for (PortalColumn col : template.getPortalColumns()) {
                for (PortletSpecification spec : col.getPortletSpecifications()) {
                    Serializable ser = spec.getProperty("instanceId");
                    if (ser != null) {
                        String id = ser.toString();
                        doc.add(new Field("refId", id, Field.Store.NO, Field.Index.UN_TOKENIZED));
                    }

                    if (spec.getType() == PortletType.RichText) {
                        // look for references within the content
                        String html =  spec.getProperty("content").toString().replaceAll("##QUOTE##", "\"");
                        int idx;
                        while (-1 != (idx = html.indexOf("<a href=\"chime://#detail:"))) {
                            // find the instance id
                            int idx2 = idx + 25;
                            int idx3 = idx2 - 1;
                            while (html.charAt(idx3) != '"') {
                                    idx3++;
                            }
                            String instStr = html.substring(idx2, idx3);
                            String instId = instStr;
                            doc.add(new Field("refId", instId, Field.Store.NO, Field.Index.UN_TOKENIZED));
                            idx2 = html.indexOf("</a>",idx3 + 1) + 4;
                            html = html.substring(idx2);
                        }
                    }
                }
            }
        }

        for (Shape shape : instance.getShapes()) {
            List<DataField> fields = shape.getFields();
            for (DataField field : fields)
            {
                String fieldName = "field" + field.getId();
                List<DataFieldValue> values = instance.getFieldValues(shape, field);
                for (DataFieldValue value : values)
                {
                    String data;
                    if (value.isInternal())
                    {
                        if (field.getShape().isNumeric())
                        { 
                            data = Tools.floatToNumeric(Float.parseFloat(value.getName()));
                            doc.add(new Field(fieldName, data, Field.Store.NO, Field.Index.UN_TOKENIZED));
                        }
                        else
                        {
                            data = value.getName();

                            if (field.getShape().getId().equals(Shape.RICHTEXT_ID) ||
                                    (shape.getId().equals(Shape.DASHBOARD_ID) && field.getShape().getId().equals(Shape.TEXT_ID))) {
                                // extract the text from the html
                                Parser parser = Parser.createParser(data, null);
                                TextExtractingVisitor visitor = new TextExtractingVisitor();
                                parser.visitAllNodesWith(visitor);
                                data = visitor.getExtractedText();

                                // look for references within the content
                                int idx;
                                String html = value.getName();
                                while (-1 != (idx = html.indexOf("<a href=\"chime://#detail:"))) {
                                    // find the instance id
                                    int idx2 = idx + 25;
                                    int idx3 = idx2 - 1;
                                    while (html.charAt(idx3) != '"') {
                                            idx3++;
                                    }
                                    String instStr = html.substring(idx2, idx3);
                                    String instId = instStr;
                                    doc.add(new Field("refId", instId, Field.Store.NO, Field.Index.UN_TOKENIZED));
                                    idx2 = html.indexOf("</a>",idx3 + 1) + 4;
                                    html = html.substring(idx2);
                                }
                            }

                            doc.add(new Field(fieldName, data, Field.Store.NO, Field.Index.TOKENIZED));
                        }

                        doc.add(new Field("content", data, Field.Store.NO, Field.Index.TOKENIZED));
                    }
                    else
                    {
                        data = String.valueOf(value.getReferenceId());
                        doc.add(new Field(fieldName + "Id", data, Field.Store.NO, Field.Index.UN_TOKENIZED));
                        doc.add(new Field("refId", data, Field.Store.NO, Field.Index.UN_TOKENIZED));
                    }
                }
            }
        }
        
        return doc;
    }
}


class OptimizerTask implements Runnable {
    private static final Logger _logger = Logger.getLogger(OptimizerTask.class);

    private int spindle;

    public OptimizerTask(int spindle) {
        this.spindle = spindle;
    }

    public void run() {
        long start = System.currentTimeMillis();

        try
        {
            String[] indexNames = Indexer.getIndexNames();
            String indexName = indexNames[spindle];

            File indexDir = new File("index/" + indexName);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriter writer = new IndexWriter(indexDir, analyzer);

            _logger.info("Optimizing " + indexName + "...");

            writer.optimize();
            writer.close();
        }
        catch (Exception e)
        {
            int x = 1;
        }

        long end = System.currentTimeMillis();
        //System.out.println("Optimized Index in " + (end - start) + " msecs");
    }
}

class IndexUpdater extends IndexerBase {
    private static final Logger _logger = Logger.getLogger(IndexUpdater.class);

    enum Type
    {
        Create,
        Modify,
        Delete
    }
    
    DataInstance _data;
    Type _type;
    
    public IndexUpdater(DataInstance data, Type type, DatabaseConnectionPool pool)
    {
        super(pool);
        _data = data;
        _type = type;
    }
    
    public void run()
    {
        long start = System.currentTimeMillis();
        DatabaseConnection database = _pool.borrowInstance(this);
        File indexDir = new File("index/" + Indexer.indexNameByInstance(_data));
        try 
        {
            StandardAnalyzer analyzer = new StandardAnalyzer();
            
            IndexWriter writer = new IndexWriter(indexDir, analyzer);
            Term term = new Term("instanceid", String.valueOf(_data.getId()));

            writer.deleteDocuments(term);

            if (_type != Type.Delete)
            {
                User user = new User();
                user.setId(User.SYSTEM);
                user.setIndexing(true);

                try {
                    DataInstance inst = DataInstanceUtils.getInstance(_data.getId(), user, database, true, false);
                    Document doc = buildDocument(inst, user, database);

                    if (_data.getShapes().get(0).getName().equals("File")) {
                        indexFileContents(doc, _data);
                    }

                    writer.addDocument(doc);
                } catch (Exception e) {
                    // we want to report the exception in the log
                }
            }
            
            //writer.optimize();
            writer.close();
        } 
        catch (Exception e) 
        {
            int x = 1;
        }

        _pool.returnInstance(database, this);
        
        long end = System.currentTimeMillis();
        _logger.info("Updated Index in " + (end - start) + " msecs");
    }
}

class IndexEventDeleter extends IndexerBase {
    private static final Logger _logger = Logger.getLogger(IndexEventDeleter.class);

    List<DataInstance> instances = new ArrayList<DataInstance>();

    public IndexEventDeleter(List<DataInstance> list, DatabaseConnectionPool pool)
    {
        super(pool);
        instances.addAll(list);
    }

    public void run()
    {
        long start = System.currentTimeMillis();
        DatabaseConnection database = _pool.borrowInstance(this);

        try {
            String[] indexNames = Indexer.getIndexNames();

            // the event index is the last one
            File indexDir = new File("index/" + indexNames[indexNames.length - 1]);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriter writer = new IndexWriter(indexDir, analyzer);

            for (DataInstance id : instances) {
                Term term = new Term("instanceid", id.getId().getValue());
                writer.deleteDocuments(term);
            }

            writer.close();
        } catch (Exception e) {

        }

        _pool.returnInstance(database, this);

        long end = System.currentTimeMillis();
        _logger.info("Updated Index in " + (end - start) + " msecs");
    }
}

class IndexBuilder extends IndexerBase {
    private static final Logger _logger = Logger.getLogger(IndexBuilder.class);

    DataInstance _type = null;
    User _user = null;
    boolean optimize = false;

    public IndexBuilder(boolean opt, User user, DatabaseConnectionPool pool)
    {
        super(pool);
        _user = user;
        optimize = opt;
    }
    
    public IndexBuilder(DataInstance type, DatabaseConnectionPool pool)
    {
        super(pool);
        _type = type;
    }
    
    public void run()
    {
        DatabaseConnection database = _pool.borrowInstance(this);
        
        try 
        {
            long start = System.currentTimeMillis();
            try
            {
                // delete everything in the index directory
                File indexRoot = new File("index");
                Tools.deleteDirectory(indexRoot);

                String[] indexNames = Indexer.getIndexNames();
                IndexWriter[] indexWriters = new IndexWriter[indexNames.length];
                for (int i = 0; i < indexNames.length; i++) {
                    File indexDir = new File("index/" + indexNames[i]);
                    StandardAnalyzer analyzer = new StandardAnalyzer();
                    IndexWriter writer = new IndexWriter(indexDir, analyzer);
                    indexWriters[i] = writer;
                }

                _logger.info("Rebuilding Index");
                int count = doIndex(indexWriters, database);

                if (optimize) {
                    _logger.info("Optimizing Index");
                    for (IndexWriter writer : indexWriters) {
                        writer.optimize();
                    }
                }

                for (IndexWriter writer : indexWriters) {
                    writer.close();
                }

                _logger.info("Done indexing " + count + " items");

            }
            catch (Exception e)
            {
               _logger.error("Indexing Exception", e);
            }

            long end = System.currentTimeMillis();
            _logger.info("System Indexed in " + (end - start) + " msecs");
        }
        catch (Exception e)
        {
            _logger.error(e);
        }
        
        _pool.returnInstance(database, this);

        Indexer.instance().setReady();
    }
    
    private int doIndex(IndexWriter[] writers, DatabaseConnection database) throws Exception
    {
        int cnt = 0;
        
        String sql = "select id from " + Tools.getTableSet() + " order by id";
        
        DataSet dataSet = database.getDataSet(sql, true);
        DatabaseConnection db = _pool.borrowInstance(this);
        while (dataSet.next())
        {
            IDataValue idVal = dataSet.getFieldValue("id");
            try
            {
                cnt++;

                DataInstance instance = DataInstanceUtils.getInstance(InstanceId.create(idVal.asString()), _user, db, true, true);

                Document doc = buildDocument(instance, _user, database);

                int idx = Indexer.indexByInstance(instance);
                IndexWriter writer = writers[idx];
                Term term = new Term("instanceid", String.valueOf(instance.getId()));
                writer.deleteDocuments(term);
                writer.addDocument(doc);

            }
            catch (Throwable t)
            {
                _logger.error(t);
            }
        }
        
        dataSet.close();
        return cnt;
    }

}

