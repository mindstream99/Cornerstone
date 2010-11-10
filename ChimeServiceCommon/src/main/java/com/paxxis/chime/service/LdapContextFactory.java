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

package com.paxxis.chime.service;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
/**
*
* @author Robert Englander
*/
public class LdapContextFactory extends ChimeConfigurable {

	private String contextFactory = null;
	private String url = null;
	private String principalSuffix = null;
	private String authType = null;
	private boolean ldapEnabled = false;
	
	@SuppressWarnings("unchecked")
	public DirContext getContext(String loginId, String password){
		DirContext ctx = null;
		try {
			Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
            env.put(Context.PROVIDER_URL, url);

            env.put(Context.SECURITY_AUTHENTICATION, authType);
            env.put(Context.SECURITY_PRINCIPAL, loginId + principalSuffix);
            env.put(Context.SECURITY_CREDENTIALS, password);
            ctx = new InitialDirContext(env);
		} catch (Exception ex){
			throw new RuntimeException(ex);
		}
		return ctx;
	}
	
	public String getContextFactory() {
		return contextFactory;
	}
	public void setContextFactory(String contextFactory) {
		this.contextFactory = contextFactory;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPrincipalSuffix() {
		return principalSuffix;
	}
	public void setPrincipalSuffix(String principalSuffix) {
		this.principalSuffix = principalSuffix;
	}
	public String getAuthType() {
		return authType;
	}
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public boolean isLdapEnabled() {
		return ldapEnabled;
	}

	public void setLdapEnabled(boolean ldapEnabled) {
		this.ldapEnabled = ldapEnabled;
	}
}
