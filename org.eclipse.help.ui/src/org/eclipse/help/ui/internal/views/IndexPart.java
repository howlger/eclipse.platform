/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class IndexPart extends HyperlinkTreePart implements IHelpUIConstants {
	private RoleFilter roleFilter;

	class IndexProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement == IndexPart.this) {
				return HelpSystem.getIndex().getEntries();
			}
			if (parentElement instanceof IIndexEntry) {
				return IndexPart.this.getChildren((IIndexEntry) parentElement);
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	class IndexLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IIndexEntry)
				return ((IIndexEntry) obj).getKeyword();
			if (obj instanceof IHelpResource)
				return ((IHelpResource) obj).getLabel();
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			return super.getImage(obj);
		}
	}

	class RoleFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof IIndexEntry) {
				return isNotFiltered((IIndexEntry) element);
			} else if (element instanceof IHelpResource) {
				return isNotFiltered((IHelpResource) element);
			}
			return false;
		}

		private boolean isNotFiltered(IIndexEntry entry) {
			IHelpResource[] topics = entry.getTopics();
			for (int i = 0; i < topics.length; i++) {
				if (isNotFiltered(topics[i]))
					return true;
			}

			IIndexEntry[] subentries = entry.getSubentries();
			for (int i = 0; i < subentries.length; i++) {
				if (isNotFiltered(subentries[i]))
					return true;
			}

			return false;
		}

		private boolean isNotFiltered(IHelpResource topic) {
			return HelpBasePlugin.getActivitySupport().isEnabled(topic.getHref());
		}
	}

	public IndexPart(Composite parent, FormToolkit toolkit, IToolBarManager tbm) {
		super(parent, toolkit, tbm);
		roleFilter = new RoleFilter();
	}

	protected void configureTreeViewer() {
		treeViewer.setContentProvider(new IndexProvider());
		treeViewer.setLabelProvider(new IndexLabelProvider());
	}

	public void init(ReusableHelpPart parent, String id, IMemento memento) {
		super.init(parent, id, memento);
		if (parent.isFilteredByRoles())
			treeViewer.addFilter(roleFilter);
	}

	protected void doOpen(Object obj) {
		if (obj instanceof IHelpResource) {
			parent.showURL(((IHelpResource) obj).getHref());
		} else if (obj instanceof IIndexEntry) {
			IIndexEntry entry = (IIndexEntry) obj;
			if (getChildren(entry).length > 0) {
				treeViewer.setExpandedState(obj, !treeViewer.getExpandedState(obj));
			}
			IHelpResource[] topics = entry.getTopics();
			if (topics.length == 1) {
				parent.showURL(topics[0].getHref());
			}
		}
	}

	protected boolean canAddBookmarks() {
		return true;
	}

	public void saveState(IMemento memento) {
	}

	public void toggleRoleFilter() {
		if (parent.isFilteredByRoles())
			treeViewer.addFilter(roleFilter);
		else
			treeViewer.removeFilter(roleFilter);
	}

	public void refilter() {
		treeViewer.refresh();
	}

	private Object[] getChildren(IIndexEntry entry) {
		/*
		 * Index entry has two types of children: topics and subentries.
		 * 
		 * The method returns topics among children only if number of the topics
		 * more than 1.
		 * 
		 * In case when the entry owns only one topic, this topic is not returned
		 * as child because the entry will represent this topic by its keyword.
		 */
		IHelpResource[] topics = entry.getTopics();
		IIndexEntry[] subentries = entry.getSubentries();

		if (topics.length <= 1) {
			return subentries;
		}

		Object[] childrens = new Object[topics.length + subentries.length];
		System.arraycopy(topics, 0, childrens, 0, topics.length);
		System.arraycopy(subentries, 0, childrens, topics.length, subentries.length);

		return childrens; 
	}

	protected Tree getTreeWidget() {
		return treeViewer.getTree();
	}
}
