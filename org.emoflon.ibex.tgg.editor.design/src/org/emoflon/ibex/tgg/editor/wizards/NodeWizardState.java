package org.emoflon.ibex.tgg.editor.wizards;

import java.util.List;

import org.eclipse.emf.ecore.EClassifier;

public class NodeWizardState extends WizardState {
	private List<EClassifier> typeList;
	private EClassifier selectedType = null;
	private String nodeName = null;

	public NodeWizardState(List<EClassifier> typeList) {
		this.typeList = typeList;
	}

	public EClassifier getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(EClassifier selectedType) {
		this.selectedType = selectedType;
		nodeName = null;
	}

	public List<EClassifier> getTypeList() {
		return typeList;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

}
