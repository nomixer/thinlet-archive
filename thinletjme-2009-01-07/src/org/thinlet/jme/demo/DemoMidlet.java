package org.thinlet.jme.demo;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.thinlet.jme.CanvasListener;
import org.thinlet.jme.ImageLoader;
import org.thinlet.jme.Thinlet;

/**
 *
 * The MIDlet responsable for initiating the display and launching the {@link org.thinlet.jme.Thinlet Thinlet} subclass {@link thinlet.Demo Demo} instance.
 *
 * @author Thomas Rørvik Skjølberg
 *
 */

public class DemoMidlet extends MIDlet implements Runnable, CanvasListener, CommandListener, ImageLoader {

	private Demo console = null;

	private Hashtable images = new Hashtable();

	private boolean started = false;
	private boolean shown = false;

	private boolean painted = false;

	private Command exit = null;

	protected void startApp() throws MIDletStateChangeException {
		if(!started) {
			started = false;
			new Thread(this).start();
		}
	}

	protected void pauseApp() {
		// do nothing
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		notifyDestroyed();
	}

	public void run() {

		Display display = Display.getDisplay(this);

		try {

			console = new Demo(display, this);

			console.setFullScreenMode(true);

			Object desktop = console.parse("/demo.xml");

			console.add(desktop);

			console.setCanvasListener(this);

			display.setCurrent(console);

			long delay = System.currentTimeMillis() + 10000;

			while(delay > System.currentTimeMillis() && (!painted || !shown)) {
				Thread.yield();
			}

			console.setNextFocusable(desktop, false);
		} catch (Exception e) {
			e.printStackTrace();

			Form form = new Form("Startup problem");
			form.append(e.getClass().getName() + "\n");
			form.append(e.getMessage() + "\n");

			exit = new Command("Exit", Command.EXIT, 0);

			form.setCommandListener(this);
			form.addCommand(exit);

			display.setCurrent(form);
		}

	}

	public Image getImage(String url) {
		if(images.containsKey(url)) {
			return (Image) images.get(url);
		}
		Image image = Thinlet.getLocalImage(url);

		if(image != null) {
			images.put(url, image);

			return image;

		} else {
			System.out.println("Could not load image " + url);
			return null;
		}
	}

	public void hideNotify(Displayable displayable) {
		painted = false;
		shown = false;
	}

	public void showNotify(Displayable displayable) {

		if(!shown) {
			shown = true;
		}

	}

	public void paintNotify(Displayable displayable) {
		if(!painted) {
			painted = true;
		}
	}

	public void commandAction(Command c, Displayable arg1) {
		if(c == exit) {
			notifyDestroyed();
		}
	}


}
