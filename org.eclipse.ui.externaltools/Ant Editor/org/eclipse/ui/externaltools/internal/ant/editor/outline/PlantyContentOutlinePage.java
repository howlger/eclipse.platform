package org.eclipse.ui.externaltools.internal.ant.editor.outline;

//
// PlantyContentOutlinePage.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyEditor;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyException;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Content outline page for planty.
 */
public class PlantyContentOutlinePage extends ContentOutlinePage {
	protected IFile file;

	
	/**
	 * The content provider for the objects shown in the outline view.
	 */
    private class PlantyContentProvider implements ITreeContentProvider {

        /**
         * do nothing
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        
        /**
         * do nothing
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
         */
        public void inputChanged(
            Viewer viewer,
            Object oldInput,
            Object newInput) {
        }
        

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
         */
        public Object[] getChildren(Object parentNode) {
            XmlElement tempParentElement = (XmlElement)parentNode;
            List tempChilds = tempParentElement.getChildNodes();
            Object[] tempChildObjects = new Object[tempChilds.size()];
            for(int i=0; i<tempChilds.size(); i++) {
                tempChildObjects[i] = (Object)tempChilds.get(i);
            }
            return tempChildObjects;
        }


        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
         */
        public Object getParent(Object aNode) {
            XmlElement tempElement = (XmlElement)aNode;
            return tempElement.getParentNode();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
         */
        public boolean hasChildren(Object aNode) {
            return ((XmlElement)aNode).getChildNodes().size() > 0;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
         */
        public Object[] getElements(Object anInputElement) {
            return getChildren(anInputElement);
        }

    }
    
    
    /**
     * The label provider for the objects shown in the outline view.
     */
    private class PlantyLabelProvider implements ILabelProvider {
            /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
         */
        public void addListener(ILabelProviderListener listener) {
        }


        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose() {
        }


        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
         */
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }


        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
         */
        public void removeListener(ILabelProviderListener listener) {
        }


        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
         */
        public Image getImage(Object anElement) {
        	XmlElement tempElement = (XmlElement)anElement;
        	if("target".equals(tempElement.getName())) {
        		return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_TARGET);
        	}
        	if("project".equals(tempElement.getName())) {
        		return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_PROJECT);
        	}
        	if("property".equals(tempElement.getName())) {
        		return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_PROPERTY);
        	}
            return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_TASK);
        }
        

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
         */
        public String getText(Object aNode) {
            return ((XmlElement)aNode).getDisplayName();
        }

}
    
	class OutlineAction extends Action {
		private Shell shell;
		public OutlineAction(String label) {
			super(label);
			getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					setEnabled(!event.getSelection().isEmpty());
				}
			});
		}
		public void setShell(Shell shell) {
			this.shell = shell;
		}
		public void run() {
			MessageDialog.openInformation(shell,"Bla", "Blub"); //$NON-NLS-1$
		}
	}
	
    
    /**
     * Creates a new PlantyContentOutlinePage.
     */
    public PlantyContentOutlinePage(IFile aFile) {
    	super();
    	this.file = aFile;
    }


    /**  
     * Creates the control (outline view) for this page
     */
    public void createControl(Composite parent) {
    	super.createControl(parent);
    
    	TreeViewer viewer = getTreeViewer();
        
        /*
         * We might want to implement our own content provider.
         * This content provider should be able to work on a dom like tree
         * structure that resembles the file contents.
         */
    	viewer.setContentProvider(new PlantyContentProvider());

        /*
         * We probably also need our own label provider.
         */ 
    	viewer.setLabelProvider(new PlantyLabelProvider());
    	viewer.setInput(getContentOutline(file));
    	viewer.expandToLevel(2);
    }

    
    /**
     * Gets the content outline for a given input element.
     * Returns the outline (a list of MarkElements), or null
     * if the outline could not be generated.
     */
    protected XmlElement getContentOutline(IAdaptable input) {
        /*
         * What happens here:
         * The file is parsed by the SAX Parser.
         * The Parser then creates the DOM Tree of the file.
         * The root element is returned here.
         */
        IFile tempFile = (IFile)input;
         
		String tempWholeDocumentString = getFileContentAsString(tempFile);         
         
        // Create the parser
        SAXParser tempParser;
        try {
            SAXParserFactory tempSAXParserFactory = SAXParserFactory.newInstance();
            tempSAXParserFactory.setNamespaceAware(false);
            tempParser = tempSAXParserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return null;
        } catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
            return null;
        } catch (FactoryConfigurationError e) {
			ExternalToolsPlugin.getDefault().log(e);
            return null;
        }

        // Create the handler
        OutlinePreparingHandler tempHandler = null;
        try {
            tempHandler = new OutlinePreparingHandler(tempFile.getLocation().toFile().getParentFile());
        } catch (ParserConfigurationException e) {
			ExternalToolsPlugin.getDefault().log(e);
            return null;
        }
        
        // Parse!
        try {
        	InputSource tempInputSource = new InputSource(new StringReader(tempWholeDocumentString));
            tempParser.parse(tempInputSource, tempHandler);
        } catch(SAXParseException e) {
            // ignore that on purpose
        } catch (SAXException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return null;
        } catch (IOException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return null;
        }
        
        XmlElement tempRootElement = tempHandler.getRootElement();
        XmlElement tempElement = new XmlElement("");
        
        // Fix position of tags
        if(tempRootElement != null) {
	        fixTagLocations(tempRootElement, tempWholeDocumentString);
	        tempElement.addChildNode(tempRootElement);
        }
                
        return tempElement;
    }

    
    /**
     * Fixes the starting positions and offset of all tags after parsing.
     */
    private void fixTagLocations(XmlElement aRootElement, String aWholeDocumentString) {
		BufferedReader tempReader = new BufferedReader(new StringReader(aWholeDocumentString));

        // Fill an array of all lines
        ArrayList tempLineList = new ArrayList();
        String tempLine;
        try {
            while( (tempLine = tempReader.readLine()) != null) {
                tempLineList.add(tempLine);
            }
        } catch (IOException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return;
        }
				
		if(tempLineList.isEmpty()) {
			return;
		}
				
		// Determine all line lenghts
		int [] tempLineLengths = new int [tempLineList.size()];
		tempLineLengths[0] = ((String)tempLineList.get(0)).length();
		for (int i = 1; i < tempLineLengths.length; i++) {
            tempLineLengths[i] = ((String)tempLineList.get(i)).length() + tempLineLengths[i-1];
        }

        
        XmlElement tempElement = aRootElement;
        while(tempElement != null) {
			/*
			 * Every element must have a starting row and column > 0 set, though
			 * it might be that the ending row and column are not set, because
			 * of a non-valid file
			 */
            int tempStartingRow = tempElement.getStartingRow(); // > 0
            int tempStartingColumn = tempElement.getStartingColumn(); // > 0
            tempLine = (String)tempLineList.get(--tempStartingRow);
            if(tempStartingColumn > 0) {
                tempLine = tempLine.substring(0, tempStartingColumn-1);
            }
            int tempLessThanIndex = tempLine.lastIndexOf('<');
            while(tempLessThanIndex == -1) {
            	/*
            	 * must be found in this loop since, this element exists.
            	 */
                tempLine = (String)tempLineList.get(--tempStartingRow);
                tempLessThanIndex = tempLine.lastIndexOf('<');
            }
            
            tempElement.setStartingColumn(tempLessThanIndex+1); // 0-based -> 1-based
            tempElement.setStartingRow(tempStartingRow+1);
            int tempStartingIndex = (tempStartingRow > 0 ? tempLineLengths[tempStartingRow-1] : 0);
            tempStartingIndex = (isMsOs()) ? tempStartingIndex+tempStartingRow : tempStartingIndex; // add one char for ever \r
            tempStartingIndex += tempLessThanIndex;
            int tempOffset = findIndexOfSubstringAfterIndex(aWholeDocumentString, tempElement.getName(), tempStartingIndex)-1;
			if(isMsOs()) {
	            tempOffset += tempStartingRow; // add one char for every \r
			}
            tempElement.setOffset(tempOffset);
            int tempLength;
            if(tempElement.getEndingRow() <= 0) {
            	tempLength = -1;
            }
            else {
            	tempStartingIndex = tempElement.getEndingRow() - 1;
            	if(tempElement.getEndingColumn() > 0) {
            		tempStartingIndex += tempElement.getEndingColumn();
            	}
            	else {
	            	tempStartingIndex += ((String)tempLineList.get(tempElement.getEndingRow()-1)).length();
            	}
            	tempStartingIndex += -2;
            	if(tempElement.getEndingRow() > 1) {
            		tempStartingIndex += tempLineLengths[tempElement.getEndingRow()-2];
            	}
	            tempLength = findIndexOfSubstringAfterIndex(aWholeDocumentString, ">", tempStartingIndex)+1;
	            tempLength -= tempOffset;
				if(isMsOs()) {
		            tempLength += tempStartingRow; // add one char for every
				}
            }
			tempElement.setLength(tempLength);
            tempElement = findNextElementToFixAfter(tempElement, true);
        }    
    }


	/**
	 * Returns whether we are on a Microsoft OS.
	 */
	public boolean isMsOs() {
		String tempSeparator = System.getProperty("line.separator");
		if(tempSeparator.length() > 1) {
			return true;
		}
		return false;
	}
	

    
    /**
     * Returns the next element that is to be fixed.
     * @param anElement the element that has just been fixed
     * @param aProcessChildsFlag whether childs still need to be fixed or not
     */
    private XmlElement findNextElementToFixAfter(XmlElement anElement, boolean aProcessChildsFlag) {
        List tempChildNodes = null;
        
        if(aProcessChildsFlag) {
	        tempChildNodes = anElement.getChildNodes();
	        if(tempChildNodes.size() > 0) {
	            return (XmlElement)tempChildNodes.get(0);
	        }
        }
        
        XmlElement tempParent = anElement.getParentNode();
        if(tempParent != null) {
            tempChildNodes = tempParent.getChildNodes();
            int tempIndex = tempChildNodes.indexOf(anElement);
            if(tempIndex+1 < tempChildNodes.size()) {
                return (XmlElement)tempChildNodes.get(tempIndex+1);
            }
            return findNextElementToFixAfter(tempParent, false);
        }
        return null;
    }


	/**
	 * Returns the content of the specified file as <code>String</code>.
	 * <P>
	 * Tabs will be converted to spaces according to the tab size.
	 */
	protected String getFileContentAsString(IFile aFile) {
        InputStream tempStream;
        try {
            tempStream = aFile.getContents();
        } catch (CoreException e) {
        	ExternalToolsPlugin.getDefault().log(e);
        	return "";
        }
        
        InputStreamReader tempReader = new InputStreamReader(tempStream);
        BufferedReader tempBufferedReader = new BufferedReader(tempReader);

        StringBuffer tempResult = new StringBuffer();
        try {
            String tempLine;
            tempLine = tempBufferedReader.readLine();
        
            while(tempLine != null) {
                if(tempResult.length() != 0) {
                	tempResult.append("\n");
                }
                tempResult.append(tempLine);
                tempLine = tempBufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

//		tempResult = convertTabsToSpacesIn(tempResult);

		return tempResult.toString();
	}


	/**
	 * Replaces all tabs with the appropriate amount of spaces in the specified
	 * <code>StringBuffer</code>.
	 * 
	 * @return the modified <code>aStringBuffer</code>
	 */
	protected StringBuffer convertTabsToSpacesIn(StringBuffer aStringBuffer) {
	
		String tempReplacementString = "";
		for(int i=0; i< PlantyEditor.TAB_WIDTH; i++) {
			tempReplacementString += " ";
		}
		
		int tempIndex = aStringBuffer.toString().indexOf("\t");
		while(tempIndex != -1) {
			aStringBuffer = aStringBuffer.replace(tempIndex, tempIndex+1, tempReplacementString);
			tempIndex = aStringBuffer.toString().indexOf("\t");
		}
		
		return aStringBuffer;
	}	


	/**
	 * Returns the index of the first occurence of <code>aSubString</code> in 
	 * <code>aSourceString</code> after <code>aStartIndex</code>.
	 * 
	 * @throws PlantyException if the specified sub string does not exist in
	 * the source string.
	 */
	protected int findIndexOfSubstringAfterIndex(String aSourceString, String aSubString, int aStartIndex) {
		int tempIndex = aSourceString.indexOf(aSubString, aStartIndex);
		if(tempIndex == -1) {
			throw new PlantyException("Substring '"+aSubString+"' cannot be found in the specified source string.");
		}
		return tempIndex;
	}		


    /**
     * Forces the page to update its contents.
     *
     * @see ReadmeEditor#doSave(IProgressMonitor)
     */
    public void update() {
    	getControl().setRedraw(false);
    	getTreeViewer().setInput(getContentOutline(file));
    	getTreeViewer().expandToLevel(2);
    	getControl().setRedraw(true);
    }
}
