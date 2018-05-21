package org.emoflon.ibex.tgg.editor.diagram.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.command.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.query.IdentifiedElementQuery;
import org.eclipse.sirius.business.api.session.DefaultLocalSessionCreationOperation;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionCreationOperation;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.common.tools.api.interpreter.EvaluationException;
import org.eclipse.sirius.common.tools.api.interpreter.IInterpreter;
import org.eclipse.sirius.common.tools.api.util.StringUtil;
import org.eclipse.sirius.diagram.business.internal.metamodel.spec.DSemanticDiagramSpec;
import org.eclipse.sirius.diagram.sequence.business.internal.metamodel.SequenceDDiagramSpec;
import org.eclipse.sirius.tools.api.command.semantic.AddSemanticResourceCommand;
import org.eclipse.sirius.tools.api.interpreter.InterpreterUtil;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.ui.business.api.viewpoint.ViewpointSelectionCallback;
import org.eclipse.sirius.viewpoint.DAnalysis;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DRepresentationDescriptor;
import org.eclipse.sirius.viewpoint.DView;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGBuilder;
import org.moflon.core.ui.AbstractCommandHandler;
import org.moflon.tgg.mosl.tgg.ComplementRule;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

public class OpenDiagramHandler extends AbstractCommandHandler {

	private final String REPRESENTATIONS_FILE_NAME = "representations.aird";
	private Viewpoint tggEditor = null;
	private Session session = null;
	private URI ruleURI = null;
	private DRepresentation representation = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// reset attributes
		session = null;
		ruleURI = null;
		representation = null;
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		IEditorInput input = activePage.getActiveEditor().getEditorInput();
		IFile rule = ((IFileEditorInput) input).getFile();
		IProject project = rule.getProject();
		IFile airdFile = project.getFile(REPRESENTATIONS_FILE_NAME);
		URI sessionModelURI = URI
				.createPlatformResourceURI(project.getFullPath().append(REPRESENTATIONS_FILE_NAME).toString(), true);
		ruleURI = URI.createPlatformResourceURI(rule.getFullPath().toString(), true);

		Collection<URI> ruleURIs = Collections.emptyList();
		try {
			ruleURIs = getRuleURIs(project.getFolder(IbexTGGBuilder.SRC_FOLDER));
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if (airdFile.exists() && SessionManager.INSTANCE.getSessions().size() > 0) {
			System.out.println("File existente");
			session = SessionManager.INSTANCE.getSession(sessionModelURI, new NullProgressMonitor());
		}

		if (session == null) {
			System.out.println("Nueva session");
			session = createNewSession(sessionModelURI);
			if (session == null) {
				throw new ExecutionException("It was not possible to create a new sirius session");
			}
			addRuleRessources(ruleURIs);
		} else {
			addRule(ruleURI);
		}

		session.save(new NullProgressMonitor());
		// ViewpointSelection.openViewpointsSelectionDialog(session);
		// SessionHelper.openStartupRepresentations(session, null);

		Set<Viewpoint> viewpoints = org.eclipse.sirius.business.api.componentization.ViewpointRegistry.getInstance()
				.getViewpoints();

		for (Viewpoint vp : viewpoints) {
			if (vp.getName().equals("tggEditor")) {
				tggEditor = vp;
				break;
			}
		}

		if (tggEditor != null) {
			session.getTransactionalEditingDomain().getCommandStack()
					.execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

						@Override
						protected void doExecute() {
							new ViewpointSelectionCallback().selectViewpoint(tggEditor, session, true,
									new NullProgressMonitor());
						}
					});
			session.save(new NullProgressMonitor());

			Collection<Viewpoint> sessionViewpoints = session.getSelectedViewpoints(false);
			for (Viewpoint vp : sessionViewpoints) {
				if (vp.getName().equals(tggEditor.getName())) {
					tggEditor = vp;
					break;
				}
			}

