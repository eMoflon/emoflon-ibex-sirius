package org.emoflon.ibex.tgg.editor.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DEdge;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.emoflon.ibex.tgg.editor.wizards.BaseCorrPage;
import org.emoflon.ibex.tgg.editor.wizards.BaseNodePage;
import org.emoflon.ibex.tgg.editor.wizards.CorrWizard;
import org.emoflon.ibex.tgg.editor.wizards.CorrWizardState;
import org.emoflon.ibex.tgg.editor.wizards.NamedElementLabelProvider;
import org.emoflon.ibex.tgg.editor.wizards.NodeWizard;
import org.emoflon.ibex.tgg.editor.wizards.NodeWizardState;
import org.moflon.tgg.mosl.tgg.AttrCond;
import org.moflon.tgg.mosl.tgg.AttrCondDef;
import org.moflon.tgg.mosl.tgg.AttributeAssignment;
import org.moflon.tgg.mosl.tgg.AttributeConstraint;
import org.moflon.tgg.mosl.tgg.AttributeExpression;
import org.moflon.tgg.mosl.tgg.ComplementRule;
import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.CorrVariablePattern;
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

public class DesignServices extends CommonServices{
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

			// Set the default operator "++" for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue("++");
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
			targetObjects = ((Rule) tgg).getSourcePatterns();
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

			// Set the default operator "++" for the new correspondence
			Operator op = TggFactory.eINSTANCE.createOperator();
			op.setValue("++");
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

		// Delete correspondence type from schema if there are no more uses of it
		CorrType corrType = corr.getType();
		int numUses = 0;
		for (CorrVariablePattern c : correspondenceList) {
			if (c.getType() == corrType) {
				numUses++;
				if (numUses > 1)
					break;
			}
		}

		if (numUses == 1) {
			schema.getCorrespondenceTypes().remove(corrType);
		}

		// Delete correspondence from rule
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

		if (tgg instanceof Rule) {
			correspondenceList = new ArrayList<CorrVariablePattern>(((Rule) tgg).getCorrespondencePatterns());
			sourceObjects = ((Rule) tgg).getSourcePatterns();
			targetObjects = ((Rule) tgg).getTargetPatterns();
		} else if (tgg instanceof ComplementRule) {
			correspondenceList = new ArrayList<CorrVariablePattern>(((ComplementRule) tgg).getCorrespondencePatterns());
			sourceObjects = ((ComplementRule) tgg).getSourcePatterns();
			targetObjects = ((ComplementRule) tgg).getTargetPatterns();
		}

		else {
			return false;
		}

		if (correspondenceList == null || sourceObjects == null || targetObjects == null) {
			return false;
		}

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

	public String getCondAttribute(AttrCond attrCond) {
		AttrCondDef def = attrCond.getName();
		String attr = def.getName();
		attr += "(";
		List<ParamValue> params = attrCond.getValues();
		for (ParamValue p : params) {
			if (p instanceof AttributeExpression) {
				AttributeExpression tmp = (AttributeExpression) p;
				attr = attr + tmp.getObjectVar().getName() + "." + tmp.getAttribute().getName() + ", ";
			} else if (p instanceof LiteralExpression) {
				LiteralExpression tmp = (LiteralExpression) p;
				attr = attr + tmp.getValue() + ", ";
			} else if (p instanceof LocalVariable) {
				LocalVariable tmp = (LocalVariable) p;
				attr = attr + tmp.getName() + ", ";
			}

			/*
			 * TODO else if(p instanceof EnumExpression) { EnumExpression tmp =
			 * (EnumExpression)p; attr = attr + tmp.getEenum().get + ", "; }
			 */

		}
		if (attr.length() > 2) {
			attr = attr.substring(0, attr.length() - 2);
		}
		attr += ")";

		return attr;
	}
	
	private ObjectVariablePattern getTargetObjectFromEdge(DEdge edgeView) {
		DDiagramElement t = (DDiagramElement) edgeView.getTargetNode();
		ObjectVariablePattern targetObject = (ObjectVariablePattern) t.getTarget();

		return targetObject;
	}
	
	private LinkVariablePattern findLinkBetweenObjectPatterns(ObjectVariablePattern x, ObjectVariablePattern y) {
		List<LinkVariablePattern> linkList = x.getLinkVariablePatterns();
		EqualityHelper eq = new EqualityHelper();

		// find the link variable pattern between object patterns x and y
		for (LinkVariablePattern link : linkList) {
			if (eq.equals(link.getTarget(), y)) {

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

}
