package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.moflon.tgg.mosl.tgg.CorrType;
import org.moflon.tgg.mosl.tgg.TggFactory;

public class CorrPageFour extends BaseCorrPage {
	private Text textField;
	private Label label1;

	public CorrPageFour(CorrWizardState state) {
		super(state, "NewCorrType", "New Correspondence Type", "Type a name for the new correspondence type");
	}

	@Override
	public void createControl(Composite parent) {
		CorrType type = TggFactory.eINSTANCE.createCorrType();
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		container.setLayout(layout1);

		label1 = new Label(container, SWT.WRAP);
		final GridData gd1 = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		label1.setLayoutData(gd1);
		label1.setText("Name for new correspondence type:");

		textField = new Text(container, SWT.BORDER | SWT.SINGLE);
		textField.setText("");
		textField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text t = (Text) (e.widget);
				if (!t.isFocusControl()) {
					return;
				}

				else {
					String text = t.getText();
					if (text.length() > 0) {
						type.setName(text);
						type.setSource(state.getSelectedSource().getType());
						type.setTarget(state.getSelectedTarget().getType());
						state.setNewType(type);
						setPageComplete(true);
					} else {
						state.setNewType(null);
						setPageComplete(false);
					}
				}

			}
		});
		final GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		textField.setLayoutData(gd2);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if (state.getSelectedType() == null) {
				setPageComplete(false);
				textField.setText("");
			}
		}
	}

}
