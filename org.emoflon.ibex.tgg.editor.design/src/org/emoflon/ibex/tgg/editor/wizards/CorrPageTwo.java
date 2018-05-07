package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class CorrPageTwo extends BaseWizardPage {
	private ListViewer sourceSelector;
	private ListViewer targetSelector;

	public CorrPageTwo(WizardState state) {
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
				if(state.getSelectedTarget() != null)
					setPageComplete(true);
			}
		});

		sourceSelector.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				ObjectVariablePattern sObject = (ObjectVariablePattern) element;
				EqualityHelper eq = new EqualityHelper();
				if (state.getSelectedType() != null
						&& eq.equals(state.getSelectedType().getSource(), sObject.getType()))
					return true;
				else
					return false;
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
				if(state.getSelectedSource() != null)
					setPageComplete(true);
			}
		});

		targetSelector.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				ObjectVariablePattern tObject = (ObjectVariablePattern) element;
				EqualityHelper eq = new EqualityHelper();
				if (state.getSelectedType() != null
						&& eq.equals(state.getSelectedType().getTarget(), tObject.getType()))
					return true;
				else
					return false;
			}
		});
		
		targetSelector.setInput(state.getTargetObjects());

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

	public void refreshViewers() {
		sourceSelector.refresh();
		targetSelector.refresh();
	}

}
