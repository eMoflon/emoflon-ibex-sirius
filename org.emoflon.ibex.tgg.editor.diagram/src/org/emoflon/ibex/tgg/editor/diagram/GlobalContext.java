package org.emoflon.ibex.tgg.editor.diagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.emoflon.ibex.tgg.editor.tgg.CorrVariablePattern;
import org.emoflon.ibex.tgg.editor.tgg.LinkVariablePattern;
import org.emoflon.ibex.tgg.editor.tgg.NamedElements;
import org.emoflon.ibex.tgg.editor.tgg.ObjectVariablePattern;

// Helps to store and manage the global context of a rule
class GlobalContext {
	
	// Map to store global objects, i.e. the context of a rule
	// K: Object name, V: Set containing all the global objects with that name
	private Map<String, Set<NamedElements>> contextMap;

	public GlobalContext() {
		contextMap = new HashMap<String, Set<NamedElements>>();
	}

	private void add(NamedElements globalObject) {
		if (contextMap.containsKey(globalObject.getName())) {
			Set<NamedElements> s = contextMap.get(globalObject.getName());
			s.add(globalObject);
		} else {
			Set<NamedElements> s = new HashSet<NamedElements>();
			s.add(globalObject);
			contextMap.put(globalObject.getName(), s);
		}
	}

	void addAll(List<? extends NamedElements> globalObjects) {
		globalObjects.stream().forEach(g -> add(g));
	}

	Set<NamedElements> get(String objectName) {
		return contextMap.get(objectName);
	}

	void addContext(GlobalContext otherContext) {
		contextMap.putAll(otherContext.contextMap);
	}

	boolean containsObjectName(String objectName) {
		return contextMap.containsKey(objectName);
	}

	// Return the first object match that has no operator (i.e. black node),
	// if no black node found, then return a green node that matches the given
	// object name.
	private NamedElements getFirstMatch(String objectName) {
		Set<NamedElements> s = contextMap.get(objectName);
		NamedElements match = null;
		if (s == null || s.size() < 1) {
			return match;
		}
		for (NamedElements namedElementObject : s) {
			if (namedElementObject instanceof ObjectVariablePattern) {
				match = namedElementObject;
				if (((ObjectVariablePattern) match).getOp() == null) {
					// black node found in the matches set
					break;
				}
			} else if (namedElementObject instanceof CorrVariablePattern) {
				match = namedElementObject;
				if (((CorrVariablePattern) match).getOp() == null) {
					// black node found in the matches set
					break;
				}
			}
		}
		return match;
	}

	public List<NamedElements> getAllFirstMatches() {
		List<NamedElements> matches = new ArrayList<NamedElements>();
		for (String objectName : contextMap.keySet()) {
			NamedElements match = getFirstMatch(objectName);
			if (match != null) {
				matches.add(match);
			}
		}
		return matches;
	}

	List<LinkVariablePattern> getAllLinksFromObject(ObjectVariablePattern object) {
		List<LinkVariablePattern> links = new ArrayList<LinkVariablePattern>();
		Set<NamedElements> s = contextMap.get(object.getName());
		if (s != null) {
			for (NamedElements namedElementObject : s) {
				if (!(namedElementObject instanceof ObjectVariablePattern)) {
					continue;
				}
				links.addAll(((ObjectVariablePattern) namedElementObject).getLinkVariablePatterns());
			}
		}
		return links;
	}
}
