/*******************************************************************************
 * Copyright (c) 2011 CodeSourcery
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dmitry Kozlov <ddk@codesourcery.com> - initial API and implementation 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.launch.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class OprofileRemoteLaunchPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.launch.remote"; //$NON-NLS-1$

	// The shared instance
	private static OprofileRemoteLaunchPlugin plugin;
	
	/**
	 * The constructor
	 */
	public OprofileRemoteLaunchPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static OprofileRemoteLaunchPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Returns the registered opcontrol provider or throws an exception
	 * @return the RemoteLinuxOpcontrolProvider registered with the plugin
	 * @throws OpcontrolException
	 */
	public RemoteLinuxOpcontrolProvider getOpcontrolProvider(ILaunchConfiguration config, 
			ILaunch launch, IProgressMonitor monitor) throws OpcontrolException {
		
		RemoteLinuxOpcontrolProvider opc = new RemoteLinuxOpcontrolProvider(config, launch, monitor);
		return opc;
	}	
}
