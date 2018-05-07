package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.jface.wizard.WizardPage;

public abstract class BaseWizardPage extends WizardPage {
	
	protected WizardState state;

	public BaseWizardPage(WizardState state, String pageName, String title, String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
		this.state = state;
	}

	public WizardState getState() {
		return state;
	}
}
