package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.emoflon.ibex.tgg.editor.diagram.ui.NamedElementLabelProvider;

class NodePageOne extends BaseNodePage {
	private ListViewer typeSelector;
	private String fFilter;
	private Text textArea;
	private ViewerFilter vFilter;

	NodePageOne(NodeWizardState state) {
		super(state, "NodeTypeSelection", "Select Node Type", "Select the type of the new node");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		container.setLayout(layout1);
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Node type");

		fFilter = "";
		textArea = createFilterText(container);

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
		if (state.getTypeList().size() >= 1) {
			typeSelector.getList().setSelection(0);
		}

		vFilter = new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof EClassifier) {
					String elementName = ((EClassifier) element).getName();
					if (elementName.toLowerCase().contains((fFilter).toLowerCase())) {
						return true;
					}

				}
				return false;
			}
		};

		typeSelector.addFilter(vFilter);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if (state.getTypeList().size() >= 1) {
				// Auto-select if there is at least one item
				typeSelector.getList().setSelection(0);
				EClassifier selectedType = state.getTypeList().get(0);
				selectNodeType(selectedType);
			}

			textArea.setFocus();
		}
	}

	private void selectNodeType(EClassifier selectedType) {
		state.setSelectedType(selectedType);
		setPageComplete(true);
	}

	private Text createFilterText(Composite parent) {
		Text text = new Text(parent, SWT.BORDER);

		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		text.setLayoutData(gd);
		text.setFont(parent.getFont());

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (e.getSource() instanceof Text) {
					fFilter = ((Text) e.getSource()).getText();
					typeSelector.removeFilter(vFilter);
					typeSelector.addFilter(vFilter);
					if (state.getTypeList().size() >= 1) {
						typeSelector.getList().setSelection(0);
						EClassifier seletectedType = (EClassifier) typeSelector.getElementAt(0);
						state.setSelectedType(seletectedType);
						
					}
				}
			}
		};

		text.addModifyListener(listener);

		return text;
	}

}
