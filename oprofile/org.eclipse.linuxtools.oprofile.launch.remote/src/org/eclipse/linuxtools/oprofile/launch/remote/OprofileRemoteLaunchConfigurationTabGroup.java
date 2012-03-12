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
import java.util.Arrays;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.linuxtools.internal.profiling.launch.remote.ProfileRemoteLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.internal.profiling.launch.remote.RemoteArgumentsTab;
import org.eclipse.linuxtools.oprofile.launch.configuration.OprofileEventConfigTab;
import org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab;

public class OprofileRemoteLaunchConfigurationTabGroup extends ProfileRemoteLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new MainTab(true));
		tabs.add(new RemoteArgumentsTab());
		
		tabs.addAll(Arrays.asList(getProfileTabs()));
		
		tabs.add(new EnvironmentTab());
		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());
		
		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}
	
	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		return new AbstractLaunchConfigurationTab[] { 
							new OprofileRemoteSetupTab(), 
							new OprofileEventConfigTab() };
	}

}
