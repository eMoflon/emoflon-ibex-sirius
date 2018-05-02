package org.emoflon.ibex.tgg.editor.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.moflon.tgg.mosl.tgg.AttrCond;
import org.moflon.tgg.mosl.tgg.AttrCondDef;
import org.moflon.tgg.mosl.tgg.AttributeExpression;
import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.CorrVariablePattern;
import org.moflon.tgg.mosl.tgg.LinkVariablePattern;
import org.moflon.tgg.mosl.tgg.LiteralExpression;
import org.moflon.tgg.mosl.tgg.LocalVariable;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;
import org.moflon.tgg.mosl.tgg.Operator;
import org.moflon.tgg.mosl.tgg.ParamValue;
import org.moflon.tgg.mosl.tgg.Rule;
import org.moflon.tgg.mosl.tgg.Schema;
import org.moflon.tgg.mosl.tgg.TggFactory;
import org.moflon.tgg.mosl.tgg.TripleGraphGrammarFile;
import org.eclipse.sirius.business.api.query.EObjectQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DEdge;
import org.eclipse.sirius.diagram.DNode;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.ecore.extender.business.api.accessor.ModelAccessor;

/**
 * The services class used by VSM.
 */
public class Services {

	private EClass createEClass(final String name) {
		final EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(name);
		return eClass;
	}

	public int applyChanges(TripleGraphGrammarFile file, List<Rule> currentRuleList) {
		EObjectQuery query = new EObjectQuery(file);
		Session s = query.getSession();
		XMIResourceImpl xmiressourceImpl = (XMIResourceImpl) s.getSemanticResources().iterator().next();
		final TripleGraphGrammarFile semanticFile = (TripleGraphGrammarFile) xmiressourceImpl.getContents().get(0);
		final ModelAccessor ma = s.getModelAccessor();
		System.out.println("can edit rules : " + ma.getPermissionAuthority().canEditFeature(semanticFile, "rules"));
		TransactionalEditingDomain ted = s.getTransactionalEditingDomain();
		ted.getCommandStack().execute(new RecordingCommand(ted) {

			@Override
			protected void doExecute() {
				semanticFile.getRules().clear();
				semanticFile.getRules().addAll(currentRuleList);
			}
		});
		return 0;
	}

	public String getLinkEdgeName(ObjectVariablePattern sourceObject, DEdge edgeView) {

		// Get the target object from the edge view
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);

