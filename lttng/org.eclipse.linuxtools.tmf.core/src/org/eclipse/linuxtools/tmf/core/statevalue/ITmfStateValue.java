/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statevalue;


/**
 * This is the interface for using state values and reading their contents.
 * 
 * @author alexmont
 * 
 */
public interface ITmfStateValue {

    /**
     * Each implementation has to supply a "type" number. This will get written
     * as-is in the History file to recognize the type, so it needs to be unique
     * 
     * @return The unique "int8" assigned to this state value type
     */
    public byte getType();

    /**
     * Only "null values" should return true here
     * 
     * @return True if this type of SV is considered "null", false if it
     *         contains a real value.
     */
    public boolean isNull();

    /**
     * Read the contained value as an 'int' primitive
     * 
     * @return The integer contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as an integer
     */
    public int unboxInt() throws StateValueTypeException;

    /**
     * Read the contained value as a String
     * 
     * @return The String contained in the state value
     * @throws StateValueTypeException
     *             If the contained value cannot be read as a String
     */
    public String unboxStr() throws StateValueTypeException;
}
