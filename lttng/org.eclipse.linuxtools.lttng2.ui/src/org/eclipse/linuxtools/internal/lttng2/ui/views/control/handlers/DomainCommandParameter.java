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
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 *  Class containing parameter for the command execution. 
 */
public class DomainCommandParameter extends CommandParameter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TraceDomainComponent fDomain;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param session - a trace session component.
     * @param domain - a trace domain component
     */
    public DomainCommandParameter(TraceSessionComponent session, TraceDomainComponent domain) {
        super(session);
        fDomain = domain;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the trace domain component
     */
    public TraceDomainComponent getDomain() {
        return fDomain;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public DomainCommandParameter clone() {
        DomainCommandParameter clone = (DomainCommandParameter) super.clone();
        clone.fDomain = fDomain;
        return clone;
    }
}