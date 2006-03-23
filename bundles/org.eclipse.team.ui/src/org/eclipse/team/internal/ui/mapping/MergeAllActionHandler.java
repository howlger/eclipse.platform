/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.ide.IDE;

public class MergeAllActionHandler extends MergeActionHandler implements IDiffChangeListener {

	private MergeAllOperation operation;

	public MergeAllActionHandler(ISynchronizePageConfiguration configuration) {
		super(configuration);
		getContext().getDiffTree().addDiffChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.MergeActionHandler#getOperation()
	 */
	protected synchronized SynchronizationOperation getOperation() {
		if (operation == null) {
			operation = createOperation();
		}
		return operation;
	}

	protected MergeAllOperation createOperation() {
		return new MergeAllOperation(getJobName(), getConfiguration(), getMappings(), getContext());
	}

	private IMergeContext getContext() {
		return ((IMergeContext)getConfiguration().getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT));
	}

	private ResourceMapping[] getMappings() {
		return ((ISynchronizationScope)getConfiguration().getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_SCOPE)).getMappings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#diffsChanged(org.eclipse.team.core.diff.IDiffChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		synchronized (this) {
			operation = null;
		}
		setEnabled(event.getTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK) > 0 
				|| event.getTree().countFor(IThreeWayDiff.CONFLICTING, IThreeWayDiff.DIRECTION_MASK) > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#propertyChanged(org.eclipse.team.core.diff.IDiffTree, int, org.eclipse.core.runtime.IPath[])
	 */
	public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
		// Nothing to do
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.MergeActionHandler#dispose()
	 */
	public void dispose() {
		getContext().getDiffTree().removeDiffChangeListener(this);
		super.dispose();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (saveDirtyEditors() && promptToUpdate())
			return super.execute(event);
		return null;
	}
	
	/**
	 * Prompt to save all dirty editors and return whether to proceed
	 * or not.
	 * @return whether to proceed
	 * or not
	 */
	public final boolean saveDirtyEditors() {
		if(needsToSaveDirtyEditors()) {
			if(!saveAllEditors(getTargetResources(), confirmSaveOfDirtyEditor())) {
				return false;
			}
		}
		return true;
	}
	
	private IResource[] getTargetResources() {
		return getContext().getDiffTree().getAffectedResources();
	}

	/**
	 * Save all dirty editors in the workbench that are open on files that may
	 * be affected by this operation. Opens a dialog to prompt the user if
	 * <code>confirm</code> is true. Return true if successful. Return false
	 * if the user has canceled the command. Must be called from the UI thread.
	 * @param resources the root resources being operated on
	 * @param confirm prompt the user if true
	 * @return boolean false if the operation was canceled.
	 */
	public final boolean saveAllEditors(IResource[] resources, boolean confirm) {
		return IDE.saveAllEditors(resources, confirm);
	}

	/**
	 * Return whether dirty editor should be saved before this action is run.
	 * Default is <code>true</code>.
	 * 
	 * @return whether dirty editor should be saved before this action is run
	 */
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
	/**
	 * Returns whether the user should be prompted to save dirty editors. The
	 * default is <code>true</code>.
	 * 
	 * @return whether the user should be prompted to save dirty editors
	 */
	protected boolean confirmSaveOfDirtyEditor() {
		return true;
	}
	
	protected String getJobName() {
		String name = getConfiguration().getParticipant().getName();
		return NLS.bind(TeamUIMessages.MergeAllActionHandler_0, Utils.shortenText(30, name));
	}
	
	protected boolean promptToUpdate() {
		final IResourceDiffTree tree = getContext().getDiffTree();
		if (tree.isEmpty()) {
			return false;
		}
		final long count = tree.countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK) + tree.countFor(IThreeWayDiff.CONFLICTING, IThreeWayDiff.DIRECTION_MASK);
		if (count == 0)
			return false;
		final boolean[] result = new boolean[] {true};
		TeamUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				String sizeString = Long.toString(count);
				String message = tree.size() > 1 ? NLS.bind(TeamUIMessages.MergeAllActionHandler_1, new String[] { sizeString }) : 
					NLS.bind(TeamUIMessages.MergeAllActionHandler_2, new String[] { sizeString });
				result[0] = MessageDialog.openQuestion(getConfiguration().getSite().getShell(), 
						NLS.bind(TeamUIMessages.MergeAllActionHandler_3, new String[] { sizeString }), message); 					 
			}
		});
		return result[0];
	}
}
