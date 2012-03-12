package org.eclipse.linuxtools.oprofile.launch.remote;

import org.eclipse.cdt.launch.ui.CMainTab;

/** 
 * Extends CodeSourcery's modified CMainTab from CDT
 * to set allowsRemote to always true. 
 */
public class MainTab extends CMainTab {
	
	public MainTab(boolean terminalOption) {
		super(terminalOption ? WANTS_TERMINAL : 0);
	}

	protected boolean allowsRemote() {
		return true;
	}
	
}
