package org.thinlet.jme.demo;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

import org.thinlet.jme.Thinlet;

/**
 *
 * Simple demonstration of widgets and events
 *
 */

public class Demo extends Thinlet {

	private Display display;

	private MIDlet midlet;

	private Object dialog;

	private Object sl_red, sl_green, sl_blue;
	private Object tf_hue, tf_saturation, tf_brightness;
	private Object pb_hue, pb_saturation, pb_brightness;
	private Object rgb_label;

	public Demo(Display display, MIDlet midlet) throws Exception {
		this.display = display;

		this.midlet = midlet;
	}

	protected void select() {
	}

	public void exit() {
		midlet.notifyDestroyed();
	}

	/**
	 * Called if the demo.xml was loaded,
	 * it fills the textarea from a resource file
	 */
    public void loadText(Object textarea, String path) throws Exception {
        Reader reader = new InputStreamReader(
            getClass().getResourceAsStream(path));
        StringBuffer text = new StringBuffer();
        for (int c = reader.read(); c != -1; c = reader.read()) {
            if (((c > 0x1f) && (c < 0x7f)) ||
                    ((c > 0x9f) && (c < 0xffff)) || (c == '\n')) {
                text.append((char) c);
            }
            else if (c == '\t') {
                text.append("  ");
            }
        }
        reader.close();

	 	setString(textarea, "text", text.toString());
	}

	/**
	 * Updates textarea's editable property depending on a checkbox state
	 */
	public void changeEditable(boolean editable, Object textarea) {
		setBoolean(textarea, "editable", editable);
	}

	/**
	 * Updates textarea's enabled property
	 */
	public void changeEnabled(boolean enabled, Object textarea) {
		setBoolean(textarea, "enabled", enabled);
	}

	/**
	 * Shows the modal find dialog, creates only one dialog instance
	 */
	public void showDialog() throws Exception {
		if (dialog == null) {
			dialog = parse("/demodialog.xml");
		}
		add(dialog);
	}

	/**
	 * Updates the textarea's selection range,
	 * and add the search string to the history
	 */
	public void findText(Object combobox, String what, boolean match, boolean down) {
		closeDialog();
		if (what.length() == 0) { return; }

		boolean cacheditem = false;
		for (int i = getCount(combobox) - 1; i >= 0; i--) {
			String choicetext = getString(getItem(combobox, i), "text");
			if (what.equals(choicetext)) { cacheditem = true; break; }
		}
		if (!cacheditem) {
			Object choice = create("choice");
			setString(choice, "text", what);
			add(combobox, choice);
		}

		Object textarea = find("textarea");
		int end = getInteger(textarea, "end");
		String text = getString(textarea, "text");

		if (!match) {
			what = what.toLowerCase();
			text = text.toLowerCase();
		}

		int index = text.indexOf(what, down ? end : 0);
		if (!down && (index != -1) && (index >= end)) { index = -1; }
		if (index != -1) {
			setInteger(textarea, "start", index);
			setInteger(textarea, "end", index + what.length());
			requestFocus(textarea);
		}
		else {
			display.vibrate(1000);
		}
	}

	/**
	 * Closes the dialog
	 */
	public void closeDialog() {
		remove(dialog);
	}

	/**
	 * Insert a new item into the list
	 */
	public void insertList(Object list) {
		Object item = create("item");
		setString(item, "text", "New item");
		setIcon(item, "icon", getIcon("/icon/library.gif"));
		add(list, item, 0);
	}

	/**
	 * Removes the selected items from the list
	 */
	public void deleteList(Object delete, Object list) {
		for (int i = getCount(list) - 1; i >= 0; i--) {
			Object item = getItem(list, i);
			if (getBoolean(item, "selected")) {
				remove(item);
			}
		}
		setBoolean(delete, "enabled", false);
	}

	/**
	 * Delete button's state depends on the list selection
	 */
	public void changeSelection(Object list, Object delete) {
		setBoolean(delete, "enabled", getSelectedIndex(list) != -1);
	}

	/**
	 * Clears list selection and updates the selection model
	 */
	public void setSelection(Object list, String selection, Object delete) {
		for (int i = getCount(list) - 1; i >= 0; i--) {
			setBoolean(getItem(list, i), "selected", false);
		}
		setChoice(list, "selection", selection);
		setBoolean(delete, "enabled", false);
	}

	public void sliderChanged(int value, Object spinbox) {
		setString(spinbox, "text", String.valueOf(value));
		hsbChanged();
	}

	public void spinboxChanged(String text, Object slider) {
		try {
			int value = Integer.parseInt(text);
			if ((value >= 0) && (value <= 255)) {
				setInteger(slider, "value", value);
				hsbChanged();
			}
		} catch (NumberFormatException nfe) {
			display.vibrate(1000);
		}
	}

