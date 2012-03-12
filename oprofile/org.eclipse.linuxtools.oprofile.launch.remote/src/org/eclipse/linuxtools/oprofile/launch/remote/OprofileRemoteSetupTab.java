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

import org.eclipse.cdt.launch.ui.RemotePathEntry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This tab is used by the launcher to configure global oprofile run options.
 */
public class OprofileRemoteSetupTab extends OprofileSetupTab {

	protected RemotePathEntry kernelImageRemotePath;
	
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		options.saveConfiguration(config);
		try {
			config.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void createControl(Composite parent) {
		options = new LaunchOptions();

		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		GridData data;
		GridLayout layout;
		createVerticalSpacer(top, 1);

		// Create container for kernel image file selection
		Composite p = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		p.setLayoutData(data);

		Label l = new Label(p, SWT.NONE);
		l.setText(OprofileLaunchMessages.getString("tab.global.kernelImage.label.text")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		kernelImageRemotePath = new RemotePathEntry(
				p, 
				"", 
				OprofileLaunchMessages.getString("tab.global.kernelImage.label.text"),
				false);	
		
		GridData gd3 = (GridData)kernelImageRemotePath.getLayoutData();
		gd3 = (gd3 == null) ? new GridData() : gd3;
		gd3.horizontalAlignment = GridData.FILL;
		kernelImageRemotePath.setLayoutData(gd3);
		
		kernelImageRemotePath.setChangeListener(new RemotePathEntry.ChangeListener() {

			public void changed() {
				handleKernelImageRemotePathModify();
			}
		});		


		createVerticalSpacer(top, 1);

		// Create checkbox options container
		p = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		p.setLayoutData(data);

		checkSeparateLibrary = createCheckButton(p, OprofileLaunchMessages.getString("tab.global.check.separateLibrary.text")); //$NON-NLS-1$
		checkSeparateKernel = createCheckButton(p, OprofileLaunchMessages.getString("tab.global.check.separateKernel.text")); //$NON-NLS-1$		
		
	}

	public void initializeFrom(ILaunchConfiguration config) {
		options.loadConfiguration(config);
		
		kernelImageRemotePath.setText(options.getKernelImageFile());
		
		int separate = options.getSeparateSamples();
		
		if (separate == OprofileDaemonOptions.SEPARATE_NONE) {
			checkSeparateLibrary.setSelection(false);
			checkSeparateKernel.setSelection(false);
		} else {
			//note that opcontrol will nicely ignore the trailing comma
			if ((separate & OprofileDaemonOptions.SEPARATE_LIBRARY) != 0)
				checkSeparateLibrary.setSelection(true);
			if ((separate & OprofileDaemonOptions.SEPARATE_KERNEL) != 0)
				checkSeparateKernel.setSelection(true);
		}
	}
	
	private void handleKernelImageRemotePathModify() {
		options.setKernelImageFile(kernelImageRemotePath.getText());

/*		FIXME: check if remote path to kernel image is incorrect and show error
			if ( ) {
			String msg = OprofileLaunchMessages.getString("tab.global.kernelImage.kernel.nonexistent"); //$NON-NLS-1$
			Object[] args = new Object[] { filename };
			String errorMessage = MessageFormat.format(msg, args);
			
			setErrorMessage(errorMessage);
			updateLaunchConfigurationDialog();
		}
		*/
	}

}
