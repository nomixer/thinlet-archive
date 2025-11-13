package org.thinlet.jme.event;

public class ComponentEvent extends InputEvent {

    /**
     * The first number in the range of ids used for component events.
     */
    public static final int COMPONENT_FIRST		= 100;

    /**
     * The last number in the range of ids used for component events.
     */
    public static final int COMPONENT_LAST		= 103;

   /**
     * This event indicates that the component's position changed.
     */
    public static final int COMPONENT_MOVED	= COMPONENT_FIRST;

    /**
     * This event indicates that the component's size changed.
     */
    public static final int COMPONENT_RESIZED	= 1 + COMPONENT_FIRST;

    /**
     * This event indicates that the component was made visible.
     */
    public static final int COMPONENT_SHOWN	= 2 + COMPONENT_FIRST;

    /**
     * This event indicates that the component was rendered invisible.
     */
    public static final int COMPONENT_HIDDEN	= 3 + COMPONENT_FIRST;

	protected ComponentEvent(int id, int mask) {
		super(id, mask);
	}

}
