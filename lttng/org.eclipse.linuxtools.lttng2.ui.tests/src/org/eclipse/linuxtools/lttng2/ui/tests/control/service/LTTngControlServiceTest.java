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
package org.eclipse.linuxtools.lttng2.ui.tests.control.service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng2.stubs.service.CommandShellFactory;
import org.eclipse.linuxtools.internal.lttng2.stubs.shells.LTTngToolsFileShell;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ILttngControlService;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.LTTngControlService;
import org.eclipse.linuxtools.lttng2.ui.tests.Activator;
/**
 * The class <code>LTTngControlServiceTest</code> contains test for the class <code>{@link  LTTngControlService}</code>.
 */
@SuppressWarnings("nls")
public class LTTngControlServiceTest extends TestCase {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "LTTngServiceTest.cfg";
    
    private static final String SCEN_LTTNG_NOT_INSTALLED = "LttngNotInstalled";
    private static final String SCEN_NO_SESSION_AVAILABLE = "NoSessionAvailable";
    private static final String SCEN_GET_SESSION_NAMES1 = "GetSessionNames1";
    private static final String SCEN_GET_SESSION_NAME_NOT_EXIST = "GetSessionNameNotExist";
    private static final String SCEN_GET_SESSION_GARBAGE_OUT = "GetSessionGarbageOut";
    private static final String SCEN_GET_SESSION1 = "GetSession1";
    private static final String SCEN_GET_KERNEL_PROVIDER1 = "GetKernelProvider1";
    private static final String SCEN_GET_UST_PROVIDER1 = "GetUstProvider1";
    private static final String SCEN_GET_UST_PROVIDER2 = "GetUstProvider2";
    private static final String SCEN_CREATE_SESSION1 = "CreateSession1";
    private static final String SCEN_CREATE_SESSION_VARIANTS = "CreateSessionVariants";
    private static final String SCEN_DESTROY_SESSION1 = "DestroySession1";
    private static final String SCEN_CHANNEL_HANDLING = "ChannelHandling";
    private static final String SCEN_EVENT_HANDLING = "EventHandling";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private CommandShellFactory fShellFactory;
    private String fTestfile;
    private LTTngToolsFileShell fShell;
    private ILttngControlService fService;

    
   // ------------------------------------------------------------------------
    // Static methods
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    /**
     * Perform pre-test initialization.
     *
     * @throws Exception if the initialization fails for some reason
     *
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        fShellFactory = CommandShellFactory.getInstance();

        URL location = FileLocator.find(Activator.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestfile = testfile.getAbsolutePath();
        
        fShell = fShellFactory.getFileShell();
        fShell.loadScenarioFile(fTestfile);
        fService = new LTTngControlService(fShell);
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception if the clean-up fails for some reason
     *
     */
    @Override
    public void tearDown() throws Exception {
    }

    // ------------------------------------------------------------------------
    // Test Cases
    // ------------------------------------------------------------------------
    
