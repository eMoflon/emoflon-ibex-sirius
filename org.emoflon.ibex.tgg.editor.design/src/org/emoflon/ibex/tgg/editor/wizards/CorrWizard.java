package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class CorrWizard extends Wizard {

	protected CorrPageOne page1;
	protected CorrPageTwo page2;
	protected CorrPageThree page3;
	protected CorrPageFour page4;
	private WizardState state;

	public CorrWizard(WizardState state) {
		super();
		this.state = state;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return "New Correspondence";
	}

	@Override
	public void addPages() {
		page1 = new CorrPageOne(state);
		page2 = new CorrPageTwo(state);
		page3 = new CorrPageThree(state);
		page4 = new CorrPageFour(state);
		addPage(page1);
		addPage(page2);
		addPage(page3);
		addPage(page4);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
		if (currentPage == page1 && state.isCreateNewType()) {
			return page2;
		} else if (currentPage == page1 && !state.isCreateNewType()) {
			page4.setPageComplete(true);
			page2.refreshViewers();
			return page2;
		} else if (currentPage == page2 && !state.isCreateNewType()) {
			return page3;
		}
		return null;
	}

	@Override
	public boolean performFinish() {
		if (state.getSelectedType() != null && state.getSelectedSource() != null && state.getSelectedTarget() != null
				&& state.getCorrName() != null) {
			state.setDone(true);
		}
		return true;
	}

}
