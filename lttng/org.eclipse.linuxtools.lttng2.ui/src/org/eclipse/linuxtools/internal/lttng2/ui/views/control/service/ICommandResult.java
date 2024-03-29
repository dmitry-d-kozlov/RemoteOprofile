/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 *********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.service;

/**
 * <b><u>IommandResult</u></b>
 * <p>
 * Interface for providing command execution result.
 * </p>
 */
public interface ICommandResult {
    /**
     * The result of the command. 
     * @return 0 if successful else >0 
     */
    public int getResult() ;

    /**
     * Sets the command result value.
     * @param result
     */
    public void setResult(int result);

    /**
     * @return returns the command output.
     */
    public String[] getOutput();

    /**
     * Sets the command output.
     * @param output
     */
    public void setOutput(String[] output);
}