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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.SumAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.MockDataSet;

import junit.framework.TestCase;

public class SumAggregateTest extends TestCase {
	public SumAggregateTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAggregate() {
		SumAggregate aa = new SumAggregate();
		Number num;

		num = aa.aggregate(null);
		assertNull(num);

		num = aa.aggregate(new Number[] {});
		assertNull(num);

		num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,0}));
		assertEquals(0, num.intValue());
		
		num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {-1,0,1}));
		assertEquals(0, num.intValue());

		num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,1}));
		assertEquals(1, num.intValue());


		num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,0}));
		assertEquals(0.0, num.doubleValue(), 0.0);
		
		num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {-1,0,1}));
		assertEquals(0.0, num.doubleValue(), 0.0);

		num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,1,2}));
		assertEquals(3.0, num.doubleValue(), 0.0);
	}

	public void testGetID() {
		SumAggregate aa = new SumAggregate();
		assertTrue(SumAggregate.ID.equals(aa.getID()));
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
