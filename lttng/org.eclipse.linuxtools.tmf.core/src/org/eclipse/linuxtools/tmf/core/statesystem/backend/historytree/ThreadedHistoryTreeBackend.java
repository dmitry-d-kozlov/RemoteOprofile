/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.linuxtools.tmf.core.statesystem.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * Variant of the HistoryTreeBackend which runs all the interval-insertion logic
 * in a separate thread.
 * 
 * @author alexmont
 * 
 */
public class ThreadedHistoryTreeBackend extends HistoryTreeBackend implements
        Runnable {

    /*
     * From superclass:
     * 
     * protected final StateHistoryTree sht;
     */

    private BlockingQueue<HTInterval> intervalQueue;
    private final Thread shtThread;

    /**
     * New state history constructor
     * 
     * Note that it usually doesn't make sense to use a Threaded HT if you're
     * opening an existing state-file, but you know what you're doing...
     * 
     * @param newStateFile
     *            The name of the history file that will be created. Should end
     *            in ".ht"
     * @param blockSize
     *            The size of the blocks in the file
     * @param maxChildren
     *            The maximum number of children allowed for each core node
     * @param startTime
     *            The earliest timestamp stored in the history
     * @param queueSize
     *            The size of the interval insertion queue. 2000 - 10000 usually
     *            works well
     * @throws IOException
     *             If there was a problem opening the history file for writing
     */
    public ThreadedHistoryTreeBackend(File newStateFile, int blockSize,
            int maxChildren, long startTime, int queueSize) throws IOException {
        super(newStateFile, blockSize, maxChildren, startTime);

        intervalQueue = new ArrayBlockingQueue<HTInterval>(queueSize);
        shtThread = new Thread(this, "History Tree Thread"); //$NON-NLS-1$
        shtThread.start();
    }

    /**
     * New State History constructor. This version provides default values for
     * blockSize and maxChildren.
     * 
     * @param newStateFile
     *            The name of the history file that will be created. Should end
     *            in ".ht"
     * @param startTime
     *            The earliest timestamp stored in the history
     * @param queueSize
     *            The size of the interval insertion queue. 2000 - 10000 usually
     *            works well
     * @throws IOException
     *             If there was a problem opening the history file for writing
     */
    public ThreadedHistoryTreeBackend(File newStateFile, long startTime,
            int queueSize) throws IOException {
        super(newStateFile, startTime);

        intervalQueue = new ArrayBlockingQueue<HTInterval>(queueSize);
        shtThread = new Thread(this, "History Tree Thread"); //$NON-NLS-1$
        shtThread.start();
    }

    /*
     * The Threaded version does not specify an "existing file" constructor,
     * since the history is already built (and we only use the other thread
     * during building). Just use a plain HistoryTreeProvider in this case.
     * 
     * TODO but what about streaming??
     */

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        /*
         * Here, instead of directly inserting the elements in the History Tree
         * underneath, we'll put them in the Queue. They will then be taken and
         * processed by the other thread executing the run() method.
         */
        HTInterval interval = new HTInterval(stateStartTime, stateEndTime,
                quark, (TmfStateValue) value);
        try {
            intervalQueue.put(interval);
        } catch (InterruptedException e) {
            /* We should not get interrupted here */
            System.out.println("State system got interrupted!"); //$NON-NLS-1$
            e.printStackTrace();
        }
    }

    @Override
    public void finishedBuilding(long endTime) throws TimeRangeException {
        /*
         * We need to commit everything in the History Tree and stop the
         * standalone thread before returning to the StateHistorySystem. (SHS
         * will then write the Attribute Tree to the file, that must not happen
         * at the same time we are writing the last nodes!)
         */

        /*
         * Send the "poison pill" in the queue, then wait for the HT to finish
         * its closeTree()
         */
        try {
            intervalQueue.put(new HTInterval(-1, endTime, -1,
                    TmfStateValue.nullValue()));
            shtThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void run() {
        if (intervalQueue == null) {
            System.err.println("Cannot start the storage backend without its interval queue."); //$NON-NLS-1$
            return;
        }
        HTInterval currentInterval;
        try {
            currentInterval = intervalQueue.take();
            while (currentInterval.getStartTime() != -1) {
                /* Send the interval to the History Tree */
                sht.insertInterval(currentInterval);
                currentInterval = intervalQueue.take();
            }
            assert (currentInterval.getAttribute() == -1);
            /*
             * We've been told we're done, let's write down everything and quit
             */
            sht.closeTree();
            return;
        } catch (InterruptedException e) {
            /* We've been interrupted abnormally */
            System.out.println("State History Tree interrupted!"); //$NON-NLS-1$
            e.printStackTrace();
        } catch (TimeRangeException e) {
            /* This also should not happen */
            e.printStackTrace();
        }
    }

}
