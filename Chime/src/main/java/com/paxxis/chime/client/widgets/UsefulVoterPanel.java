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

package com.paxxis.chime.client.widgets;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.Timer;

/**
 *
 * @author Robert Englander
 */
public class UsefulVoterPanel extends LayoutContainer {

    private boolean throttleUser = true;

    private IconButton yes;
    private IconButton no;

    private boolean yesEnabled = true;
    private boolean noEnabled = true;

    private InterceptedHtml label;
    private InterceptedHtml suffix;
    private String suffixText = "";
    private String promptText = null;

    private VoteListener listener;

    public interface VoteListener {
        public void onVote(boolean positive);
    }

    public UsefulVoterPanel(String txt, VoteListener listener) {
        this(txt, listener, null);
    }

    public UsefulVoterPanel(String txt, VoteListener listener, String userPrompt) {
        this.listener = listener;
        promptText = userPrompt;
        init(txt);
    }

    /*
    public UsefulVoterPanel(String txt, String suffixText,
            boolean yesEnabled, boolean noEnabled, VoteListener listener) {
        this.listener = listener;
        this.suffixText = suffixText;
        this.yesEnabled = yesEnabled;
        this.noEnabled = noEnabled;
        throttleUser = false;
        init(txt);
    }
    */
    
    public void updatePrefix(String txt) {
        label.setHtml("<span class=\"useful-voter\">" + txt + "&nbsp;&nbsp;</span>");
    }

    public void updateSuffix(String txt, boolean yesEnabled, boolean noEnabled) {
        throttleUser = false;
        suffixText = txt;
        suffix.setHtml("<span class=\"useful-voter\">&nbsp;&nbsp;" + txt + "</span>");
        this.yesEnabled = yesEnabled;
        this.noEnabled = noEnabled;

        if (yesEnabled) {
            yes.changeStyle("yes-icon");
        } else {
            yes.changeStyle("yesDisabled-icon");
        }

        if (noEnabled) {
            no.changeStyle("no-icon");
        } else {
            no.changeStyle("noDisabled-icon");
        }
    }

    private void afterVote() {
        if (throttleUser) {
            Timer t = new Timer() {
                @Override
                public void run() {
                    yes.changeStyle("yes-icon");
                    no.changeStyle("no-icon");
                    updateSuffix(suffixText, yesEnabled, noEnabled);
                    throttleUser = true;
                }
            };

            yes.changeStyle("yesDisabled-icon");
            no.changeStyle("noDisabled-icon");
            suffix.setHtml("<span class=\"useful-voter-red\">&nbsp;&nbsp;Thank you for contributing!</span>");
            t.schedule(5000);
        }
    }

    private void promptUser(final boolean vote) {
        if (promptText != null) {
            Listener<ChimeMessageBoxEvent> l = new Listener<ChimeMessageBoxEvent>() {
                public void handleEvent(ChimeMessageBoxEvent evt) {
                    Button btn = evt.getButtonClicked();
                    if (btn != null) {
                        if (btn.getText().equalsIgnoreCase("yes")) {
                            applyVote(vote);
                        }
                    }
                }
            };

            ChimeMessageBox.confirm("Apply Vote", promptText, l);
        } else {
            applyVote(vote);
        }
    }

    private void applyVote(boolean vote) {
        listener.onVote(vote);
        afterVote();
    }

    private void init(String txt) {
        if (yesEnabled) {
            yes = new IconButton("yes-icon");
        } else {
            yes = new IconButton("yesDisabled-icon");
        }

        yes.setWidth(32);
        yes.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    promptUser(true);
                }
            }
        );

        if (noEnabled) {
            no = new IconButton("no-icon");
        } else {
            no = new IconButton("noDisabled-icon");
        }

        no.setWidth(32);
        no.addSelectionListener(
            new SelectionListener<IconButtonEvent>()
            {
                @Override
                public void componentSelected(IconButtonEvent evt) {
                    promptUser(false);
                } 
            }
        );

        label = new InterceptedHtml();
        suffix = new InterceptedHtml();

        TableRowLayout layout = new TableRowLayout();
        layout.setColumns(3);
        setLayout(layout);

        label.setHtml("<span class=\"useful-voter\">" + txt + "&nbsp;&nbsp;</span>");

        add(label, new TableData()); //RowData(-1, -1, new Margins(5)));
        add(yes, new TableData()); //RowData(-1, -1, new Margins(5))); //, new RowData());
        add(no, new TableData()); //RowData(-1, -1, new Margins(5))); //, new RowData());
        add(suffix, new TableData()); //RowData(-1, -1, new Margins(5)));
        layout();
    }
}
