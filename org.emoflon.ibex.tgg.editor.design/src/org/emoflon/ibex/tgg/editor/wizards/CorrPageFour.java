package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CorrPageFour extends BaseWizardPage {

	public CorrPageFour(WizardState state) {
		super(state, "", "", "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}

}
