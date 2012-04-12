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

package org.eclipse.linuxtools.internal.oprofile.core;

public class OprofileConstants {

	// Initialize the Oprofile kernel module and oprofilefs
	public static final String OPD_INIT_MODULE = "--init"; //$NON-NLS-1$
	// Setup daemon collection arguments
	public static final String OPD_SETUP = "--setup"; //$NON-NLS-1$
	public static final String OPD_HELP = "--help"; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE = "--separate="; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE_SEPARATOR = ","; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE_NONE = "none"; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE_LIBRARY = "library"; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE_KERNEL = "kernel"; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE_THREAD = "thread"; //$NON-NLS-1$
	public static final String OPD_SETUP_SEPARATE_CPU = "cpu"; //$NON-NLS-1$
	public static final String OPD_SETUP_EVENT = "--event="; //$NON-NLS-1$
	public static final String OPD_SETUP_EVENT_SEPARATOR = ":"; //$NON-NLS-1$
	public static final String OPD_SETUP_EVENT_TRUE = "1"; //$NON-NLS-1$
	public static final String OPD_SETUP_EVENT_FALSE = "0"; //$NON-NLS-1$
	public static final String OPD_SETUP_EVENT_DEFAULT = "default"; //$NON-NLS-1$
	public static final String OPD_SETUP_IMAGE = "--image="; //$NON-NLS-1$
	public static final String OPD_CALLGRAPH_DEPTH = "--callgraph="; //$NON-NLS-1$
	// Kernel image file options
	public static final String OPD_KERNEL_NONE = "--no-vmlinux"; //$NON-NLS-1$
	public static final String OPD_KERNEL_FILE = "--vmlinux="; //$NON-NLS-1$
	// Start the daemon process without starting data collection
	public static final String OPD_START_DAEMON = "--start-daemon"; //$NON-NLS-1$
	// Start collecting profiling data
	public static final String OPD_START_COLLECTION = "--start"; //$NON-NLS-1$
	// Flush the collected profiling data to disk
	public static final String OPD_DUMP = "--dump"; //$NON-NLS-1$
	// Stop data collection
	public static final String OPD_STOP_COLLECTION = "--stop"; //$NON-NLS-1$
	// Stop data collection and stop daemon
	public static final String OPD_SHUTDOWN = "--shutdown"; //$NON-NLS-1$
	// Clear out data from current session
	public static final String OPD_RESET = "--reset"; //$NON-NLS-1$
	// Unload the oprofile kernel module and oprofilefs
	public static final String OPD_DEINIT_MODULE = "--deinit"; //$NON-NLS-1$
	public static final String OPCONTROL_EXECUTABLE = "opcontrol";
	// Location of opcontrol security wrapper
	public static final String OPCONTROL_REL_PATH = "natives/linux/scripts/" + OPCONTROL_EXECUTABLE; //$NON-NLS-1$
	// Ugh. Need to know whether the module is loaded without running oprofile commands...
	static final String[] OPROFILE_CPU_TYPE_FILES = {
		"/dev/oprofile/cpu_type", //$NON-NLS-1$
		"/proc/sys/dev/oprofile/cpu_type"  //$NON-NLS-1$
	};

}
