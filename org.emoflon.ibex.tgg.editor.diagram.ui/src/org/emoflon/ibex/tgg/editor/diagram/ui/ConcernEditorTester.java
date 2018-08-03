package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.sirius.diagram.DDiagram;
import org.eclipse.sirius.ui.business.api.dialect.DialectEditor;
import org.eclipse.sirius.viewpoint.DRepresentation;

public class ConcernEditorTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isConcernedEditor".equals(property)) {
			// called in a with activeEditor element
			if (receiver instanceof DialectEditor) {
				DRepresentation activeRepresentation = ((DialectEditor) receiver).getRepresentation();
				if (activeRepresentation instanceof DDiagram) {
					// the id property in the VSM editor : name in the meta
					// model.
					String repName = ((DDiagram) activeRepresentation).getDescription().getName();
					return repName.equals("tggRule") || repName.equals("complementRule");
				}
			}
		}
		return false;
	}

}
