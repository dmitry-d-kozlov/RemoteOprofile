/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Dmitry Kozlov <ddk@codesourcery.com> - added runOpReport
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core;

import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;

/**
 * Interface for oprofile core to utilize opcontrol program. Platform plugins should define/register
 * an OpcontrolProvider for the core to use.
 */
public interface IOpcontrolProvider {
	
	/**
	 * Initialize the Oprofile kernel module
	 * @throws OpcontrolException
	 */
	public void initModule() throws OpcontrolException;
	
	/**
	 * De-initialize (unload) the kernel module
	 * @throws OpcontrolException
	 */
	public void deinitModule() throws OpcontrolException;
	
	/**
	 * Clears out data from the current session
	 * @throws OpcontrolException
	 */
	public void reset() throws OpcontrolException;
	
	/**
	 * Flush the current oprofiled sample buffers to disk
	 * @throws OpcontrolException
	 */
	public void dumpSamples() throws OpcontrolException;
	
	/**
	 * Setup oprofiled collection parameters
	 * @param options a list of command-line arguments for opcontrol
	 * @param events list of events to collect
	 * @throws OpcontrolException
	 */
	public void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException;
	
	/**
	 * Start data collection by oprofiled (will start oprofiled if necessary)
	 * @throws OpcontrolException
	 */
	public void startCollection() throws OpcontrolException;
	
	/**
	 * Stop data collection (does NOT stop daemon)
	 * @throws OpcontrolException
	 */
	public void stopCollection() throws OpcontrolException;
	
	/**
	 * Stop data collection and shutdown oprofiled
	 * @throws OpcontrolException
	 */
	public void shutdownDaemon() throws OpcontrolException;
	
	/**
	 * Start oprofiled (does NOT start data collection)
	 * @throws OpcontrolException
	 */
	public void startDaemon() throws OpcontrolException;
	
	/**
	 * Save the current session
	 * @throws OpcontrolException
	 */
	public void saveSession(String name) throws OpcontrolException;

	/**
	 * Delete the session with the specified name for the specified event
	 * @param sessionName The name of the session to delete
	 * @param eventName The name of the event containing the session
	 * @throws OpcontrolException
	 */
	public void deleteSession (String sessionName, String sessionEvent) throws OpcontrolException;

	public boolean status() throws OpcontrolException;

	/**
     * Run opreport command
     * @param args opreport arguments except -X flag, which is added
     * @return
	 * @throws OpcontrolException
	 */
	public InputStream runOpReport(ArrayList<String> args) throws OpcontrolException;
}
