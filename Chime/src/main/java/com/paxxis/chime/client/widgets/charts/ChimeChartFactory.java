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

package com.paxxis.chime.client.widgets.charts;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.paxxis.chime.client.ChimeListStore;
import com.paxxis.chime.client.common.portal.PortletSpecification;

/**
 * A factory for creating Chime chart instances.
 * 
 * @author Robert Englander
 *
 */
public class ChimeChartFactory {

	public static class ChartTypeModel extends BaseModel {
		private static final long serialVersionUID = 1L;

		private ChartType type;
		
		public ChartTypeModel(ChartType t) {
			type = t;
			set("type", type);
			set("name", type.name());
		}
		
		public ChartType getChartType() {
			return get("type");
		}
	}
	
	public enum ChartType {
		Pie,
		Bar,
		Column,
		Area,
		Gauge
	}

	/**
	 * Factory instance can't be created.  All access is via static methods.
	 */
	private ChimeChartFactory() {
	}
	
	public static ChimeListStore<ChartTypeModel> getListStore() {
        ChimeListStore<ChartTypeModel> chartStore = new ChimeListStore<ChartTypeModel>();
        chartStore.add(new ChartTypeModel(ChartType.Pie));
        chartStore.add(new ChartTypeModel(ChartType.Bar));
        chartStore.add(new ChartTypeModel(ChartType.Column));
        chartStore.add(new ChartTypeModel(ChartType.Area));
        chartStore.add(new ChartTypeModel(ChartType.Gauge));
        
        return chartStore;
	}
	
	public static ChartType getChartType(String name) {
		ChartType type = ChartType.valueOf(name);
		return type;
	}
	
	public static ChimeChart create(ChartType type, String title, int axisCol,
			int valueCol, int minValue, int maxValue) {
		
		PortletSpecification spec = new PortletSpecification();
		spec.setProperty("chart", type.name());
		spec.setProperty("field", title);
		spec.setProperty("labelY", "");
		spec.setProperty("axisCol", axisCol);
		spec.setProperty("valueCol", valueCol);
		spec.setProperty("min", minValue);
		spec.setProperty("max", maxValue);
                spec.setProperty("square", false);		
		return create(spec);
	}
	
	public static ChimeChart create(PortletSpecification spec) {
		ChimeChart chart = null;
		
		Object object = spec.getProperty("chart");
		if (object != null) {
			ChartType chartType = ChartType.valueOf(object.toString());
			switch (chartType) {
				case Area:
					chart = new ChimeAreaChart(spec);
					break;
				case Column:
					chart = new ChimeColumnChart(spec);
					break;
				case Bar:
					chart = new ChimeBarChart(spec);
					break;
				case Pie:
					chart = new ChimePieChart(spec);
					break;
				case Gauge:
					chart = new ChimeGaugeChart(spec);
					break;
			}
		}
		
		return chart;
	}
	
}
