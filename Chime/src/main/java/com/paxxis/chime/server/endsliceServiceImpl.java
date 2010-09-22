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

package com.paxxis.chime.server;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.paxxis.chime.client.AddCommentResponseHandler;
import com.paxxis.chime.client.ApplyRatingResponseHandler;
import com.paxxis.chime.client.ApplyTagResponseHandler;
import com.paxxis.chime.client.ApplyVoteResponseHandler;
import com.paxxis.chime.client.BrandingData;
import com.paxxis.chime.client.ChimeClient;
import com.paxxis.chime.client.CommentsResponseHandler;
import com.paxxis.chime.client.CommentsResponseObject;
import com.paxxis.chime.client.CreateDiscussionResponseHandler;
import com.paxxis.chime.client.DataInstanceResponseHandler;
import com.paxxis.chime.client.DataInstanceResponseObject;
import com.paxxis.chime.client.DiscussionsResponseHandler;
import com.paxxis.chime.client.DiscussionsResponseObject;
import com.paxxis.chime.client.EditCommunityResponseHandler;
import com.paxxis.chime.client.EditInstanceResponseHandler;
import com.paxxis.chime.client.EditShapeResponseHandler;
import com.paxxis.chime.client.EditUserResponseHandler;
import com.paxxis.chime.client.EventWrapper;
import com.paxxis.chime.client.FindInstancesResponseHandler;
import com.paxxis.chime.client.FindInstancesResponseObject;
import com.paxxis.chime.client.LockResponseHandler;
import com.paxxis.chime.client.LoginResponseHandler;
import com.paxxis.chime.client.LoginResponseObject;
import com.paxxis.chime.client.LogoutResponseHandler;
import com.paxxis.chime.client.LogoutResponseObject;
import com.paxxis.chime.client.MultiResponseHandler;
import com.paxxis.chime.client.PingResponseHandler;
import com.paxxis.chime.client.RatingsResponseHandler;
import com.paxxis.chime.client.RatingsResponseObject;
import com.paxxis.chime.client.RunCALScriptResponseHandler;
import com.paxxis.chime.client.ServiceResponseObject;
import com.paxxis.chime.client.ShapeResponseHandler;
import com.paxxis.chime.client.ShapeResponseObject;
import com.paxxis.chime.client.SubscribeResponseHandler;
import com.paxxis.chime.client.UserMessagesResponseHandler;
import com.paxxis.chime.client.endsliceService;
import com.paxxis.chime.client.common.AddCommentRequest;
import com.paxxis.chime.client.common.AddCommentResponse;
import com.paxxis.chime.client.common.ApplyReviewRequest;
import com.paxxis.chime.client.common.ApplyReviewResponse;
import com.paxxis.chime.client.common.ApplyTagRequest;
import com.paxxis.chime.client.common.ApplyTagResponse;
import com.paxxis.chime.client.common.ApplyVoteRequest;
import com.paxxis.chime.client.common.ApplyVoteResponse;
import com.paxxis.chime.client.common.CommentsRequest;
import com.paxxis.chime.client.common.CommentsResponse;
import com.paxxis.chime.client.common.CreateDiscussionRequest;
import com.paxxis.chime.client.common.CreateDiscussionResponse;
import com.paxxis.chime.client.common.DataInstanceEvent;
import com.paxxis.chime.client.common.DataInstanceRequest;
import com.paxxis.chime.client.common.DataInstanceResponse;
import com.paxxis.chime.client.common.DiscussionsRequest;
import com.paxxis.chime.client.common.DiscussionsResponse;
import com.paxxis.chime.client.common.EditCommunityRequest;
import com.paxxis.chime.client.common.EditCommunityResponse;
import com.paxxis.chime.client.common.EditDataInstanceRequest;
import com.paxxis.chime.client.common.EditDataInstanceResponse;
import com.paxxis.chime.client.common.EditShapeRequest;
import com.paxxis.chime.client.common.EditShapeResponse;
import com.paxxis.chime.client.common.EditUserRequest;
import com.paxxis.chime.client.common.EditUserResponse;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.FindInstancesRequest;
import com.paxxis.chime.client.common.FindInstancesResponse;
import com.paxxis.chime.client.common.LockRequest;
import com.paxxis.chime.client.common.LockResponse;
import com.paxxis.chime.client.common.LoginRequest;
import com.paxxis.chime.client.common.LoginResponse;
import com.paxxis.chime.client.common.LogoutRequest;
import com.paxxis.chime.client.common.LogoutResponse;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.MultiRequest;
import com.paxxis.chime.client.common.MultiResponse;
import com.paxxis.chime.client.common.PingRequest;
import com.paxxis.chime.client.common.PingResponse;
import com.paxxis.chime.client.common.ReviewsRequest;
import com.paxxis.chime.client.common.ReviewsResponse;
import com.paxxis.chime.client.common.RunCALScriptRequest;
import com.paxxis.chime.client.common.RunCALScriptResponse;
import com.paxxis.chime.client.common.ShapeRequest;
import com.paxxis.chime.client.common.ShapeResponse;
import com.paxxis.chime.client.common.SubscribeRequest;
import com.paxxis.chime.client.common.SubscribeResponse;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessagesRequest;
import com.paxxis.chime.client.common.UserMessagesResponse;
import com.paxxis.chime.common.JavaObjectPayload;
import com.paxxis.chime.server.ServiceBusSenderPool.PoolEntry;
import com.paxxis.chime.service.JndiInitialContextFactory;
import com.paxxis.chime.service.ServiceBusConnector;
import com.paxxis.chime.service.ServiceBusMessageReceiver;

