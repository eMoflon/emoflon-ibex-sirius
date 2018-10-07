package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class NodeWizard extends Wizard {

	private NodePageOne page1;
	private NodePageTwo page2;
	private NodeWizardState state;
	private String title;

	public NodeWizard(NodeWizardState state, String title) {
		super();
		this.state = state;
		this.title = title;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return title;
	}

	@Override
	public void addPages() {
		page1 = new NodePageOne(state);
		page2 = new NodePageTwo(state);
		addPage(page1);
		addPage(page2);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
		if(currentPage == page1)
			return page2;
		
		return null;
	}

	@Override
	public boolean performFinish() {
		if (state.getSelectedType() != null && state.getNodeName() != null) {
			state.setDone(true);
		}
		return true;
	}

}
