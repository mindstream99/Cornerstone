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

package com.paxxis.chime.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.FindInstancesResponse;
import com.paxxis.chime.client.widgets.ChimeMessageBox;
import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.RequestMessage;

/**
 * 
 * @author Robert Englander
 */
public class DataInstanceStore extends ListStore<DataInstanceModel> implements QueryProvider
{
    private ChimeAsyncCallback<DataInstanceResponseObject> dataInstanceCallBack;
    private ChimeAsyncCallback<FindInstancesResponseObject> findInstancesCallBack;
    private DataStoreUpdateListener listener;
    private boolean _includeLinks;
    private int truncateDescriptions;
    private boolean isActive = true;

    public DataInstanceStore(DataStoreUpdateListener l, boolean includeLinks, int truncDescriptions)
    {
        this.
        _includeLinks = includeLinks;
        truncateDescriptions = truncDescriptions;
        listener = l;
        dataInstanceCallBack = new ChimeAsyncCallback<DataInstanceResponseObject>() {
            public void onSuccess(DataInstanceResponseObject resp) { 
                if (isActive) {
                    removeAll();

                    if (resp.isResponse()) {
                        final DataInstanceResponse response = resp.getResponse();
                        List<DataInstance> instances = response.getDataInstances();

                        boolean topRule = false;
                        final List<DataInstanceModel> models = new ArrayList<DataInstanceModel>();
                        for (DataInstance instance : instances) {
                           DataInstanceModel m = new DataInstanceModel(instance, _includeLinks, truncateDescriptions, topRule);
                           models.add(m);
                           topRule = true;
                        }

                        add(models);
                        notifyListener(response);
                    } else {
                        final ErrorMessage error = resp.getError();
                        if (error.getType() == ErrorMessage.Type.SessionExpiration) {
                            ServiceManager.reLogin(
                                new Runnable() {
                                    public void run() {
                                        RequestMessage rm = error.getRequest();
                                        if (rm != null && rm instanceof DataInstanceRequest) {
                                            query((DataInstanceRequest)rm);
                                        }
                                    }
                                },
                                new Runnable() {
                                    public void run() {
                                    }
                                }
                            );
                        } else {
                            String errorMsg = resp.getError().getMessage();
                            ChimeMessageBox.alert("Error", errorMsg, null);
                        }
                    }
                }
            }
        };

        findInstancesCallBack = new ChimeAsyncCallback<FindInstancesResponseObject>() {
            public void onSuccess(FindInstancesResponseObject resp) { 
                if (isActive) {
                    removeAll();
                    final FindInstancesResponse response = resp.getResponse();
                    List<DataInstance> instances = response.getDataInstances();

                    boolean topRule = false;
                    final List<DataInstanceModel> models = new ArrayList<DataInstanceModel>();
                    for (DataInstance instance : instances)
                    {
                       DataInstanceModel m = new DataInstanceModel(instance, _includeLinks, truncateDescriptions, topRule);
                       models.add(m);
                       topRule = true;
                    }

                    add(models);
                    commitChanges();

                    notifyListener(response);
                }
            }
        };
    }

    public void setActive(boolean val) {
        isActive = val;
    }
    
    protected void notifyListener(DataInstanceResponse resp)
    {
        if (listener != null)
        {
            listener.onDataInstanceResponse(resp);
        }
    }
    
    protected void notifyListener(FindInstancesResponse resp)
    {
        if (listener != null)
        {
            listener.onFindInstancesResponse(resp);
        }
    }
    
    public void query(DataInstanceRequest request)
    {
        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendDataInstanceRequest(request, dataInstanceCallBack);
    }
    
    public void query(FindInstancesRequest request)
    {
        request.setUser(ServiceManager.getActiveUser());
        ServiceManager.getService().sendFindInstancesRequest(request, findInstancesCallBack);
    }
}
