package org.thinlet.jme.event;

public class FocusEvent extends InputEvent {

	// resized, shownotify

    /**
     * The first number in the range of ids used for focus events.
     */
    public static final int FOCUS_FIRST		= 1004;

    /**
     * The last number in the range of ids used for focus events.
     */
    public static final int FOCUS_LAST		= 1005;

    /**
     * This event indicates that the Component is now the focus owner.
     */
    public static final int FOCUS_GAINED = FOCUS_FIRST; //Event.GOT_FOCUS

    /**
     * This event indicates that the Component is no longer the focus owner.
     */
    public static final int FOCUS_LOST = 1 + FOCUS_FIRST; //Event.LOST_FOCUS

	public FocusEvent(int id, int mask) {
		super(id, mask);
	}

}
