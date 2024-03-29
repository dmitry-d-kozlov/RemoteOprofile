/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.AnnotationColumn;
import org.eclipse.ui.internal.texteditor.LineNumberColumn;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.ui.texteditor.rulers.RulerColumnRegistry;


public class STAnnotatedCSourceEditor extends CEditor implements LineBackgroundListener {
	private final static String ST_RULER= "STRuler";
	
	protected STContributedRulerColumn fAbstractSTRulerColumn;
	
	private IColumnSupport fColumnSupport;
	
	private LineNumberColumn fLineColumn;
	
	private STContributedRulerColumn fColumn;
	
	private StyledText fCachedTextWidget;
	
	private AbstractSTAnnotatedSourceEditorInput fInput;
	
	private ArrayList<ISTAnnotationColumn> fListColumns;
	
	private STChangeRulerColumn fSTChangeRulerColumn;
	
	@Override
	public void createPartControl(Composite parent){
		if (fInput == null){
			super.createPartControl(parent);
			return;
		}

		GridLayout layout= new GridLayout();
		layout.numColumns=fInput.getColumnCount()+1;
		layout.horizontalSpacing=0;
		parent.setLayout(layout);
		GridData  gd =new GridData();
		gd.grabExcessHorizontalSpace =false;
		gd.grabExcessVerticalSpace =true;

		parent.setLayoutData(gd );
            
		Composite fParentSv= new Composite(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan =fInput.getColumnCount()+1;
		gd.horizontalIndent=gd.verticalIndent=0;
		fParentSv.setLayoutData(gd);
		FillLayout fillLayout= new FillLayout(SWT.HORIZONTAL|SWT.VERTICAL);
		fillLayout.marginWidth=fillLayout.marginHeight=0;
		fParentSv.setLayout(fillLayout);
			
		super.createPartControl(fParentSv);

		STColumnSupport columnSupport= (STColumnSupport)getAdapter(IColumnSupport.class);
		RulerColumnRegistry registry = RulerColumnRegistry.getDefault();
			
		for(int i=1;i<=fInput.getColumnCount();i++){
			RulerColumnDescriptor abstractSTColumnDescriptor= registry.getColumnDescriptor(STContributedRulerColumn.ID);
			columnSupport.addSTColumn((CompositeRuler)getVerticalRuler(), abstractSTColumnDescriptor,fListColumns.get(i-1));
				
		}

		CompositeRuler vr = (CompositeRuler)super.getVerticalRuler();
		int count = 0;
		Font font = parent.getFont();
		FontData fd = font.getFontData()[0];
		fd.setStyle(SWT.BOLD);
		for(Iterator<?> iter = vr.getDecoratorIterator();iter.hasNext();){
			IVerticalRulerColumn column = (IVerticalRulerColumn)iter.next();
			if (column instanceof STContributedRulerColumn){
				STContributedRulerColumn fSTColumn = (STContributedRulerColumn)column;
				Label label = new Label(parent, SWT.BORDER);
				gd = new GridData();
				count++;
				if (count==1) gd.horizontalIndent=VERTICAL_RULER_WIDTH+5;
				else gd.horizontalIndent=0;
				gd.widthHint = fSTColumn.getWidth();
				label.setFont(new Font(label.getDisplay(), fd));
				label.setLayoutData(gd);
				label.moveAbove(fParentSv);
				label.setText(fSTColumn.getAnnotationColumn(0).getTitle());
				fSTColumn.setLabelColumn(label);

				if (fSTColumn.isShowingSTRuler()){
					ToolTipSupport.enableFor(fSTColumn);
				}
			}
		}
		
		Label label = new Label(parent, SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		label.setFont(new Font(label.getDisplay(), fd));
		label.setLayoutData(gd);
		label.moveAbove(fParentSv);
		label.setText(getTitle());
			
		showLinesColored();
		
		if (getViewer() != null){
			ISourceViewer sv = getViewer();

			if (sv.getTextWidget() != null){
					
				fCachedTextWidget =  sv.getTextWidget();
				fCachedTextWidget.addLineBackgroundListener(this);
			}
		}
	}
	
	@Override
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		
		CompositeRuler vr = (CompositeRuler)super.getVerticalRuler();
		
		for(Iterator<?> iter = vr.getDecoratorIterator();iter.hasNext();){
			IVerticalRulerColumn column = (IVerticalRulerColumn)iter.next();
			if (column instanceof STContributedRulerColumn){
				STContributedRulerColumn fSTColumn = (STContributedRulerColumn)column;
				IAction stprofcolAction = getAction(ISTTextEditorActionConstants.ST_TOGGLE);
				stprofcolAction.setChecked(fSTColumn != null && fSTColumn.isShowingSTRuler());
			}
			else if (column instanceof LineNumberColumn){
				LineNumberColumn fLineColumn = (LineNumberColumn)column;
				IAction lineNumberAction= getAction(ITextEditorActionConstants.LINENUMBERS_TOGGLE);
				lineNumberAction.setChecked(fLineColumn != null && fLineColumn.isShowingLineNumbers());
			}
		}
		
		if (fInput != null){
			IAction stAction= getAction(ISTTextEditorActionConstants.ST_TOGGLE);
			menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, stAction);
		}
		
		
		
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required) {
		if (IColumnSupport.class.equals(required)) {
			if (fColumnSupport == null)
				fColumnSupport= createSTColumnSupport();
			return fColumnSupport;
		}

		return super.getAdapter(required);
	}
	
