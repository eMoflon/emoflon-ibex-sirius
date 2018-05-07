package org.emoflon.ibex.tgg.editor.wizards;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class CorrWizard extends Wizard {
	
	protected CorrPageOne page1;
	protected CorrPageTwo page2;
	protected CorrPageThree page3;
	protected CorrPageFour page4;
	private WizardState state;
	
	public CorrWizard(List<CorrType> corrTypeList, List<ObjectVariablePattern> sourceObjects, List<ObjectVariablePattern> targetObjects) {
		super();
		setNeedsProgressMonitor(true);
		state = new WizardState(corrTypeList, sourceObjects, targetObjects);
	}
	
	@Override
	public String getWindowTitle() {
		return "New Correspondence";
	}
	
	@Override
	public void addPages( ) {
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
	        return page4;
	    }
	    else if(currentPage == page1 && !state.isCreateNewType()) {
	    	page4.setPageComplete(true);
	    	page2.refreshViewers();
	    	return page2;
	    }
	    else if(currentPage == page2) {
	    	return page3;
	    }
	    return null;
	}

	@Override
	public boolean performFinish() {
		state.setDone(true);
		return true;
	}

}
