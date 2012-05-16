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
package org.eclipse.linuxtools.oprofile.tests;

import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.model.OpModelSession;

/*
 * A faked OpModelSession object where there is no image. 
 * Note: technically this shouldn't really happen in normal operation
 *       of the plugin unless there is a major b0rk-up with the xml parsers..
 */
public class TestingOpModelSession4 extends OpModelSession {
	public TestingOpModelSession4(OpModelEvent event, String name) {
		super(event, name);
	}
	@Override
	protected OpModelImage getNewImage() {
		return null;
	}
}