    public void testGetSessionNames() {
        try {
            fShell.setScenario(SCEN_LTTNG_NOT_INSTALLED);
            fService.getSessionNames(new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
         // success
        }
    }
    
    public void testGetSessionNames1() {
        try {
            fShell.setScenario(SCEN_NO_SESSION_AVAILABLE);
            String[] result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(0, result.length);
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testGetSessionNames2() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_NAMES1);
            String[] result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals("mysession1", result[0]);
            assertEquals("mysession", result[1]);
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testGetSessionNotExist() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_NAME_NOT_EXIST);
            fService.getSessionNames(new NullProgressMonitor());
            fail("No exeption thrown");
            
        } catch (ExecutionException e) {
            // success
        }
    }
    
    public void testGetSessionNameGarbage() {
        try {
            fShell.setScenario(SCEN_GET_SESSION_GARBAGE_OUT);
            String[] result = fService.getSessionNames(new NullProgressMonitor());

            assertNotNull(result);
            assertEquals(0, result.length);
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testGetSession1() {
        try {
            fShell.setScenario(SCEN_GET_SESSION1);
            ISessionInfo session = fService.getSession("mysession", new NullProgressMonitor());

            // Verify Session
            assertNotNull(session);
            assertEquals("mysession", session.getName());
            assertEquals("/home/user/lttng-traces/mysession-20120129-084256", session.getSessionPath());
            assertEquals(TraceSessionState.ACTIVE, session.getSessionState());
            
            IDomainInfo[] domains = session.getDomains();
            assertNotNull(domains);
            assertEquals(2, domains.length);
            
            // Verify Kernel domain
            assertEquals("Kernel", domains[0].getName());
            IChannelInfo[] channels =  domains[0].getChannels();
            assertNotNull(channels);
            assertEquals(2, channels.length);

            // Verify Kernel's channel0 
            assertEquals("channel0", channels[0].getName());
            assertEquals(4, channels[0].getNumberOfSubBuffers());
            assertEquals("splice()", channels[0].getOutputType());
            assertEquals(false, channels[0].isOverwriteMode());
            assertEquals(200, channels[0].getReadTimer());
            assertEquals(TraceEnablement.ENABLED, channels[0].getState());
            assertEquals(262144, channels[0].getSubBufferSize());
            assertEquals(0, channels[0].getSwitchTimer());
            
            // Verify event info
            IEventInfo[] channel0Events = channels[0].getEvents();
            assertNotNull(channel0Events);
            assertEquals(2, channel0Events.length);
            assertEquals("block_rq_remap", channel0Events[0].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[0].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, channel0Events[0].getEventType());
            assertEquals(TraceEnablement.ENABLED, channel0Events[0].getState());
            
            assertEquals("block_bio_remap", channel0Events[1].getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, channel0Events[1].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, channel0Events[1].getEventType());
            assertEquals(TraceEnablement.DISABLED, channel0Events[1].getState());
            
            // Verify Kernel's channel1 
            assertEquals("channel1", channels[1].getName());
            assertEquals(4, channels[1].getNumberOfSubBuffers());
            assertEquals("splice()", channels[1].getOutputType());
            assertEquals(true, channels[1].isOverwriteMode());
            assertEquals(400, channels[1].getReadTimer());
            assertEquals(TraceEnablement.DISABLED, channels[1].getState());
            assertEquals(524288, channels[1].getSubBufferSize());
            assertEquals(100, channels[1].getSwitchTimer());
            
            // Verify event info
            IEventInfo[] channel1Events = channels[1].getEvents();
            assertEquals(0, channel1Events.length);
            
            // Verify domain UST global
            assertEquals("UST global", domains[1].getName());
            
            IChannelInfo[] ustChannels =  domains[1].getChannels();
            
            // Verify UST global's mychannel1 
            assertEquals("mychannel1", ustChannels[0].getName());
            assertEquals(8, ustChannels[0].getNumberOfSubBuffers());
            assertEquals("mmap()", ustChannels[0].getOutputType());
            assertEquals(true, ustChannels[0].isOverwriteMode());
            assertEquals(100, ustChannels[0].getReadTimer());
            assertEquals(TraceEnablement.DISABLED, ustChannels[0].getState());
            assertEquals(8192, ustChannels[0].getSubBufferSize());
            assertEquals(200, ustChannels[0].getSwitchTimer());
            
            // Verify event info
            IEventInfo[] ustEvents = ustChannels[0].getEvents();
            assertEquals(0, ustEvents.length);

            // Verify UST global's channel0 
            assertEquals("channel0", ustChannels[1].getName());
            assertEquals(4, ustChannels[1].getNumberOfSubBuffers());
            assertEquals("mmap()", ustChannels[1].getOutputType());
            assertEquals(false, ustChannels[1].isOverwriteMode());
            assertEquals(200, ustChannels[1].getReadTimer());
            assertEquals(TraceEnablement.ENABLED, ustChannels[1].getState());
            assertEquals(4096, ustChannels[1].getSubBufferSize());
            assertEquals(0, ustChannels[1].getSwitchTimer());
            
            // Verify event info
            ustEvents = ustChannels[1].getEvents();
            assertEquals(2, ustEvents.length);
            
            assertEquals("ust_tests_hello:tptest_sighandler", ustEvents[0].getName());
            assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, ustEvents[0].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, ustEvents[0].getEventType());
            assertEquals(TraceEnablement.DISABLED, ustEvents[0].getState());
            
            assertEquals("*", ustEvents[1].getName());
            assertEquals(TraceLogLevel.LEVEL_UNKNOWN, ustEvents[1].getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, ustEvents[1].getEventType());
            assertEquals(TraceEnablement.ENABLED, ustEvents[1].getState());
            
            // next session (no detailed information available)
            session = fService.getSession("mysession1", new NullProgressMonitor());
            assertNotNull(session);
            assertEquals("mysession1", session.getName());
            assertEquals("/home/user/lttng-traces/mysession1-20120203-133225", session.getSessionPath());
            assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
            
            domains = session.getDomains();
            assertNotNull(domains);
            assertEquals(0, domains.length);
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testGetKernelProvider() {
        try {
            fShell.setScenario(SCEN_GET_KERNEL_PROVIDER1);
            List<IBaseEventInfo> events = fService.getKernelProvider(new NullProgressMonitor());

            // Verify event info
            assertNotNull(events);
            assertEquals(3, events.size());
            
            IBaseEventInfo baseEventInfo = (IBaseEventInfo) events.get(0);
            assertNotNull(baseEventInfo);
            assertEquals("sched_kthread_stop", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events.get(1);
            assertEquals("sched_kthread_stop_ret", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events.get(2);
            assertEquals("sched_wakeup_new", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testGetUstProvider() {
        try {
            fShell.setScenario(SCEN_GET_UST_PROVIDER1);
            List<IUstProviderInfo> providers = fService.getUstProvider();

            // Check all providers
            assertNotNull(providers);
            assertEquals(2, providers.size());
            
            //Verify first provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(0).getName());
            assertEquals(9379, providers.get(0).getPid());
            
            // Verify event info
            IBaseEventInfo[] events = providers.get(0).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            IBaseEventInfo baseEventInfo = (IBaseEventInfo) events[0];
            assertNotNull(baseEventInfo);
            assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_DEBUG_MODULE, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events[1];
            assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_INFO, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

            //Verify second provider
            assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello", providers.get(1).getName());
            assertEquals(4852, providers.get(1).getPid());
            
            // Verify event info
            events = providers.get(1).getEvents();
            assertNotNull(events);
            assertEquals(2, events.length);

            baseEventInfo = (IBaseEventInfo) events[0];
            assertNotNull(baseEventInfo);
            assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_WARNING, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());
            
            baseEventInfo = (IBaseEventInfo) events[1];
            assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
            assertEquals(TraceLogLevel.TRACE_DEBUG_FUNCTION, baseEventInfo.getLogLevel());
            assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testUstProvider2() {
        try {
            fShell.setScenario(SCEN_GET_UST_PROVIDER2);
            List<IUstProviderInfo> providers = fService.getUstProvider();

            assertNotNull(providers);
            assertEquals(0, providers.size());
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testCreateSession() {
        try {
            fShell.setScenario(SCEN_CREATE_SESSION1);

            ISessionInfo info = fService.createSession("mysession2", null, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("mysession2", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("mysession2"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testCreateSessionVariants() {

        fShell.setScenario(SCEN_CREATE_SESSION_VARIANTS);

        try {
            fService.createSession("alreadyExist", null, new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        }
        
        try {
            fService.createSession("wrongName", null, new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        } 

        try {
            fService.createSession("withPath", "/home/user/hallo", new NullProgressMonitor());
            fail("No exeption thrown");
        } catch (ExecutionException e) {
            // success
        } 

        try {
            ISessionInfo info = fService.createSession("session with spaces", null, new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("session with spaces", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("session with spaces"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());
            
        } catch (ExecutionException e) {
            fail(e.toString());
        } 

        try {
            ISessionInfo info = fService.createSession("pathWithSpaces", "/home/user/hallo user/here", new NullProgressMonitor());
            assertNotNull(info);
            assertEquals("pathWithSpaces", info.getName());
            assertNotNull(info.getSessionPath());
            assertTrue(info.getSessionPath().contains("/home/user/hallo user/here"));
            assertEquals(TraceSessionState.INACTIVE, info.getSessionState());
            
        } catch (ExecutionException e) {
            fail(e.toString());
        } 
    }

    public void testDestroySession() {
        try {
            fShell.setScenario(SCEN_DESTROY_SESSION1);
            fService.destroySession("mysession2", new NullProgressMonitor());
        } catch (ExecutionException e) {
            fail(e.toString());
        } 
    }

    public void testCreateChannel() {
        try {

            String sessionName = "mysession2";
            List<String> list = new ArrayList<String>();
            String kernelChannel0 = "mychannel0";
            String kernelChannel1 = "mychannel1";
            list.add(kernelChannel0);
            list.add(kernelChannel1);
            
            fShell.setScenario(SCEN_CHANNEL_HANDLING);
            
            // Create/enable/configure 2 kernel channels
            ChannelInfo chanInfo = new ChannelInfo("");
            chanInfo.setOverwriteMode(true);
            chanInfo.setSubBufferSize(16384);
            chanInfo.setReadTimer(100);
            chanInfo.setSwitchTimer(200);
            chanInfo.setNumberOfSubBuffers(2);
            fService.enableChannels(sessionName, list, true, chanInfo, new NullProgressMonitor());
            
            // Create/enable/configure 1 UST channel
            list.clear();
            list.add("ustChannel");
            
            chanInfo = new ChannelInfo("");
            chanInfo.setOverwriteMode(true);
            chanInfo.setSubBufferSize(32768);
            chanInfo.setReadTimer(200);
            chanInfo.setSwitchTimer(100);
            chanInfo.setNumberOfSubBuffers(1);
            fService.enableChannels(sessionName, list, false, chanInfo, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testDisableChannel() {
        try {

            String sessionName = "mysession2";
            List<String> list = new ArrayList<String>();
            String kernelChannel0 = "mychannel0";
            String kernelChannel1 = "mychannel1";
            list.add(kernelChannel0);
            list.add(kernelChannel1);
            
            fShell.setScenario(SCEN_CHANNEL_HANDLING);
            fService.disableChannels(sessionName, list, true, new NullProgressMonitor());
            
            list.clear();
            list.add("ustChannel");
            fService.disableChannels(sessionName, list, false, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testEnableChannel() {
        try {

            String sessionName = "mysession2";
            List<String> list = new ArrayList<String>();
            String kernelChannel0 = "mychannel0";
            String kernelChannel1 = "mychannel1";
            list.add(kernelChannel0);
            list.add(kernelChannel1);
            
            fShell.setScenario(SCEN_CHANNEL_HANDLING);
            fService.enableChannels(sessionName, list, true, null, new NullProgressMonitor());
            
            // Create/enable/configure 1 UST channel
            list.clear();
            list.add("ustChannel");
            
            fService.enableChannels(sessionName, list, false, null, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
//    public void tesEnableChannelNoTracer() {
//        try {
//            ILttngControlService service = new LTTngControlService(fShellFactory.getShellForChannelNoTracer());
//            service.getSessionNames(new NullProgressMonitor());
//            fail("No exeption thrown");
//            
//        } catch (ExecutionException e) {
//            // success
//        }
//    }    
    
    public void testEnableEvents() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            List<String> list = new ArrayList<String>();
            String eventName0 = "block_rq_remap";
            String eventName1 = "block_bio_remap";
            String eventName2 = "softirq_entry";
            list.add(eventName0);
            list.add(eventName1);
            list.add(eventName2);
            fShell.setScenario(SCEN_EVENT_HANDLING);
            fService.enableEvents(sessionName, null, list, true, new NullProgressMonitor());
            
            // 2) session name, channel=mychannel, event name= null, kernel
            String channelName = "mychannel";
            fService.enableEvents(sessionName, channelName, null, true, new NullProgressMonitor());

            // 3) session name, channel=mychannel, 1 event name, ust
            String ustEventName = "ust_tests_hello:tptest_sighandler";
            list.clear();
            list.add(ustEventName);
            fService.enableEvents(sessionName, channelName, list, false, new NullProgressMonitor());
            
            // 4) session name, channel = mychannel, no event name, ust
            list.clear();
            fService.enableEvents(sessionName, channelName, list, false, new NullProgressMonitor());
            
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
    public void testEnableSyscalls() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            
            fShell.setScenario(SCEN_EVENT_HANDLING);

            // 1) session name, channel = null 
            fService.enableSyscalls(sessionName, null, new NullProgressMonitor());

            // 2) session name, channel = mychannel
            fService.enableSyscalls(sessionName, channelName, new NullProgressMonitor());
                        
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testDynamicProbe() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            String eventName0 = "myevent0";
            String eventName1 = "myevent1";
            String functionProbe = "0xc0101340";
            String dynProbe = "init_post";
            
            fShell.setScenario(SCEN_EVENT_HANDLING);

            // 1) session name, channel = null, event name, function probe, probe 
            fService.enableProbe(sessionName, null, eventName0, true, functionProbe, new NullProgressMonitor());

            // 2) session name, channel = mychannel
            fService.enableProbe(sessionName, channelName, eventName1, false, dynProbe, new NullProgressMonitor());
                        
        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }

    public void testEnableLogLevel() {
        try {
            // 1) session name, channel = null, 3 event names, kernel
            String sessionName = "mysession2";
            String channelName = "mychannel";
            String eventName4 = "myevent4";
            String eventName5 = "myevent5";
            
            fShell.setScenario(SCEN_EVENT_HANDLING);
            
            // 1) session name, channel = null, event name, loglevel-only, TRACE_DEBUG
            fService.enableLogLevel(sessionName, null, eventName4, LogLevelType.LOGLEVEL_ONLY, TraceLogLevel.TRACE_DEBUG, new NullProgressMonitor());

            // 2) session name, channel = mychannel, null, loglevel, TRACE_DEBUG_FUNCTION
            fService.enableLogLevel(sessionName, channelName, eventName5, LogLevelType.LOGLEVEL, TraceLogLevel.TRACE_DEBUG_FUNCTION, new NullProgressMonitor());

        } catch (ExecutionException e) {
            fail(e.toString());
        }
    }
    
}