		// find the link variable pattern between object patterns sourceObject and
		// targetObject
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject);
		if (link != null) {
			return link.getType().getName();
		}

		return null;
	}

	private ObjectVariablePattern getTargetObjectFromEdge(DEdge edgeView) {
		DDiagramElement t = (DDiagramElement) edgeView.getTargetNode();
		ObjectVariablePattern targetObject = (ObjectVariablePattern) t.getTarget();

		return targetObject;
	}

	private ObjectVariablePattern getSourceObjectFromEdge(DEdge edgeView) {
		DDiagramElement s = (DDiagramElement) edgeView.getSourceNode();
		ObjectVariablePattern targetObject = (ObjectVariablePattern) s.getTarget();

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

	private CorrVariablePattern findCorrespondence(Rule rule, ObjectVariablePattern sourceObject,
			ObjectVariablePattern targetObject) {
		List<CorrVariablePattern> corrList = rule.getCorrespondencePatterns();
		EqualityHelper eq = new EqualityHelper();

		for (CorrVariablePattern corr : corrList) {
			if (eq.equals(corr.getSource(), sourceObject) && eq.equals(corr.getTarget(), targetObject)) {
				return corr;
			}
		}

		return null;
	}

	public boolean checkCorrespondence(Rule rule, ObjectVariablePattern sourceObject,
			ObjectVariablePattern targetObject) {

		CorrVariablePattern corr = findCorrespondence(rule, sourceObject, targetObject);

		return corr != null ? true : false;
	}

	public boolean equalCorrespondenceOperator(Rule rule, DEdge edgeView, String op) {
		ObjectVariablePattern sourceObject = getSourceObjectFromEdge(edgeView);
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);

		// find correspondence between source and target objects
		CorrVariablePattern corr = findCorrespondence(rule, sourceObject, targetObject);
		if (corr != null) {
			if (corr.getOp() != null) {
				return corr.getOp().getValue().equals(op) ? true : false;
			} else if (op.equals("")) {
				return true;
			}
		}
		return false;
	}

	public List<CorrVariablePattern> toggleCorrOperator(Rule rule, DEdge edgeView) {
		ObjectVariablePattern sourceObject = getSourceObjectFromEdge(edgeView);
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);

		// find correspondence between source and target objects
		CorrVariablePattern corr = findCorrespondence(rule, sourceObject, targetObject);

		if (corr != null) {
			// Toggle correspondence operator
			if (corr.getOp() == null) {
				Operator operator = TggFactory.eINSTANCE.createOperator();
				operator.setValue("++");
				corr.setOp(operator);
			} else {
				corr.setOp(null);
			}
		}

		return rule.getCorrespondencePatterns();
	}

	public Operator toggleObjectOperator(ObjectVariablePattern object) {
		Operator op = object.getOp();
		if (op == null) {
			Operator operator = TggFactory.eINSTANCE.createOperator();
			operator.setValue("++");
			return operator;
		} else {
			return null;
		}
	}

	public List<LinkVariablePattern> addLinkEdge(ObjectVariablePattern sourceObject,
			ObjectVariablePattern targetObject) {

		LinkVariablePattern link = TggFactory.eINSTANCE.createLinkVariablePattern();
		link.setTarget(targetObject);

		List<EReference> referenceList = sourceObject.getType().getEReferences();

		ElementListSelectionDialog dlg = new ElementListSelectionDialog(Display.getCurrent().getActiveShell(),
				new ENamedElementLabelProvider());
		dlg.setTitle("New Link");
		dlg.setMessage("Type of the new link relation");
		dlg.setElements(referenceList.toArray());
		dlg.setMultipleSelection(false);
		EReference selectedType = null;

		if (dlg.open() == Window.OK) {
			selectedType = (EReference) dlg.getResult()[0];
		}

		link.setType(selectedType);
		sourceObject.getLinkVariablePatterns().add(link);

		return sourceObject.getLinkVariablePatterns();
	}

	public List<LinkVariablePattern> deleteLinkEdge(ObjectVariablePattern sourceObject, DEdge edgeView) {
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);
		LinkVariablePattern link = findLinkBetweenObjectPatterns(sourceObject, targetObject);
		sourceObject.getLinkVariablePatterns().remove(link);

		return sourceObject.getLinkVariablePatterns();
	}

	public List<CorrVariablePattern> addCorrespondenceEdge(Rule rule, ObjectVariablePattern sourceObject,
			ObjectVariablePattern targetObject) {

		// Create a new correspondence type
		CorrType corrType = TggFactory.eINSTANCE.createCorrType();
		String corrName = askStringFromUser(null, "New Correspondence", "Name:", "NewCorrespondence");
		corrType.setName(corrName);
		corrType.setSource(sourceObject.getType());
		corrType.setTarget(targetObject.getType());

		// Add correspondence type to the schema if it was not already there
		Schema schema = rule.getSchema();
		List<CorrType> corrTypes = schema.getCorrespondenceTypes();
		EqualityHelper eq = new EqualityHelper();
		boolean containsType = false;
		for (CorrType t : corrTypes) {
			if (eq.equals(t, corrType)) {
				containsType = true;
				break;
			}
		}
		if (!containsType) {
			schema.getCorrespondenceTypes().add(corrType);
		}

		// Set the new correspondence
		CorrVariablePattern corr = TggFactory.eINSTANCE.createCorrVariablePattern();
		corr.setType(corrType);
		corr.setSource(sourceObject);
		corr.setTarget(targetObject);
		corr.setName(corrName);
		// Default Operator for correspondence is ++
		Operator operator = TggFactory.eINSTANCE.createOperator();
		operator.setValue("++");
		corr.setOp(operator);

		// Add the actual correspondence to the rule
		rule.getCorrespondencePatterns().add(corr);

		return rule.getCorrespondencePatterns();
	}
	
	public List<CorrVariablePattern> deleteCorrespondence(Rule rule, DEdge edgeView) {
		ObjectVariablePattern sourceObject = getSourceObjectFromEdge(edgeView);
		ObjectVariablePattern targetObject = getTargetObjectFromEdge(edgeView);
		
		// find correspondence between source and target objects
		CorrVariablePattern corr = findCorrespondence(rule, sourceObject, targetObject);
		
		// Delete correspondence from rule
		rule.getCorrespondencePatterns().remove(corr);
		
		// Delete correspondence type from schema
		Schema schema = rule.getSchema();
		schema.getCorrespondenceTypes().remove(corr.getType());
		return rule.getCorrespondencePatterns();
	}
	
	public String deleteNode(DSemanticDiagram diagram, DNode node) {
		Rule rootRule = (Rule)diagram.getTarget();
		Schema schema = rootRule.getSchema();
		List<CorrVariablePattern> correspondenceList = rootRule.getCorrespondencePatterns();
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

	public String askStringFromUser(EObject self, String title, String message, String initialValue) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, initialValue, null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		} else {
			return initialValue;
		}
	}

	public EClass askTypeFromUser(EObject self, String title, String message, String initialValue,
			boolean isSourceNode) {
		Rule tgg = (Rule) self;
		Schema schema = tgg.getSchema();
		Map<String, List<EClassifier>> classifiers;

		if (isSourceNode) {
			classifiers = getClassifiersInPackageList(schema.getSourceTypes());
		} else {
			classifiers = getClassifiersInPackageList(schema.getTargetTypes());
		}

		List<EClassifier> outputList = new ArrayList<EClassifier>();

		for (String key : classifiers.keySet()) {
			outputList.addAll(classifiers.get(key));
		}

		ElementListSelectionDialog dlg = new ElementListSelectionDialog(Display.getCurrent().getActiveShell(),
				new ENamedElementLabelProvider());
		dlg.setTitle(title);
		dlg.setMessage(message);
		dlg.setElements(outputList.toArray());
		dlg.setMultipleSelection(false);

		if (dlg.open() == Window.OK) {
			return (EClass) dlg.getResult()[0];
		} else {
			return createEClass(initialValue);
		}

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
}
