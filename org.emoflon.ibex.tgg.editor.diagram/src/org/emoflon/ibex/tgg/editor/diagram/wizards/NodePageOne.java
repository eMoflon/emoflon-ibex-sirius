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

public class NodePageOne extends BaseNodePage {
	private ListViewer typeSelector;

	public NodePageOne(NodeWizardState state) {
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
				state.setSelectedType(selectedType);
				setPageComplete(true);
			}
		});

		typeSelector.setInput(state.getTypeList());

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

}
