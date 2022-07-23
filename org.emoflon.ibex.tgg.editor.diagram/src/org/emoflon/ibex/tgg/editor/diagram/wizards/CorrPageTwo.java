package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.emoflon.ibex.tgg.editor.diagram.ui.NamedElementLabelProvider;
import org.emoflon.ibex.tgg.editor.tgg.ObjectVariablePattern;

class CorrPageTwo extends BaseCorrPage {
	private CustomListViewer sourceSelector;
	private CustomListViewer targetSelector;
	private TypeFilter sourceTypeFilter;
	private TypeFilter targetTypeFilter;

	CorrPageTwo(CorrWizardState state) {
		super(state, "SourceTargetSelection", "Select Source and Target Objects",
				"Select the source and target objects of the new correspondence");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		FillLayout layout1 = new FillLayout(SWT.HORIZONTAL);
		container.setLayout(layout1);
		Composite sourceContainer = new Composite(container, SWT.NONE);
		Composite targetContainer = new Composite(container, SWT.NONE);
		sourceTypeFilter = new TypeFilter();
		targetTypeFilter = new TypeFilter();
		GridLayout layout2 = new GridLayout();
		sourceContainer.setLayout(layout2);
		targetContainer.setLayout(layout2);
		Label label1 = new Label(sourceContainer, SWT.NONE);
		label1.setText("Select source object:");
		sourceSelector = new CustomListViewer(sourceContainer);
		sourceSelector.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ObjectVariablePattern selectedSource = (ObjectVariablePattern) selection.getFirstElement();
				selectSource(selectedSource);
			}
		});

		sourceSelector.setInput(state.getSourceObjects());

		Label label2 = new Label(targetContainer, SWT.NONE);
		label2.setText("Select target object:");

		targetSelector = new CustomListViewer(targetContainer);
		targetSelector.setLabelProvider(new NamedElementLabelProvider());

		targetSelector.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ObjectVariablePattern selectedTarget = (ObjectVariablePattern) selection.getFirstElement();
				selecTarget(selectedTarget);
			}
		});

		targetSelector.setInput(state.getTargetObjects());

		if (!state.isCreateNewType()) {
			sourceSelector.addFilter(sourceTypeFilter);
			targetSelector.addFilter(targetTypeFilter);
		}

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if (state.getSelectedTarget() == null || state.getSelectedSource() == null) {
				setPageComplete(false);
				setErrorMessage(null);
				sourceSelector.getList().deselectAll();
				targetSelector.getList().deselectAll();
			}

			sourceSelector.resetFilters();
			targetSelector.resetFilters();
			if (state.getSelectedType() != null && !state.isCreateNewType()) {
				sourceTypeFilter.setType(state.getSelectedType().getSource());
				targetTypeFilter.setType(state.getSelectedType().getTarget());
				sourceSelector.addFilter(sourceTypeFilter);
				targetSelector.addFilter(targetTypeFilter);
			}
			if (sourceSelector.getList().getItemCount() == 1) {
				// Auto-select if there is only one item
				ObjectVariablePattern selectedSource = (ObjectVariablePattern) sourceSelector.getElementAt(0);
				sourceSelector.getList().setSelection(0);
				selectSource(selectedSource);
			}
			if (targetSelector.getList().getItemCount() == 1) {
				// Auto-select if there is only one item
				ObjectVariablePattern selectedTarget = (ObjectVariablePattern) targetSelector.getElementAt(0);
				targetSelector.getList().setSelection(0);
				selecTarget(selectedTarget);
			}
		}
	}

	private boolean isTypeAlreadyInSchema(ObjectVariablePattern source, ObjectVariablePattern target) {
		boolean alreadyInSchema = state.getCorrTypeList().stream()
				.anyMatch(tp -> tp.getTarget().getName().equals(target.getType().getName())
						&& tp.getSource().getName().equals(source.getType().getName()));

		return alreadyInSchema;
	}

	private void selectSource(ObjectVariablePattern selectedSource) {
		if (selectedSource == null) {
			return;
		}
		state.setSelectedSource(selectedSource);
		((BaseCorrPage) getNextPage()).setPageComplete(false);
		if (state.getSelectedTarget() != null) {
			if (state.isCreateNewType()) {
				CorrPageFour p4 = (CorrPageFour) getWizard().getPage("NewCorrType");
				CorrPageThree p3 = (CorrPageThree) getWizard().getPage("Name");
				p4.setErrorMessage(null);
				p4.textField.setText("");
				p4.setPageComplete(false);
				p3.setPageComplete(false);
				if (!isTypeAlreadyInSchema(selectedSource, state.getSelectedTarget())) {
					setErrorMessage(null);
					setPageComplete(true);
				} else {
					setPageComplete(false);
					setErrorMessage(
							"A correspondence type for the same source and target pattern types already exists in project's schema");
				}
			} else {
				setPageComplete(true);
			}
		}
	}

	private void selecTarget(ObjectVariablePattern selectedTarget) {
		if (selectedTarget == null) {
			return;
		}
		state.setSelectedTarget(selectedTarget);
		((BaseCorrPage) getNextPage()).setPageComplete(false);
		if (state.getSelectedSource() != null) {
			if (state.isCreateNewType()) {
				CorrPageFour p4 = (CorrPageFour) getWizard().getPage("NewCorrType");
				CorrPageThree p3 = (CorrPageThree) getWizard().getPage("Name");
				p4.setErrorMessage(null);
				p4.textField.setText("");
				p4.setPageComplete(false);
				p3.setPageComplete(false);
				if (!isTypeAlreadyInSchema(state.getSelectedSource(), selectedTarget)) {
					setErrorMessage(null);
					setPageComplete(true);
				} else {
					setPageComplete(false);
					setErrorMessage(
							"A correspondence type for the same source and target pattern types already exists in project's schema");
				}
			} else {
				setPageComplete(true);
			}
		}
	}

}
