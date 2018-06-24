package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.sirius.diagram.ui.tools.api.figure.anchor.AnchorProvider;
import org.eclipse.sirius.diagram.ui.tools.api.graphical.edit.styles.SimpleStyleConfiguration;

public class CorrespondenceStyleConfiguration extends SimpleStyleConfiguration {
	@Override
	public AnchorProvider getAnchorProvider() {
		return new CorrespondenceAnchorProvider();
	}
}
