package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.sirius.diagram.ui.tools.api.graphical.edit.styles.SimpleStyleConfiguration;
import org.eclipse.sirius.ext.gmf.runtime.gef.ui.figures.util.AnchorProvider;

public class CorrespondenceStyleConfiguration extends SimpleStyleConfiguration {
	private static CorrespondenceStyleConfiguration instance = new CorrespondenceStyleConfiguration();

	private CorrespondenceStyleConfiguration() {
		// empty.
	}

	@Override
	public AnchorProvider getAnchorProvider() {
		return CorrespondenceAnchorProvider.getInstance();
	}

	public static CorrespondenceStyleConfiguration getInstance() {
		return instance;
	}
}
