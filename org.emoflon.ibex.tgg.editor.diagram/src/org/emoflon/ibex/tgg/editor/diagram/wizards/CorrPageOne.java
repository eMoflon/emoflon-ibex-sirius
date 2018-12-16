package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.moflon.tgg.mosl.tgg.CorrType;

class CorrPageOne extends BaseCorrPage {
	private CustomListViewer lv;

	CorrPageOne(CorrWizardState state) {
		super(state, "SelectAction", "Select Type", "Select the correspondence's type");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		container.setLayout(layout1);
		// Create a group to contain 2 radio
		Composite selectionContainer = new Composite(container, SWT.NONE);
		selectionContainer.setLayout(new RowLayout(SWT.VERTICAL));
		Button buttonExistingType = new Button(selectionContainer, SWT.RADIO);
		buttonExistingType.setText("Use an existing correspondence type");
		Button buttonNewType = new Button(selectionContainer, SWT.RADIO);
		buttonNewType.setText("Define a new correspondence type");
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		Composite listContainer = new Composite(container, SWT.NONE);
		listContainer.setVisible(false);
		listContainer.setLayoutData(gd);
		GridLayout layout2 = new GridLayout();
		listContainer.setLayout(layout2);
		Label label1 = new Label(listContainer, SWT.NONE);
		label1.setText("Select desired type:");
		lv = new CustomListViewer(listContainer);

		lv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				CorrType selType = (CorrType) selection.getFirstElement();
				if (selType == null) {
					return;
				}
				state.setSelectedType(selType);
				((BaseCorrPage) getNextPage()).setPageComplete(false);
				setPageComplete(true);
			}
		});
		buttonNewType.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();

				if (source.getSelection()) {
					listContainer.setVisible(false);
					state.setCreateNewType(true);
					((BaseCorrPage) getNextPage()).setPageComplete(false);
					((BaseCorrPage) getWizard().getPage("Name")).setPageComplete(false);
					setPageComplete(true);
				}
			}

		});

		buttonExistingType.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();

				if (source.getSelection()) {
					state.setCreateNewType(false);
					listContainer.setVisible(true);
					lv.setInput(state.getCorrTypeList());
					if(state.getCorrTypeList().size() == 1) {
						// Auto-select if there is only one item
						lv.getList().setSelection(0);
					}
					((BaseCorrPage) getNextPage()).setPageComplete(false);
					setPageComplete(false);
				}
			}

		});

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}

}
