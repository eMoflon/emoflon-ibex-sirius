package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.jface.wizard.WizardPage;

public abstract class BaseWizardPage extends WizardPage {

	public BaseWizardPage(String pageName, String title, String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
	}
}
