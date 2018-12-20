package org.emoflon.ibex.tgg.editor.diagram.wizards;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.emoflon.ibex.tgg.editor.diagram.ui.NamedElementLabelProvider;

class CustomListViewer extends ListViewer {
	
	private int listHeight = 8;

	CustomListViewer(Composite parent) {
		super(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		gd.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, listHeight);
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

	public int getListHeight() {
		return listHeight;
	}

	public void setListHeight(int listHeight) {
		this.listHeight = listHeight;
	}

}
