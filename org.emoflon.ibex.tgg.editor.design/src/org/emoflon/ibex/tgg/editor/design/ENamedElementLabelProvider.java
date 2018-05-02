package org.emoflon.ibex.tgg.editor.design;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class ENamedElementLabelProvider extends BaseLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof ENamedElement) {
			ENamedElement namedElement = (ENamedElement)element;
			
			return namedElement.getName();
		}
		
		return null;
	}


}
