/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.launch.launching;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.linux.LinuxOpcontrolProvider;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.swt.widgets.Display;

public class OprofileLaunchConfigurationDelegate extends AbstractOprofileLaunchConfigurationDelegate {

	@Override
	protected boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents) {
		//set up and launch the oprofile daemon
		try {
			// Set current project to allow using the oprofile path that
			// was chosen for the project 
			LinuxOpcontrolProvider.setCurrentProject(getProject());
			
			if (!oprofileStatus())
				return false;
			
			//kill the daemon (it shouldn't be running already, but to be safe)
			oprofileShutdown();
			
			//reset data from the (possibly) existing default session, 
			// otherwise multiple runs will combine samples and results
			// won't make much sense
			oprofileReset();
			
			//setup the events and other parameters
			oprofileSetupDaemon(options.getOprofileDaemonOptions(), daemonEvents);
			
			//start the daemon & collection of samples 
			//note: since the daemon is only profiling for the specific image we told 
			// it to, no matter to start the daemon before the binary itself is run
			oprofileStartCollection();
		} catch (OpcontrolException oe) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	@Override
	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch, Process process) {
		//add a listener for termination of the launch
		ILaunchManager lmgr = DebugPlugin.getDefault().getLaunchManager();
		lmgr.addLaunchListener(new LaunchTerminationWatcher(launch));
	}
	
	//A class used to listen for the termination of the current launch, and 
	// run some functions when it is finished. 
	class LaunchTerminationWatcher implements ILaunchesListener2 {
		private ILaunch launch;
		public LaunchTerminationWatcher(ILaunch il) {
			launch = il;
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
						oprofileDumpSamples();
						oprofileShutdown();

						//need to run this in the ui thread otherwise get SWT Exceptions
						// based on concurrency issues
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								refreshOprofileView();
							}
						});
					}
				}
			} catch (OpcontrolException oe) {
				OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
			}
		}
		public void launchesAdded(ILaunch[] launches) { /* dont care */}
		public void launchesChanged(ILaunch[] launches) { /* dont care */ }
		public void launchesRemoved(ILaunch[] launches) { /* dont care */ }
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) { return null; /* dont care */}
}
