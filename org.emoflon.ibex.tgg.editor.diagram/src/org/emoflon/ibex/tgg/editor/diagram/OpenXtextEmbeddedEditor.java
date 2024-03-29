package org.emoflon.ibex.tgg.editor.diagram;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.sirius.diagram.DNodeList;
import org.eclipse.sirius.diagram.DNodeListElement;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.emoflon.ibex.tgg.editor.diagram.ui.XtextEmbeddedEditor;
import org.emoflon.ibex.tgg.editor.tgg.AttrCond;
import org.emoflon.ibex.tgg.editor.tgg.Rule;
import org.emoflon.ibex.tgg.editor.ui.internal.EditorActivator;

import com.google.inject.Injector;

// Wrapper for opening the Xtext embedded editor
public class OpenXtextEmbeddedEditor {
	private final String ENDING_BLOCK_DELIMITER = "}";

	public boolean open(EObject decorator) {
		return open(decorator, null);
	}

	public boolean open(EObject decorator, EObject originalSemanticElement) {
		String endingBlockDelimiter = null;
		if (originalSemanticElement != null && originalSemanticElement instanceof AttrCond) {
			endingBlockDelimiter = ENDING_BLOCK_DELIMITER;
		} else if (decorator instanceof DNodeList) {
			String mappingName = ((DNodeList) decorator).getActualMapping().getName();
			// TODO Change strings with constants
			if (mappingName.equals("attrCondContainer")) {
				EObject rootElement = (((DNodeList) decorator).getTarget());
				endingBlockDelimiter = ENDING_BLOCK_DELIMITER;
				if (rootElement instanceof Rule) {
					if (((Rule) rootElement).getAttrConditions().size() > 0) {
						originalSemanticElement = (EObject) ((Rule) rootElement).getAttrConditions().get(0);
					}
				} 
			}
		} else if (decorator instanceof DNodeListElement) {
			String mappingName = ((DNodeListElement) decorator).getActualMapping().getName();
			if (mappingName.equals("atrrConditionNode")) {
				endingBlockDelimiter = ENDING_BLOCK_DELIMITER;
			}
		}
		DiagramEditPart diagramEditPart = ((DiagramEditor) getActiveEditor()).getDiagramEditPart();
		EditPart editPart = diagramEditPart.findEditPart(diagramEditPart, decorator);
		if (editPart != null && (editPart instanceof IGraphicalEditPart)) {
			openEmbeddedEditor((IGraphicalEditPart) editPart, originalSemanticElement, endingBlockDelimiter);
		}

		return true;

	}

	private void openEmbeddedEditor(IGraphicalEditPart graphicalEditPart, EObject originalSemanticElement,
			String endingBlockDelimiter) {
		XtextEmbeddedEditor embeddedEditor = new XtextEmbeddedEditor(graphicalEditPart, originalSemanticElement,
				endingBlockDelimiter, getInjector());
		embeddedEditor.showEditor();
	}

	/**
	 * Return the injector associated to you domain model plug-in.
	 * 
	 * @return
	 */
	private Injector getInjector() {
		return EditorActivator.getInstance().getInjector(EditorActivator.ORG_EMOFLON_IBEX_TGG_EDITOR_TGG);
	}

	private IEditorPart getActiveEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		return page.getActiveEditor();
	}
}
