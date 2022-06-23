package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.emoflon.ibex.tgg.editor.builder.TGGBuildUtil;
import org.moflon.core.ui.AbstractCommandHandler;

public class OpenProjectDiagramFromSirius extends AbstractCommandHandler {

	private final TGGGraphicalEditorLauncher editorLauncher = new TGGGraphicalEditorLauncher();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow iWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (iWorkbenchWindow == null)
			return null;
		IWorkbenchPage iWorkbenchPage = iWorkbenchWindow.getActivePage();
		if (iWorkbenchPage == null)
			return null;
		IEditorPart iEditorPart = iWorkbenchPage.getActiveEditor();
		if (iEditorPart == null)
			return null;
		IEditorInput input = iEditorPart.getEditorInput();
		IFile file = null;
		if (input instanceof URIEditorInput)
			file = (IFile) ResourcesPlugin.getWorkspace().getRoot()
					.findMember(((URIEditorInput) input).getURI().toPlatformString(true));
		IProject project = file.getProject();
		IFile schemaFile = findSchemaFileInProject(project);
		if (schemaFile != null) {
			editorLauncher.open(schemaFile.getFullPath());
		}
		return null;
	}

	private IFile findSchemaFileInProject(IProject project) {
		if (project == null)
			return null;
		return project.getFile(TGGBuildUtil.SCHEMA_FILE);
	}
}