import de.novanic.eventservice.service.RemoteEventServiceServlet;

/** 
 *
 * @author Robert Englander
 */
public class endsliceServiceImpl extends RemoteEventServiceServlet implements endsliceService {
    JndiInitialContextFactory _contextFactory;
    ServiceBusSenderPool senderPool;
    private ServiceBusMessageReceiver receiver;
    private ServiceBusConnector connector;
    private UpdateEventHandler handler; 
    private Object lock = new Object();
    private boolean initialized = false;
    private BrandingData brandingData = new BrandingData();
    
    
    @Override
    public void destroy() {
        senderPool.shutdown();
        super.destroy();
    }

	private static final long serialVersionUID = 1L;

	public BrandingData getBrandingData() {
		synchronized (lock) {
			Logger.getLogger(endsliceServiceImpl.class).info("Returning branding data: " + brandingData.getHeading());
			return brandingData;
		}
	}
	
	public boolean isReady() {
		boolean ready = false;
		synchronized (lock) {
			ready = (initialized && connector != null && connector.isConnected());
		}
		
		return ready;
	}

	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        initialize();
	}
	
	@Override
	public void initialize() {
        
		try {
			synchronized (lock) {
				if (!initialized) {
					try {
				        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			            DocumentBuilder db = dbf.newDocumentBuilder();
			            Document doc = db.parse("./ChimeBranding.xml");
			            Element root = doc.getDocumentElement();
			            NodeList list = root.getElementsByTagName("heading");
			            if (list.getLength() > 0) {
			            	Node node = list.item(0);
	                        String title = node.getAttributes().getNamedItem("title").getNodeValue();
			            	brandingData.setHeading(title);
			            }
					} catch (Exception e) {
						Logger.getLogger(endsliceServiceImpl.class).error("Failed to read branding file");
						Logger.getLogger(endsliceServiceImpl.class).error(e.getCause().getLocalizedMessage());
					}

					_contextFactory = new JndiInitialContextFactory();

			        _contextFactory.setContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			        _contextFactory.setProviderUrl("tcp://localhost:61616");      //"discovery://(multicast://default)");

			        senderPool = new ServiceBusSenderPool(10, _contextFactory, "chimeFactory", "ChimeRequestQueue");

			        connector = new ServiceBusConnector();
			        receiver = new ServiceBusMessageReceiver();
			        connector.addServiceBusConnectorClient(receiver);
			        connector.setInitialContextFactory(_contextFactory);
			        connector.setConnectionFactoryName("chimeFactory");
			        receiver.setDestinationName("ChimeUpdateTopic");
			        handler = new UpdateEventHandler(
			        	new UpdateEventListener() {

							@Override
							public void onDataInstanceUpdate(DataInstanceEvent event) {
								EventWrapper<DataInstanceEvent> evt = new EventWrapper<DataInstanceEvent>(event);
						    	addEvent(EventWrapper.DOMAIN, evt);
							}

			        	}
			        );
			        receiver.setMessageHandler(handler);
			        connector.connect();
			        initialized = true;
				}
			}
		} catch (Exception e) {
			throw new RemoteException(e.getCause());
		}
    }
    
    public ShapeResponseObject sendShapeRequest(ShapeRequest request)
    {
        ShapeResponseObject obj = new ShapeResponseObject();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);
        Message response = client.execute(request, new ShapeResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof ShapeResponse)
        {
            ShapeResponse resp = (ShapeResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<PingResponse> sendPingRequest(PingRequest request) 
    {
        ServiceResponseObject<PingResponse> obj = new ServiceResponseObject<PingResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new PingResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof PingResponse)
        {
            PingResponse resp = (PingResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    } 

    public ServiceResponseObject<LockResponse> sendLockRequest(LockRequest request)
    {
        ServiceResponseObject<LockResponse> obj = new ServiceResponseObject<LockResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new LockResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof LockResponse)
        {
            LockResponse resp = (LockResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<SubscribeResponse> sendSubscribeRequest(SubscribeRequest request)
    {
        ServiceResponseObject<SubscribeResponse> obj = new ServiceResponseObject<SubscribeResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new SubscribeResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof SubscribeResponse)
        {
            SubscribeResponse resp = (SubscribeResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<RunCALScriptResponse> sendRunCALScriptRequest(RunCALScriptRequest request)
    {
        ServiceResponseObject<RunCALScriptResponse> obj = new ServiceResponseObject<RunCALScriptResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new RunCALScriptResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof RunCALScriptResponse)
        {
            RunCALScriptResponse resp = (RunCALScriptResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<EditCommunityResponse> sendEditCommunityRequest(EditCommunityRequest request)
    {
        ServiceResponseObject<EditCommunityResponse> obj = new ServiceResponseObject<EditCommunityResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new EditCommunityResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof EditCommunityResponse)
        {
            EditCommunityResponse resp = (EditCommunityResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<EditUserResponse> sendEditUserRequest(EditUserRequest request)
    {
        ServiceResponseObject<EditUserResponse> obj = new ServiceResponseObject<EditUserResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new EditUserResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof EditUserResponse)
        {
            EditUserResponse resp = (EditUserResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<MultiResponse> sendMultiRequest(MultiRequest request) 
    {
        ServiceResponseObject<MultiResponse> obj = new ServiceResponseObject<MultiResponse>();
        
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new MultiResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof MultiResponse)
        {
            MultiResponse resp = (MultiResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    } 

    public ServiceResponseObject<EditDataInstanceResponse> sendEditDataInstanceRequest(EditDataInstanceRequest request)
    {
        ServiceResponseObject<EditDataInstanceResponse> obj = new ServiceResponseObject<EditDataInstanceResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new EditInstanceResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof EditDataInstanceResponse)
        {
            EditDataInstanceResponse resp = (EditDataInstanceResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<EditShapeResponse> sendEditDataTypeRequest(EditShapeRequest request)
    {
        ServiceResponseObject<EditShapeResponse> obj = new ServiceResponseObject<EditShapeResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new EditShapeResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof EditShapeResponse)
        {
            EditShapeResponse resp = (EditShapeResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<ApplyTagResponse> sendApplyTagRequest(ApplyTagRequest request) 
    {
        ServiceResponseObject<ApplyTagResponse> obj = new ServiceResponseObject<ApplyTagResponse>();
        
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new ApplyTagResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof ApplyTagResponse)
        {
            ApplyTagResponse resp = (ApplyTagResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    }
    
    public ServiceResponseObject<ApplyReviewResponse> sendApplyRatingRequest(ApplyReviewRequest request)
    {
        ServiceResponseObject<ApplyReviewResponse> obj = new ServiceResponseObject<ApplyReviewResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new ApplyRatingResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof ApplyReviewResponse)
        {
            ApplyReviewResponse resp = (ApplyReviewResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<CreateDiscussionResponse> sendCreateDiscussionRequest(CreateDiscussionRequest request) {
        ServiceResponseObject<CreateDiscussionResponse> obj = new ServiceResponseObject<CreateDiscussionResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new CreateDiscussionResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof CreateDiscussionResponse)
        {
            CreateDiscussionResponse resp = (CreateDiscussionResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<ApplyVoteResponse> sendApplyVoteRequest(ApplyVoteRequest request)
    {
        ServiceResponseObject<ApplyVoteResponse> obj = new ServiceResponseObject<ApplyVoteResponse>();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new ApplyVoteResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof ApplyVoteResponse)
        {
            ApplyVoteResponse resp = (ApplyVoteResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public ServiceResponseObject<AddCommentResponse> sendAddCommentRequest(AddCommentRequest request) 
    {
        ServiceResponseObject<AddCommentResponse> obj = new ServiceResponseObject<AddCommentResponse>();
        
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new AddCommentResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof AddCommentResponse)
        {
            AddCommentResponse resp = (AddCommentResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    }
    
    public ServiceResponseObject<UserMessagesResponse> sendUserMessagesRequest(UserMessagesRequest request)  {
        ServiceResponseObject<UserMessagesResponse> obj = new ServiceResponseObject<UserMessagesResponse>();
        
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new UserMessagesResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof UserMessagesResponse)
        {
        	UserMessagesResponse resp = (UserMessagesResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    }
    
    public RatingsResponseObject sendRatingsRequest(ReviewsRequest request)
    {
        RatingsResponseObject obj = new RatingsResponseObject();
        
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new RatingsResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof ReviewsResponse)
        {
            ReviewsResponse resp = (ReviewsResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    }

    public CommentsResponseObject sendCommentsRequest(CommentsRequest request)
    {
        CommentsResponseObject obj = new CommentsResponseObject();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new CommentsResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof CommentsResponse)
        {
            CommentsResponse resp = (CommentsResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public DiscussionsResponseObject sendDiscussionsRequest(DiscussionsRequest request)
    {
        DiscussionsResponseObject obj = new DiscussionsResponseObject();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new DiscussionsResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof DiscussionsResponse)
        {
            DiscussionsResponse resp = (DiscussionsResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }

        return obj;
    }

    public DataInstanceResponseObject sendDataInstanceRequest(DataInstanceRequest request) 
    {
        HttpServletRequest req = getThreadLocalRequest();
        HttpSession session = req.getSession();

        DataInstanceResponseObject obj = new DataInstanceResponseObject();

        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new DataInstanceResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof DataInstanceResponse)
        {
            DataInstanceResponse resp = (DataInstanceResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    }

    public FindInstancesResponseObject sendFindInstancesRequest(FindInstancesRequest request) 
    {
        FindInstancesResponseObject obj = new FindInstancesResponseObject();
        
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        Message response = client.execute(request, new FindInstancesResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof FindInstancesResponse)
        {
            FindInstancesResponse resp = (FindInstancesResponse)response;
            obj.setResponse(resp);
        }
        else if (response instanceof ErrorMessage)
        {
            ErrorMessage resp = (ErrorMessage)response;
            obj.setError(resp);
        }
        else
        {
            obj.setResponse(null);
        }
        
        return obj;
    }
    
    public LoginResponseObject login(String name, String password)
    {
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        LoginRequest request = new LoginRequest();
        request.setUserName(name);
        request.setPassword(password);
     
        LoginResponseObject responseObject = new LoginResponseObject();
        
        Message response = client.execute(request, new LoginResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof LoginResponse)
        {
            LoginResponse resp = (LoginResponse)response;

            responseObject.setResponse(resp);
        }
        else
        {
            responseObject.setError((ErrorMessage)response);
        }
        
        return responseObject;
    }
    
    public LoginResponseObject login(User user)
    {
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);

        LoginRequest request = new LoginRequest();
        request.setUser(user);
     
        LoginResponseObject responseObject = new LoginResponseObject();
        
        Message response = client.execute(request, new LoginResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof LoginResponse)
        {
            LoginResponse resp = (LoginResponse)response;

            responseObject.setResponse(resp);
        }
        else
        {
            responseObject.setError((ErrorMessage)response);
        }
        
        return responseObject;
    }
    
    public LogoutResponseObject logout(User user)
    {
        PoolEntry entry = senderPool.borrowInstance(this);
        ChimeClient client = new ChimeClient(new JavaObjectPayload(), entry.getSender(), 30000);
        LogoutRequest request = new LogoutRequest();
        request.setUser(user);
     
        LogoutResponseObject responseObject = new LogoutResponseObject();
        
        Message response = client.execute(request, new LogoutResponseHandler());
        senderPool.returnInstance(entry, this);

        if (response instanceof LogoutResponse)
        {
            LogoutResponse resp = (LogoutResponse)response;

            responseObject.setResponse(resp);
        }
        else
        {
            responseObject.setError((ErrorMessage)response);
        }
        
        return responseObject;
    }
}
