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

package org.eclipse.linuxtools.tmf.core.statevalue;

/**
 * A state value containing a variable-sized string
 * 
 * @author alexmont
 *
 */
final class StringStateValue extends TmfStateValue {

    private final String valueStr;

    public StringStateValue(String valueAsString) {
        assert (valueAsString != null);
        this.valueStr = valueAsString;
    }

    @Override
    public byte getType() {
        return 1;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String getValue() {
        return valueStr;
    }

    @Override
    public byte[] toByteArray() {
        return valueStr.getBytes();
    }

    @Override
    public String toString() {
        return valueStr;
    }
}
