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

package com.paxxis.chime.client.editor;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.paxxis.chime.client.ChimeListStore;

/**
 * 
 * @author Robert Englander
 *
 */
public class ChartUtils {

	public static class ChartTypeModel extends BaseModel {
		private ChartType type;
		
		public ChartTypeModel(ChartType t) {
			type = t;
			set("type", type);
			set("name", ChartUtils.getName(type));
		}
		
		public String getName() {
			return get("name");
		}
		
		public ChartType getChartType() {
			return get("type");
		}
	}
	
	public enum ChartType {
		Pie,
		Bar,
		Bar3D,
		Gauge
	}

	private static final String PIE = "Pie Chart";
	private static final String BAR = "Bar Chart";
	private static final String BAR3D = "3D Bar Chart";
	private static final String GAUGE = "Gauge";
	
	private ChartUtils() {
	}
	
	public static ChimeListStore<ChartTypeModel> getListStore() {
        ChimeListStore<ChartTypeModel> chartStore = new ChimeListStore<ChartTypeModel>();
        chartStore.add(new ChartTypeModel(ChartType.Pie));
        chartStore.add(new ChartTypeModel(ChartType.Bar));
        chartStore.add(new ChartTypeModel(ChartType.Bar3D));
        chartStore.add(new ChartTypeModel(ChartType.Gauge));
        
        return chartStore;
	}
	
	public static ChartType getChartType(String name) {
		ChartType type = ChartType.valueOf(name);
		return type;
	}
	
	public static String getName(ChartType type) {
		String result = "";
		switch (type) {
			case Pie:
				result = PIE;
				break;
			case Bar:
				result = BAR;
				break;
			case Bar3D:
				result = BAR3D;
				break;
			case Gauge:
				result = GAUGE;
				break;
		}
		
		return result;
	}
}
