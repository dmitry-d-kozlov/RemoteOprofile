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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>ChangeChannelStateHandler</u></b>
 * <p>
 * Abstract command handler implementation to enable or disabling a trace channel.
 * </p>
 */
abstract public class ChangeChannelStateHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The command execution parameter.
     */
    protected Parameter fParam;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the new state to set
     */
    abstract protected TraceEnablement getNewState(); 

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Changes the state of the given channels.
     * @param domain - the domain of the channels.
     * @param channelNames - a list of channel names 
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    abstract protected void changeState(TraceDomainComponent domain, List<String> channelNames, IProgressMonitor monitor) throws ExecutionException; 

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        fLock.lock();
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

            if (window == null) {
                return false;
            }

            final Parameter param = new Parameter(fParam);

            Job job = new Job(Messages.TraceControl_ChangeChannelStateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    String errorString = null;

                    TraceSessionComponent session = null;

                    try {
                        TraceDomainComponent kernelDomain = param.getKernelDomain();
                        List<TraceChannelComponent> kernelChannels = param.getKernelChannels();

                        if (kernelDomain != null) {
                            session = (TraceSessionComponent)kernelDomain.getParent();
                            List<String> channelNames = new ArrayList<String>();
                            for (Iterator<TraceChannelComponent> iterator = kernelChannels.iterator(); iterator.hasNext();) {
                                // Enable all selected channels which are disabled
                                TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                                channelNames.add(channel.getName());
                            }

                            changeState(kernelDomain, channelNames, monitor);

                            for (Iterator<TraceChannelComponent> iterator = kernelChannels.iterator(); iterator.hasNext();) {
                                // Enable all selected channels which are disabled
                                TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                                channel.setState(getNewState());
                            }
                        }

                        TraceDomainComponent ustDomain = param.getUstDomain();
                        List<TraceChannelComponent> ustChannels = param.getUstChannels();
                        if (ustDomain != null) {
                            if (session == null) {
                                session = (TraceSessionComponent)ustDomain.getParent();
                            }

                            List<String> channelNames = new ArrayList<String>();
                            for (Iterator<TraceChannelComponent> iterator = ustChannels.iterator(); iterator.hasNext();) {
                                // Enable all selected channels which are disabled
                                TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                                channelNames.add(channel.getName());
                            }

                            changeState(ustDomain, channelNames, monitor);

                            for (Iterator<TraceChannelComponent> iterator = ustChannels.iterator(); iterator.hasNext();) {
                                // Enable all selected channels which are disabled
                                TraceChannelComponent channel = (TraceChannelComponent) iterator.next();
                                channel.setState(getNewState());
                            }
                        }
                    } catch (ExecutionException e) {
                        errorString = e.toString() + "\n"; //$NON-NLS-1$
                    }

                    // In all cases notify listeners  
                    session.fireComponentChanged(session);

                    if (errorString != null) {
                        return new Status(Status.ERROR, Activator.PLUGIN_ID, errorString);
                    }

                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TraceDomainComponent kernelDomain = null;
        TraceDomainComponent ustDomain = null;
        List<TraceChannelComponent> kernelChannels = new ArrayList<TraceChannelComponent>();
        List<TraceChannelComponent> ustChannels = new ArrayList<TraceChannelComponent>();
        
        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            String sessionName = null;
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = (Object) iterator.next();
                 
                if (element instanceof TraceChannelComponent) {
                    
                    // Add only TraceChannelComponents that are disabled
                    TraceChannelComponent channel = (TraceChannelComponent) element;
                    if (sessionName == null) {
                        sessionName = String.valueOf(channel.getSessionName());
                    }

                    // Enable command only for channels of same session
                    if (!sessionName.equals(channel.getSessionName())) {
                        kernelChannels.clear();
                        ustChannels.clear();
                        break;
                    }

                    if ((channel.getState() != getNewState())) {
                        if (channel.isKernel()) {
                            kernelChannels.add(channel);
                            if (kernelDomain == null) {
                                kernelDomain = (TraceDomainComponent) channel.getParent();
                            }
                        } else {
                            ustChannels.add(channel);
                            if (ustDomain == null) {
                                ustDomain = (TraceDomainComponent) channel.getParent();
                            }
                        }
                    }
                }
            }
        }
        
        boolean isEnabled = (!kernelChannels.isEmpty() || !ustChannels.isEmpty());
        fLock.lock();
        try {
            if (isEnabled) {
                fParam = new Parameter(kernelDomain, ustDomain, kernelChannels, ustChannels);
            }
        } finally {
            fLock.unlock();
        }
        
        return isEnabled;
    }
    
    /**
     *  Class containing parameter for the command execution. 
     */
    static protected class Parameter {
        /**
         * Kernel domain component reference.
         */
        final protected TraceDomainComponent fKernelDomain;
        /**
         * UST domain component reference.
         */
        final protected TraceDomainComponent fUstDomain;
        /**
         * The list of kernel channel components the command is to be executed on. 
         */
        final protected List<TraceChannelComponent> fKernelChannels;
        /**
         * The list of UST channel components the command is to be executed on. 
         */
        final protected List<TraceChannelComponent> fUstChannels;
        
        /**
         * Constructor
         * @param kernelDomain - a kernel domain component
         * @param ustDomain - a UST domain component
         * @param kernelChannels - list of available kernel channels
         * @param ustChannels - list of available UST channels
         */
        public Parameter(TraceDomainComponent kernelDomain, TraceDomainComponent ustDomain, List<TraceChannelComponent> kernelChannels, List<TraceChannelComponent> ustChannels) {
            fKernelDomain = kernelDomain;
            fUstDomain = ustDomain;
            fKernelChannels = new ArrayList<TraceChannelComponent>();
            fKernelChannels.addAll(kernelChannels);
            fUstChannels = new ArrayList<TraceChannelComponent>();
            fUstChannels.addAll(ustChannels);
        }
        
        /**
         * Copy constructor
         * @param other a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fKernelDomain, other.fUstDomain, other.fKernelChannels, other.fUstChannels);
        }
        
        /**
         * @return the kernel domain component.
         */
        public TraceDomainComponent getKernelDomain() {
            return fKernelDomain;
        }
        
        /**
         * @return the UST domain component.
         */
        public TraceDomainComponent getUstDomain() {
            return fUstDomain;
        }   
        
        /**
         * @return the list of kernel channel components.
         */
        public List<TraceChannelComponent> getKernelChannels() {
            return fKernelChannels;
        }
        
        /**
         * @return the list of UST channel components.
         */
        public List<TraceChannelComponent> getUstChannels() {
            return fUstChannels;
        }
    }
}
