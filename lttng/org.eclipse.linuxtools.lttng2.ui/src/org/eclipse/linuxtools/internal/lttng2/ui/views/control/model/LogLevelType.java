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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model;

/**
 * <b><u>LogLevelType</u></b>
 * <p>
 * Type of log Level enumeration.
 * </p>
 */
public enum LogLevelType {
    
    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    LOGLEVEL, // range of log levels [0,logLevel]
    LOGLEVEL_ONLY, // single log level
    LOGLEVEL_NONE; // no log level
};


