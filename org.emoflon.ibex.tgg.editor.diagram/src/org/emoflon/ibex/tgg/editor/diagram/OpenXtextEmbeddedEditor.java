package org.emoflon.ibex.tgg.editor.diagram;

import com.google.inject.Injector;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.business.internal.metamodel.spec.DNodeListElementSpec;
import org.eclipse.sirius.diagram.business.internal.metamodel.spec.DNodeListSpec;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.emoflon.ibex.tgg.editor.diagram.ui.XtextEmbeddedEditor;
import org.moflon.tgg.mosl.tgg.AttrCond;
import org.moflon.tgg.mosl.tgg.ComplementRule;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.ui.internal.TGGActivator;

public class OpenXtextEmbeddedEditor {
	private final String ENDING_BLOCK_DELIMITER = "}";

	public boolean open(EObject decorator) {
		return open(decorator, null);
	}

	public boolean open(EObject decorator, EObject originalSemanticElement) {
		String endingBlockDelimiter = null;
		if (originalSemanticElement != null && originalSemanticElement instanceof AttrCond) {
			endingBlockDelimiter = ENDING_BLOCK_DELIMITER;
		} else if (decorator instanceof DNodeListSpec) {
			String mappingName = ((DNodeListSpec) decorator).getActualMapping().getName();
			if (mappingName.equals("attrCondContainer") || mappingName.equals("attrCondContainerCmpl")) {
				EObject rootElement = (((DNodeListSpec) decorator).getTarget());
				endingBlockDelimiter = ENDING_BLOCK_DELIMITER;
				if (rootElement instanceof Rule) {
					if (((Rule) rootElement).getAttrConditions().size() > 0) {
						originalSemanticElement = (EObject) ((Rule) rootElement).getAttrConditions().get(0);
					}
				} else if (rootElement instanceof ComplementRule) {
					if (((ComplementRule) rootElement).getAttrConditions().size() > 0) {
						originalSemanticElement = (EObject) ((ComplementRule) rootElement).getAttrConditions().get(0);
					}
				}
			}
		} else if (decorator instanceof DNodeListElementSpec) {
			String mappingName = ((DNodeListElementSpec) decorator).getActualMapping().getName();
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
		return TGGActivator.getInstance().getInjector(TGGActivator.ORG_MOFLON_TGG_MOSL_TGG);
	}

	private IEditorPart getActiveEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		return page.getActiveEditor();
	}
}
