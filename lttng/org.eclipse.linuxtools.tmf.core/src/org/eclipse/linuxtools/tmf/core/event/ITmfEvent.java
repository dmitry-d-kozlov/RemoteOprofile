/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * <b><u>ITmfEvent</u></b>
 * <p>
 * The basic event structure in the TMF. In its canonical form, a data item has:
 * <ul>
 * <li> a parent trace
 * <li> a rank (order within the trace)
 * <li> a timestamp
 * <li> a source (reporting component)
 * <li> a type
 * <li> a content (payload)
 * </ul>
 * For convenience, a free-form reference field is also provided. It could be
 * used as e.g. a location marker (filename:lineno) to indicate where the event
 * was generated.
 */
public interface ITmfEvent extends Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Pre-defined event attributes 
     */
    public static final String EVENT_FIELD_TIMESTAMP = ":timestamp:"; //$NON-NLS-1$
    public static final String EVENT_FIELD_SOURCE    = ":source:";    //$NON-NLS-1$
    public static final String EVENT_FIELD_TYPE      = ":type:";      //$NON-NLS-1$
    public static final String EVENT_FIELD_CONTENT   = ":content:";   //$NON-NLS-1$
    public static final String EVENT_FIELD_REFERENCE = ":reference:"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace that 'owns' the event
     */
    public ITmfTrace<?> getTrace();

    /**
     * @return the event rank within the parent trace
     */
    public long getRank();

    /**
     * @return the event timestamp
     */
    public ITmfTimestamp getTimestamp();

    /**
     * @return the event source
     */
    public String getSource();

    /**
     * @return the event type
     */
    public ITmfEventType getType();

    /**
     * @return the event content
     */
    public ITmfEventField getContent();

    /**
     * @return the event reference
     */
    public String getReference();

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /**
     * @return a clone of the event
     */
    public ITmfEvent clone();
    
}
