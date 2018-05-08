package org.emoflon.ibex.tgg.editor.wizards;

public abstract class BaseNodePage extends BaseWizardPage {
	protected NodeWizardState state;
	
	public BaseNodePage(NodeWizardState state, String pageName, String title, String description) {
		super(pageName, title, description);
		this.state = state;
	}

	public NodeWizardState getState() {
		return state;
	}

	public void setState(NodeWizardState state) {
		this.state = state;
	}

}
