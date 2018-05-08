package org.emoflon.ibex.tgg.editor.wizards;

import java.util.List;

import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class CorrWizardState extends WizardState {
	private List<CorrType> corrTypeList;
	private List<ObjectVariablePattern> sourceObjects;
	private List<ObjectVariablePattern> targetObjects;
	private boolean createNewType = false;
	private CorrType selectedType = null;
	private ObjectVariablePattern selectedSource = null;
	private ObjectVariablePattern selectedTarget = null;
	private String corrName = null;

	public CorrWizardState(List<CorrType> corrTypeList, List<ObjectVariablePattern> sourceObjects, List<ObjectVariablePattern> targetObjects) {
		this.corrTypeList = corrTypeList;
		this.sourceObjects = sourceObjects;
		this.targetObjects = targetObjects;
	}
	
	public List<CorrType> getCorrTypeList() {
		return corrTypeList;
	}
	
	public boolean isCreateNewType() {
		return createNewType;
	}

	public CorrType getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(CorrType selectedType) {
		this.selectedType = selectedType;
		selectedSource = null;
		selectedTarget = null;
		corrName = null;
	}

	public void setCreateNewType(boolean createNewType) {
		this.createNewType = createNewType;
		selectedType = null;
		selectedSource = null;
		selectedTarget = null;
		corrName = null;
	}

	public List<ObjectVariablePattern> getSourceObjects() {
		return sourceObjects;
	}

	public List<ObjectVariablePattern> getTargetObjects() {
		return targetObjects;
	}

	public ObjectVariablePattern getSelectedSource() {
		return selectedSource;
	}

	public void setSelectedSource(ObjectVariablePattern selectedSource) {
		this.selectedSource = selectedSource;
		corrName = null;
	}

	public ObjectVariablePattern getSelectedTarget() {
		return selectedTarget;
	}

	public void setSelectedTarget(ObjectVariablePattern selectedTarget) {
		corrName = null;
		this.selectedTarget = selectedTarget;
	}

	public String getCorrName() {
		return corrName;
	}

	public void setCorrName(String corrName) {
		this.corrName = corrName;
	}

	public void setNewType(CorrType type) {
		this.selectedType = type;
		corrName = null;
	}

}
