package org.emoflon.ibex.tgg.editor.design;

import java.util.ArrayList;
import java.util.List;

import org.moflon.tgg.mosl.tgg.CorrVariablePattern;
import org.moflon.tgg.mosl.tgg.LinkVariablePattern;
import org.moflon.tgg.mosl.tgg.NamedElements;
import org.moflon.tgg.mosl.tgg.ObjectVariablePattern;

/**
 * The services class used by VSM.
 */
public class Services extends CommonServices {
	

	public List<NamedElements> getChildren(NamedElements element) {
		List<NamedElements> children = new ArrayList<NamedElements>();
		if (element instanceof ObjectVariablePattern) {
			List<LinkVariablePattern> links = ((ObjectVariablePattern) element).getLinkVariablePatterns();

			// leaf
			if (links.size() == 0) {
				return children;
			}

			for (LinkVariablePattern link : links) {
				ObjectVariablePattern child = link.getTarget();
				children.add(child);
				children.addAll(getChildren(child));
			}
		} else if (element instanceof CorrVariablePattern) {
			children.addAll(getChildren(((CorrVariablePattern) element).getSource()));
			children.addAll(getChildren(((CorrVariablePattern) element).getTarget()));
		}
		return children;
	}
}
