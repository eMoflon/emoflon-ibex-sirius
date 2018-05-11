package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

public class TypeFilter extends ViewerFilter {
	private EClass type = null;
	
	public TypeFilter() {
	}
	
	public TypeFilter(EClass type) {
		this.type = type;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ObjectVariablePattern object = (ObjectVariablePattern) element;
		EqualityHelper eq = new EqualityHelper();
		if (type == null || (type != null && eq.equals(type, object.getType())))
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
