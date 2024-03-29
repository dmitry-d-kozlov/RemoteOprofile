/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.util.Collection;
import java.util.ListIterator;
import java.util.Vector;

/**
 * <b><u>StreamInputPacketIndex</u></b>
 * <p>
 * TODO Implement me. Please.
 */
public class StreamInputPacketIndex {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Entries of the index. They are sorted by increasing begin timestamp.
     * index builder.
     */
    private final Vector<StreamInputPacketIndexEntry> entries = new Vector<StreamInputPacketIndexEntry>();

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public Collection<StreamInputPacketIndexEntry> getEntries() {
        return this.entries;
    }

    public ListIterator<StreamInputPacketIndexEntry> listIterator() {
        return this.entries.listIterator();
    }

    public ListIterator<StreamInputPacketIndexEntry> listIterator(int n) {
        return this.entries.listIterator(n);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds an entry to the index.
     *
     * @param entry
     *            The entry to add
     * @throws CTFReaderException
     */
    public void addEntry(StreamInputPacketIndexEntry entry)
            throws CTFReaderException {
        assert (entry.packetSizeBits != 0);
        assert (entry.contentSizeBits != 0);

        if (entry.timestampBegin > entry.timestampEnd) {
            throw new CTFReaderException(
                    "Packet begin timestamp is after end timestamp"); //$NON-NLS-1$
        }

        if (!this.entries.isEmpty()) {
            if (entry.timestampBegin < this.entries.lastElement().timestampBegin) {
                throw new CTFReaderException(
                        "Packets begin timestamp decreasing"); //$NON-NLS-1$
            }
        }

        this.entries.add(entry);
    }

    /**
     * Given a timestamp, this methods returns the first PacketIndexEntry that
     * could include the timestamp, that is the last packet with a begin
     * timestamp smaller than the given timestamp.
     *
     * @param timestamp
     *            The timestamp to look for.
     * @return The StreamInputPacketEntry that corresponds to the packet that
     *         includes the given timestamp.
     */
    public ListIterator<StreamInputPacketIndexEntry> search(final long timestamp) {
        /*
         * Start with min and max covering all the elements.
         */
        int max = this.entries.size() - 1;
        int min = 0;

        int guessI;
        StreamInputPacketIndexEntry guessEntry = null;

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp is negative"); //$NON-NLS-1$
        }

        for (;;) {
            /*
             * Guess in the middle of min and max. The +1 is so that in case
             * (min + 1 == max), we choose the packet at the subscript "max"
             * instead of the one at "min". Otherwise, it would give an infinite
             * loop.
             */
            guessI = (max + min + 1) / 2;
            guessEntry = this.entries.get(guessI);

            /*
             * If we reached the point where we focus on a single packet, our
             * search is done.
             */
            if (min == max) {
                break;
            }

            if (timestamp < guessEntry.timestampBegin) {
                /*
                 * If the timestamp if before the begin timestamp, we know that
                 * the packet to return is before the guess.
                 */
                max = guessI - 1;
            } else if (timestamp >= guessEntry.timestampBegin) {
                /*
                 * If the timestamp is after the begin timestamp, we know that
                 * the packet to return is after the guess or is the guess.
                 */
                min = guessI;
            }
        }

        return this.entries.listIterator(guessI);
    }

}
