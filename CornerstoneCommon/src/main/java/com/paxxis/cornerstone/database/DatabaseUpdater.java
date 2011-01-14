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

package com.paxxis.cornerstone.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.paxxis.cornerstone.database.DataSet;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;

/**
 * 
 * @author Rob Englander
 *
 */
public class DatabaseUpdater {

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String UPDATES = "updates";
	private static final String UPDATE = "update";
	private static final String TARGET = "target";
	private static final String SQL = "sql";
	private static final String VERSION = "version";
	private static final String DATABASE = "database";
	private static final String DEPENDSONNAME = "depends-on-name";
	private static final String DEPENDSONID = "depends-on-id";
	private static final String DEPENDSONVERSION = "depends-on-version";
	private static final String SCHEMA_PLACEHOLDER = "%schema%";

	static class Update implements Comparable<Update> {
		private int version;
		private List<String> sql = new ArrayList<String>();
		private String catalog;
		private String id;
		private String dependsOnId = null;
		private String dependsOnName;
		private int dependsOnVersion;
		
		public Update(int version, List<String> sql, String catalog, String id) {
			this.version = version;
			this.sql.addAll(sql);
			this.catalog = catalog;
			this.id = id;
		}
		
		public int getVersion() {
			return version;
		}
		
		public List<String> getSql() {
			return sql;
		}
		
		public String getCatalog() {
			return catalog;
		}

		public String getId() {
			return id;
		}
		
		public void setDependency(String name, String id, int version) {
			dependsOnName = name;
			dependsOnId = id;
			dependsOnVersion = version;
			
		}
		
		public boolean hasDependency() {
			return dependsOnId != null;
		}

		public String getDependsOnName() {
			return dependsOnName;
		}

		public String getDependsOnId() {
			return dependsOnId;
		}

		public int getDependsOnVersion() {
			return dependsOnVersion;
		}
		
		@Override
		public int compareTo(Update other) {
			return version - other.version;
		}
	}
	
	private DatabaseConnectionPool pool = null;
	
	public DatabaseUpdater() {
	}

	public void setConnectionPool(DatabaseConnectionPool pool) {
		this.pool = pool;
	}
	
	public void update(String updaterFileName, String target) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DatabaseConnection database = pool.borrowInstance(this);
        
