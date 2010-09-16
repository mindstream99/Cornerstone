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
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.paxxis.chime.client.ServiceManager;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.common.EditUserRequest;
import com.paxxis.chime.client.common.EditUserResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.EditUserRequest.Operation;

/**
 * @author Robert Englander
 *
 */
public class PasswordWindow extends ChimeWindow {

	public interface PasswordChangeListener {
		public void onChange(User user);
	}
	
	private Button okButton;
	private Button cancelButton;
    private TextField<String> oldPassword;
    private TextField<String> newPassword;
    private TextField<String> confirmPassword;
    private FormPanel form;
    private Html errorLabel;
    private User user;
    private boolean needOldPassword;
    private PasswordChangeListener listener;
    
    public PasswordWindow(User user, PasswordChangeListener l) {
    	super();
    	
    	this.user = user;
    	listener = l;
    	
        setModal(true);
        setHeading("Change Password");
        setMaximizable(false);
        setMinimizable(false);
        setClosable(false);
        setResizable(false);
        setShadow(false);
        setWidth(370);
    }
    
	/* (non-Javadoc)
	 * @see com.paxxis.chime.client.widgets.ChimeWindow#init()
	 */
	@Override
	protected void init() {
		form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setStyleAttribute("padding", "5");
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setFrame(false);
        form.setFieldWidth(175);
        form.setLabelWidth(125);
        
        // the admin doesn't have to enter the existing password
        needOldPassword = !ServiceManager.getActiveUser().isAdmin();
        if (needOldPassword) {
        	oldPassword = new TextField<String>();
        	oldPassword.setFieldLabel("Old Password");
        	oldPassword.setPassword(true);
            form.add(oldPassword);
            
            new KeyNav(oldPassword) {
            	public void onKeyPress(ComponentEvent evt) {
            		validate();
            	}
            	
            	public void onEnter(ComponentEvent evt) {
            		if (okButton.isEnabled()) {
            			updatePassword();
            		}
            	}
            };
        }

        newPassword = new TextField<String>();
        newPassword.setFieldLabel("New Password");
        newPassword.setPassword(true);
        form.add(newPassword);
        
        new KeyNav(newPassword) {
        	public void onKeyPress(ComponentEvent evt) {
        		validate();
        	}
        	
        	public void onEnter(ComponentEvent evt) {
        		if (okButton.isEnabled()) {
        			updatePassword();
        		}
        	}
        };

        confirmPassword = new TextField<String>();
        confirmPassword.setFieldLabel("Confirm Password");
        confirmPassword.setPassword(true);
        form.add(confirmPassword);
        
        new KeyNav(confirmPassword) {
        	public void onKeyPress(ComponentEvent evt) {
        		validate();
        	}
        	
        	public void onEnter(ComponentEvent evt) {
        		if (okButton.isEnabled()) {
        			updatePassword();
        		}
        	}
        };

        errorLabel = new Html("<div id='endslice-error-label'>&nbsp;</div>");
        form.add(errorLabel);
        
        okButton = new Button("Ok");
        okButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                        updatePassword();
                }
            }
        );

        form.addButton(okButton);
        
        cancelButton = new Button("Cancel");
        cancelButton.addSelectionListener(
            new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent evt) {
                    hide();
                }
            }
        );

        form.addButton(cancelButton);
        
        add(form);
        validate();
	}

	protected void validate() {
		DeferredCommand.addCommand(
			new Command() {
				public void execute() {
					String errorMessage = "&nbsp;";
					boolean enable = true;
					
					if (needOldPassword) {
						String old = oldPassword.getRawValue();
						if (old == null || old.trim().length() == 0) {
							errorMessage = "Please enter old password.";
							enable = false;
						}
					}
					
					if (enable) {
						String newPw = newPassword.getRawValue();
						String confirmPw = confirmPassword.getRawValue();
						if (newPw == null || confirmPw == null) {
							enable = false;
						} else {
							enable = newPw.trim().equals(confirmPw.trim());
							if (!enable) {
								errorMessage = "New password does not match confirmation password.";
							} else {
								if (newPw.length() < 5) {
									enable = false;
									errorMessage = "New password must be at least 5 characters long.";
								}
							}
						}
					}

					errorLabel.setHtml("<div id='endslice-error-label'>" + errorMessage + "</div>");
					okButton.setEnabled(enable);
				}
			}
		);
	}
	
	protected void updatePassword() {
        final AsyncCallback callback = new AsyncCallback()
        {
            public void onFailure(Throwable arg0)
            {
                ChimeMessageBox.alert("System Error", "Please contact the system administrator.", null);
            }

            public void onSuccess(Object obj)
            {
                ServiceResponseObject<EditUserResponse> response = (ServiceResponseObject<EditUserResponse>)obj;
                if (response.isResponse())
                {
                	User newUser = response.getResponse().getUser();
                	if (listener != null) {
                		listener.onChange(newUser);
                	}
                	
                	hide();
                    ChimeMessageBox.info("Chime", "Password has been changed.", null);
                }
                else
                {
                    ChimeMessageBox.alert("Error", response.getError().getMessage(), null);
                }
            }
        };

        EditUserRequest request = new EditUserRequest();
        request.setUser(ServiceManager.getActiveUser());
        request.setData(user);
        request.setOperation(Operation.Modify);
        request.setPassword(newPassword.getRawValue().trim());
        
        if (needOldPassword) {
            request.setOldPassword(oldPassword.getRawValue().trim());
        }

        ServiceManager.getService().sendEditUserRequest(request, callback);
	}
}
