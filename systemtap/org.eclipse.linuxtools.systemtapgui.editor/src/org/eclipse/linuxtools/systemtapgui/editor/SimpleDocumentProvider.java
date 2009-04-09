/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtapgui.editor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

public class SimpleDocumentProvider extends AbstractDocumentProvider {
	public boolean canSaveDocument(Object element) {
		return super.canSaveDocument(element);
	}

	protected IDocument createDocument(Object element) throws CoreException {
		if (element instanceof IEditorInput) {
			IDocument document= new Document();
			if (setDocumentContent(document, (IEditorInput) element)) {
				setupDocument(document);
			}
			return document;
		}
	
		return null;
	}
	
	/**
	 * Tries to read the file pointed at by <code>input</code> if it is an
	 * <code>IPathEditorInput</code>. If the file does not exist, <code>true</code>
	 * is returned.
	 *  
	 * @param document the document to fill with the contents of <code>input</code>
	 * @param input the editor input
	 * @return <code>true</code> if setting the content was successful or no file exists, <code>false</code> otherwise
	 * @throws CoreException if reading the file fails
	 */
	private boolean setDocumentContent(IDocument document, IEditorInput input) throws CoreException {
		Reader reader;
		String inputClassName = input.getClass().getName();
		try {
			if (input instanceof IPathEditorInput){
				reader= new FileReader(((IPathEditorInput)input).getPath().toFile());
			}
			else if ( inputClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" )
	                || inputClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) )
	            // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
	            // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
	            // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
	            // opening a file from the menu File > Open... in Eclipse 3.3.x
	            {
	                reader = new FileReader( new File( input.getToolTipText() ) );
	            }
	            else
	            {
	                return false;
	            }
		} catch (FileNotFoundException e) {
			// return empty document and save later
			return true;
		}
		
		try {
			setDocumentContent(document, reader);
			return true;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.linuxtools.systemtapgui.editor", IStatus.OK, "error reading file", e)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads in document content from a reader and fills <code>document</code>
	 * 
	 * @param document the document to fill
	 * @param reader the source
	 * @throws IOException if reading fails
	 */
	private void setDocumentContent(IDocument document, Reader reader) throws IOException {
		Reader in= new BufferedReader(reader);
		try {
			
			StringBuffer buffer= new StringBuffer(512);
			char[] readBuffer= new char[512];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			
			document.set(buffer.toString());
		} finally {
			in.close();
		}
	}

	/**
	 * Set up the document - default implementation does nothing.
	 * 
	 * @param document the new document
	 */
	protected void setupDocument(IDocument document) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doSaveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		String elementClassName = element.getClass().getName();
		File file = null;
		if (element instanceof IPathEditorInput) {
			IPathEditorInput pei= (IPathEditorInput) element;
			IPath path= pei.getPath();
			file= path.toFile();
		}
		   else if ( elementClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" )
		            || elementClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) )
		        // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
		        // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
		        // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
		        // opening a file from the menu File > Open... in Eclipse 3.3.x
		        {
		            file = new File( ( ( IEditorInput ) element ).getToolTipText() );
		        }
		if (file!=null){
			try {
				file.createNewFile();

				if (file.exists()) {
					if (file.canWrite()) {
						Writer writer= new FileWriter(file);
						writeDocumentContent(document, writer, monitor);
					} else
						throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.linuxtools.systemtapgui.editor", IStatus.OK, "file is read-only", null)); //$NON-NLS-1$ //$NON-NLS-2$
				} else
					throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.linuxtools.systemtapgui.editor", IStatus.OK, "error creating file", null)); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.linuxtools.systemtapgui.editor", IStatus.OK, "error when saving file", e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Saves the document contents to a stream.
	 * 
	 * @param document the document to save
	 * @param writer the stream to save it to
	 * @param monitor a progress monitor to report progress
	 * @throws IOException if writing fails
	 */
	private void writeDocumentContent(IDocument document, Writer writer, IProgressMonitor monitor) throws IOException {
		Writer out= new BufferedWriter(writer);
		try {
			out.write(document.get());
		} finally {
			out.close();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isModifiable(java.lang.Object)
	 */
	public boolean isModifiable( Object element )
    {
        String elementClassName = element.getClass().getName();
        if ( element instanceof IPathEditorInput )
        {
            IPathEditorInput pei = ( IPathEditorInput ) element;
            File file = pei.getPath().toFile();
            return file.canWrite() || !file.exists(); // Allow to edit new files
        }
        else if ( elementClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" )
            || elementClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) )
        // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
        // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
        // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
        // opening a file from the menu File > Open... in Eclipse 3.3.x
        {
            File file = new File( ( ( IEditorInput ) element ).getToolTipText() );
            return file.canWrite() || !file.exists(); // Allow to edit new files
        }
        
        return false;
    }

	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isReadOnly(java.lang.Object)
	 */
	public boolean isReadOnly(Object element) {
		return false;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isStateValidated(java.lang.Object)
	 */
	public boolean isStateValidated(Object element) {
		return true;
	}
}