package com.paxxis.chime.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class ChimeAsyncCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable t) {
		ServiceManager.handleServiceError(t);
	}

	@Override
	public abstract void onSuccess(T result);

}
