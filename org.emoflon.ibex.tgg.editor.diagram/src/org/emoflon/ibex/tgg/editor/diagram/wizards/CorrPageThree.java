package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CorrPageThree extends BaseCorrPage {
	private Text textField;
	
	public CorrPageThree(CorrWizardState state) {
		super(state, "Name", "Name of Correspondence", "Enter a name for the new correspondence");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		container.setLayout(layout1);

		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Correspondence name:");

		textField = new Text(container, SWT.BORDER | SWT.SINGLE);
		textField.setText("");
		textField.setFocus();
		textField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text t = (Text)(e.widget);
				if(!t.isFocusControl()) {
					return;
				}
				
				else {
					String text = t.getText();
					if(text.length() > 0) {
						state.setCorrName(text);
						setPageComplete(true);
					}
					else {
						state.setCorrName(null);
						setPageComplete(false);
					}
				}
				
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		textField.setLayoutData(gd);

		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if (state.getCorrName() == null) {
				setPageComplete(false);
				textField.setText("");
				textField.setFocus();
			}
		}
	}

}
