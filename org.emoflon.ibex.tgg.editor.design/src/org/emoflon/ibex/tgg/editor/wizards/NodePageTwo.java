package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NodePageTwo extends BaseNodePage {
	private Text textField;
	
	public NodePageTwo(NodeWizardState state) {
		super(state, "Name", "Name of Node", "Enter a name for the new node");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		container.setLayout(layout1);

		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Node name:");

		textField = new Text(container, SWT.BORDER | SWT.SINGLE);
		textField.setText("");
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
						state.setNodeName(text);
						setPageComplete(true);
					}
					else {
						state.setNodeName(null);
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
			if (state.getNodeName() == null) {
				setPageComplete(false);
				textField.setText("");
			}
		}
	}

}
