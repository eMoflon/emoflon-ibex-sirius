package org.emoflon.ibex.tgg.editor.diagram.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.session.DefaultLocalSessionCreationOperation;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionCreationOperation;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.tools.api.command.semantic.AddSemanticResourceCommand;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.ui.business.api.viewpoint.ViewpointSelectionCallback;
import org.eclipse.sirius.viewpoint.DAnalysis;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DRepresentationDescriptor;
import org.eclipse.sirius.viewpoint.DView;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.emoflon.ibex.tgg.editor.builder.TGGBuildUtil;
import org.emoflon.ibex.tgg.editor.tgg.NamedElements;
import org.emoflon.ibex.tgg.editor.tgg.Rule;
import org.emoflon.ibex.tgg.editor.tgg.Schema;
import org.emoflon.ibex.tgg.editor.tgg.TripleGraphGrammarFile;

public class TGGGraphicalEditorLauncher implements IEditorLauncher {

	private final String REPRESENTATIONS_FILE_NAME = "representations.aird";
	private final DiagramInitializer diagramInitializer = new DiagramInitializer();
	private DRepresentation representation = null;
	private NamedElements selectedElement = null;
	private TripleGraphGrammarFile schemaFile = null;
	private RepresentationDescription repDescription = null;
	private Viewpoint tggEditor = null;

