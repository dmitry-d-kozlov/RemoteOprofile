/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SortSyncMessageComparator.java,v 1.2 2006/09/20 20:56:27 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.util;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;

/**
 * Synchronous message comparator Compare two syncMessages only taking into account the event occurrence when their
 * appear.<br>
 * 
 * The message with the greater event occurrence is considered to be the greater.<br>
 * 
 * @author sveyrier
 * 
 */
public class SortSyncMessageComparator implements Comparator<GraphNode>, Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4781250984753283718L;

    /**
     * Compares two synchronous syncMessages. Returns 0 (equal) if one of the message is not synchronous
     * 
     * @return 1 if arg0 is greater, 0 if equal, -1 otherwise
     */
    @Override
    public int compare(GraphNode arg0, GraphNode arg1) {
        if (arg0 instanceof SyncMessage && arg1 instanceof SyncMessage) {
            SyncMessage m1 = (SyncMessage) arg0;
            SyncMessage m2 = (SyncMessage) arg1;
            if (m1.getEventOccurrence() > m2.getEventOccurrence())
                return 1;
            else if (m1.getEventOccurrence() == m2.getEventOccurrence())
                return 0;
            else
                return -1;
        } else
            return 0;
    }

}
