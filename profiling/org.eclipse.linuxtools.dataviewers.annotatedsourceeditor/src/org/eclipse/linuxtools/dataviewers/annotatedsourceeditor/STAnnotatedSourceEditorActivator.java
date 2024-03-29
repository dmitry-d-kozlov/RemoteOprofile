/*******************************************************************************
      * Copyright (c) 2009 STMicroelectronics.
      * All rights reserved. This program and the accompanying materials
      * are made available under the terms of the Eclipse Public License v1.0
      * which accompanies this distribution, and is available at
      * http://www.eclipse.org/legal/epl-v10.html
      *
      * Contributors:
      *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
      
**********************************************************************
*********/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class STAnnotatedSourceEditorActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.dataviewers.annotatedsourceeditor";
	
	private static final String ANNOTATION_TYPE = ".coloredLines";

	// The shared instance
	private static STAnnotatedSourceEditorActivator plugin;
	
	/**
	 * The constructor
	 */
	public STAnnotatedSourceEditorActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		EditorsUI.getPreferenceStore().setValue("STRuler",true);
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
	public static STAnnotatedSourceEditorActivator getDefault() {
		return plugin;
	}
	
	public static String getAnnotationType()
    {
        return ANNOTATION_TYPE;
    }
	
	public static String getUniqueIdentifier()
    {
        return PLUGIN_ID;
    }

}
