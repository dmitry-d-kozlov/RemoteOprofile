/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public interface IRemoteProxyManager {
	public IRemoteFileProxy getFileProxy(IProject project) throws CoreException;
	public IRemoteCommandLauncher getLauncher(IProject project) throws CoreException;
	public String getOS(IProject project) throws CoreException;
}