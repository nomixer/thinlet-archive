package org.thinlet.jme;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.Sprite;

import org.kxml2.nonamespace.XmlReader;
import org.thinlet.jme.event.AWTEvent;
import org.thinlet.jme.event.ComponentEvent;
import org.thinlet.jme.event.Cursor;
import org.thinlet.jme.event.FocusEvent;
import org.thinlet.jme.event.InputEvent;
import org.thinlet.jme.event.KeyEvent;
import org.thinlet.jme.event.MouseEvent;

/**
 * Thinlet is an XUI (XML UI) implemented using the anti-pattern of a single big class instead of a series of small classes. <br/>
 *<br/>
 * Thinlet is suitable for low-footprint applications with low- to medium-complex user interface interaction.<br/>
 * <br/>
 * Thinlet was originally developed by Robert Bajzat (rbajzat at freemail.hu), and modified for Java Micro Edition by Thomas Rørvik Skjølberg (thomas.skjolberg at adactus.no).<br/>
 * <br/>
 */

public abstract class Thinlet extends Canvas implements Runnable { // change to GameCanvas for increased performance.

	public static final int BLUE = 0x000000FF;
	public static final int GREEN = 0x0000FF00;
	public static final int RED = 0x00FF0000;
	public static final int ALPHA = 0xFF000000;
	public static final int CYAN = 0xFF00FFFF;

	protected final static char[][] numKeysNokia = new char[][] { // case key: Canvas.KEY_POUND
			new char[] { '.', '.', '-', '?', '!', '1', '\'', '@', ':', ';', '(', ')', '&', '%', '<', '>', '=', '\"', '*', '/', '$', '€', '£', '_', '[', ']',
					'{', '}' }, new char[] { 'a', 'b', 'c', 'å', 'ä', 'æ', '2' }, new char[] { 'd', 'e', 'f', '3' }, new char[] { 'g', 'h', 'i', '4' },
			new char[] { 'j', 'k', 'l', '5' }, new char[] { 'm', 'n', 'o', 'ø', '6' }, new char[] { 'p', 'q', 'r', 's', '7' },
			new char[] { 't', 'u', 'v', '8' }, new char[] { 'w', 'x', 'y', 'z', '9' }, new char[] { '*', '+' }, new char[] { ' ', '0' }, new char[] { '#' }, };

	protected static char[][] numKeys = numKeysNokia;

	protected final static char[][] numKeysSonyEricsson = new char[][] { // case key: Canvas.KEY_STAR
			new char[] { '.', '.', '-', '?', '!', '1', '\'', '@', ':', ';', '(', ')', '&', '%', '<', '>', '=', '\"', '*', '/', '$', '€', '£', '_', '[', ']',
					'{', '}' }, new char[] { 'a', 'b', 'c', 'å', 'ä', 'æ', '2' }, new char[] { 'd', 'e', 'f', '3' }, new char[] { 'g', 'h', 'i', '4' },
			new char[] { 'j', 'k', 'l', '5' }, new char[] { 'm', 'n', 'o', 'ø', '6' }, new char[] { 'p', 'q', 'r', 's', '7' },
			new char[] { 't', 'u', 'v', '8' }, new char[] { 'w', 'x', 'y', 'z', '9' }, new char[] { '*' }, new char[] { '+', '0' }, new char[] { ' ', '#' }, };

	public static Image getLocalImage(String path) {
		Image image = null;
		try {
			image = Image.createImage(path);
		} catch (IOException ioe) {
		}

		if (image != null) {
			try {
				InputStream is = Thinlet.class.getResourceAsStream(path);
				if (is != null) {
					image = Image.createImage(is);
					is.close();
				}
			} catch (Throwable e) {
			}
		}
		return image;
	}

	public static int getBlue(int color) {
		return (color & 0x0000ff);
	}

	public static int getGreen(int color) {
		return (color & 0x00ff00) >> 8;
	}

	public static int getRed(int color) {
		return (color & 0xff0000) >> 16;
	}

	public static int getAlpha(int color) {
		return (color & 0xff000000) >> 24;
	}

	public static int brighter(int color) {
		return offset(color, 10);
	}

	public static int darker(int color) {
		return offset(color, -10);
	}

	public static int offset(int color, int delta) {
		return ((((getGreen(color) + 10) & GREEN) << 8) + ((getBlue(color) + 10) & BLUE) + (((getRed(color) + 10) & RED) << 16) + (((getAlpha(color) + 10) & ALPHA) << 24));
	}

	// mobile keys
	protected static int lastKey;
	protected static int lastKeyIndex;
	protected static long lastKeyTimestamp = 0;
	protected static long keyDelay = 1000;
	protected static boolean lowerCase = false;
	/** the key for toggling upper- and lowercase input */
	protected static int caseKey = Canvas.KEY_POUND;

	public static char[] getNumKeyCharMapping(int key) {
		switch (key) {
		case Canvas.KEY_NUM1:
			return numKeys[0];
		case Canvas.KEY_NUM2:
			return numKeys[1];
		case Canvas.KEY_NUM3:
			return numKeys[2];
		case Canvas.KEY_NUM4:
			return numKeys[3];
		case Canvas.KEY_NUM5:
			return numKeys[4];
		case Canvas.KEY_NUM6:
			return numKeys[5];
		case Canvas.KEY_NUM7:
			return numKeys[6];
		case Canvas.KEY_NUM8:
			return numKeys[7];
		case Canvas.KEY_NUM9:
			return numKeys[8];
		case Canvas.KEY_STAR:
			return numKeys[9];
		case Canvas.KEY_NUM0:
			return numKeys[10];
		case Canvas.KEY_POUND:
			return numKeys[11];
		}
		return null;
	}

	// key indexes
	public static final int UP = 0;
	public static final int DOWN = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int ACTION = 4;
	public static final int LEFT_SOFT = 5;
	public static final int RIGHT_SOFT = 6;
	public static final int C = 7;
	public static final int BACK = 8;
	public static final int CENTER_SOFT = 9;

	public static final int UNKNOWN_KEY = Integer.MIN_VALUE;

	protected int[] keys = null; // up, down, left, right, action, left soft, right soft, c, back, soft left

	public boolean isKey(int systemKey, int ourKey) {
		return keys[ourKey] == systemKey;
	}
	/**
	 *
	 * Try to auto-detect key settings based on a system property
	 *
	 */
	public void keys() {
		String str = System.getProperty("microedition.platform");
		if (str != null) {
			str = str.toLowerCase();
			if (str.indexOf("sonyericsson") != -1 || str.indexOf("nokia") != -1) {

				if (str.indexOf("m600") != -1 || str.indexOf("p990") != -1) {
					keys = new int[] { -1, -2, -3, -4, -5, -6, -20, -8, -11, -7 };
				} else {
					keys = new int[] { -1, -2, -3, -4, -5, -6, -7, -8, -11, UNKNOWN_KEY };
				}
			} else if (str.indexOf("samsung") != -1 || str.equals("j2me") /*Samsung D600, A900*/) {
				keys = new int[] { -1, -2, -3, -4, -5, -6, -7, -8, UNKNOWN_KEY, UNKNOWN_KEY };
				System.out.println("Using Samsung key definitions (" + str + ")");
			} else if (str.indexOf("sie-s75") != -1 || str.indexOf("sie-sl75") != -1) {
				keys = new int[] { -59, -60, -61, -62, -26, -1, -4, UNKNOWN_KEY, UNKNOWN_KEY, UNKNOWN_KEY };
				System.out.println("Using Siemens S75 key definitions (" + str + ")");
			} else if (str.indexOf("siemens") != -1 || str.indexOf("sxg75") != -1) {
				keys = new int[] { -1, -2, -3, -4, -5, -6, -7, -8, UNKNOWN_KEY, UNKNOWN_KEY };
				System.out.println("Using Siemens key definitions (" + str + ")");

			} else if (str.indexOf("sunmicrosystems_wtk") != -1 || str.indexOf("MM-9000") != -1 || str.indexOf("A-900") != -1) {
				keys = new int[] { -1, -2, -3, -4, -5, -6, -7, UNKNOWN_KEY, -8, UNKNOWN_KEY };
				System.out.println("Using Sprint key definitions (" + str + ")");
			} else {
				// MOTOROLA and Nokia SDK and Samsung uses 'j2me' !!
				keys = new int[] { -1, -6, -2, -5, -20, -21, -22, -8, -11, UNKNOWN_KEY };

				// samsung sgh-d807
				//keys = new int[]{-1, -2, -3, -4, -5, -6, -7, -8, UNKNOWN_KEY, UNKNOWN_KEY};

				System.out.println("Using Motorola key definitions (" + str + ")");
			}
		} else {
			keys = new int[] { -1, -2, -3, -4, -5, -6, -7, -8, -11, UNKNOWN_KEY };
		}
	}

	/**
	 *
	 * Translate the directional and fire keys to the Canvas defaults.
	 *
	 * @param key
	 * @return
	 */

	public int translateKey(int key) {
		if (key == Canvas.DOWN || key == GameCanvas.DOWN_PRESSED || key == keys[DOWN]) {
			key = Canvas.DOWN;
		} else if (key == Canvas.UP || key == GameCanvas.UP_PRESSED || key == keys[UP]) {
			key = Canvas.UP;
		} else if (key == Canvas.RIGHT || key == GameCanvas.RIGHT_PRESSED || key == keys[RIGHT]) {
			key = Canvas.RIGHT;
		} else if (key == Canvas.LEFT || key == GameCanvas.LEFT_PRESSED || key == keys[LEFT]) {
			key = Canvas.LEFT;
		} else if (key == Canvas.FIRE || key == GameCanvas.FIRE_PRESSED || key == keys[ACTION]) {
			key = Canvas.FIRE;
		}
		return key;
	}

	protected Font font;

	protected int c_bg;

	protected int c_text;

	protected int c_textbg;

	protected int c_border;

	protected int c_disable;

	protected int c_hover;

	protected int c_press;

	protected int c_focus;

	protected int c_select;

	protected int c_ctrl;

	protected int block;

	protected Image hgradient, vgradient;

	protected Thread timer;

	protected long watchdelay;

	protected long watch;

	protected String clipboard;

	// enter the starting characters of a list item text within a short time to
	// select
	protected String findprefix = "";

	protected long findtime;

	protected Object content = createImpl("desktop");

	protected Object mouseinside;

	protected Object insidepart;

	protected Object mousepressed;

	protected Object pressedpart;

	protected int referencex, referencey;

	protected int mousex, mousey;

	protected Object focusowner;

	protected boolean focusinside;

	protected Object popupowner;

	protected Object tooltipowner;

	protected Object attentionReference;

	protected static final int DRAG_ENTERED = AWTEvent.RESERVED_ID_MAX + 1;

	protected static final int DRAG_EXITED = AWTEvent.RESERVED_ID_MAX + 2;

	protected static long WHEEL_MASK = 0;

	protected static int MOUSE_WHEEL = 0;

	protected static Object[] TXT_AA, G_AA;

	/** attention fields */
	protected Rectangle attentionRect = null;
	protected int attentionSpan = 20;
	protected int attentionProgress = 0;
	protected static int attentionDelta = 2; // this should be an natural integer multiple of attentionSpan
	protected static int attentionThickness = 3; // rectangle thickness

	/** For the pointer helper */
	protected Image pointer = null;
	protected Rectangle pointerRect = null;
	/** for grey-shading of background graphics */
	protected Rectangle inverseShadow = null;

	/** Helper interface for application display states. Remove this to tighten the footprint even further. */
	protected CanvasListener canvasListener;

	/** helper interface for plug-in image buffer. If not set, no caching is done, and only local path (resource) URLs load. Remove this to tighten the footprint even further.*/
	protected ImageLoader loader;

	protected static int evm = 0;

	static {
		WHEEL_MASK = 0; // AWTEvent.class.getField("MOUSE_WHEEL_EVENT_MASK").getLong(null);
		MOUSE_WHEEL = 1; //MouseEvent.class.getField("MOUSE_WHEEL").getInt(null);

		// EVM has larger fillRect, fillOval, and drawImage(part), others are
		// correct
		// contributed by Ibsen Ramos-Bonilla and AK
		try {
			if ((System.getProperty("java.vendor").indexOf("Insignia") != -1) && System.getProperty("os.name").indexOf("Windows CE") == -1) {
				evm = -1;
			}
		} catch (Exception exc) { /* never */
		}
	}

