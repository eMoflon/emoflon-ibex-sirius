package org.emoflon.ibex.tgg.editor.diagram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.requests.ArrangeRequest;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
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
import org.emoflon.ibex.tgg.editor.diagram.wizards.NodeWizard;
import org.emoflon.ibex.tgg.editor.diagram.wizards.NodeWizardState;
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

public class DesignServices extends CommonServices {
	private Map<String, NamedElements> nodeMap;

	public DesignServices() {
		nodeMap = new HashMap<String, NamedElements>();
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
		LinkVariablePattern link = findLinkBetweenObjectPatterns(source, target);
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
		LinkVariablePattern link = findLinkBetweenObjectPatterns(source, target);
		if (link != null) {
			if (addLinkEdge(newSource, target, link.getOp())) {
				// Remove old link relation
				source.getLinkVariablePatterns().remove(link);
				return true;
			}
		}
		return false;
	}

	public boolean reconnectRefineEdgeSource(ComplementRule source, Rule target, NamedElements newSource) {
		// Set new refine relation
		((ComplementRule) newSource).setKernel(target);
		// Remove old refine relation
		source.setKernel(null);
		return true;
	}

	public String getLinkEdgeName(ObjectVariablePattern sourceObject, DEdge edgeView) {
		// Get the target object from the edge view
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);

