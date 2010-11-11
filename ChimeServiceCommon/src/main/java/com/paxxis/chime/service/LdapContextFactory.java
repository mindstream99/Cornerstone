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
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.paxxis.chime.client.common.Shape;
import com.paxxis.chime.client.common.User;
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
	private String baseDN = null;
	private boolean autoCreate = false;
	
	private static final String NAME_ATTR = "displayName";
	private static final String EMAIL_ATTR = "mail";
	private static final String DEPT_ATTR = "department";
	private static final String LOGINID_ATTR = "sAMAccountName";
	
	
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
			throw new RuntimeException("The login id and/or password are not valid");
		}
		return ctx;
	}
	
	public User getLdapUser(String loginId, String password, Shape userShape) {
		User user=null;
		try {
			DirContext ctx = this.getContext(loginId, password);

			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String[] attrIDs = {NAME_ATTR, EMAIL_ATTR, DEPT_ATTR};
			constraints.setReturningAttributes(attrIDs);
			NamingEnumeration answer = ctx.search(baseDN , LOGINID_ATTR + "=" + loginId, constraints);
			if (answer.hasMore()) {
				user = new User();
				user.addShape(userShape);
				Attributes attrs = ((SearchResult) answer.next()).getAttributes();
				user.setFieldValues(User.LOGINID, loginId);
				user.setName(attrs.get(NAME_ATTR).get().toString());				
				user.setFieldValues(User.EMAILADDR_FIELD, attrs.get(EMAIL_ATTR).get().toString());
			} else {
				throw new Exception("Invalid User");
			}
		} catch (Exception ex) {
			throw new RuntimeException("New Chime user: The login id and/or password are not valid.");
		}
		return user;
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
	
	public String getBaseDN() {
		return baseDN;
	}

	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}

	public boolean isAutoCreate() {
		return autoCreate;
	}

	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}
}
