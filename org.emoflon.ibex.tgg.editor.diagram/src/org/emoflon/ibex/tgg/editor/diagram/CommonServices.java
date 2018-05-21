package org.emoflon.ibex.tgg.editor.diagram;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.moflon.tgg.mosl.tgg.Operator;
import org.moflon.tgg.mosl.tgg.TggFactory;

public class CommonServices {
	protected final String DEFAULT_OPERATOR = "++";
	
	public Operator getDefaultOperator(EObject self) {
		Operator operator = TggFactory.eINSTANCE.createOperator();
		operator.setValue(DEFAULT_OPERATOR);
		return operator;
	}
	
	public String askStringFromUser(EObject self, String title, String message, String initialValue) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, initialValue, null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		} else {
			return initialValue;
		}
	}
}
