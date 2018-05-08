package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class CorrPageTwo extends BaseCorrPage {
	private ListViewer sourceSelector;
	private ListViewer targetSelector;
	private TypeFilter sourceTypeFilter;
	private TypeFilter targetTypeFilter;

	public CorrPageTwo(CorrWizardState state) {
		super(state, "SourceTargetSelection", "Select Source and Target Objects",
				"Select the source and target objects of the new correspondence");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		FillLayout layout1 = new FillLayout(SWT.HORIZONTAL);
		container.setLayout(layout1);
		sourceTypeFilter = new TypeFilter();
		targetTypeFilter = new TypeFilter();
		Composite sourceContainer = new Composite(container, SWT.NONE);
		Composite targetContainer = new Composite(container, SWT.NONE);
		GridLayout layout2 = new GridLayout();
		sourceContainer.setLayout(layout2);
		targetContainer.setLayout(layout2);
		Label label1 = new Label(sourceContainer, SWT.NONE);
		label1.setText("Select source object:");
		sourceSelector = new CustomListViewer(sourceContainer);
		sourceSelector.setLabelProvider(new NamedElementLabelProvider());
		sourceSelector.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ObjectVariablePattern selectedSource = (ObjectVariablePattern) selection.getFirstElement();
				state.setSelectedSource(selectedSource);
				if (state.getSelectedTarget() != null)
					setPageComplete(true);
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
				state.setSelectedTarget(selectedTarget);
				if (state.getSelectedSource() != null)
					setPageComplete(true);
			}
		});

		targetSelector.setInput(state.getTargetObjects());

		sourceSelector.addFilter(sourceTypeFilter);
		targetSelector.addFilter(targetTypeFilter);

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
				sourceSelector.getList().deselectAll();
				targetSelector.getList().deselectAll();
			}
			sourceSelector.resetFilters();
			targetSelector.resetFilters();
			if (state.getSelectedType() != null) {
				sourceTypeFilter.setType(state.getSelectedType().getSource());
				targetTypeFilter.setType(state.getSelectedType().getTarget());
				sourceSelector.addFilter(sourceTypeFilter);
				targetSelector.addFilter(targetTypeFilter);
			}
		}
	}

}
