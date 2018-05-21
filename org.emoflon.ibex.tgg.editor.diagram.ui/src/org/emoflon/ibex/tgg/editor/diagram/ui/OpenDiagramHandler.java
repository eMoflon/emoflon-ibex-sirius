package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.moflon.core.ui.AbstractCommandHandler;

public class OpenDiagramHandler extends AbstractCommandHandler {

	private final TGGGraphicalEditorLauncher editorLauncher = new TGGGraphicalEditorLauncher();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		IEditorInput input = activePage.getActiveEditor().getEditorInput();
		IFile tggFile = ((IFileEditorInput) input).getFile();
		editorLauncher.open(tggFile.getFullPath());
		return null;
	}
}
