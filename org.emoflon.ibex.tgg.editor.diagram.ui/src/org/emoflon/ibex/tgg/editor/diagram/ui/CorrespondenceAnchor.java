package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class CorrespondenceAnchor extends ChopboxAnchor {

	public CorrespondenceAnchor() {
		super();
	}

	public CorrespondenceAnchor(IFigure owner) {
		super(owner);
	}

	@Override
	public Point getLocation(Point reference) {
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getBox());
		//r.translate(-1, -1);
		//r.resize(1, 1);

		getOwner().translateToAbsolute(r);
		float centerX = r.x + 0.5f * r.width;
		float centerY = r.y + 0.5f * r.height;
		Point leftPort = new Point(r.x, Math.round(centerY));
		Point RightPort = new Point(r.x + r.width, Math.round(centerY));

		float dx = reference.x - centerX;

		if (dx < 0) {
			return leftPort;
		} else {
			return RightPort;
		}
	}
}
