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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.linux.LinuxOpcontrolProvider;
import org.eclipse.linuxtools.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IServiceCommandShell;

/**
 * A class which encapsulates running opcontrol on remote target.
 */
public class RemoteLinuxOpcontrolProvider extends LinuxOpcontrolProvider {

	protected static final String EXIT_COMMAND = " ; exit";

	protected static final String ECHO_EXIT_CODE_COMMAND = " ; echo Command exit code is $?";

	private static final String TAR_COMMAND = "tar czf";
	
	public final static String SUDO_PREFIX = "sudo";
	
	public final static String OPCONTROL_PATH = SUDO_PREFIX + " " + "/usr/bin/opcontrol";

	public static final String OPARCHIVE_PROGRAM = SUDO_PREFIX + " " + "/usr/bin/oparchive"; 

	public static final String OPARCHIVE_TMP_DIR = "/tmp";
	
	public static final String OPARCHIVE_DIR_NAME = "oparchive"; 
	
	public static final String OPARCHIVE_TGZ_NAME = "oparchive.tgz"; 

	public static final String RM_COMMAND = SUDO_PREFIX + " " + "rm -rf ";


	protected ILaunchConfiguration config;
	protected ILaunch launch;
	IRemoteCmdSubSystem remoteCommandSubsystem;
	IProgressMonitor monitor;

	public RemoteLinuxOpcontrolProvider(ILaunchConfiguration config, 
			ILaunch launch, IProgressMonitor monitor) throws OpcontrolException {
		super(OPCONTROL_PATH);
		this.config = config;
		this.launch = launch;
		this.monitor = monitor;
		// Create command subsystem using proper ProgressMonitor for use by RemoteOpcontrolProvider
		try {
			this.remoteCommandSubsystem = LaunchUtils.getRemoteCmdSubSystem(config, new SubProgressMonitor(monitor, 0));
		} catch (CoreException e) {
			throw new OpcontrolException(new Status(IStatus.ERROR, OprofileRemoteLaunchPlugin.PLUGIN_ID, e.getMessage()));
		}		
	}
	
	/**
	 * Run opcontrol with specified args
	 * @return true if any output was produced on the error stream. Unfortunately
	 * this appears to currently be the only way we can tell if user correctly
	 * entered the password
	 * Will add opcontrol program to beginning of args
	 * args: list of opcontrol arguments (not including opcontrol program itself)
	 */
	@Override
	protected boolean runOpcontrol(ArrayList<String> args) throws OpcontrolException {
		args.add(0, OPCONTROL_PROGRAM);
		setVerbosityLevel(args);
		
		return runRemoteCommand(args);
	}

	protected boolean runRemoteCommand(ArrayList<String> args) throws OpcontrolException {
		args.add(ECHO_EXIT_CODE_COMMAND);
		args.add(EXIT_COMMAND);
		String command = getCommandString(args);
		try {
			if (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_MODEL)) {
				monitor.subTask("Initializing RSE"); //$NON-NLS-1$
				try {
					RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_MODEL);
				} catch (InterruptedException e) {
					throw new CoreException(new Status(IStatus.ERROR, "com.codesourcery.help",
							e.getLocalizedMessage(), e));
				}
			}

			IRemoteCmdSubSystem remoteCommandSubsystem = LaunchUtils.getRemoteCmdSubSystem(
			    config, new SubProgressMonitor(monitor, 0));			
			
