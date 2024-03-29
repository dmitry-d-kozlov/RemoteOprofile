/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Thomas Gatterweh	- Updated scaling / synchronization
 *   Francois Chouinard - Refactoring to align with TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * <b><u>TmfTimestamp</u></b>
 * <p>
 * A generic implementation of ITmfTimestamp.
 */
public class TmfTimestamp implements ITmfTimestamp {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The beginning of time
     */
    public static final ITmfTimestamp BIG_BANG = 
            new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE, 0);

    /**
     * The end of time
     */
    public static final ITmfTimestamp BIG_CRUNCH = 
            new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE, 0);
    
    /**
     * Zero
     */
    public static final ITmfTimestamp ZERO = 
            new TmfTimestamp(0, 0, 0);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The timestamp raw value (mantissa)
     */
    protected long fValue;

    /**
     * The timestamp scale (magnitude)
     */
    protected int fScale;

    /**
     * The value precision (tolerance)
     */
    protected int fPrecision;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfTimestamp() {
        this(0, 0, 0);
    }

    /**
     * Simple constructor (scale = precision = 0)
     *
     * @param value the timestamp value
     */
    public TmfTimestamp(long value) {
        this(value, 0, 0);
    }

    /**
     * Simple constructor (precision = 0)
     * 
     * @param value the timestamp value
     * @param scale the timestamp scale
     */
    public TmfTimestamp(long value, int scale) {
        this(value, scale, 0);
    }

    /**
     * Full constructor
     * 
     * @param value the timestamp value
     * @param scale the timestamp scale
     * @param precision the timestamp precision
     */
    public TmfTimestamp(long value, int scale, int precision) {
        fValue = value;
        fScale = scale;
        fPrecision = Math.abs(precision);
    }

    /**
     * Copy constructor
     * 
     * @param timestamp the timestamp to copy
     */
    public TmfTimestamp(ITmfTimestamp timestamp) {
        if (timestamp == null)
            throw new IllegalArgumentException();
        fValue = timestamp.getValue();
        fScale = timestamp.getScale();
        fPrecision = timestamp.getPrecision();
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#getValue()
     */
    @Override
    public long getValue() {
        return fValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#getScale()
     */
    @Override
    public int getScale() {
        return fScale;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#getPrecision()
     */
    @Override
    public int getPrecision() {
        return fPrecision;
    }

    private static final long scalingFactors[] = new long[] {
        1L,
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L,
    };

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#normalize(long, int)
     */
    @Override
    public ITmfTimestamp normalize(long offset, int scale) throws ArithmeticException {

        long value = fValue;
        int precision = fPrecision;

        // Handle the trivial case
        if (fScale == scale && offset == 0)
            return new TmfTimestamp(this);

        // First, scale the timestamp
        if (fScale != scale) {
            int scaleDiff = Math.abs(fScale - scale);
            if (scaleDiff >= scalingFactors.length) {
                throw new ArithmeticException("Scaling exception"); //$NON-NLS-1$
            }

            long scalingFactor = scalingFactors[scaleDiff];
            if (scale < fScale) {
                value *= scalingFactor;
                precision *= scalingFactor;
            } else {
                value /= scalingFactor;
                precision /= scalingFactor;
            }
        }

        // Then, apply the offset
        if (offset < 0) {
            value = (value < Long.MIN_VALUE - offset) ? Long.MIN_VALUE : value + offset;
        } else {
            value = (value > Long.MAX_VALUE - offset) ? Long.MAX_VALUE : value + offset;
        }

        return new TmfTimestamp(value, scale, precision);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#compareTo(org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp, boolean)
     */
    @Override
    public int compareTo(ITmfTimestamp ts, boolean withinPrecision) {

        // Check the corner cases (we can't use equals() because it uses compareTo()...)
        if (this == ts || (fValue == ts.getValue() && fScale == ts.getScale()))
            return 0;
        if ((fValue == BIG_BANG.getValue() && fScale == BIG_BANG.getScale()) || (ts.getValue() == BIG_CRUNCH.getValue() && ts.getScale() == BIG_CRUNCH.getScale()))
            return -1;
        if ((fValue == BIG_CRUNCH.getValue() && fScale == BIG_CRUNCH.getScale()) || (ts.getValue() == BIG_BANG.getValue() && ts.getScale() == BIG_BANG.getScale()))
            return 1;
        
        try {
            ITmfTimestamp nts = ts.normalize(0, fScale);
            long delta = fValue - nts.getValue();
            if ((delta == 0) || (withinPrecision && (Math.abs(delta) <= (fPrecision + nts.getPrecision())))) {
                return 0;
            }
            return (delta > 0) ? 1 : -1;
        }
        catch (ArithmeticException e) {
            // Scaling error. We can figure it out nonetheless.

            // First, look at the sign of the mantissa
            long value = ts.getValue();
            if (fValue == 0 && value == 0)
                return 0;
            if (fValue  < 0 && value >= 0)
                return -1;
            if (fValue >= 0 && value < 0)
                return 1;

            // Otherwise, just compare the scales
            int scale = ts.getScale();
            return (fScale > scale) ? (fValue >= 0) ? 1 : -1 : (fValue >= 0) ? -1 : 1;  
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#getDelta(org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp)
     */
    @Override
    public ITmfTimestamp getDelta(ITmfTimestamp ts) {
        ITmfTimestamp nts = ts.normalize(0, fScale);
        long value = fValue - nts.getValue();
        return new TmfTimestamp(value, fScale, fPrecision + nts.getPrecision());
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfTimestamp clone() {
        TmfTimestamp clone = null;
        try {
            clone = (TmfTimestamp) super.clone();
            clone.fValue = fValue;
            clone.fScale = fScale;
            clone.fPrecision = fPrecision;
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp#compareTo(org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp)
     */
    @Override
    public int compareTo(ITmfTimestamp ts) {
        return compareTo(ts, false);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fValue ^ (fValue >>> 32));
        result = prime * result + fScale;
        result = prime * result + fPrecision;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof TmfTimestamp))
            return false;
        TmfTimestamp ts = (TmfTimestamp) other;
        return compareTo(ts, false) == 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfTimestamp [fValue=" + fValue + ", fScale=" + fScale + ", fPrecision=" + fPrecision + "]";
    }

}
