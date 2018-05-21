package org.emoflon.ibex.tgg.editor.diagram.core;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.internal.session.SessionFactoryImpl;
import org.eclipse.sirius.viewpoint.DAnalysis;

public class CustomSessionFactory extends SessionFactoryImpl
		implements org.eclipse.sirius.business.api.session.factory.SessionFactory {

	@Override
	protected Session createSession(DAnalysis analysis, TransactionalEditingDomain transactionalEditingDomain) {
		return new CustomDAnalysisSessionImpl(analysis);
	}

}
