package org.emoflon.ibex.tgg.editor.diagram;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.requests.ArrangeRequest;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.query.EObjectQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DDiagramElementContainer;
import org.eclipse.sirius.diagram.DEdge;
import org.eclipse.sirius.diagram.DNode;
import org.eclipse.sirius.diagram.DNodeList;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
import org.eclipse.sirius.viewpoint.ViewpointPackage;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.emoflon.ibex.tgg.editor.diagram.wizards.BaseCorrPage;
import org.emoflon.ibex.tgg.editor.diagram.wizards.BaseNodePage;
import org.emoflon.ibex.tgg.editor.diagram.wizards.CorrWizard;
import org.emoflon.ibex.tgg.editor.diagram.wizards.CorrWizardState;
import org.emoflon.ibex.tgg.editor.diagram.ui.DiagramInitializer;
import org.emoflon.ibex.tgg.editor.diagram.ui.NamedElementLabelProvider;
import org.emoflon.ibex.tgg.editor.diagram.ui.TGGGraphicalEditorLauncher;
import org.emoflon.ibex.tgg.editor.diagram.wizards.NodeWizard;
import org.emoflon.ibex.tgg.editor.diagram.wizards.NodeWizardState;
import org.emoflon.ibex.tgg.ide.admin.IbexTGGNature;
import org.moflon.tgg.mosl.tgg.AttrCond;
import org.moflon.tgg.mosl.tgg.AttrCondDef;
import org.moflon.tgg.mosl.tgg.AttrCondDefLibrary;
import org.moflon.tgg.mosl.tgg.AttributeAssignment;
import org.moflon.tgg.mosl.tgg.AttributeConstraint;
import org.moflon.tgg.mosl.tgg.AttributeExpression;
import org.moflon.tgg.mosl.tgg.ComplementRule;
import org.moflon.tgg.mosl.tgg.ContextObjectVariablePattern;
import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.CorrVariablePattern;
import org.moflon.tgg.mosl.tgg.EnumExpression;
import org.moflon.tgg.mosl.tgg.Expression;
import org.moflon.tgg.mosl.tgg.LinkVariablePattern;
import org.moflon.tgg.mosl.tgg.LiteralExpression;
import org.moflon.tgg.mosl.tgg.LocalVariable;
import org.moflon.tgg.mosl.tgg.NamedElements;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;
import org.moflon.tgg.mosl.tgg.Operator;
import org.moflon.tgg.mosl.tgg.ParamValue;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.tgg.Schema;
import org.moflon.tgg.mosl.tgg.TggFactory;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;

// Class containing all Sirius services (i.e. callbacks) related with the graphical representation of elements of the TGG meta-model.
// This class also contains methods for creating and editing elements of the TGG meta-model.
public class DesignServices {

	// Cache map to store the global objects in the context of each rule
	// K: Rule name, V: Map of object name (String), and the object itself stored in
	// a set.
	// Different objects with the same name can be stored in the same set, if they
	// are also global context for the rule
	// (NamedElements)
	private static Map<String, GlobalContext> globalCache = new HashMap<String, GlobalContext>();
	protected final String DEFAULT_OPERATOR = "++";

	public Operator getDefaultOperator(EObject self) {
		Operator operator = TggFactory.eINSTANCE.createOperator();
		operator.setValue(DEFAULT_OPERATOR);
		return operator;
	}

