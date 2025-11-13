package org.thinlet.jme.event;

public class InputEvent extends AWTEvent {

	public static final int ALT_MASK = 0;
	int mask;

	protected InputEvent(int id, int mask) {
		super(id);
		this.mask = mask;
	}

	public boolean isShiftDown() {
		return false;
	}

	public boolean isControlDown()  {
		return false;
	}

	public boolean isMetaDown() {
		return false;
	}

	public int getModifiers() {
		return mask;
	}

	// linje 6096 TODO

	public static int getField(String string) {
		return 0;
	}

}
