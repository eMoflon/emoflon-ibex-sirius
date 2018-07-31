package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator;

public class CorrespondenceBorderItemLocator extends BorderItemLocator {

	private final Dimension forcedBorderItemOffset;
	private int forcedSide;

	/**
	 * Creates a new SouthCenteredBorderItemLocator with the specified parentFigure.
	 * 
	 * @param parentFigure
	 *            the parent figure
	 */
	public CorrespondenceBorderItemLocator(IFigure parentFigure) {
		this(parentFigure, null, -1);
	}

	/**
	 * Creates a new SouthCenteredBorderItemLocator with the specified parentFigure.
	 * 
	 * @param parentFigure
	 *            the parent figure
	 * @param forcedBorderItemOffset
	 *            unchangeable border item offset.
	 */
	public CorrespondenceBorderItemLocator(IFigure parentFigure, Dimension forcedBorderItemOffset, int forcedSide) {
		super(parentFigure);
		this.forcedBorderItemOffset = forcedBorderItemOffset;
		this.forcedSide = forcedSide;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator#setPreferredSideOfParent(int)
	 */
	@Override
	public void setPreferredSideOfParent(int preferredSide) {
		if(forcedSide != -1) {
			super.setPreferredSideOfParent(forcedSide);
		}
		else {
			super.setPreferredSideOfParent(preferredSide);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator#setCurrentSideOfParent(int)
	 */
	@Override
	public void setCurrentSideOfParent(int side) {
		if(forcedSide != -1) {
			super.setCurrentSideOfParent(forcedSide);
		}
		else {
			super.setCurrentSideOfParent(side);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void relocate(IFigure borderItem) {
		super.relocate(borderItem);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator#setBorderItemOffset(org.eclipse.draw2d.geometry.Dimension)
	 */
	@Override
	public void setBorderItemOffset(Dimension borderItemOffset) {
		if (forcedBorderItemOffset != null) {
			super.setBorderItemOffset(forcedBorderItemOffset);
		} else {
			super.setBorderItemOffset(borderItemOffset);

		}
	}

}
