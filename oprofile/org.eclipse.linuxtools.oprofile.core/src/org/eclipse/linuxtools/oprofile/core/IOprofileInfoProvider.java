/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dmitry Kozlov <ddk@codesourcery.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.oprofile.core;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.daemon.OpEvent;

/**
 * Interface to query general information about oprofile.  
 */
public interface IOprofileInfoProvider {
		
	/**
	 * Queries oprofile for the number of counters on the current CPU.
	 * Used only in launch config tabs.
	 * @return the number of counters
	 */
	public int getNumberOfCounters();
	
	/**
	 * Returns the CPU speed of the current configuration.
	 * @return the cpu speed in MHz
	 */
	public double getCpuFrequency();
	
	/**
	 * Get all the events that may be collected on the given counter.
	 * @param num the counter number
	 * @return an array of all valid events -- NEVER RETURNS NULL!
	 */
	public OpEvent[] getEvents(int num);
	
	/**
	 * Returns the default location of the oprofile samples directory.
	 * @return the default samples directory
	 */
	public String getDefaultSamplesDirectory();
	
	/**
	 * Returns the oprofile daemon log file.
	 * @return the log file (absolute pathname)
	 */
	public String getLogFile();
	
	/**
	 * Returns whether or not oprofile is in timer mode.
	 * @return true if oprofile is in timer mode, false otherwise
	 */
	public boolean getTimerMode();
	
	/**
	 * Checks the requested counter, event, and unit mask for vailidity.
	 * @param ctr	the counter
	 * @param event	the event name
	 * @param um	the unit mask
	 * @return whether the requested event is valid
	 * @throws OpcontrolException 
	 */
	public Boolean checkEvent(int ctr, String event, int um) throws OpcontrolException;
	
	/**
	 * Returns a list of all the events collected on the system, as well as
	 * the sessions under each of them.
	 * @throws OpcontrolException 
	 * @returns a list of all collected events
	 */
	public OpModelEvent[] getEvents() throws OpcontrolException;
	
	/**
	 * Return a list of all the Samples in the given session.
	 * @param session the session for which to get samples
	 * @param shell the composite shell to use for the progress dialog
	 * @throws OpcontrolException 
	 */
	public OpModelImage getModelData(String eventName, String sessionName) throws OpcontrolException;
}
