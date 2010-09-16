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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.ChimeDialogManager;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceManagerAdapter;
import com.paxxis.chime.client.ServiceManagerListener;

/**
 *
 * @author Robert Englander
 */
public class LoginPanel extends ContentPanel
{
    private Html _info;
    private InterceptedHtml _notRegisteredMessage;

    private ButtonBar buttonBar;
    private Button _okButton;
    private TextField<String> _userName;
    private TextField<String> _password;
    private boolean _isActive = false;
    private boolean _isInfoMode = false;
    private String _infoMessage = null;
    private boolean _logoutOnCancel = false;

    private FormPanel _form = new FormPanel();

    private Runnable _loginTask = null;
    private Runnable _cancelTask = null;

    private ServiceManagerListener _serviceManagerListener = null;

    public LoginPanel()
    {
        super();
        init(false);
    }

    public void onRender(Element parent, int pos)
    {
        super.onRender(parent, pos);
    }

    private void init(boolean relogin)
    {
        _form.setHeaderVisible(false);
        _form.setBorders(false);
        _form.setBodyBorder(false);
        _form.setStyleAttribute("padding", "5");
        _form.setButtonAlign(HorizontalAlignment.CENTER);
        _form.setFrame(false);
        _form.setFieldWidth(225);
        _form.setLabelWidth(85);

        _isInfoMode = (_infoMessage != null);

        setHeading("Login");
        setShadow(true);
        setWidth(370);

        if (_isInfoMode) {
            if (relogin) {
                _info = new Html("<div id='endslice-form-label'>" + _infoMessage + "&nbsp;&nbsp;Do you want to login as a different user?</div>");
            } else {
                _info = new Html("<div id='endslice-form-label'>" + _infoMessage + "&nbsp;&nbsp;Do you want to login now?</div>");
            }

            _form.add(_info);
        }


        _userName = new TextField<String>();
        _userName.setFieldLabel("User Name");
        _userName.setVisible(!_isInfoMode);

        new KeyNav(_userName)
        {
            public void onKeyPress(final ComponentEvent ce)
            {
                updateState();
            }

            public void onEnter(ComponentEvent ce)
            {
                doLogin();
            }
        };

        _form.add(_userName);

        _password = new TextField<String>();
        _password.setPassword(true);
        _password.setFieldLabel("Password");
        _password.setVisible(!_isInfoMode);

        new KeyNav(_password)
        {
            public void onKeyPress(final ComponentEvent cd)
            {
                updateState();
            }

            public void onEnter(ComponentEvent ce)
            {
                doLogin();
            }
        };

        _form.add(_password);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(HorizontalAlignment.CENTER);

        _okButton = new Button("Ok");
        _okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent evt)
                {
                    if (_isInfoMode)
                    {
                        setLoginMode();
                    }
                    else
                    {
                        doLogin();
                    }
                }
            }
        );

        buttonBar.add(_okButton);

        _form.add(buttonBar);

        _serviceManagerListener = new ServiceManagerAdapter()
        {
            public void onLoginResponse(LoginResponseObject resp)
            {
                if (_isActive)
                {
                    _isActive = false;
                    handleLoginResponse(resp);
                }
            }
        };

        ServiceManager.addListener(_serviceManagerListener);
        add(_form);

        setButtonLabels();

        _userName.setValue("");
        _password.setValue("");

        _userName.focus();

        updateState();
    }

    private void setLoginMode()
    {
        _isInfoMode = false;
        setButtonLabels();
        _info.setHtml("");
        _info.setVisible(false);
        _userName.setVisible(true);
        _password.setVisible(true);


        layout();
        updateState();
        _userName.focus();
    }

    private void setButtonLabels()
    {
        if (_isInfoMode)
        {
            _okButton.setText("Yes");
        }
        else
        {
            _okButton.setText("Ok");
        }
    }

    private void relaunch()
    {
        _userName.selectAll();
        _userName.focus();
    }

    protected void updateState()
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    if (_isInfoMode)
                    {
                        _okButton.setEnabled(true);
                    }
                    else
                    {
                        _okButton.setEnabled(canLogin());
                    }
                }
            }
        );
    }

    protected boolean canLogin()
    {
        String name = _userName.getRawValue();
        String pw = _password.getRawValue();
        boolean canLogin = false;

        if (name != null && pw != null)
        {
            canLogin = (name.trim().length() > 0 && pw.trim().length() > 0);
        }

        return canLogin;
    }

    protected void handleLoginResponse(final LoginResponseObject resp)
    {
        DeferredCommand.addCommand(
            new Command()
            {
                public void execute()
                {
                    if (resp.isResponse())
                    {
                        ChimeDialogManager.instance().clear();
                        _password.setRawValue("");
                        updateState();
                    }
                    else
                    {
                        final Listener<ChimeMessageBoxEvent> cb = new Listener<ChimeMessageBoxEvent>()
                        {
                            public void handleEvent(ChimeMessageBoxEvent evt)
                            {
                                relaunch();
                            }
                        };

                        ChimeMessageBox.alert("Login Failed", resp.getError().getMessage(), cb);
                    }
                }
            }
        );
    }

    protected void doLogin()
    {
        if (canLogin())
        {
            String name = _userName.getValue().toString();
            String pw = _password.getValue().toString();
            _isActive = true;
            ServiceManager.login(name, pw);
        }
    }

}
