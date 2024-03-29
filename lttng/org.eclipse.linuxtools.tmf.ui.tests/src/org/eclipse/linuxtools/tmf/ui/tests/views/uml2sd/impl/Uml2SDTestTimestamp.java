/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.impl;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

public class Uml2SDTestTimestamp extends TmfTimestamp {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public Uml2SDTestTimestamp(long value) {
        super(value, IUml2SDTestConstants.TIME_SCALE);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

}