        try {
        	database.startTransaction();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(updaterFileName);
            Element root = doc.getDocumentElement();

            String catalog = pool.getCatalog();
            String dbType = pool.getDbType();
            
            String id = root.getAttributes().getNamedItem(ID).getNodeValue();
            String name = root.getAttributes().getNamedItem(NAME).getNodeValue();
            
            NodeList updatesList = root.getElementsByTagName(UPDATES);
            Node node = updatesList.item(0);
            int count = updatesList.getLength();
            if (count != 1) {
            	throw new Exception("Update File must contain a single 'updates' element.");
            }

            int currentVersion = getCurrentVersion(database, catalog, id);
            int targetVersion = Integer.parseInt(target);
            
            if (targetVersion <= currentVersion) {
            	throw new Exception("Target version " + targetVersion + " must be greater than current version " + currentVersion);
            }
            
            System.out.println("Updating " + name + "[" + id + "] on catalog " + catalog);
            System.out.println("Moving from version " + currentVersion + " to version " + targetVersion);
            System.out.println("Using updates for database type " + dbType);

            List<Update> updates = new ArrayList<Update>();
            
            NodeList children = node.getChildNodes();
            int updateCount = children.getLength();
            for (int i = 0; i < updateCount; i++) {
                Node updateNode = children.item(i);
                if (UPDATE.equalsIgnoreCase(updateNode.getNodeName())) {
                    int updateVersion = Integer.valueOf(updateNode.getAttributes().getNamedItem(VERSION).getNodeValue());
                    if (updateVersion > currentVersion && updateVersion <= targetVersion) {
                        NodeList updateChildren = updateNode.getChildNodes();
                        int updateChildCount = updateChildren.getLength();
                        for (int j = 0; j < updateChildCount; j++) {
                        	Node targetNode = updateChildren.item(j);
                        	if (TARGET.equalsIgnoreCase(targetNode.getNodeName())) {
                                String targetType = targetNode.getAttributes().getNamedItem(DATABASE).getNodeValue();
                                if (dbType.equalsIgnoreCase(targetType)) {
                                	NodeList targetChildren = targetNode.getChildNodes();
                                	int targetChildCount = targetChildren.getLength();
                                	List<String> sqlList = new ArrayList<String>();
                                	for (int k = 0; k < targetChildCount; k++) {
                                		Node sqlNode = targetChildren.item(k);
                                		if (SQL.equalsIgnoreCase(sqlNode.getNodeName())) {
                                        	String sql = sqlNode.getTextContent();
                                        	sqlList.add(sql);
                                		}
                                	}

                                	Update update = new Update(updateVersion, sqlList, catalog, id);
                                	
                                	Node v = updateNode.getAttributes().getNamedItem(DEPENDSONID);
                                    if (v != null) {
                                        String dependsOnId = v.getNodeValue();
                                        String dependsOnName = updateNode.getAttributes().getNamedItem(DEPENDSONNAME).getNodeValue();
                                        int dependsOnVersion = Integer.valueOf(updateNode.getAttributes().getNamedItem(DEPENDSONVERSION).getNodeValue());
                                        update.setDependency(dependsOnName, dependsOnId, dependsOnVersion);
                                    }

                                    updates.add(update);
                                }
                        	}
                        }
                    }
                }
            }

            Collections.sort(updates);

            if (updates.size() == 0) {
            	throw new Exception("Nothing to do.  Database is already at target version.");
            }
            
            validateDependencies(updates, database);
            
            performUpdates(updates, database);
            String sql;
            if (currentVersion == 0) {
            	sql = "insert into " + catalog + ".Version values( '" + id +
            			"', '" + name + "', " + targetVersion + ", CURRENT_TIMESTAMP)";
            				
            } else {
            	sql = "update " + catalog + ".Version set version = " + targetVersion + 
            					" where id = '" + id + "'";
            }
        	database.executeStatement(sql);
        	database.commitTransaction();
        	
        	System.out.println("\nDatabase successfully updated to version " + targetVersion);
        } catch (Exception e) {
        	try {
        		database.rollbackTransaction();
        	} catch (Exception ee) {
        	}
        	System.out.print("\nUpdate Failed: " + e.getLocalizedMessage());
        }
		
        pool.returnInstance(database, this);
	}
	
	private void performUpdates(List<Update> updates, DatabaseConnection database) throws Exception {
		database.startTransaction();
		try {
			for (Update update : updates) {
				List<String> sqlList = update.getSql();
				for (String sql : sqlList) {
					sql = sql.replaceAll(SCHEMA_PLACEHOLDER, database.getCatalog());
					database.executeStatement(sql);
				}
			}
			database.commitTransaction();
		} catch (Exception e) {
			try {
				database.rollbackTransaction();
			} catch (Exception ee) {
				throw ee;
			}
			
			throw e;
		}
	}

	private void validateDependencies(List<Update> updates, DatabaseConnection database) throws Exception {
		for (Update update : updates) {
			if (update.hasDependency()) {
				int needsVersion = update.getDependsOnVersion();
				int depVersion = getCurrentVersion(database, update.getCatalog(), update.getDependsOnId());
				if (depVersion < needsVersion) {
					String msg = "Update requires version " + needsVersion + " of " +
									update.getDependsOnName() + "[" + update.getDependsOnId() + "]";
					throw new Exception(msg);
				}
			}
		}
	}
	
	private int getCurrentVersion(DatabaseConnection database, String catalog, String id) throws Exception {
		int version = 0;
		String sql = "select version from " + catalog + ".Version where id = '" + id + "'";
		DataSet dataSet = database.getDataSet(sql, true);
		if (dataSet.next()) {
			version = dataSet.getFieldValue("version").asInteger();
		}
		
		return version;
	}
}