		// find the link variable pattern between object patterns sourceObject and
		// targetObject
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject);
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
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject);
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

		// find the link variable pattern between object patterns sourceObject and
		// targetObject
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject);
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
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject);
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
			// TODO enum attr
			System.out.println(((EnumExpression) expr).getEenum().toString());
			System.out.println(((EnumExpression) expr).getLiteral().toString());
		}

		return sb.toString();
	}

	public List<ObjectVariablePattern> findChildNodes(ObjectVariablePattern parent, DSemanticDiagram diagram,
			boolean isSourceNode) {
		NamedElements rule = (NamedElements) diagram.getTarget();
		List<LinkVariablePattern> linkList = parent.getLinkVariablePatterns();
		List<ObjectVariablePattern> nodeList = null;
		List<ObjectVariablePattern> childNodes = new ArrayList<ObjectVariablePattern>();
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
				}
			}
		}

		return childNodes;
	}

	public List<ObjectVariablePattern> findGlobalChildNodes(ObjectVariablePattern parent, DSemanticDiagram diagram) {
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

		List<LinkVariablePattern> linkList = parent.getLinkVariablePatterns();

		for (ObjectVariablePattern node : nodeList) {
			if (findLinkToTarget(linkList, node) != null) {
				childNodes.add(node);
			}
		}

		return childNodes;
	}

	public List<ObjectVariablePattern> findL(ObjectVariablePattern parent) {
		final List<ObjectVariablePattern> nodeList = new ArrayList<ObjectVariablePattern>();

		if (!nodeMap.containsKey(parent.getName())) {
			return nodeList;
		}

		NamedElements globalNode = nodeMap.get(parent.getName());
		if (globalNode instanceof ObjectVariablePattern) {
			ObjectVariablePattern castedGlobalNode = (ObjectVariablePattern) globalNode;
			castedGlobalNode.getLinkVariablePatterns().stream().forEach(l -> nodeList.add(l.getTarget()));
		}
		return nodeList;
	}

	public List<NamedElements> getNodes(EObject context, DSemanticDiagram diagram, String populateTask) {
		List<NamedElements> nodes = new ArrayList<NamedElements>();
		List<Rule> visitedRules = new ArrayList<Rule>();
		Map<String, NamedElements> nodeMap = new HashMap<String, NamedElements>();
		if (context instanceof ComplementRule) {
			nodeMap = populateHelper(((ComplementRule) context).getKernel(), nodeMap, visitedRules, populateTask);
		} else if (context instanceof Rule) {
			nodeMap = populateHelper(((Rule) context).getSupertypes(), nodeMap, visitedRules, populateTask);
		}

		nodes.addAll(nodeMap.values());

		updateNodemap(nodeMap);

		return nodes;
	}

	public List<NamedElements> getGlobalCorrespondences(EObject context, DSemanticDiagram diagram) {
		List<NamedElements> globalCorr = new ArrayList<NamedElements>();
		Map<String, NamedElements> corrMap = new HashMap<String, NamedElements>();
		if (context instanceof ComplementRule) {
			corrMap = populateHelper(((ComplementRule) context).getKernel(), corrMap, new ArrayList<Rule>(),
					"correspondences");
		} else if (context instanceof Rule) {
			corrMap = populateHelper(((Rule) context).getSupertypes(), corrMap, new ArrayList<Rule>(),
					"correspondences");
		}

		globalCorr.addAll(corrMap.values());
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

	private Map<String, NamedElements> populateHelper(Rule contextRule, Map<String, NamedElements> populationMap,
			List<Rule> visitedRules, String populateTask) {
		if (contextRule == null || visitedRules.contains(contextRule)) {
			return populationMap;
		}

		switch (populateTask) {
		case "sourceNodes":
			addNamedElementsToMap(contextRule.getSourcePatterns(), populationMap);
			break;
		case "targetNodes":
			addNamedElementsToMap(contextRule.getTargetPatterns(), populationMap);
			break;
		case "correspondences":
			addNamedElementsToMap(contextRule.getCorrespondencePatterns(), populationMap);
			break;
		}

		visitedRules.add(contextRule);

		if (contextRule.getSupertypes() != null) {
			populationMap = populateHelper(contextRule.getSupertypes(), populationMap, visitedRules, populateTask);
		}

		return populationMap;
	}

	private Map<String, NamedElements> populateHelper(List<Rule> contextRules, Map<String, NamedElements> populationMap,
			List<Rule> visitedRules, String populateTask) {

		if (contextRules != null) {
			for (Rule contextRule : contextRules) {
				populationMap = populateHelper(contextRule, populationMap, visitedRules, populateTask);
			}
		}

		return populationMap;
	}

	private void addNamedElementsToMap(List<? extends NamedElements> namedElements, Map<String, NamedElements> map) {
		for (NamedElements n : namedElements) {
			map.put(n.getName(), n);
		}
	}

	private ObjectVariablePattern getTargetObjectFromEdge(DEdge edgeView) {
		DDiagramElement t = (DDiagramElement) edgeView.getTargetNode();
		ObjectVariablePattern targetObject = (ObjectVariablePattern) t.getTarget();

		return targetObject;
	}

	private LinkVariablePattern findLinkBetweenObjectPatterns(ObjectVariablePattern x, ObjectVariablePattern y) {
		List<LinkVariablePattern> linkList = null;

		// Try to find link locally
		linkList = x.getLinkVariablePatterns();
		// find the link variable pattern between object patterns x and y
		LinkVariablePattern link = findLinkToTarget(linkList, y);
		if (link != null) {
			return link;
		}
		// try to find link in global node map if previous search was unsuccessful
		if (nodeMap.containsKey(x.getName())) {
			ObjectVariablePattern globalNode = (ObjectVariablePattern) nodeMap.get(x.getName());
			linkList = globalNode.getLinkVariablePatterns();
			link = findLinkToTarget(linkList, y);
			if (link != null) {
				return link;
			}
		}
		
		return null;
	}

	private LinkVariablePattern findLinkToTarget(List<LinkVariablePattern> linkList, ObjectVariablePattern target) {
		for (LinkVariablePattern link : linkList) {
			if (link.getTarget().getName().equals(target.getName())) {

				return link;
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
			}
			if (idx < params.size() - 1) {
				attr = attr + ", ";
				idx++;
			}

			/*
			 * TODO else if(p instanceof EnumExpression) { EnumExpression tmp =
			 * (EnumExpression)p; attr = attr + tmp.getEenum().get + ", "; }
			 */

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
				List<LinkVariablePattern> links = contextNode.getLinkVariablePatterns();
				List<LinkVariablePattern> newLinks = new ArrayList<LinkVariablePattern>();
				List<ObjectVariablePattern> childNodes = findChildNodes(contextNode,
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
							if (newLink.getOp().getValue().equals(DEFAULT_OPERATOR)) {
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
				// triggerArrangeAll();
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
			// triggerArrangeAll();
		}
		return null;
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

	private String getObjectVariableName(EObject objVar) {
		String objVarName = "";
		if (objVar instanceof ObjectVariablePattern) {
			objVarName = ((ObjectVariablePattern) objVar).getName();
		} else if (objVar instanceof ContextObjectVariablePattern) {
			objVarName = ((ContextObjectVariablePattern) objVar).getName();
		}

		return objVarName;
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

	private synchronized void updateNodemap(Map<String, NamedElements> map) {
		nodeMap.putAll(map);
	}

	@SuppressWarnings("unused")
	private void triggerArrangeAll() {
		ArrangeRequest arrangeRequest = new ArrangeRequest(ActionIds.ACTION_ARRANGE_ALL);
		List<Object> partsToArrange = new ArrayList<Object>(1);
		DiagramEditPart diagramEditPart = ((DiagramEditor) getActiveEditor()).getDiagramEditPart();
		partsToArrange.add(diagramEditPart);
		arrangeRequest.setPartsToArrange(partsToArrange);
		diagramEditPart.performRequest(arrangeRequest);
	}

}
