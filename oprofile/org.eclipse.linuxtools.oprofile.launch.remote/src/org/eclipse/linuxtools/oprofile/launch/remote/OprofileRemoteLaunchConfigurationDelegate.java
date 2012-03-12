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

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.linuxtools.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.oprofile.launch.launching.AbstractOprofileLaunchConfigurationDelegate;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.swt.widgets.Display;

import com.codesourcery.help.DsfLaunchDelegate;

public class OprofileRemoteLaunchConfigurationDelegate extends AbstractOprofileLaunchConfigurationDelegate {

	// The shared instance of IOpocontrolProvider used in this launch 
	RemoteLinuxOpcontrolProvider opc; 
	IRemoteCmdSubSystem remoteCommandSubsystem;

	
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		try {
			IPath exePath = CDebugUtils.verifyProgramPath(config);

			opc = OprofileRemoteLaunchPlugin.getDefault().getOpcontrolProvider(config, launch, monitor);

			// Upload program binary to target
			String remoteProgram = DsfLaunchDelegate.uploadApplication(config, monitor, exePath);

			// Some code from local oprofile launch delegate 
			LaunchOptions options = new LaunchOptions();		//default options created in the constructor
			options.loadConfiguration(config);
			// FIXME: adapt to Remote path
			options.setBinaryImage(remoteProgram);

			//if daemonEvents null or zero size, the default event will be used
			OprofileDaemonEvent[] daemonEvents = null;
			if (!config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false)) {
				//get the events to profile from the counters
				OprofileCounter[] counters = OprofileCounter.getCounters(config);
				ArrayList<OprofileDaemonEvent> events = new ArrayList<OprofileDaemonEvent>();
				
				for (int i = 0; i < counters.length; ++i) {
					if (counters[i].getEnabled())
						events.add(counters[i].getDaemonEvent());
				}
				
				daemonEvents = new OprofileDaemonEvent[events.size()];
				events.toArray(daemonEvents);
			}			

			
			
			if (!preExec(options, daemonEvents)) return;
			
			DsfLaunchDelegate.runRemotely(config, launch, monitor, remoteProgram, LaunchUtils.getProgramArguments(config));


			postExec(options, daemonEvents, launch, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents) {
		//set up and launch the oprofile daemon
		try {			
			if (!opc.status())
				return false;
			
			//kill the daemon (it shouldn't be running already, but to be safe)
			opc.shutdownDaemon();
			
			//reset data from the (possibly) existing default session, 
			// otherwise multiple runs will combine samples and results
			// won't make much sense
			opc.reset();
			
			//setup the events and other parameters
			opc.setupDaemon(options.getOprofileDaemonOptions(), daemonEvents);
			
			//start the daemon & collection of samples 
			//note: since the daemon is only profiling for the specific image we told 
			// it to, no matter to start the daemon before the binary itself is run
			opc.startCollection();
		} catch (OpcontrolException oe) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch, Process process) {
		//add a listener for termination of the launch
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchListener(new LaunchTerminationWatcher(launch, options));
	}
	
	//A class used to listen for the termination of the current launch, and 
	// run some functions when it is finished. 
	class LaunchTerminationWatcher implements ILaunchesListener2 {
		private ILaunch launch;
		private final LaunchOptions launchOptions;
		
		public LaunchTerminationWatcher(ILaunch il, LaunchOptions options) {
			launch = il;
			launchOptions = options;
		}
		public void launchesTerminated(ILaunch[] launches) {
			try {
				for (ILaunch l : launches) {
					/**
					 * Dump samples from the daemon,
					 * shut down the daemon,
					 * activate the OProfile view (open it if it isn't already),
					 * refresh the view (which parses the data/ui model and displays it).
					 */
					if (l.equals(launch)) {
						opc.dumpSamples();
						opc.shutdownDaemon();
						opc.runOparchive(launchOptions);
						opc.downloadOprofileResults();
						
						// FIXME: run oparchive and download it to host 

						//need to run this in the ui thread otherwise get SWT Exceptions
						// based on concurrency issues
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								refreshOprofileView();
							}
						});
					}
				}
			} catch (CoreException e) {
				OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
			}
		}
		public void launchesAdded(ILaunch[] launches) { /* dont care */}
		public void launchesChanged(ILaunch[] launches) { /* dont care */ }
		public void launchesRemoved(ILaunch[] launches) { /* dont care */ }
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) { return null; /* dont care */}
}
