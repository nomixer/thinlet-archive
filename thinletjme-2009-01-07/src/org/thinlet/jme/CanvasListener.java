package org.thinlet.jme;

import javax.microedition.lcdui.Displayable;

public interface CanvasListener {

	void hideNotify(Displayable displayable);

	void showNotify(Displayable displayable);

	void paintNotify(Displayable displayable);

}
