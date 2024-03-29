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

import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * <b><u>TmfCoalescedDataRequest</u></b>
 * <p>
 */
public class TmfCoalescedDataRequest<T extends ITmfEvent> extends TmfDataRequest<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	/**
	 * The list of coalesced requests
	 */
	protected Vector<ITmfDataRequest<T>> fRequests = new Vector<ITmfDataRequest<T>>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Request all the events of a given type (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     */
    public TmfCoalescedDataRequest(Class<T> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<T> dataType, ExecutionType priority) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request all the events of a given type from the given index (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type from the given index (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, ExecutionType priority) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given index (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type from the given index (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     * 
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, ExecutionType priority) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given index (high priority).
     * Events are returned in blocks of the given size.
     * 
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize) {
        super(dataType, index, nbRequested, blockSize, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type from the given index (given priority).
     * Events are returned in blocks of the given size.
     * 
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize, ExecutionType priority) {
        super(dataType, index, nbRequested, blockSize, priority);
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

	public void addRequest(ITmfDataRequest<T> request) {
		fRequests.add(request);
	}

	public boolean isCompatible(ITmfDataRequest<T> request) {

		boolean ok = request.getIndex() == getIndex();
		ok &= request.getNbRequested()  == getNbRequested();
		ok &= request.getExecType()     == getExecType();
		//ok &= request.getDataType()     == getDataType();
		
		return ok;
	}

    // ------------------------------------------------------------------------
    // ITmfDataRequest
    // ------------------------------------------------------------------------

    @Override
	public void handleData(T data) {
		super.handleData(data);
    	// Don't call sub-requests handleData() unless this is a
		// TmfCoalescedDataRequest; extended classes should call
		// the sub-requests handleData().
		if (getClass() == TmfCoalescedDataRequest.class) {
	    	for (ITmfDataRequest<T> request : fRequests) {
	    	    if (!request.isCompleted()) {
                    if (request.getDataType().isInstance(data)) {
                        request.handleData(data);
                    }
	    	    }
	    	}
		}
    }

    @Override
    public void start() {
        for (ITmfDataRequest<T> request : fRequests) {
            if (!request.isCompleted()) {
                request.start();
            }
        }
        super.start();
    }
    
	@Override
    public void done() {
    	for (ITmfDataRequest<T> request : fRequests) {
    	    if (!request.isCompleted()) {
    	        request.done();
    	    }
    	}
    	super.done();
    }

    @Override
    public void fail() {
    	for (ITmfDataRequest<T> request : fRequests) {
    		request.fail();
    	}
    	super.fail();
    }

    @Override
    public void cancel() {
    	for (ITmfDataRequest<T> request : fRequests) {
    	    if (!request.isCompleted()) {
    	        request.cancel();
    	    }
    	}
    	super.cancel();
    }
    
    @Override
    public boolean isCompleted() {
        // Firstly, check if coalescing request is completed
        if (super.isCompleted()) {
            return true;
        }

        // Secondly, check if all sub-requests are finished
        if (fRequests.size() > 0) {
            // If all sub requests are completed the coalesced request is 
            // treated as completed, too.
            for (ITmfDataRequest<T> request : fRequests) {
                if (!request.isCompleted()) {
                    return false;
                }
            }
            return true;
        }

        // Coalescing request is not finished if there are no sub-requests
        return false;
    }

    @Override
    public boolean isCancelled() {
        // Firstly, check if coalescing request is canceled
        if (super.isCancelled()) {
            return true;
        }

        // Secondly, check if all sub-requests are canceled
        if (fRequests.size() > 0) {
            // If all sub requests are canceled the coalesced request is 
            // treated as completed, too.
            for (ITmfDataRequest<T> request : fRequests) {
                if (!request.isCancelled()) {
                    return false;
                }
            }
            return true;
        }

        // Coalescing request is not canceled if there are no sub-requests
        return false;

    }

    
    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    // All requests have a unique id
    public int hashCode() {
    	return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof TmfCoalescedDataRequest<?>) {
    		TmfCoalescedDataRequest<?> request = (TmfCoalescedDataRequest<?>) other;
       		return 	(request.getDataType()    == getDataType())   &&
       				(request.getIndex()       == getIndex())      &&
       				(request.getNbRequested() == getNbRequested() &&
      				(request.getExecType()    == getExecType()));
       	}
       	return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TmfCoalescedDataRequest(" + getRequestId() + "," + getDataType().getSimpleName() 
			+ "," + getIndex() + "," + getNbRequested() + "," + getBlockSize() + ")]";
    }
}