			// get representation
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
				if (currentRep instanceof SequenceDDiagramSpec) {
					rootObject = ((SequenceDDiagramSpec) currentRep).getTarget();
				} else if (currentRep instanceof DSemanticDiagramSpec) {
					rootObject = ((DSemanticDiagramSpec) currentRep).getTarget();
				}

				Resource eResource = rootObject.eResource();
				if(eResource == null)
					continue;
				URI eUri = eResource.getURI();
				if (eUri.equals(ruleURI)) {
					representation = currentRep;
					break;
				}
			}

			if (representation == null) {
				// create new representation
				session.getTransactionalEditingDomain().getCommandStack()
						.execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

							@Override
							protected void doExecute() {
								if (ruleURI != null) {
									ResourceSet rs = session.getTransactionalEditingDomain().getResourceSet();
									Resource res = rs.getResource(ruleURI, true);
									EObject container = res.getContents().get(0);
									if (!(container instanceof TripleGraphGrammarFile)) {
										return;
									}

									List<Rule> rules = ((TripleGraphGrammarFile) container).getRules();
									List<ComplementRule> complementRules = ((TripleGraphGrammarFile) container)
											.getComplementRules();
									EObject selectedObject = null;
									RepresentationDescription repDescription = null;

									// TODO Change size of 3
									if (tggEditor == null || tggEditor.getOwnedRepresentations().size() < 3) {
										return;
									}

									if (rules.size() == 1 && complementRules.size() == 0) {
										selectedObject = rules.get(0);
										// Rule representation
										repDescription = tggEditor.getOwnedRepresentations().get(1);
									} else if (rules.size() == 0 && complementRules.size() == 1) {
										selectedObject = complementRules.get(0);
										// Complement Rule representation
										repDescription = tggEditor.getOwnedRepresentations().get(2);
									} else {
										return;
									}

									IInterpreter interpreter = InterpreterUtil.getInterpreter(selectedObject);
									String name = new IdentifiedElementQuery(repDescription).getLabel();
									if (!StringUtil.isEmpty(repDescription.getTitleExpression())) {
										try {
											name = interpreter.evaluateString(selectedObject,
													repDescription.getTitleExpression());
										} catch (final EvaluationException e) {
											// TODO exception logger
											e.printStackTrace();
										}
									}

									representation = DialectManager.INSTANCE.createRepresentation(name, selectedObject,
											repDescription, session, new NullProgressMonitor());
								}

							}
						});
				session.save(new NullProgressMonitor());
			}

			if (representation != null) {
				DialectUIManager.INSTANCE.openEditor(session, representation, new NullProgressMonitor());
			}
		}

		return null;
	}

	private Collection<URI> getRuleURIs(IFolder root) throws CoreException {
		Collection<URI> URIs = new ArrayList<URI>();
		for (IResource iResource : root.members()) {
			if (iResource instanceof IFile) {
				if (iResource.getName().endsWith(IbexTGGBuilder.TGG_FILE_EXTENSION)) {
					URIs.add(URI.createPlatformResourceURI(iResource.getFullPath().toString(), true));
				}
			} else if (iResource instanceof IFolder) {
				URIs.addAll(getRuleURIs((IFolder) iResource));
			}
		}
		return URIs;
	}

	private void addRule(URI ruleURI) {
		Command addSemanticResourceCmd = new AddSemanticResourceCommand(session, ruleURI, new NullProgressMonitor());
		session.getTransactionalEditingDomain().getCommandStack().execute(addSemanticResourceCmd);
	}

	private void addRuleRessources(Collection<URI> ruleURIs) {
		for (URI uri : ruleURIs) {
			addRule(uri);
		}
	}

	private Session createNewSession(URI sessionModelURI) {
		SessionCreationOperation sessionCreationOperation = new DefaultLocalSessionCreationOperation(sessionModelURI,
				new NullProgressMonitor());
		try {
			sessionCreationOperation.execute();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return sessionCreationOperation.getCreatedSession();
	}

}
