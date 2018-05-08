package org.emoflon.ibex.tgg.editor.wizards;

public abstract class WizardState {

	protected boolean done;

	public WizardState() {

	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
