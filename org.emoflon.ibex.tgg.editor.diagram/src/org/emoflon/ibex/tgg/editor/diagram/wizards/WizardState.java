package org.emoflon.ibex.tgg.editor.diagram.wizards;

abstract class WizardState {

	private boolean done;

	public WizardState() {

	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
