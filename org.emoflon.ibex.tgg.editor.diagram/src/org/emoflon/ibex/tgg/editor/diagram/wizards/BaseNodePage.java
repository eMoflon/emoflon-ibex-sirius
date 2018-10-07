package org.emoflon.ibex.tgg.editor.diagram.wizards;

public abstract class BaseNodePage extends BaseWizardPage {
	protected NodeWizardState state;
	
	BaseNodePage(NodeWizardState state, String pageName, String title, String description) {
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
