package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.emoflon.ibex.tgg.editor.tgg.ObjectVariablePattern;

class TypeFilter extends ViewerFilter {
	private EClass type = null;
	
	public TypeFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ObjectVariablePattern object = (ObjectVariablePattern) element;
		if (type == null || (type != null && type.isSuperTypeOf(object.getType())))
			return true;
		else
			return false;
	}

	public EClass getType() {
		return type;
	}

	public void setType(EClass type) {
		this.type = type;
	}

}