	public Thinlet() { // fixed by Mike Hartshorn (javac1.1 bug)
		keys();

		setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));

		// original colors
		int background = 0xe6e6e6;
		int text = 0x000000;
		int textbackground = 0xffffff;
		int border = 0x909090;
		int disable = 0xAAb0b0b0;
		int hover = 0xededed;
		int press = 0xb9b9b9;
		int focus = 0x89899a;
		int select = 0xc5c5dd;

		/*
		int background = 0xbac0cc;
		int text = 0xFFFFFF;
		int textbackground = 0x6078c0;
		int border = 0x39438a;
		int disable = 0xAAb0b0b0;
		int hover = 0x7890c0;
		int press = 0x7890c0;
		int focus = 0x304890;
		int select = 0x304890;
		 */

		setColors(background, text, textbackground, border, disable, hover, press, focus, select);

		pointer = getIcon("/icon/pointer.png", false);
	}

	/**
	 * Sets the 9 colors used for components, and repaints the whole UI
	 *
	 * @param background
	 *            the backround of panels (dialogs, desktops), and disabled
	 *            controls, not editable texts, lines between list items (the
	 *            default value if <i>#e6e6e6</i>)
	 * @param text
	 *            for text, arrow foreground (<i>black</i> by default)
	 * @param textbackground
	 *            the background of text components, and lists (<i>white</i>
	 *            by default)
	 * @param border
	 *            for outer in inner borders of enabled components (<i>#909090</i>
	 *            by default)
	 * @param disable
	 *            for text, border, arrow color in disabled components (<i>#b0b0b0</i>
	 *            by default)
	 * @param hover
	 *            indicates that the mouse is inside a button area (<i>#ededed</i>
	 *            by default)
	 * @param press
	 *            for pressed buttons, gradient image is calculated using the
	 *            background and this press color (<i>#b9b9b9</i> by default)
	 * @param focus
	 *            for text caret and rectagle color marking the focus owner (<i>#89899a</i>
	 *            by default)
	 * @param select
	 *            used as the background of selected text, and list items, and
	 *            in slider (<i>#c5c5dd</i> by default)
	 */

	public void setColors(int background, int text, int textbackground, int border, int disable, int hover, int press, int focus, int select) {
		c_bg = (background);
		c_text = (text);
		c_textbg = (textbackground);
		c_border = (border);
		c_disable = (disable);
		c_hover = (hover);
		c_press = (press);
		c_focus = (focus);
		c_select = (select);
		hgradient = vgradient = null;
		repaint();
	}

	/**
	 * Sets the only one font used everywhere, and revalidates the whole UI.
	 * Scrollbar width/height, spinbox, and combobox button width, and slider
	 * size is the same as the font height
	 *
	 * @param font
	 */

	public void setFont(Font font) {
		block = font.getHeight();
		this.font = font;
		hgradient = vgradient = null;
		if (content != null)
			validate(content);
	}

	protected void doLayout(Object component) {
		String classname = getClass(component);
		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Image icon = getIcon(component, "icon", null);
				layoutField(component, block, false, (icon != null) ? icon.getWidth() : 0);
			} // set editable -> validate (overwrite textfield repaint)
			else {
				int selected = getInteger(component, "selected", -1);
				if (selected != -1) { // ...
					Object choice = getItem(component, selected);
					set(component, "text", get(choice, "text"));
					set(component, "icon", get(choice, "icon"));
				}
			}
		} else if (("textfield" == classname) || ("passwordfield" == classname)) {
			layoutField(component, 0, ("passwordfield" == classname), 0);
		} else if ("textarea" == classname) {
			String text = getString(component, "text", "");
			int start = getInteger(component, "start", 0);
			if (start > text.length()) {
				setInteger(component, "start", start = text.length(), 0);
			}
			int end = getInteger(component, "end", 0);
			if (end > text.length()) {
				setInteger(component, "end", end = text.length(), 0);
			}

			boolean wrap = getBoolean(component, "wrap", false);
			char[] chars = null;
			if (wrap) {
				Rectangle bounds = getRectangle(component, "bounds");
				chars = getChars(component, text, true, bounds.width - 4, bounds.height);
				if (chars == null) { // need scrollbars
					chars = getChars(component, text, true, bounds.width - block - 4, 0);
				}
			} else {
				chars = getChars(component, text, false, 0, 0);
			}

			Font currentfont = (Font) get(component, "font");
			Font fm = (currentfont != null) ? currentfont : font;
			int width = 0, height = 0;
			int caretx = 0;
			int carety = 0;
			for (int i = 0, j = 0; j <= chars.length; j++) {
				if ((j == chars.length) || (chars[j] == '\n')) {
					width = Math.max(width, fm.charsWidth(chars, i, j - i));
					if ((end >= i) && (end <= j)) {
						caretx = fm.charsWidth(chars, i, end - i);
						carety = height;
					}
					height += fm.getHeight();
					i = j + 1;
				}
			}
			layoutScroll(component, width + 2, height - fm.getHeight() + 2, 0, 0, 0, 0, getBoolean(component, "border", true), 0);
			scrollToVisible(component, caretx, carety, 2, font.getHeight() + 2); // ?
		} else if ("tabbedpane" == classname) {
			// tabbedpane (not selected) tab padding are 1, 3, 1, and 3 pt
			Rectangle bounds = getRectangle(component, "bounds");
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
			boolean stacked = (placement == "stacked");

			// draw up tabs in row/column
			int tabd = 0;
			Rectangle first = null; // x/y location of tab left/top
			int tabsize = 0; // max height/width of tabs
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				if ((tabd == 0) && ((first = getRectangle(tab, "bounds")) != null)) {
					tabd = horizontal ? first.x : first.y; // restore previous
					// offset
				}
				Dimension d = getSize(tab, stacked ? 8 : horizontal ? 12 : 9, stacked ? 3 : horizontal ? 5 : 8);
				setRectangle(tab, "bounds", horizontal ? tabd : 0, horizontal ? 0 : tabd, stacked ? bounds.width : d.width, d.height);
				if (stacked) {
					tabd += d.height;
				} else {
					tabd += (horizontal ? d.width : d.height) - 3;
					tabsize = Math.max(tabsize, horizontal ? d.height : d.width);
				}
			}

			// match tab height/width, set tab content size
			int cx = (placement == "left") ? (tabsize + 1) : 2;
			int cy = (placement == "top") ? (tabsize + 1) : 2;
			int cwidth = bounds.width - ((horizontal || stacked) ? 4 : (tabsize + 3));
			int cheight = bounds.height - (stacked ? (tabd + 3) : (horizontal ? (tabsize + 3) : 4));
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				Rectangle r = getRectangle(tab, "bounds");
				if (!stacked) {
					if (horizontal) {
						if (placement == "bottom") {
							r.y = bounds.height - tabsize;
						}
						r.height = tabsize;
					} else {
						if (placement == "right") {
							r.x = bounds.width - tabsize;
						}
						r.width = tabsize;
					}
				}

				Object comp = get(tab, ":comp"); // relative to the tab
				// location
				if ((comp != null) && getBoolean(comp, "visible", true)) {
					setRectangle(comp, "bounds", cx - r.x, stacked ? (r.height + 1) : (cy - r.y), cwidth, cheight);
					doLayout(comp);
				}
			}
			checkOffset(component);
		} else if (("panel" == classname) || (classname == "dialog")) {
			int gap = getInteger(component, "gap", 0);
			int[][] grid = getGrid(component);
			int top = 0;
			int left = 0;
			int contentwidth = 0;
			int contentheight = 0;
			if (grid != null) { // has subcomponents
				top = getInteger(component, "top", 0);
				left = getInteger(component, "left", 0);
				int bottom = getInteger(component, "bottom", 0);
				int right = getInteger(component, "right", 0);
				// sums the preferred size of cell widths and heights, gaps
				contentwidth = left + getSum(grid[0], 0, grid[0].length, gap, false) + right;
				contentheight = top + getSum(grid[1], 0, grid[1].length, gap, false) + bottom;
			}

			int titleheight = getSize(component, 0, 0).height; // title text
			// and icon
			setInteger(component, ":titleheight", titleheight, 0);
			boolean scrollable = getBoolean(component, "scrollable", false);
			boolean border = ("panel" == classname) && getBoolean(component, "border", false);
			int iborder = (border ? 1 : 0);
			if (scrollable) { // set scrollpane areas
				if ("panel" == classname) {
					int head = titleheight / 2;
					int headgap = (titleheight > 0) ? (titleheight - head - iborder) : 0;
					scrollable = layoutScroll(component, contentwidth, contentheight, head, 0, 0, 0, border, headgap);
				} else { // dialog
					scrollable = layoutScroll(component, contentwidth, contentheight, 3 + titleheight, 3, 3, 3, true, 0);
				}
			}
			if (!scrollable) { // clear scrollpane bounds //+
				set(component, ":view", null);
				set(component, ":port", null);
			}

			if (grid != null) {
				int areax = 0;
				int areay = 0;
				int areawidth = 0;
				int areaheight = 0;
				if (scrollable) {
					// components are relative to the viewport
					Rectangle view = getRectangle(component, ":view");
					areawidth = view.width;
					areaheight = view.height;
				} else { // scrollpane isn't required
					// components are relative to top/left corner
					Rectangle bounds = getRectangle(component, "bounds");
					areawidth = bounds.width;
					areaheight = bounds.height;
					if ("panel" == classname) {
						areax = iborder;
						areay = Math.max(iborder, titleheight);
						areawidth -= 2 * iborder;
						areaheight -= areay + iborder;
					} else { // dialog
						areax = 4;
						areay = 4 + titleheight;
						areawidth -= 8;
						areaheight -= areay + 4;
					}
				}

				for (int i = 0; i < 2; i++) { // i=0: horizontal, i=1:
					// vertical
					// remaining space
					int d = ((i == 0) ? (areawidth - contentwidth) : (areaheight - contentheight));
					if (d != 0) { // + > 0
						int w = getSum(grid[2 + i], 0, grid[2 + i].length, 0, false);
						if (w > 0) {
							for (int j = 0; j < grid[i].length; j++) {
								if (grid[2 + i][j] != 0) {
									grid[i][j] += d * grid[2 + i][j] / w;
								}
							}
						}
					}
				}

				Object comp = get(component, ":comp");
				for (int i = 0; comp != null; comp = get(comp, ":next")) {
					if (!getBoolean(comp, "visible", true)) {
						continue;
					}
					int ix = areax + left + getSum(grid[0], 0, grid[4][i], gap, true);
					int iy = areay + top + getSum(grid[1], 0, grid[5][i], gap, true);
					int iwidth = getSum(grid[0], grid[4][i], grid[6][i], gap, false);
					int iheight = getSum(grid[1], grid[5][i], grid[7][i], gap, false);
					String halign = getString(comp, "halign", "fill");
					String valign = getString(comp, "valign", "fill");
					if ((halign != "fill") || (valign != "fill")) {
						Dimension d = getPreferredSize(comp);
						if (halign != "fill") {
							int dw = Math.max(0, iwidth - d.width);
							if (halign == "center") {
								ix += dw / 2;
							} else if (halign == "right") {
								ix += dw;
							}
							iwidth -= dw;
						}
						if (valign != "fill") {
							int dh = Math.max(0, iheight - d.height);
							if (valign == "center") {
								iy += dh / 2;
							} else if (valign == "bottom") {
								iy += dh;
							}
							iheight -= dh;
						}
					}
					setRectangle(comp, "bounds", ix, iy, iwidth, iheight);
					doLayout(comp);
					i++;
				}
			}
		} else if ("desktop" == classname) {
			Rectangle bounds = getRectangle(component, "bounds");
			for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if (iclass == "dialog") {
					Dimension d = getPreferredSize(comp);
					if (get(comp, "bounds") == null)
						setRectangle(comp, "bounds", Math.max(0, (bounds.width - d.width) / 2), Math.max(0, (bounds.height - d.height) / 2), Math.min(d.width,
								bounds.width), Math.min(d.height, bounds.height));
				} else if ((iclass != ":combolist") && (iclass != ":popup")) {
					setRectangle(comp, "bounds", 0, 0, bounds.width, bounds.height);
				}
				doLayout(comp);
			}
		} else if ("spinbox" == classname) {
			layoutField(component, block, false, 0);
		} else if ("splitpane" == classname) {
			Rectangle bounds = getRectangle(component, "bounds");
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			int maxdiv = Math.max(0, (horizontal ? bounds.width : bounds.height) - 5);

			Object comp1 = get(component, ":comp");
			boolean visible1 = (comp1 != null) && getBoolean(comp1, "visible", true);
			if (divider == -1) {
				int d1 = 0;
				if (visible1) {
					Dimension d = getPreferredSize(comp1);
					d1 = horizontal ? d.width : d.height;
				}
				divider = Math.min(d1, maxdiv);
				setInteger(component, "divider", divider, -1);
			} else if (divider > maxdiv) {
				setInteger(component, "divider", divider = maxdiv, -1);
			}

			if (visible1) {
				setRectangle(comp1, "bounds", 0, 0, horizontal ? divider : bounds.width, horizontal ? bounds.height : divider);
				doLayout(comp1);
			}
			Object comp2 = (comp1 != null) ? get(comp1, ":next") : null;
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				setRectangle(comp2, "bounds", horizontal ? (divider + 5) : 0, horizontal ? 0 : (divider + 5), horizontal ? (bounds.width - 5 - divider)
						: bounds.width, horizontal ? bounds.height : (bounds.height - 5 - divider));
				doLayout(comp2);
			}
		} else if (("list" == classname) || ("table" == classname) || ("tree" == classname)) {
			int line = getBoolean(component, "line", true) ? 1 : 0;
			int width = 0;
			int columnheight = 0;
			if ("table" == classname) {
				Object header = get(component, "header");
				int[] columnwidths = null;
				if (header != null) {
					columnwidths = new int[getCount(header)];
					Object column = get(header, ":comp");
					for (int i = 0; i < columnwidths.length; i++) {
						if (i != 0) {
							column = get(column, ":next");
						}
						columnwidths[i] = getInteger(column, "width", 80);
						width += columnwidths[i];
						Dimension d = getSize(column, 2, 2);
						columnheight = Math.max(columnheight, d.height);
					}
				}
				set(component, ":widths", columnwidths);
			}
			int y = 0;
			int level = 0;
			for (Object item = get(component, ":comp"); item != null;) {
				int x = 0;
				int iwidth = 0;
				int iheight = 0;
				if ("table" == classname) {
					iwidth = width;
					for (Object cell = get(item, ":comp"); cell != null; cell = get(cell, ":next")) {
						Dimension d = getSize(cell, 2, 2);
						iheight = Math.max(iheight, d.height);
					}
				} else {
					if ("tree" == classname) {
						x = (level + 1) * block;
					}
					Dimension d = getSize(item, 6, 2);
					iwidth = d.width;
					iheight = d.height;
					width = Math.max(width, x + d.width);
				}
				setRectangle(item, "bounds", x, y, iwidth, iheight);
				y += iheight + line;
				if ("tree" == classname) {
					Object next = get(item, ":comp");
					if ((next != null) && getBoolean(item, "expanded", true)) {
						level++;
					} else {
						while (((next = get(item, ":next")) == null) && (level > 0)) {
							item = getParent(item);
							level--;
						}
					}
					item = next;
				} else {
					item = get(item, ":next");
				}
			}
			layoutScroll(component, width, y - line, columnheight, 0, 0, 0, true, 0);
		} else if ("menubar" == classname) {
			Rectangle bounds = getRectangle(component, "bounds");
			int x = 0;
			for (Object menu = get(component, ":comp"); menu != null; menu = get(menu, ":next")) {
				Dimension d = getSize(menu, 8, 4);
				setRectangle(menu, "bounds", x, 0, d.width, bounds.height);
				x += d.width;
			}
		} else if ("bean" == classname) {
			throw new RuntimeException("Bean not implemented!");
			/*
			Rectangle r = getRectangle(component, "bounds");
			((Component) get(component, "bean")).setBounds(r);
			 */
		}
	}

	/**
	 * Scroll tabs to make the selected one visible
	 *
	 * @param component
	 *            a tabbedpane
	 */
	protected void checkOffset(Object component) {
		String placement = getString(component, "placement", "top");
		int selected = getInteger(component, "selected", 0);
		int i = 0;
		if (placement == "stacked") {
			int dy = 0;
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				Rectangle r = getRectangle(tab, "bounds");
				r.y = dy;
				dy += r.height;
				if (i == selected) {
					dy += getRectangle(get(tab, ":comp"), "bounds").height + 2;
				}
				i++;
			}
			checkLocation(component); // layout changed, check the hovered tab
			return;
		}
		boolean horizontal = ((placement == "top") || (placement == "bottom"));
		Rectangle bounds = getRectangle(component, "bounds");
		int panesize = horizontal ? bounds.width : bounds.height;
		int first = 0;
		int last = 0;
		int d = 0;
		for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
			Rectangle r = getRectangle(tab, "bounds");
			if (i == 0) {
				first = (horizontal ? r.x : r.y);
			}
			last = (horizontal ? (r.x + r.width) : (r.y + r.height));
			if (i == selected) {
				int ifrom = (horizontal ? r.x : r.y) - 6;
				int ito = (horizontal ? (r.x + r.width) : (r.y + r.height)) + 6;
				if (ifrom < 0) {
					d = -ifrom;
				} else if (ito > panesize) {
					d = panesize - ito;
				}
			}
			i++;
		}
		d = Math.min(-first, Math.max(d, panesize - last));
		if (d != 0) {
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				Rectangle r = getRectangle(tab, "bounds");
				if (horizontal) {
					r.x += d;
				} else {
					r.y += d;
				}
				Object comp = get(tab, ":comp"); // relative to the tab
				// location
				if ((comp != null) && getBoolean(comp, "visible", true)) {
					Rectangle rc = getRectangle(comp, "bounds");
					if (horizontal) {
						rc.x -= d;
					} else {
						rc.y -= d;
					}
				}
			}
			checkLocation(component); // layout changed, check the hovered tab
		}
	}

	protected char[] getChars(Object component, String text, boolean wrap, int width, int height) {
		char[] chars = (char[]) get(component, ":text");
		if ((chars == null) || (chars.length != text.length())) {
			chars = text.toCharArray();
			set(component, ":text", chars);
		} else
			text.getChars(0, chars.length, chars, 0);

		if (wrap) {
			Font currentfont = (Font) get(component, "font");
			Font fm = ((currentfont != null) ? currentfont : font);
			int lines = (height - 4 + fm.getHeight()) / fm.getHeight();
			boolean prevletter = false;
			int n = chars.length;
			int linecount = 0;
			for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last
				// space index
				// (before k)
				if (((k == n) || (chars[k] == '\n') || (chars[k] == ' ')) && (j > i) && (fm.charsWidth(chars, i, k - i) > width)) {
					chars[j] = '\n';
					k--; // draw line to the begin of the current word (+
					// spaces) if it is out of width
				} else if ((k == n) || (chars[k] == '\n')) { // draw line to
					// the text/line
					// end
					j = k;
					prevletter = false;
				} else {
					if ((chars[k] == ' ') && (prevletter || (j > i))) {
						j = k;
					} // keep spaces starting the line
					prevletter = (chars[k] != ' ');
					continue;
				}
				linecount++;
				if ((lines != 0) && (linecount == lines)) {
					return null;
				}
				i = j + 1;
			}
		}
		return chars;
	}

	/**
	 * @param component
	 *            a menuitem
	 * @return key modifier strings and key text
	 */
	protected String getAccelerator(Object component) {
		Object accelerator = get(component, "accelerator");
		if (accelerator != null) {
			long keystroke = ((Long) accelerator).longValue();
			int keycode = (int) (keystroke >> 32);
			int modifiers = (int) (keystroke & 0xffff);
			return KeyEvent.getKeyModifiersText(keycode) + " " + KeyEvent.getKeyText(modifiers);
		}
		return null;
	}

	/**
	 * Pop up the list of choices for the given combobox
	 *
	 * @param combobox
	 * @return the created combolist
	 */
	protected Object popupCombo(Object combobox) {
		// combobox bounds relative to the root desktop
		int combox = 0, comboy = 0, combowidth = 0, comboheight = 0;
		for (Object comp = combobox; comp != content; comp = getParent(comp)) {
			Rectangle r = getRectangle(comp, "bounds");
			combox += r.x;
			comboy += r.y;
			Rectangle view = getRectangle(comp, ":view");
			if (view != null) {
				combox -= view.x;
				comboy -= view.y;
				Rectangle port = getRectangle(comp, ":port");
				combox += port.x;
				comboy += port.y;
			}
			if (comp == combobox) {
				combowidth = r.width;
				comboheight = r.height;
			}
		}
		// :combolist -> combobox and combobox -> :combolist
		Object combolist = createImpl(":combolist");
		set(combolist, "combobox", combobox);
		set(combobox, ":combolist", combolist);
		// add :combolist to the root desktop and set the combobox as popupowner
		popupowner = combobox;
		insertItem(content, ":comp", combolist, 0);
		set(combolist, ":parent", content);
		// lay out choices verticaly and calculate max width and height sum
		int pw = 0;
		int ph = 0;
		for (Object item = get(combobox, ":comp"); item != null; item = get(item, ":next")) {
			Dimension d = getSize(item, 8, 4);
			setRectangle(item, "bounds", 0, ph, d.width, d.height);
			pw = Math.max(pw, d.width);
			ph += d.height;
		}
		if(combowidth < pw) {
			// if the width is too great, add support for scrollbars
			ph += block;
		}

		// set :combolist bounds
		int listy = 0, listheight = 0;
		int bellow = getRectangle(content, "bounds").height - comboy - comboheight - 1;
		if ((ph + 2 > bellow) && (comboy - 1 > bellow)) { // popup above
			// combobox
			listy = Math.max(0, comboy - 1 - ph - 2);
			listheight = Math.min(comboy - 1, ph + 2);
		} else { // popup bellow combobox
			listy = comboy + comboheight + 1;
			listheight = Math.min(bellow, ph + 2);
		}
		setRectangle(combolist, "bounds", combox, listy, combowidth, listheight);
		layoutScroll(combolist, pw, ph, 0, 0, 0, 0, true, 0);
		repaint(combolist);
		// hover the selected item
		int selected = getInteger(combobox, "selected", -1);
		setInside(combolist, (selected != -1) ? getItem(combobox, selected) : null, true);
		return combolist;
	}

	/**
	 * @param component
	 *            menubar or :popup
	 * @return the created popupmenu
	 */
	protected Object popupMenu(Object component) {
		Object popup = get(component, ":popup"); // first :popup child
		Object selected = get(component, "selected"); // selected menu in of
		// the component
		if (popup != null) { // remove its current :popup
			if (get(popup, "menu") == selected) {
				return null;
			} // but the currect one
			set(popup, "selected", null);
			set(popup, "menu", null);
			repaint(popup);
			removeItemImpl(content, popup);
			set(popup, ":parent", null);
			set(component, ":popup", null);
			checkLocation(popup);

			popupMenu(popup); // remove recursively
		}
		// pop up the selected menu only
		if ((selected == null) || (getClass(selected) != "menu")) {
			return null;
		}

		// create the :popup, :popup.menu -> menu,
		// menubar|:popup.:popup -> :popup, menubar|:popup.selected -> menu
		popup = createImpl(":popup");
		set(popup, "menu", selected);
		set(component, ":popup", popup);
		insertItem(content, ":comp", popup, 0);
		set(popup, ":parent", content);
		// calculates the bounds of the previous menubar/:popup relative to the
		// root desktop
		int menux = 0, menuy = 0, menuwidth = 0, menuheight = 0;
		for (Object comp = component; comp != content; comp = getParent(comp)) {
			Rectangle r = getRectangle(comp, "bounds");
			menux += r.x;
			menuy += r.y;
			Rectangle view = getRectangle(comp, ":view");
			if (view != null) {
				menux -= view.x;
				menuy -= view.y;
				Rectangle port = getRectangle(comp, ":port");
				menux += port.x;
				menuy += port.y;
			}
			if (comp == component) {
				menuwidth = r.width;
				menuheight = r.height;
			}
		}
		// set :popup bounds
		Rectangle menubounds = getRectangle(selected, "bounds");
		boolean menubar = ("menubar" == getClass(component));
		if (menubar) {
			popupowner = component;
		}
		popup(selected, popup, menubar ? 'D' : 'R', menubar ? (menux + menubounds.x) : menux, menuy + menubounds.y, menubar ? menubounds.width : menuwidth,
				menubar ? menuheight : menubounds.height, menubar ? 1 : 3);
		return popup;
	}

	/**
	 * @param popupmenu
	 */
	protected void popupPopup(Object popupmenu, int x, int y) {
		// :popup.menu -> popupmenu, popupmenu.:popup -> :popup
		Object popup = createImpl(":popup");
		set(popup, "menu", popupmenu);
		set(popupmenu, ":popup", popup);
		// add :popup to the root desktop and set the combobox as popupowner
		popupowner = popupmenu;
		insertItem(content, ":comp", popup, 0);
		set(popup, ":parent", content);
		// lay out
		popup(popupmenu, popup, 'D', x, y, 0, 0, 0);
		// invoke menushown listener
		invoke(popupmenu, null, "menushown"); // TODO before
	}

	/**
	 * Lays out a popupmenu
	 *
	 * @param menu
	 *            menubar's menu, menu's menu, or component's popupmenu
	 *            including items
	 * @param popup
	 *            created popupmenu
	 * @param direction
	 *            'U' for up, 'D' for down, and 'R' for right
	 * @param x
	 *            menu's x location relative to the desktop
	 * @param y
	 *            menu's y location
	 * @param width
	 *            menu's width, or zero for popupmenu
	 * @param height
	 *            menu's height
	 * @param offset
	 *            inner padding relative to the menu's bounds
	 */
	protected void popup(Object menu, Object popup, char direction, int x, int y, int width, int height, int offset) {
		int pw = 0;
		int ph = 0;
		for (Object item = get(menu, ":comp"); item != null; item = get(item, ":next")) {
			String itemclass = getClass(item);
			Dimension d = (itemclass == "separator") ? new Dimension(1, 1) : getSize(item, 8, 4);
			if (itemclass == "checkboxmenuitem") {
				d.width = d.width + block + 3;
				d.height = Math.max(block, d.height);
			} else if (itemclass == "menu") {
				d.width += block;
			}
			String accelerator = getAccelerator(item); // add accelerator width
			if (accelerator != null) {
				d.width += 4 + font.stringWidth(accelerator); // TODO
				// font,
				// height
				// and
				// gap
			}
			setRectangle(item, "bounds", 1, 1 + ph, d.width, d.height);
			pw = Math.max(pw, d.width);
			ph += d.height;
		}
		pw += 2;
		ph += 2; // add border widths
		// set :popup bounds
		Rectangle desktop = getRectangle(content, "bounds");
		if (direction == 'R') {
			x += ((x + width - offset + pw > desktop.width) && (x >= pw - offset)) ? (offset - pw) : (width - offset);
			if ((y + ph > desktop.height) && (ph <= y + height)) {
				y -= ph - height;
			}
		} else {
			boolean topspace = (y >= ph - offset); // sufficient space above
			boolean bottomspace = (desktop.height - y - height >= ph - offset);
			y += ((direction == 'U') ? (topspace || !bottomspace) : (!bottomspace && topspace)) ? (offset - ph) : (height - offset);
		}
		setRectangle(popup, "bounds", Math.max(0, Math.min(x, desktop.width - pw)), Math.max(0, Math.min(y, desktop.height - ph)), pw, ph);
		repaint(popup);
	}

	/**
	 * @param item
	 *            //TODO can be scrollbar string
	 */
	protected void closeCombo(Object combobox, Object combolist, Object item) {
		if ((item != null) && getBoolean(item, "enabled", true)) {
			String text = getString(item, "text", "");
			set(combobox, "text", text); // if editable
			setInteger(combobox, "start", text.length(), 0);
			setInteger(combobox, "end", 0, 0);
			set(combobox, "icon", get(item, "icon"));
			validate(combobox);
			setInteger(combobox, "selected", getIndex(combobox, item), -1);
			invoke(combobox, item, "action");
		}
		set(combolist, "combobox", null);
		set(combobox, ":combolist", null);
		removeItemImpl(content, combolist);
		repaint(combolist);
		set(combolist, ":parent", null);
		popupowner = null;
		checkLocation(combolist);
	}

	protected void closeup() {
		if (popupowner != null) {
			String classname = getClass(popupowner);
			if ("menubar" == classname) {
				set(popupowner, "selected", null);
				popupMenu(popupowner);
				repaint(popupowner); // , selected
			} else if ("combobox" == classname) {
				closeCombo(popupowner, get(popupowner, ":combolist"), null);
			} else { // "popupmenu"
				popupMenu(popupowner);
			}
			popupowner = null;
		}
	}

	protected void showTip() {
		String text = null;
		tooltipowner = null;
		String classname = getClass(mouseinside);
		if ((classname == "tabbedpane") || (classname == "menubar") || (classname == ":popup")) {
			if (insidepart != null) {
				text = getString(insidepart, "tooltip", null);
			}
		} else if (classname == ":combolist") {
			if (insidepart instanceof Object[]) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		// TODO list table tree
		if (text == null) {
			text = getString(mouseinside, "tooltip", null);
		} else {
			tooltipowner = insidepart;
		}

		if (text != null && text.length() > 0) {
			Font fm = font;
			int width = fm.stringWidth(text) + 4;
			int height = fm.getHeight() + 4;
			if (tooltipowner == null) {
				tooltipowner = mouseinside;
			}
			Rectangle bounds = getRectangle(content, "bounds");
			int tx = Math.max(0, Math.min(mousex + 10, bounds.width - width));
			int ty = Math.max(0, Math.min(mousey + 10, bounds.height - height));
			setRectangle(tooltipowner, ":tooltipbounds", tx, ty, width, height);

			// repair disable shadow
			Rectangle shadow = inverseShadow;
			inverseShadow = null;
			repaint(tx, ty, width, height);

			// kindof not 100% but good enough
			serviceRepaints();
			inverseShadow = shadow;
		}
	}

	protected void hideTip() {
		if (tooltipowner != null) {
			Rectangle bounds = getRectangle(tooltipowner, ":tooltipbounds");
			set(tooltipowner, ":tooltipbounds", null);
			tooltipowner = null;
			repaint(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	protected void layoutField(Object component, int dw, boolean hidden, int left) {
		int width = getRectangle(component, "bounds").width - left - dw;
		String text = getString(component, "text", "");
		int start = getInteger(component, "start", 0);
		if (start > text.length()) {
			setInteger(component, "start", start = text.length(), 0);
		}
		int end = getInteger(component, "end", 0);
		if (end > text.length()) {
			setInteger(component, "end", end = text.length(), 0);
		}
		int offset = getInteger(component, ":offset", 0);
		int off = offset;
		Font currentfont = (Font) get(component, "font");
		Font fm = ((currentfont != null) ? currentfont : font);
		int textwidth = hidden ? (fm.charWidth('*') * text.length()) : fm.stringWidth(text);
		int caret = hidden ? (fm.charWidth('*') * end) : fm.stringWidth(text.substring(0, end));
		if (textwidth <= width - 4) { // text fits inside the available space
			Object align = get(component, "alignment");
			if (align == null) {
				off = 0;
			} // left alignment
			else {
				off = textwidth - width + 4; // right alignment
				if (align == "center") {
					off /= 2;
				} // center alignment
			}
		} else { // text is scrollable
			if (off > caret) {
				off = caret;
			} else if (off < caret - width + 4) {
				off = caret - width + 4;
			}
			off = Math.max(0, Math.min(off, textwidth - width + 4));
		}
		if (off != offset) {
			setInteger(component, ":offset", off, 0);
		}
	}

	/**
	 * Set viewport (:port) bounds excluding borders, view position and content
	 * size (:view), horizontal (:horizontal), and vertical (:vertical)
	 * scrollbar bounds
	 *
	 * @param component
	 *            scrollable widget
	 * @param contentwidth
	 *            preferred component width
	 * @param contentheight
	 *            preferred component height
	 * @param top
	 *            top inset (e.g. table header, dialog title, half of panel
	 *            title)
	 * @param left
	 *            left inset (e.g. dialog border)
	 * @param bottom
	 *            bottom inset (e.g. dialog border)
	 * @param right
	 *            right inset (e.g. dialog border)
	 * @param topgap
	 *            (lower half of panel title)
	 * @return true if scrollpane is required, otherwise false
	 *
	 * list: 0, 0, 0, 0, true, 0 | table: header, ... | dialog: header, 3, 3, 3,
	 * true, 0 title-border panel: header / 2, 0, 0, 0, true, head
	 */
	protected boolean layoutScroll(Object component, int contentwidth, int contentheight, int top, int left, int bottom, int right, boolean border, int topgap) {
		Rectangle bounds = getRectangle(component, "bounds");
		int iborder = border ? 1 : 0;
		int iscroll = block + 1 - iborder;
		int portwidth = bounds.width - left - right - 2 * iborder; // available
		// horizontal
		// space
		int portheight = bounds.height - top - topgap - bottom - 2 * iborder; // vertical
		// space
		boolean hneed = contentwidth > portwidth; // horizontal scrollbar
		// required
		boolean vneed = contentheight > portheight - (hneed ? iscroll : 0); // vertical
		// scrollbar
		// needed
		if (vneed) {
			portwidth -= iscroll;
		} // subtract by vertical scrollbar width
		hneed = hneed || (vneed && (contentwidth > portwidth));
		if (hneed) {
			portheight -= iscroll;
		} // subtract by horizontal scrollbar height

		setRectangle(component, ":port", left + iborder, top + iborder + topgap, portwidth, portheight);
		if (hneed) {
			setRectangle(component, ":horizontal", left, bounds.height - bottom - block - 1, bounds.width - left - right - (vneed ? block : 0), block + 1);
		} else {
			set(component, ":horizontal", null);
		}
		if (vneed) {
			setRectangle(component, ":vertical", bounds.width - right - block - 1, top, block + 1, bounds.height - top - bottom - (hneed ? block : 0));
		} else {
			set(component, ":vertical", null);
		}

		contentwidth = Math.max(contentwidth, portwidth);
		contentheight = Math.max(contentheight, portheight);
		int viewx = 0, viewy = 0;
		Rectangle view = getRectangle(component, ":view");
		if (view != null) { // check the previous location
			viewx = Math.max(0, Math.min(view.x, contentwidth - portwidth));
			viewy = Math.max(0, Math.min(view.y, contentheight - portheight));
		}
		setRectangle(component, ":view", viewx, viewy, contentwidth, contentheight);
		return vneed || hneed;
	}

	protected void scrollToVisible(Object component, int x, int y, int width, int height) {
		Rectangle view = getRectangle(component, ":view");
		Rectangle port = getRectangle(component, ":port");
		if(view != null && port != null) {
			int vx = Math.max(x + width - port.width, Math.min(view.x, x));
			int vy = Math.max(y + height - port.height, Math.min(view.y, y));
			if ((view.x != vx) || (view.y != vy)) {
				repaint(component); // horizontal | vertical
				view.x = vx;
				view.y = vy;
			}
		}
	}

	/**
	 * Gets the preferred size of the root component
	 *
	 * @return a dimension object indicating the root component's preferred size
	 */
	public Dimension getPreferredSize() {
		return getPreferredSize(content);
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	protected Dimension getPreferredSize(Object component) {
		int width = getInteger(component, "width", 0);
		int height = getInteger(component, "height", 0);
		if ((width > 0) && (height > 0)) {
			return new Dimension(width, height);
		}
		String classname = getClass(component);
		if ("label" == classname) {
			return getSize(component, 0, 0);
		}
		if (("button" == classname) || ("togglebutton" == classname)) {
			boolean link = ("button" == classname) && (get(component, "type") == "link");
			return getSize(component, link ? 0 : 12, link ? 0 : 6);
		}
		if ("checkbox" == classname) {
			Dimension d = getSize(component, 0, 0);
			d.width = d.width + block + 3;
			d.height = Math.max(block, d.height);
			return d;
		}
		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Dimension size = getFieldSize(component);
				Image icon = getIcon(component, "icon", null);
				if (icon != null) {
					size.width += icon.getWidth();
					size.height = Math.max(size.height, icon.getHeight() + 2);
				}
				size.width += block;
				return size;
			} else {
				// maximum size of current values and choices including 2-2-2-2
				// insets
				Dimension size = getSize(component, 4, 4);
				for (Object item = get(component, ":comp"); item != null; item = get(item, ":next")) {
					Dimension d = getSize(item, 4, 4);
					size.width = Math.max(d.width, size.width);
					size.height = Math.max(d.height, size.height);
				}
				size.width += block;
				if (size.height == 4) { // no content nor items, set text height
					Font customfont = (Font) get(component, "font");
					Font fm = ((customfont != null) ? customfont : font);
					size.height = font.getHeight() + 4;
				}
				return size;
			}
		}
		if (("textfield" == classname) || ("passwordfield" == classname)) {
			return getFieldSize(component);
		}
		if ("textarea" == classname) {
			int columns = getInteger(component, "columns", 0);
			int rows = getInteger(component, "rows", 0); // 'e' -> 'm' ?
			Font currentfont = (Font) get(component, "font");
			Font fm = ((currentfont != null) ? currentfont : font);
			return new Dimension(((columns > 0) ? (columns * fm.charWidth('e') + 2) : 76) + 2 + block,
					((rows > 0) ? (rows * fm.getHeight() - fm.getHeight() + 2) : 76) + 2 + block);
		}
		if ("tabbedpane" == classname) {
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement != "left") && (placement != "right"));
			int tabsize = 0; // max tab height (for horizontal),
			// max tabwidth (for vertical), or sum of tab heights for stacked
			int contentwidth = 0;
			int contentheight = 0; // max content size
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				Dimension d = getSize(tab, 0, 0);
				if (placement == "stacked") {
					tabsize += d.height + 3;
				} else {
					tabsize = Math.max(tabsize, horizontal ? d.height + 5 : d.width + 9);
				}

				Object comp = get(tab, ":comp");
				if ((comp != null) && getBoolean(comp, "visible", true)) {
					Dimension dc = getPreferredSize(comp);
					contentwidth = Math.max(contentwidth, dc.width);
					contentheight = Math.max(contentheight, dc.height);
				}
			}
			return new Dimension(contentwidth + (horizontal ? 4 : (tabsize + 3)), contentheight + (horizontal ? (tabsize + 3) : 4));
		}
		if (("panel" == classname) || (classname == "dialog")) {
			// title text and icon height
			Dimension size = getSize(component, 0, 0);
			// add border size
			if (classname == "dialog") {
				size.width = 8;
				size.height += 8; // title width neglected
			} else if (getBoolean(component, "border", false)) { // bordered
				// panel
				size.width = 2;
				size.height += (size.height > 0) ? 1 : 2; // title includes
				// line
			} else {
				size.width = 0;
			} // title width is clipped
			// add paddings
			size.width += getInteger(component, "left", 0) + getInteger(component, "right", 0);
			size.height += getInteger(component, "top", 0) + getInteger(component, "bottom", 0);
			// add content preferred size
			int gap = getInteger(component, "gap", 0);
			int[][] grid = getGrid(component);
			if (grid != null) { // has components
				size.width += getSum(grid[0], 0, grid[0].length, gap, false);
				size.height += getSum(grid[1], 0, grid[1].length, gap, false);
			}
			return size;
		} else if ("desktop" == classname) {
			Dimension size = new Dimension();
			for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if ((iclass != "dialog") && (iclass != ":popup") && (iclass != ":combolist")) {
					Dimension d = getPreferredSize(comp);
					size.width = Math.max(d.width, size.width);
					size.height = Math.max(d.height, size.height);
				}
			}
			return size;
		}
		if ("spinbox" == classname) {
			Dimension size = getFieldSize(component);
			size.width += block;
			return size;
		}
		if ("progressbar" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			return new Dimension(horizontal ? 76 : 6, horizontal ? 6 : 76);
		}
		if ("slider" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			return new Dimension(horizontal ? 76 : 10, horizontal ? 10 : 76);
		}
		if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			Object comp1 = get(component, ":comp");
			Dimension size = ((comp1 == null) || !getBoolean(comp1, "visible", true)) ? new Dimension() : getPreferredSize(comp1);
			Object comp2 = get(comp1, ":next");
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				Dimension d = getPreferredSize(comp2);
				size.width = horizontal ? (size.width + d.width) : Math.max(size.width, d.width);
				size.height = horizontal ? Math.max(size.height, d.height) : (size.height + d.height);
			}
			if (horizontal) {
				size.width += 5;
			} else {
				size.height += 5;
			}
			return size;
		}
		if (("list" == classname) || ("table" == classname) || ("tree" == classname)) {
			return new Dimension(76 + 2 + block, 76 + 2 + block);
		}
		if ("separator" == classname) {
			return new Dimension(1, 1);
		}
		if ("menubar" == classname) {
			Dimension size = new Dimension(0, 0);
			for (Object menu = get(component, ":comp"); menu != null; menu = get(menu, ":next")) {
				Dimension d = getSize(menu, 8, 4);
				size.width += d.width;
				size.height = Math.max(size.height, d.height);
			}
			return size;
		}
		if ("bean" == classname) {
			throw new RuntimeException("Not implemented");
			/*
			return ((Component) get(component, "bean")).getPreferredSize();
			 */
		}
		throw new IllegalArgumentException(classname);
	}

	/**
	 * @param component
	 *            a container
	 * @return null for zero visible subcomponent, otherwise an array contains
	 *         the following lists:
	 *         <ul>
	 *         <li>columnwidths, preferred width of grid columns</li>
	 *         <li>rowheights, preferred heights of grid rows</li>
	 *         <li>columnweights, grid column-width weights</li>
	 *         <li>rowweights, grid row-height weights</li>
	 *         <li>gridx, horizontal location of the subcomponents</li>
	 *         <li>gridy, vertical locations</li>
	 *         <li>gridwidth, column spans</li>
	 *         <li>gridheight, row spans</li>
	 *         </ul>
	 */
	protected int[][] getGrid(Object component) {
		int count = 0; // count of the visible subcomponents
		for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
			if (getBoolean(comp, "visible", true)) {
				count++;
			}
		}
		if (count == 0) {
			return null;
		} // zero subcomponent
		int columns = getInteger(component, "columns", 0);
		int icols = (columns != 0) ? columns : count;
		int irows = (columns != 0) ? ((count + columns - 1) / columns) : 1;
		int[][] grid = { new int[icols], new int[irows], // columnwidths,
				// rowheights
				new int[icols], new int[irows], // columnweights, rowweights
				new int[count], new int[count], // gridx, gridy
				new int[count], new int[count] }; // gridwidth, gridheight
		int[] columnheight = new int[icols];
		int[][] cache = null; // preferredwidth, height, columnweight,
		// rowweight

		int i = 0;
		int x = 0;
		int y = 0;
		int nextsize = 0;
		for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
			if (!getBoolean(comp, "visible", true)) {
				continue;
			}
			int colspan = ((columns != 0) && (columns < count)) ? Math.min(getInteger(comp, "colspan", 1), columns) : 1;
			int rowspan = (columns != 1) ? getInteger(comp, "rowspan", 1) : 1;

			for (int j = 0; j < colspan; j++) {
				if ((columns != 0) && (x + colspan > columns)) {
					x = 0;
					y++;
					j = -1;
				} else if (columnheight[x + j] > y) {
					x += (j + 1);
					j = -1;
				}
			}
			if (y + rowspan > grid[1].length) {
				int[] rowheights = new int[y + rowspan];
				System.arraycopy(grid[1], 0, rowheights, 0, grid[1].length);
				grid[1] = rowheights;
				int[] rowweights = new int[y + rowspan];
				System.arraycopy(grid[3], 0, rowweights, 0, grid[3].length);
				grid[3] = rowweights;
			}
			for (int j = 0; j < colspan; j++) {
				columnheight[x + j] = y + rowspan;
			}

			int weightx = getInteger(comp, "weightx", 0);
			int weighty = getInteger(comp, "weighty", 0);
			Dimension d = getPreferredSize(comp);

			if (colspan == 1) {
				grid[0][x] = Math.max(grid[0][x], d.width); // columnwidths
				grid[2][x] = Math.max(grid[2][x], weightx); // columnweights
			} else {
				if (cache == null) {
					cache = new int[4][count];
				}
				cache[0][i] = d.width;
				cache[2][i] = weightx;
				if ((nextsize == 0) || (colspan < nextsize)) {
					nextsize = colspan;
				}
			}
			if (rowspan == 1) {
				grid[1][y] = Math.max(grid[1][y], d.height); // rowheights
				grid[3][y] = Math.max(grid[3][y], weighty); // rowweights
			} else {
				if (cache == null) {
					cache = new int[4][count];
				}
				cache[1][i] = d.height;
				cache[3][i] = weighty;
				if ((nextsize == 0) || (rowspan < nextsize)) {
					nextsize = rowspan;
				}
			}
			grid[4][i] = x; // gridx
			grid[5][i] = y; // gridy
			grid[6][i] = colspan; // gridwidth
			grid[7][i] = rowspan; // gridheight

			x += colspan;
			i++;
		}

		while (nextsize != 0) {
			int size = nextsize;
			nextsize = 0;
			for (int j = 0; j < 2; j++) { // horizontal, vertical
				for (int k = 0; k < count; k++) {
					if (grid[6 + j][k] == size) { // gridwidth, gridheight
						int gridpoint = grid[4 + j][k]; // gridx, gridy

						int weightdiff = cache[2 + j][k];
						for (int m = 0; (weightdiff > 0) && (m < size); m++) {
							weightdiff -= grid[2 + j][gridpoint + m];
						}
						if (weightdiff > 0) {
							int weightsum = cache[2 + j][k] - weightdiff;
							for (int m = 0; (weightsum > 0) && (m < size); m++) {
								int weight = grid[2 + j][gridpoint + m];
								if (weight > 0) {
									int weightinc = weight * weightdiff / weightsum;
									grid[2 + j][gridpoint + m] += weightinc;
									weightdiff -= weightinc;
									weightsum -= weightinc;
								}
							}
							grid[2 + j][gridpoint + size - 1] += weightdiff;
						}

						int sizediff = cache[j][k];
						int weightsum = 0;
						for (int m = 0; (sizediff > 0) && (m < size); m++) {
							sizediff -= grid[j][gridpoint + m];
							weightsum += grid[2 + j][gridpoint + m];
						}
						if (sizediff > 0) {
							for (int m = 0; (weightsum > 0) && (m < size); m++) {
								int weight = grid[2 + j][gridpoint + m];
								if (weight > 0) {
									int sizeinc = weight * sizediff / weightsum;
									grid[j][gridpoint + m] += sizeinc;
									sizediff -= sizeinc;
									weightsum -= weight;
								}
							}
							grid[j][gridpoint + size - 1] += sizediff;
						}
					} else if ((grid[6 + j][k] > size) && ((nextsize == 0) || (grid[6 + j][k] < nextsize))) {
						nextsize = grid[6 + j][k];
					}
				}
			}
		}
		return grid;
	}

	protected int getSum(int[] values, int from, int length, int gap, boolean last) {
		if (length <= 0) {
			return 0;
		}
		int value = 0;
		for (int i = 0; i < length; i++) {
			value += values[from + i];
		}
		return value + (length - (last ? 0 : 1)) * gap;
	}

	protected Dimension getFieldSize(Object component) {
		int columns = getInteger(component, "columns", 0);
		Font currentfont = (Font) get(component, "font");
		Font fm = ((currentfont != null) ? currentfont : font);
		return new Dimension(((columns > 0) ? (columns * fm.charWidth('e')) : 76) + 4, fm.getHeight() + 4); // fm.stringWidth(text)
	}

	/**
	 * @param component
	 *            a widget including the text and icon parameters
	 * @param dx
	 *            increase width by this value
	 * @param dy
	 *            increase height by this value
	 * @return size of the text and the image (plus a gap) including the given
	 *         offsets
	 */
	protected Dimension getSize(Object component, int dx, int dy) {
		String text = getString(component, "text", null);
		int tw = 0;
		int th = 0;
		if (text != null) {
			Font customfont = (Font) get(component, "font");
			Font fm = ((customfont != null) ? customfont : font);
			tw = fm.stringWidth(text);
			th = fm.getHeight();
		}
		Image icon = getIcon(component, "icon", null);
		int iw = 0;
		int ih = 0;
		if (icon != null) {
			iw = icon.getWidth();
			ih = icon.getHeight();
			if (text != null) {
				iw += 2;
			}
		}
		return new Dimension(tw + iw + dx, Math.max(th, ih) + dy);
	}

	protected void paintShadowImpl(Graphics g, int x, int y, int width, int height) {
		g.setClip(x, y, width, height);

		/*
		try {
		    com.nokia.mid.ui.DirectGraphics dg = com.nokia.mid.ui.DirectUtils.getDirectGraphics(g);
		    dg.setARGBColor(c_disable);
		    g.fillRect(x, y, width, height);
		} catch(Throwable e) {
		 */
		g.setColor(c_disable);

		int canvasWidth = getWidth();
		int canvasHeight = getHeight();

		for (int i = -canvasWidth; i < canvasHeight; i += 2) {
			g.drawLine(x, i, x + width, i + width);
		}
		//        }

	}

	protected void paintShadow(Graphics g, int x, int y, int width, int height, Rectangle rect) {

		if (!(x + width < rect.x || x > rect.x + rect.width || y + height < rect.y || y > rect.y + rect.height)) {

			// above
			if (rect.y > y && rect.y < y + height) {
				paintShadowImpl(g, x, y, width, rect.y - y);
			}

			// below
			if (rect.y + rect.height >= y && rect.y + rect.height < y + height) {
				paintShadowImpl(g, x, rect.y + rect.height, width, y + height - (rect.y + rect.height));
			}

			// left
			if (rect.x > x && rect.x < x + width) {
				paintShadowImpl(g, x, rect.y, rect.x - x, rect.height);
			}

			// right
			if (rect.x + rect.width >= x && rect.x + rect.width < x + width) {
				paintShadowImpl(g, rect.x + rect.width, rect.y, x + width - (rect.x + rect.width), rect.height);
			}
		} // else misses

	}

	/**
	 * Paints the components inside the graphics clip area
	 */
	public void paint(Graphics g) {

		g.setFont(font);

		if (hgradient == null) {
			initGradients();
		}

		int x = g.getClipX();
		int y = g.getClipY();
		int width = g.getClipWidth();
		int height = g.getClipHeight();

		paint(g, x, y, width, height, content, true);

		Object dialog = content;
		for (Object next = get(content, ":comp"); next != null; next = get(content, ":next")) {
			if (getClass(next) == "dialog") {
				dialog = next;
				break;
			}
		}

		// uncomment for debugging
		// paint the focus points
		/*
		if (popupowner == null) {
		    g.setClip(0, 0, getWidth(), getHeight());
		    findNextFocuable(g, dialog, -1);
		}
		*/

		// paint the focus owner
		/*
		if (focusowner != null) {
		    Rectangle r = getAbsoluteBounds(focusowner);
		    g.drawRect(r.x, r.y, r.width-1, r.height-1);
		}
		*/

		// paint shadow here - the pointer and attention are painted over
		if (inverseShadow != null) {
			paintShadow(g, x, y, width, height, inverseShadow);
		}

		// paint a frame of the attention
		if (attentionProgress <= attentionSpan && attentionRect != null) {
			Rectangle desktop = getRectangle(content, "bounds");

			g.setColor(0xFF000000); // black
			g.setClip(0, 0, getWidth(), getHeight());

			for (int i = 0; i < attentionThickness; i++) {
				g.drawRect(desktop.x - i + ((attentionRect.x - desktop.x) * attentionProgress) / attentionSpan, desktop.y - i
						+ ((attentionRect.y - desktop.y) * attentionProgress) / attentionSpan,

				desktop.width + 2 * i + ((attentionRect.width - desktop.width) * attentionProgress) / attentionSpan - 1, desktop.height + 2 * i
						+ ((attentionRect.height - desktop.height) * attentionProgress) / attentionSpan - 1);
			}
		}

		if (pointerRect != null) {
			g.setClip(0, 0, getWidth(), getHeight());
			g.drawImage(pointer, pointerRect.x, pointerRect.y, Graphics.TOP | Graphics.LEFT);
		}

		if (canvasListener != null) {
			canvasListener.paintNotify(this);
		}

	}

	//rcs: scroll text area to end
	public void scrollToEnd(Object component, int y, int height) {
		Rectangle view = getRectangle(component, ":view");
		Rectangle port = getRectangle(component, ":port");
		try {
			view.y = Math.max(y + height - port.height, Math.min(view.y, y));
			repaint(component);
		} catch (Exception ex) {
		}
	}

	/**
	 * rcs: returns the focus owner name
	 */
	public String getFocusOwner() {
		return getString(focusowner, "name");
	}

	protected void initGradients() {
		int[][] pix = new int[2][block * block];
		int r1 = getRed(c_bg);
		int r2 = getRed(c_press);
		int g1 = getGreen(c_bg);
		int g2 = getGreen(c_press);
		int b1 = getBlue(c_bg);
		int b2 = getBlue(c_press);

		for (int i = 0; i < block; i++) {
			int cr = r1 - (r1 - r2) * i / block;
			int cg = g1 - (g1 - g2) * i / block;
			int cb = b1 - (b1 - b2) * i / block;
			int color = (255 << 24) | (cr << 16) | (cg << 8) | cb;
			for (int j = 0; j < block; j++) {
				pix[0][i * block + j] = color;
				pix[1][j * block + i] = color;
			}
		}
		hgradient = Image.createImage(block, block);
		hgradient.getGraphics().drawRGB(pix[0], 0, block, 0, 0, block, block, true);
		vgradient = Image.createImage(block, block);
		vgradient.getGraphics().drawRGB(pix[0], 0, block, 0, 0, block, block, true);
	}

	protected Rectangle getAbsoluteBounds(Object component) { // not for popups
		Rectangle bounds = getRectangle(component, "bounds");

		if (bounds == null)
			throw new RuntimeException("Component of type '" + getClass(component) + "' has not been initiated");

		int x = bounds.x;
		int y = bounds.y;

		int width = bounds.width;
		int height = bounds.height;

		Object parent = getParent(component);
		Rectangle parentBounds = null;
		while (parent != null) {
			parentBounds = getRectangle(parent, "bounds");

			if (getClass(parent) != "node") {

				Rectangle port = getRectangle(parent, ":port");
				Rectangle view = getRectangle(parent, ":view");
				if (port != null && view != null) {
					x -= view.x - port.x;
					y -= view.y - port.y;

					// keep bounds within view port horizontally.
					// so now x and y is relative to the parent top left corner
					// note: this is maybe not entirely correct, but working for now.
					if (x + width > port.width) {
						width = port.width - x;
					}
				}

				x += parentBounds.x;
				y += parentBounds.y;
			}

			parent = getParent(parent);
		}

		return new Rectangle(x, y, width, height);
	}

	/**
	 * Get the next focusable component given a directional key. This allowes for free-space navigation in 2D!
	 *
	 * @param g graphics object for visualization
	 * @param content the subtree from which to consider navigation
	 * @param key the direction key
	 * @return the most probably match in the given direction
	 *
	 * @author Thomas Rørvik Skjølberg
	 *
	 */

	public Object findNextFocuable(Graphics g, Object content, int key) {

		Object closest = null;

		Object reference = focusowner;

		if (getClass(focusowner) == "tabbedpane" || getClass(focusowner) == "menubar") {
			reference = get(focusowner, ":lead");
			if (reference == null) {
				reference = getItem(focusowner, getInteger(focusowner, "selected", 0));
			}
		}

		Rectangle absoluteReference;

		int referenceXPos;
		int referenceYPos;

		if (reference != null) {
			absoluteReference = getAbsoluteBounds(reference);
			referenceXPos = absoluteReference.x + absoluteReference.width / 2;
			referenceYPos = absoluteReference.y + absoluteReference.height / 2;
		} else {
			referenceXPos = referenceYPos = -1;
			absoluteReference = new Rectangle(-1, -1, 0, 0);
		}

		if (g != null) {
			g.setColor(GREEN);
			for (int i = 3; i < 6; i++) {
				g.drawRect(referenceXPos - i, referenceYPos - i, 2 * i, 2 * i);
			}
		}

		int closestDistance = Integer.MAX_VALUE / 2;

		int x = 0;
		int y = 0;

		for (Object next = null, parent = content; true; parent = next) {

			// go into or not!
			if (getClass(parent) == "tab") {
				Object tabParent = getParent(next);
				boolean skip = getItem(tabParent, getInteger(tabParent, "selected", 0)) != next;
				if (skip) {
					// dont go into
					next = null;
					// Skip " + getClass(parent);
				} else {
					// go into getClass(parent)
					next = get(parent, ":comp"); // check first subcomponent
				}
			} else if (getClass(parent) == "node") {
				if (getBoolean(next, "expanded", true)) {
					doNodeTree(g, next, x, y);
				}
				next = null; // skip
			} else if (getClass(parent) == "combobox" || getClass(parent) == "menu" || getClass(parent) == "list" || getClass(parent) == "tree"
					|| getClass(parent) == "table") {
				next = null;
			} else {
				next = get(parent, ":comp"); // check first subcomponent
			}

			if (next == null) { // check next component
				next = get(parent, ":next");
			} else {
				Rectangle r = getRectangle(parent, "bounds");

				if (r != null) {
					x += r.x;
					y += r.y;

				}

				Rectangle port = getRectangle(parent, ":port");
				Rectangle view = getRectangle(parent, ":view");

				if (port != null && view != null) {
					x += port.x - view.x;
					y += port.y - view.y;
				}
			}

			while (next == null) { // find the next of the parents, or the
				// topmost

				parent = getParent(parent); // current is not on the desktop

				if (parent != null) {
					Rectangle r = getRectangle(parent, "bounds");
					if (r != null) {
						x -= r.x;
						y -= r.y;
					}

					Rectangle port = getRectangle(parent, ":port");
					Rectangle view = getRectangle(parent, ":view");

					if (port != null && view != null) {
						x -= port.x - view.x;
						y -= port.y - view.y;
					}
				}

				if (parent == null) {
					// Exit: No parent for getClass(parent)
					return closest;
				}
				if (parent == content) {
					next = parent; // the topmost (desktop or modal dialog)
				} else {
					next = get(parent, ":next");
				}
			}

			Rectangle r = getRectangle(next, "bounds");
			if (r != null) {
				if (g != null) {
					g.setColor(BLUE);
				}

				String classname = getClass(next);

				if (classname == "list" || classname == "spinbox" || classname == "menu" || classname == "combobox" || classname == "checkbox"
						|| classname == "button" || classname == "tab" || classname == "item" || classname == "row" || classname == "node"
						|| classname == "textarea" || classname == "textfield" || classname == "passwordfield" || classname == "slider"
						|| classname == "choice" || classname == "list" || classname == "tree" || classname == "table") {

					if (getBoolean(next, "editable", true) || classname == "combobox") {

						if (isEnabledAndVisible(next) || classname == "tab" || classname == "item" || classname == "node") {
							if (g != null) {
								// draw diagonal line
								g.drawLine(x + r.x, y + r.y, x + r.x + r.width, y + r.y + r.height);
							}

							Object asKeyNavigates = get(next, "asKeyNavigates");

							if (key != -1 && (asKeyNavigates == null || !asKeyNavigates.equals("ignore"))) {
								// Key is valid

								int distance = getDistance(reference, referenceXPos, referenceYPos, absoluteReference, next, key);
								if (distance != -1 && distance < closestDistance) {
									// New closest
									closestDistance = distance;
									closest = next;
								} else if (distance == -1) { // Wrong direction

								}

							} // else key is invalid

						}
					} else {
						// next is not enabled and visible
					}
				} else {
					// custom component?
				}
			} else {

			}

			if (next == content) { // one focusable, no loop
				return closest;
			}
		} // hahaha
	}

	/**
	 *
	 * Get the weigthed distance from a center and a rectangle.
	 *
	 * TODO additional logic
	 *
	 * @param reference
	 * @param x the horizontal coordinate for the center distance
	 * @param y the vertical coordinate for the center distance
	 * @param absoluteBounds the rectangle to which border distance is absolute
	 * @param next
	 * @param key the directional key.
	 * @return -1 if travel in the wrong direction - else the weigthed relative distance
	 */

	public int getDistance(Object reference, int x, int y, Rectangle absoluteBounds, Object next, int key) {
		Object policy = null;

		policy = get(next, ":interactiveFocusPolicy");

		// feature: less 'sideways' sensitivity when vertical than horizontal movement

		if (policy == "center") {
			return getCenterDistance(x, y, key, next, 3, 10);
		} else if (policy == "edge") {
			return getBorderDistance(absoluteBounds, key, next, 3, 10);
		} else if (policy == null) {
			int border = getBorderDistance(absoluteBounds, key, next, 1, 10);
			int center = getCenterDistance(x, y, key, next, 1, 10);

			if (getClass(reference).equals("tab") && getClass(next).equals("tab")) { // workaround for tabs which share/overlap borders
				border = 0;
			}

			if (border == -1 || center == -1) {
				return -1;
			}
			return border + center;
		}
		return -1;
	}

	protected int getCenterDistance(int x, int y, int key, Object component, int sidewaysFactorX, int sidewaysFactorY) {

		Rectangle bounds = getAbsoluteBounds(component);

		int dx = bounds.x + bounds.width / 2 - x;
		int dy = bounds.y + bounds.height / 2 - y;

		if (key == KeyEvent.VK_RIGHT) {
			if (dx <= 0)
				return -1;
			return dx + sidewaysFactorY * Math.abs(dy);
		} else if (key == KeyEvent.VK_LEFT) {
			if (dx >= 0)
				return -1;
			return -dx + sidewaysFactorY * Math.abs(dy);
		} else if (key == KeyEvent.VK_UP) {
			if (dy >= 0)
				return -1;
			return -dy + sidewaysFactorX * Math.abs(dx);
		} else if (key == KeyEvent.VK_DOWN) {
			if (dy <= 0)
				return -1;
			return dy + sidewaysFactorX * Math.abs(dx);
		} else
			return -1;

	}

	protected int getBorderDistance(Rectangle absoluteBounds, int key, Object component, int sidewaysFactorX, int sidewaysFactorY) {

		Rectangle bounds = getAbsoluteBounds(component);

		int dx;
		if (absoluteBounds.x + absoluteBounds.width <= bounds.x) { // from is to the right
			if (key == KeyEvent.VK_LEFT)
				return -1;

			dx = bounds.x - (absoluteBounds.x + absoluteBounds.width);
		} else if (absoluteBounds.x >= bounds.x + bounds.width) { // from is to the left
			if (key == KeyEvent.VK_RIGHT)
				return -1;

			dx = absoluteBounds.x - (bounds.x + bounds.width);
		} else { // from is within
			if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
				return -1;
			}

			dx = 0;
		}

		int dy;
		if (absoluteBounds.y + absoluteBounds.height <= bounds.y) {
			if (key == KeyEvent.VK_UP)
				return -1;
			dy = bounds.y - (absoluteBounds.y + absoluteBounds.height);
		} else if (absoluteBounds.y >= bounds.y + bounds.height) {
			if (key == KeyEvent.VK_DOWN)
				return -1;

			dy = absoluteBounds.y - (bounds.y + bounds.height);
		} else {
			if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
				return -1;
			}
			dy = 0;
		}

		if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_UP) {
			return dy + sidewaysFactorX * dx;
		} else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT) {
			return dx + sidewaysFactorY * dy;
		}

		return -1;
	}

	protected void doNodeTree(Graphics g, Object root, int x, int y) {
		for (Object next = null, parent = root; true; parent = next) {

			next = get(parent, ":comp"); // check first subcomponent

			if (next == null) { // check next component
				next = get(parent, ":next");
			}

			if (next != null) {
				if (g != null)
					g.setColor(0xFF00FFFF); // cyan
				Rectangle r = getRectangle(next, "bounds");
				if (r != null) {
					if (g != null)
						g.drawLine(x + r.x, y + r.y, x + r.x + r.width, y + r.y + r.height);
				}

				if (!getBoolean(next, "expanded", true)) {
					next = null;
				}
			}

			while (next == null) { // find the next of the parents, or the
				// topmost
				parent = getParent(parent); // current is not on the desktop
				if (parent == null || getClass(parent) != "node") {
					// Exit: No/invalid parent
					return;
				}
				if (parent == root) {
					next = parent; // the topmost (desktop or modal dialog)
				} else {
					next = get(parent, ":next");
				}
			}

			if (next == root) { // one fucusable, no loop
				// Exit: Next was content
				return;
			}
		}
	}

	/**
	 * @param clipx
	 *            the cliping rectangle is relative to the component's parent
	 *            location similar to the component's bounds rectangle
	 * @param clipy
	 * @param clipwidth
	 * @param clipheight
	 * @throws java.lang.IllegalArgumentException
	 */
	protected void paint(Graphics g, int clipx, int clipy, int clipwidth, int clipheight, Object component, boolean enabled) {

		if (!getBoolean(component, "visible", true)) {
			return;
		}
		Rectangle bounds = getRectangle(component, "bounds");
		if (bounds == null) {
			return;
		}
		// negative component width indicates invalid component layout
		if (bounds.width < 0) {
			bounds.width = Math.abs(bounds.width);
			doLayout(component);
		}
		// return if the component was out of the cliping rectangle
		if ((clipx + clipwidth < bounds.x) || (clipx > bounds.x + bounds.width) || (clipy + clipheight < bounds.y) || (clipy > bounds.y + bounds.height)) {
			return;
		}
		// set the clip rectangle relative to the component location
		clipx -= bounds.x;
		clipy -= bounds.y;
		g.translate(bounds.x, bounds.y);
		// g.setClip(0, 0, bounds.width, bounds.height);
		String classname = getClass(component);
		boolean pressed = (mousepressed == component);
		boolean inside = (mouseinside == component) && ((mousepressed == null) || pressed);
		boolean focus = focusinside && (focusowner == component);
		enabled = getBoolean(component, "enabled", true); // enabled &&

		if ("label" == classname) {
			paint(component, 0, 0, bounds.width, bounds.height, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 0, 0, 0, 0, false,
					enabled ? 'e' : 'd', "left", true, false);
		} else if (("button" == classname) || ("togglebutton" == classname)) {
			boolean toggled = ("togglebutton" == classname) && getBoolean(component, "selected", false);
			boolean link = ("button" == classname) && (get(component, "type") == "link");
			if (link) {
				paint(component, 0, 0, bounds.width, bounds.height, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 0, 0, 0, 0, focus,
						enabled ? (pressed ? 'e' : 'l') : 'd', "center", true, enabled && (inside != pressed));
			} else { // disabled toggled
				char mode = enabled ? ((inside != pressed) ? 'h' : ((pressed || toggled) ? 'p' : 'g')) : 'd';
				paint(component, 0, 0, bounds.width, bounds.height, g, clipx, clipy, clipwidth, clipheight, true, true, true, true, 2, 5, 2, 5, focus, mode,
						"center", true, false);
				// (enabled && ("button" == classname) && get(component, "type")
				// == "default")...
			}
		} else if ("checkbox" == classname) {
			paint(component, 0, 0, bounds.width, bounds.height, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 0, block + 3, 0, 0, false,
					enabled ? 'e' : 'd', "left", true, false);

			boolean selected = getBoolean(component, "selected", false);
			String group = getString(component, "group", null);
			int border = enabled ? c_border : c_disable;
			int foreground = enabled ? ((inside != pressed) ? c_hover : (pressed ? c_press : c_ctrl)) : c_bg;
			int dy = (bounds.height - block + 2) / 2;
			if (group == null) {
				paintRect(g, 1, dy + 1, block - 2, block - 2, border, foreground, true, true, true, true, true);
			} else {
				g.setColor((foreground != c_ctrl) ? foreground : c_bg);
				//g.fillOval(1, dy + 1, block - 3 + evm, block - 3 + evm);
				g.fillRect(1, dy + 1, block - 3 + evm, block - 3 + evm);
				g.setColor(border);
				//g.drawOval(1, dy + 1, block - 3, block - 3);
				g.drawRect(1, dy + 1, block - 3, block - 3);
			}
			if (focus) {
				drawFocus(g, 0, 0, bounds.width - 1, bounds.height - 1);
			}
			if ((!selected && inside && pressed) || (selected && (!inside || !pressed))) {
				g.setColor(enabled ? c_text : c_disable);
				if (group == null) {
					g.fillRect(3, dy + block - 9, 2 + evm, 6 + evm);
					g.drawLine(3, dy + block - 4, block - 4, dy + 3);
					g.drawLine(4, dy + block - 4, block - 4, dy + 4);
				} else {
					g.fillRect(5, dy + 5, block - 10 + evm, block - 10 + evm);
					//g.fillOval(5, dy + 5, block - 10 + evm, block - 10 + evm);
					g.drawRect(4, dy + 4, block - 9, block - 9);
					//g.drawOval(4, dy + 4, block - 9, block - 9);
				}
			}
		} else if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Image icon = getIcon(component, "icon", null);
				int left = (icon != null) ? icon.getWidth() : 0;
				paintField(g, clipx, clipy, clipwidth, clipheight, component, bounds.width - block, bounds.height, focus, enabled, false, left);
				if (icon != null) {
					g.drawImage(icon, 2, (bounds.height - icon.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
				}
				paintArrow(g, bounds.width - block, 0, block, bounds.height, 'S', enabled, inside, pressed, "down", true, false, true, true, true);
			} else {
				paint(component, 0, 0, bounds.width, bounds.height, g, clipx, clipy, clipwidth, clipheight, true, true, true, true, 1, 1, 1, 1 + block, focus,
						enabled ? ((inside != pressed) ? 'h' : (pressed ? 'p' : 'g')) : 'd', "left", false, false);
				g.setColor(enabled ? c_text : c_disable);
				paintArrow(g, bounds.width - block, 0, block, bounds.height, 'S');
			}
		} else if (":combolist" == classname) {
			paintScroll(component, classname, pressed, inside, focus, false, enabled, g, clipx, clipy, clipwidth, clipheight);
		} else if (("textfield" == classname) || ("passwordfield" == classname)) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component, bounds.width, bounds.height, focus, enabled, ("passwordfield" == classname), 0);
		} else if ("textarea" == classname) {
			paintScroll(component, classname, pressed, inside, focus, true, enabled, g, clipx, clipy, clipwidth, clipheight);
		} else if ("tabbedpane" == classname) {
			int i = 0;
			Object selectedtab = null;
			int selected = getInteger(component, "selected", 0);
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
			boolean stacked = (placement == "stacked");
			int bx = stacked ? 0 : horizontal ? 2 : 1, by = stacked ? 0 : horizontal ? 1 : 2, bw = 2 * bx, bh = 2 * by;
			// paint tabs except the selected one
			int pcx = clipx, pcy = clipy, pcw = clipwidth, pch = clipheight;
			clipx = Math.max(0, clipx);
			clipy = Math.max(0, clipy);
			clipwidth = Math.min(bounds.width, pcx + pcw) - clipx;
			clipheight = Math.min(bounds.height, pcy + pch) - clipy;
			g.clipRect(clipx, clipy, clipwidth, clipheight); // intersection
			// of clip and
			// bound
			Object lead = get(component, ":lead");

			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				Rectangle r = getRectangle(tab, "bounds");
				if (selected != i) {
					boolean hover = inside && (mousepressed == null) && (insidepart == tab);
					boolean tabenabled = enabled && getBoolean(tab, "enabled", true);
					paint(tab, r.x + bx, r.y + by, r.width - bw, r.height - bh, g, clipx, clipy, clipwidth, clipheight, (placement != "bottom"),
							(placement != "right"), !stacked && (placement != "top"), (placement != "left"), 1, 3, 1, 3, focus && tab == lead,
							tabenabled ? (hover ? 'h' : 'g') : 'd', "left", true, false);
				} else {
					selectedtab = tab;
					// paint tabbedpane border
					paintBorderBackground(tab, (placement == "left") ? r.width - 1 : 0, stacked ? (r.y + r.height - 1) : (placement == "top") ? r.height - 1
							: 0, (horizontal || stacked) ? bounds.width : (bounds.width - r.width + 1), stacked ? (bounds.height - r.y - r.height + 1)
							: horizontal ? (bounds.height - r.height + 1) : bounds.height, g, true, true, true, true, enabled ? 'e' : 'd');
					Object comp = get(selectedtab, ":comp");
					if ((comp != null) && getBoolean(comp, "visible", true)) {
						clipx -= r.x;
						clipy -= r.y;
						g.translate(r.x, r.y); // relative to tab
						paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
						clipx += r.x;
						clipy += r.y;
						g.translate(-r.x, -r.y);
					}
				}
				i++;
			}

			// paint selected tab and its content
			if (selectedtab != null) {
				Rectangle r = getRectangle(selectedtab, "bounds");
				// paint selected tab
				int ph = stacked ? 3 : (horizontal ? 5 : 4);
				int pv = stacked ? 1 : (horizontal ? 2 : 3);
				paint(selectedtab, r.x, r.y, r.width, r.height, g, clipx, clipy, clipwidth, clipheight, (placement != "bottom"), (placement != "right"),
						!stacked && (placement != "top"), (placement != "left"), pv, ph, pv, ph, focus && selectedtab == lead, enabled ? 'b' : 'i', "left",
						true, false);
			}
			g.setClip(pcx, pcy, pcw, pch);
		} else if (("panel" == classname) || ("dialog" == classname)) {
			int titleheight = getInteger(component, ":titleheight", 0);
			if ("dialog" == classname) {
				paint(component, 0, 0, bounds.width, 3 + titleheight, g, clipx, clipy, clipwidth, clipheight, true, true, false, true, 1, 2, 1, 2, false, 'g',
						"left", false, false);
				int controlx = bounds.width - titleheight - 1;
				if (getBoolean(component, "closable", false)) {
					paint(component, g, controlx, 3, titleheight - 2, titleheight - 2, 'c');
					controlx -= titleheight;
				}
				if (getBoolean(component, "maximizable", false)) {
					paint(component, g, controlx, 3, titleheight - 2, titleheight - 2, 'm');
					controlx -= titleheight;
				}
				if (getBoolean(component, "iconifiable", false)) {
					paint(component, g, controlx, 3, titleheight - 2, titleheight - 2, 'i');
				}
				paintRect(g, 0, 3 + titleheight, bounds.width, bounds.height - 3 - titleheight, c_border, c_press, false, true, true, true, true); // lower part excluding titlebar
				paintBorderBackground(component, // content area
						3, 3 + titleheight, bounds.width - 6, bounds.height - 6 - titleheight, g, true, true, true, true, 'b');
			} else { // panel
				boolean border = getBoolean(component, "border", false);
				paintBorderBackground(component, 0, titleheight / 2, bounds.width, bounds.height - (titleheight / 2), g, border, border, border, border,
						enabled ? 'e' : 'd');
				paint(component, 0, 0, bounds.width, titleheight, // panel
						// title
						g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 0, 3, 0, 3, false, enabled ? 'x' : 'd', "left", false, false);
			}

			if (get(component, ":port") != null) {
				paintScroll(component, classname, pressed, inside, focus, false, enabled, g, clipx, clipy, clipwidth, clipheight);
			} else {
				for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
					paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
				}
			}
		} else if ("desktop" == classname) {
			paintRect(g, 0, 0, bounds.width, bounds.height, c_border, c_bg, false, false, false, false, true);
			paintReverse(g, clipx, clipy, clipwidth, clipheight, get(component, ":comp"), enabled);
			// g.setColor(Color.red); if (clip != null) g.drawRect(clipx, clipy,
			// clipwidth, clipheight);
			if ((tooltipowner != null) && (component == content)) {
				Rectangle r = getRectangle(tooltipowner, ":tooltipbounds");
				paintRect(g, r.x, r.y, r.width, r.height, c_border, c_bg, true, true, true, true, true);
				String text = getString(tooltipowner, "tooltip", null);
				g.setColor(c_text);
				g.drawString(text, r.x + 2, r.y + g.getFont().getBaselinePosition() + 2, Graphics.LEFT | Graphics.BASELINE); // +nullpointerexception
			}
		} else if ("spinbox" == classname) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component, bounds.width - block, bounds.height, focus, enabled, false, 0);
			paintArrow(g, bounds.width - block, 0, block, bounds.height / 2, 'N', enabled, inside, pressed, "up", true, false, false, true, true);
			paintArrow(g, bounds.width - block, bounds.height / 2, block, bounds.height - (bounds.height / 2), 'S', enabled, inside, pressed, "down", true,
					false, true, true, true);
		} else if ("progressbar" == classname) {
			int minimum = getInteger(component, "minimum", 0);
			int maximum = getInteger(component, "maximum", 100);
			int value = getInteger(component, "value", 0);
			// fixed by by Mike Hartshorn and Timothy Stack

			boolean horizontal = ("vertical" != get(component, "orientation"));
			int length;

			if (maximum - minimum != 0) {
				length = (value - minimum) * ((horizontal ? bounds.width : bounds.height) - 1) / (maximum - minimum);
			} else {
				length = 0;
			}
			paintRect(g, 0, 0, horizontal ? length : bounds.width, horizontal ? bounds.height : length, enabled ? c_border : c_disable, c_select, true, true,
					horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length, horizontal ? (bounds.width - length) : bounds.width, horizontal ? bounds.height
					: (bounds.height - length), enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
		} else if ("slider" == classname) {
			if (focus) {
				drawFocus(g, 0, 0, bounds.width - 1, bounds.height - 1);
			}
			int minimum = getInteger(component, "minimum", 0);
			int maximum = getInteger(component, "maximum", 100);
			int value = getInteger(component, "value", 0);
			boolean horizontal = ("vertical" != get(component, "orientation"));

			int length;
			if (maximum - minimum != 0) {
				length = (value - minimum) * ((horizontal ? bounds.width : bounds.height) - block) / (maximum - minimum);
			} else {
				length = 0;
			}

			// slider track used part
			paintRect(g, horizontal ? 0 : 3, horizontal ? 3 : 0, horizontal ? length : (bounds.width - 6), horizontal ? (bounds.height - 6) : length,
					enabled ? c_border : c_disable, c_bg, true, true, horizontal, !horizontal, true);

			// paint box
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length, horizontal ? block : bounds.width, horizontal ? bounds.height : block,
					enabled ? c_border : c_disable,
					// enabled ? c_ctrl : c_bg,

					// hvis enabled og hover så sett inn // TODO sett inn farger
					enabled ? focusowner == component ? c_hover : (insidepart == component /*
					 * || mouseinside ==
					 * component
					 */? c_hover : c_ctrl) : c_bg,
					// c_ctrl,
					true, true, true, true, true);

			// slider track used part
			paintRect(g, horizontal ? (block + length) : 3, horizontal ? 3 : (block + length), bounds.width - (horizontal ? (block + length) : 6),
					bounds.height - (horizontal ? 6 : (block + length)), enabled ? c_border : c_disable, c_bg, horizontal, !horizontal, true, true, true);
		} else if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			paintRect(g, horizontal ? divider : 0, horizontal ? 0 : divider, horizontal ? 5 : bounds.width, horizontal ? bounds.height : 5, c_border, c_bg,
					false, false, false, false, true);
			if (focus) {
				if (horizontal) {
					drawFocus(g, divider, 0, 4, bounds.height - 1);
				} else {
					drawFocus(g, 0, divider, bounds.width - 1, 4);
				}
			}
			g.setColor(enabled ? c_border : c_disable);
			int xy = horizontal ? bounds.height : bounds.width;
			int xy1 = Math.max(0, xy / 2 - 12);
			int xy2 = Math.min(xy / 2 + 12, xy - 1);
			for (int i = divider + 1; i < divider + 4; i += 2) {
				if (horizontal) {
					g.drawLine(i, xy1, i, xy2);
				} else {
					g.drawLine(xy1, i, xy2, i);
				}
			}
			Object comp1 = get(component, ":comp");
			if (comp1 != null) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp1, enabled);
				Object comp2 = get(comp1, ":next");
				if (comp2 != null) {
					paint(g, clipx, clipy, clipwidth, clipheight, comp2, enabled);
				}
			}
		} else if (("list" == classname) || ("table" == classname) || ("tree" == classname)) {
			paintScroll(component, classname, pressed, inside, focus, focus && (get(component, ":comp") == null), enabled, g, clipx, clipy, clipwidth,
					clipheight);
		} else if ("separator" == classname) {
			g.setColor(enabled ? c_border : c_disable);
			g.fillRect(0, 0, bounds.width + evm, bounds.height + evm);
		} else if ("menubar" == classname) {
			Object selected = get(component, "selected");
			int lastx = 0;
			Object lead = get(component, ":lead");

			for (Object menu = get(component, ":comp"); menu != null; menu = get(menu, ":next")) {
				Rectangle mb = getRectangle(menu, "bounds");
				if (clipx + clipwidth <= mb.x) {
					break;
				}
				if (clipx >= mb.x + mb.width) {
					continue;
				}
				boolean menuenabled = enabled && getBoolean(menu, "enabled", true);
				boolean armed = (selected == menu);
				boolean hoover = (selected == null) && (insidepart == menu);
				paint(menu, mb.x, 0, mb.width, bounds.height, g, clipx, clipy, clipwidth, clipheight, // TODO disabled
						armed, armed, true, armed, 1, 3, 1, 3, menu == lead && focusowner == component, enabled ? (menuenabled ? (armed ? 's' : (hoover ? 'h'
								: 'g')) : 'r') : 'd', "left", true, false);
				lastx = mb.x + mb.width;
			}
			paintRect(g, lastx, 0, bounds.width - lastx, bounds.height, enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, false, false, true, false,
					true);
		} else if (":popup" == classname) {
			paintRect(g, 0, 0, bounds.width, bounds.height, c_border, c_textbg, true, true, true, true, true);
			Object selected = get(component, "selected");
			for (Object menu = get(get(component, "menu"), ":comp"); menu != null; menu = get(menu, ":next")) {
				Rectangle r = getRectangle(menu, "bounds");
				if (clipy + clipheight <= r.y) {
					break;
				}
				if (clipy >= r.y + r.height) {
					continue;
				}
				String itemclass = getClass(menu);
				if (itemclass == "separator") {
					g.setColor(c_border);
					g.fillRect(r.x, r.y, bounds.width - 2 + evm, r.height + evm);
				} else {
					boolean armed = (selected == menu);
					boolean menuenabled = getBoolean(menu, "enabled", true);
					paint(menu, r.x, r.y, bounds.width - 2, r.height, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 2,
							(itemclass == "checkboxmenuitem") ? (block + 7) : 4, 2, 4, false, menuenabled ? (armed ? 's' : 't') : 'd', "left", true, false);
					if (itemclass == "checkboxmenuitem") {
						boolean checked = getBoolean(menu, "selected", false);
						String group = getString(menu, "group", null);
						g.translate(r.x + 4, r.y + 2);
						g.setColor(menuenabled ? c_border : c_disable);
						if (group == null) {
							g.drawRect(1, 1, block - 3, block - 3);
						} else {
							g.drawRect(1, 1, block - 3, block - 3);
							//g.drawOval(1, 1, block - 3, block - 3);
						}
						if (checked) {
							g.setColor(menuenabled ? c_text : c_disable);
							if (group == null) {
								g.fillRect(3, block - 9, 2 + evm, 6 + evm);
								g.drawLine(3, block - 4, block - 4, 3);
								g.drawLine(4, block - 4, block - 4, 4);
							} else {
								g.fillRect(5, 5, block - 10 + evm, block - 10 + evm);
								//g.fillOval(5, 5, block - 10 + evm, block - 10 + evm);
								g.drawRect(4, 4, block - 9, block - 9);
								//g.drawOval(4, 4, block - 9, block - 9);
							}
						}
						g.translate(-r.x - 4, -r.y - 2);
					}
					if (itemclass == "menu") {
						paintArrow(g, r.x + bounds.width - block, r.y, block, r.height, 'E');
					} else {
						String accelerator = getAccelerator(menu);
						if (accelerator != null) { // TODO
							g.drawString(accelerator, bounds.width - 4 - font.stringWidth(accelerator), r.y + 2 + 10, Graphics.LEFT | Graphics.BASELINE);
						}
					}
				}
			}
		} else if ("bean" == classname) {
			throw new RuntimeException("not implemented");
			/*
			g.clipRect(0, 0, bounds.width, bounds.height);
			((Component) get(component, "bean")).paint(g);
			g.setClip(clipx, clipy, clipwidth, clipheight);
			 */
		} else
			throw new IllegalArgumentException(classname);
		g.translate(-bounds.x, -bounds.y);
		clipx += bounds.x;
		clipy += bounds.y;
	}

	protected void paintReverse(Graphics g, int clipx, int clipy, int clipwidth, int clipheight, Object component, boolean enabled) {
		if (component != null) {
			Rectangle bounds = getRectangle(component, "bounds");
			if ((clipx < bounds.x) || (clipx + clipwidth > bounds.x + bounds.width) || (clipy < bounds.y) || (clipy + clipheight > bounds.y + bounds.height)) {
				paintReverse(g, clipx, clipy, clipwidth, clipheight, get(component, ":next"), enabled);
			}
			paint(g, clipx, clipy, clipwidth, clipheight, component, enabled);
		}
	}

	protected void paintField(Graphics g, int clipx, int clipy, int clipwidth, int clipheight, Object component, int width, int height, boolean focus,
			boolean enabled, boolean hidden, int left) {
		boolean editable = getBoolean(component, "editable", true);
		paintRect(g, 0, 0, width, height, enabled ? c_border : c_disable, editable ? getColor(component, "background", c_textbg) : c_bg, true, true, true,
				true, true);
		g.clipRect(1 + left, 1, width - left - 2, height - 2);

		String text = getString(component, "text", "");
		int offset = getInteger(component, ":offset", 0);
		Font currentfont = (Font) get(component, "font");
		if (currentfont != null) {
			g.setFont(currentfont);
		}
		Font fm = g.getFont();

		int caret = 0;
		if (focus) {
			int start = getInteger(component, "start", 0);
			int end = getInteger(component, "end", 0);
			caret = hidden ? (fm.charWidth('*') * end) : fm.stringWidth(text.substring(0, end));
			if (start != end) {
				int is = hidden ? (fm.charWidth('*') * start) : fm.stringWidth(text.substring(0, start));
				g.setColor(c_select);
				g.fillRect(2 + left - offset + Math.min(is, caret), 1, Math.abs(caret - is) + evm, height - 2 + evm);
			}
		}

		if (focus) { // draw caret
			g.setColor(c_focus);
			g.fillRect(1 + left - offset + caret, 1, 1 + evm, height - 2 + evm);
		}

		setColor(g, enabled ? getColor(component, "foreground", c_text) : c_disable);
		int fx = 2 + left - offset;
		int fy = (height + font.getBaselinePosition() - (font.getHeight() - font.getBaselinePosition())) / 2;
		if (hidden) {
			int fh = fm.charWidth('*');
			for (int i = text.length(); i > 0; i--) {
				g.drawString("*", fx, fy, Graphics.LEFT | Graphics.BASELINE);
				fx += fh;
			}
		} else {
			g.drawString(text, fx, fy, Graphics.LEFT | Graphics.BASELINE);
		}
		if (currentfont != null) {
			g.setFont(font);
		}
		g.setClip(clipx, clipy, clipwidth, clipheight);

		if (focus) { // draw dotted rectangle
			drawFocus(g, 1, 1, width - 3, height - 3);
		}
	}

	protected void setColor(Graphics g, int color) {
		if (g == null)
			throw new RuntimeException("Cannot set color on null graphics");
		g.setColor(color);
	}

	protected int getColor(Object component, String key, int defaultColor) {
		Object value = get(component, key);
		return (value != null) ? ((Integer) value).intValue() : defaultColor;
	}

	/**
	 * @param component
	 *            scrollable widget
	 * @param classname
	 * @param pressed
	 * @param inside
	 * @param focus
	 * @param enabled
	 * @param g
	 *            grahics context
	 * @param clipx
	 *            current cliping x location relative to the component
	 * @param clipy
	 *            y location of the cliping area relative to the component
	 * @param clipwidth
	 *            width of the cliping area
	 * @param clipheight
	 *            height of the cliping area
	 * @param header
	 *            column height
	 * @param topborder
	 *            bordered on the top if true
	 * @param border
	 *            define left, bottom, and right border if true
	 */
	protected void paintScroll(Object component, String classname, boolean pressed, boolean inside, boolean focus, boolean drawfocus, boolean enabled,
			Graphics g, int clipx, int clipy, int clipwidth, int clipheight) {
		Rectangle port = getRectangle(component, ":port");
		Rectangle horizontal = getRectangle(component, ":horizontal");
		Rectangle vertical = getRectangle(component, ":vertical");
		Rectangle view = getRectangle(component, ":view");

		if (horizontal != null) { // paint horizontal scrollbar
			int x = horizontal.x;
			int y = horizontal.y;
			int width = horizontal.width;
			int height = horizontal.height;
			paintArrow(g, x, y, block, height, 'W', enabled, inside, pressed, "left", true, true, true, false, true);
			paintArrow(g, x + width - block, y, block, height, 'E', enabled, inside, pressed, "right", true, false, true, true, true);

			int track = width - (2 * block);
			if (track < 10) {
				paintRect(g, x + block, y, track, height, enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
			} else {
				int knob = Math.max(track * port.width / view.width, 10);
				int decrease = view.x * (track - knob) / (view.width - port.width);
				paintRect(g, x + block, y, decrease, height, enabled ? c_border : c_disable, c_bg, false, true, true, false, true);
				paintRect(g, x + block + decrease, y, knob, height, enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, true, true, true, true, true);
				int n = Math.min(5, (knob - 4) / 3);
				g.setColor(enabled ? c_border : c_disable);
				int cx = (x + block + decrease) + (knob + 2 - n * 3) / 2;
				for (int i = 0; i < n; i++) {
					g.drawLine(cx + i * 3, y + 3, cx + i * 3, y + height - 5);
				}
				int increase = track - decrease - knob;
				paintRect(g, x + block + decrease + knob, y, increase, height, enabled ? c_border : c_disable, c_bg, false, false, true, true, true);
			}
		}

		if (vertical != null) { // paint vertical scrollbar
			int x = vertical.x;
			int y = vertical.y;
			int width = vertical.width;
			int height = vertical.height;
			paintArrow(g, x, y, width, block, 'N', enabled, inside, pressed, "up", true, true, false, true, false);
			paintArrow(g, x, y + height - block, width, block, 'S', enabled, inside, pressed, "down", false, true, true, true, false);

			int track = height - (2 * block);
			if (track < 10) {
				paintRect(g, x, y + block, width, track, enabled ? c_border : c_disable, c_bg, true, true, true, true, false);
			} else {
				int knob = Math.max(track * port.height / view.height, 10);
				int decrease = view.y * (track - knob) / (view.height - port.height);
				paintRect(g, x, y + block, width, decrease, enabled ? c_border : c_disable, c_bg, true, false, false, true, false);
				paintRect(g, x, y + block + decrease, width, knob, enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, true, true, true, true, false);
				int n = Math.min(5, (knob - 4) / 3);
				g.setColor(enabled ? c_border : c_disable);
				int cy = (y + block + decrease) + (knob + 2 - n * 3) / 2;
				for (int i = 0; i < n; i++) {
					g.drawLine(x + 3, cy + i * 3, x + width - 5, cy + i * 3);
				}
				int increase = track - decrease - knob;
				paintRect(g, x, y + block + decrease + knob, width, increase, enabled ? c_border : c_disable, c_bg, false, false, true, true, false);
			}
		}

		boolean hneed = (horizontal != null);
		boolean vneed = (vertical != null);
		if (("panel" != classname) && ("dialog" != classname) && (("textarea" != classname) || getBoolean(component, "border", true))) {
			paintRect(g, port.x - 1, port.y - 1, port.width + (vneed ? 1 : 2), port.height + (hneed ? 1 : 2), enabled ? c_border : c_disable, getColor(
					component, "background", c_textbg), true, true, !hneed, !vneed, true); // TODO
			// not
			// editable
			// textarea
			// background
			// color
			if ("table" == classname) {
				Object header = get(component, "header");
				if (header != null) {
					int[] columnwidths = (int[]) get(component, ":widths");
					Object column = get(header, ":comp");
					int x = 0;
					g.clipRect(0, 0, port.width + 2, port.y); // not 2 and
					// decrease clip
					// area...
					for (int i = 0; i < columnwidths.length; i++) {
						if (i != 0) {
							column = get(column, ":next");
						}
						boolean lastcolumn = (i == columnwidths.length - 1);
						int width = lastcolumn ? (view.width - x + 2) : columnwidths[i];

						paint(column, x - view.x, 0, width, port.y - 1, g, clipx, clipy, clipwidth, clipheight, true, true, false, lastcolumn, 1, 1, 0, 0,
								false, enabled ? 'g' : 'd', "left", false, false);

						Object sort = get(column, "sort"); // "none", "ascent",
						// "descent"
						if (sort != null) {
							paintArrow(g, x - view.x + width - block, 0, block, port.y, (sort == "ascent") ? 'S' : 'N');
						}
						x += width;
					}
					g.setClip(clipx, clipy, clipwidth, clipheight);
				}
			}
		}
		int x1 = Math.max(clipx, port.x);
		int x2 = Math.min(clipx + clipwidth, port.x + port.width);
		int y1 = Math.max(clipy, port.y);
		int y2 = Math.min(clipy + clipheight, port.y + port.height);
		if ((x2 > x1) && (y2 > y1)) {
			g.clipRect(x1, y1, x2 - x1, y2 - y1);
			g.translate(port.x - view.x, port.y - view.y);

			paint(component, classname, focus, enabled, g, view.x - port.x + x1, view.y - port.y + y1, x2 - x1, y2 - y1, port.width, view.width);

			g.translate(view.x - port.x, view.y - port.y);
			g.setClip(clipx, clipy, clipwidth, clipheight);
		}
		if (focus && drawfocus) { // draw dotted rectangle around the viewport
			drawFocus(g, port.x, port.y, port.width - 1, port.height - 1);
		}
	}

	/**
	 * Paint scrollable content
	 *
	 * @param component
	 *            a panel
	 */
	protected void paint(Object component, String classname, boolean focus, boolean enabled, Graphics g, int clipx, int clipy, int clipwidth, int clipheight,
			int portwidth, int viewwidth) {
		if ("textarea" == classname) {
			char[] chars = (char[]) get(component, ":text");
			int start = focus ? getInteger(component, "start", 0) : 0;
			int end = focus ? getInteger(component, "end", 0) : 0;
			int is = Math.min(start, end);
			int ie = Math.max(start, end);
			Font customfont = (Font) get(component, "font");
			if (customfont != null) {
				g.setFont(customfont);
			}
			Font fm = g.getFont();
			int fontascent = fm.getBaselinePosition();
			int fontheight = fm.getHeight();
			int ascent = 1;

			int textcolor = enabled ? getColor(component, "foreground", c_text) : c_disable;
			for (int i = 0, j = 0; j <= chars.length; j++) {
				if ((j == chars.length) || (chars[j] == '\n')) {
					if (clipy + clipheight <= ascent) {
						break;
					} // the next lines are bellow paint rectangle
					if (clipy < ascent + fontheight) { // this line is not
						// above painting area
						if (focus && (is != ie) && (ie >= i) && (is <= j)) {
							int xs = (is < i) ? -1 : ((is > j) ? (viewwidth - 1) : fm.charsWidth(chars, i, is - i));
							int xe = ((j != -1) && (ie > j)) ? (viewwidth - 1) : fm.charsWidth(chars, i, ie - i);
							g.setColor(c_select);
							g.fillRect(1 + xs, ascent, xe - xs + evm, fontheight + evm);
						}
						g.setColor(textcolor);
						g.drawChars(chars, i, j - i, 1, ascent + fontascent, Graphics.LEFT | Graphics.BASELINE);
						if (focus && (end >= i) && (end <= j)) {
							int caret = fm.charsWidth(chars, i, end - i);
							g.setColor(c_focus);
							g.fillRect(caret, ascent, 1 + evm, fontheight + evm);
						}
					}
					ascent += fontheight;
					i = j + 1;
				}
			}
			if (customfont != null) {
				g.setFont(font);
			} // restore the default font
		} else if (":combolist" == classname) {
			Object lead = get(component, ":lead");
			for (Object choice = get(get(component, "combobox"), ":comp"); choice != null; choice = get(choice, ":next")) {
				Rectangle r = getRectangle(choice, "bounds");
				if (clipy + clipheight <= r.y) {
					break;
				}
				if (clipy >= r.y + r.height) {
					continue;
				}
				paint(choice, r.x, r.y, portwidth, r.height, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 2, 4, 2, 4, false, getBoolean(
						choice, "enabled", true) ? ((lead == choice) ? 's' : 't') : 'd', "left", false, false);
			}
		} else if (("panel" == classname) || ("dialog" == classname)) {
			for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
			}
		} else { // if (("list" == classname) || ("table" == classname) ||
			// ("tree" == classname))
			Object lead = get(component, ":lead");
			int[] columnwidths = ("table" == classname) ? ((int[]) get(component, ":widths")) : null;
			boolean line = getBoolean(component, "line", true);
			int iline = line ? 1 : 0;
			boolean angle = ("tree" == classname) && getBoolean(component, "angle", false);
			for (Object item = get(component, ":comp"), next = null; item != null; item = next) {
				if (focus && (lead == null)) {
					set(component, ":lead", lead = item); // draw first item
					// focused when lead
					// is null
				}
				Rectangle r = getRectangle(item, "bounds");
				if (clipy + clipheight <= r.y) {
					break;
				} // clip rectangle is above
				boolean subnode = false;
				boolean expanded = false;
				if ("tree" != classname) {
					next = get(item, ":next");
				} else {
					subnode = (next = get(item, ":comp")) != null;
					expanded = subnode && getBoolean(item, "expanded", true);
					if (!expanded) {
						for (Object node = item; (node != component) && ((next = get(node, ":next")) == null); node = getParent(node))
							;
					}
				}
				if (clipy >= r.y + r.height + iline) {
					if (angle) { // TODO draw dashed line
						Object nodebelow = get(item, ":next");
						if (nodebelow != null) { // and the next node is
							// bellow clipy
							g.setColor(c_bg);
							int x = r.x - block / 2;
							g.drawLine(x, r.y, x, getRectangle(nodebelow, "bounds").y);
						}
					}
					continue; // clip rectangle is bellow
				}

				boolean selected = getBoolean(item, "selected", false);
				paintRect(g, ("tree" != classname) ? 0 : r.x, r.y, ("tree" != classname) ? viewwidth : r.width, r.height, c_border, selected ? c_select
						: c_textbg, false, false, false, false, true);
				if (focus && (lead == item)) { // focused
					drawFocus(g, ("tree" != classname) ? 0 : r.x, r.y, (("tree" != classname) ? viewwidth : r.width) - 1, r.height - 1);
				}
				if (line) {
					g.setColor(c_bg);
					g.drawLine(0, r.y + r.height, viewwidth, r.y + r.height);
				}
				if ("table" != classname) { // list or tree
					boolean itemenabled = enabled && getBoolean(item, "enabled", true);
					paint(item, r.x, r.y, viewwidth, r.height, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 1, 3, 1, 3, false,
							itemenabled ? 'e' : 'd', "left", false, false);
					if ("tree" == classname) {
						int x = r.x - block / 2;
						int y = r.y + (r.height - 1) / 2;
						if (angle) {
							g.setColor(c_bg);
							g.drawLine(x, r.y, x, y);
							g.drawLine(x, y, r.x - 1, y);
							Object nodebelow = get(item, ":next");
							if (nodebelow != null) {
								g.drawLine(x, y, x, getRectangle(nodebelow, "bounds").y);
							}
						}
						if (subnode) {
							paintRect(g, x - 4, y - 4, 9, 9, itemenabled ? c_border : c_disable, itemenabled ? c_ctrl : c_bg, true, true, true, true, true);
							g.setColor(itemenabled ? c_text : c_disable);
							g.drawLine(x - 2, y, x + 2, y);
							if (!expanded) {
								g.drawLine(x, y - 2, x, y + 2);
							}
						}
					}
				} else { // table
					int i = 0;
					int x = 0;
					for (Object cell = get(item, ":comp"); cell != null; cell = get(cell, ":next")) {
						if (clipx + clipwidth <= x) {
							break;
						}
						// column width is defined by header calculated in
						// layout, otherwise is 80
						int iwidth = 80; // TODO sett denne i xml'n
						if ((columnwidths != null) && (columnwidths.length > i)) {
							iwidth = (i != columnwidths.length - 1) ? columnwidths[i] : Math.max(iwidth, viewwidth - x);
						}
						if (clipx < x + iwidth) {
							boolean cellenabled = enabled && getBoolean(cell, "enabled", true);
							paint(cell, r.x + x, r.y, iwidth, r.height - 1, g, clipx, clipy, clipwidth, clipheight, false, false, false, false, 1, 1, 1, 1,
									false, cellenabled ? 'e' : 'd', "left", false, false);
						}
						i++;
						x += iwidth;
					}
				}
			}
		}
	}

	protected void paintRect(Graphics g, int x, int y, int width, int height, int border, int bg, boolean top, boolean left, boolean bottom, boolean right,
			boolean horizontal) {

		if ((width <= 0) || (height <= 0))
			return;
		g.setColor(border);
		if (top) {
			g.drawLine(x + width - 1, y, x, y);
			y++;
			height--;
			if (height <= 0)
				return;
		}
		if (left) {
			g.drawLine(x, y, x, y + height - 1);
			x++;
			width--;
			if (width <= 0)
				return;
		}
		if (bottom) {
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			height--;
			if (height <= 0)
				return;
		}
		if (right) {
			g.drawLine(x + width - 1, y + height - 1, x + width - 1, y);
			width--;
			if (width <= 0)
				return;
		}

		if (bg == c_ctrl) {
			fill(g, x, y, width, height, horizontal);
		} else {
			g.setColor(bg);
			g.fillRect(x, y, width + evm, height + evm);
		}
	}

	/**
	 * Fill the given rectangle with gradient
	 */

	protected void fill(Graphics g, int x, int y, int width, int height, boolean horizontal) {
		if (horizontal) {
			int part;
			int offset;
			if (height > block) {
				setColor(g, c_bg);
				g.fillRect(x, y, width + evm, height - block + evm);

				offset = height - block;
				part = 0;
			} else {
				part = block - height;
				offset = 0;
			}

			for (int i = 0; i < width; i += block) {
				g.drawRegion(hgradient, 0, part, Math.min(block, width - i) + evm, block - part + evm, Sprite.TRANS_NONE, x + Math.min(i, width) + evm, y + evm
						+ offset, Graphics.TOP | Graphics.LEFT);
			}
		} else {
			int part;
			int offset;
			if (width > block) {
				setColor(g, c_bg);
				g.fillRect(x, y, width - block + evm, height + evm);

				offset = width - block;
				part = 0;
			} else {
				part = block - width;
				offset = 0;
			}

			for (int i = 0; i < height; i += block) {
				g.drawRegion(hgradient, 0, part, Math.min(block, height - i) + evm, block - part + evm, Sprite.TRANS_ROT90, x + evm + offset, y
						+ Math.min(i, height) + evm, Graphics.TOP | Graphics.LEFT);
			}

		}
	}

	/**
	 *
	 * Key press key pre-processing.
	 *
	 * TODO abc type field in the upper right corner, mobile style.
	 *
	 */

	protected void keyPressed(int keyCode) {
		if ((popupowner != null) || (focusowner != null)) {
			hideTip();
			int keychar = 0, key = 0;

			switch (keyCode) {
			// case Canvas.KEY_NUM2:
			case Canvas.UP:
				key = KeyEvent.VK_UP;
				keychar = 0;
				break;
			// case Canvas.KEY_NUM8:
			case Canvas.DOWN:
				key = KeyEvent.VK_DOWN;
				keychar = 0;
				break;
			// case Canvas.KEY_NUM4:
			case Canvas.LEFT:
				key = KeyEvent.VK_LEFT;
				keychar = 0;
				break;
			// case Canvas.KEY_NUM6:
			case Canvas.RIGHT:
				key = KeyEvent.VK_RIGHT;
				keychar = 0;
				break;
			// case Canvas.KEY_NUM5:
			case Canvas.FIRE:
				key = KeyEvent.VK_ENTER;
				keychar = 0;
				break;
			}

			if (key != 0) {
				KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, 0);
				event.setKey(key);
				event.setKeyChar(keychar);
				processEvent(event);
			} else {
				// abc, def, ghi and so on
				// do we want another character?
				Object component = (popupowner != null) ? popupowner : focusowner;
				if (isDigitExclusive(component)) {
					// TODO uniform constants
					switch (keyCode) {
					case Canvas.KEY_NUM0: {
						keychar = KeyEvent.VK_0;
						break;
					}
					case Canvas.KEY_NUM1: {
						keychar = KeyEvent.VK_1;
						break;
					}
					case Canvas.KEY_NUM2: {
						keychar = KeyEvent.VK_2;
						break;
					}
					case Canvas.KEY_NUM3: {
						keychar = KeyEvent.VK_3;
						break;
					}
					case Canvas.KEY_NUM4: {
						keychar = KeyEvent.VK_4;
						break;
					}
					case Canvas.KEY_NUM5: {
						keychar = KeyEvent.VK_5;
						break;
					}
					case Canvas.KEY_NUM6: {
						keychar = KeyEvent.VK_6;
						break;
					}
					case Canvas.KEY_NUM7: {
						keychar = KeyEvent.VK_7;
						break;
					}
					case Canvas.KEY_NUM8: {
						keychar = KeyEvent.VK_8;
						break;
					}
					case Canvas.KEY_NUM9: {
						keychar = KeyEvent.VK_9;
						break;
					}
					default: {
						if (isKey(keyCode, C)) {
							keyCode = KeyEvent.VK_BACK_SPACE;
						} else {
							return;
						}
					}
					}

					processKeyPress(component, false, false, 1, keychar, keyCode, false);
				} else {
					if (keyCode == caseKey) {
						lowerCase = !lowerCase;
					} else {
						char[] keys = getNumKeyCharMapping(keyCode);
						if (keys == null) {
							if (isKey(keyCode, C)) {
								processKeyPress(component, false, false, 1, 0, KeyEvent.VK_BACK_SPACE, false);
							}
							return; // no mapping exists
						}

						long time = System.currentTimeMillis();

						boolean replace;
						if (keyCode != lastKey || lastKeyTimestamp + keyDelay < time) {
							// add new key
							lastKeyIndex = 0;

							replace = false;
						} else {
							// change current key
							if (lastKeyIndex + 1 < keys.length) {
								lastKeyIndex++;
							} else {
								lastKeyIndex = 0;
							}

							replace = true;
						}

						lastKeyTimestamp = time;
						lastKey = keyCode;

						keychar = (lowerCase ? keys[lastKeyIndex] : Character.toUpperCase(keys[lastKeyIndex]));

						processKeyPress(component, false, false, 1, keychar, keyCode, replace);
					}
				}
			}
		}
	}

	/**
	 *
	 * Does the component only accept digits?
	 *
	 * TODO configurable as property?
	 *
	 * @param component
	 * @return
	 */

	protected boolean isDigitExclusive(Object component) {
		if (getClass(component) == "spinbox") {
			return true;
		}
		return false;
	}

	protected void keyRepeated(int keyCode) {
		keyPressed(keyCode);
	}

	protected void paintArrow(Graphics g, int x, int y, int width, int height, char dir, boolean enabled, boolean inside, boolean pressed, String part,
			boolean top, boolean left, boolean bottom, boolean right, boolean horizontal) {
		inside = inside && (insidepart == part);
		pressed = pressed && (pressedpart == part);
		paintRect(g, x, y, width, height, enabled ? c_border : c_disable, enabled ? ((inside != pressed) ? c_hover : (pressed ? c_press : c_ctrl)) : c_bg, top,
				left, bottom, right, horizontal);
		setColor(g, enabled ? c_text : c_disable);
		paintArrow(g, x + (left ? 1 : 0), y + (top ? 1 : 0), width - (left ? 1 : 0) - (right ? 1 : 0), height - (top ? 1 : 0) - (bottom ? 1 : 0), dir);
	}

	protected void paintArrow(Graphics g, int x, int y, int width, int height, char dir) {
		int cx = x + width / 2 - 2;
		int cy = y + height / 2 - 2;
		for (int i = 0; i < 4; i++) {
			if (dir == 'N') { // north
				g.drawLine(cx + 1 - i, cy + i, cx + 1/* 2 */+ i, cy + i);
			} else if (dir == 'W') { // west
				g.drawLine(cx + i, cy + 1 - i, cx + i, cy + 1/* 2 */+ i);
			} else if (dir == 'S') { // south
				g.drawLine(cx + 1 - i, cy + 4 - i, cx + 1/* 2 */+ i, cy + 4 - i);
			} else { // east
				g.drawLine(cx + 4 - i, cy + 1 - i, cx + 4 - i, cy + 1/* 2 */+ i);
			}
		}
	}

	/**
	 * Paint component's borders and background
	 */
	protected void paintBorderBackground(Object component, int x, int y, int width, int height, Graphics g, boolean top, boolean left, boolean bottom,
			boolean right, char mode) {
		if ((width <= 0) || (height <= 0)) {
			return;
		}

		if (top || left || bottom || right) { // draw border
			setColor(g, ((mode != 'd') && (mode != 'i')) ? c_border : c_disable);
			if (top) {
				g.drawLine(x + width - 1, y, x, y);
				y++;
				height--;
				if (height <= 0) {
					return;
				}
			}
			if (left) {
				g.drawLine(x, y, x, y + height - 1);
				x++;
				width--;
				if (width <= 0) {
					return;
				}
			}
			if (bottom) {
				g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
				height--;
				if (height <= 0) {
					return;
				}
			}
			if (right) {
				g.drawLine(x + width - 1, y + height - 1, x + width - 1, y);
				width--;
				if (width <= 0) {
					return;
				}
			}
		}

		Integer background = (Integer) get(component, "background");

		switch (mode) {
		case 'e':
		case 'l':
		case 'd':
			if (background == null) {
				return;
			}
			break;
		case 'g':
		case 'r':
			if (background == null) {
				fill(g, x, y, width, height, true);
				return;
			}
		case 'x':
			return;
		case 'b':
		case 'i':
			if (background == null) {
				g.setColor(c_bg);
			} else {
				g.setColor(background.intValue());
			}
			break;
		case 'h':
			if (background != null) {
				g.setColor(brighter(background.intValue()));
			} else {
				g.setColor(c_hover);
			}
			break;
		case 'p':
			if (background != null) {
				g.setColor(darker(background.intValue()));
			} else {
				g.setColor(c_press);
			}
			break;
		case 't':
			if (background == null) {
				g.setColor(c_textbg);
			} else {
				g.setColor(background.intValue());
			}
			break;
		case 's':
			g.setColor(c_select);
			break;
		default:
			throw new IllegalArgumentException();
		}
		g.fillRect(x, y, width + evm, height + evm);
	}

	protected void paint(Object component, Graphics g, int x, int y, int width, int height, char type) {
		paintBorderBackground(component, x, y, width, height, g, true, true, true, true, 'g');
		g.setColor(0xFF000000); // black
		switch (type) {
		case 'c': // closable dialog button
			g.drawLine(x + 3, y + 4, x + width - 5, y + height - 4);
			g.drawLine(x + 3, y + 3, x + width - 4, y + height - 4);
			g.drawLine(x + 4, y + 3, x + width - 4, y + height - 5);
			g.drawLine(x + width - 5, y + 3, x + 3, y + height - 5);
			g.drawLine(x + width - 4, y + 3, x + 3, y + height - 4);
			g.drawLine(x + width - 4, y + 4, x + 4, y + height - 4);
			break;
		case 'm': // maximizable dialog button
			g.drawRect(x + 3, y + 3, width - 7, height - 7);
			g.drawLine(x + 4, y + 4, x + width - 5, y + 4);
			break;
		case 'i': // iconifiable dialog button
			g.fillRect(x + 3, y + height - 5, width - 6, 2);
			break;
		}
	}

	/**
	 * Paint component icon and text (using default or custom font)
	 *
	 * @param mnemonic
	 *            find mnemonic index and underline text
	 */
	protected void paint(Object component, int x, int y, int width, int height, Graphics g, int clipx, int clipy, int clipwidth, int clipheight, boolean top,
			boolean left, boolean bottom, boolean right, int toppadding, int leftpadding, int bottompadding, int rightpadding, boolean focus, char mode,
			String alignment, boolean mnemonic, boolean underline) {
		paintBorderBackground(component, x, y, width, height, g, top, left, bottom, right, mode);
		if (top) {
			y++;
			height--;
		}
		if (left) {
			x++;
			width--;
		}
		if (bottom) {
			height--;
		}
		if (right) {
			width--;
		}
		if ((width <= 0) || (height <= 0)) {
			return;
		}

		if (focus) {
			drawFocus(g, x + 1, y + 1, width - 3, height - 3);
		}

		String text = getString(component, "text", null);
		Image icon = getIcon(component, "icon", null);
		if ((text == null) && (icon == null)) {
			return;
		}

		x += leftpadding;
		y += toppadding;
		width -= leftpadding + rightpadding;
		height -= toppadding + bottompadding;

		alignment = getString(component, "alignment", alignment);
		Font customfont = (text != null) ? (Font) get(component, "font") : null;
		if (customfont != null) {
			g.setFont(customfont);
		}

		Font fm = null;
		int tw = 0, th = 0;
		int ta = 0;
		if (text != null) {
			fm = g.getFont();
			tw = fm.stringWidth(text);
			ta = fm.getBaselinePosition();
			th = fm.getHeight() - fm.getBaselinePosition() + ta;
		}
		int iw = 0, ih = 0;
		if (icon != null) {
			iw = icon.getWidth();
			ih = icon.getHeight();
			if (text != null) {
				iw += 2;
			}
		}

		boolean clipped = (tw + iw > width) || (th > height) || (ih > height);
		int cx = x;
		if ("center" == alignment) {
			cx += (width - tw - iw) / 2;
		} else if ("right" == alignment) {
			cx += width - tw - iw;
		}

		if (clipped) {
			g.clipRect(x, y, width, height);
		}
		if (mode == 'x') {
			g.drawLine(cx, y + height / 2, cx + iw + tw, y + height / 2);
		}
		if (icon != null) {
			g.drawImage(icon, cx, y + (height - ih) / 2, Graphics.TOP | Graphics.LEFT);
			cx += iw;
		}
		if (text != null) {
			int foreground = getInteger(component, "foreground", (mode == 'l') ? 0x000000FF // blue
					: (((mode != 'd') && (mode != 'r')) ? c_text : c_disable));
			g.setColor(foreground);
			int ty = y + (height - th) / 2 + ta;
			g.drawString(text, cx, ty, Graphics.LEFT | Graphics.BASELINE);
			if (mnemonic) {
				int imnemonic = getInteger(component, "mnemonic", -1);
				if ((imnemonic != -1) && (imnemonic < text.length())) {
					int mx = cx + fm.stringWidth(text.substring(0, imnemonic));
					g.drawLine(mx, ty + 1, mx + fm.charWidth(text.charAt(imnemonic)), ty + 1);
				}
			}
			if (underline) { // for link button
				g.drawLine(cx, ty + 1, cx + tw, ty + 1);
			}
		}
		if (clipped) {
			g.setClip(clipx, clipy, clipwidth, clipheight);
		}

		if (customfont != null) {
			g.setFont(font);
		} // restore the default font
	}

	protected void drawFocus(Graphics g, int x, int y, int width, int height) {
		setColor(g, c_focus);
		int x2 = x + 1 - height % 2;
		for (int i = 0; i <= width; i += 2) {
			g.fillRect(x + i, y, 1, 1);
			g.fillRect(x2 + i, y + height, 1, 1);
		}
		int y2 = y - width % 2;
		for (int i = 2; i <= height; i += 2) {
			g.fillRect(x, y + i, 1, 1);
			g.fillRect(x + width, y2 + i, 1, 1);
		}
	}

	/**
	 * A second thread is used to repeat value change events for scrollbar or
	 * spinbox during the mouse is pressed, or to pop up tooltip
	 */
	public synchronized void run() {
		while (timer == Thread.currentThread()) {

			try {
				if (watch == 0) {
					wait(0);
				} else {
					long current = System.currentTimeMillis();
					if (watch > current) {
						wait(watch - current);
					} else {
						watch = 0;
						watch: if ((watchdelay == 300L) || (watchdelay == 60L)) {
							if (processScroll(mousepressed, pressedpart)) {
								setTimer(60L);
							}
						} else if ((watchdelay == 375L) || (watchdelay == 75L)) {
							if (processSpin(mousepressed, pressedpart)) {
								setTimer(75L);
							}
						} else if (watchdelay == 750L) {
							showTip();
						} else if (watchdelay == 1500L) {
							showTip();

							setTimer(3000L);
						} else if (watchdelay == 3000L && focusowner != null) {
							// Start attention
							if (popupowner != null) {
								Object last = null;

								for (Object i = get(popupowner, ":popup"); i != null; i = get(i, ":popup")) {
									last = i;
								}

								Object target = get(last, "selected");

								if (target != null) {
									// popups have absolute position bounds
									Rectangle next = getRectangle(last, "bounds");

									// relative to the desktop rectangle or the popup owner ? TODO
									int x = next.x;
									int y = next.y;

									Rectangle targetBounds = getRectangle(target, "bounds");

									paintAttention(new Rectangle(x + targetBounds.x, y + targetBounds.y, next.width, targetBounds.height));

									break watch;
								}
							}
							Rectangle target = getFocusCenter();

							if (target != null) {
								paintAttention(target);
							} else {
								attentionProgress = attentionSpan;
							}
						} else if (watchdelay == 3500L) { // rcs: auto hide tip
							hideTip();
						}
					}
				}
			} catch (InterruptedException ie) {
				// ignore
			}
		}
	}

	/**
	 *
	 * Get the current focus center rectangle
	 *
	 * @return
	 */

	protected Rectangle getFocusCenter() {

		if (popupowner != null) {
			Object last = null;

			for (Object i = get(popupowner, ":popup"); i != null; i = get(i, ":popup")) {
				last = i;
			}

			Object target = get(last, "selected");

			if (target != null) {
				// popups have absolute position bounds
				Rectangle next = getRectangle(last, "bounds");

				// relative to the desktop rectangle or the popup owner ? TODO
				int x = next.x;
				int y = next.y;

				Rectangle targetBounds = getRectangle(target, "bounds");

				return new Rectangle(x + targetBounds.x, y + targetBounds.y, next.width, targetBounds.height);
			}
		}

		Object classname = getClass(focusowner);

		if (classname == "tabbedpane" || classname == "menubar" || classname == "tree" || classname == "list" || classname == "table") {
			Object target = get(focusowner, ":lead");
			if (target == null) {
				target = getItem(focusowner, getInteger(focusowner, "selected", 0));
			}
			return getAbsoluteBounds(target);
		} else {
			return getAbsoluteBounds(focusowner);
		}
	}

	/**
	 *
	 * Paint thick rectangle of decreasing size so that the current point of focus easily can be seen.
	 *
	 * @param attentionRect
	 * @throws InterruptedException
	 */

	protected void paintAttention(Rectangle attentionRect) throws InterruptedException {
		this.attentionRect = attentionRect;
		attentionProgress = 0;

		Rectangle desktop = getRectangle(content, "bounds");
		do {

			repaint(desktop.x - attentionThickness + ((attentionRect.x - desktop.x) * (attentionProgress - attentionDelta)) / attentionSpan, desktop.y
					- attentionThickness + ((attentionRect.y - desktop.y) * (attentionProgress - attentionDelta)) / attentionSpan,

			desktop.width + 2 * attentionThickness + ((attentionRect.width - desktop.width) * (attentionProgress - attentionDelta)) / attentionSpan,
					desktop.height + 2 * attentionThickness + ((attentionRect.height - desktop.height) * (attentionProgress - attentionDelta)) / attentionSpan);

			Thread.sleep(50);
			attentionProgress += attentionDelta;
		} while (attentionProgress <= attentionSpan + 1); // paint is untill attentionProgress < attentionSpan so this gives necessary last refresh to remove the attention!

		Thread.sleep(500);

		attentionProgress = attentionSpan + 1;

		repaint(attentionRect.x - attentionThickness, attentionRect.y - attentionThickness, attentionRect.width + 2 * attentionThickness, attentionRect.height
				+ 2 * attentionThickness);

	}

	protected void setTimer(long delay) {
		watchdelay = delay;
		if (delay == 0) {
			watch = 0;
		} else {
			long prev = watch;
			watch = System.currentTimeMillis() + delay;
			if (timer == null) {
				timer = new Thread(this);
				timer.setPriority(Thread.MIN_PRIORITY);
				timer.start();
			}
			if ((prev == 0) || (watch < prev)) {
				synchronized (this) {
					notify();
				}
				// synchronized (this) { try { notify(); }catch
				// (IllegalMonitorStateException imse) {} }
			}
		}
	}

	/**
	 * This component can be traversed using Tab or Shift-Tab keyboard focus
	 * traversal, although 1.4 replaced this method by <i>isFocusable</i>, so
	 * 1.4 compilers write deprecation warning
	 *
	 * @return true as focus-transverable component, overwrites the default
	 *         false value
	 */
	public boolean isFocusTraversable() {
		return true;
	}

	/**
	 * Dispatches mouse, key, focus, and component events occurring on the
	 * <i>Thinlet</i> component internally
	 */
	public void processEvent(AWTEvent e) {
		// evm (touchscreen) events: entered/moved/pressed -> dragged ->
		// dragged/released/exited
		int id = e.getID();
		if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_MOVED) || (id == MouseEvent.MOUSE_EXITED) || (id == MouseEvent.MOUSE_PRESSED)
				|| (id == MouseEvent.MOUSE_DRAGGED) || (id == MouseEvent.MOUSE_RELEASED)) {
			MouseEvent me = (MouseEvent) e;
			int x = me.getX();
			int y = me.getY();
			int clickcount = me.getClickCount();
			boolean shiftdown = me.isShiftDown();
			boolean controldown = me.isControlDown();
			boolean popuptrigger = (id == MouseEvent.MOUSE_PRESSED) && me.isMetaDown(); // isPopupTrigger is platform dependent

			if (id == MouseEvent.MOUSE_ENTERED) {
				if (mousepressed == null) {
					findComponent(content, x, y);
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_ENTERED, mouseinside, insidepart);
				}
			} else if (id == MouseEvent.MOUSE_MOVED) {
				Object previnside = mouseinside;
				Object prevpart = insidepart;
				findComponent(content, x, y);
				if ((previnside == mouseinside) && (prevpart == insidepart)) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_MOVED, mouseinside, insidepart);
				} else {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_EXITED, previnside, prevpart);
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_ENTERED, mouseinside, insidepart);
				}
			} else if (id == MouseEvent.MOUSE_EXITED) {
				if (mousepressed == null) {
					Object mouseexit = mouseinside;
					Object exitpart = insidepart;
					mouseinside = insidepart = null;
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_EXITED, mouseexit, exitpart);
				}
			} else if (id == MouseEvent.MOUSE_PRESSED) {
				if (popupowner != null) { // remove popup
					String classname = getClass(mouseinside);
					if ((popupowner != mouseinside) && (classname != ":popup") && (classname != ":combolist")) {
						closeup();
					}
				}
				hideTip(); // remove tooltip
				mousepressed = mouseinside;
				pressedpart = insidepart;
				handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_PRESSED, mousepressed, pressedpart);
			} else if (id == MouseEvent.MOUSE_DRAGGED) {
				hideTip(); // remove tooltip
				Object previnside = mouseinside;
				Object prevpart = insidepart;
				findComponent(content, x, y);
				boolean same = (previnside == mouseinside) && (prevpart == insidepart);
				boolean isin = (mousepressed == mouseinside) && (pressedpart == insidepart);
				boolean wasin = (mousepressed == previnside) && (pressedpart == prevpart);

				if (wasin && !isin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_EXITED, mousepressed, pressedpart);
				} else if (!same && (popupowner != null) && !wasin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, DRAG_EXITED, previnside, prevpart);
				}
				if (isin && !wasin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_ENTERED, mousepressed, pressedpart);
				} else if (!same && (popupowner != null) && !isin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, DRAG_ENTERED, mouseinside, insidepart);
				}
				if (isin == wasin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_DRAGGED, mousepressed, pressedpart);
				}
			} else if (id == MouseEvent.MOUSE_RELEASED) {
				hideTip(); // remove tooltip
				Object mouserelease = mousepressed;
				Object releasepart = pressedpart;
				mousepressed = pressedpart = null;
				handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_RELEASED, mouserelease, releasepart);
				if ((mouseinside != null) && ((mouserelease != mouseinside) || (releasepart != insidepart))) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger, MouseEvent.MOUSE_ENTERED, mouseinside, insidepart);
				}
			}
		} else if (id == MOUSE_WHEEL) {
			// TODO up and point side button style
			/*
			Rectangle port = getRectangle(mouseinside, ":port");
			if (port != null) { // is scrollable
			    // TODO hide tooltip?
			    Rectangle bounds = getRectangle(mouseinside, "bounds");
			    try { // mouse wheel is supported since 1.4 thus it use
			            // reflection
			        if (wheelrotation == null) {
			            wheelrotation = e.getClass().getMethod(
			                    "getWheelRotation", null);
			        }
			        int rotation = ((Integer) wheelrotation.invoke(e, null))
			                .intValue();

			        if (port.x + port.width < bounds.width) { // has vertical
			                                                    // scrollbar
			            processScroll(mouseinside, (rotation > 0) ? "down"
			                    : "up"); // TODO scroll panels too
			        } else if (port.y + port.height < bounds.height) { // has
			                                                            // horizontal
			                                                            // scrollbar
			            processScroll(mouseinside, (rotation > 0) ? "right"
			                    : "left");
			        }
			    } catch (Exception exc) { // never
			    }
			}
			 */
		} else if ((id == KeyEvent.KEY_PRESSED) || (id == KeyEvent.KEY_TYPED)) {
			if (focusinside && ((popupowner != null) || (focusowner != null))) {
				hideTip(); // remove tooltip
				KeyEvent ke = (KeyEvent) e;
				int keychar = ke.getKeyChar();
				boolean control = (keychar <= 0x1f) || ((keychar >= 0x7f) && (keychar <= 0x9f)) || (keychar >= 0xffff) || ke.isControlDown();
				int keycode = control ? ke.getKeyCode() : 0;

				if ((control == (id == KeyEvent.KEY_PRESSED))
						&& processKeyPress((popupowner != null) ? popupowner : focusowner, ke.isShiftDown(), ke.isControlDown(), ke.getModifiers(), control ? 0
								: keychar, keycode, false)) {
					ke.consume();
				} else if ((keycode == KeyEvent.VK_TAB) || ((keycode == KeyEvent.VK_F6) && (ke.isAltDown() || ke.isControlDown()))) {
					boolean outgo = (keycode == KeyEvent.VK_F6);
					if (!ke.isShiftDown() ? setNextFocusable(focusowner, outgo) : setPreviousFocusable(focusowner, outgo)) {
						ke.consume();
					}
					/*
					else if (MOUSE_WHEEL != 0) { // 1.4
					    if (!ke.isShiftDown()) {
					        transferFocus();
					    } else {
					        try {
					            getClass().getMethod("transferFocusBackward",
					                    null).invoke(this, null);
					        } catch (Exception exc) { // never
					        }
					    }
					}*/
					repaint(focusowner);
					closeup();
				} else if (keycode == KeyEvent.VK_F8) {
					for (Object splitpane = focusowner; splitpane != null; splitpane = getParent(splitpane)) {
						if (getClass(splitpane) == "splitpane") {
							setFocus(splitpane, false);
							repaint(splitpane);
							ke.consume();
							break; // middle
						}
					}
				} else if ((id == KeyEvent.KEY_PRESSED) && ((keychar != 0) || ke.isActionKey())
						&& checkMnemonic(focusowner, true, null, ke.getKeyCode(), ke.getModifiers())) {
					// key consumed
					ke.consume();
				} else {

					// key not consumed
					Object dialog = content;
					for (Object next = get(content, ":comp"); next != null; next = get(content, ":next")) {
						if (getClass(next) == "dialog") {
							dialog = next;
							break;
						}
					}

					Object next = findNextFocuable(null, dialog, keycode);

					if (next != null) {

						setFocusImpl(next);
					}
				}
			}
		} else if (id == FocusEvent.FOCUS_LOST) {
			focusinside = false;
			if (focusowner != null) {
				repaint(focusowner);
			}
			closeup();
		} else if (id == FocusEvent.FOCUS_GAINED) {
			focusinside = true;
			if (focusowner == null) {
				setFocus(content, false);
			} else {
				repaint(focusowner);
			}
		} else if ((id == ComponentEvent.COMPONENT_RESIZED) || (id == ComponentEvent.COMPONENT_SHOWN)) {
			Dimension d = getSize();
			setRectangle(content, "bounds", 0, 0, d.width, d.height);
			validate(content);
			closeup();
			if (!focusinside) {
				requestFocus();
			}
		}
	}

	protected Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}

	protected void requestFocus() {
		FocusEvent focus = new FocusEvent(FocusEvent.FOCUS_GAINED, 0);
		processEvent(focus);

	}

	/**
	 *
	 * Called by superclass on complete disapperament or partial screen obstruction
	 *
	 */

	protected void hideNotify() {
		AWTEvent event = new AWTEvent(ComponentEvent.COMPONENT_HIDDEN);
		processEvent(event);

		if (canvasListener != null) {
			canvasListener.hideNotify(this);
		}

	}

	/**
	 *
	 * Called by superclass on visual apperance.
	 *
	 */

	protected void showNotify() {
		AWTEvent event = new AWTEvent(ComponentEvent.COMPONENT_SHOWN);
		processEvent(event);

		if (canvasListener != null) {
			canvasListener.showNotify(this);
		}
	}

	/**
	 *
	 * Print friendly method call signature
	 *
	 * @param method
	 * @param data
	 * @return
	 */

	public StringBuffer spyMethod(String method, Object[] data) {
		StringBuffer b = new StringBuffer();
		b.append(method);

		b.append("(");
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				if(data[i] != null) {
					b.append(data[i]);
					if (i != data.length - 1) {
						b.append(", ");
					}
				}
			}
		}
		b.append(")");
		return b;
	}

	public void setFocusImpl(Object next) {
		if (getClass(next) == "tab" || getClass(next) == "menu" || getClass(next) == "item" || getClass(next) == "cell") {
			Object parent = getParent(next);
			set(parent, ":lead", next);
			next = parent;
		}

		if (focusowner != next) {
			closeup();
		}

		setFocus(next, false);

		setTimerImpl(next);

		Object parent = getParent(next);
		boolean scrollable = getBoolean(parent, "scrollable", false);
		if(scrollable) {
			Rectangle bounds = getRectangle(next, "bounds");

			scrollToVisible(parent, bounds.x, bounds.y, bounds.width, bounds.height);
		}

		movePointer();

		repaint(content);
	}

	/**
	 *
	 * Move pointer according to the focus center
	 *
	 */

	protected void movePointer() {

		// refresh current
		Rectangle current = this.pointerRect;
		if (current != null) {
			repaint(current.x, current.y, pointer.getWidth(), pointer.getHeight());
		}

		Rectangle absolute = getFocusCenter();

		Rectangle rect = new Rectangle(absolute.x + absolute.width - 10, absolute.y + absolute.height - 5, -1, -1);

		// keep whole pointer within screen
		if (rect.x + pointer.getWidth() > getWidth()) {
			rect.x = getWidth() - pointer.getWidth();
		} else if (rect.x < 0) {
			rect.x = 0;
		}

		if (rect.y + pointer.getHeight() > getHeight()) {
			rect.y = getHeight() - pointer.getHeight();
		} else if (rect.y < 0) {
			rect.y = 0;
		}

		repaint(rect.x, rect.y, pointer.getWidth(), pointer.getHeight());

		this.pointerRect = rect;
	}

	protected void setTimerImpl(Object next) {
		// hide tip if any
		hideTip();

		// must set the mouseinside to trigger the tooltip
		if (getClass(next) == "tabbedpane" || getClass(next) == "menubar" || getClass(next) == "table" || getClass(next) == "list" || getClass(next) == "tree") {
			mouseinside = get(next, ":lead");
		} else {
			mouseinside = next;
		}

		Rectangle ref = getAbsoluteBounds(next);

		if (getClass(next) == "textarea") {
			mousex = ref.x + 3 * ref.width / 5;
			mousey = ref.y + 3 * ref.height / 5;
		} else {

			// the tooltip was an offset of 10 so that the text does not appear below the mouse pointer
			mousex = ref.x - 2 + ref.width / 2;
			mousey = ref.y - 2 + ref.height;
		}

		setTimer(1500L);
	}

	/**
	 * Check the previous mouse location again because of a possible layout change
	 */
	protected void checkLocation(Object component) {
		if (mouseinside == component) { // parameter added by scolebourne
			findComponent(content, mousex, mousey);
			handleMouseEvent(mousex, mousex, 1, false, false, false, MouseEvent.MOUSE_ENTERED, mouseinside, insidepart);
		}
	}

	protected boolean processKeyPress(Object component, boolean shiftdown, boolean controldown, int modifiers, int keychar, int keycode, boolean replace) {
		String classname = getClass(component);

		if ("button" == classname) {
			if (keychar == KeyEvent.VK_SPACE || ((keycode == KeyEvent.VK_ENTER) && ((get(component, "type") == "default") || (get(component, "type") == null))) || ((keycode == KeyEvent.VK_ESCAPE) && (get(component, "type") == "cancel"))) {
				// pressedkey = keychar;
				invoke(component, null, "action");
				repaint(component);
				return true;
			}
		} else if (("checkbox" == classname) || ("togglebutton" == classname)) {
			if (keychar == KeyEvent.VK_SPACE || keycode == KeyEvent.VK_ENTER) {
				changeCheck(component, true);
				repaint(component);
				return true;
			}
		} else if ("combobox" == classname) {
			Object combolist = get(component, ":combolist");
			if (combolist == null) { // the drop down list is not visible
				boolean editable = getBoolean(component, "editable", true);
				if (editable && processField(component, shiftdown, controldown, modifiers, keychar, keycode, false, false, false, replace)) {
					setInteger(component, "selected", -1, -1);
					return true;
				}
				if ((keychar == KeyEvent.VK_SPACE) || /* (keycode == KeyEvent.VK_DOWN) || */keycode == KeyEvent.VK_ENTER) {
					popupCombo(component);
				}
				// +findText
				else
					return false;
			} else {
				if ((keycode == KeyEvent.VK_UP) || (keycode == KeyEvent.VK_DOWN) || (keycode == KeyEvent.VK_PAGE_UP) || (keycode == KeyEvent.VK_PAGE_DOWN)
						|| (keycode == KeyEvent.VK_HOME) || (keycode == KeyEvent.VK_END)) {
					Object next = getListItem(component, combolist, keycode, get(combolist, ":lead"), false);
					if (next != null) {
						setInside(combolist, next, true);
					}
				} else if ((keycode == KeyEvent.VK_ENTER) || (keychar == KeyEvent.VK_SPACE)) {
					closeCombo(component, combolist, get(combolist, ":lead")); // Alt+Up
				} else if (keycode == KeyEvent.VK_ESCAPE) {
					closeCombo(component, combolist, null);
				} else if (!processField(component, shiftdown, controldown, modifiers, keychar, keycode, false, false, false, replace)) {
					Object item = findText((char) keychar, component, combolist, false);
					if (item != null) {
						setInside(combolist, item, true);
					} else
						return false;
				}
			}
			return true;
		} else if (("textfield" == classname) || ("passwordfield" == classname)) {
			return processField(component, shiftdown, controldown, modifiers, keychar, keycode, false, ("passwordfield" == classname), false, replace);
		} else if ("textarea" == classname) {
			boolean mode = getBoolean(component, "interactionMode", true);
			boolean selected = getBoolean(component, "interactionState", false);

			if (keycode == KeyEvent.VK_ESCAPE && selected) {
				setBoolean(component, "interactionState", false);
				return true;
			} else if (keycode == KeyEvent.VK_ENTER && mode && !selected) {
				setBoolean(component, "interactionState", true);
				return true;
			} else if (selected || !mode) {
				char[] chars = (char[]) get(component, ":text");
				int start = getInteger(component, "start", 0);
				int end = getInteger(component, "end", 0);

				int istart = start;
				int iend = end;
				String insert = null;
				if ((keycode == KeyEvent.VK_HOME) && !controldown) {
					while ((iend > 0) && (chars[iend - 1] != '\n')) {
						iend--;
					}
					if (!shiftdown) {
						istart = iend;
					}
				} else if ((keycode == KeyEvent.VK_END) && !controldown) {
					while ((iend < chars.length) && (chars[iend] != '\n')) {
						iend++;
					}
					if (!shiftdown) {
						istart = iend;
					}
				} else if ((keycode == KeyEvent.VK_UP) || (keycode == KeyEvent.VK_PAGE_UP) || (keycode == KeyEvent.VK_DOWN)
						|| (keycode == KeyEvent.VK_PAGE_DOWN)) {
					Font currentfont = (Font) get(component, "font");
					Font fm = ((currentfont != null) ? currentfont : font);
					int fh = fm.getHeight();
					int y = 0;
					int linestart = 0;
					for (int i = 0; i < iend; i++) {
						if ((chars[i] == '\n') || (chars[i] == '\t')) {
							linestart = i + 1;
							y += fh;
						}
					}
					if (keycode == KeyEvent.VK_UP) {
						y -= fh;
					} else if (keycode == KeyEvent.VK_DOWN) {
						y += fh;
					} else {
						int dy = getRectangle(component, ":port").height - fm.getHeight(); // adjust by font size by Thomas
						y += (keycode == KeyEvent.VK_PAGE_UP) ? -dy : dy; // VK_PAGE_DOWN
					}
					int x = fm.charsWidth(chars, linestart, iend - linestart);
					iend = getCaretLocation(component, x, y, true, false);
					if (!shiftdown) {
						istart = iend;
					}
				} else
					return processField(component, shiftdown, controldown, modifiers, keychar, keycode, true, false, false, replace);
				return changeField(component, getString(component, "text", ""), insert, istart, iend, start, end);
			}
		} else if ("tabbedpane" == classname) {

			if (keycode == KeyEvent.VK_ENTER) {
				Object selected = getItem(component, getInteger(component, "selected", 0));
				Object lead = get(component, ":lead");

				if (lead != selected) {
					setInteger(component, "selected", getIndex(component, lead), 0);
					checkOffset(component);
					repaint(component);

					// show tool tip and attention
					setTimer(1500L);
					invoke(component, lead, "action");
				}
			}
		} else if ("spinbox" == classname) {
			// satt inn av Thomas
			boolean mode = getBoolean(component, "interactionMode", true);
			boolean selected = getBoolean(component, "interactionState", false);

			if (keycode == KeyEvent.VK_ENTER && mode) {
				setBoolean(component, "interactionState", !selected);
				return true;
			} else if (selected || !mode) {
				if ((keycode == KeyEvent.VK_UP) || (keycode == KeyEvent.VK_DOWN)) {
					processSpin(component, (keycode == KeyEvent.VK_UP) ? "up" : "down");
					return true;
				}
				return processField(component, shiftdown, controldown, modifiers, keychar, keycode, false, false, true, replace);
			} else if (KeyEvent.VK_0 <= keychar && keychar <= KeyEvent.VK_9) {
				return processField(component, shiftdown, controldown, modifiers, keychar, keycode, false, false, true, replace);
			}
		} else if ("slider" == classname) {
			boolean mode = getBoolean(component, "interactionMode", true);
			boolean selected = getBoolean(component, "interactionState", false);

			if (keycode == KeyEvent.VK_ENTER && mode) {
				setBoolean(component, "interactionState", !selected);
				return true;
			} else if (selected || !mode) {
				int value = getInteger(component, "value", 0);
				int d = 0;
				if ((keycode == KeyEvent.VK_HOME) || (keycode == KeyEvent.VK_LEFT) // || (keycode ==
						// KeyEvent.VK_UP)
						|| (keycode == KeyEvent.VK_PAGE_UP)) {

					d = getInteger(component, "minimum", 0) - value;
					if ((keycode == KeyEvent.VK_LEFT) || (keycode == KeyEvent.VK_UP)) {
						d = Math.max(d, -getInteger(component, "unit", 5));
					} else if (keycode == KeyEvent.VK_PAGE_UP) {
						d = Math.max(d, -getInteger(component, "block", 25));
					}
				} else if ((keycode == KeyEvent.VK_END) || (keycode == KeyEvent.VK_RIGHT) // || (keycode ==
						// KeyEvent.VK_DOWN)
						|| (keycode == KeyEvent.VK_PAGE_DOWN)) {
					d = getInteger(component, "maximum", 100) - value;
					if ((keycode == KeyEvent.VK_RIGHT) /*
					 * || (keycode ==
					 * KeyEvent.VK_DOWN)
					 */) {
						d = Math.min(d, getInteger(component, "unit", 5));
					} else if (keycode == KeyEvent.VK_PAGE_DOWN) {
						d = Math.min(d, getInteger(component, "block", 25));
					}
				}
				if (d != 0) {
					setInteger(component, "value", value + d, 0);
					repaint(component);
					invoke(component, null, "action");
					return true;
				}
			}
		} else if ("splitpane" == classname) {
			int divider = getInteger(component, "divider", -1);
			int d = 0;
			if (keycode == KeyEvent.VK_HOME) {
				d = -divider;
			} else if ((keycode == KeyEvent.VK_LEFT) || (keycode == KeyEvent.VK_UP)) {
				d = Math.max(-10, -divider);
			} else if ((keycode == KeyEvent.VK_END) || (keycode == KeyEvent.VK_RIGHT) || (keycode == KeyEvent.VK_DOWN)) {
				boolean horizontal = ("vertical" != get(component, "orientation"));
				Rectangle bounds = getRectangle(component, "bounds");
				int max = (horizontal ? bounds.width : bounds.height) - 5;
				d = max - divider;
				if (keycode != KeyEvent.VK_END) {
					d = Math.min(d, 10);
				}
			}
			if (d != 0) {
				setInteger(component, "divider", divider + d, -1);
				validate(component);
			}
		} else if (("list" == classname) || ("table" == classname)) {
			return processList(component, shiftdown, controldown, keychar, keycode, false);
		} else if ("tree" == classname) {

			if (keycode == KeyEvent.VK_ENTER) {

				Object lead = get(component, ":lead");
				Object node = get(lead, ":comp");

				if (node != null) {

					if (!getBoolean(lead, "expanded", true)) { // expand
						setBoolean(lead, "expanded", true, true);
						// selectItem(component, lead, true);
						validate(component);
						invoke(component, lead, "expand"); // lead
					} else { // collapse
						setBoolean(lead, "expanded", false, true);
						// selectItem(component, lead, true);
						validate(component);
						invoke(component, lead, "collapse"); // lead
						return true;
					}
					return true;

				}

			}

			return processList(component, shiftdown, controldown, keychar, keycode, true);
		} else if (("menubar" == classname) || ("popupmenu" == classname)) {
			// find the last open :popup and the previous one

			Object previous = null;
			Object last = null;
			for (Object i = get(component, ":popup"); i != null; i = get(i, ":popup")) {
				previous = last;
				last = i;
			}

			// selected is the current item of the last, or the previous :popup,
			// or null
			Object selected = get(last, "selected");
			Object hotpopup = ((selected != null) || (previous == null)) ? last : previous;
			if ((selected == null) && (previous != null)) {
				selected = get(previous, "selected");
			}

			if ((keycode == KeyEvent.VK_UP) || (keycode == KeyEvent.VK_DOWN)) {
				Object next = getMenu(hotpopup, selected, keycode == KeyEvent.VK_DOWN, true);
				if (next != null) {
					set(hotpopup, "selected", null);
					popupMenu(hotpopup);
					set(hotpopup, "selected", next);

					repaint(hotpopup);
				} else if (popupowner != component) {
					return false;
				} else {
					closeup();
				}
			} else if (keycode == KeyEvent.VK_LEFT) {
				if (previous != null) { // close the last :popup
					selected = get(previous, "selected");
					set(previous, "selected", null);
					popupMenu(previous);
					set(previous, "selected", selected);

					repaint(previous); // , selected
				} else if ("menubar" == classname) { // select the previous menubar menu
					//Object next = last == null && selected == null ? get(component, ":lead") : getMenu(component, get(component, "selected"), false, false);
					if (last != null || selected != null) {
						Object next = getMenu(component, get(component, "selected"), false, false);
						if (next != null) {
							set(component, "selected", next);
							Object popup = popupMenu(component);
							set(popup, "selected", getMenu(popup, null, true, true));

							repaint(component); // , selected
						} else {
							closeup();
						}
					}
					movePointer();

					return false;
				}
			} else if (keycode == KeyEvent.VK_RIGHT) {
				if ((previous != null) && (selected == null)) { // ?
					set(last, "selected", get(get(last, "menu"), ":comp"));
					repaint(last); // , selected
				} else if ((selected != null) && (getClass(selected) == "menu")) { // expand menu
					Object popup = popupMenu(last);
					set(popup, "selected", getMenu(popup, null, true, true));
				} else if ("menubar" == classname) { // select the next menubar menu
					//Object next = last == null && selected == null ? get(component, ":lead") : getMenu(component, get(component, "selected"), true, false);

					if (last != null || selected != null) {
						Object next = getMenu(component, get(component, "selected"), true, false);
						if (next != null) {
							set(component, "selected", next);
							Object popup = popupMenu(component);
							set(popup, "selected", getMenu(popup, null, true, true));

							repaint(component); // , selected
						} else {
							closeup();
						}
					}
					movePointer();

					return false;
				}
			}

			else if ((keycode == KeyEvent.VK_ENTER) || (keychar == KeyEvent.VK_SPACE) || (keycode == KeyEvent.VK_ESCAPE)) {

				if ((keycode != KeyEvent.VK_ESCAPE) && getBoolean(selected, "enabled", true)) {

					Object next = getMenu(hotpopup, selected, keycode == KeyEvent.VK_DOWN, true);
					Object lead = get(component, ":lead");

					if ((selected != null) && (getClass(selected) == "checkboxmenuitem")) {
						changeCheck(selected, false);
					} else if (selected == null && lead != null && (lead != next)) {
						if (popupowner != component) {
							set(component, "selected", lead);
							popupMenu(component);

							repaint(component);
						} else {
							closeup();

						}
						movePointer();
						return true;
					} else {
						invoke(selected, null, "action");
					}
				}

				closeup();

				movePointer();
			} else
				return false;

			movePointer();

			setTimer(3000L);
			return true;
		}
		return false;
	}

	protected boolean changeCheck(Object component, boolean box) {
		String group = getString(component, "group", null);
		if (group != null) {
			if (getBoolean(component, "selected", false)) {
				return false;
			}
			for (Object comp = get(getParent(component), ":comp"); comp != null; comp = get(comp, ":next")) {
				if (comp == component) {
					setBoolean(component, "selected", true);
				} else if (group.equals(get(comp, "group")) && getBoolean(comp, "selected", false)) {
					setBoolean(comp, "selected", false);
					if (box) {
						repaint(comp);
					} // checkbox only
				}
			}
		} else {
			setBoolean(component, "selected", !getBoolean(component, "selected", false), false);
		}
		invoke(component, null, "action");
		return true;
	}

	/**
	 * @param component
	 *            a :popup or a menubar
	 * @param part
	 *            the currently selected item, return the first/last if null
	 * @param forward
	 *            find the next item if true, the previous otherwise
	 * @param popup
	 *            the given component is :popup if true, menubar otherwise
	 * @return the next/previous item relative to the current one excluding
	 *         separators, or null
	 */
	protected Object getMenu(Object component, Object part, boolean forward, boolean popup) {
		Object previous = null;
		int i = forward && part != null ? 0 : 1;
		for (Object item = (i == 0) ? get(part, ":next") : get(popup ? get(component, "menu") : component, ":comp"); (i == 0) ? (item != null) : (item != part); item = get(
				item, ":next")) {
			if ((getClass(item) != "separator") && getBoolean(item, "enabled", true)) {
				if (forward) {
					// return null if the same (deselect)
					if (item == part) {
						return null;
					} else {
						return item;
					}
				}
				previous = item;
			}
		}
		// }
		if (previous == part) {
			return null;
		} else {
			return previous;
		}
	}

	/**
	 * Process keyboard events for textfield, passwordfield, textarea, combobox,
	 * and spinbox
	 *
	 * @param multiline
	 *            true for textarea, otherwise false
	 * @param hidden
	 *            true for passwordfield, otherwise false
	 * @param filter
	 *            true for spinbox, otherwise false
	 */
	protected boolean processField(Object component, boolean shiftdown, boolean controldown, int modifiers, int keychar, int keycode, boolean multiline,
			boolean hidden, boolean filter, boolean replace) {
		String text = getString(component, "text", "");
		int start = getInteger(component, "start", 0);
		int end = getInteger(component, "end", 0);
		boolean editable = getBoolean(component, "editable", true);

		int istart = start;
		int iend = end;
		String insert = null;
		if (editable && (keychar != 0) &&
		// ((modifiers == 0) || (modifiers == InputEvent.SHIFT_MASK))) {
				(modifiers != InputEvent.ALT_MASK)) {
			insert = String.valueOf((char) keychar);

			if (replace) {
				istart--;
			}
		} else if (editable && (keycode == KeyEvent.VK_ENTER)) {
			if (multiline) {
				insert = "\n";
			} else {
				return invoke(component, null, "perform");
			}
		} else if (editable && (keycode == KeyEvent.VK_BACK_SPACE)) {
			insert = "";
			if (start == end) {
				istart -= 1;
			}
		} else if (keycode == KeyEvent.VK_END) {
			iend = text.length();
			if (!shiftdown) {
				istart = iend;
			}
		} else if (keycode == KeyEvent.VK_HOME) {
			iend = 0;
			if (!shiftdown) {
				istart = iend;
			}
		} else if (keycode == KeyEvent.VK_LEFT) {
			if (controldown) {
				for (int i = 0; i < 2; i++) {
					while ((iend > 0) && ((i != 0) == KeyEvent.isLetterOrDigit(text.charAt(iend - 1)))) {
						iend--;
					}
				}
			} else {
				iend -= 1;
			}
			if (!shiftdown) {
				istart = iend;
			}
		} else if (keycode == KeyEvent.VK_RIGHT) {
			if (controldown) {
				for (int i = 0; i < 2; i++) {
					while ((iend < text.length()) && ((i == 0) == KeyEvent.isLetterOrDigit(text.charAt(iend)))) {
						iend++;
					}
				}
			} else {
				iend += 1;
			}
			if (!shiftdown) {
				istart = iend;
			}
		} else if (editable && (keycode == KeyEvent.VK_DELETE)) {
			insert = "";
			if (start == end) {
				iend += 1;
			}
		} else if (controldown && ((keycode == KeyEvent.VK_A) || (keycode == 0xBF))) {
			istart = 0; // KeyEvent.VK_SLASH
			iend = text.length();
		} else if (controldown && (keycode == 0xDC)) {
			istart = iend = text.length(); // KeyEvent.VK_BACK_SLASH
		} else if ((editable && !hidden && controldown && (keycode == KeyEvent.VK_X)) || (!hidden && controldown && (keycode == KeyEvent.VK_C))) {
			if (start != end) {
				/*
				clipboard = text.substring(Math.min(start, end), Math.max(
				        start, end));
				try {
				    getToolkit().getSystemClipboard().setContents(
				            new StringSelection(clipboard), null);
				} catch (Exception exc) {
				}
				 */
				if (keycode == KeyEvent.VK_X) {
					insert = "";
				} else {
					return true;
				}
			}
		} else if (editable && controldown && (keycode == KeyEvent.VK_V)) {
			/*
			try {
			    insert = (String) getToolkit().getSystemClipboard()
			            .getContents(this).getTransferData(
			                    DataFlavor.stringFlavor);
			} catch (Exception exc) {
			 */
			insert = clipboard;
			//}
			if (insert != null) { // no text on system clipboard nor internal
				// clipboard text
				insert = filter(insert, multiline);
			}
		}
		if (filter && (insert != null)) { // contributed by Michael Nascimento
			for (int i = insert.length() - 1; i >= 0; i--) {
				if (!Character.isDigit(insert.charAt(i))) {
					return false;
				}
			}
		}
		return changeField(component, text, insert, istart, iend, start, end);
	}

	/**
	 * @param text
	 * @param multiline
	 * @return
	 */
	protected static String filter(String text, boolean multiline) {
		StringBuffer filtered = new StringBuffer(text.length());
		for (int i = 0; i < text.length(); i++) {
			char ckey = text.charAt(i);
			if (((ckey > 0x1f) && (ckey < 0x7f)) || ((ckey > 0x9f) && (ckey < 0xffff)) || (multiline && (ckey == '\n'))) {
				filtered.append(ckey);
			}
		}
		return (filtered.length() != text.length()) ? filtered.toString() : text;
	}

	/**
	 * @param component
	 *            a textfield, passwordfield, textarea, combobox, or spinbox
	 * @param text
	 *            current text
	 * @param insert
	 *            a string to replace thr current selection
	 * @param movestart
	 *            new selection start position
	 * @param moveend
	 *            new caret (selection end) position
	 * @param start
	 *            current selection start position
	 * @param end
	 *            current caret position
	 * @return true if selection, caret location, or text content changed
	 */
	protected boolean changeField(Object component, String text, String insert, int movestart, int moveend, int start, int end) {
		movestart = Math.max(0, Math.min(movestart, text.length()));
		moveend = Math.max(0, Math.min(moveend, text.length()));
		if ((insert == null) && (start == movestart) && (end == moveend)) {
			return false;
		}
		if (insert != null) {
			int min = Math.min(movestart, moveend);
			set(component, "text", text.substring(0, min) + insert + text.substring(Math.max(movestart, moveend)));
			movestart = moveend = min + insert.length();
			invoke(component, null, "action"); // deprecated
		}
		if (start != movestart) {
			setInteger(component, "start", movestart, 0);
		}
		if (end != moveend) {
			setInteger(component, "end", moveend, 0);
		}
		validate(component);
		invoke(component, null, (insert != null) ? ((insert.length() > 0) ? "insert" : "remove") : "caret");
		return true;
	}

	protected boolean processList(Object component, boolean shiftdown, boolean controldown, int keychar, int keycode, boolean recursive) {
		if ((keycode == KeyEvent.VK_UP)
				|| // select previous/next/first/... item
				(keycode == KeyEvent.VK_DOWN) || (keycode == KeyEvent.VK_PAGE_UP) || (keycode == KeyEvent.VK_PAGE_DOWN) || (keycode == KeyEvent.VK_HOME)
				|| (keycode == KeyEvent.VK_END)) {

			Object lead = get(component, ":lead");
			Object row = getListItem(component, component, keycode, lead, recursive);
			if (row != null) {
				String selection = getString(component, "selection", "single");
				if (shiftdown && (selection != "single") && (lead != null)) {
					extend(component, lead, row, recursive);
				} else if (!controldown) {
					// selectItem(component, row, recursive);
				}

				setLead(component, lead, row);

				movePointer();

				// show tool tip and attention
				setTimer(1500L);

				return true;
			}
		} else if (keycode == KeyEvent.VK_LEFT) {
			/*
			if ("list" == getClass(component))
			    return false;
			 */
			return processScroll(component, "left");
		} else if (keycode == KeyEvent.VK_RIGHT) {
			/*
			if ("list" == getClass(component))
			    return false;
			 */
			return processScroll(component, "right");
		} else if (keychar == KeyEvent.VK_SPACE || keycode == KeyEvent.VK_ENTER) { // select
			// the
			// current
			// item
			// select(component, get(component, ":lead"), recursive, shiftdown,
			// controldown); //...
			select(component, get(component, ":lead"), recursive, shiftdown, true); // ...
			return true;
		} else if (controldown) {
			if (((keycode == KeyEvent.VK_A) || (keycode == 0xBF)) && // KeyEvent.VK_SLASH
					(getString(component, "selection", "single") != "single")) { // select
				// all
				selectAll(component, true, recursive);
				return true;
			} else if (keycode == 0xDC) { // KeyEvent.VK_BACK_SLASH //
				// deselect all
				selectAll(component, false, recursive);
				return true;
			}
		} else {
			Object item = findText((char) keychar, component, component, recursive);
			if (item != null) {
				select(component, item, recursive, false, false);
				return true;
			}
		}
		return false;
	}

	/**
	 * Search for the next/first appropriate item starting with the collected
	 * string or the given single character
	 *
	 * @param keychar
	 *            the last typed character
	 * @param component
	 *            a list, tree, table, or combobox
	 * @param leadowner
	 *            the list, tree, table, or the combobox's drop down list
	 * @param recursive
	 *            if the component is a tree
	 * @return the appropriate item or null
	 */
	protected Object findText(char keychar, Object component, Object leadowner, boolean recursive) {
		if (keychar != 0) {
			long current = System.currentTimeMillis();
			int i = (current > findtime + 1000) ? 1 : 0; // clear the
			// starting string
			// after a second
			findtime = current;
			Object lead = get(leadowner, ":lead");
			for (; i < 2; i++) { // 0: find the long text, 1: the stating
				// character only
				findprefix = (i == 0) ? (findprefix + keychar) : String.valueOf(keychar);
				for (int j = 0; j < 2; j++) { // 0: lead to last, 1: first to
					// lead
					for (Object item = (j == 0) ? ((i == 0) ? lead : getNextItem(component, lead, recursive)) : get(component, ":comp"); (j == 0) ? (item != null)
							: (item != lead); item = getNextItem(component, item, recursive)) {
						if (getString(item, "text", "").regionMatches(true, 0, findprefix, 0, findprefix.length())) { // table
							// first
							// column...
							return item;
						}
					}
				}
			}
		}
		return null;
	}

	protected Object getListItem(Object component, Object scrollpane, int keycode, Object lead, boolean recursive) {
		Object row = null;
		if (keycode == KeyEvent.VK_UP) {
			for (Object prev = get(component, ":comp"); prev != lead; prev = getNextItem(component, prev, recursive)) {
				row = prev; // component -> getParent(lead)
			}
		} else if (keycode == KeyEvent.VK_DOWN) {
			row = (lead == null) ? get(component, ":comp") : getNextItem(component, lead, recursive);
		} else if ((keycode == KeyEvent.VK_PAGE_UP) || (keycode == KeyEvent.VK_PAGE_DOWN)) {
			Rectangle view = getRectangle(scrollpane, ":view");
			Rectangle port = getRectangle(scrollpane, ":port");
			Rectangle rl = (lead != null) ? getRectangle(lead, "bounds") : null;
			int vy = (keycode == KeyEvent.VK_PAGE_UP) ? view.y : (view.y + port.height);
			if ((keycode == KeyEvent.VK_PAGE_UP) && (rl != null) && (rl.y <= view.y)) {
				vy -= port.height;
			}
			if ((keycode == KeyEvent.VK_PAGE_DOWN) && (rl != null) && (rl.y + rl.height >= view.y + port.height)) {
				vy += port.height;
			}
			for (Object item = get(component, ":comp"); item != null; item = getNextItem(component, item, recursive)) {
				Rectangle r = getRectangle(item, "bounds");
				if (keycode == KeyEvent.VK_PAGE_UP) {
					row = item;
					if (r.y + r.height > vy) {
						break;
					}
				} else {
					if (r.y > vy) {
						break;
					}
					row = item;
				}
			}
		} else if (keycode == KeyEvent.VK_HOME) {
			row = get(component, ":comp");
		} else if (keycode == KeyEvent.VK_END) {
			for (Object last = lead; last != null; last = getNextItem(component, last, recursive)) {
				row = last;
			}
		} else if (keycode == KeyEvent.VK_ENTER) {
			return lead;
		}
		return row;
	}

	/**
	 * Select all the items
	 *
	 * @param component
	 *            a list/tree/table
	 * @param selected
	 *            selects or deselects items
	 * @param recursive
	 *            true for tree
	 */
	protected void selectAll(Object component, boolean selected, boolean recursive) {
		boolean changed = false;
		for (Object item = get(component, ":comp"); item != null; item = getNextItem(component, item, recursive)) {
			if (setBoolean(item, "selected", selected, false)) {
				repaint(component, null, item);
				changed = true;
			}
		}
		set(component, ":anchor", null);
		if (changed) {
			invoke(component, null, "action");
		}
	}

	/**
	 * Select a single given item, deselect others
	 *
	 * @param component
	 *            a list/tree/table
	 * @param row
	 *            the item/node/row to select
	 * @param recursive
	 *            true for tree
	 */
	protected void selectItem(Object component, Object row, boolean recursive) {
		boolean changed = false;
		for (Object item = get(component, ":comp"); item != null; item = getNextItem(component, item, recursive)) {
			if (setBoolean(item, "selected", (item == row), false)) {
				repaint(component, null, item);
				changed = true;
			}
		}
		set(component, ":anchor", null);
		if (changed) {
			invoke(component, row, "action");
		}
	}

	protected void extend(Object component, Object lead, Object row, boolean recursive) {
		Object anchor = get(component, ":anchor");
		if (anchor == null) {
			set(component, ":anchor", anchor = lead);
		}
		char select = 'n';
		boolean changed = false;
		for (Object item = get(component, ":comp"); // anchor - row
		item != null; item = getNextItem(component, item, recursive)) {
			if (item == anchor)
				select = (select == 'n') ? 'y' : 'r';
			if (item == row)
				select = (select == 'n') ? 'y' : 'r';
			if (setBoolean(item, "selected", (select != 'n'), false)) {
				repaint(component, null, item);
				changed = true;
			}
			if (select == 'r')
				select = 'n';
		}
		if (changed) {
			invoke(component, row, "action");
		}
	}

	/**
	 * Update the lead item of a list/tree/table, repaint, and scroll
	 *
	 * @param component
	 *            a list, tree, or table
	 * @param oldlead
	 *            the current lead item
	 * @param lead
	 *            the new lead item
	 */
	protected void setLead(Object component, Object oldlead, Object lead) {
		if (oldlead != lead) { // ?
			if (oldlead != null) {
				repaint(component, null, oldlead);
			}
			set(component, ":lead", lead);
			repaint(component, null, lead);

			Rectangle r = getRectangle(lead, "bounds");
			scrollToVisible(component, r.x, r.y, 0, r.height);
		}
	}

	/**
	 * Update the lead item of a combolist, repaint, and scroll
	 *
	 * @param component
	 *            a combobox drop down list
	 * @param part
	 *            the current hotspot item
	 * @param scroll
	 *            scroll to the part if true
	 */
	protected void setInside(Object component, Object part, boolean scroll) {
		Object previous = get(component, ":lead");
		if (previous != null) {
			repaint(component, ":combolist", previous);
		}
		set(component, ":lead", part);
		if (part != null) {
			repaint(component, ":combolist", part);
			if (scroll) {
				Rectangle r = getRectangle(part, "bounds");
				scrollToVisible(component, r.x, r.y, 0, r.height);
			}
		}
	}

	/**
	 * @param x
	 *            mouse x position relative to thinlet component
	 * @param y
	 *            mouse y position relative to the main desktop
	 */
	protected void handleMouseEvent(int x, int y, int clickcount, boolean shiftdown, boolean controldown, boolean popuptrigger, int id, Object component,
			Object part) {
		if (id == MouseEvent.MOUSE_ENTERED) {
			setTimer(750L);
		} else if (id == MouseEvent.MOUSE_EXITED) {
			hideTip();
		}
		if (!getBoolean(component, "enabled", true)) {
			return;
		}
		String classname = getClass(component);
		if (("button" == classname) || ("checkbox" == classname) || ("togglebutton" == classname)) {
			if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED) || (id == MouseEvent.MOUSE_PRESSED) || (id == MouseEvent.MOUSE_RELEASED)) {
				// button event
				if (id == MouseEvent.MOUSE_PRESSED) {
					setFocus(component, true);
				}
				if (("button" == classname) && ((mousepressed == null) || (mousepressed == component))
						&& ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED)) && (get(component, "type") == "link")) {
					setCursor(Cursor.getPredefinedCursor((id == MouseEvent.MOUSE_ENTERED) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
				} else if ((id == MouseEvent.MOUSE_RELEASED) && (mouseinside == component)) {
					if ("button" != classname) {
						changeCheck(component, true);
					} else
						invoke(component, null, "action");
				}
				repaint(component);
			}
		} else if ("combobox" == classname) {
			boolean editable = getBoolean(component, "editable", true);
			if (editable && (part == null)) { // textfield area
				Image icon = null;
				int left = ((id == MouseEvent.MOUSE_PRESSED) && ((icon = getIcon(component, "icon", null)) != null)) ? icon.getWidth() : 0;
				processField(x, y, clickcount, id, component, false, false, left, popuptrigger);
			} else if (part != "icon") { // part = "down"
				if (((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED)) && (mousepressed == null)) {
					if (editable) {
						repaint(component, "combobox", part);
					} // hover the arrow button
					else {
						repaint(component);
					} // hover the whole combobox
				} else if (id == MouseEvent.MOUSE_PRESSED) {
					Object combolist = get(component, ":combolist");
					if (combolist == null) { // combolist is closed
						setFocus(component, true);
						repaint(component);
						popupCombo(component);
					} else { // combolist is visible
						closeCombo(component, combolist, null);
					}
				} else if (id == MouseEvent.MOUSE_RELEASED) {
					if (mouseinside != component) {
						Object combolist = get(component, ":combolist");
						closeCombo(component, combolist, ((mouseinside == combolist) && (insidepart instanceof Object[])) ? insidepart : null);
					} else {
						repaint(component);
					}
				}
			}
		} else if (":combolist" == classname) {
			if (!processScroll(x, y, id, component, part)) {
				if ((id == MouseEvent.MOUSE_ENTERED) || (id == DRAG_ENTERED)) {
					if (part != null) { // + scroll if dragged
						setInside(component, part, false);
					}
				} else if (id == MouseEvent.MOUSE_RELEASED) {
					closeCombo(get(component, "combobox"), component, part);
				}
			}
		} else if (("textfield" == classname) || ("passwordfield" == classname)) {
			processField(x, y, clickcount, id, component, false, ("passwordfield" == classname), 0, popuptrigger);
		} else if ("textarea" == classname) {
			if (!processScroll(x, y, id, component, part)) {
				processField(x, y, clickcount, id, component, true, false, 0, popuptrigger);
			}
		} else if ("panel" == classname) {
			processScroll(x, y, id, component, part);
		} else if ("desktop" == classname) {
			if (part == "modal") {
				if (id == MouseEvent.MOUSE_ENTERED) {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				} else if (id == MouseEvent.MOUSE_EXITED) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		} else if ("spinbox" == classname) {
			if (part == null) {
				processField(x, y, clickcount, id, component, false, false, 0, popuptrigger);
			} else { // part = "up" || "down"
				if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED) || (id == MouseEvent.MOUSE_PRESSED)
						|| (id == MouseEvent.MOUSE_RELEASED)) {

					if (id == MouseEvent.MOUSE_PRESSED) {
						setFocus(component, true);
						if (processSpin(component, part)) {
							setTimer(375L);
						}
						// settext: start end selection, parse exception...
					} else {
						if (id == MouseEvent.MOUSE_RELEASED) {
							setTimer(0L);
						}
					}
					repaint(component, classname, part);
				}
			}
		} else if ("tabbedpane" == classname) {
			if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED)) {
				if ((part != null) && getBoolean(part, "enabled", true) && (getInteger(component, "selected", 0) != getIndex(component, part))) {
					repaint(component, "tabbedpane", part);
				}
			} else if ((part != null) && (id == MouseEvent.MOUSE_PRESSED) && getBoolean(part, "enabled", true)) {
				int selected = getInteger(component, "selected", 0);
				int current = getIndex(component, part);
				if (selected == current) {
					setFocus(component, true);

					repaint(component, "tabbedpane", part);
				} else {
					setInteger(component, "selected", current, 0);

					// Object tabcontent = getItem(component, current);
					// setFocus((tabcontent != null) ? tabcontent : component);
					//setNextFocusable(component, false);

					setFocusImpl(part);

					checkOffset(component);
					repaint(component);
					invoke(component, part, "action");
				}
			}
		} else if ("slider" == classname) {
			if ((id == MouseEvent.MOUSE_PRESSED) || (id == MouseEvent.MOUSE_DRAGGED)) {
				if (id == MouseEvent.MOUSE_PRESSED) {
					setReference(component, block / 2, block / 2);
					setFocus(component, true);
				}
				int minimum = getInteger(component, "minimum", 0);
				int maximum = getInteger(component, "maximum", 100);
				int value = getInteger(component, "value", 0);
				Rectangle bounds = getRectangle(component, "bounds");
				boolean horizontal = ("vertical" != get(component, "orientation"));
				int newvalue = minimum + (horizontal ? (x - referencex) : (y - referencey)) * (maximum - minimum)
						/ ((horizontal ? bounds.width : bounds.height) - block); // ...
				// +0.5
				newvalue = Math.max(minimum, Math.min(newvalue, maximum));
				if (value != newvalue) { // fixed by Andrew de Torres
					setInteger(component, "value", newvalue, 0);
					invoke(component, null, "action");
				}
				if ((value != newvalue) || (id == MouseEvent.MOUSE_PRESSED)) {
					repaint(component);
				}
			} else if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_EXITED) {
				repaint(component);
			}
		} else if ("splitpane" == classname) {
			if (id == MouseEvent.MOUSE_PRESSED) {
				setReference(component, 2, 2);
			} else if (id == MouseEvent.MOUSE_DRAGGED) {
				int divider = getInteger(component, "divider", -1);
				boolean horizontal = ("vertical" != get(component, "orientation"));
				int moveto = horizontal ? (x - referencex) : (y - referencey);
				Rectangle bounds = getRectangle(component, "bounds");
				moveto = Math.max(0, Math.min(moveto, Math.abs(horizontal ? bounds.width : bounds.height) - 5));
				if (divider != moveto) {
					setInteger(component, "divider", moveto, -1);
					validate(component);
				}
			} else if ((id == MouseEvent.MOUSE_ENTERED) && (mousepressed == null)) {
				boolean horizontal = ("vertical" != get(component, "orientation"));
				setCursor(Cursor.getPredefinedCursor(horizontal ? Cursor.E_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR));
			} else if (((id == MouseEvent.MOUSE_EXITED) && (mousepressed == null)) || ((id == MouseEvent.MOUSE_RELEASED) && (mouseinside != component))) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		} else if (("list" == classname) || ("table" == classname) || ("tree" == classname)) {
			if (!processScroll(x, y, id, component, part)) {
				if (((id == MouseEvent.MOUSE_PRESSED) || ((id == MouseEvent.MOUSE_DRAGGED) && !shiftdown && !controldown))) {
					// Rectangle view = getRectangle(component, ":view");
					Rectangle port = getRectangle(component, ":port");
					int my = y + port.y - referencey;
					for (Object item = get(component, ":comp"); item != null;) {
						Rectangle r = getRectangle(item, "bounds");
						if (my < r.y + r.height) {
							if (id == MouseEvent.MOUSE_DRAGGED) { // !!!
								scrollToVisible(component, r.x, r.y, 0, r.height);
							} else if ("tree" == classname) {
								int mx = x + port.x - referencex;
								if (mx < r.x) {
									if ((mx >= r.x - block) && (get(item, ":comp") != null)) {
										boolean expanded = getBoolean(item, "expanded", true);
										setBoolean(item, "expanded", !expanded, true);
										selectItem(component, item, true);
										setLead(component, get(component, ":lead"), item);
										setFocus(component, true);
										validate(component);
										invoke(component, item, expanded ? "collapse" : "expand"); // item
									}
									break;
								}
							}
							if ((id != MouseEvent.MOUSE_DRAGGED) || !getBoolean(item, "selected", false)) {
								if (id != MouseEvent.MOUSE_DRAGGED) {
									if (setFocus(component, true)) {
										repaint(component, classname, item);
									} // ?
								}
								if (!popuptrigger || !getBoolean(item, "selected", false)) { // don't
									// update
									// selection
									select(component, item, ("tree" == classname), shiftdown, controldown);
									if (clickcount == 2) {
										invoke(component, item, "perform");
									}
								}
							}
							break;
						}
						item = getNextItem(component, item, ("tree" == classname));
					}
				}
			}
		} else if ("menubar" == classname) {
			Object selected = get(component, "selected");
			if (((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED)) && (part != null) && (selected == null)
					&& getBoolean(part, "enabled", true)) {
				repaint(component, classname, part);
			} else if ((part != null) && ((selected == null) ? (id == MouseEvent.MOUSE_PRESSED) : ((id == MouseEvent.MOUSE_ENTERED) || (id == DRAG_ENTERED)))
					&& getBoolean(part, "enabled", true)) {

				set(component, "selected", part);

				setFocus(component, true);

				popupMenu(component);

				Object lead = get(component, ":lead");
				set(component, ":lead", part);

				if (lead == null) {
					repaint(component, classname, part);
				} else {
					repaint(component);
				}

			} else if ((id == MouseEvent.MOUSE_PRESSED) && (selected != null)) {
				closeup();
			} else if (id == MouseEvent.MOUSE_RELEASED) {
				if ((part != insidepart) && ((insidepart == null) || ((insidepart instanceof Object[]) && (getClass(insidepart) != "menu")))) {
					if ((insidepart != null) && getBoolean(insidepart, "enabled", true)) {
						if (getClass(insidepart) == "checkboxmenuitem") {
							changeCheck(insidepart, false);
						} else
							invoke(insidepart, null, "action");
					}
					closeup();
				}
			}
		} else if ((":popup" == classname) || ("popupmenu" == classname)) {

			if (((id == MouseEvent.MOUSE_ENTERED) || (id == DRAG_ENTERED)) && (part != null) && getBoolean(part, "enabled", true)) {
				set(component, "selected", part);
				popupMenu(component);
				repaint(component, classname, part);
			} else if ((id == MouseEvent.MOUSE_RELEASED) && ((part != null) || ((insidepart != null) && ("popupmenu" == classname)))) {
				if ((insidepart == null) || (getClass(insidepart) != "menu")) {
					if ((insidepart != null) && getBoolean(insidepart, "enabled", true)) {
						if (getClass(insidepart) == "checkboxmenuitem") {
							changeCheck(insidepart, false);
						} else
							invoke(insidepart, null, "action");
					}
					closeup();
				}
			} else if (((id == MouseEvent.MOUSE_EXITED) || (id == DRAG_EXITED)) && (part != null) && getBoolean(part, "enabled", true)) {
				if (getClass(part) != "menu") {
					set(component, "selected", null);
				}
				repaint(component, classname, part);
			}
		} else if ("dialog" == classname) {
			if (part == "header") {
				if (id == MouseEvent.MOUSE_PRESSED) {
					Rectangle bounds = getRectangle(component, "bounds");
					referencex = x - bounds.x;
					referencey = y - bounds.y;
					Object parent = getParent(component);
					if (get(parent, ":comp") != component) { // to front
						removeItemImpl(parent, component);
						insertItem(parent, ":comp", component, 0);
						set(component, ":parent", parent);
						repaint(component); // to front always...
						setNextFocusable(component, false);
					}
				} else if (id == MouseEvent.MOUSE_DRAGGED) {
					Rectangle bounds = getRectangle(component, "bounds");
					Rectangle parents = getRectangle(getParent(component), "bounds");
					int mx = Math.max(0, Math.min(x - referencex, parents.width - bounds.width));
					int my = Math.max(0, Math.min(y - referencey, parents.height - bounds.height));
					if ((bounds.x != mx) || (bounds.y != my)) {
						// repaint the union of the previous and next bounds
						repaint(component, Math.min(bounds.x, mx), Math.min(bounds.y, my), bounds.width + Math.abs(mx - bounds.x), bounds.height
								+ Math.abs(my - bounds.y));
						bounds.x = mx;
						bounds.y = my;
					}
				}
				// rcs: close dialog button
			} else if (part == "closebutton") {
				if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_EXITED || id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED) {
					if (id == MouseEvent.MOUSE_RELEASED && mouseinside == component)
						invoke(component, null, "close");
					repaint(component); // todo: don't repaint the whole dialog
				}
			} else if (!processScroll(x, y, id, component, part) && (part != null)) {
				if (id == MouseEvent.MOUSE_PRESSED) {
					referencex = x;
					referencey = y;
				} else if (id == MouseEvent.MOUSE_DRAGGED) {
					repaint(component);
					Rectangle bounds = getRectangle(component, "bounds");
					if ((part == ":nw") || (part == ":n") || (part == ":ne")) {
						bounds.y += y - referencey;
						bounds.height -= y - referencey;
					}
					if ((part == ":ne") || (part == ":e") || (part == ":se")) {
						bounds.width += x - referencex;
					}
					if ((part == ":sw") || (part == ":s") || (part == ":se")) {
						bounds.height += y - referencey;
					}
					if ((part == ":nw") || (part == ":w") || (part == ":sw")) {
						bounds.x += x - referencex;
						bounds.width -= x - referencex;
					}
					referencex = x;
					referencey = y;
					doLayout(component);
					repaint(component);
				} else if (id == MouseEvent.MOUSE_ENTERED) {
					setCursor(Cursor.getPredefinedCursor((part == ":n") ? Cursor.N_RESIZE_CURSOR : (part == ":ne") ? Cursor.NE_RESIZE_CURSOR
							: (part == ":e") ? Cursor.E_RESIZE_CURSOR : (part == ":se") ? Cursor.SE_RESIZE_CURSOR : (part == ":s") ? Cursor.S_RESIZE_CURSOR
									: (part == ":sw") ? Cursor.SW_RESIZE_CURSOR : (part == ":w") ? Cursor.W_RESIZE_CURSOR : Cursor.NW_RESIZE_CURSOR));
				} else if (id == MouseEvent.MOUSE_EXITED) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}

		if (popuptrigger) {
			Object popupmenu = get(component, "popupmenu");
			if (popupmenu != null) {
				popupPopup(popupmenu, x, y);
				mouseinside = mousepressed = popupmenu;
				insidepart = pressedpart = null;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	protected void setCursor(Object predefinedCursor) {
		// do nothing
	}

	/**
	 * Calculate the given point in a component relative to the thinlet desktop
	 * and set as reference value
	 *
	 * @param component
	 *            a widget
	 * @param x
	 *            reference point relative to the component left edge
	 * @param y
	 *            relative to the top edge
	 */
	protected void setReference(Object component, int x, int y) {
		referencex = x;
		referencey = y;
		for (; component != null; component = getParent(component)) {
			Rectangle bounds = getRectangle(component, "bounds");
			referencex += bounds.x;
			referencey += bounds.y;

			Rectangle port = getRectangle(component, ":port");
			if (port != null) { // content scrolled
				Rectangle view = getRectangle(component, ":view");
				referencex -= view.x - port.x;
				referencey -= view.y - port.y;
			}
		}
	}

	protected void select(Object component, Object row, boolean recursive, boolean shiftdown, boolean controldown) {
		String selection = getString(component, "selection", "single");
		Object lead = null;
		if (shiftdown && (selection != "single") && ((lead = get(component, ":lead")) != null)) {
			extend(component, lead, row, recursive);
		} else {
			if (controldown && (selection == "multiple")) {
				setBoolean(row, "selected", !getBoolean(row, "selected", false), false);
				repaint(component, null, row);
				invoke(component, row, "action");
				set(component, ":anchor", null);
			} else if (controldown && getBoolean(row, "selected", false)) {
				for (Object item = row; item != null; item = getNextItem(component, item, recursive)) {
					if (setBoolean(item, "selected", false, false)) {
						repaint(component, null, item);
					}
				}
				invoke(component, row, "action");
				set(component, ":anchor", null);
			} else {
				selectItem(component, row, recursive);
			}
		}
		setLead(component, (lead != null) ? lead : get(component, ":lead"), row);
	}

	/**
	 * Find the next item after the given
	 *
	 * @param component
	 *            a list/tree/table widget
	 * @param item
	 *            the next item after this, or the first if null
	 * @param recursive
	 *            true if tree
	 * @return next (or first) item
	 */
	protected Object getNextItem(Object component, Object item, boolean recursive) {
		if (!recursive) {
			return get(item, ":next");
		}
		Object next = get(item, ":comp");
		if ((next == null) || !getBoolean(item, "expanded", true)) {
			while ((item != component) && ((next = get(item, ":next")) == null)) {
				item = getParent(item);
			}
		}
		return next;
	}

	protected void processField(int x, int y, int clickcount, int id, Object component, boolean multiline, boolean hidden, int left, boolean popuptrigger) {
		if (id == MouseEvent.MOUSE_PRESSED) {
			// + middle=alt paste clipboard content
			setReference(component, 2 + left, 2);
			int mx = x - referencex;
			int my = 0;
			if (!multiline) {
				mx += getInteger(component, ":offset", 0);
			} else {
				Rectangle port = getRectangle(component, ":port");
				mx += port.x - 1;
				my = y - referencey + port.y - 1;
			}
			int caretstart = getCaretLocation(component, mx, my, multiline, hidden);
			if (popuptrigger) {
				int start = getInteger(component, "start", 0);
				int end = getInteger(component, "end", 0);
				if ((caretstart >= Math.min(start, end)) && // inside selected
						// text
						(caretstart <= Math.max(start, end)))
					return;
			}
			int caretend = caretstart;
			if (clickcount > 1) {
				String text = getString(component, "text", "");
				while ((caretstart > 0) && ((clickcount == 2) ? KeyEvent.isLetterOrDigit(text.charAt(caretstart - 1)) : (text.charAt(caretstart - 1) != '\n'))) {
					caretstart--;
				}
				while ((caretend < text.length()) && ((clickcount == 2) ? KeyEvent.isLetterOrDigit(text.charAt(caretend)) : (text.charAt(caretend) != '\n'))) {
					caretend++;
				}
			}
			setInteger(component, "start", caretstart, 0);
			setInteger(component, "end", caretend, 0);
			setFocus(component, true);
			validate(component); // caret check only
		} else if (id == MouseEvent.MOUSE_DRAGGED) {
			int mx = x - referencex;
			int my = 0;
			if (!multiline) {
				mx += getInteger(component, ":offset", 0);
			} else {
				Rectangle port = getRectangle(component, ":port");
				mx += port.x - 1;
				my = y - referencey + port.y - 1;
			}
			int dragcaret = getCaretLocation(component, mx, my, multiline, hidden);
			if (dragcaret != getInteger(component, "end", 0)) {
				setInteger(component, "end", dragcaret, 0);
				validate(component); // caret check only
			}
		} else if ((id == MouseEvent.MOUSE_ENTERED) && (mousepressed == null)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		} else if (((id == MouseEvent.MOUSE_EXITED) && (mousepressed == null))
				|| ((id == MouseEvent.MOUSE_RELEASED) && ((mouseinside != component) || (insidepart != null)))) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	protected int getCaretLocation(Object component, int x, int y, boolean multiline, boolean hidden) {
		Font currentfont = (Font) get(component, "font");
		Font fm = ((currentfont != null) ? currentfont : font);
		char[] chars = multiline ? ((char[]) get(component, ":text")) : getString(component, "text", "").toCharArray(); // update it
		int linestart = 0;
		if (multiline) {
			int height = fm.getHeight(); // find the line start by y value
			for (int i = 0; (y >= height) && (i < chars.length); i++) {
				if ((chars[i] == '\n') || (chars[i] == '\t')) {
					linestart = i + 1;
					y -= height;
				}
			}
		}
		for (int i = linestart; i < chars.length; i++) {
			if ((chars[i] == '\n') || (chars[i] == '\t')) {
				return i;
			}
			int charwidth = fm.charWidth(hidden ? '*' : chars[i]);
			if (x <= (charwidth / 2)) {
				return i;
			}
			x -= charwidth;
		}
		return chars.length;
	}

	protected boolean processScroll(int x, int y, int id, Object component, Object part) {
		if ((part == "up") || (part == "down") || (part == "left") || (part == "right")) {
			if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED) || (id == MouseEvent.MOUSE_PRESSED) || (id == MouseEvent.MOUSE_RELEASED)) {
				if (id == MouseEvent.MOUSE_PRESSED) {
					if (processScroll(component, part)) {
						setTimer(300L);
						return true;
					}
				} else {
					if (id == MouseEvent.MOUSE_RELEASED) {
						setTimer(0L);
					}
					repaint(component, null, part);
				}
			}
		} else if ((part == "uptrack") || (part == "downtrack") || (part == "lefttrack") || (part == "righttrack")) {
			if (id == MouseEvent.MOUSE_PRESSED) {
				if (processScroll(component, part)) {
					setTimer(300L);
				}
			} else if (id == MouseEvent.MOUSE_RELEASED) {
				setTimer(0L);
			}
		} else if ((part == "vknob") || (part == "hknob")) {
			if ((id == MouseEvent.MOUSE_ENTERED) || (id == MouseEvent.MOUSE_EXITED) || (id == MouseEvent.MOUSE_PRESSED) || (id == MouseEvent.MOUSE_RELEASED)) {
				repaint(component, null, (part == "vknob") ? "vertical" : "horizontal");
			}

			if (id == MouseEvent.MOUSE_PRESSED) {
				Rectangle port = getRectangle(component, ":port");
				Rectangle view = getRectangle(component, ":view");
				if (part == "hknob") {
					referencex = x - view.x * (port.width - 2 * block) / view.width;
				} else {
					referencey = y - view.y * (port.height - 2 * block) / view.height;
				}
			} else if (id == MouseEvent.MOUSE_DRAGGED) {
				Rectangle port = getRectangle(component, ":port");
				Rectangle view = getRectangle(component, ":view");
				if (part == "hknob") {
					int viewx = (x - referencex) * view.width / (port.width - 2 * block);
					viewx = Math.max(0, Math.min(viewx, view.width - port.width));
					if (view.x != viewx) {
						view.x = viewx;
						repaint(component, null, "horizontal");
					}
				} else { // (part == "vknob")
					int viewy = (y - referencey) * view.height / (port.height - 2 * block);
					viewy = Math.max(0, Math.min(viewy, view.height - port.height));
					if (view.y != viewy) {
						view.y = viewy;
						repaint(component, null, "vertical");
					}
				}
			}
		} else if (part == "corner") {
			part = "corner"; // compiler bug
		} else { // ?
			if (id == MouseEvent.MOUSE_PRESSED) {
				Rectangle port = getRectangle(component, ":port");
				if (port != null) {
					setReference(component, port.x, port.y);
				}
			}
			return false;
		}
		return true;
	}

	protected boolean processScroll(Object component, Object part) {
		Rectangle view = getRectangle(component, ":view");
		Rectangle port = ((part == "left") || (part == "up")) ? null : getRectangle(component, ":port");
		int dx = 0;
		int dy = 0;
		if (part == "left") {
			dx = -10;
		} else if (part == "lefttrack") {
			dx = -port.width;
		} else if (part == "right") {
			dx = 10;
		} else if (part == "righttrack") {
			dx = port.width;
		} else if (part == "up") {
			dy = -10;
		} else if (part == "uptrack") {
			dy = -port.height;
		} else if (part == "down") {
			dy = 10;
		} else if (part == "downtrack") {
			dy = port.height;
		}
		if (dx != 0) {
			dx = (dx < 0) ? Math.max(-view.x, dx) : Math.min(dx, view.width - port.width - view.x);
		} else if (dy != 0) {
			dy = (dy < 0) ? Math.max(-view.y, dy) : Math.min(dy, view.height - port.height - view.y);
		} else
			return false;
		if ((dx == 0) && (dy == 0)) {
			return false;
		}
		view.x += dx;
		view.y += dy;
		repaint(component, null, (dx != 0) ? "horizontal" : "vertical");
		/*
		return (((part == "left") || (part == "lefttrack")) && (view.x > 0))
		        || (((part == "right") || (part == "righttrack")) && (view.x < view.width - port.width))
		        || (((part == "up") || (part == "uptrack")) && (view.y > 0))
		        || (((part == "down") || (part == "downtrack")) && (view.y < view.height - port.height));
		 */
		return true;
	}

	protected boolean processSpin(Object component, Object part) {
		String text = getString(component, "text", "");
		try {
			int itext = Integer.parseInt(text);
			int step = getInteger(component, "step", 1);
			if ((part == "up") ? (itext + step <= getInteger(component, "maximum", Integer.MAX_VALUE)) : (itext - step >= getInteger(component, "minimum",
					Integer.MIN_VALUE))) {
				String value = String.valueOf((part == "up") ? (itext + step) : (itext - step));
				setString(component, "text", value, null);
				setInteger(component, "start", value.length(), 0);
				setInteger(component, "end", 0, 0);
				repaint(component, "spinbox", "text");
				invoke(component, null, "action");
				return true;
			}
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	/**
	 * Invokes a method, such as an action, on the specified component.
	 *
	 * @param component
	 *            the component to fire the event on, such as a textfield or
	 *            table
	 * @param part
	 *            the part of the component, null for a textfield, the row for a
	 *            table
	 * @param event
	 *            the event to send, such as 'action'
	 * @return true if a method object was fired
	 */
	// comment written by scolebourne
	protected boolean invoke(Object component, Object part, String event) { // TODO make protected?
		Object method = get(component, event);
		if (method != null) {
			invokeImpl(component, method, part);
			return true;
		}
		return false;
	}

	protected void invokeImpl(Object component, Object method, Object part) {
		Object[] data = (Object[]) method;
		Object[] args = (data.length > 2) ? new Object[(data.length - 2) / 3] : null;
		if (args != null)
			for (int i = 0; i < args.length; i++) {
				Object target = data[2 + 3 * i];
				if ("thinlet" == target) {
					args[i] = this;
				} else if (("constant" == target)) { // constant value
					args[i] = data[2 + 3 * i + 1];
				} else {
					if ("item" == target) {
						target = part;
					}
					Object parametername = data[2 + 3 * i + 1];
					if (parametername == null) {
						args[i] = target;
						// args[i] = new Widget(this, target);
					} else {
						args[i] = (target != null) ? get(target, parametername) : null;
						if (args[i] == null) {
							args[i] = data[2 + 3 * i + 2];
						}
					}
				}
			}

		// jme style reflection: string matching :(
		try {
			invokeImpl(component, (String) data[1], args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		try {
		    ((Method) data[1]).invoke(data[0], args);
		} catch (InvocationTargetException ite) {
		    handleException(ite.getTargetException());
		} catch (Throwable throwable) {
		    handleException(throwable);
		}*/
	}

	protected abstract void invokeImpl(Object component, String method, Object[] data) throws Exception;

	/**
	 * Overwrite this method to handle exceptions thrown by the invoked custom
	 * methods
	 *
	 * @param throwable
	 *            the thrown exception by the bussiness logic
	 */
	protected void handleException(Throwable throwable) {
		throwable.printStackTrace();
	}

	protected boolean findComponent(Object component, int x, int y) {
		if (component == content) {
			mouseinside = insidepart = null;
			mousex = x;
			mousey = y;
		}
		if (!getBoolean(component, "visible", true)) {
			return false;
		}
		Rectangle bounds = getRectangle(component, "bounds");
		if ((bounds == null) || !(bounds.contains(x, y))) {
			return false;
		}
		mouseinside = component;
		x -= bounds.x;
		y -= bounds.y;
		String classname = getClass(component);

		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true) && (x <= bounds.width - block)) {
				Image icon = getIcon(component, "icon", null);
				insidepart = ((icon != null) && (x <= 2 + icon.getWidth())) ? "icon" : null;
			} else {
				insidepart = "down";
			}
		} else if (":combolist" == classname) {
			if (!findScroll(component, x, y)) {
				y += getRectangle(component, ":view").y;
				for (Object choice = get(get(component, "combobox"), ":comp"); choice != null; choice = get(choice, ":next")) {
					Rectangle r = getRectangle(choice, "bounds");
					if ((y >= r.y) && (y < r.y + r.height)) {
						insidepart = choice;
						break;
					}
				}
			}
		} else if ("textarea" == classname) {
			findScroll(component, x, y);
		} else if ("tabbedpane" == classname) {
			int selected = getInteger(component, "selected", 0);
			int i = 0;
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				Rectangle r = getRectangle(tab, "bounds");
				if (i == selected) {
					Object tabcontent = get(tab, ":comp");
					if ((tabcontent != null) && findComponent(tabcontent, x - r.x, y - r.y)) {
						break;
					}
				}
				if (r.contains(x, y)) {
					insidepart = tab;
					break;
				}
				i++;
			}
		} else if (("panel" == classname) || ("desktop" == classname) || ("dialog" == classname)) {
			if ("dialog" == classname) {
				boolean resizable = getBoolean(component, "resizable", false);
				if (resizable && (x < 4)) {
					insidepart = (y < block) ? ":nw" : (y >= bounds.height - block) ? ":sw" : ":w";
				} else if (resizable && (y < 4)) {
					insidepart = (x < block) ? ":nw" : (x >= bounds.width - block) ? ":ne" : ":n";
				} else if (resizable && (x >= bounds.width - 4)) {
					insidepart = (y < block) ? ":ne" : (y >= bounds.height - block) ? ":se" : ":e";
				} else if (resizable && (y >= bounds.height - 4)) {
					insidepart = (x < block) ? ":sw" : (x >= bounds.width - block) ? ":se" : ":s";
				} else {
					int titleheight = getInteger(component, ":titleheight", 0);
					if (y < 4 + titleheight) {
						insidepart = "header";
					}
				}
			}
			if ((insidepart == null) && !findScroll(component, x, y)) {
				Rectangle port = getRectangle(component, ":port");
				if (port != null) { // content scrolled
					Rectangle view = getRectangle(component, ":view");
					x += view.x - port.x;
					y += view.y - port.y;
				}
				for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
					if (findComponent(comp, x, y)) {
						break;
					}
					if (("desktop" == classname) && getBoolean(comp, "modal", false)) {
						insidepart = "modal";
						break;
					} // && dialog
				}
			}
		} else if ("spinbox" == classname) {
			insidepart = (x <= bounds.width - block) ? null : ((y <= bounds.height / 2) ? "up" : "down");
		} else if ("splitpane" == classname) {
			Object comp1 = get(component, ":comp");
			if (comp1 != null) {
				if (!findComponent(comp1, x, y)) {
					Object comp2 = get(comp1, ":next");
					if (comp2 != null) {
						findComponent(comp2, x, y);
					}
				}
			}
		} else if ("list" == classname) {
			findScroll(component, x, y);
		} else if ("table" == classname) {
			if (!findScroll(component, x, y)) {
			}
		} else if ("tree" == classname) {
			findScroll(component, x, y);
		} else if ("menubar" == classname) {
			for (Object menu = get(component, ":comp"); menu != null; menu = get(menu, ":next")) {
				Rectangle r = getRectangle(menu, "bounds");
				if ((x >= r.x) && (x < r.x + r.width)) {
					insidepart = menu;
					break;
				}
			}
		} else if ("slider" == classname) {
			insidepart = component;
		} else if (":popup" == classname) {
			for (Object menu = get(get(component, "menu"), ":comp"); menu != null; menu = get(menu, ":next")) {
				Rectangle r = getRectangle(menu, "bounds");
				if ((y >= r.y) && (y < r.y + r.height)) {
					insidepart = menu;
					break;
				}
			}
		}
		return true;
	}

	/**
	 * @param component
	 *            a scrollable widget
	 * @param x
	 *            point x location
	 * @param y
	 *            point y location
	 * @return true if the point (x, y) is inside scroll-control area
	 *         (scrollbars, corners, borders), false otherwise (vievport,
	 *         header, or no scrollpane)
	 */
	protected boolean findScroll(Object component, int x, int y) {
		Rectangle port = getRectangle(component, ":port");
		if ((port == null) || port.contains(x, y)) {
			return false;
		}
		Rectangle view = getRectangle(component, ":view");
		Rectangle horizontal = getRectangle(component, ":horizontal");
		Rectangle vertical = getRectangle(component, ":vertical");
		if ((horizontal != null) && horizontal.contains(x, y)) {
			findScroll(x - horizontal.x, horizontal.width, port.width, view.x, view.width, true);
		} else if ((vertical != null) && vertical.contains(x, y)) {
			findScroll(y - vertical.y, vertical.height, port.height, view.y, view.height, false);
		} else {
			insidepart = "corner";
		}
		return true;
	}

	/**
	 * @param p
	 *            x or y relative to the scrollbar begin
	 * @param size
	 *            scrollbar width or height
	 * @param portsize
	 *            viewport width or height
	 * @param viewp
	 *            view x or y
	 * @param viewsize
	 *            view width or height
	 * @param horizontal
	 *            if true horizontal, vertical otherwise
	 */
	protected void findScroll(int p, int size, int portsize, int viewp, int viewsize, boolean horizontal) {
		if (p < block) {
			insidepart = horizontal ? "left" : "up";
		} else if (p > size - block) {
			insidepart = horizontal ? "right" : "down";
		} else {
			int track = size - 2 * block;
			if (track < 10) {
				insidepart = "corner";
				return;
			} // too small
			int knob = Math.max(track * portsize / viewsize, 10);
			int decrease = viewp * (track - knob) / (viewsize - portsize);
			if (p < block + decrease) {
				insidepart = horizontal ? "lefttrack" : "uptrack";
			} else if (p < block + decrease + knob) {
				insidepart = horizontal ? "hknob" : "vknob";
			} else {
				insidepart = horizontal ? "righttrack" : "downtrack";
			}
		}
	}

	protected void repaint(Object component, Object classname, Object part) {
		Rectangle b = getRectangle(component, "bounds");
		if (classname == "combobox") { // combobox down arrow
			repaint(component, b.x + b.width - block, b.y, block, b.height); // icon?+
		} else if (classname == "spinbox") {
			if (part == "text") { // spinbox textfield content
				repaint(component, b.x, b.y, b.width - block, b.height);
			} else { // spinbox increase or decrease button
				repaint(component, b.x + b.width - block, (part == "up") ? b.y : (b.y + b.height - b.height / 2), block, b.height / 2);
			}
		}
		// else if (classname == "dialog") {}
		// int titleheight = getInteger(component, ":titleheight", 0);
		// else if (classname == "splitpane") {}
		else if ((classname == "tabbedpane") || // tab
				(classname == "menubar") || (classname == ":popup")) { // menuitem
			Rectangle r = getRectangle(part, "bounds");
			repaint(component, b.x + r.x, b.y + r.y, (classname == ":popup") ? b.width : r.width, r.height);
		}
		// classname: ":combolist" "textarea" "list" "table" "tree"
		else if ((part == "left") || (part == "right")) { // horizontal
			// scrollbar button
			Rectangle r = getRectangle(component, ":horizontal");
			repaint(component, b.x + ((part == "left") ? r.x : (r.x + r.width - block)), b.y + r.y, block, r.height);
		} else if ((part == "up") || (part == "down")) { // vertical
			// scrollbar button
			Rectangle r = getRectangle(component, ":vertical");
			repaint(component, b.x + r.x, b.y + ((part == "up") ? r.y : (r.y + r.height - block)), r.width, block);
		} else if ((part == "text") || (part == "horizontal") || (part == "vertical")) {
			Rectangle port = getRectangle(component, ":port"); // textarea or
			// content
			repaint(component, b.x + port.x, b.y + port.y, port.width, port.height);
			if (part == "horizontal") {
				Rectangle r = getRectangle(component, ":horizontal");
				repaint(component, b.x + r.x, b.y + r.y, r.width, r.height);
				repaint(component, b.x + r.x, b.y, r.width, port.y); // paint
				// header
				// too
			} else if (part == "vertical") {
				Rectangle r = getRectangle(component, ":vertical");
				repaint(component, b.x + r.x, b.y + r.y, r.width, r.height);
			}
		} else { // repaint the whole line of its subcomponent
			Rectangle port = getRectangle(component, ":port");
			Rectangle view = getRectangle(component, ":view");
			Rectangle r = getRectangle(part, "bounds");
			if ((r.y + r.height >= view.y) && (r.y <= view.y + port.height)) {
				repaint(component, b.x + port.x, b.y + port.y - view.y + r.y, port.width, r.height);
				// ? need cut item rectangle above/bellow viewport
			}
		}
	}

	/**
	 * Layout and paint the given component later
	 *
	 * @param component
	 */
	protected void validate(Object component) {
		repaint(component);
		Rectangle bounds = getRectangle(component, "bounds");
		if (bounds != null) {
			bounds.width = -1 * Math.abs(bounds.width);
		}
	}

	/**
	 * Repaint the given component's area later
	 *
	 * @param component
	 *            a visible widget inside thinlet desktop
	 */
	public void repaint(Object component) {
		Rectangle bounds = getRectangle(component, "bounds");
		if (bounds != null) {
			repaint(component, bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	/**
	 * Repaint the given component's area later
	 *
	 * @param component
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void repaint(Object component, int x, int y, int width, int height) {
		while ((component = getParent(component)) != null) {
			Rectangle bounds = getRectangle(component, "bounds");
			x += bounds.x;
			y += bounds.y;
			Rectangle view = getRectangle(component, ":view");
			if (view != null) {
				Rectangle port = getRectangle(component, ":port");
				x += -view.x + port.x;
				y += -view.y + port.y; // + clip :port
			}
		}
		repaint(x, y, width, height);
	}

	/**
	 * Requests that both the <i>Thinlet</i> component, and the given widget
	 * get the input focus
	 *
	 * @param component
	 *            a focusable widget inside visible and enabled parents, and
	 *            tabbedpane's selected tab
	 * @return true, if the given component was focusable
	 */
	public boolean requestFocus(Object component) { // #
		if (isFocusable(component, true)) {
			setFocus(component, false);
			repaint(component);
			return true;
		}
		return false;
	}

	/**
	 * Request focus for the given component
	 *
	 * @param component
	 *            a focusable component
	 * @return true if the focusowner was changed, otherwise false
	 */
	protected boolean setFocus(Object component, boolean enter) {

		if (!focusinside) { // request focus for the thinlet component
			requestFocus();
		}

		if (focusowner != component) {
			Object focused = focusowner;
			if (focusowner != null) {
				focusowner = null; // clear focusowner

				Object classname = getClass(focused);
				if (classname == "spinbox" || classname == "slider" || classname == "textfield" || classname == "textarea") {
					if (!getBoolean(focused, "interactionMode", true)) {
						setBoolean(focused, "interactionState", false);
					}
				}

				repaint(focused);
				// invoke the focus listener of the previously focused component
				invoke(focused, null, "focuslost");

			}
			if (focusowner == null) { // it won't be null, if refocused
				focusowner = component;
				Object classname = getClass(component);
				if (classname == "spinbox" || classname == "slider" || classname == "textfield" || classname == "textarea") {
					if (!getBoolean(component, "interactionMode", false)) {
						setBoolean(component, "interactionState", enter);
					}
				}

				// invoke the focus listener of the new focused component
				invoke(component, null, "focusgained");
			}
			return true;
		} else {
			Object classname = getClass(component);
			if (classname == "spinbox" || classname == "slider" || classname == "textfield" || classname == "textarea") {
				if (!getBoolean(component, "interactionMode", false)) {
					setBoolean(component, "interactionState", enter);
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * @return next focusable component is found (not the first of the
	 *         desktop/dialog)
	 */
	public boolean setNextFocusable(Object current, boolean outgo) {
		boolean consumed = true;
		for (Object next = null, component = current; true; component = next) {
			next = get(component, ":comp"); // check first subcomponent
			if (next == null) {
				next = get(component, ":next");
			} // check next component
			while (next == null) { // find the next of the parents, or the
				// topmost
				component = getParent(component); // current is not on the
				// desktop
				if (component == null) {
					return false;
				}
				if ((component == content) || ((getClass(component) == "dialog") && (!outgo || getBoolean(component, "modal", false)))) {
					consumed = false; // find next focusable but does not
					// consume event
					next = component; // the topmost (desktop or modal dialog)
				} else {
					next = get(component, ":next");
				}
			}
			if (next == current) {
				return false;
			} // one fucusable, no loop
			if (isFocusable(next, false)) {
				//setFocus(next, false);
				setFocusImpl(next);
				return consumed;
			}
		}
	}

	/**
	 * @return previous focusable component is found (not the last of the
	 *         desktop/dialog)
	 */
	protected boolean setPreviousFocusable(Object component, boolean outgo) {
		for (int i = 0; i < 2; i++) { // 0 is backward direction
			Object previous = getPreviousFocusable(component, null, true, false, (i == 0), outgo);
			if (previous != null) {
				setFocus(previous, false);
				return (i == 0);
			}
		}
		return false;
	}

	/**
	 * For the starting component search its parent direction for a focusable
	 * component, and then its next component (if not search backward from the
	 * component).<br />
	 * For its parent components check its first component, the current one, and
	 * its parent direction (backward search), or its parent, then next
	 * component (forward direction).<br />
	 * For the rest components check the next, then the first subcomponent
	 * direction, and finally check whether the component is focusable.
	 */
	protected Object getPreviousFocusable(Object component, Object block, boolean start, boolean upward, boolean backward, boolean outgo) {
		Object previous = null;
		if ((component != null) && (component != block)) {
			boolean go = ((getClass(component) != "dialog") || (outgo && !getBoolean(component, "modal", false)));
			if (!start && !upward && go) {
				previous = getPreviousFocusable(get(component, ":next"), block, false, false, backward, outgo);
			}
			if ((previous == null) && ((upward && backward) || (!start && !upward))) {
				previous = getPreviousFocusable(get(component, ":comp"), block, false, false, backward, outgo);
				if ((previous == null) && isFocusable(component, false)) {
					previous = component;
				}
			}
			if ((previous == null) && (start || upward) && go) {
				previous = getPreviousFocusable(getParent(component), component, false, true, backward, outgo);
			}
			if ((previous == null) && (start || upward) && !backward && go) {
				previous = getPreviousFocusable(get(component, ":next"), block, false, false, backward, outgo);
			}
		}
		return previous;
	}

	protected boolean isEnabledAndVisible(Object component) {
		for (Object comp = component; comp != null;) {
			// component and parents are enabled and visible
			if (!getBoolean(comp, "enabled", true) || !getBoolean(comp, "visible", true)) {
				return false;
			}
			Object parent = getParent(comp);
			// inside the selected tabbedpane tab
			if ((getClass(comp) == "tab") && (getItem(parent, getInteger(parent, "selected", 0)) != comp)) {
				return false;
			}
			comp = parent;
		}
		return true;
	}

	/**
	 * Check whether the given widget can become focusowner
	 *
	 * @param component
	 *            check this widget
	 * @param forced
	 *            splitpane is also checked (e.g. false for tab navigating, and
	 *            true for mouse selection or application request)
	 * @return true if focusable, otherwise false
	 */
	protected boolean isFocusable(Object component, boolean forced) {
		String classname = getClass(component);
		if ((classname == "button") || (classname == "checkbox") || ("togglebutton" == classname) || (classname == "combobox") || (classname == "textfield")
				|| (classname == "passwordfield") || (classname == "textarea") || (classname == "spinbox") || (classname == "slider") || (classname == "list")
				|| (classname == "table") || (classname == "tree") || (classname == "tabbedpane") || (forced && (classname == "splitpane"))) {
			for (Object comp = component; comp != null;) {
				// component and parents are enabled and visible
				if (!getBoolean(comp, "enabled", true) || !getBoolean(comp, "visible", true)) {
					return false;
				}
				Object parent = getParent(comp);
				// inside the selected tabbedpane tab
				if ((getClass(comp) == "tab") && (getItem(parent, getInteger(parent, "selected", 0)) != comp)) {
					return false;
				}
				comp = parent;
			}
			return true;
		}
		return false;
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Creates a new component
	 *
	 * @param classname
	 *            the widget type (e.g. <i>button</i>)
	 * @return a new component, every component is simply an <i>Object</i>
	 * @throws java.lang.IllegalArgumentException
	 *             for unknown widget type
	 */
	public static Object create(String classname) { // #
		for (int i = 0; i < dtd.length; i += 3) {
			if (dtd[i].equals(classname)) {
				Object impl = createImpl((String) dtd[i]);
				if (dtd[i] == "spinbox" || dtd[i] == "slider" || dtd[i] == "textarea" || dtd[i] == "textfield") {
					set(impl, "tooltip", "Toggle to edit");
				}
				return impl;
			}
		}
		throw new IllegalArgumentException("unknown " + classname);
	}

	/**
	 * Gets the type of the given component
	 *
	 * @param component
	 *            a widget
	 * @return the class name of the component (e.g. <i>button</i>)
	 */
	public static String getClass(Object component) { // #
		return (String) get(component, ":class");
	}

	/**
	 * Get the topmost component
	 *
	 * @return the root object (it is a <i>desktop</i>), never <i>null</i>
	 */
	public Object getDesktop() {// #
		return content;
	}

	protected static Object createImpl(String classname) {
		//		System.out.println("CreateImpl " + classname);

		return new Object[] { ":class", classname, null };
	}

	protected static boolean set(Object component, Object key, Object value) {
		//System.out.println("Set " + component + " " + key + " " + value);
		Object[] previous = (Object[]) component;
		for (Object[] entry = previous; entry != null; entry = (Object[]) entry[2]) {
			if (entry[0] == key) {
				if (value != null) { // set the row's value
					Object oldvalue = entry[1];
					entry[1] = value;
					return !value.equals(oldvalue);
				} else { // remove the row
					previous[2] = entry[2];
					entry[2] = null;
					return true;
				}
			}
			previous = entry;
		}
		if (value != null) { // append a new row
			previous[2] = new Object[] { key, value, null };
			return true;
		}
		return false;
	}

	protected static Object get(Object component, Object key) {
		for (Object[] entry = (Object[]) component; entry != null; entry = (Object[]) entry[2]) {
			if (entry[0] == key) { // TODO doesn't work under symbian OS?
				return entry[1];
			}
		}
		return null;
	}

	/**
	 * Gets the count of subcomponents in the list of the given component
	 *
	 * @param component
	 *            a widget
	 * @return the number of components in this component
	 */
	public int getCount(Object component) {
		return getItemCountImpl(component, ":comp");
	}

	/**
	 * Gets the parent of this component
	 *
	 * @param component
	 *            a widget
	 * @return the parent container of this component or item
	 */
	public Object getParent(Object component) {
		return get(component, ":parent");
	}

	/**
	 * Gets the index of the first selected item in the given component
	 *
	 * @param component
	 *            a widget (combobox, tabbedpane, list, table, or tree)
	 * @return the first selected index or -1
	 */
	public int getSelectedIndex(Object component) {
		String classname = getClass(component);
		if ((classname == "combobox") || (classname == "tabbedpane")) {
			return getInteger(component, "selected", (classname == "combobox") ? -1 : 0);
		}
		if ((classname == "list") || (classname == "table") || (classname == "tree")) {
			Object item = get(component, ":comp");
			for (int i = 0; item != null; i++) {
				if (getBoolean(item, "selected", false)) {
					return i;
				}
				item = get(item, ":next");
			}
			return -1;
		}
		throw new IllegalArgumentException(classname);
	}

	/**
	 * Gets the first selected item of the given component
	 *
	 * @param component
	 *            a widget (combobox, tabbedpane, list, table, or tree)
	 * @return the first selected item or null
	 */
	public Object getSelectedItem(Object component) {
		String classname = getClass(component);
		if ((classname == "combobox") || (classname == "tabbedpane")) {
			int index = getInteger(component, "selected", (classname == "combobox") ? -1 : 0);
			return (index != -1) ? getItemImpl(component, ":comp", index) : null;
		}
		if ((classname == "list") || (classname == "table") || (classname == "tree")) {
			for (Object item = findNextItem(component, classname, null); item != null; item = findNextItem(component, classname, item)) {
				if (getBoolean(item, "selected", false)) {
					return item;
				}
			}
			return null;
		}
		throw new IllegalArgumentException(classname);
	}

	/**
	 * Gets the selected item of the given component (list, table, or tree) when
	 * multiple selection is allowed
	 *
	 * @param component
	 *            a widget
	 * @return the array of selected items, or a 0 length array
	 */
	public Object[] getSelectedItems(Object component) {
		String classname = getClass(component);
		Object[] selecteds = new Object[0];
		for (Object item = findNextItem(component, classname, null); item != null; item = findNextItem(component, classname, item)) {
			if (getBoolean(item, "selected", false)) {
				Object[] temp = new Object[selecteds.length + 1];
				System.arraycopy(selecteds, 0, temp, 0, selecteds.length);
				temp[selecteds.length] = item;
				selecteds = temp;
			}
		}
		return selecteds;
	}

	/**
	 * @return the first or the next item of the (list, table, or tree)
	 *         component
	 */
	protected Object findNextItem(Object component, String classname, Object item) {
		if (item == null) { // first item
			return get(component, ":comp");
		} else if ("tree" == classname) { // next tree node
			Object next = get(item, ":comp");
			if ((next == null) || !getBoolean(item, "expanded", true)) { // no
				// subnode
				// or
				// collapsed
				while ((item != component) && ((next = get(item, ":next")) == null)) {
					item = getParent(item); // next node of in backward path
				}
			}
			return next;
		} else { // next list or tree item
			return get(item, ":next");
		}
	}

	/**
	 * Removes all the components from this container's specified list
	 *
	 * @param component
	 *            the specified container
	 */
	public void removeAll(Object component) {
		if (get(component, ":comp") != null) {
			set(component, ":comp", null);
			update(component, "validate");
		}
	}

	protected static int getItemCountImpl(Object component, String key) {
		int i = 0;
		for (Object comp = get(component, key); comp != null; comp = get(comp, ":next")) {
			i++;
		}
		return i;
	}

	/**
	 * Returns the subcomponent of the given component's specified list at the
	 * given index
	 *
	 * @param component
	 *            a specified container
	 * @param index
	 *            the index of the component to get
	 * @return the index<sup>th</sup> component in this container
	 */
	public Object getItem(Object component, int index) {
		return getItemImpl(component, ":comp", index);
	}

	/**
	 * Gets all the components in this container
	 *
	 * @param component
	 *            a specified container
	 * @return an array of all the components in this container
	 */
	public Object[] getItems(Object component) {
		Object[] items = new Object[getItemCountImpl(component, ":comp")];
		Object comp = get(component, ":comp");
		for (int i = 0; i < items.length; i++) {
			items[i] = comp;
			comp = get(comp, ":next");
		}
		return items;
	}

	/**
	 * Referenced by DOM, replace by getItem for others
	 */
	protected static Object getItemImpl(Object component, Object key, int index) {
		int i = 0;
		for (Object item = get(component, key); item != null; item = get(item, ":next")) {
			if (i == index) {
				return item;
			}
			i++;
		}
		return null;
	}

	protected int getIndex(Object component, Object value) {
		int index = 0;
		for (Object item = get(component, ":comp"); item != null; item = get(item, ":next")) {
			if (value == item) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * Adds the specified component to the root desktop
	 *
	 * @param component
	 *            a widget to be added
	 */
	public void add(Object component) {
		add(content, component, 0);
	}

	/**
	 * Adds the specified component to the end of the specified container
	 *
	 * @param parent
	 *            a container
	 * @param component
	 *            a component to be added
	 */
	public void add(Object parent, Object component) {
		add(parent, component, -1);
	}

	/**
	 * Adds the specified component to the container at the given position
	 *
	 * @param parent
	 *            a container
	 * @param component
	 *            a component to be inserted
	 * @param index
	 *            the position at which to insert the component, or -1 to insert
	 *            the component at the end
	 */
	public void add(Object parent, Object component, int index) {
		addImpl(parent, component, index);
		update(component, "validate");
		/*
		if (parent == content) {
		    setNextFocusable(component, false);
		}
		 */
	}

	/**
	 * Referenced by DOM
	 */
	protected void insertItem(Object parent, Object key, Object component, int index) {
		Object item = parent, next = get(parent, key);
		for (int i = 0;; i++) {
			if ((i == index) || (next == null)) {
				set(item, key, component);
				set(component, ":next", next);
				break;
			}
			next = get(item = next, key = ":next");
		}
	}

	/**
	 * Remove the specified component from its parent list, or delete
	 * component's popupmenu or table's header
	 *
	 * @param component
	 *            the component to be removed
	 */
	public void remove(Object component) {
		update(component, "validate");
		Object parent = getParent(component);
		Object classname = getClass(component);
		if (("popupmenu" == classname) || ("header" == classname)) {
			set(parent, classname, null);
		} else {
			removeItemImpl(parent, component);
			// request focus for its parent if the component (or subcomponent) is
			// currently focused
			/*
			for (Object comp = focusowner; comp != null; comp = getParent(comp)) {
			    if (comp == component) {
			        setNextFocusable(parent, false);
			        break;
			    }
			}
			 */
		}
	}

	/**
	 * Delete the give component from its parent list
	 *
	 * @param parent
	 * @param component
	 */
	protected void removeItemImpl(Object parent, Object component) {
		Object previous = null; // the widget before the given component
		for (Object comp = get(parent, ":comp"); comp != null;) {
			Object next = get(comp, ":next");
			if (next == component) {
				previous = comp;
				break;
			}
			comp = next;
		}
		set((previous != null) ? previous : parent, (previous != null) ? ":next" : ":comp", get(component, ":next"));
		set(component, ":next", null);
		set(component, ":parent", null); // not required
	}

	/**
	 * Finds the first component from the root desktop by a specified name value
	 *
	 * @param name
	 *            parameter value identifies the widget
	 * @return the first suitable component, or null
	 */
	public Object find(String name) {
		return find(content, name);
	}

	/**
	 * Finds the first component from the specified component by a name
	 *
	 * @param component
	 *            the widget is searched inside this component
	 * @param name
	 *            parameter value identifies the widget
	 * @return the first suitable component, or null
	 */
	public Object find(Object component, String name) {
		if (name.equals(get(component, "name"))) {
			return component;
		}
		// otherwise search in its subcomponents
		Object found = null;
		for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
			if ((found = find(comp, name)) != null) {
				return found;
			}
		}
		// search in table header
		Object header = get(component, "header"); // if ("table" == classname)
		if ((header != null) && ((found = find(header, name)) != null)) {
			return found;
		}
		// search in component's popupmenu
		Object popupmenu = get(component, "popupmenu"); // if
		// instance(classname,
		// "component")
		if ((popupmenu != null) && ((found = find(popupmenu, name)) != null)) {
			return found;
		}
		return null;
	}

	/**
	 * mnemonic (e.g. Alt-X): - check: label, button, checkbox, togglebutton,
	 * menubar menus, tabbedpane tabs - path: panel, desktop, dialog, splitpane
	 * components, tabbedpane selected component accelerator (e.g. Ctrl-Shift-X,
	 * F4): - check: menuitem, checkboxmenuitem - path: see above, and menubar,
	 * and menu items menubar F10: check menubar only button enter, escape:
	 * check button only
	 *
	 * @param component
	 * @param parent
	 *            check upwards if true
	 * @param checked
	 *            this leaf is already checked
	 * @param mnemonic
	 * @return true if the char was consumed
	 */
	protected boolean checkMnemonic(Object component, boolean parent, Object checked, int keycode, int modifiers) {
		// Check mnemonic
		if ((component == null) || !getBoolean(component, "visible", true) || !getBoolean(component, "enabled", true)) { // + enabled
			// comp in
			// disabled
			// parent
			return false;
		}
		String classname = getClass(component);
		if ("label" == classname) {
			if (hasMnemonic(component, keycode, modifiers)) {
				Object labelfor = get(component, "for");
				if (labelfor != null) {
					requestFocus(labelfor);
					return true;
				}
			}
		} else if ("button" == classname) {
			if (((modifiers == 0) && (((keycode == KeyEvent.VK_ENTER) && (get(component, "type") == "default")) || ((keycode == KeyEvent.VK_ESCAPE) && (get(
					component, "type") == "cancel"))))
					|| hasMnemonic(component, keycode, modifiers)) {
				invoke(component, null, "action");
				repaint(component);
				return true;
			}
		} else if (("checkbox" == classname) || ("togglebutton" == classname)) {
			if (hasMnemonic(component, keycode, modifiers)) {
				changeCheck(component, true);
				repaint(component);
				return true;
			}
		} else if ("menubar" == classname) {
			for (Object menu = get(component, ":comp"); menu != null; menu = get(menu, ":next")) {
				if (hasMnemonic(menu, keycode, modifiers) || ((modifiers == 0) && (keycode == KeyEvent.VK_F10))) {
					closeup();
					set(component, "selected", menu);
					popupMenu(component);
					repaint(component, "menubar", menu);
					return true;
				}
			}
		} else if (("menuitem" == classname) || ("checkboxmenuitem" == classname)) {
			if (hasAccelerator(component, keycode, modifiers)) {
				invoke(component, null, "action");
			}
		} else if ("tabbedpane" == classname) {
			int selected = getInteger(component, "selected", 0);
			int i = 0;
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				if (hasMnemonic(tab, keycode, modifiers)) {
					if (selected != i) {
						setInteger(component, "selected", i, 0);
						repaint(component);
						invoke(component, getItem(component, i), "action");
					}
					return true;
				}
				i++;
			}
			Object comp = get(getItem(component, selected), ":comp");
			if ((comp != null) && (comp != checked) && checkMnemonic(comp, false, null, keycode, modifiers)) {
				return true;
			}
		}
		// check subcomponents
		if (("panel" == classname) || ("desktop" == classname) || ("dialog" == classname) || ("splitpane" == classname) || ("menubar" == classname)
				|| ("menu" == classname)) {
			for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
				if ((comp != checked) && checkMnemonic(comp, false, null, keycode, modifiers)) {
					return true;
				}
			}
		}
		// check parent
		if (parent && (("dialog" != classname) || !getBoolean(component, "modal", false))) {
			if (checkMnemonic(getParent(component), true, ("tab" == classname) ? checked : component, keycode, modifiers)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param component
	 * @param keycode
	 * @param modifiers
	 * @return true if the component has the given mnemonic
	 */
	protected boolean hasMnemonic(Object component, int keycode, int modifiers) {
		if (modifiers == InputEvent.ALT_MASK) {
			int index = getInteger(component, "mnemonic", -1);
			if (index != -1) {
				String text = getString(component, "text", null);
				return (text != null) && (text.length() > index) && (Character.toUpperCase(text.charAt(index)) == keycode);
			}
		}
		return false;
	}

	/**
	 * @param component
	 * @param keycode
	 * @param modifiers
	 * @return true if the component has the given accelerator
	 */
	protected boolean hasAccelerator(Object component, int keycode, int modifiers) {
		Object accelerator = get(component, "accelerator");
		if (accelerator != null) {
			long keystroke = ((Long) accelerator).longValue();
			return ((keystroke >> 32) == modifiers) && ((keystroke & 0xffff) == keycode);
		}
		return false;
	}

	/**
	 * Binds the specified key to the specified value, and stores in this
	 * component. <i>Null</i> value removes the property. Use the parameter tag
	 * in the xml resource to bind a string value, the format is:
	 * <i>parameter='key=value'</i>
	 *
	 * @param component
	 *            the hashtable is binded to this component
	 * @param key
	 *            the client property key
	 * @param value
	 *            the new client property value
	 */
	public void putProperty(Object component, Object key, Object value) {
		Object table = get(component, ":bind");
		if (value != null) {
			if (table == null) {
				set(component, ":bind", table = new Hashtable());
			}
			((Hashtable) table).put(key, value);
		} else if (table != null) {
			((Hashtable) table).remove(key);
		}
	}

	/**
	 * Returns the value of the property with the specified key.
	 *
	 * @param component
	 *            searches the hashtable of this component
	 * @param key
	 *            the client property key
	 * @return the value to which the key is mapped or null if the key is not
	 *         mapped to any value
	 */
	public Object getProperty(Object component, Object key) {
		Object table = get(component, ":bind");
		return (table != null) ? ((Hashtable) table).get(key) : null;
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Creates a component (and its subcomponents, and properties) from the
	 * given xml resource
	 *
	 * @param path
	 *            is relative to your thinlet instance or the classpath (if the
	 *            path starts with an <i>/</i> character), or a full URL
	 * @return the root component of the parsed resource
	 * @throws java.io.IOException
	 */
	public Object parse(String path) throws IOException {

		InputStream inputstream = getClass().getResourceAsStream(path);
		if (inputstream == null) {
			throw new RuntimeException("Could not read " + path);
		}
		return parseImpl(inputstream, this);
	}

	/**
	 * Creates a component from the given stream
	 *
	 * @param inputstream
	 *            e.g. <i>new URL("http://myserver/myservlet").openStream()</i>
	 * @return the root component of the parsed stream
	 * @throws java.io.IOException
	 */
	public Object parse(InputStream inputstream) throws IOException {
		return parseImpl(inputstream, this);
	}

	protected Object parseImpl(InputStream inputstream, Object handler) throws IOException {
		if (inputstream == null) {
			throw new RuntimeException("Null input stream");
		}
		Reader reader = new InputStreamReader(inputstream);
		try {
			Object[] parentlist = null;
			Object current = null;
			Vector methods = null;
			String encoding = "UTF-8"; // encoding value of xml declaration

			XmlReader parser = new XmlReader(reader);

			if (parser.moveToStartElement()) {

				int event = XmlReader.START_TAG;
				do {

					if (event == XmlReader.START_TAG) {
						String tagname = parser.getName();

						parentlist = new Object[] { current, parentlist, tagname };
						current = (current != null) ? addElement(current, tagname) : create(tagname);

						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String key = parser.getAttributeName(i);
							String text = parser.getAttributeValue(i);
							methods = addAttribute(current, key, text.toString(), encoding, methods);
						}

					} else if (event == XmlReader.END_TAG) {
						if (parentlist[0] == null) {
							finishParse(methods, current, handler);
							return current;
						}

						// step to parent
						current = parentlist[0];
						parentlist = (Object[]) parentlist[1];

					}
					event = parser.next();
				} while (event != XmlReader.END_DOCUMENT);
			}
			finishParse(methods, current, handler);
			return current;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * @param methods
	 *            methods and label's 'for' widgets are stored in this vector
	 *            because these may reference to widgets which are not parsed at
	 *            that time
	 */

	protected void finishParse(Vector methods, Object root, Object handler) {
		if (methods != null)
			for (int i = 0; i < methods.size(); i += 3) {
				Object component = methods.elementAt(i);
				Object[] definition = (Object[]) methods.elementAt(i + 1);
				String value = (String) methods.elementAt(i + 2);

				if ("method" == definition[0]) {
					Object[] method = getMethod(component, value, root, handler);
					if ("init" == definition[1]) {
						invokeImpl(null, method, null); // ignore component on init!
					} else {
						set(component, definition[1], method);
					}
				} else { // ("component" == definition[0])
					Object reference = find(root, value); // +start find from
					// the component
					if (reference == null)
						throw new IllegalArgumentException(value + " not found");
					set(component, definition[1], reference);
				}
			}
	}

	/**
	 * Add the component to the parent's ':comp' list, and set its ':parent' or
	 * set single components
	 *
	 * @param index
	 *            add at the specified index
	 * @throws java.lang.IllegalArgumentException
	 */
	protected void addImpl(Object parent, Object component, int index) {
		String parentclass = getClass(parent);
		String classname = getClass(component);
		if ((("combobox" == parentclass) && ("choice" == classname))
				|| (("tabbedpane" == parentclass) && ("tab" == classname))
				|| (("list" == parentclass) && ("item" == classname))
				|| (("table" == parentclass) && ("row" == classname))
				|| (("header" == parentclass) && ("column" == classname))
				|| (("row" == parentclass) && ("cell" == classname))
				|| ((("tree" == parentclass) || ("node" == parentclass)) && ("node" == classname))
				|| (("menubar" == parentclass) && ("menu" == classname))
				|| ((("menu" == parentclass) || ("popupmenu" == parentclass)) && (("menu" == classname) || ("menuitem" == classname)
						|| ("checkboxmenuitem" == classname) || ("separator" == classname)))
				|| ((("panel" == parentclass) || ("desktop" == parentclass) || ("splitpane" == parentclass) || ("dialog" == parentclass) || ("tab" == parentclass))
						&& instance(classname, "component") && (classname != "popupmenu"))) {
			insertItem(parent, ":comp", component, index);
			set(component, ":parent", parent);
		} else if ((("table" == parentclass) && ("header" == classname)) || (("popupmenu" == classname) && instance(parentclass, "component"))) {
			set(parent, classname, component);
			set(component, ":parent", parent);
		} else
			throw new IllegalArgumentException(classname + " add " + parentclass);
	}

	protected boolean instance(Object classname, Object extendclass) {
		if (classname == extendclass) {
			return true;
		}
		for (int i = 0; i < dtd.length; i += 3) {
			if (classname == dtd[i]) {
				return instance(dtd[i + 1], extendclass);
			}
		}
		return false;
	}

	public Object addElement(Object parent, String name) {
		Object component = create(name);
		addImpl(parent, component, -1);
		return component;
	}

	/**
	 * Called by the <code>parse</code> method
	 *
	 * @throws UnsupportedEncodingException
	 * @throws java.lang.IllegalArgumentException
	 */
	public Vector addAttribute(Object component, String key, String value, String encoding, Vector lasts) throws UnsupportedEncodingException {
		Object[] definition = getDefinition(getClass(component), key, null);
		key = (String) definition[1];
		if ("string" == definition[0]) {
			value = (encoding == null) ? new String(value) : new String(value.getBytes(), 0, value.length(), encoding);
			setString(component, key, value, (String) definition[3]);
		} else if ("choice" == definition[0]) {
			String[] values = (String[]) definition[3];
			setChoice(component, key, value, values, values[0]);
		} else if ("boolean" == definition[0]) {
			if ("true".equals(value)) {
				if (definition[3] == Boolean.FALSE) {
					set(component, key, Boolean.TRUE);
				}
			} else if ("false".equals(value)) {
				if (definition[3] == Boolean.TRUE) {
					set(component, key, Boolean.FALSE);
				}
			} else
				throw new IllegalArgumentException(value);
		} else if ("integer" == definition[0]) {

			int integer = 0;
			if (value.startsWith("#")) {
				integer = Integer.parseInt(value.substring(1), 16);
			} else if (value.startsWith("0x")) {
				integer = Integer.parseInt(value.substring(2), 16);
			} else if (value.indexOf(',') != -1) { // three separated integer including red, green, and
				// blue
				Vector tokens = getTokens(value, new char[]{' ', '\r', '\n', '\t', ','});
				integer = 0xff000000 | ((Integer.parseInt((String)tokens.elementAt(0)) & 0xff) << 16) | ((Integer.parseInt((String)tokens.elementAt(1)) & 0xff) << 8)
						| (Integer.parseInt((String)tokens.elementAt(2)) & 0xff);
			} else {
				try {
					integer = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					int prosent = Integer.parseInt(value.substring(0, value.length() - 1));

					char unit = Character.toLowerCase(value.charAt(value.length() - 1));

					if (unit == 'w') {
						integer = (getWidth() * prosent) / 100;
					} else if (unit == 'h') {
						integer = (getHeight() * prosent) / 100;
					} else
						throw new RuntimeException();
				}
			}
			set(component, key, new Integer(integer));
		} else if ("icon" == definition[0]) {
			set(component, key, getIcon(value));
		} else if (("method" == definition[0]) || ("component" == definition[0])) {
			if (lasts == null) {
				lasts = new Vector();
			}
			lasts.addElement(component);
			lasts.addElement(definition);
			lasts.addElement(value);
		} else if ("property" == definition[0]) {
			Vector tokens = getTokens(value, new char[]{';'});

			for(int i = 0; i < tokens.size(); i++) {
				String token = (String)tokens.elementAt(i);
				int equals = token.indexOf('=');
				if (equals == -1) {
					throw new IllegalArgumentException(token);
				}
				putProperty(component, new String(token.substring(0, equals)), new String(token.substring(equals + 1)));
			}
		} else if ("font" == definition[0]) {
			String name = null;
			boolean bold = false;
			boolean italic = false;
			int size = 0;
			Vector tokens = getTokens(value, new char[]{' ', '\t', '\n', '\r', '\f'}); // " \t\n\r\f"
			for(int i = 0; i < tokens.size(); i++) {
				String token = (String)tokens.elementAt(i);
				if ("bold".equalsIgnoreCase(token)) {
					bold = true;
				} else if ("italic".equalsIgnoreCase(token)) {
					italic = true;
				} else {
					try {
						size = Integer.parseInt(token);
					} catch (NumberFormatException nfe) {
						name = (name == null) ? new String(token) : (name + " " + token);
					}
				}
			}
			/*
			if (name == null) {
			    name = font.getName();
			}
			 */
			if (size == 0) {
				size = font.getSize();
			}
			int fontNameIdentifier;
			if (name == null) {
				name = "FACE_SYSTEM";
			}

			if (name.equals("FACE_PROPORTIONAL")) {
				fontNameIdentifier = Font.FACE_PROPORTIONAL;
			} else if (name.equals("FACE_MONOSPACE")) {
				fontNameIdentifier = Font.FACE_MONOSPACE;
			} else
				fontNameIdentifier = Font.FACE_SYSTEM;

			if (size < 12) {
				size = Font.SIZE_SMALL;
			} else if (size < 14) {
				size = Font.SIZE_MEDIUM;
			} else
				size = Font.SIZE_LARGE;

			set(component, key, Font.getFont(fontNameIdentifier, (bold ? Font.STYLE_BOLD : 0) | (italic ? Font.STYLE_ITALIC : 0), size));

		} else if ("keystroke" == definition[0]) {
			setKeystrokeImpl(component, key, value);
		} else if ("bean" == definition[0]) {
			throw new RuntimeException("Not implemented");
			/*
			try {
			    Component bean = (Component) Class.forName(value).newInstance();
			    set(component, key, bean);
			} catch (Exception exc) {
			    throw new IllegalArgumentException(value);
			}*/
		} else
			throw new IllegalArgumentException((String) definition[0]);
		return lasts;
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	protected static Object[] getDefinition(Object classname, String key, String type) {
		Object currentname = classname;
		while (classname != null) {
			for (int i = 0; i < dtd.length; i += 3) {
				if (dtd[i] == classname) {
					Object[][] attributes = (Object[][]) dtd[i + 2];
					if (attributes != null) {
						for (int j = 0; j < attributes.length; j++) {
							if (attributes[j][1].equals(key)) {
								if ((type != null) && (type != attributes[j][0])) {
									throw new IllegalArgumentException(attributes[j][0].toString());
								}
								return attributes[j];
							}
						}
					}
					classname = dtd[i + 1];
					break;
				}
			}
		}
		throw new IllegalArgumentException("unknown " + key + " " + type + " for " + currentname);
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Sets the given property pair (key and value) for the component
	 */
	public void setString(Object component, String key, String value) {
		Object[] definition = getDefinition(getClass(component), key, "string");
		if (setString(component, (String) definition[1], value, (String) definition[3])) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the property value of the given component by the property key
	 */
	public String getString(Object component, String key) {
		return (String) get(component, key, "string");

	}

	/**
	 * Sets the given property pair (key and value) for the component
	 */
	public void setChoice(Object component, String key, String value) {
		Object[] definition = getDefinition(getClass(component), key, "choice");
		String[] values = (String[]) definition[3];
		if (setChoice(component, (String) definition[1], value, values, values[0])) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the property value of the given component by the property key
	 */
	public String getChoice(Object component, String key) {
		Object[] definition = getDefinition(getClass(component), key, "choice");
		return getString(component, (String) definition[1], ((String[]) definition[3])[0]);
	}

	/**
	 * Sets the given property pair (key and value) for the component
	 */
	public void setBoolean(Object component, String key, boolean value) {
		Object[] definition = getDefinition(getClass(component), key, "boolean");
		if (setBoolean(component, (String) definition[1], value, (definition[3] == Boolean.TRUE))) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the property value of the given component by the property key
	 */
	public boolean getBoolean(Object component, String key) {
		return get(component, key, "boolean") == Boolean.TRUE;
	}

	/**
	 * Sets the given property pair (key and value) for the component
	 */
	public void setInteger(Object component, String key, int value) {
		Object[] definition = getDefinition(getClass(component), key, "integer");
		if (setInteger(component, (String) definition[1], value, ((Integer) definition[3]).intValue())) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the property value of the given component by the property key
	 */
	public int getInteger(Object component, String key) {
		return ((Integer) get(component, key, "integer")).intValue();
	}

	/**
	 * Sets the given property pair (key and value) for the component
	 */
	public void setIcon(Object component, String key, Image icon) {
		Object[] definition = getDefinition(getClass(component), key, "icon");
		if (set(component, definition[1], icon)) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the property value of the given component by the property key
	 */
	public Image getIcon(Object component, String key) {
		return (Image) get(component, key, "icon");
	}

	public void setKeystroke(Object component, String key, String value) {
		Object[] definition = getDefinition(getClass(component), key, "keystroke");
		// TODO check if changed
		setKeystrokeImpl(component, (String) definition[1], value);
		update(component, definition[2]);
	}

	/**
	 * Get the AWT component of the given (currently <i>bean</i>) widget
	 *
	 * @param component
	 *            a <i>bean</i> widget
	 * @param key
	 *            the identifier of the parameter
	 * @return an AWT component, or null
	 */
	/*
	public Component getComponent(Object component, String key) {
	    return (Component) get(component, key, "bean");
	}*/

	/**
	 * Set custom font on a component, use the other <code>setFont</code>
	 * method instead
	 */
	public void setFont(Object component, Font font) { // deprecated
		setFont(component, "font", font);
	}

	/**
	 * Set custom font on a component
	 *
	 * @param component
	 *            component to use the custom font
	 * @param font
	 *            custom font to use, or null to reset component to use default
	 *            font
	 */
	public void setFont(Object component, String key, Font font) {
		Object[] definition = getDefinition(getClass(component), key, "font");
		if (set(component, definition[1], font)) {
			update(component, definition[2]);
		}
	}

	/**
	 * Get custom font of a component.
	 *
	 * @param component
	 *            a component
	 * @param key
	 *            the identifier of the parameter, e.g. "font"
	 * @return may return null if the default font is used
	 */
	public Font getFont(Object component, String key) { // written by abial
		return (Font) get(component, key, "font");
	}

	/**
	 * Set the AWT component for the given (currently <i>bean</i>) widget
	 *
	 * @param component
	 *            a <i>bean</i> widget
	 * @param key
	 *            the identifier of the parameter
	 * @param bean
	 *            an AWT component, or null
	 */
	/*
	public void setComponent(Object component, String key, Component bean) {
	    Object[] definition = getDefinition(getClass(component), key, "bean");
	    if (set(component, definition[1], bean)) { // noticed by Dawid Weiss
	        update(component, definition[2]);
	    }
	}*/

	protected void setKeystrokeImpl(Object component, String key, String value) {
		Long keystroke = null;
		if (value != null) {
			String token = value;
			try {
				int keycode = 0, modifiers = 0;
				Vector tokens = getTokens(value, new char[]{' ', '\r', '\n', '\t', '+'}); // " \r\n\t+"
				for(int i = 0; i < tokens.size(); i++) {

					token = ((String)tokens.elementAt(i)).toUpperCase();

					try {
						modifiers = modifiers | InputEvent.getField(token + "_MASK"); //.getInt(null);
					} catch (Exception exc) { // not mask value
						keycode = KeyEvent.getField("VK_" + token); // .getInt(null);
					}

				}
				keystroke = new Long(((long) modifiers) << 32 | keycode);
			} catch (Exception exc) {
				throw new IllegalArgumentException(token);
			}
		}
		set(component, key, keystroke);
	}

	/**
	 *
	 * Method which replaces the StringTokenizer
	 *
	 * @param value
	 * @param breaks
	 * @return
	 */

	public static Vector getTokens(String value, char[] breaks) {
		Vector vector = new Vector();

		StringBuffer buffer = new StringBuffer();

		scan:
		for(int i = 0; i < value.length(); i++) {
			char next = value.charAt(i);

			for(int k = 0; k < breaks.length; k++) {
				if(next == breaks[k]) {
					if(buffer.length() > 0) {
						vector.addElement(buffer.toString());

						buffer.setLength(0);
					}

					continue scan;
				}
			}

			buffer.append(next);
		}

		if(buffer.length() > 0) {
			vector.addElement(buffer.toString());
		}

		return vector;
	}
	// TODO add set/getComponent for popupmenu and header

	public Object getWidget(Object component, String key) {
		if ("popupmenu".equals(key)) {
			return get(component, "popupmenu");
		} else if ("header".equals(key)) {
			return get(component, "header");
		} else
			throw new IllegalArgumentException(key);
	}

	protected static Object get(Object component, String key, String type) {
		Object[] definition = getDefinition(getClass(component), key, type);
		Object value = get(component, definition[1]);
		return (value != null) ? value : definition[3];
	}

	/**
	 * Sets a new event handler method for a component
	 *
	 * @param component
	 *            the target component
	 * @param key
	 *            the key name of the parameter (e.g. <i>action</i>)
	 * @param value
	 *            the method name and parameters (e.g. <i>foo(this, this.text,
	 *            mybutton, mybutton.enabled)</i> for <i>public void foo(Object
	 *            component, String text, Object mybutton, boolean enabled)</i>)
	 * @param root
	 *            the search starting component for name components in the
	 *            arguments
	 * @param handler
	 *            the target event handler object including the method
	 * @throws java.lang.IllegalArgumentException
	 */
	public void setMethod(Object component, String key, String value, Object root, Object handler) {
		key = (String) getDefinition(getClass(component), key, "method")[1];
		Object[] method = getMethod(component, value, root, handler);
		set(component, key, method);
	}

	/**
	 * @return an object list including as follows: - handler object, - method, -
	 *         list of parameters including 3 values: - ("thinlet", null, null)
	 *         for the single thinlet component, - (target component, null,
	 *         null) for named widget as parameter, e.g. mybutton, - (target,
	 *         parameter name, default value) for a widget's given property,
	 *         e.g. mylabel.enabled, - ("item", null, null) for an item of the
	 *         target component as parameter, e.g. tree node, - ("item",
	 *         parameter name, default value) for the item's given property e.g.
	 *         list item's text, - ("constant", string object, null) for
	 *         constant number (int, long, double, float) or string given as
	 *         'text'.
	 */
	protected Object[] getMethod(Object component, String value, Object root, Object handler) {
		Vector tokens = getTokens(value, new char[]{'(', ',', ' ', '\r', '\n', '\t', ')'}); // "(, \r\n\t)"

		String methodname = (String)tokens.elementAt(0);
		int n = tokens.size() - 1;
		Object[] data = new Object[2 + 3 * n];
		Class[] parametertypes = (n > 0) ? new Class[n] : null;
		for (int i = 0; i < n; i++) {
			String arg = (String)tokens.elementAt(i + 1);
			if ("thinlet".equals(arg)) {
				data[2 + 3 * i] = "thinlet"; // the target component
				parametertypes[i] = Thinlet.class;
			} else if ((arg.length() > 1) && // constant string value
					(arg.charAt(0) == '\'') && (arg.charAt(arg.length() - 1) == '\'')) {
				data[2 + 3 * i] = "constant";
				data[2 + 3 * i + 1] = new String(arg.substring(1, arg.length() - 1));
				parametertypes[i] = String.class;
			} else {
				int dot = arg.indexOf('.');
				String compname = (dot == -1) ? arg : arg.substring(0, dot);
				Object comp = null;
				String classname = null;
				if ("item".equals(compname)) {
					comp = "item";
					String parentclass = getClass(component);
					if ("list" == parentclass) {
						classname = "item";
					} else if ("tree" == parentclass) {
						classname = "node";
					} else if ("table" == parentclass) {
						classname = "row";
					} else if ("combobox" == parentclass) {
						classname = "choice";
					} else if ("tabbedpane" == parentclass) {
						classname = "tab";
					} else
						throw new IllegalArgumentException(parentclass + " has no item");
				} else if ("this".equals(compname)) {
					comp = component;
					classname = getClass(comp);
				} else if ("null".equals(compname)) {
					data[2 + 3 * i] = "null";
				} else if ((comp = find(root, compname)) != null) { // a
					// widget's
					// name
					classname = getClass(comp);
				} else {
					try { // maybe constant number
						if (arg.regionMatches(true, arg.length() - 1, "F", 0, 1)) { // float
							data[2 + 3 * i + 1] = Float.valueOf(arg.substring(0, arg.length() - 1));
							parametertypes[i] = Float.class;
						} else if (arg.regionMatches(true, arg.length() - 1, "L", 0, 1)) { // long
							data[2 + 3 * i + 1] = new Long(Long.parseLong(arg.substring(0, arg.length() - 1)));
							parametertypes[i] = Long.class;
						} else if (dot != -1) { // double
							data[2 + 3 * i + 1] = Double.valueOf(arg);
							parametertypes[i] = Double.class;
						} else { // integer
							data[2 + 3 * i + 1] = Integer.valueOf(arg);
							parametertypes[i] = Integer.class;
						}
						data[2 + 3 * i] = "constant";
						continue;
					} catch (NumberFormatException nfe) { // widget's name not
						// found nor
						// constant
						throw new IllegalArgumentException("Unknown " + arg);
					}
				}
				data[2 + 3 * i] = comp; // the target component
				if (dot == -1) {
					parametertypes[i] = Object.class; // Widget.class
				} else {
					Object[] definition = getDefinition(classname, arg.substring(dot + 1), null);
					data[2 + 3 * i + 1] = definition[1]; // parameter name,
					// e.g. enabled
					data[2 + 3 * i + 2] = definition[3]; // default value,
					// e.g. Boolean.TRUE
					Object fieldclass = definition[0];
					if ((fieldclass == "string") || (fieldclass == "choice")) {
						parametertypes[i] = String.class;
					} else if (fieldclass == "boolean") {
						parametertypes[i] = Boolean.class;
					} else if (fieldclass == "integer") {
						parametertypes[i] = Integer.class;
					} else if (fieldclass == "icon") {
						parametertypes[i] = Image.class;
					} else
						throw new IllegalArgumentException((String) fieldclass);
				}
			}
		}
		data[0] = handler;
		try {
			data[1] = methodname;
			return data;
		} catch (Exception exc) {
			throw new IllegalArgumentException(value + " " + exc.getMessage());
		}
	}

	protected void update(Object component, Object mode) {
		if ("parent" == mode) {
			component = getParent(component);
			mode = "validate";
		}
		boolean firstpaint = true;
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		while (component != null) {
			if (!getBoolean(component, "visible", true)) {
				break;
			}
			if ("paint" == mode) {// || (firstpaint && (component == content))
				Rectangle bounds = getRectangle(component, "bounds");
				if (bounds == null) {
					return;
				}
				if (firstpaint) {
					x = bounds.x;
					y = bounds.y;
					width = Math.abs(bounds.width);
					height = bounds.height;
					firstpaint = false;
				} else {
					x += bounds.x;
					y += bounds.y;
				}
				if (component == content) {
					repaint(x, y, width, height);
				}
			}
			Object parent = getParent(component);
			String classname = getClass(parent);
			if ("combobox" == classname) {
				parent = get(parent, ":combolist");
			} else if ("menu" == classname) {
				parent = get(parent, ":popup");
			} else if (("paint" == mode) && ("tabbedpane" == classname)) {
				if (getItem(parent, getInteger(parent, "selected", 0)) != component) {
					break;
				}
			}
			if (("layout" == mode)
					|| (("validate" == mode) && (("list" == classname) || ("table" == classname) || ("tree" == classname) || ("dialog" == classname) || (parent == content)))) {
				Rectangle bounds = getRectangle(parent, "bounds");
				if (bounds == null) {
					return;
				}
				bounds.width = -1 * Math.abs(bounds.width);
				mode = "paint";
			}
			component = parent;
		}
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	protected boolean setString(Object component, String key, String value, String defaultvalue) {
		return set(component, key, value); // use defaultvalue
	}

	protected String getString(Object component, String key, String defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : (String) value;
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	protected boolean setChoice(Object component, String key, String value, String[] values, String defaultvalue) {
		if (value == null) {
			return set(component, key, defaultvalue);
		}
		for (int i = 0; i < values.length; i++) {
			if (value.equals(values[i])) {
				return set(component, key, values[i]);
			}
		}
		throw new IllegalArgumentException("unknown " + value + " for " + key);
	}

	protected Image getIcon(Object component, String key, Image defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : (Image) value;
	}

	protected boolean setBoolean(Object component, String key, boolean value, boolean defaultvalue) {
		return set(component, key, (value == defaultvalue) ? null : (value ? Boolean.TRUE : Boolean.FALSE));
	}

	protected boolean getBoolean(Object component, String key, boolean defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : ((Boolean) value).booleanValue();
	}

	protected boolean setInteger(Object component, String key, int value, int defaultvalue) {
		return set(component, key, (value == defaultvalue) ? null : new Integer(value));
	}

	protected int getInteger(Object component, String key, int defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : ((Integer) value).intValue();
	}

	public void setRectangle(Object component, String key, int x, int y, int width, int height) {
		Rectangle rectangle = getRectangle(component, key);
		if (rectangle != null) {
			rectangle.x = x;
			rectangle.y = y;
			rectangle.width = width;
			rectangle.height = height;
		} else {
			set(component, key, new Rectangle(x, y, width, height));
		}
	}

	protected Rectangle getRectangle(Object component, String key) {
		return (Rectangle) get(component, key);
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Creates an image, and loads it immediately by default
	 *
	 * @param path
	 *            is relative to your thinlet instance or the classpath (if the
	 *            path starts with <i>'/'</i> character), or a full URL
	 * @return the loaded image or null
	 */
	public Image getIcon(String path) {
		return getIcon(path, true);
	}

	/**
	 * Creates an image from the specified resource. To speed up loading the
	 * same images use a cache (a simple hashtable). And flush the resources
	 * being used by an image when you won't use it henceforward
	 *
	 * @param path
	 *            is relative to your thinlet instance or the classpath, or an
	 *            URL
	 * @param preload
	 *            waits for the whole image if true, starts loading (and
	 *            repaints, and updates the layout) only when required (painted,
	 *            or size requested) if false
	 * @return the loaded image or null
	 */
	public Image getIcon(String path, boolean preload) {
		if ((path == null) || (path.length() == 0)) {
			return null;
		}

		return loader != null ? loader.getImage(path) : getLocalImage(path);
	}

	/**
	 * This method is called by the FrameLauncher if the window was closing, or
	 * AppletLauncher's destroy method. Overwrite it to e.g. save the
	 * application changes.
	 *
	 * @return true to exit, and false to keep the frame and continue the
	 *         application
	 */
	public boolean destroy() {
		return true;
	}

	protected static final Object[] dtd;
	static {
		Integer integer_1 = new Integer(-1);
		Integer integer0 = new Integer(0);
		Integer integer1 = new Integer(1);
		String[] orientation = { "horizontal", "vertical" };
		String[] leftcenterright = { "left", "center", "right" };
		String[] selections = { "single", "interval", "multiple" }; // +none
		dtd = new Object[] {
				"component",
				null,
				new Object[][] {
						{ "string", "name", "", null },
						{ "boolean", "enabled", "paint", Boolean.TRUE },
						{ "boolean", "visible", "parent", Boolean.TRUE },
						{ "boolean", "i18n", "validate", Boolean.FALSE },
						{ "boolean", "interactionState", "validate", Boolean.FALSE },
						// rcs: optionally don't paint the component body, see trough it,
						// save time, use it just as a container, label/multilabel etc.
						{ "boolean", "transparent", "validate", Boolean.FALSE },
						// rcs, skinlet: should this component text be parsed and painted as rich text ( styled )?
						// see drawStyledChars() comment in Skinlet.java
						{ "boolean", "styled", "validate", Boolean.FALSE }, { "string", "tooltip", "", null }, { "font", "font", "validate", null },
						{ "integer", "foreground", "paint", integer0 }, { "integer", "background", "paint", integer0 },
						{ "integer", "width", "validate", integer0 }, { "integer", "height", "validate", integer0 },
						{ "integer", "colspan", "validate", integer1 }, { "integer", "rowspan", "validate", integer1 },
						{ "integer", "weightx", "validate", integer0 }, { "integer", "weighty", "validate", integer0 },
						{ "choice", "halign", "validate", new String[] { "fill", "center", "left", "right" } },
						{ "choice", "valign", "validate", new String[] { "fill", "center", "top", "bottom" } },
						// component class String null*
						// parent Object null
						// (bounds) Rectangle 0 0 0 0
						{ "property", "property", "", null }, { "method", "init", "", null } },
				"label",
				"component",
				new Object[][] { { "string", "text", "validate", null }, { "icon", "icon", "validate", null },
						{ "icon", "hicon", "validate", null }, // rcs: hover icon
						{ "boolean", "i18n", "validate", Boolean.FALSE }, { "choice", "alignment", "validate", leftcenterright },
						{ "integer", "mnemonic", "paint", integer_1 }, { "component", "for", "", null } },
				"button",
				"label",
				new Object[][] { { "choice", "alignment", "validate", new String[] { "center", "left", "right" } }, { "method", "action", "", null },
						{ "choice", "type", "", new String[] { "normal", "default", "cancel", "link" } } },
				"checkbox",
				"label",
				new Object[][] { { "boolean", "selected", "paint", Boolean.FALSE }, //...group
						{ "string", "group", "paint", null }, //...group
						{ "method", "action", "", null } },
				"togglebutton",
				"checkbox",
				null,
				"combobox",
				"textfield",
				new Object[][] { { "icon", "icon", "validate", null }, { "integer", "selected", "layout", integer_1 } },
				"choice",
				null,
				new Object[][] { { "string", "name", "", null }, { "boolean", "enabled", "paint", Boolean.TRUE },
						{ "boolean", "i18n", "validate", Boolean.FALSE }, { "string", "text", "", null }, { "icon", "icon", "", null },
						{ "choice", "alignment", "", leftcenterright }, { "string", "tooltip", "", null }, { "font", "font", "validate", null },
						{ "integer", "foreground", "paint", integer0 }, { "integer", "background", "paint", integer0 }, { "property", "property", "", null },
						{ "boolean", "styled", "validate", Boolean.FALSE } },
				"textfield",
				"component",
				new Object[][] { { "string", "text", "layout", "" }, { "integer", "columns", "validate", integer0 },
						{ "boolean", "editable", "paint", Boolean.TRUE }, { "boolean", "i18n", "validate", Boolean.FALSE },
						{ "integer", "start", "layout", integer0 }, { "integer", "end", "layout", integer0 }, { "method", "action", "", null },
						{ "method", "insert", "", null }, { "method", "remove", "", null }, { "method", "caret", "", null }, { "method", "perform", "", null } },
				"passwordfield",
				"textfield",
				null,
				"textarea",
				"textfield",
				new Object[][] { { "integer", "rows", "validate", integer0 }, { "boolean", "border", "validate", Boolean.TRUE },
						{ "boolean", "wrap", "layout", Boolean.FALSE } },
				"tabbedpane",
				"component",
				new Object[][] { { "choice", "placement", "validate", new String[] { "top", "left", "bottom", "right", "stacked" } },
						{ "integer", "selected", "paint", integer0 }, { "method", "action", "", null } }, //...focus
				"tab",
				"choice",
				new Object[][] { { "integer", "mnemonic", "paint", integer_1 } },
				"panel",
				"component",
				new Object[][] { { "integer", "columns", "validate", integer0 }, { "integer", "top", "validate", integer0 },
						{ "integer", "left", "validate", integer0 }, { "integer", "bottom", "validate", integer0 },
						{ "integer", "right", "validate", integer0 }, { "integer", "gap", "validate", integer0 }, { "string", "text", "validate", null },
						{ "icon", "icon", "validate", null }, { "boolean", "i18n", "validate", Boolean.FALSE },
						{ "boolean", "border", "validate", Boolean.FALSE }, { "boolean", "scrollable", "validate", Boolean.FALSE } },
				"desktop",
				"component",
				null,
				"dialog",
				"panel",
				new Object[][] { { "string", "text", "", null }, { "icon", "icon", "", null }, { "method", "close", "", null }, // rcs: executes on dialog close
						{ "boolean", "modal", "", Boolean.FALSE } },
				"spinbox",
				"textfield",
				new Object[][] {
						{ "integer", "minimum", "paint", integer0 }, //...checkvalue
						{ "integer", "maximum", "paint", new Integer(100) }, { "integer", "step", "paint", integer1 },
						{ "integer", "value", "paint", integer0 } },
				"progressbar",
				"component",
				new Object[][] { { "choice", "orientation", "validate", orientation }, { "integer", "minimum", "paint", integer0 }, //...checkvalue
						{ "integer", "maximum", "paint", new Integer(100) }, { "integer", "value", "paint", integer0 } },
				// change stringpainted
				"slider",
				"progressbar",
				new Object[][] { { "integer", "unit", "", new Integer(5) }, { "integer", "block", "", new Integer(25) }, { "method", "action", "", null } },
				// minor/majortickspacing
				// inverted
				// labelincrement labelstart
				"splitpane",
				"component",
				new Object[][] { { "choice", "orientation", "validate", orientation }, { "integer", "divider", "layout", integer_1 } },
				"list",
				"component",
				new Object[][] { { "choice", "selection", "paint", selections }, { "method", "action", "", null }, { "method", "perform", "", null }, //...?
						{ "boolean", "line", "validate", Boolean.TRUE } },
				"item",
				"choice",
				new Object[][] { { "boolean", "selected", "", Boolean.FALSE } },
				"table",
				"list",
				new Object[][] {
				/*{ "choice", "selection",
					new String[] { "singlerow", "rowinterval", "multiplerow",
						"cell", "cellinterval",
						"singlecolumn", "columninterval", "multiplecolumn" } }*/},
				"header",
				null,
				null,
				// reordering allowed
				// autoresize mode: off next (column boundries) subsequents last all columns
				// column row selection
				// selection row column cell
				// editing row/column
				"column", "choice",
				new Object[][] { { "integer", "width", "", new Integer(80) }, { "choice", "sort", "", new String[] { "none", "ascent", "descent" } } }, "row",
				null, new Object[][] { { "boolean", "selected", "", Boolean.FALSE } }, "cell", "choice", null, "tree", "list",
				new Object[][] { { "method", "expand", "", null }, { "method", "collapse", "", null } }, "node", "choice",
				new Object[][] { { "boolean", "selected", "", Boolean.FALSE }, { "boolean", "expanded", "", Boolean.TRUE } }, "separator", "component", null,
				"menubar", "component", new Object[][] { { "choice", "placement", "validate", new String[] { "top", "bottom" } } }, "menu", "choice",
				new Object[][] { { "integer", "mnemonic", "paint", integer_1 } }, "menuitem", "choice",
				new Object[][] { { "keystroke", "accelerator", "", null }, { "method", "action", "", null }, { "integer", "mnemonic", "paint", integer_1 }
				//... KeyStroke=keyCode+modifiers(SHIFT CTRL META ALT_MASK)
				}, "checkboxmenuitem", "menuitem", new Object[][] { { "boolean", "selected", "paint", Boolean.FALSE }, //...group
						{ "string", "group", "paint", null } }, //...group
				"popupmenu", "component", null, // Post menu: Shift+F10
				"bean", "component", new Object[][] { { "bean", "bean", "", null } } };
	}

	public CanvasListener getCanvasListener() {
		return canvasListener;
	}

	public void setCanvasListener(CanvasListener canvasListener) {
		this.canvasListener = canvasListener;
	}

	public Rectangle getInverseShadow() {
		return inverseShadow;
	}

	public void setInverseShadow(Rectangle inverseShadow) {
		this.inverseShadow = inverseShadow;
	}

	public ImageLoader getLoader() {
		return loader;
	}

	public void setLoader(ImageLoader loader) {
		this.loader = loader;
	}

	protected void pointerPressed(int x, int y) {

		// evm (touchscreen) events: entered/moved/pressed -> dragged ->
		// dragged/released/exited

		MouseEvent event = new MouseEvent(MouseEvent.MOUSE_ENTERED, x, y, 0);

		processEvent(event);

		event = new MouseEvent(MouseEvent.MOUSE_MOVED, x, y, 0);

		processEvent(event);

		event = new MouseEvent(MouseEvent.MOUSE_PRESSED, x, y, 0);

		processEvent(event);

	}

	// evm (touchscreen) events: entered/moved/pressed -> dragged ->
	// dragged/released/exited

	protected void pointerDragged(int x, int y) {

		MouseEvent event = new MouseEvent(MouseEvent.MOUSE_ENTERED, x, y, 0);

		processEvent(event);

	}

	// evm (touchscreen) events: entered/moved/pressed -> dragged ->
	// dragged/released/exited

	protected void pointerReleased(int x, int y) {

		MouseEvent event = new MouseEvent(MouseEvent.MOUSE_DRAGGED, x, y, 0);

		processEvent(event);

		event = new MouseEvent(MouseEvent.MOUSE_RELEASED, x, y, 0);

		processEvent(event);

		event = new MouseEvent(MouseEvent.MOUSE_EXITED, x, y, 0);

		processEvent(event);

	}

}