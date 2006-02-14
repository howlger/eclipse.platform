/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends AbstractDebugContextActionDelegate {
	

	protected void doAction(Object element) throws DebugException {
		if (element instanceof ILaunch) {
			ILaunch launch = (ILaunch) element;
			if (!launch.isTerminated() && DebugPlugin.getDefault().getLaunchManager().isRegistered(launch)) {
				launch.terminate();
			}			
		}
	}
	
	/**
	 * Update the action enablement based on the launches present in
	 * the launch manager. selection is unused and can be <code>null</code>.
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection selection) {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		for (int i= 0; i< launches.length; i++) {
			ILaunch launch= launches[i];
			if (!launch.isTerminated()) {
				action.setEnabled(true);
				return;
			}
		}
		action.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#getSelection()
	 */
	protected IStructuredSelection getContext() {
		return new StructuredSelection(DebugPlugin.getDefault().getLaunchManager().getLaunches());
	}		
	
	
}
