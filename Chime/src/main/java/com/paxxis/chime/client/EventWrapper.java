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

import com.paxxis.chime.client.common.Message;

import java.io.Serializable;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
/**
 * 
 * @author Robert Englander
 *
 */
public class EventWrapper<D extends Message> implements Event, Serializable {

	public transient static final Domain DOMAIN = DomainFactory.getDomain("vibrant");
	
	private static final long serialVersionUID = 1L;

	private D message;

	public EventWrapper() {
	}

	public EventWrapper(D msg) {
		message = msg;
	}
	
	public D getMessage() {
		return message;
	}
}
