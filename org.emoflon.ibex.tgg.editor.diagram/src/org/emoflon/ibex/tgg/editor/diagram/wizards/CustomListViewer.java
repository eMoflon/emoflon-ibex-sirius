package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.emoflon.ibex.tgg.editor.diagram.ui.NamedElementLabelProvider;

class CustomListViewer extends ListViewer {

	CustomListViewer(Composite parent) {
		super(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		getList().setLayoutData(gd);

		setLabelProvider(new NamedElementLabelProvider());
		setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				@SuppressWarnings("unchecked")
				java.util.List<EObject> l = (java.util.List<EObject>) inputElement;
				return l.toArray();
			}
		});

		setComparator(new ViewerComparator());

	}

}