			Object[] x = remoteCommandSubsystem.runCommand(command, null, monitor);
			IServiceCommandShell shell = (IServiceCommandShell)x[0]; 
/*			Object [] out = shell.listOutput();
			for (int i = out.length-1; i >=0; i--) {
				if ( out[i].toString().equals(ECHO_EXIT_CODE_COMMAND.replace("$?", "0").substring(8))) {
					return true;
				}
			}
			return false;*/
			return true;
		} catch (Exception e) { 
			throw new OpcontrolException(
					new Status(
							IStatus.ERROR, 
							OprofileRemoteLaunchPlugin.PLUGIN_ID,
							"Faled to run command " + command,
							e
						)); //$NON-NLS-1$
		}
	}
	
	public boolean runOparchive(LaunchOptions launchOptions) throws OpcontrolException {
		ArrayList<String> rmArgs = new ArrayList<String>();
		rmArgs.add(RM_COMMAND + " " + OPARCHIVE_TMP_DIR + File.separator + OPARCHIVE_DIR_NAME + " " + OPARCHIVE_TGZ_NAME);		
		if ( ! runRemoteCommand(rmArgs) ) {
			return false;
		}
		
		ArrayList<String> opArchiveArgs = new ArrayList<String>();
		opArchiveArgs.add(0, OPARCHIVE_PROGRAM);
		opArchiveArgs.add("--output-directory=" + OPARCHIVE_TMP_DIR + File.separator + OPARCHIVE_DIR_NAME);
		String s = launchOptions.getBinaryImage();
		if ( s.lastIndexOf(File.separator) != -1 ) {
			opArchiveArgs.add("--image-path=" + launchOptions.getBinaryImage().substring(0,s.lastIndexOf(File.separator)));
		}
		if ( ! runRemoteCommand(opArchiveArgs) ) {
			return false;
		}
		
		return true;
		
	}
	
	public boolean downloadOprofileResults() throws CoreException {

		// Download and extract archive
		ArrayList<String> tarArgs = new ArrayList<String>();
		tarArgs.add(TAR_COMMAND);
		tarArgs.add(OPARCHIVE_TMP_DIR + File.separator + OPARCHIVE_TGZ_NAME);
		tarArgs.add(OPARCHIVE_TMP_DIR + File.separator + OPARCHIVE_DIR_NAME);		
		if ( ! runRemoteCommand(tarArgs) ) {
			return false;
		}
		
		IPath pluginDir = OprofileRemoteLaunchPlugin.getDefault().getStateLocation();
		IPath oparchveTgzLocalPath  = pluginDir.append(OPARCHIVE_TGZ_NAME);
		try {
			// Remove previous local file with the same name
			IFileStore oparchveTgzLocalFile = EFS.getStore(URIUtil.toURI(oparchveTgzLocalPath));
			if (oparchveTgzLocalFile.fetchInfo().exists()) {
				oparchveTgzLocalFile.delete(EFS.NONE, null);
			} 
			
			// Download file with oparchive results
			downloadFile(OPARCHIVE_TMP_DIR + File.separator + OPARCHIVE_TGZ_NAME, oparchveTgzLocalPath.toOSString(), monitor);

			// Remove previous dir with oparchive data
			IFileStore oprofileResultsLocalDir = EFS.getStore(URIUtil.toURI(pluginDir.append(OPARCHIVE_TMP_DIR).append(OPARCHIVE_DIR_NAME)));
			if (oprofileResultsLocalDir.fetchInfo().exists()) {
				oprofileResultsLocalDir.delete(EFS.NONE, null);
			} 

			// Extract tgz file
			TGZUtil.extractTgz(oparchveTgzLocalPath.toOSString(), pluginDir.toOSString());
			
		} catch (IOException e) {
			throw new CoreException( new Status(
					IStatus.ERROR, 
					OprofileRemoteLaunchPlugin.PLUGIN_ID,
					"IO error extracting" + oparchveTgzLocalPath.toOSString(), 
					e) );
		}
		
		return true;
	}

	/**
	 * Download file from target
	 * @param remoteFileName absolute name of file on remote system
	 * @param localFileName absolute name of file on local system
	 * @throws CoreException 
	 */
	protected void downloadFile(String remoteFileName, String localFileName, IProgressMonitor monitor) throws CoreException {
		if (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_MODEL)) {
			monitor.subTask("Initializing RSE"); //$NON-NLS-1$
			try {
				RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_MODEL);
			} catch (InterruptedException e) {
				throw new CoreException(new Status(IStatus.ERROR, "com.codesourcery.help",
						e.getLocalizedMessage(), e));
			}
		}

		IRemoteFileSubSystem files = LaunchUtils.getRemoteFileSubSystem(
		    config, new SubProgressMonitor(monitor, 0));

		try {
			files.connect(new SubProgressMonitor(monitor, 0), false);

			IRemoteFile remoteFile = files.getRemoteFileObject(remoteFileName, new SubProgressMonitor(monitor, 0));

			if ( ! remoteFile.exists() ) {
					throw new IOException("Remote directory " + remoteFile.getAbsolutePath() + " does not exist");
			}

			files.download(remoteFile, localFileName, "UTF-8", new SubProgressMonitor(monitor, 0));

		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, OprofileRemoteLaunchPlugin.PLUGIN_ID,
					"Could not download file from target: " + remoteFileName + " to " + localFileName, e));
		}
		
	}
	
	public InputStream runOpReport(ArrayList<String> args) throws OpcontrolException {
		IPath pluginDir = OprofileRemoteLaunchPlugin.getDefault().getStateLocation();
		IPath sessionDir = pluginDir.append(OPARCHIVE_TMP_DIR.substring(1)).append(OPARCHIVE_DIR_NAME);
		sessionDir = sessionDir.append("var").append("lib").append("oprofile");
		args.add("--session-dir=" + sessionDir.toOSString());
		return super.runOpReport(args);
	}

}