	@Override
	public void open(IPath filePath) {
		Display display = Display.getCurrent();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(display.getActiveShell());
		try {
			dialog.run(true, true, (monitor) -> {
				SubMonitor progressMonitor = SubMonitor.convert(monitor, "Opening TGG Graphical Editor...", 100);
				launchEditor(filePath, display, progressMonitor);
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Operation canceled
		}
	}

	private void launchEditor(IPath filePath, Display display, SubMonitor progressMonitor) {
		// reset attributes
		representation = null;
		selectedElement = null;
		repDescription = null;
		schemaFile = null;
		tggEditor = null;

		Session session = null;
		URI ruleURI = null;
		IProject project = null;

		progressMonitor.subTask("Loading representations file");
		IFile tggFile = FileBuffers.getWorkspaceFileAtLocation(filePath);
		if (tggFile == null || !tggFile.exists()) {
			progressMonitor.setCanceled(true);
			return;
		}
		project = tggFile.getProject();
		IFile airdFile = project.getFile(REPRESENTATIONS_FILE_NAME);
		URI sessionModelURI = URI
				.createPlatformResourceURI(project.getFullPath().append(REPRESENTATIONS_FILE_NAME).toString(), true);
		progressMonitor.worked(5);

		progressMonitor.subTask("Loading rules in project");
		ruleURI = URI.createPlatformResourceURI(tggFile.getFullPath().toString(), true);
		Collection<URI> ruleURIs = Collections.emptyList();
		try {
			ruleURIs = getRuleURIs(project.getFolder(TGGBuildUtil.SRC_FOLDER));
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		progressMonitor.worked(5);

		progressMonitor.subTask("Loading editor session");
		// Representations file exist already
		if (airdFile.exists() && SessionManager.INSTANCE.getSessions().size() > 0) {
			// Try to reopen a session
			session = SessionManager.INSTANCE.getSession(sessionModelURI, progressMonitor.split(40));
		}
		// A new session has to be created
		if (session == null) {
			progressMonitor.setWorkRemaining(90);
			session = createNewSession(sessionModelURI, progressMonitor.split(40));
			if (session == null) {
				progressMonitor.setCanceled(true);
				return;
			}
			progressMonitor.subTask("Adding rules of this project to the editor's session");
			addRuleRessources(ruleURIs, session, progressMonitor.split(10));
		} else {
			progressMonitor.subTask("Adding rule's resources to the editor's session");
			addRule(ruleURI, session, progressMonitor.split(10));
		}

		session.save(progressMonitor.split(1));
		launchEditor(null, ruleURI, session, project, display, progressMonitor);
	}

	public void launchEditor(NamedElements elementToShow, URI ruleURI, Session session, IProject project,
			Display display, SubMonitor progressMonitor) {
		tggEditor = getTGGEditorFromSession(session);
		if (tggEditor == null) {
			tggEditor = getTGGEditorViewpointFromRegistry(session, progressMonitor);
			setSessionViewpoint(session, progressMonitor);
		}
		selectedElement = elementToShow;
		if (tggEditor != null) {
			if (selectedElement == null && ruleURI != null) {
				ResourceSet rs = session.getTransactionalEditingDomain().getResourceSet();
				Resource res = rs.getResource(ruleURI, true);
				EObject container = res.getContents().get(0);
				if (!(container instanceof TripleGraphGrammarFile)) {
					progressMonitor.setCanceled(true);
					return;
				}
				TripleGraphGrammarFile castedTGGFile = (TripleGraphGrammarFile) container;
				List<Rule> rules = castedTGGFile.getRules();
				setLauncherParams(rules, castedTGGFile, display, progressMonitor);
			}

			repDescription = getRepresentationDescription(selectedElement, session, progressMonitor);
			representation = findExistingRepresentation(selectedElement, ruleURI, session, progressMonitor);

			if (representation == null) {
				progressMonitor.subTask("Creating new representation for the rule");
				final EObject semancticElement = selectedElement != null ? selectedElement : schemaFile;
				// create new representation
				session.getTransactionalEditingDomain().getCommandStack()
						.execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

							@Override
							protected void doExecute() {
								if (semancticElement != null && repDescription != null) {
									String name = diagramInitializer.initDiagram(semancticElement, project, null)
											+ " - " + repDescription.getLabel();
									representation = DialectManager.INSTANCE.createRepresentation(name,
											semancticElement, repDescription, session, progressMonitor.split(10));
								}

							}
						});
				session.save(progressMonitor.split(1));
			}

			progressMonitor.setWorkRemaining(10);
			openEditor(representation, session, progressMonitor);
		} else {
			progressMonitor.setCanceled(true);
			return;
		}
		SubMonitor.done(progressMonitor);
	}

	public RepresentationDescription getRepresentationDescription(NamedElements elementToShow, Session session, SubMonitor progressMonitor) {
		if (tggEditor == null) {
			tggEditor = getTGGEditorViewpointFromRegistry(session, progressMonitor);
			setSessionViewpoint(session, progressMonitor);
		}

		if (elementToShow instanceof Rule) {
			// Rule representation
			repDescription = tggEditor.getOwnedRepresentations().get(1);
		} else if (elementToShow == null && schemaFile != null) {
			// TGG File representation
			repDescription = tggEditor.getOwnedRepresentations().get(0);
		} else {
			progressMonitor.setCanceled(true);
			return null;
		}

		return repDescription;
	}

	public DRepresentation findExistingRepresentation(NamedElements element, URI ruleURI, Session session,
			SubMonitor progressMonitor) {
		// get representation (if there is already one)
		DAnalysis root = (DAnalysis) session.getSessionResource().getContents().get(0);
		List<DView> views = root.getOwnedViews();

		List<DRepresentation> representations = new BasicEList<DRepresentation>();

		for (DView view : views) {
			List<DRepresentationDescriptor> descriptions = view.getOwnedRepresentationDescriptors();
			for (DRepresentationDescriptor description : descriptions) {
				representations.add(description.getRepresentation());
			}
		}

		EObject rootObject = null;
		for (DRepresentation currentRep : representations) {
			if (currentRep instanceof DSemanticDiagram) {
				rootObject = ((DSemanticDiagram) currentRep).getTarget();
			}
			if (rootObject == null) {
				continue;
			}
			Resource eResource = rootObject.eResource();
			if (eResource == null)
				continue;
			URI eUri = eResource.getURI();
			if (ruleURI != null && eUri.equals(ruleURI)) {
				if (element != null && rootObject instanceof NamedElements
						&& ((NamedElements) rootObject).getName().equals(element.getName())) {
					representation = currentRep;
					final NamedElements rule = (NamedElements) rootObject;
					String ruleName = ((NamedElements) rule).getName();
					updateDiagramName(ruleName, session);
					break;
				} else if (schemaFile != null && rootObject instanceof TripleGraphGrammarFile) {
					Schema schema = ((TripleGraphGrammarFile) rootObject).getSchema();
					if (schema.getName() != null && schema.getName().equals(schemaFile.getSchema().getName())) {
						representation = currentRep;
						updateDiagramName(schema.getName(), session);
					}
				}
			}
		}
		progressMonitor.worked(5);

		return representation;
	}

	public void openEditor(DRepresentation representation, Session session, SubMonitor progressMonitor) {
		if (representation != null) {
			progressMonitor.subTask("Opening graphical editor");
			DialectUIManager.INSTANCE.openEditor(session, representation, progressMonitor.split(10));
		} else {
			progressMonitor.setCanceled(true);
			return;
		}
	}

	private Collection<URI> getRuleURIs(IFolder root) throws CoreException {
		Collection<URI> URIs = new ArrayList<URI>();
		for (IResource iResource : root.members()) {
			if (iResource instanceof IFile) {
				if (iResource.getName().endsWith(TGGBuildUtil.TGG_FILE_EXTENSION)) {
					URIs.add(URI.createPlatformResourceURI(iResource.getFullPath().toString(), true));
				}
			} else if (iResource instanceof IFolder) {
				URIs.addAll(getRuleURIs((IFolder) iResource));
			}
		}
		return URIs;
	}

	private void addRule(URI ruleURI, Session session, IProgressMonitor monitor) {
		Command addSemanticResourceCmd = new AddSemanticResourceCommand(session, ruleURI, monitor);
		session.getTransactionalEditingDomain().getCommandStack().execute(addSemanticResourceCmd);
	}

	private void addRuleRessources(Collection<URI> ruleURIs, Session session, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, ruleURIs.size());
		for (URI uri : ruleURIs) {
			addRule(uri, session, progress.split(1));
		}
	}

