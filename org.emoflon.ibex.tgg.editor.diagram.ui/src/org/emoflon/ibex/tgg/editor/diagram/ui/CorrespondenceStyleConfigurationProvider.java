package org.emoflon.ibex.tgg.editor.diagram.ui;

import org.eclipse.sirius.diagram.description.DiagramElementMapping;
import org.eclipse.sirius.diagram.description.NodeMapping;
import org.eclipse.sirius.diagram.ui.tools.api.graphical.edit.styles.IStyleConfigurationProvider;
import org.eclipse.sirius.diagram.ui.tools.api.graphical.edit.styles.StyleConfiguration;
import org.eclipse.sirius.viewpoint.Style;

public class CorrespondenceStyleConfigurationProvider implements IStyleConfigurationProvider {

	@Override
	public StyleConfiguration createStyleConfiguration(DiagramElementMapping mapping, Style style) {
		if (mapping instanceof NodeMapping) {
			NodeMapping nodeMapping = (NodeMapping) mapping;
			if (nodeMapping.getName() != null && (nodeMapping.getName()
					.equals(TGGSiriusEditorConstants.CORRESPONDENCE_MAPPING_NAME)
					|| nodeMapping.getName().equals(TGGSiriusEditorConstants.GLOBAL_CORRESPONDENCE_MAPPING_NAME)
					|| nodeMapping.getName().equals(TGGSiriusEditorConstants.CORRESPONDENCE_CMPL_MAPPING_NAME))) {
				return CorrespondenceStyleConfiguration.getInstance();
			}
		}
		return null;
	}

	@Override
	public boolean provides(DiagramElementMapping mapping, Style style) {
		if (mapping instanceof NodeMapping) {
			NodeMapping nodeMapping = (NodeMapping) mapping;
			if (nodeMapping.getName() != null && (nodeMapping.getName()
					.equals(TGGSiriusEditorConstants.CORRESPONDENCE_MAPPING_NAME)
					|| nodeMapping.getName().equals(TGGSiriusEditorConstants.GLOBAL_CORRESPONDENCE_MAPPING_NAME)
					|| nodeMapping.getName().equals(TGGSiriusEditorConstants.CORRESPONDENCE_CMPL_MAPPING_NAME))) {
				return true;
			}
		}

		return false;
	}

}
