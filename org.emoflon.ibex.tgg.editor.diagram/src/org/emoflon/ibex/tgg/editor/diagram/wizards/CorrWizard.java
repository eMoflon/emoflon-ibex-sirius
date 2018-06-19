package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class CorrWizard extends Wizard {

	protected CorrPageOne page1;
	protected CorrPageTwo page2;
	protected CorrPageThree page3;
	protected CorrPageFour page4;
	private CorrWizardState state;

	public CorrWizard(CorrWizardState state) {
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
		if (currentPage == page1) {
			return page2;

		} else if (currentPage == page2) {
			if (!state.isCreateNewType()) {
				page4.setPageComplete(true);
				return page3;
			} else {
				return page4;
			}
		} else if (currentPage == page4) {
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
		else {
			state.setDone(false);
			
		}
		return true;
	}

}
