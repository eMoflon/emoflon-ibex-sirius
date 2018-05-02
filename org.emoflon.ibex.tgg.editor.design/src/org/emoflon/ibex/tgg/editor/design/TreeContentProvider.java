package org.emoflon.ibex.tgg.editor.design;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class TreeContentProvider implements ITreeContentProvider  {
	
	Map<String, List<String>> map;
	
	public TreeContentProvider(Map<String, List<String>> map) {
		this.map = map;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(hasChildren(parentElement)) {
			return map.get(parentElement).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(map.containsKey(element)) {
			return true;
		}
		return false;
	}

}