	public String askStringFromUser(EObject self, String title, String message, String initialValue) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, initialValue, null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		} else {
			return initialValue;
		}
	}

	public EObject getCorrespondenceEdgePathElement(EObject context, EObject element, EObject source) {
		if (element == source) {
			return element;
		}
		return null;
	}

	public boolean addLinkEdge(ObjectVariablePattern sourceObject, ObjectVariablePattern targetObject, Operator op) {

		LinkVariablePattern link = TggFactory.eINSTANCE.createLinkVariablePattern();
		link.setTarget(targetObject);
		link.setOp(op);

		List<EReference> referenceList = sourceObject.getType().getEReferences();

		ElementListSelectionDialog dlg = new ElementListSelectionDialog(Display.getCurrent().getActiveShell(),
				new NamedElementLabelProvider());
		dlg.setTitle("New Link");
		dlg.setMessage("Type of the link relation");
		dlg.setElements(referenceList.toArray());
		dlg.setMultipleSelection(false);
		EReference selectedType = null;

		if (dlg.open() == Window.OK) {
			selectedType = (EReference) dlg.getResult()[0];
			link.setType(selectedType);
			sourceObject.getLinkVariablePatterns().add(link);
			return true;
		}

		return false;
	}

	public AttrCondDef selectAttrCondDef(EObject context) {
		List<AttrCondDef> attrCondDefList = null;
		TripleGraphGrammarFile tggFile = null;
		List<EObject> tggFileContents = context.eResource().getContents();
		if (tggFileContents.size() > 0 && tggFileContents.get(0) instanceof TripleGraphGrammarFile) {
			tggFile = (TripleGraphGrammarFile) tggFileContents.get(0);
		} else {
			return null;
		}

		attrCondDefList = tggFile.getLibrary() != null ? tggFile.getLibrary().getAttributeCondDefs()
				: loadAttrCondDefLibrary(context).getAttributeCondDefs();

		ElementListSelectionDialog dlg = new ElementListSelectionDialog(Display.getCurrent().getActiveShell(),
				new NamedElementLabelProvider());
		dlg.setTitle("New Attribute Condition");
		dlg.setMessage("Select a function definition for the new attribute condition");
		dlg.setElements(attrCondDefList.toArray());
		dlg.setMultipleSelection(false);
		AttrCondDef selectedDef = null;

		if (dlg.open() == Window.OK) {
			selectedDef = (AttrCondDef) dlg.getResult()[0];
		}

		return selectedDef;
	}

	public boolean addAttrCondition(EObject context, EObject decorator) {
		AttrCondDef selectedDef = selectAttrCondDef(context);
		if (selectedDef == null) {
			return false;
		}
		List<AttrCond> attrCondList = getAttrCondList(context);
		if (attrCondList == null) {
			return false;
		}
		AttrCond newCond = TggFactory.eINSTANCE.createAttrCond();
		newCond.setName(selectedDef);
		attrCondList.add(newCond);
		OpenXtextEmbeddedEditor embeddedEditor = new OpenXtextEmbeddedEditor();
		embeddedEditor.open(decorator, newCond);
		return true;
	}

	public boolean addCorrespondence(NamedElements tgg, DSemanticDiagram diagram) {
		Schema schema;
		List<CorrVariablePattern> corrList;
		List<ObjectVariablePattern> sourceObjects;
		List<ObjectVariablePattern> targetObjects;

		if (tgg instanceof Rule) {
			schema = ((Rule) tgg).getSchema();
			corrList = ((Rule) tgg).getCorrespondencePatterns();
			sourceObjects = ((Rule) tgg).getSourcePatterns();
			targetObjects = ((Rule) tgg).getTargetPatterns();
		} else if (tgg instanceof ComplementRule) {
			schema = ((ComplementRule) tgg).getKernel().getSchema();
			corrList = ((ComplementRule) tgg).getCorrespondencePatterns();
			sourceObjects = ((ComplementRule) tgg).getSourcePatterns();
			targetObjects = ((ComplementRule) tgg).getTargetPatterns();
		} else {
			return false;
		}

		List<CorrType> corrTypes = schema.getCorrespondenceTypes();
		CorrWizardState state = new CorrWizardState(corrTypes, sourceObjects, targetObjects);
		openCorrWizard(state);
		if (state.isDone()) {

			CorrType type = state.getSelectedType();
			if (!corrTypes.contains(type)) {
				// Add new correspondence type to the schema
				corrTypes.add(type);
				saveSession(tgg);
			}

			ObjectVariablePattern source = state.getSelectedSource();
			ObjectVariablePattern target = state.getSelectedTarget();
			String corrName = state.getCorrName();

			CorrVariablePattern correspondence = TggFactory.eINSTANCE.createCorrVariablePattern();
			correspondence.setType(type);
			correspondence.setSource(source);
			correspondence.setTarget(target);
			correspondence.setName(corrName);

			// Set the default operator DEFAULT_OPERATOR for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue(DEFAULT_OPERATOR);
			correspondence.setOp(op);

			// Add the new correspondence to the TGG rule
			corrList.add(correspondence);

		}
		return true;
	}

	public boolean addNode(NamedElements tgg, DSemanticDiagram diagram, boolean isSourceNode) {
		Schema schema;
		List<ObjectVariablePattern> sourceObjects;
		List<ObjectVariablePattern> targetObjects;
		if (tgg instanceof Rule) {
			schema = ((Rule) tgg).getSchema();
			sourceObjects = ((Rule) tgg).getSourcePatterns();
			targetObjects = ((Rule) tgg).getTargetPatterns();
		} else if (tgg instanceof ComplementRule) {
			schema = ((ComplementRule) tgg).getKernel().getSchema();
			sourceObjects = ((ComplementRule) tgg).getSourcePatterns();
			targetObjects = ((ComplementRule) tgg).getTargetPatterns();
		} else {
			return false;
		}

		Map<String, List<EClassifier>> classifiers;
		String wizardTitel = null;
		if (isSourceNode) {
			classifiers = getClassifiersInPackageList(schema.getSourceTypes());
			wizardTitel = "New Source Node";
		} else {
			classifiers = getClassifiersInPackageList(schema.getTargetTypes());
			wizardTitel = "New Target Node";
		}

		List<EClassifier> outputList = combineObjectClassifierLists(classifiers);

		NodeWizardState state = new NodeWizardState(outputList);
		openNodeWizard(state, wizardTitel);

		if (state.isDone()) {
			EClass type = (EClass) state.getSelectedType();
			String nodeName = state.getNodeName();

			ObjectVariablePattern node = TggFactory.eINSTANCE.createObjectVariablePattern();
			node.setName(nodeName);
			node.setType(type);

			// Set the default operator DEFAULT_OPERATOR for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue(DEFAULT_OPERATOR);
			node.setOp(op);

			// Add the new node to the TGG rule
			if (isSourceNode)
				sourceObjects.add(node);
			else
				targetObjects.add(node);
		}
		return true;
	}

	public boolean deleteCorrespondence(CorrVariablePattern corr, DSemanticDiagram diagram) {
		List<CorrVariablePattern> correspondenceList;
		Rule rootRule = null;
		NamedElements tgg = (NamedElements) diagram.getTarget();

		if (tgg == null)
			return false;

		if (tgg instanceof Rule) {
			rootRule = (Rule) tgg;
			correspondenceList = ((Rule) tgg).getCorrespondencePatterns();
		} else if (tgg instanceof ComplementRule) {
			rootRule = ((ComplementRule) tgg).getKernel();
			correspondenceList = ((ComplementRule) tgg).getCorrespondencePatterns();
		} else {
			return false;
		}

		Schema schema = null;
		if (rootRule == null)
			return false;
		schema = rootRule.getSchema();
		if (schema == null)
			return false;

		CorrType corrType = corr.getType();
		int numUses = new EObjectQuery(corrType).getInverseReferences("type").size();
		if (numUses < 2) {
			String message = "The correspondence type \"" + corrType.getName()
					+ "\" is now unused in this TGG project.\n\nDo you want to delete it?";
			boolean deleteType = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					"Unused correspondence type detected", message);
			if (deleteType)
				schema.getCorrespondenceTypes().remove(corrType);
		}

		correspondenceList.remove(corr);

		return true;
	}

	public boolean deleteNode(ObjectVariablePattern node, DSemanticDiagram diagram, boolean isSourceNode) {
		NamedElements tgg = (NamedElements) diagram.getTarget();
		if (tgg == null)
			return false;

		List<CorrVariablePattern> correspondenceList = null;
		List<ObjectVariablePattern> sourceObjects = null;
		List<ObjectVariablePattern> targetObjects = null;
		List<AttrCond> attrConditions = null;

		if (tgg instanceof Rule) {
			correspondenceList = new ArrayList<CorrVariablePattern>(((Rule) tgg).getCorrespondencePatterns());
			sourceObjects = ((Rule) tgg).getSourcePatterns();
			targetObjects = ((Rule) tgg).getTargetPatterns();
			attrConditions = ((Rule) tgg).getAttrConditions();
		} else if (tgg instanceof ComplementRule) {
			correspondenceList = new ArrayList<CorrVariablePattern>(((ComplementRule) tgg).getCorrespondencePatterns());
			sourceObjects = ((ComplementRule) tgg).getSourcePatterns();
			targetObjects = ((ComplementRule) tgg).getTargetPatterns();
			attrConditions = ((ComplementRule) tgg).getAttrConditions();
		}

		else {
			return false;
		}

		if (correspondenceList == null || sourceObjects == null || targetObjects == null) {
			return false;
		}

		// Check whether attribute conditions have to be deleted and open a confirmation
		// dialog if so
		List<AttrCond> attrConditionSelection = attrConditions.stream()
				.filter(c -> c.getValues().stream().anyMatch(p -> p instanceof AttributeExpression
						&& getObjectVariableName(((AttributeExpression) p).getObjectVar()).equals(node.getName())))
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder();
		sb.append("By executing this operation the following attribute conditions have to be deleted:\n\n");
		for (AttrCond attrCond : attrConditionSelection) {
			sb.append(getAttrCondLabel(attrCond));
			sb.append("\n");
		}
		sb.append("\n\nDo you want to continue?");

		boolean continueOperation = true;
		if (attrConditionSelection.size() > 0) {
			continueOperation = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					"Delete attribute condition?", sb.toString());
		}

		if (!continueOperation) {
			return false;
		}

		attrConditions.removeAll(attrConditionSelection);

		// Delete all correspondences that involve this node
		for (CorrVariablePattern corr : correspondenceList) {
			if (isSourceNode && corr.getSource() == node || !isSourceNode && corr.getTarget() == node) {
				deleteCorrespondence(corr, diagram);
			}
		}

		List<ObjectVariablePattern> nodeList = null;
		if (isSourceNode) {
			nodeList = sourceObjects;
		} else {
			nodeList = targetObjects;
		}

		// Delete all links that involve this node
		for (ObjectVariablePattern obj : nodeList) {
			List<LinkVariablePattern> links = new ArrayList<LinkVariablePattern>(obj.getLinkVariablePatterns());
			for (LinkVariablePattern link : links) {
				if (link.getTarget() == node) {
					obj.getLinkVariablePatterns().remove(link);
				}
			}
		}

		// Delete node from rule
		if (isSourceNode) {
			sourceObjects.remove(node);
		} else {
			targetObjects.remove(node);
		}

		return true;
	}

	public boolean reconnectLinkTarget(ObjectVariablePattern source, ObjectVariablePattern target,
			ObjectVariablePattern newTarget) {
		LinkVariablePattern link = findLinkBetweenObjectPatternsLocally(source, target);
		if (link != null) {
			if (addLinkEdge(source, newTarget, link.getOp())) {
				// Remove old link relation
				source.getLinkVariablePatterns().remove(link);
				return true;
			}
		}
		return false;
	}

	public boolean reconnectLinkSource(ObjectVariablePattern source, ObjectVariablePattern target,
			ObjectVariablePattern newSource) {
		LinkVariablePattern link = findLinkBetweenObjectPatternsLocally(source, target);
		if (link != null) {
			if (addLinkEdge(newSource, target, link.getOp())) {
				// Remove old link relation
				source.getLinkVariablePatterns().remove(link);
				return true;
			}
		}
		return false;
	}

	public List<Rule> reconnectRefineEdgeTarget(Rule source, Rule target, Rule newTarget) {
		List<Rule> superTypes = source.getSupertypes();
		superTypes.remove(target);
		if (newTarget != null) {
			superTypes.add(newTarget);
		}
		return superTypes;
	}

	public List<Rule> deleteRefineEdgeOfRule(Rule source, DEdge edgeView) {
		DDiagramElement t = (DDiagramElement) edgeView.getTargetNode();
		Rule targetObject = (Rule) t.getTarget();
		return reconnectRefineEdgeTarget(source, targetObject, null);
	}

	public String getLinkEdgeName(ObjectVariablePattern sourceObject, DEdge edgeView) {
		// Get the target object from the edge view
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);
		// find the link variable pattern between object patterns sourceObject and
		// targetObject
		DSemanticDiagram diagram = (DSemanticDiagram) edgeView.getParentDiagram();
		if (!(diagram.getTarget() instanceof NamedElements)) {
			return null;
		}
		NamedElements rule = (NamedElements) diagram.getTarget();
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject, rule);
		if (link != null) {
			String op = "";
			if (link.getOp() != null) {
				op = link.getOp().getValue();
			}
			String label = op + " " + link.getType().getName();
			return label;
		}

		return null;
	}

	public boolean toggleLinkOperator(ObjectVariablePattern sourceObject, DEdge edgeView) {
		// Get the target object from the edge view
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);

		// find the link variable pattern between object patterns sourceObject and
		// targetObject
		LinkVariablePattern link = findLinkBetweenObjectPatternsLocally(sourceObject, targetObject);
		if (link != null) {
			// Toggle link operator
			if (link.getOp() == null) {
				Operator op = getDefaultOperator(null);
				link.setOp(op);
				return true;
			} else {
				link.setOp(null);
			}
		}

		return false;
	}

	public Operator getLinkOperator(ObjectVariablePattern sourceObject, DEdge edgeView) {
		// Get the target object from the edge view
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);
		DSemanticDiagram diagram = (DSemanticDiagram) edgeView.getParentDiagram();
		if (!(diagram.getTarget() instanceof NamedElements)) {
			return null;
		}
		NamedElements rule = (NamedElements) diagram.getTarget();
		// find the link variable pattern between object patterns sourceObject and
		// targetObject
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject, rule);
		if (link != null) {
			return link.getOp();
		}

		return TggFactory.eINSTANCE.createOperator();
	}

	public Operator toggleCorrOperator(CorrVariablePattern corr) {
		// Toggle correspondence operator
		if (corr.getOp() == null) {
			return getDefaultOperator(null);
		} else {
			return null;
		}
	}

	public Operator toggleObjectOperator(ObjectVariablePattern object) {
		Operator op = object.getOp();

		if (op == null) {
			List<AttributeConstraint> attrConstraints = new ArrayList<AttributeConstraint>(
					object.getAttributeConstraints());
			for (AttributeConstraint constr : attrConstraints) {
				if (constr.getOp().equals("==")) {
					// transform the "==" constraint into an ":=" assignment
					AttributeAssignment asgn = TggFactory.eINSTANCE.createAttributeAssignment();
					asgn.setOp(":=");
					asgn.setAttribute(constr.getAttribute());
					asgn.setValueExp(constr.getValueExp());
					// delete attribute constraint
					object.getAttributeConstraints().remove(constr);
					// add attribute assignment
					object.getAttributeAssignments().add(asgn);
				}
			}
			return getDefaultOperator(null);
		} else {
			List<AttributeAssignment> attrAssignments = new ArrayList<AttributeAssignment>(
					object.getAttributeAssignments());
			for (AttributeAssignment asgn : attrAssignments) {
				if (asgn.getOp().equals(":=")) {
					// transform the ":=" assignment into a "==" constraint
					AttributeConstraint constr = TggFactory.eINSTANCE.createAttributeConstraint();
					constr.setOp("==");
					constr.setAttribute(asgn.getAttribute());
					constr.setValueExp(asgn.getValueExp());
					// delete attribute assignment
					object.getAttributeAssignments().remove(asgn);
					// add attribute constraint
					object.getAttributeConstraints().add(constr);
				}
			}
			return null;
		}
	}

	public List<LinkVariablePattern> deleteLinkEdge(ObjectVariablePattern sourceObject, DEdge edgeView) {
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);
		LinkVariablePattern link = findLinkBetweenObjectPatternsLocally(sourceObject, targetObject);
		sourceObject.getLinkVariablePatterns().remove(link);

		return sourceObject.getLinkVariablePatterns();
	}

	public String getAttrAssignmentLabel(EObject assgn) {
		StringBuilder sb = new StringBuilder();
		String attrName = null;
		String op = null;
		Expression expr = null;
		if (assgn instanceof AttributeAssignment) {
			attrName = ((AttributeAssignment) assgn).getAttribute().getName();
			op = ((AttributeAssignment) assgn).getOp();
			expr = ((AttributeAssignment) assgn).getValueExp();
		} else if (assgn instanceof AttributeConstraint) {
			attrName = ((AttributeConstraint) assgn).getAttribute().getName();
			op = ((AttributeConstraint) assgn).getOp();
			expr = ((AttributeConstraint) assgn).getValueExp();
		} else {
			return null;
		}
		sb.append(attrName);
		sb.append(" ");
		sb.append(op);
		sb.append(" ");

		if (expr instanceof LiteralExpression) {
			sb.append(((LiteralExpression) expr).getValue());
		} else if (expr instanceof AttributeExpression) {
			sb.append(getObjectVariableName(((AttributeExpression) expr).getObjectVar()));
			sb.append(".");
			sb.append(((AttributeExpression) expr).getAttribute().getName());
		} else if (expr instanceof EnumExpression) {
			sb.append(getEnumExprName(expr));
		}

		return sb.toString();
	}

	public List<ObjectVariablePattern> findLocalChildrenOfGlobalNode(ObjectVariablePattern parent,
			DSemanticDiagram diagram, boolean isSourceNode) {
		List<ObjectVariablePattern> childNodes = new ArrayList<ObjectVariablePattern>();
		NamedElements rule = (NamedElements) diagram.getTarget();
		GlobalContext globalContext = getGlobalContextOfRule(rule);
		if (globalContext == null) {
			return childNodes;
		}

		List<LinkVariablePattern> linkList = globalContext.getAllLinksFromObject(parent);

		List<ObjectVariablePattern> nodeList = null;
		if (isSourceNode) {
			if (rule instanceof Rule) {
				nodeList = ((Rule) rule).getSourcePatterns();
			} else if (rule instanceof ComplementRule) {
				nodeList = ((ComplementRule) rule).getSourcePatterns();
			} else {
				return Collections.emptyList();
			}
		} else {
			if (rule instanceof Rule) {
				nodeList = ((Rule) rule).getTargetPatterns();
			} else if (rule instanceof ComplementRule) {
				nodeList = ((ComplementRule) rule).getTargetPatterns();
			} else {
				return Collections.emptyList();
			}
		}
		for (ObjectVariablePattern node : nodeList) {
			for (LinkVariablePattern link : linkList) {
				if (node.getName().equals(link.getTarget().getName())) {
					childNodes.add(node);
					break;
				}
			}
		}

		return childNodes;
	}

	public List<ObjectVariablePattern> findGlobalChildrenOfGlobalNode(ObjectVariablePattern parent,
			DSemanticDiagram diagram) {
		final List<ObjectVariablePattern> nodeList = new ArrayList<ObjectVariablePattern>();
		List<ObjectVariablePattern> childNodes = new ArrayList<ObjectVariablePattern>();
		diagram.getContainers().forEach(c -> {
			if (!c.getActualMapping().getName().contains("global")) {
				return;
			}
			if (c.getTarget() instanceof ObjectVariablePattern) {
				nodeList.add((ObjectVariablePattern) c.getTarget());
			}
		});

		if (!(diagram.getTarget() instanceof NamedElements)) {
			return childNodes;
		}
		GlobalContext globalContext = getGlobalContextOfRule((NamedElements) diagram.getTarget());
		if (globalContext == null) {
			return childNodes;
		}
		List<LinkVariablePattern> linkList = globalContext.getAllLinksFromObject(parent);

		linkList.stream().forEach(l -> {
			ObjectVariablePattern childNode = findLinkTarget(l, nodeList);
			if (childNode != null) {
				childNodes.add(childNode);
			}
		});

		return childNodes;
	}

	public List<ObjectVariablePattern> findGlobalChildrenOfLocalNode(ObjectVariablePattern parent, NamedElements rule) {
		final Set<ObjectVariablePattern> childrenSet = new HashSet<ObjectVariablePattern>();
		final List<ObjectVariablePattern> globalNodeList = new ArrayList<ObjectVariablePattern>();

		GlobalContext globalContext = getGlobalContextOfRule(rule);

		if (globalContext == null || !globalContext.containsObjectName(parent.getName())) {
			return globalNodeList;
		}

		List<LinkVariablePattern> linkList = globalContext.getAllLinksFromObject(parent);
		linkList.stream().forEach(l -> childrenSet.add(l.getTarget()));

		childrenSet.stream().forEach(n -> {
			Set<NamedElements> globalChildNodeSet = globalContext.get(n.getName());
			if (globalChildNodeSet != null) {
				globalChildNodeSet.stream().forEach(g -> {
					if (!(g instanceof ObjectVariablePattern)) {
						return;
					}
					globalNodeList.add((ObjectVariablePattern) g);
				});
			}
		});

		return globalNodeList;
	}

	public List<NamedElements> getGlobalNodes(EObject context, String populateTask) {
		List<NamedElements> nodes = new ArrayList<NamedElements>();
		List<Rule> visitedRules = new ArrayList<Rule>();
		GlobalContext globalContext;
		if (context instanceof NamedElements) {
			globalContext = initGlobalContext((NamedElements) context);
		} else {
			return Collections.emptyList();
		}

		GlobalContext newGlobalContext = new GlobalContext();
		if (context instanceof ComplementRule) {
			populateHelper(((ComplementRule) context).getKernel(), newGlobalContext, visitedRules, populateTask);
		} else if (context instanceof Rule) {
			populateHelper(((Rule) context).getSupertypes(), newGlobalContext, visitedRules, populateTask);
		}

		globalContext.addContext(newGlobalContext);
		nodes.addAll(newGlobalContext.getAllFirstMatches());

		return nodes;
	}

	public List<NamedElements> getGlobalCorrespondences(EObject context) {
		GlobalContext globalContext;
		if (context instanceof NamedElements) {
			globalContext = initGlobalContext((NamedElements) context);
		} else {
			return Collections.emptyList();
		}

		List<NamedElements> globalCorr = new ArrayList<NamedElements>();
		GlobalContext corrGlobalContext = new GlobalContext();
		if (context instanceof ComplementRule) {
			corrGlobalContext = populateHelper(((ComplementRule) context).getKernel(), corrGlobalContext,
					new ArrayList<Rule>(), "correspondences");
		} else if (context instanceof Rule) {
			corrGlobalContext = populateHelper(((Rule) context).getSupertypes(), corrGlobalContext,
					new ArrayList<Rule>(), "correspondences");
		}

		globalContext.addContext(corrGlobalContext);
		globalCorr.addAll(corrGlobalContext.getAllFirstMatches());

		return globalCorr;
	}

	public ObjectVariablePattern findGlobalCorrPort(CorrVariablePattern correspondence, DSemanticDiagram diagram,
			boolean isSourceNode) {
		final List<ObjectVariablePattern> nodeList = new ArrayList<ObjectVariablePattern>();
		diagram.getContainers().forEach(c -> {
			if (!c.getActualMapping().getName().contains("global")) {
				return;
			}
			if (c.getTarget() instanceof ObjectVariablePattern) {
				nodeList.add((ObjectVariablePattern) c.getTarget());
			}
		});

		if (isSourceNode && diagram.getTarget() instanceof Rule) {
			nodeList.addAll(((Rule) diagram.getTarget()).getSourcePatterns());
		}

		else if (isSourceNode && diagram.getTarget() instanceof ComplementRule) {
			nodeList.addAll(((ComplementRule) diagram.getTarget()).getSourcePatterns());
		}

		else if (!isSourceNode && diagram.getTarget() instanceof Rule) {
			nodeList.addAll(((Rule) diagram.getTarget()).getTargetPatterns());
		}

		else if (!isSourceNode && diagram.getTarget() instanceof ComplementRule) {
			nodeList.addAll(((ComplementRule) diagram.getTarget()).getTargetPatterns());
		}

		for (ObjectVariablePattern node : nodeList) {
			if (isSourceNode && node.getName().equals(correspondence.getSource().getName())) {
				return node;
			} else if (!isSourceNode && node.getName().equals(correspondence.getTarget().getName())) {
				return node;
			}
		}

		return null;
	}

	public List<ObjectVariablePattern> findLocalChildrenOfLocalNode(ObjectVariablePattern parentNode) {
		List<ObjectVariablePattern> localChildren = new ArrayList<ObjectVariablePattern>();
		parentNode.getLinkVariablePatterns().stream().forEach(p -> localChildren.add(p.getTarget()));
		return localChildren;
	}

	private GlobalContext initGlobalContext(NamedElements rule) {
		GlobalContext globalContext;
		String ruleName = ((NamedElements) rule).getName();
		if (globalCache.containsKey(ruleName)) {
			globalContext = globalCache.get(ruleName);
		} else {
			globalContext = new GlobalContext();
			updateCache(ruleName, globalContext);
		}

		return globalContext;
	}

	private GlobalContext populateHelper(Rule contextRule, GlobalContext globalContext, List<Rule> visitedRules,
			String populateTask) {
		if (contextRule == null || visitedRules.contains(contextRule)) {
			return globalContext;
		}

		switch (populateTask) {
		case "sourceNodes":
			globalContext.addAll(contextRule.getSourcePatterns());
			break;
		case "targetNodes":
			globalContext.addAll(contextRule.getTargetPatterns());
			break;
		case "correspondences":
			globalContext.addAll(contextRule.getCorrespondencePatterns());
			break;
		}

		visitedRules.add(contextRule);

		if (contextRule.getSupertypes() != null) {
			globalContext = populateHelper(contextRule.getSupertypes(), globalContext, visitedRules, populateTask);
		}

		return globalContext;
	}

	private GlobalContext populateHelper(List<Rule> contextRules, GlobalContext globalContext, List<Rule> visitedRules,
			String populateTask) {

		if (contextRules != null) {
			for (Rule contextRule : contextRules) {
				globalContext = populateHelper(contextRule, globalContext, visitedRules, populateTask);
			}
		}

		return globalContext;
	}

	public ObjectVariablePattern getTargetObjectFromEdge(DEdge edgeView) {
		DDiagramElement t = (DDiagramElement) edgeView.getTargetNode();
		ObjectVariablePattern targetObject = (ObjectVariablePattern) t.getTarget();

		return targetObject;
	}

	private LinkVariablePattern findLinkBetweenObjectPatterns(ObjectVariablePattern x, ObjectVariablePattern y,
			NamedElements rule) {
		// Try to find the link variable pattern between object patterns x and y locally
		LinkVariablePattern link = findLinkBetweenObjectPatternsLocally(x, y);
		if (link != null) {
			return link;
		}

		// try to find link in global context if previous search was unsuccessful
		GlobalContext globalContext = getGlobalContextOfRule(rule);
		if (globalContext != null && globalContext.containsObjectName(x.getName())) {
			List<LinkVariablePattern> linkList = globalContext.getAllLinksFromObject(x);
			link = findLinkFromTarget(linkList, y);
			if (link != null) {
				return link;
			}
		}

		return null;
	}

	private LinkVariablePattern findLinkBetweenObjectPatternsLocally(ObjectVariablePattern x, ObjectVariablePattern y) {
		List<LinkVariablePattern> linkList = x.getLinkVariablePatterns();
		// Try to find the link variable pattern between object patterns x and y locally
		return findLinkFromTarget(linkList, y);
	}

	private LinkVariablePattern findLinkFromTarget(List<LinkVariablePattern> linkList, ObjectVariablePattern target) {
		for (LinkVariablePattern link : linkList) {
			if (link.getTarget().getName().equals(target.getName())) {

				return link;
			}
		}

		return null;
	}

	private ObjectVariablePattern findLinkTarget(LinkVariablePattern link, List<ObjectVariablePattern> targets) {
		for (ObjectVariablePattern target : targets) {
			if (link.getTarget().getName().equals(target.getName())) {

				return target;
			}
		}

		return null;
	}

	private Map<String, List<EClassifier>> getClassifiersInPackageList(List<EPackage> packages) {
		// K: Package name, V: List of the classifiers inside that package
		Map<String, List<EClassifier>> classifierNames = new HashMap<String, List<EClassifier>>();
		for (EPackage p : packages) {
			classifierNames.put(p.getName(), p.getEClassifiers());
		}

		return classifierNames;
	}

	private List<EClassifier> combineObjectClassifierLists(Map<String, List<EClassifier>> input) {
		Set<String> keys = input.keySet();
		List<EClassifier> outputList = new ArrayList<EClassifier>();
		for (String key : keys) {
			outputList.addAll(input.get(key));
		}

		return outputList;
	}

	private CorrWizardState openCorrWizard(CorrWizardState state) {
		WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), new CorrWizard(state));
		dialog.open();
		BaseCorrPage lastPage = (BaseCorrPage) dialog.getCurrentPage();
		return lastPage.getState();
	}

	private NodeWizardState openNodeWizard(NodeWizardState state, String titel) {
		WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), new NodeWizard(state, titel));
		dialog.open();
		BaseNodePage lastPage = (BaseNodePage) dialog.getCurrentPage();
		return lastPage.getState();
	}

	public String getAttrCondLabel(AttrCond attrCond) {
		AttrCondDef def = attrCond.getName();
		String attr = def.getName();
		attr += "(";
		int idx = 0;
		List<ParamValue> params = attrCond.getValues();
		for (ParamValue p : params) {
			if (p instanceof AttributeExpression) {
				AttributeExpression tmp = (AttributeExpression) p;
				EObject objVar = tmp.getObjectVar();
				attr = attr + getObjectVariableName(objVar) + "." + tmp.getAttribute().getName();
			} else if (p instanceof LiteralExpression) {
				LiteralExpression tmp = (LiteralExpression) p;
				attr = attr + tmp.getValue();
			} else if (p instanceof LocalVariable) {
				LocalVariable tmp = (LocalVariable) p;
				attr = attr + tmp.getName();
			} else if (p instanceof EnumExpression) {
				EnumExpression tmp = (EnumExpression) p;
				attr = attr + getEnumExprName(tmp);
			}

			if (idx < params.size() - 1) {
				attr = attr + ", ";
				idx++;
			}

		}

		attr += ")";

		return attr;
	}

	public ObjectVariablePattern makeGlobalNodeContextNode(DSemanticDecorator decorator, EObject rule) {
		if (decorator instanceof DNodeList) {
			if (((DNodeList) decorator).getActualMapping().getName().contains("global")) {
				ObjectVariablePattern obj = (ObjectVariablePattern) decorator.getTarget();
				Rule rootRule = (Rule) obj.eContainer();
				boolean isSourceNode = rootRule.getSourcePatterns().contains(obj);
				ObjectVariablePattern contextNode = EcoreUtil.copy(obj);
				// clear constraints and assignments because we don't want to override them
				contextNode.getAttributeAssignments().clear();
				contextNode.getAttributeConstraints().clear();
				List<LinkVariablePattern> links = contextNode.getLinkVariablePatterns();
				if (!(rule instanceof NamedElements)) {
					return null;
				}
				globalLinksToLocalLinks((DNodeList) decorator, contextNode, (NamedElements) rule);
				List<LinkVariablePattern> newLinks = new ArrayList<LinkVariablePattern>();
				List<ObjectVariablePattern> childNodes = findLocalChildrenOfGlobalNode(contextNode,
						(DSemanticDiagram) ((DNodeList) decorator).getParentDiagram(), isSourceNode);
				// Filter links that do not point to a local node and replace references to a
				// local node with the actual local node
				for (LinkVariablePattern link : links) {
					for (ObjectVariablePattern localNode : childNodes) {
						if (link.getTarget().getName().equals(localNode.getName())) {
							LinkVariablePattern newLink = EcoreUtil.copy(link);
							newLink.setTarget(localNode);
							// Global green links to a local node become black links when its owner becomes
							// a context node
							if (newLink.getOp() != null && newLink.getOp().getValue().equals(DEFAULT_OPERATOR)) {
								newLink.setOp(null);
							}
							newLinks.add(newLink);
							break;
						}
					}
				}
				// Replace links
				links.clear();
				links.addAll(newLinks);
				if (obj.getOp() != null && obj.getOp().getValue().equals(DEFAULT_OPERATOR)) {
					// Global green nodes become black when they are context nodes
					toggleObjectOperator(contextNode);
					contextNode.setOp(null);
				}

				// Add new context node to rule
				if (isSourceNode) {
					if (rule instanceof Rule) {
						((Rule) rule).getSourcePatterns().add(contextNode);
					} else if (rule instanceof ComplementRule) {
						((ComplementRule) rule).getSourcePatterns().add(contextNode);
					}
				} else {
					if (rule instanceof Rule) {
						((Rule) rule).getTargetPatterns().add(contextNode);
					} else if (rule instanceof ComplementRule) {
						((ComplementRule) rule).getTargetPatterns().add(contextNode);
					}
				}

				// Trigger arrange-all
				triggerArrangeAll();
				return contextNode;
			}
		} else if (decorator instanceof DNode) {
			if (((DNode) decorator).getActualMapping().getLabel().toLowerCase().contains("global")) {
				List<ObjectVariablePattern> localSourceObjects = new ArrayList<ObjectVariablePattern>();
				List<ObjectVariablePattern> localTargetObjects = new ArrayList<ObjectVariablePattern>();
				List<CorrVariablePattern> correspondences = null;
				if (rule instanceof Rule) {
					localSourceObjects.addAll(((Rule) rule).getSourcePatterns());
					localTargetObjects.addAll(((Rule) rule).getTargetPatterns());
					correspondences = ((Rule) rule).getCorrespondencePatterns();
				} else if (rule instanceof ComplementRule) {
					localSourceObjects.addAll(((ComplementRule) rule).getSourcePatterns());
					localTargetObjects.addAll(((ComplementRule) rule).getTargetPatterns());
					correspondences = ((ComplementRule) rule).getCorrespondencePatterns();
				}
				CorrVariablePattern contextCorrespondence = (CorrVariablePattern) EcoreUtil.copy(decorator.getTarget());
				ObjectVariablePattern globalSourceObj = contextCorrespondence.getSource();
				ObjectVariablePattern globalTargetObj = contextCorrespondence.getTarget();
				ObjectVariablePattern localSourceObj = null;
				ObjectVariablePattern localTargetObj = null;
				for (ObjectVariablePattern obj : localSourceObjects) {
					if (nodesWithSameName(globalSourceObj, obj)) {
						localSourceObj = obj;
						break;
					}
				}
				for (ObjectVariablePattern obj : localTargetObjects) {
					if (nodesWithSameName(globalTargetObj, obj)) {
						localTargetObj = obj;
						break;
					}
				}

				if (localSourceObj == null) {
					// Convert the correspondence's source node to a context node
					DNodeList globalNodeDecorator = findNodeListDecorator(((DNode) decorator).getParentDiagram(),
							globalSourceObj.getName());
					localSourceObj = makeGlobalNodeContextNode(globalNodeDecorator, rule);
				}
				if (localTargetObj == null) {
					// Convert the correspondence's target node to a context node
					DNodeList globalNodeDecorator = findNodeListDecorator(((DNode) decorator).getParentDiagram(),
							globalTargetObj.getName());
					localTargetObj = makeGlobalNodeContextNode(globalNodeDecorator, rule);
				}

				// Update source and target references
				contextCorrespondence.setSource(localSourceObj);
				contextCorrespondence.setTarget(localTargetObj);

				// Global green correspondences become black when they are context
				// correspondences
				if (contextCorrespondence.getOp() != null
						&& contextCorrespondence.getOp().getValue().equals(DEFAULT_OPERATOR)) {
					contextCorrespondence.setOp(null);
				}

				// Add new context correspondence to rule
				correspondences.add(contextCorrespondence);
			}

			// Trigger arrange-all
			triggerArrangeAll();
		}
		return null;
	}

	// Make the embedded editor only available for local elements of type Node or
	// NodeList
	public boolean provideXtextEmbeddedEditor(DSemanticDecorator decorator) {
		if (decorator instanceof DNodeList || decorator instanceof DNode) {
			return !isDiagramElementGlobal((DDiagramElement) decorator);
		}
		return false;
	}

	// Make the navigation tool only available for global elements of type Node or
	// NodeList
	public boolean provideNavigationTool(DSemanticDecorator decorator) {
		if (decorator instanceof DNodeList || decorator instanceof DNode) {
			return isDiagramElementGlobal((DDiagramElement) decorator);
		}
		return false;
	}

	public EObject findParentRuleOfNode(DDiagramElement diagramElement) {
		if (!(diagramElement.getTarget() instanceof NamedElements)) {
			return null;
		}
		NamedElements node = (NamedElements) diagramElement.getTarget();
		DSemanticDiagram diagram = (DSemanticDiagram) diagramElement.getParentDiagram();
		if (!(diagram.getTarget() instanceof NamedElements)) {
			return null;
		}
		NamedElements rule = (NamedElements) diagram.getTarget();
		GlobalContext globalContext = getGlobalContextOfRule(rule);
		if (globalContext == null) {
			return rule;
		}
		Set<NamedElements> globalNodeSet = globalContext.get(node.getName());
		if (globalNodeSet == null || globalNodeSet.size() < 1) {
			return rule;
		}

		EObject parentRule = rule;
		NamedElements[] globalNodes = new NamedElements[globalNodeSet.size()];
		globalNodeSet.toArray(globalNodes);
		if (globalNodeSet.size() == 1) {
			parentRule = globalNodes[0].eContainer();
		} else {
			// More than one parent rule containing the node
			// Let the user select the rule of interest
			List<NamedElements> ruleList = new ArrayList<NamedElements>();
			for (NamedElements n : globalNodes) {
				EObject nRule = n.eContainer();
				if (nRule instanceof NamedElements) {
					ruleList.add((NamedElements) nRule);
				}
			}

			ElementListSelectionDialog dlg = new ElementListSelectionDialog(Display.getCurrent().getActiveShell(),
					new NamedElementLabelProvider());
			dlg.setTitle("Select the rule you want to navigate to");
			dlg.setMessage(
					"The selected global element is defined in more than one parent rule. Please select the rule you want to navigate to.");
			dlg.setElements(ruleList.toArray());
			dlg.setMultipleSelection(false);

			if (dlg.open() == Window.OK) {
				parentRule = (EObject) dlg.getResult()[0];
			}

		}
		return parentRule;
	}

	public List<TripleGraphGrammarFile> getTGGfilesInProject(TripleGraphGrammarFile schemaFile) {
		List<TripleGraphGrammarFile> tggFiles = new ArrayList<TripleGraphGrammarFile>();
		Session session = SessionManager.INSTANCE.getSession(schemaFile);
		if (session == null) {
			return tggFiles;
		}
		Collection<Resource> semanticResources = session.getSemanticResources();
		for (Resource r : semanticResources) {
			EObject container = r.getContents().get(0);
			if (container instanceof TripleGraphGrammarFile) {
				TripleGraphGrammarFile tggFile = (TripleGraphGrammarFile) container;
				if ((tggFile.getRules() != null && tggFile.getRules().size() > 0)
						|| (tggFile.getComplementRules() != null && tggFile.getComplementRules().size() > 0)) {
					// We only want to display tgg files containing rules or complement rules
					tggFiles.add(tggFile);
				} else {
					String uri = r.getURI().toPlatformString(true);
					if (!uri.contains(IbexTGGNature.SCHEMA_FILE)
							&& !uri.contains(DiagramInitializer.ATTR_COND_DEF_LIBRARY_PATH)) {
						tggFiles.add(tggFile);
					}

				}
			}
		}
		return tggFiles;
	}

	public boolean moveRuleFromFile(NamedElements rule, TripleGraphGrammarFile srcFile,
			TripleGraphGrammarFile dstFile) {
		if (rule instanceof Rule) {
			srcFile.getRules().remove(rule);
			dstFile.getRules().add((Rule) rule);
			saveSession(rule);
		} else if (rule instanceof ComplementRule) {
			srcFile.getComplementRules().remove(rule);
			dstFile.getComplementRules().add((ComplementRule) rule);
			saveSession(rule);
		}

		return true;
	}

	public boolean saveSession(EObject context) {
		Session session = SessionManager.INSTANCE.getSession(context);
		session.save(new NullProgressMonitor());
		return true;
	}

	public boolean openDiagram(NamedElements rule) throws InvocationTargetException, InterruptedException {
		TGGGraphicalEditorLauncher diagramLauncher = new TGGGraphicalEditorLauncher();
		URI ruleURI = rule.eResource().getURI();
		Session session = SessionManager.INSTANCE.getSession(rule);
		if (session == null || ruleURI == null) {
			return false;
		}
		diagramLauncher.getTGGEditorFromSession(session);
		SubMonitor progressMonitor = SubMonitor.convert(new NullProgressMonitor(),
				"Opening diagram of " + rule.getName() + " ...", 100);
		RepresentationDescription repDescription = diagramLauncher.getRepresentationDescription(rule, session,
				progressMonitor);
		if (repDescription == null) {
			return false;
		}
		DiagramInitializer diagramInitializer = new DiagramInitializer();
		IProject project = DiagramInitializer.getActiveProject();
		String name = diagramInitializer.initDiagram(rule, project, null) + " - " + repDescription.getLabel();
		DRepresentation representation = diagramLauncher.findExistingRepresentation(rule, ruleURI, session,
				progressMonitor);
		if (representation == null) {
			// create new representation
			representation = DialectManager.INSTANCE.createRepresentation(name, rule, repDescription, session,
					progressMonitor);
			session.save(progressMonitor);
		}
		diagramLauncher.openEditor(representation, session, progressMonitor);

		return true;
	}

	public boolean updateDiagramName(NamedElements rule) {
		EObjectQuery eObjectQuery = new EObjectQuery(rule);
		Collection<EObject> referencers = eObjectQuery
				.getInverseReferences(ViewpointPackage.Literals.DSEMANTIC_DECORATOR__TARGET);
		for (EObject referencer : referencers) {
			if (referencer instanceof DSemanticDiagram) {
				// found the rule diagram
				String repName = "";
				if (rule instanceof Rule) {
					repName = "TGG Rule";
				} else if (rule instanceof ComplementRule) {
					repName = "Complement Rule";
				}
				DSemanticDiagram diagram = (DSemanticDiagram) referencer;
				diagram.setName(rule.getName() + " - " + repName);
				return true;
			}
		}
		return false;
	}

	private DNodeList findNodeListDecorator(DDiagram diagram, String containerSemanticElementName) {
		List<DDiagramElementContainer> containers = diagram.getContainers();
		for (DDiagramElementContainer container : containers) {
			if (container.getTarget() instanceof ObjectVariablePattern
					&& ((ObjectVariablePattern) container.getTarget()).getName().equals(containerSemanticElementName)) {
				return (DNodeList) container;
			}
		}

		return null;
	}

	private boolean nodesWithSameName(ObjectVariablePattern obj1, ObjectVariablePattern obj2) {
		return obj1.getName().equals(obj2.getName());
	}

	private boolean isDiagramElementGlobal(DDiagramElement diagramElement) {
		String mappingName = ((DDiagramElement) diagramElement).getDiagramElementMapping().getName().toLowerCase();
		if (mappingName.contains("global")) {
			return true;
		}
		return false;
	}

	private String getObjectVariableName(EObject objVar) {
		String objVarName = "";
		if (objVar instanceof ObjectVariablePattern) {
			objVarName = ((ObjectVariablePattern) objVar).getName();
		} else if (objVar instanceof ContextObjectVariablePattern) {
			objVarName = ((ContextObjectVariablePattern) objVar).getName();
		}

		return objVarName;
	}

	private String getEnumExprName(Expression expr) {
		EnumExpression enumExpr = (EnumExpression) expr;
		StringBuilder sb = new StringBuilder();
		sb.append("enum::");
		sb.append(enumExpr.getEenum().getEPackage().getName());
		sb.append(".");
		sb.append(enumExpr.getEenum().getName());
		sb.append("::");
		sb.append(enumExpr.getLiteral().getName());
		return sb.toString();
	}

	private AttrCondDefLibrary loadAttrCondDefLibrary(EObject context) {
		AttrCondDefLibrary library = null;
		try {
			Session s = SessionManager.INSTANCE.getSession(context);
			library = DiagramInitializer.loadAttrCondDefLibrary(s.getTransactionalEditingDomain().getResourceSet());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return library;
	}

	private List<AttrCond> getAttrCondList(EObject obj) {
		List<AttrCond> attrCondList = null;
		if (obj instanceof Rule) {
			attrCondList = ((Rule) obj).getAttrConditions();
		} else if (obj instanceof ComplementRule) {
			attrCondList = ((ComplementRule) obj).getAttrConditions();
		} else if (obj instanceof AttrCond) {
			attrCondList = getAttrCondList(obj.eContainer());
		}

		return attrCondList;
	}

	private IEditorPart getActiveEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		return page.getActiveEditor();
	}

	private synchronized void updateCache(String ruleName, GlobalContext globalContext) {
		globalCache.put(ruleName, globalContext);
	}

	private void globalLinksToLocalLinks(DNodeList decorator, ObjectVariablePattern target, NamedElements rule) {
		List<DEdge> incomingEdges = decorator.getIncomingEdges();
		GlobalContext globalContext = getGlobalContextOfRule(rule);
		if (globalContext == null) {
			return;
		}
		for (DEdge inEdge : incomingEdges) {
			if (!inEdge.getMapping().getName().equals("localNode2GlobalNodeEdge")) {
				continue;
			}
			if (inEdge.getSourceNode() instanceof DNodeList) {
				DNodeList parentNodeDecorator = (DNodeList) inEdge.getSourceNode();
				ObjectVariablePattern parentNode = (ObjectVariablePattern) parentNodeDecorator.getTarget();
				List<LinkVariablePattern> localLinkList = parentNode.getLinkVariablePatterns();
				Set<NamedElements> globalParentNodeSet = globalContext.get(parentNode.getName());
				if (globalParentNodeSet == null) {
					continue;
				}
				for (NamedElements globalParentNode : globalParentNodeSet) {
					if (!(globalParentNode instanceof ObjectVariablePattern)) {
						continue;
					}
					ObjectVariablePattern castedGlobalParentNode = (ObjectVariablePattern) globalParentNode;
					LinkVariablePattern globalLink = findLinkFromTarget(
							castedGlobalParentNode.getLinkVariablePatterns(), target);
					if (globalLink != null) {
						LinkVariablePattern localLink = EcoreUtil.copy(globalLink);
						if (localLink.getOp() != null && localLink.getOp().getValue().equals(DEFAULT_OPERATOR)) {
							localLink.setOp(null);
						}
						localLink.setTarget(target);
						localLinkList.add(localLink);
						break;
					}
				}
			}
		}
	}

	private GlobalContext getGlobalContextOfRule(NamedElements rule) {
		return globalCache.get(rule.getName());
	}

	private void triggerArrangeAll() {
		ArrangeRequest arrangeRequest = new ArrangeRequest(ActionIds.ACTION_ARRANGE_ALL);
		List<Object> partsToArrange = new ArrayList<Object>(1);
		DiagramEditPart diagramEditPart = ((DiagramEditor) getActiveEditor()).getDiagramEditPart();
		partsToArrange.add(diagramEditPart);
		arrangeRequest.setPartsToArrange(partsToArrange);
		diagramEditPart.performRequest(arrangeRequest);
	}

}
