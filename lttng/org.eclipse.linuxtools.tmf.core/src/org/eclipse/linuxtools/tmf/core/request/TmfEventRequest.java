/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * <b><u>TmfEventRequest</u></b>
 * <p>
 * Implement me. Please.
 */
public abstract class TmfEventRequest<T extends ITmfEvent> extends TmfDataRequest<T> implements ITmfEventRequest<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfTimeRange fRange;	// The requested events time range

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Request all the events of a given type (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     */
    public TmfEventRequest(Class<T> dataType) {
        this(dataType, TmfTimeRange.ETERNITY, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param priority the requested execution priority
     */
    public TmfEventRequest(Class<T> dataType, ExecutionType priority) {
        this(dataType, TmfTimeRange.ETERNITY, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request all the events of a given type for the given time range (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range) {
        this(dataType, range, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type for the given time range (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param priority the requested execution priority
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, ExecutionType priority) {
        this(dataType, range, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given time range (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested) {
        this(dataType, range, 0, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }
    
    /**
     * Request 'n' events of a given type for the given time range (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     * @param priority the requested execution priority
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, ExecutionType priority) {
        this(dataType, range, 0, nbRequested, DEFAULT_BLOCK_SIZE, priority);
    }
    
    /**
     * Request 'n' events of a given type for the given time range (high priority).
     * Events are returned in blocks of the given size.
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize) {
    	this(dataType, range, 0, nbRequested, blockSize, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type for the given time range (given priority).
     * Events are returned in blocks of the given size.
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize, ExecutionType priority) {
    	this(dataType, range, 0, nbRequested, blockSize, priority);
    }

    /**
     * Request 'n' events of a given type for the given time range (given priority).
     * Events are returned in blocks of the given size.
     * 
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int index, int nbRequested, int blockSize, ExecutionType priority) {
    	super(dataType, index, nbRequested, blockSize, priority);
    	fRange = range;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the requested time range
     */
    @Override
	public TmfTimeRange getRange() {
        return fRange;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * this method is called by the event provider to set the index corresponding
     * to the time range start time once it is known
     * 
     * @param index the start index
     */
    @Override
	public void setStartIndex(int index) {
    	setIndex(index);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    // All requests have a unique id
    public int hashCode() {
    	return getRequestId();
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof TmfEventRequest<?>) {
    		TmfEventRequest<?> request = (TmfEventRequest<?>) other;
    		return super.equals(other) && request.fRange.equals(fRange);
    	}
    	return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TmfEventRequest(" + getRequestId() + "," + getDataType().getSimpleName() 
			+ "," + getRange() + "," + getIndex() + "," + getNbRequested() + "," + getBlockSize() + ")]";
    }

}