	private Session createNewSession(URI sessionModelURI, IProgressMonitor monitor) {
		SessionCreationOperation sessionCreationOperation = new DefaultLocalSessionCreationOperation(sessionModelURI,
				monitor);
		try {
			sessionCreationOperation.execute();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return sessionCreationOperation.getCreatedSession();

	}

	private void updateDiagramName(final String name, Session session) {
		session.getTransactionalEditingDomain().getCommandStack()
				.execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

					@Override
					protected void doExecute() {
						// Update diagram Name
						repDescription.setName(name + " - " + repDescription.getLabel());
					}
				});
	}

	private NamedElements setLauncherParams(List<Rule> rules,
			TripleGraphGrammarFile castedTGGFile, Display display, SubMonitor progressMonitor) {
		if (tggEditor == null || tggEditor.getOwnedRepresentations().size() < 3 || rules == null) {
			progressMonitor.setCanceled(true);
			return null;
		}
		progressMonitor.worked(1);

		progressMonitor.subTask("Getting rules representation");
		if (rules.size() == 1) {
			selectedElement = rules.get(0);
			// Rule representation
			repDescription = tggEditor.getOwnedRepresentations().get(1);
		} else if (rules.size() > 1) {
			display.syncExec(new Runnable() {

				@Override
				public void run() {
					ElementListSelectionDialog dlg = new ElementListSelectionDialog(display.getActiveShell(),
							new NamedElementLabelProvider());
					List<NamedElements> elements = new ArrayList<NamedElements>();
					elements.addAll(rules);
					dlg.setTitle("Select one Rule to Open with the Graphical Editor");
					dlg.setMessage(
							"More than one rule were found in this file. Please select one rule to open with the graphical editor");
					dlg.setElements(elements.toArray());
					dlg.setMultipleSelection(false);
					if (dlg.open() == Window.OK) {
						selectedElement = (NamedElements) dlg.getResult()[0];
					} else {
						progressMonitor.setCanceled(true);
						return;
					}
				}
			});
		} else if (castedTGGFile.getSchema() != null) {
			// Schema file handling
			schemaFile = castedTGGFile;
		} else {
			progressMonitor.setCanceled(true);
			return null;
		}

		return selectedElement;
	}

	private Viewpoint getTGGEditorViewpointFromRegistry(Session session, SubMonitor progressMonitor) {
		progressMonitor.subTask("Getting TGGEditor viewpoint");
		Set<Viewpoint> viewpoints = org.eclipse.sirius.business.api.componentization.ViewpointRegistry.getInstance()
				.getViewpoints();
		for (Viewpoint vp : viewpoints) {
			if (vp.getName().equals("tggEditor")) {
				tggEditor = vp;
				break;
			}
		}
		progressMonitor.worked(5);
		return tggEditor;
	}

	public Viewpoint getTGGEditorFromSession(Session session) {
		Collection<Viewpoint> sessionViewpoints = session.getSelectedViewpoints(false);
		for (Viewpoint vp : sessionViewpoints) {
			if (vp.getName().equals("tggEditor")) {
				return vp;
			}
		}
		return null;
	}

	private void setSessionViewpoint(Session session, SubMonitor progressMonitor) {
		progressMonitor.subTask("Setting the TGGEditor viewpoint to this session");
		session.getTransactionalEditingDomain().getCommandStack()
				.execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

					@Override
					protected void doExecute() {
						new ViewpointSelectionCallback().selectViewpoint(tggEditor, session, true,
								progressMonitor.split(5));
					}
				});
		session.save(progressMonitor.split(1));
		Collection<Viewpoint> sessionViewpoints = session.getSelectedViewpoints(false);
		for (Viewpoint vp : sessionViewpoints) {
			if (vp.getName().equals(tggEditor.getName())) {
				tggEditor = vp;
				break;
			}
		}

		progressMonitor.worked(1);
	}

}