	public void storeWidgets(Object sl_red, Object sl_green, Object sl_blue,
			Object tf_hue, Object tf_saturation, Object tf_brightness,
			Object pb_hue, Object pb_saturation, Object pb_brightness, Object rgb_label) {
		this.sl_red = sl_red;
		this.sl_green = sl_green;
		this.sl_blue = sl_blue;
		this.tf_hue = tf_hue;
		this.tf_saturation = tf_saturation;
		this.tf_brightness = tf_brightness;
		this.pb_hue = pb_hue;
		this.pb_saturation = pb_saturation;
		this.pb_brightness = pb_brightness;
		this.rgb_label = rgb_label;
	}

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
    	float hue, saturation, brightness;
    	if (hsbvals == null) {
    	    hsbvals = new float[3];
    	}
        	int cmax = (r > g) ? r : g;
    	if (b > cmax) cmax = b;
    	int cmin = (r < g) ? r : g;
    	if (b < cmin) cmin = b;

    	brightness = ((float) cmax) / 255.0f;
    	if (cmax != 0)
    	    saturation = ((float) (cmax - cmin)) / ((float) cmax);
    	else
    	    saturation = 0;
    	if (saturation == 0)
    	    hue = 0;
    	else {
    	    float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
    	    float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
    	    float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
    	    if (r == cmax)
    		hue = bluec - greenc;
    	    else if (g == cmax)
    	        hue = 2.0f + redc - bluec;
                else
    		hue = 4.0f + greenc - redc;
    	    hue = hue / 6.0f;
    	    if (hue < 0)
    		hue = hue + 1.0f;
    	}
    	hsbvals[0] = hue;
    	hsbvals[1] = saturation;
    	hsbvals[2] = brightness;
    	return hsbvals;
    }

	private void hsbChanged() {
		int red = getInteger(sl_red, "value");
		int green = getInteger(sl_green, "value");
		int blue = getInteger(sl_blue, "value");

		float[] hsb = RGBtoHSB(red, green, blue, null);

		setString(tf_hue, "text", String.valueOf(hsb[0]));
		setString(tf_saturation, "text", String.valueOf(hsb[1]));
		setString(tf_brightness, "text", String.valueOf(hsb[2]));

		setInteger(pb_hue, "value", (int) (100f * hsb[0]));
		setInteger(pb_saturation, "value", (int) (100f * hsb[1]));
		setInteger(pb_brightness, "value", (int) (100f * hsb[2]));

		setInteger(rgb_label, "background", ((red << 16) & 0x00FF0000) + ((green << 8) & 0x0000FF00) + blue);
		setInteger(rgb_label, "foreground", (((255 - red) << 16) & 0x00FF0000) | (((255 - green) << 8) & 0x0000FF00) | (255 - blue));
	}

	/**
	 *
	 * Java Micro Edition reflection via String compare and method array.
	 *
	 */

	protected void invokeImpl(Object component, String method, Object[] data) throws Exception {
		if(method.equals("storeWidgets")) {
			storeWidgets(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
		} else if(method.equals("sliderChanged")) {
			sliderChanged(Integer.parseInt(data[0].toString()), data[1]);
		} else if(method.equals("spinboxChanged")) {
			spinboxChanged(data[0].toString(), data[1]);
		} else if(method.equals("loadText")) {
			loadText(data[0], (String) data[1]);
		} else if(method.equals("changeEnabled")) {
			changeEnabled(data[0].equals(Boolean.TRUE), data[1]);
		} else if(method.equals("changeEditable")) {
			changeEditable(data[0].equals(Boolean.TRUE), data[1]);
		} else if(method.equals("showDialog")) {
			showDialog();
		} else if(method.equals("closeDialog")) {
			closeDialog();
		} else if(method.equals("findText")) {
			findText(data[0], (String) data[1], data[2].equals(Boolean.TRUE), data[3].equals(Boolean.TRUE));
		} else if(method.equals("setSelection")) {
			setSelection(data[0], (String) data[1], data[2]);
		} else if(method.equals("insertList")) {
			insertList(data[0]);
		} else if(method.equals("deleteList")) {
			deleteList(data[0], data[1]);
		} else if(method.equals("changeSelection")) {
			changeSelection(data[0], data[1]);
		} else if(method.equals("exit")) {
			exit();
		} else throw new RuntimeException("Unknown method signature " + spyMethod(method, data).toString());
	}

   protected void keyPressed(int keyCode) {
	    // example of filter for non-standard buttons
	    // for multi-screen purposes, transition logic should be implemented as a state machine.
        if(isKey(keyCode, LEFT_SOFT)) {
        	leftSoftKeyPressed();
        } else if(isKey(keyCode, CENTER_SOFT)) {
        	centerSoftKeyPressed();
        } else if(isKey(keyCode, RIGHT_SOFT)) {
        	rightSoftKeyPressed();
        } else {
            super.keyPressed(translateKey(keyCode));
        }
    }

	/**
   *
   * Example of soft button behavior.
   *
   */

    private void centerSoftKeyPressed() {
		System.out.println("Center soft button pressed");

		midlet.notifyDestroyed();
    }

	/**
     *
     * Example of soft button behavior.
     *
     */

	private void rightSoftKeyPressed() {
		System.out.println("Right soft button pressed");

		midlet.notifyDestroyed();
	}

    /**
     *
     * Example of soft button behavior.
     *
     */

	private void leftSoftKeyPressed() {
		System.out.println("Left soft button pressed");

		midlet.notifyDestroyed();
	}

}