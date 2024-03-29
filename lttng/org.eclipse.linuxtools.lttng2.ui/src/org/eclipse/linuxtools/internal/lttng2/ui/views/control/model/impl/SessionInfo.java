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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceSessionState;

/**
 * <b><u>SessionInfo</u></b>
 * <p>
 * Implementation of the trace session interface (ISessionInfo) to store session
 * related data. 
 * </p>
 */
public class SessionInfo extends TraceInfo implements ISessionInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The trace session state.
     */
    private TraceSessionState fState = TraceSessionState.INACTIVE;
    /** 
     * The trace session path for storing traces.
     */
    private String fSessionPath = ""; //$NON-NLS-1$
    /**
     * The domains information of this session.
     */
    private List<IDomainInfo> fDomains = new ArrayList<IDomainInfo>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of base event
     */
    public SessionInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public SessionInfo(SessionInfo other) {
        super(other);
        fState = other.fState;
        fSessionPath = other.fSessionPath;
        
        for (Iterator<IDomainInfo> iterator = other.fDomains.iterator(); iterator.hasNext();) {
            IDomainInfo domain = iterator.next();
            if (domain instanceof DomainInfo) {
                fDomains.add(new DomainInfo((DomainInfo)domain));
            } else {
                fDomains.add(domain);
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#getSessionState()
     */
    @Override
    public TraceSessionState getSessionState() {
        return fState;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#setSessionState(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceSessionState)
     */
    @Override
    public void setSessionState(TraceSessionState state) {
        fState = state;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#setSessionState(java.lang.String)
     */
    @Override
    public void setSessionState(String stateName) {
        if (TraceSessionState.INACTIVE.getInName().equals(stateName)) {
            fState = TraceSessionState.INACTIVE;
        } else if (TraceSessionState.ACTIVE.getInName().equals(stateName)) {
            fState = TraceSessionState.ACTIVE;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#getSessionPath()
     */
    @Override
    public String getSessionPath() {
        return fSessionPath;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#setSessionPath(java.lang.String)
     */
    @Override
    public void setSessionPath(String path) {
        fSessionPath = path;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#getDomains()
     */
    @Override
    public IDomainInfo[] getDomains() {
        return fDomains.toArray(new IDomainInfo[fDomains.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#setDomains(java.util.List)
     */
    @Override
    public void setDomains(List<IDomainInfo> domains) {
        for (Iterator<IDomainInfo> iterator = domains.iterator(); iterator.hasNext();) {
            IDomainInfo domainInfo = (IDomainInfo) iterator.next();
            fDomains.add(domainInfo);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo#addDomain(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IDomainInfo)
     */
    @Override
    public void addDomain(IDomainInfo domainInfo) {
        fDomains.add(domainInfo);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceInfo#formatString()
     */
    @Override
    @SuppressWarnings("nls")
    public String formatString() {
        StringBuffer output = new StringBuffer();
        // Tracing session mysession: [active]
        output.append("Tracing session ");
        output.append(getName());
        output.append(": [");
        output.append(getSessionState().getInName());
        output.append("]\n");
        
        //    Trace path: /home/user/lttng-traces/mysession-20120129-084256
        output.append("    Trace path: ");
        output.append(getSessionPath());
        output.append('\n');
        
        for (Iterator<IDomainInfo> iterator = fDomains.iterator(); iterator.hasNext();) {
            IDomainInfo domain = (IDomainInfo) iterator.next();
            output.append(domain.formatString());
        }
        return output.toString();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fDomains == null) ? 0 : fDomains.hashCode());
        result = prime * result + ((fSessionPath == null) ? 0 : fSessionPath.hashCode());
        result = prime * result + ((fState == null) ? 0 : (fState.ordinal() + 1));
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SessionInfo other = (SessionInfo) obj;
        if (fDomains == null) {
            if (other.fDomains != null) {
                return false;
            }
        } else if (!fDomains.equals(other.fDomains)) {
            return false;
        }
        if (fSessionPath == null) {
            if (other.fSessionPath != null) {
                return false;
            }
        } else if (!fSessionPath.equals(other.fSessionPath)) {
            return false;
        }
        if (fState != other.fState) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[SessionInfo(");
            output.append(super.toString());
            output.append(",State=");
            output.append(fState);
            output.append(",Domains=");
            for (Iterator<IDomainInfo> iterator = fDomains.iterator(); iterator.hasNext();) {
                IDomainInfo domain = (IDomainInfo) iterator.next();
                output.append(domain.toString());
            }
            output.append(")]");
            return output.toString();
    }
}
