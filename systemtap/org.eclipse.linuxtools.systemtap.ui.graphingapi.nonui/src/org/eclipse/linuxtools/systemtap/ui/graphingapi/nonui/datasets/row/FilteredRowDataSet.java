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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.IDataSetFilter;



public class FilteredRowDataSet extends RowDataSet implements IFilteredDataSet {
	public FilteredRowDataSet(RowDataSet data) {
		super(data.getTitles());
		original = data;
		this.data = data.data;
		filters = new ArrayList<IDataSetFilter>();

		filtersChanged = false;
		topData = null;
		dataCount = data.getRowCount();
	}
	
	public FilteredRowDataSet(String[] titles) {
		this(new RowDataSet(titles));
	}
	
	//Overwrite methods to insure data is removed from the original DataSet
	public void append(IDataEntry entry) throws ArrayIndexOutOfBoundsException {
		original.append(entry);
	}
	
	public boolean remove(IDataEntry entry) {
		return original.remove(entry);
	}
	
	public boolean remove(int entry) {
		return original.remove(entry);
	}
	//End overwrite methods to insure data is removed from the original DataSet
	
 	//Overwrite to ensure the data returned has all the filters applied
	public Object[] getColumn(int col, int start, int end) {
		rebuildDataSet();
		return super.getColumn(col, start, end);
	}

	public Object[] getRow(int row) {
		rebuildDataSet();
		return super.getRow(row);
	}
	
	//public int getRowCount() - calls getEntryCount();
	
	public Object[] getHistoricalData(String key, int col, int start, int end) {
		rebuildDataSet();
		return super.getHistoricalData(key, col, start, end);
	}
	
	public int getEntryCount() {
		rebuildDataSet();
		return super.getEntryCount();
	}
	
	public IDataEntry getEntry(int entry) {
		rebuildDataSet();
		return super.getEntry(entry);
	}
 	//End overwrite to ensure the data returned has all the filters applied

	//IFilteredDataSet Methods
	public void addFilter(IDataSetFilter filter) {
		filters.add(filter);
		filtersChanged = true;
	}
	
	public boolean removeFilter(IDataSetFilter filter) {
		filtersChanged = filters.remove(filter);
		return filtersChanged;
	}
	
	public void clearFilters() {
		filters.clear();
		filtersChanged = true;
	}
	
	public IDataSetFilter[] getFilters() {
		IDataSetFilter[] f = new IDataSetFilter[filters.size()];
		filters.toArray(f);
		return f;
	}
	//End IFilteredDataSet Methods
	
	@SuppressWarnings("unchecked")
	private void rebuildDataSet() {
		IDataEntry top = original.getEntry(original.getEntryCount()-1);

		if(filtersChanged || dataCount != original.getRowCount() || topData != top) {
			dataCount = original.getRowCount();
			topData = top;

			ArrayList[] filterData = getFilterData();
			for(int i=0; i<filters.size(); i++)
				filterData = ((IDataSetFilter)filters.get(i)).filter(filterData);
			setFilteredData(filterData);
		}
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList[] getFilterData() {
		ArrayList[] data = new ArrayList[original.getColCount()];
		for(int i=0; i<data.length; i++)
			data[i] = new ArrayList<Object>();

		Object[] row;
		for(int j,i=0; i<original.getRowCount(); i++) {
			row = original.getRow(i);
			for(j=0; j<data.length; j++)
				data[j].add(row[j]);
		}
		
		return data;
	}
	
	@SuppressWarnings("unchecked")
	private void setFilteredData(ArrayList[] data) {
		this.data = new ArrayList<IDataEntry>();
		
		RowEntry entry;
		Object[] row;
		for(int j,i=0; i<data[0].size(); i++) {
			row = new Object[data.length];
			entry = new RowEntry();
			for(j=0; j<data.length; j++)
				row[j] = data[j].get(i);
			entry.putRow(0, row);
			this.data.add(entry);
		}
	}

	private RowDataSet original;
	private ArrayList<IDataSetFilter> filters;

	private boolean filtersChanged;
	private int dataCount;
	private Object topData;
}
