/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.model.OpModelSession;
import org.eclipse.linuxtools.oprofile.tests.TestingOpModelRoot;

public class TestDataModel extends TestCase {
	private TestingOpModelRoot _testRoot;
	
	public TestDataModel() {
		super("test data model"); //$NON-NLS-1$
	}
	
	@Override
	protected void setUp() throws Exception {
		_testRoot = new TestingOpModelRoot();
		_testRoot.refreshModel();
	}
	
	public void testParse() {
		OpModelEvent[] events = _testRoot.getEvents();
		assertEquals(3, events.length);
		assertEquals(TestingOpModelRoot.NAME_E1, events[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2, events[1].getName());
		
		OpModelSession[] e1_sessions = events[0].getSessions(), e2_sessions = events[1].getSessions();
		assertEquals(1, e1_sessions.length);
		assertEquals(4, e2_sessions.length);
		
		assertEquals(205000, e1_sessions[0].getCount());
		assertEquals(205000, e2_sessions[0].getCount());
		assertEquals(200000, e2_sessions[1].getCount());
		assertEquals(OpModelImage.IMAGE_PARSE_ERROR, e2_sessions[2].getCount());
		assertEquals(0, e2_sessions[3].getCount());
		
		assertEquals(TestingOpModelRoot.NAME_E1_S1, e1_sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S1, e2_sessions[0].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S2, e2_sessions[1].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S3, e2_sessions[2].getName());
		assertEquals(TestingOpModelRoot.NAME_E2_S4, e2_sessions[3].getName());
		
		//further image parsing is tested in the TestModelDataParse testParse
	}
	
	public void testStringOutput() {
		assertEquals(TestingOpModelRoot.ROOT_OUTPUT, _testRoot.toString());
	}
}
