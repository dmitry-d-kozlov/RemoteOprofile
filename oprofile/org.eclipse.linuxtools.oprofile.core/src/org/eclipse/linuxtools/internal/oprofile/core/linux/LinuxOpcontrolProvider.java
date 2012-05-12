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
 *    Dmitry Kozlov <ddk@codesourcery.com> - added runOpReport, runOpHelp, runAddr2Line
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileConstants;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileInfoProvider;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileProperties;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionManager;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;
import org.eclipse.osgi.util.NLS;

/**
 * A class which encapsulates running opcontrol.
 */
public class LinuxOpcontrolProvider implements IOpcontrolProvider {
	private final String opcontrolProgram;

	// Logging verbosity. Specified with setupDaemon.
	//--verbosity=all generates WAY too much stuff in the log
	private String verbosity = ""; //$NON-NLS-1$

	// Project that will be profiled.
	public static IProject currentProject;
	static {
		initializeOprofileModule();
	}
	
	
	public LinuxOpcontrolProvider() throws OpcontrolException {
		opcontrolProgram = findOpcontrol();
	}

	/**
	 * Unload the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void deinitModule() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_DEINIT_MODULE);
	}
	
	/**
	 * Dump collected profiling data
	 * @throws OpcontrolException
	 */
	public void dumpSamples() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_DUMP);
	}
	
	/**
	 * Loads the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void initModule() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_INIT_MODULE);
	}
	
	/**
	 * Clears out data from current session
	 * @throws OpcontrolException
	 */
	public void reset() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_RESET);
	}
	
	/**
	 * Saves the current ("default") session
	 * @param name	the name to which to save the session
	 * @throws OpcontrolException
	 */
	public void saveSession(String name) throws OpcontrolException {
		SessionManager sessMan;
		try {
			sessMan = new SessionManager(SessionManager.SESSION_LOCATION);
			for (String event : sessMan.getSessionEvents(SessionManager.CURRENT)){
				sessMan.addSession(name, event);
				String oldFile = SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + event + SessionManager.CURRENT;
				String newFile = SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + event + name;
				Process p = Runtime.getRuntime().exec("cp " + oldFile + " " + newFile);
				p.waitFor();
			}
			sessMan.write();
		} catch (FileNotFoundException e) {
			//intentionally blank
			//during a save, the session file will exist
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete the session with the specified name for the specified event
	 * @param sessionName The name of the session to delete
	 * @param eventName The name of the event containing the session
	 * @throws OpcontrolException
	 */
	public void deleteSession (String sessionName, String eventName) throws OpcontrolException {
		File file = new File (SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + eventName + sessionName);
		file.delete();
		SessionManager sessMan = new SessionManager(SessionManager.SESSION_LOCATION);
		sessMan.removeSession(sessionName, eventName);
		sessMan.write();
	}

	/**
	 * Give setup aruments
	 * @param args	list of parameters for daemon
	 * @throws OpcontrolException
	 */
	public void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException {
		// Convert options & events to arguments for opcontrol
		ArrayList<String> args = new ArrayList<String>();
		args.add(OprofileConstants.OPD_SETUP);
		optionsToArguments(args, options);
		if (!OprofileInfoProvider.getTimerMode()) {
			if (events == null || events.length == 0) {
				args.add(OprofileConstants.OPD_SETUP_EVENT + OprofileConstants.OPD_SETUP_EVENT_DEFAULT);
			} else {
				for (int i = 0; i < events.length; ++i) {
					eventToArguments(args, events[i]);
				}
			}
		}
		runOpcontrol(args);
	}
	
	/**
	 * Stop data collection and remove daemon
	 * @throws OpcontrolException
	 */
	public void shutdownDaemon() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_SHUTDOWN);
	}
	
	/**
	 * Start data collection (will start daemon if necessary)
	 * @throws OpcontrolException
	 */
	public void startCollection() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_START_COLLECTION);
	}
	
	/**
	 * Start daemon without starting profiling
	 * @throws OpcontrolException
	 */
	public void startDaemon() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_START_DAEMON);
	}
	
	/**
	 * Stop data collection
	 * @throws OpcontrolException
	 */
	public void stopCollection() throws OpcontrolException {
		runOpcontrol(OprofileConstants.OPD_STOP_COLLECTION);
	}

	/**
	 * Check status. returns true if any status was returned
	 * @throws OpcontrolException
	 */
	public boolean status() throws OpcontrolException {
		return runOpcontrol(OprofileConstants.OPD_HELP);
	}
	
	// Convenience function
	private boolean runOpcontrol(String cmd) throws OpcontrolException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(cmd);
		return runOpcontrol(list);
	}
	
	// Will add opcontrol program to beginning of args
	// args: list of opcontrol arguments (not including opcontrol program itself)
	/**
	 * @return true if any output was produced on the error stream. Unfortunately
	 * this appears to currently be the only way we can tell if user correctly
	 * entered the password
	 */
	private boolean runOpcontrol(ArrayList<String> args) throws OpcontrolException {	
		IProject project = LinuxOpcontrolProvider.getCurrentProject();
		
		
		// If no linuxtools' toolchain is defined for this project, use the path for the
		// link created by the installation script
		if(project == null || LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
			args.add(0, opcontrolProgram);
		} else{
			args.add(0, OprofileConstants.OPCONTROL_EXECUTABLE);
		}

		// Verbosity hack. If --start or --start-daemon, add verbosity, if set
		String cmd = (String) args.get(1);
		if (verbosity.length() > 0 && (cmd.equals (OprofileConstants.OPD_START_COLLECTION) || cmd.equals(OprofileConstants.OPD_START_DAEMON))) {
			args.add(verbosity);
		}
		
		String[] cmdArray = new String[args.size()];
		args.toArray(cmdArray);
		
		// Print what is passed on to opcontrol
		if (OprofileCorePlugin.isDebugMode()) {
			printOpcontrolCmd(cmdArray);
		}
		
		Process p = null;
		try {
			if(project == null || LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
				p = Runtime.getRuntime().exec(cmdArray);
			} else{
				p = RuntimeProcessFactory.getFactory().sudoExec(cmdArray, project);
			}
		} catch (IOException ioe) {			
			throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolRun", ioe)); //$NON-NLS-1$
		}
		
		if (p != null) {
			BufferedReader errout = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String errOutput = ""; //$NON-NLS-1$
			String output = "", s; //$NON-NLS-1$
			try {
				while ((s = errout.readLine()) != null) {
					errOutput += s + "\n"; //$NON-NLS-1$
				}
				// Unfortunately, when piped through consolehelper stderr output
				// is redirected to stdout. Need to read stdout and do some
				// string matching in order to give some better advice as to how to
				// alleviate the nmi_watchdog problem. See RH BZ #694631
				while ((s = stdout.readLine()) != null) {
					output += s + "\n"; //$NON-NLS-1$
				}
				stdout.close();
				errout.close();

				int ret = p.waitFor();
				if (ret != 0) {
					OpControlErrorHandler errHandler = OpControlErrorHandler.getInstance();
					OpcontrolException ex = errHandler.handleError(output, errOutput);
					throw ex;
				}
				
				if (errOutput.length() != 0) {
					return true;
				}
				
			} catch (IOException ioe) { 
				ioe.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Print to stdout what is passed on to opcontrol.
	 * 
	 * @param cmdArray
	 */
	private void printOpcontrolCmd(String[] cmdArray) {
		StringBuffer buf = new StringBuffer();
		for (String token: cmdArray) {
			buf.append(token);
			buf.append(" ");
		}
		System.out.println(OprofileCorePlugin.DEBUG_PRINT_PREFIX + buf.toString());
	}
	
	private static String findOpcontrol() throws OpcontrolException {
		IProject project = LinuxOpcontrolProvider.getCurrentProject();
		URL url = FileLocator.find(Platform.getBundle(OprofileCorePlugin
				.getId()), new Path(OprofileConstants.OPCONTROL_REL_PATH), null);

		if (url != null) {
			try {
				return FileLocator.toFileURL(url).getPath();
			} catch (IOException ignore) {
			}
		// If no linuxtools' toolchain is defined for this project and oprofile is not
		// installed, throw exception
		} else if(project == null || LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
			throw new OpcontrolException(OprofileCorePlugin.createErrorStatus(
					"opcontrolProvider", null)); //$NON-NLS-1$
		}

		return null;
	}      

	// Convert the event into arguments for opcontrol
	private void eventToArguments(ArrayList<String> args, OprofileDaemonEvent event) {
		// Event spec: "EVENT:count:mask:profileKernel:profileUser"
		String spec = new String(OprofileConstants.OPD_SETUP_EVENT);
		spec += event.getEvent().getText();
		spec += OprofileConstants.OPD_SETUP_EVENT_SEPARATOR;
		spec += event.getResetCount();
		spec += OprofileConstants.OPD_SETUP_EVENT_SEPARATOR;
		spec += event.getEvent().getUnitMask().getMaskValue();
		spec += OprofileConstants.OPD_SETUP_EVENT_SEPARATOR;
		spec += (event.getProfileKernel() ? OprofileConstants.OPD_SETUP_EVENT_TRUE : OprofileConstants.OPD_SETUP_EVENT_FALSE);
		spec += OprofileConstants.OPD_SETUP_EVENT_SEPARATOR;
		spec += (event.getProfileUser() ? OprofileConstants.OPD_SETUP_EVENT_TRUE : OprofileConstants.OPD_SETUP_EVENT_FALSE);
		args.add(spec);
	}
	
	// Convert the options into arguments for opcontrol
	private void optionsToArguments(ArrayList<String> args, OprofileDaemonOptions options) {
		// Add separate flags
		int mask = options.getSeparateProfilesMask();

		String separate = new String(OprofileConstants.OPD_SETUP_SEPARATE);
		
		if (mask == OprofileDaemonOptions.SEPARATE_NONE) {
			separate += OprofileConstants.OPD_SETUP_SEPARATE_NONE;
		} else {
			//note that opcontrol will nicely ignore the trailing comma
			if ((mask & OprofileDaemonOptions.SEPARATE_LIBRARY) != 0)
				separate += OprofileConstants.OPD_SETUP_SEPARATE_LIBRARY + OprofileConstants.OPD_SETUP_SEPARATE_SEPARATOR;
			if ((mask & OprofileDaemonOptions.SEPARATE_KERNEL) != 0)
				separate += OprofileConstants.OPD_SETUP_SEPARATE_KERNEL + OprofileConstants.OPD_SETUP_SEPARATE_SEPARATOR;
			if ((mask & OprofileDaemonOptions.SEPARATE_THREAD) != 0)
				separate += OprofileConstants.OPD_SETUP_SEPARATE_THREAD + OprofileConstants.OPD_SETUP_SEPARATE_SEPARATOR;
			if ((mask & OprofileDaemonOptions.SEPARATE_CPU) != 0)
				separate += OprofileConstants.OPD_SETUP_SEPARATE_CPU + OprofileConstants.OPD_SETUP_SEPARATE_SEPARATOR;
		}
		args.add(separate);
		
		// Add kernel image
		if (options.getKernelImageFile() == null || options.getKernelImageFile().length() == 0) {
			args.add(OprofileConstants.OPD_KERNEL_NONE);
		} else {
			args.add(OprofileConstants.OPD_KERNEL_FILE + options.getKernelImageFile());
		}

		//image filter -- always non-null
		args.add(OprofileConstants.OPD_SETUP_IMAGE + options.getBinaryImage());
		
		//callgraph depth
		args.add(OprofileConstants.OPD_CALLGRAPH_DEPTH + options.getCallgraphDepth());
	}

	/**
     * Run opreport command
     * @param args opreport arguments except -X flag, which is added by this method 
     * @return
	 * @throws OpcontrolException
	 */
	public InputStream runOpReport(ArrayList<String> args) throws OpcontrolException {
		args.add(0, "opreport"); //$NON-NLS-1$
		args.add(1, "-X");       //$NON-NLS-1$
		return runCommand(args);
	}

	public static IProject getCurrentProject(){
		return currentProject;
	}

	// Reloads oprofile modules by calling 'opcontrol --deinit' and 'opcontrol --init'
	public static void setCurrentProject(IProject project){

		if(currentProject == null){
			currentProject = project;
			initializeOprofileModule();
		} else {
			String currentPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(currentProject);
			String newPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);

			if(!currentPath.equals(newPath)){
				try {
					OprofileCorePlugin.getDefault().getOpcontrolProvider().deinitModule();
					currentProject = project;
					OprofileCorePlugin.getDefault().getOpcontrolProvider().initModule();
				} catch (OpcontrolException e) {
					OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
				}
				currentProject = project;
			}
		}
	}

	// Initializes static data for oprofile.
	public static void initializeOprofileCore () {
		if (isKernelModuleLoaded()){
			OprofileInfoProvider.info = OprofileInfoProvider.getInfo();

			if (OprofileInfoProvider.info == null) {
				throw new ExceptionInInitializerError(OprofileProperties.getString("fatal.opinfoNotParsed")); //$NON-NLS-1$
			}
		}
	}

	// initialize oprofile module by calling `opcontrol --init`
	public static void initializeOprofile() {
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().initModule();
		} catch (OpcontrolException e) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
		}
	}

	// This requires more inside knowledge about Oprofile than one would like,
	// but it is the only way of knowing whether the module is loaded (and we can
	// succesfully call into the oprofile wrapper library without causing it to print out
	// a lot of warnings).
	public static boolean isKernelModuleLoaded() {
		for (int i = 0; i < OprofileConstants.OPROFILE_CPU_TYPE_FILES.length; ++i) {
			File f = new File(OprofileConstants.OPROFILE_CPU_TYPE_FILES[i]);
			if (f.exists())
				return true;
		}

		return false;
	}

	/**
		 * Initialize the oprofile module
		 *
		 * This function will check if the kernel module is
		 * loaded. If it is not, it will attempt to load it
		 * (which will cause the system to prompt the user for
		 * root access).
		 */
		public static void initializeOprofileModule() {
			// Check if kernel module is loaded, if not, try to load it
			if (!LinuxOpcontrolProvider.isKernelModuleLoaded())
				LinuxOpcontrolProvider.initializeOprofile();

			//it still may not have loaded, if not, critical error
			if (!LinuxOpcontrolProvider.isKernelModuleLoaded()) {
				OprofileCorePlugin.showErrorDialog("oprofileInit", null); //$NON-NLS-1$
	//			throw new ExceptionInInitializerError(OprofileProperties.getString("fatal.kernelModuleNotLoaded")); //$NON-NLS-1$
			}  else {
				LinuxOpcontrolProvider.initializeOprofileCore();
			}
		}

	public InputStream runOpHelp(ArrayList<String> args) throws OpcontrolException {
		args.add(0, "ophelp"); //$NON-NLS-1$
		args.add(1, "-X");       //$NON-NLS-1$
		return runCommand(args);
	}

	public InputStream runAddr2Line(ArrayList<String> args)	throws OpcontrolException {
		args.add(0, "addr2line"); //$NON-NLS-1$
		args.add(1, "-e");       //$NON-NLS-1$
		return runCommand(args);
	}

	protected InputStream runCommand(ArrayList<String> args) throws OpcontrolException {
		final StringBuilder output = new StringBuilder();
		final StringBuilder errorOutput = new StringBuilder();
		Thread errReaderThread = null;
		try {
			Process p = RuntimeProcessFactory.getFactory().exec(args.toArray(new String[]{}), LinuxOpcontrolProvider.getCurrentProject());

			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			try {
				// Read output of command
				errReaderThread = new Thread(new Runnable() {
					      public void run(){
								String line = null;
								try {
									while (((line = stdError.readLine()) != null)) {
										errorOutput.append(line + System.getProperty("line.separator")); //$NON-NLS-1$
									}
								} catch (IOException e) {
								}
					      }
					    });
				errReaderThread.start();

				String s = null;
				while ((s = stdInput.readLine()) != null) {
					output.append(s + System.getProperty("line.separator")); //$NON-NLS-1$
				}

			} finally {
				stdInput.close();
				stdError.close();

				if(errReaderThread != null) {
					try {
						errReaderThread.join();
					} catch (InterruptedException e) {
					}
				}
			}
			int exitCode = p.waitFor();
			if (exitCode != 0){
				throw new OpcontrolException(new Status(IStatus.ERROR, OprofileCorePlugin.getId(),
					NLS.bind(OprofileProperties.getString("command.error.nonZeroExitCode"),args.get(0),exitCode), null));  //$NON-NLS-1$ 
			}
		} catch (IOException e) {
			throw new OpcontrolException(new Status(IStatus.ERROR, OprofileCorePlugin.getId(),
					NLS.bind(OprofileProperties.getString("command.error.ioException"),args.get(0)), e));  //$NON-NLS-1$
		} catch (InterruptedException e) {
		}

		if (!errorOutput.toString().trim().equals("")) { //$NON-NLS-1$
			throw new OpcontrolException(new Status(IStatus.ERROR, OprofileCorePlugin.getId(),
				NLS.bind(OprofileProperties.getString("command.error.nonEmptyStderr"), args.get(0), errorOutput.toString().trim()))); //$NON-NLS-1$
		}

		// convert the string to inputstream to pass
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(output.toString().getBytes("UTF-8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new OpcontrolException(new Status(IStatus.ERROR, OprofileCorePlugin.getId(),
					NLS.bind(OprofileProperties.getString("command.error.cantConvertEncoding"), args.get(0)), e)); //$NON-NLS-1$
		}

		return is;
	}

}