	protected IColumnSupport createSTColumnSupport() {
		return new STColumnSupport(this,RulerColumnRegistry.getDefault());
	}
	
	protected class STColumnSupport extends AbstractDecoratedTextEditor.ColumnSupport{
		private final STAnnotatedCSourceEditor fEditor;
		private final RulerColumnRegistry fRegistry;
		private final ArrayList<ISTAnnotationColumn> fColumns;

		public STColumnSupport(STAnnotatedCSourceEditor editor, RulerColumnRegistry registry) {
			super(editor, registry);
			fEditor = editor;
			fRegistry = registry;
			fColumns= new ArrayList<ISTAnnotationColumn>();
		}
		
		private int computeIndex(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
			int index= 0;
			List<?> all= fRegistry.getColumnDescriptors();
			int newPos= all.indexOf(descriptor);
			for (Iterator<?> it= ruler.getDecoratorIterator(); it.hasNext();) {
				IVerticalRulerColumn column= (IVerticalRulerColumn) it.next();
				if (column instanceof IContributedRulerColumn) {
					RulerColumnDescriptor rcd= ((IContributedRulerColumn)column).getDescriptor();
					if (rcd != null && all.indexOf(rcd) > newPos)
						break;
				} else if ("org.eclipse.jface.text.source.projection.ProjectionRulerColumn".equals(column.getClass().getName())) { //$NON-NLS-1$
					// projection column is always the rightmost column
					break;
				}
				index++;
			}
			return index;
		}
		
		public void addSTColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor,final ISTAnnotationColumn annotationColumn) {
			
			final int idx= computeIndex(ruler, descriptor);
			
			SafeRunnable runnable= new SafeRunnable() {
				public void run() throws Exception {
					IContributedRulerColumn column= descriptor.createColumn(fEditor);
					fColumns.add(annotationColumn);
					initializeColumn(column);
					ruler.addDecorator(idx, column);
				}
			};
			SafeRunner.run(runnable);
		}
		
