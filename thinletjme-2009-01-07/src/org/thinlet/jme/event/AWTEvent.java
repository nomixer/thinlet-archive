package org.thinlet.jme.event;

public class AWTEvent {

	public static final int RESERVED_ID_MAX = Integer.MAX_VALUE / 2;

	/**
	 * The event id.
	 */

	protected int id;

	public AWTEvent(int id) {

		this.id = id;

	}

	/**
	 * Gets the event id.
	 *
	 * @return an event id.
	 */
	public int getID() {
		return id;
	}

}
