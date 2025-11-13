package thinlet.examples.demo;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import thinlet.*;

/**
 *
 */
public class Demo extends Thinlet {

	/**
	 *
	 */
	public Demo() {
		try {
			add(parse("demo.xml"));
		} catch (Exception exc) { exc.printStackTrace(); }
	}
	
	/**
	 *
	 */
	public static void main(String[] args) {
		new FrameLauncher("Demo", new Demo(), 320, 320);
	}

	boolean textinit;
	boolean valueinit;

	/**
	 *
	 */
	public void tabChanged() {
		int selected = getInteger(find("tabbedpane"), "selected");
		if ((selected == 0) && !textinit) {
			try {
				InputStream inputstream = null;
				try {
					inputstream = getClass().getResourceAsStream("dialog.xml");
				} catch (Throwable e) {}
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
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
				setString(find("textarea"), "text", text.toString());
				textinit = true;
			} catch (Exception exc) { getToolkit().beep(); }
		}
		else if ((selected == 2) && !valueinit) {
			hsbChanged();
			valueinit = true;
		}
	}


	/**
	 *
	 */
	public void changeEditable() {
		boolean editable = getBoolean(find("cb_editable"), "selected");
		setBoolean(find("textarea"), "editable", editable);
	}

	/**
	 *
	 */
	public void changeEnabled() {
		boolean enabled = getBoolean(find("cb_enabled"), "selected");
		setBoolean(find("textarea"), "enabled", enabled);
	}

	Object dialog;

	/**
	 *
	 */
	public void showDialog() {
		try {
			if (dialog == null) {
				dialog = parse("dialog.xml");
			}
			add(dialog);
			setBoolean(find("b_finddialog"), "enabled", false);
			//requestFocus(find(dialog, "b_find"));
		} catch (Exception exc) { exc.printStackTrace(); }
	}

	/**
	 *
	 */
	public void findText() {
		closeDialog();
		Object combobox = find(dialog, "ch_what");
		String what = getString(combobox, "text");
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

		boolean match = getBoolean(find(dialog, "cb_match"), "selected");
		if (!match) {
			what = what.toLowerCase();
			text = text.toLowerCase();
		}

		boolean down = getBoolean(find(dialog, "rb_down"), "selected");
		int index = text.indexOf(what, down ? end : 0);
		if (!down && (index != -1) && (index >= end)) { index = -1; }
		if (index != -1) {
			setInteger(textarea, "start", index);
			setInteger(textarea, "end", index + what.length());
			requestFocus(textarea);
		}
		else {
			getToolkit().beep();
		}
	}

	/**
	 *
	 */
	public void closeDialog() {
		remove(dialog);
		Object button = find("b_finddialog");
		setBoolean(button, "enabled", true);
		requestFocus(button);
	}
	
	/**
	 *
	 */
	public void insertList() {
		Object item = create("item");
		setString(item, "text", "New item");
		setIcon(item, "icon", getIcon("/icons/bookmarks.gif"));
		add(find("list"), item, 0);
	}

	/**
	 *
	 */
	public void deleteList() {
		Object list = find("list");
		for (int i = getCount(list) - 1; i >= 0; i--) {
			Object item = getItem(list, i);
			if (getBoolean(item, "selected")) {
				remove(item);
			}
		}
	}

	/**
	 *
	 */
	public void setSelection() {
		Object list = find("list");
		for (int i = getCount(list) - 1; i >= 0; i--) {
			setBoolean(getItem(list, i), "selected", false);
		}
		setChoice(list, "selection", getString(find("selection"), "text"));
	}

	public void redSlider() { sliderChanged("red"); }
	public void redSpinbox() { spinboxChanged("red"); }
	public void greenSlider() { sliderChanged("green"); }
	public void greenSpinbox() { spinboxChanged("green"); }
	public void blueSlider() { sliderChanged("blue"); }
	public void blueSpinbox() { spinboxChanged("blue"); }

	/**
	 *
	 */
	private void sliderChanged(String color) {
		int value = getInteger(find("sl_" + color), "value");
		setString(find("sb_" + color), "text", String.valueOf(value));
		hsbChanged();
	}
	
	/**
	 *
	 */
	private void spinboxChanged(String color) {
		try {
			int value = Integer.parseInt(getString(find("sb_" + color), "text"));
			if ((value >= 0) && (value <= 255)) {
				setInteger(find("sl_" + color), "value", value);
				hsbChanged();
			}
		} catch (NumberFormatException nfe) { getToolkit().beep(); }
	}

	/**
	 *
	 */
	private void hsbChanged() {
		int red = getInteger(find("sl_red"), "value");
		int green = getInteger(find("sl_green"), "value");
		int blue = getInteger(find("sl_blue"), "value");
		
		float[] hsb = Color.RGBtoHSB(red, green, blue, null);

		setString(find("tf_hue"), "text", String.valueOf(hsb[0]));
		setString(find("tf_saturation"), "text", String.valueOf(hsb[1]));
		setString(find("tf_brightness"), "text", String.valueOf(hsb[2]));

		setInteger(find("pb_hue"), "value", (int) (100f * hsb[0]));
		setInteger(find("pb_saturation"), "value", (int) (100f * hsb[1]));
		setInteger(find("pb_brightness"), "value", (int) (100f * hsb[2]));
	}
	
	Hashtable name2component = new Hashtable();

	/**
	 *
	 */
	public Object find(String name) {
		Object component = name2component.get(name);
		if (component == null) {
			component = super.find(name);
			name2component.put(name, component);
		}
		return component;
	}
}