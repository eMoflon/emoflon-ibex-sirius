package org.emoflon.ibex.tgg.editor.diagram.ui;

import java.io.IOException;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGBuilder;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.moflon.tgg.mosl.tgg.ComplementRule;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

public class DiagramInitializer {
	public String initDiagram(EObject context, IProject project) {

		// check if the initialization has already been done and returns a name for the diagram
		String diagramName = getDiagramName(context);
		if(diagramName != null)
			return diagramName;
		
		// Initialization: Find schema file and solve cross references
		IFile schemaFile = findSchemaFileInProject(project);
		if (schemaFile == null || (schemaFile != null && !schemaFile.exists())) {
			project = getActiveProject();
		}
		XtextResourceSet resourceSet = new XtextResourceSet();
		if (schemaFile != null && schemaFile.exists()) {
			XtextResource schemaResource = new XtextResource();
			try {
				schemaResource = loadSchema(resourceSet, schemaFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (schemaIsOfExpectedType(schemaResource)) {
				// Load
				try {
					visitAllFiles(resourceSet, project.getFolder(IbexTGGBuilder.SRC_FOLDER), this::loadRules);
				} catch (CoreException | IOException e) {
					e.printStackTrace();
				}
				EcoreUtil2.resolveLazyCrossReferences(schemaResource, () -> false);
				resourceSet.getResources().forEach(r -> EcoreUtil2.resolveLazyCrossReferences(r, () -> false));
				EcoreUtil.resolveAll(resourceSet);
			}
		}
		return project.getName();
	}

	private String getDiagramName(EObject element) {
		// Check if already initialized
		if (element instanceof TripleGraphGrammarFile) {
			// Check if schema already set
			if (((TripleGraphGrammarFile) element).getSchema() != null) {
				return ((TripleGraphGrammarFile) element).getSchema().getName();
			}
		} else if (element instanceof Rule) {
			// Check if schema already set
			if (((Rule) element).getSchema() != null) {
				return ((Rule) element).getName();
			}
		}

		else if (element instanceof ComplementRule) {
			if (((ComplementRule) element).getKernel() != null) {
				Rule rootRule = ((ComplementRule) element).getKernel();
				// Check if schema already set
				if (rootRule != null && rootRule.getSchema() != null) {
					// Schema already set
					return ((ComplementRule) element).getName();
				}
			}
		}
		
		return null;
	}

	private IProject getActiveProject() {
		IWorkbench iWorkbench = PlatformUI.getWorkbench();
		if (iWorkbench == null)
			return null;
		IWorkbenchWindow iWorkbenchWindow = iWorkbench.getActiveWorkbenchWindow();
		if (iWorkbenchWindow == null)
			return null;
		IWorkbenchPage iWorkbenchPage = iWorkbenchWindow.getActivePage();
		if (iWorkbenchPage == null)
			return null;
		IEditorPart iEditorPart = iWorkbenchPage.getActiveEditor();
		if (iEditorPart == null)
			return null;
		IEditorInput input = iEditorPart.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return null;
		IFile file = ((IFileEditorInput) input).getFile();
		if (file == null)
			return null;

		return file.getProject();
	}

	private IFile findSchemaFileInProject(IProject project) {
		if (project == null)
			return null;
		return project.getFile(IbexTGGNature.SCHEMA_FILE);
	}

	private <ACC> void visitAllFiles(ACC accumulator, IFolder root, BiConsumer<IFile, ACC> action)
			throws CoreException, IOException {
		for (IResource iResource : root.members()) {
			if (iResource instanceof IFile) {
				action.accept((IFile) iResource, accumulator);
			} else if (iResource instanceof IFolder) {
				visitAllFiles(accumulator, IFolder.class.cast(iResource), action);
			}
		}
	}

	private XtextResource loadSchema(XtextResourceSet resourceSet, IFile schemaFile) throws IOException {
		XtextResource schemaResource = (XtextResource) resourceSet
				.createResource(URI.createPlatformResourceURI(schemaFile.getFullPath().toString(), true));
		schemaResource.load(null);
		EcoreUtil.resolveAll(resourceSet);
		return schemaResource;
	}

	private void loadRules(IFile file, XtextResourceSet resourceSet) {
		if (file.getName().endsWith(IbexTGGBuilder.TGG_FILE_EXTENSION)) {
			resourceSet.getResource(URI.createPlatformResourceURI(file.getFullPath().toString(), true), true);
		}
	}

	private boolean schemaIsOfExpectedType(XtextResource schemaResource) {
		return schemaResource.getContents().size() == 1
				&& schemaResource.getContents().get(0) instanceof TripleGraphGrammarFile;
	}

}
