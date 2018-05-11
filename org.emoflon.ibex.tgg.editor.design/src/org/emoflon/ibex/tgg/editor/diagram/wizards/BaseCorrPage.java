package org.emoflon.ibex.tgg.editor.diagram.wizards;

public abstract class BaseCorrPage extends BaseWizardPage {
	protected CorrWizardState state;
	
	public BaseCorrPage(CorrWizardState state, String pageName, String title, String description) {
		super(pageName, title, description);
		this.state = state;
	}

	public CorrWizardState getState() {
		return state;
	}

	public void setState(CorrWizardState state) {
		this.state = state;
	}

}
