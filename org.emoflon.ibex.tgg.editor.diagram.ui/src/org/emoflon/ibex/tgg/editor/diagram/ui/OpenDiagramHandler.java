package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
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
		IFile file = null;
		IWorkbenchPage activePage = window.getActivePage();
		IEditorPart editorPart = activePage.getActiveEditor();
		if (editorPart != null) {
			IEditorInput input = editorPart.getEditorInput();
			if (input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
			}
		}
		if (file != null) {
			editorLauncher.open(file.getFullPath());
		}
		return null;
	}
}
