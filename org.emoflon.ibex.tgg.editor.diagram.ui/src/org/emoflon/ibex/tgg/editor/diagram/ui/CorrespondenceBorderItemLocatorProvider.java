package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.IBorderItemLocator;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.ui.tools.api.graphical.edit.styles.BorderItemLocatorProvider;
import org.eclipse.sirius.diagram.ui.tools.api.graphical.edit.styles.IBorderItemOffsets;

public class CorrespondenceBorderItemLocatorProvider implements BorderItemLocatorProvider {
private static CorrespondenceBorderItemLocatorProvider instance = new CorrespondenceBorderItemLocatorProvider();
	
	private CorrespondenceBorderItemLocatorProvider() {
		// empty.
	}
	
	@Override
	public IBorderItemLocator getBorderItemLocator(IFigure figure, DDiagramElement diagramElementOwner,
			DDiagramElement diagramElementBorderItem) {
		CorrespondenceBorderItemLocator locator;
		if(diagramElementBorderItem.getDiagramElementMapping().getName().toLowerCase().contains("left")) {
			// Left port
			locator = new CorrespondenceBorderItemLocator(figure, IBorderItemOffsets.DEFAULT_OFFSET, PositionConstants.WEST);
		}
		else {
			// Right port
			locator = new CorrespondenceBorderItemLocator(figure, IBorderItemOffsets.DEFAULT_OFFSET, PositionConstants.EAST);
		}
		
		return locator;
	}

	public static BorderItemLocatorProvider getInstance() {
		return instance;
	}

}
