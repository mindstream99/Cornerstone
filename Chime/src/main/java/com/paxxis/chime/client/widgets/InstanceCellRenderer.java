package com.paxxis.chime.client.widgets;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.paxxis.chime.client.Utils;
import com.paxxis.chime.client.common.DataInstance;
import com.paxxis.chime.client.portal.DataRowModel;

public class InstanceCellRenderer implements GridCellRenderer<DataRowModel> {
	
	/** the default margins for placing the renderer in the cell */
	private Margins margins = new Margins(3, 3, 3, 0);
	
	private boolean useInterceptedHtml = true;
	
	public InstanceCellRenderer() {
		this(true);
	}
	
	public InstanceCellRenderer(boolean intercepted) {
		useInterceptedHtml = intercepted;
	}

	/**
	 * Constructor
	 * @param m the margins to use when placing the renderer in the cell.
	 */
	public InstanceCellRenderer(Margins m) {
		this(true);
		margins = m;
	}

	public InstanceCellRenderer(Margins m, boolean intercepted) {
		this(intercepted);
		margins = m;
	}
	
	@Override
	public Object render(DataRowModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<DataRowModel> store, Grid<DataRowModel> grid) {

		if (property.equals(DataRowModel.BLANK)) {
			LayoutContainer lc = new LayoutContainer();
			lc.setHeight(15);
			return lc;
		} else if (model instanceof FieldValueModel) {
			FieldValueModel fieldValueModel = (FieldValueModel)model;
			DataInstance inst = (DataInstance)fieldValueModel.getValue();
			return generateFieldContent(inst);
		} else {
			return model.get(property);
		}
	}

	private String generateFieldContent(DataInstance inst) {
    	String name = inst.getName().replaceAll(" ", "&nbsp;");
        
		String stringContent;
        if (useInterceptedHtml) {
            stringContent = Utils.toHoverUrl(inst.getId(), name);
        } else {
            stringContent = name;
        }

        return stringContent;
	}
}
