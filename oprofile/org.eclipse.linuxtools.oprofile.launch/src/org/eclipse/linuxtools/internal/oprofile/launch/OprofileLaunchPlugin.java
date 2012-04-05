/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.launch;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.EventConfigCache;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class OprofileLaunchPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static OprofileLaunchPlugin plugin;
	//shared cache instance for configuration
	private static EventConfigCache eventConfigCache = null;

	private static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.launch"; //$NON-NLS-1$

	// The launch type ID for profiling
	public static final String ID_LAUNCH_PROFILE = PLUGIN_ID + ".oprofile"; //$NON-NLS-1$
	public static final String ID_LAUNCH_PROFILE_MANUAL = PLUGIN_ID + ".oprofile.manual"; //$NON-NLS-1$


	/* Launch Configuration attributes */
	
	// Global options \\
	public static final String ATTR_KERNEL_IMAGE_FILE = ID_LAUNCH_PROFILE + ".KERNEL_IMAGE"; 			//$NON-NLS-1$
	public static final String ATTR_SEPARATE_SAMPLES = ID_LAUNCH_PROFILE + ".SEPARATE_SAMPLES"; 		//$NON-NLS-1$
	public static final String ATTR_USE_DEFAULT_EVENT = ID_LAUNCH_PROFILE + ".USE_DEFAULT_EVENT";		//$NON-NLS-1$

	// Manual Profile string \\
	public static final String ATTR_MANUAL_PROFILE = ID_LAUNCH_PROFILE + ".MANUAL_PROFILE";		//$NON-NLS-1$
	
	// Counter Attributes \\
	private static final String ATTR_COUNTER(int nr) { return ID_LAUNCH_PROFILE + ".COUNTER_" + nr; } 					//$NON-NLS-1$
	public static final String ATTR_COUNTER_ENABLED(int nr)  { return ATTR_COUNTER(nr) + ".ENABLED"; } 					//$NON-NLS-1$
	public static final String ATTR_COUNTER_EVENT(int nr) { return ATTR_COUNTER(nr)  + ".EVENT"; } 						//$NON-NLS-1$
	public static final String ATTR_COUNTER_PROFILE_KERNEL(int nr) { return ATTR_COUNTER(nr) + ".PROFILE_KERNEL"; }		//$NON-NLS-1$
	public static final String ATTR_COUNTER_PROFILE_USER(int nr) { return ATTR_COUNTER(nr) + ".PROFILE_USER"; } 		//$NON-NLS-1$
	public static final String ATTR_COUNTER_COUNT(int nr) { return ATTR_COUNTER(nr) + ".COUNT"; } 						//$NON-NLS-1$
	public static final String ATTR_COUNTER_UNIT_MASK(int nr) { return  ATTR_COUNTER(nr) + ".UNIT_MASK"; } 				//$NON-NLS-1$
	
	
	public static final String ICON_PATH = "icons/"; //$NON-NLS-1$
	public static final String ICON_EVENT_TAB = ICON_PATH + "event_tab.gif"; //$NON-NLS-1$
	public static final String ICON_GLOBAL_TAB = ICON_PATH + "global_tab.gif"; //$NON-NLS-1$
	
	
	/**
	 * The constructor.
	 */
	public OprofileLaunchPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}	

	public static OprofileLaunchPlugin getDefault() {
		return plugin;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}	

	public static EventConfigCache getCache() {
		if (eventConfigCache == null) {
			eventConfigCache = new EventConfigCache();
		}
		
		return eventConfigCache;
	}
}