		protected void initializeColumn(IContributedRulerColumn column) {
			super.initializeColumn(column);
			RulerColumnDescriptor descriptor= column.getDescriptor();
			IVerticalRuler ruler= getVerticalRuler();
			if (ruler instanceof CompositeRuler) {
				if (AnnotationColumn.ID.equals(descriptor.getId())) {
					((AnnotationColumn)column).setDelegate(createAnnotationRulerColumn((CompositeRuler) ruler));
				} else if (LineNumberColumn.ID.equals(descriptor.getId())) {
					fLineColumn= ((LineNumberColumn) column);
					fLineColumn.setForwarder(new LineNumberColumn.ICompatibilityForwarder() {
						public IVerticalRulerColumn createLineNumberRulerColumn() {
							return fEditor.createLineNumberRulerColumn();
						}
						public boolean isQuickDiffEnabled() {
							return fEditor.isPrefQuickDiffAlwaysOn();
						}
						public boolean isLineNumberRulerVisible() {
							return fEditor.isLineNumberRulerVisible();
						}
					});
				}
				if (STContributedRulerColumn.ID.equals(descriptor.getId())){
					fColumn = ((STContributedRulerColumn) column);
					//this is a workaround...
					fColumn.setForwarder(new STContributedRulerColumn.ICompatibilityForwarder() {
						public IVerticalRulerColumn createSTRulerColumn() {
							if (fColumns != null && fColumns.size() > 0){
								IVerticalRulerColumn fDelegate = fEditor.createSTRulerColumn(fColumns.get(fColumns.size()-1));
								return fDelegate;
							}
							return null;
						}
						public boolean isQuickDiffEnabled() {
							return fEditor.isPrefQuickDiffAlwaysOn();
						}
						public boolean isSTRulerVisible() {
							return fEditor.isSTRulerVisible();
						}
					});
				}
			}
		}
	}
	
	protected IVerticalRulerColumn createSTRulerColumn(ISTAnnotationColumn annotationColumn) {
		fSTChangeRulerColumn= new STChangeRulerColumn(getSharedColors(),annotationColumn);
		((IChangeRulerColumn) fSTChangeRulerColumn).setHover(createChangeHover());
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		
		return fSTChangeRulerColumn;
	}

	public void lineGetBackground(LineBackgroundEvent event) {
		if (fInput != null){
			StyledTextContent c = (StyledTextContent)event.data;
			int line = c.getLineAtOffset(event.lineOffset);
			event.lineBackground = fInput.getColor(line);
		}
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		
		if (input!=null && input instanceof AbstractSTAnnotatedSourceEditorInput){
			fInput = (AbstractSTAnnotatedSourceEditorInput)input;
			fListColumns = fInput.getColumns();
		}
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		
		ResourceAction action;
		
		action= new ResourceAction(STTextEditorMessages.getBundleForConstructedKeys(), "Editor.ToggleSTColumnAction.", IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				toggleSTRuler();
			}
		};
		
		if (fInput != null){
			action.setActionDefinitionId(ISTTextEditorActionDefinitionIds.ST_TOGGLE);
			setAction(ISTTextEditorActionConstants.ST_TOGGLE, action);
		}
	}
	
	private void toggleSTRuler() {
		// globally
		IPreferenceStore store= EditorsUI.getPreferenceStore();
		store.setValue(ST_RULER, !isSTRulerVisible());
	}
	
	protected boolean isSTRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store != null ? store.getBoolean(ST_RULER) : true;
	}
	
	private static class ToolTipSupport extends DefaultToolTip{
		private STContributedRulerColumn control;
		static class ToolTipArea{
			final int line;
			final ISTAnnotationColumn ac;
			
			ToolTipArea(int line,ISTAnnotationColumn ac){
				this.line = line;
				this.ac = ac;
			}
			
			public String getToolTip(){
				return ac.getTooltip(line);
			}
		
		}

		protected ToolTipSupport(STContributedRulerColumn control, int style,boolean manualActivation) {
			super(control.getControl(), style, manualActivation);
			this.control = control;
		}

		protected Object getToolTipArea(Event event) {
			int line = control.toDocumentLineNumber(event.y);
			return new ToolTipArea(line,control.getAnnotationColumn(line));
		}

		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			comp.setLayout(new FillLayout());
			Label b = new Label(comp,SWT.NONE);
			ToolTipArea area  = (ToolTipArea)getToolTipArea(event);
			if (area != null && area.getToolTip().trim().length()>0){
				b.setText(area.getToolTip());
			}
			return comp;
		}

		public static void enableFor(STContributedRulerColumn control) {
			new ToolTipSupport(control, ToolTip.NO_RECREATE, false);
		}
	}
	
	protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors) {
		IOverviewRuler ruler= new STOverviewRuler(getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors);
		MarkerAnnotationPreferences fAnnotationPreferences = EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		Iterator<?> e= fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference preference= (AnnotationPreference) e.next();
			if (preference.contributesToHeader())
				ruler.addHeaderAnnotationType(preference.getAnnotationType());
		}
		return ruler;
		
	}
	
	private void showLinesColored(){
		STOverviewRuler or = (STOverviewRuler)getOverviewRuler();
		AnnotationModel am = (AnnotationModel)or.getModel();
		IDocument doc = getSourceViewer().getDocument();
		int lines = doc.getNumberOfLines();

		for(int i=0;i<lines;i++){
			try {
				Color color = fInput.getColor(i);
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				if (r !=255 || g != 255 || b!= 255){
					int offset = doc.getLineOffset(i);
					String type = STAnnotatedSourceEditorActivator.getUniqueIdentifier() + STAnnotatedSourceEditorActivator.getAnnotationType(); 
					Annotation annotation = new Annotation(type,true,"");
					or.setAnnotationColor(annotation, color);
					am.addAnnotation(annotation,new Position(offset));
			}
			} catch (BadLocationException e) {
				Status s =new Status(Status.ERROR,STAnnotatedSourceEditorActivator.PLUGIN_ID,Status.ERROR,e.getMessage(),e);
				STAnnotatedSourceEditorActivator.getDefault().getLog().log(s);
			}
		}
	}
}
