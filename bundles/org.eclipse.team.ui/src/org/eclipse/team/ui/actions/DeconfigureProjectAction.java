package org.eclipse.team.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ui.Policy;

/**
 * Action for deconfiguring a project. Deconfiguring involves removing
 * associated provider for the project.
 */
public class DeconfigureProjectAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IProject project = getSelectedProjects()[0];
					TeamPlugin.getManager().removeProvider(project, monitor);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("DeconfigureProjectAction.deconfigureProject"), PROGRESS_BUSYCURSOR);
	}
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IProject[] selectedProjects = getSelectedProjects();
		if (selectedProjects.length != 1) return false;
		if (TeamPlugin.getManager().getProvider(selectedProjects[0]) == null) return false;
		return true;
	}
}