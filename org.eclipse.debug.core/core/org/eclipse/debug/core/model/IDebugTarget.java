package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpointListener;

/**
 * A debug target is a debuggable execution context. For example, a debug target
 * may represent a debuggable process or a virtual machine. A debug target is the root
 * of the debug element hierarchy. A debug target contains threads. Minimally, a debug
 * target supports the following:
 * <ul>
 * <li>terminate
 * <li>suspend/resume
 * <li>breakpoints
 * <li>disconnect
 * </ul>
 * <p>
 * Generally, launching a debug session results in the creation of a
 * debug target. Launching is a client responsibility, as is debug target
 * creation.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see ITerminate
 * @see ISuspendResume
 * @see IBreakpointListener
 * @see IDisconnect
 * @see IMemoryBlockRetrieval
 * @see org.eclipse.debug.core.ILaunch
 */
public interface IDebugTarget extends IDebugElement, ITerminate, ISuspendResume, IBreakpointListener, IDisconnect, IMemoryBlockRetrieval {
	/**
	 * Returns the system process associated with this debug target.
	 * 
	 * @return the system process associated with this debug target
	 */
	public IProcess getProcess();
	/**
	 * Returns the threads contained in this debug target. An
	 * empty collection is returned if this debug target contains
	 * no threads.
	 * 
	 * @return a collection of threads
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * @since 2.0
	 */
	public IThread[] getThreads() throws DebugException;
	
	/**
	 * Returns whether this debug target currently contains any threads.
	 * 
	 * @return whether this debug target currently contains any threads
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * @since 2.0
	 */
	public boolean hasThreads() throws DebugException;
	
	/**
	 * Returns the name of this debug target. Name format is debug model
	 * specific, and should be specified by a debug model.
	 *
	 * @return this target's name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public String getName() throws DebugException;
	
	/**
	 * Returns whether the given breakpoint is supported by this
	 * target - i.e. whether the given breakpoint could be installed
	 * in this target. 
	 * 
	 * @return whether the given breakpoint is supported by this
	 * target
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint);
}


