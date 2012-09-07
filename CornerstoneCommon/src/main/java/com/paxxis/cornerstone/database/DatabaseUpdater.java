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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	private static final int LATESTVERSION = Integer.MAX_VALUE;

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
        DatabaseConnection database = null;
        
        try {
            database = pool.borrowInstance(this);
        	database.startTransaction();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(updaterFileName);
            Element root = doc.getDocumentElement();

            String catalog = pool.getCatalog();
            String dbType = pool.getTypeProvider().getName();
            
            String id = root.getAttributes().getNamedItem(ID).getNodeValue();
            String name = root.getAttributes().getNamedItem(NAME).getNodeValue();
            
            NodeList updatesList = root.getElementsByTagName(UPDATES);
            Node node = updatesList.item(0);
            int count = updatesList.getLength();
            if (count != 1) {
            	throw new Exception("Update File must contain a single 'updates' element.");
            }

            int currentVersion = getCurrentVersion(database, catalog, id);
            int targetVersion = LATESTVERSION;
            if (target != null) {
                targetVersion = Integer.parseInt(target);
            }
            
            if (targetVersion <= currentVersion) {
            	throw new Exception("Target version " + targetVersion + " must be greater than current version " + currentVersion);
            }
            
            System.out.println("Updating " + name + "[" + id + "] on catalog " + catalog);
            System.out.println("Moving from version " + currentVersion + " to " + 
            						(targetVersion == LATESTVERSION ? "Latest Version" : " version " + targetVersion));
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
                                        	List<String> sqlLines = parseLines(sql);
                                        	for (String sqlLine : sqlLines) {
                                        		if (!sqlLine.isEmpty()) {
                                                	sqlList.add(sqlLine);
                                        		}
                                        	}
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

            targetVersion = validateTargetVersion(updates, currentVersion, targetVersion, database);
            validateDependencies(updates, database);
            
            performUpdates(updates, database);
            String sql;
            if (currentVersion == 0) {
            	sql = "insert into " + catalog + ".Version values( '" + id +
            			"', '" + name + "', " + targetVersion + ", CURRENT_TIMESTAMP)";
            				
            } else {
            	sql = "update " + catalog + ".Version set version = " + targetVersion + 
            					", timestamp = CURRENT_TIMESTAMP where id = '" + id + "'";
            }
        	database.executeStatement(sql);
        	database.commitTransaction();
        	
        	System.out.println("\nDatabase successfully updated to version " + targetVersion + "\n");
        } catch (Exception e) {
        	try {
        	    if (database != null) {
        		database.rollbackTransaction();
        	    }
        	} catch (Exception ee) {
        	}
        	System.out.println("\nUpdate Failed: " + e.getLocalizedMessage() + "\n");
        } finally {
            if (database != null) {
                pool.returnInstance(database, this);
            }
        }
		
	}
	
	/**
	 * Parses the input into separate lines, combining lines that are continued using
	 * a backslash at the end of the line
	 */
	private List<String> parseLines(String input) throws Exception {
		StringReader rdr = new StringReader(input);
		BufferedReader br = new BufferedReader(rdr);

        List<String> list = new ArrayList<String>();
        
        String strLine;
        boolean prevContinues = false;
	    while ((strLine = br.readLine()) != null)   {
	    	strLine = strLine.trim();
	    	boolean thisContinues = strLine.endsWith("\\");
	    	if (thisContinues) {
	    		int last = strLine.length() - 1;
	    		strLine = strLine.substring(0, last);
	    	}
	    	
	    	if (prevContinues) {
	    		prevContinues = false;
	    		int last = list.size() - 1;
	    		String full = list.get(last) + strLine;
	    		list.set(last, full);
	    	} else {
		    	list.add(strLine);
	    	}
	    	
	    	prevContinues = thisContinues;
	    }
	    
	    return list;
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

	private int validateTargetVersion(List<Update> updates, int currentVersion, int targetVersion, DatabaseConnection database) throws Exception {
		int target = targetVersion;
		if (target == LATESTVERSION) {
			// find the latest version in the list
			int latest = 0;
			for (Update update : updates) {
				if (update.getVersion() > latest) {
					latest = update.getVersion();
				}
			}
			
			if (latest <= currentVersion) {
				String msg = "Database version is already at or past the latest version being applied.";
				throw new Exception(msg);
			}
			
			target = latest;
		}
		
		// there must be at least one update for each version after the current version, up to
		// and including the target version
		List<Integer> missing = new ArrayList<Integer>();
		for (int i = (currentVersion + 1); i <= target; i++) {
			boolean foundOne = false;
			for (Update update : updates) {
				if (update.getVersion() == i) {
					foundOne = true;
					break;
				}
			}
			
			if (!foundOne) {
				missing.add(i);
			}
		}
		
		if (missing.size() > 0) {
			StringBuilder msg = new StringBuilder("No updates found for target versions: ");
			String op = "";
			for (Integer version : missing) {
				msg.append(op).append(version);
				op = ", ";
			}
			
			throw new Exception(msg.toString());
		}
		
		return target;
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
		IDataSet dataSet = null;
		try {
		    dataSet = database.getDataSet(sql, true);
    		if (dataSet.next()) {
    			version = dataSet.getFieldValue("version").asInteger();
    		}
		} finally {
            if (dataSet != null) {
                dataSet.close();
            }
		}
		
		return version;
	}
}
