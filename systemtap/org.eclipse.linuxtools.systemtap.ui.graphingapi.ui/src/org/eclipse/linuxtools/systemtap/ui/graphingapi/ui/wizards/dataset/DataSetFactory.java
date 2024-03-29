/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.FilteredRowDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table.FilteredTableDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table.TableParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.datadisplay.DataGrid;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.internal.Localization;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;



public final class DataSetFactory {
	public static IDataSet createDataSet(String id, String[] labels) {
		if(id.equals(RowDataSet.ID))
			return new RowDataSet(labels);
		else if(id.equals(TableDataSet.ID))
			return new TableDataSet(labels);
		return null;
	}
	
	public static IFilteredDataSet createFilteredDataSet(String id, String[] labels) {
		if(id.equals(RowDataSet.ID))
			return new FilteredRowDataSet(labels);
		else if(id.equals(TableDataSet.ID))
			return new FilteredTableDataSet(labels);
		return new FilteredRowDataSet(labels);
	}
	
	public static IFilteredDataSet createFilteredDataSet(IDataSet set) {
		if(set instanceof RowDataSet)
			return new FilteredRowDataSet((RowDataSet)set);
		else if(set instanceof TableDataSet)
			return new FilteredTableDataSet((TableDataSet)set);
		return null;
	}
	
	public static String[] getIDs() {
		return ids;
	}
	
	public static String getName(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return names[index];
		return null;
	}
	
	public static String getDescription(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return descriptions[index];
		return null;
	}
	
	public static ParsingWizardPage getParsingWizardPage(String id) {
		ParsingWizardPage page = null;
		
		int index = getIndex(id);
		if(index >=0 && index < dataSetWizards.length)
			return dataSetWizards[index];
		
		return page;
	}
	
	public static IDataSetParser createParserXML(String id, IMemento source) {
		if(id.equals(ids[0]))
			return new RowParser(source);
		else if(id.equals(ids[1]))
			return new TableParser(source);
			
		return null;
	}
	
	public static DataGrid getDataGrid(Composite composite, IDataSet set) {
		if(set instanceof RowDataSet)
			return new DataGrid(composite, (RowDataSet)set, DataGrid.NONE);
		else if(set instanceof TableDataSet)
			return new DataGrid(composite, (TableDataSet)set, DataGrid.FULL_UPDATE);
			
		return null;
	}
	
	private static int getIndex(String id) {
		for(int i=0; i<ids.length; i++)
			if(id.equals(ids[i]))
				return i;
		return -1;
	}
	
	private static final String[] ids = {
		RowDataSet.ID, 
		TableDataSet.ID
	};
	
	private static final String[] names = {
		Localization.getString("DataSetFactory.RowDataSet"),
		Localization.getString("DataSetFactory.TableDataSet")
	};
	
	private static final String[] descriptions = {
		Localization.getString("DataSetFactory.RowDataSetDescription") +
		Localization.getString("DataSetFactory.DataSetExample") +
		Localization.getString("DataSetFactory.DataSetHeader") +
		"1305	2309	4233\n" +
		"2322	3234	4223\n" +
		"2321	3123	4533\n" +
		"2343	2931	4423\n" +
		"1356	2984	3850\n",

		Localization.getString("DataSetFactory.TableDataSetDescription") +
		Localization.getString("DataSetFactory.DataSetExample") +
		Localization.getString("DataSetFactory.DataSetHeader") +
		"2322	3232	3453\n" +
		"2321	3123	4533\n" +
		"2145	2135	5921\n" +
		"-------------------\n" +
		Localization.getString("DataSetFactory.DataSetHeader") +
		"2343	2931	4423\n" +
		"2234	2723	5233\n" +
		"3215	3565	4922\n" +
		"-------------------\n"
	};
	
	private static final ParsingWizardPage[] dataSetWizards = new ParsingWizardPage[] {
		new SelectRowParsingWizardPage(),
		new SelectTableParsingWizardPage()
	};
}
