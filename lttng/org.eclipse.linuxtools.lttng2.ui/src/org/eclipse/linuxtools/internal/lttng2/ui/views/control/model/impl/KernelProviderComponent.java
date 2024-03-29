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

import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.KernelProviderPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <b><u>KernelProviderComponent</u></b>
 * <p>
 * TODO
 * </p>
 */
public class KernelProviderComponent extends TraceControlComponent {
    
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String KERNEL_PROVIDER_ICON_FILE = "icons/obj16/targets.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public KernelProviderComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(KERNEL_PROVIDER_ICON_FILE);
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the events information for this component. 
     * @param eventInfos - events information to set. 
     */
    public void setEventInfo(List<IBaseEventInfo> eventInfos) {
        for (Iterator<IBaseEventInfo> iterator = eventInfos.iterator(); iterator.hasNext();) {
            IBaseEventInfo baseEventInfo = (IBaseEventInfo) iterator.next();
            BaseEventComponent component = new BaseEventComponent(baseEventInfo.getName(), this);
            component.setEventInfo(baseEventInfo);
            addChild(component);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlComponent#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new KernelProviderPropertySource(this);
        }
        return null;
    } 

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
}
