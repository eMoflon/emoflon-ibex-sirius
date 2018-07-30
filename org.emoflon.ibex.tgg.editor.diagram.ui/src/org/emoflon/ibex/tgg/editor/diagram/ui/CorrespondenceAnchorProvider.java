package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.sirius.diagram.ui.tools.api.figure.AirDefaultSizeNodeFigure;
import org.eclipse.sirius.diagram.ui.tools.api.figure.anchor.AnchorProvider;

public class CorrespondenceAnchorProvider implements AnchorProvider {

	private static CorrespondenceAnchorProvider instance = new CorrespondenceAnchorProvider();

	private CorrespondenceAnchorProvider() {
		// empty.
	}

	@Override
	public ConnectionAnchor createAnchor(AirDefaultSizeNodeFigure figure, PrecisionPoint referencePoint) {
		return new CorrespondenceAnchor(figure);
	}

	@Override
	public ConnectionAnchor createDefaultAnchor(AirDefaultSizeNodeFigure figure) {
		return new CorrespondenceAnchor(figure);
	}

	public static CorrespondenceAnchorProvider getInstance() {
		return instance;
	}

}
