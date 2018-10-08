package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.emoflon.ibex.tgg.editor.diagram.ui.NamedElementLabelProvider;

class NodePageOne extends BaseNodePage {
	private ListViewer typeSelector;

	NodePageOne(NodeWizardState state) {
		super(state, "NodeTypeSelection", "Select Node Type", "Select the type of the new node");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		container.setLayout(layout1);
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Select node type:");
		typeSelector = new CustomListViewer(container);
		typeSelector.setLabelProvider(new NamedElementLabelProvider());
		typeSelector.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				EClassifier selectedType = (EClassifier) selection.getFirstElement();
				selectNodeType(selectedType);
			}
		});

		typeSelector.setInput(state.getTypeList());

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if(state.getTypeList().size() == 1) {
				// Auto-select if there is only one item
				typeSelector.getList().setSelection(0);
				EClassifier selectedType = state.getTypeList().get(0);
				selectNodeType(selectedType);
			}
		}
	}
	
	private void selectNodeType(EClassifier selectedType) {
		state.setSelectedType(selectedType);
		setPageComplete(true);
	}

}
