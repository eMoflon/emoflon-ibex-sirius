package org.emoflon.ibex.tgg.editor.wizards;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.moflon.tgg.mosl.tgg.NamedElements;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class NamedElementLabelProvider extends BaseLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ObjectVariablePattern) {
			ObjectVariablePattern namedElement = (ObjectVariablePattern) element;
			String label = namedElement.getName() + " : " + namedElement.getType().getName();
			return label;
		} else if (element instanceof ENamedElement) {
			ENamedElement namedElement = (ENamedElement) element;
			return namedElement.getName();
		} else if (element instanceof NamedElements) {
			NamedElements namedElement = (NamedElements) element;
			return namedElement.getName();
		}

		return null;
	}

}
