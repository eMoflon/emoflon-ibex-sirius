package org.emoflon.ibex.tgg.editor.wizards;

import java.util.List;

import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class WizardState {
	private List<CorrType> corrTypeList;
	private List<ObjectVariablePattern> sourceObjects;
	private List<ObjectVariablePattern> targetObjects;
	private boolean createNewType = false;
	private boolean done = false;
	private CorrType selectedType = null;
	private ObjectVariablePattern selectedSource = null;
	private ObjectVariablePattern selectedTarget = null;
	private String corrName = null;

	public WizardState(List<CorrType> corrTypeList, List<ObjectVariablePattern> sourceObjects, List<ObjectVariablePattern> targetObjects) {
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
	}

	public void setCreateNewType(boolean createNewType) {
		this.createNewType = createNewType;
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
	}

	public ObjectVariablePattern getSelectedTarget() {
		return selectedTarget;
	}

	public void setSelectedTarget(ObjectVariablePattern selectedTarget) {
		this.selectedTarget = selectedTarget;
	}

	public String getCorrName() {
		return corrName;
	}

	public void setCorrName(String corrName) {
		this.corrName = corrName;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

}
