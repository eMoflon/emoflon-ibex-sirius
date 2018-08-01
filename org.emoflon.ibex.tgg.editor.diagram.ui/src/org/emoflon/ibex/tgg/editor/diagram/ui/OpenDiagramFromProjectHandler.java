package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.moflon.core.ui.AbstractCommandHandler;

public class OpenDiagramFromProjectHandler extends AbstractCommandHandler {

	private final TGGGraphicalEditorLauncher editorLauncher = new TGGGraphicalEditorLauncher();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IFile file = null;
		ISelection selection = window.getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IAdaptable) {
                IProject project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
                file = findSchemaFileInProject(project);
            }
		}
		if (file != null) {
			editorLauncher.open(file.getFullPath());
		}
		return null;
	}

	private IFile findSchemaFileInProject(IProject project) {
		if (project == null)
			return null;
		return project.getFile(IbexTGGNature.SCHEMA_FILE);
	}
}
