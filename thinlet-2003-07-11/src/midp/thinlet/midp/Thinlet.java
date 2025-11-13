/*
	Thinlet GUI toolkit - www.thinlet.com
	Copyright (C) 2002 Robert Bajzat (robert.bajzat@thinlet.com)
	
	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.
	
	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package thinlet.midp;

import java.applet.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 */
public class Thinlet extends Container
	implements Runnable, Serializable {

	private transient Font font;
	private transient int c_bg;
	private transient int c_text;
	private transient int c_textbg;
	private transient int c_border;
	private transient int c_disable;
	private transient int c_hover;
	private transient int c_press;
	private transient int c_focus;
	private transient int c_select;
	private transient int c_ctrl;
	private transient int block;
	//java private transient Image hgradient, vgradient, hmodal;

        private static ResourceBundle langResource = null;
        private static ResourceBundle langResourceDefault = null;
        private transient boolean allI18n = false;
	private transient Thread timer;
	private transient long watchdelay;
	private transient long watch;
	private transient String clipboard;
	
	// enter the starting characters of a list item text within a short time to select
	private transient String findprefix = "";
	private transient long findtime;

	private Object content = createImpl("desktop");
	private transient Object mouseinside;
	private transient Object insidepart;
	private transient Object mousepressed;
	private transient Object pressedpart;
	private transient int referencex, referencey;
	private transient int mousex, mousey;
	private transient Object focusowner;
	private transient boolean focusinside;
	private transient Object popupowner;
	private transient Object tooltipowner;
	//private transient int pressedkey;
	
	private static final int DRAG_ENTERED = AWTEvent.RESERVED_ID_MAX + 1;
	private static final int DRAG_EXITED = AWTEvent.RESERVED_ID_MAX + 2;
	
	private static long WHEEL_MASK = 0;
	private static int MOUSE_WHEEL = 0;
	private static Method wheelrotation = null;
	private static int evm = 0;
	static {
		try {
			WHEEL_MASK = AWTEvent.class.getField("MOUSE_WHEEL_EVENT_MASK").getLong(null);
			MOUSE_WHEEL = MouseEvent.class.getField("MOUSE_WHEEL").getInt(null);
		} catch (Exception exc) { /* not 1.4 */ }
	}
	{
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		//setFont((Font) getToolkit().getDesktopProperty("win.messagebox.font"));
		setColors(0xe6e6e6, 0x000000, 0xffffff,
			0x909090, 0xb0b0b0, 0xededed, 0xb9b9b9, 0x89899a, 0xc5c5dd);
			
		// disable global focus-manager for this component in 1.4
		if (MOUSE_WHEEL != 0) {
			try {
				getClass().getMethod("setFocusTraversalKeysEnabled", new Class[] { Boolean.TYPE }).
					invoke(this, new Object[] { FALSE });
			} catch (Exception exc) { /* never */ }
		}
		// set listeners flags
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK |
			AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
			AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | WHEEL_MASK);
			// EVM has larger fillRect, fillOval, and drawImage(part), others are correct
			// contributed by Ibsen Ramos-Bonilla
			try {
				if (System.getProperty("java.vendor").indexOf("Insignia") != -1) { evm = -1; }
			} catch (Exception exc) { /* never */ }
	}
	
	// button and togglebutton border + padding
	private static final Insets BUTTON = new Insets(3, 6, 3, 6);
	// tabbedpane (not selected) tab padding are 1, 3, 1, and 3 pt
	private static final Insets ITEM = new Insets(1, 3, 1, 3); // list/tree/table item padding

	/**
	 * Sets the 9 colors used for components, and repaints the whole UI
	 *
	 * @param background the backround of panels (dialogs, desktops),
	 * and disabled controls, not editable texts, lines between list items
	 * (the default value if <i>#e6e6e6</i>)
	 * @param text for text, arrow foreground (<i>black</i> by default)
	 * @param textbackground the background of text components, and lists
	 * (<i>white</i> by default)
	 * @param border for outer in inner borders of enabled components
	 * (<i>#909090</i> by default)
	 * @param disable for text, border, arrow color in disabled components
	 * (<i>#b0b0b0</i> by default)
	 * @param hover indicates that the mouse is inside a button area
	 * (<i>#ededed</i> by default)
	 * @param press for pressed buttons,
	 * gradient image is calculated using the background and this press color
	 * (<i>#b9b9b9</i> by default)
	 * @param focus for text caret and rectagle color marking the focus owner
	 * (<i>#89899a</i> by default)
	 * @param select used as the background of selected text, and list items,
	 * and in slider (<i>#c5c5dd</i> by default)
	 */
	public void setColors(int background, int text, int textbackground,
			int border, int disable, int hover, int press,
			int focus, int select) {
		c_bg = background; c_text = text;
		c_textbg = textbackground; c_border = border;
		c_disable = disable; c_hover = hover;
		c_press = press; c_focus = focus;
		c_select = select;
		hgradient = vgradient = hmodal = null;
		repaint();
	}
	
	//setDesktopProperty+

	/**
	 * Sets the only one font used everywhere, and revalidates the whole UI.
	 * Scrollbar width/height, spinbox, and combobox button width,
	 * and slider size is the same as the font height
	 *
	 * @param font the default font is <i>SansSerif</i>, <i>plain</i>, and <i>12pt</i>
	 */
	public void setFont(Font font) {
		block = getFontMetrics(font).getHeight();
		super.setFont(font);
		this.font = font;
		hgradient = vgradient = hmodal = null;
		if (content != null) validate(content);
	}
	
	/**
	 * Set custom font on a component
	 *
	 * @param component component to use the custom font
	 * @param font custom font to use, or null to reset component to use default font
	 */
        public void setFont(Object component, Font font) {
            if (component != null) set(component, "font", font);
        }
    
	/**
	 * Set custom color on a component.
         * <p>Notes: For "foreground" key, this sets the text color.
         * For "background" key, on gradient-filled
         * components (such as tabs, buttons etc) this will result in a 
         * component filled with solid background color, and not a new gradient.
         * Also, Color.brighter() will be used for highlight, and Color.darker()
         * will be used for pressed or not selected.
	 *
	 * @param component component to use for custom color
         * @param key currently "background" and "foreground" are supported
	 * @param color custom color to use, or null to reset component to use default color
	 */
        public void setColor(Object component, String key, int color) {
            if (component != null) set(component, key, color);
        }
        
	/**
         * Set current language resource bundle. This flushes all cached translated values, performs
         * lazy loading of new values, and repaints the desktop. This implementation allows applications to switch
         * language resources on the fly, without rebuilding/reloading components that use them.
         * 
         * <p>The pseudo-code is as follows:
         * <ul>
         *     <li>if langResource && langResourceDefault are null, don't
         *       translate anything, no matter what other settings are. This
         *       behaviour provides compatibility with previous versions.</li>
         *     <li>if only langResourceDefault is set, use this when translation is required</li>
         *     <li>if allI18n is set to true:
         *         <ul>
         *         <li>if property "i18n" on a component is missing,
         *           or set to "true", translate</li>
         *         <li>if property "i18n" is present, and set to "false",
         *           do not translate</li>
         *         </ul></li>
         *     <li>if allI18n is set to false:
         *         <ul>
         *         <li>if property "i18n" on a component is missing,
         *           or set to "false", do not translate</li>
         *         <li>if property "i18n" is present, and set to "true",
         *           translate</li>
         *         </ul></li>
         *   </ul>
         *     <p>The "translate" step is applied only to values from "text"
         *     and "tooltip" properties (for now), and is applied as follows:
         *     <ul>
         *     <li>use the value of "text" or "tooltip" as a lookup key</li>
         *     <li>use langResource to lookup the result value
         *       <ul>
         *       <li>if no value is found, use langResourceDefault for lookup
         *         <ul>
         *         <li>if no value is found, just return the original value of
         *           the property. Set a flag on component that prevents
         *           lookups in the future. This flag is cleared when langResource is changed.</li>
         *         </ul></li>
         *       </ul></li>
         *     <li>cache the result value, if any</li>
         *     </ul>
         *     <p>If translated value is found successfully, it is cached in the
         *     component. This cache is gradually flushed when setLangResource
         *     is called. Cached value is also flushed when setString() is
         *     called on a component.
         * 
	 * @param res resource bundle containing localized texts for "text" and "tooltip"
	 */
        public void setLangResource(ResourceBundle res) {
            langResource = res;
            doLayout(content);
            repaint(content);
        }
        
	/**
         * Returns language resource bundle currently in use, or default bundle, or null.
	 */
        public static ResourceBundle getLangResource() {
            return langResource;
        }
        
	/**
         * Set default language resource bundle. Resources from this bundle will be used if
         * they are missing in the current bundle.
         *
	 * @param res resource bundle containing default localized texts for "text" and "tooltip"
	 */
        public void setLangResourceDefault(ResourceBundle res) {
            langResourceDefault = res;
            if (langResource == null) setLangResource(res);
        }
        
	/**
         * Returns default language resource bundle, or null.
	 */
        public static ResourceBundle getLangResourceDefault() {
            return langResourceDefault;
        }
        
	/**
	 * Sets the default behaviour of internationalization code. If set to "true", try to translate
         * all components' "text" and "tooltip" values, unless explicitly prohibited by setting
         * <code>i18n="false"</code> on a specific component. If set to "false", do not translate
         * unless explicitly requested by setting <code>i18n="true"</code> on a specific component.
         *<p>Default value is "false", to provide backwards compatibility.
         *
         *@param val if "true", translate by default; if "false", do not translate by default.
	 */
        public void setAllI18n(boolean val) {
            allI18n = val;
        }
        
	/**
	 *
	 */
	private void doLayout(Object component) {
		String classname = getClass(component);
		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Image icon = getIcon(component, "icon", null);
				layoutField(component, block, false,
					(icon != null) ? icon.getWidth() : 0);
			} // set editable -> validate (overwrite textfield repaint)
			else {
				int selected = getInteger(component, "selected", -1);
				if (selected != -1) { //...
					Object choice = getItem(component, selected);
					set(component, "text", get(choice, "text"));
					set(component, "icon", get(choice, "icon"));
				}
			}
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			layoutField(component, 0, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			String text = getString(component, "text", "");
			int start = getInteger(component, "start", 0);
			if (start > text.length()) { setInteger(component, "start", start = text.length(), 0); }
			int end = getInteger(component, "end", 0);
			if (end > text.length()) { setInteger(component, "end", end = text.length(), 0); }
			
			boolean wrap = getBoolean(component, "wrap", false);
			char[] chars = null;
			if (wrap) {
				int[] bounds = getRectangle(component, "bounds");
				chars = getChars(component, text, true, bounds[0] - 4, bounds[1]);
				if (chars == null) { // need scrollbars
					chars = getChars(component, text, true, bounds[0] - block - 4, 0);
				}
			}
			else {
				chars = getChars(component, text, false, 0, 0);
			}
			
			Font currentfont = (Font) get(component, "font");
			FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
			int width = 0, height = 0;
			int caretx = 0; int carety = 0;
			for (int i = 0, j = 0; j <= chars.length; j++) {
				if ((j == chars.length) || (chars[j] == '\n')) {
					width = Math.max(width, font.charsWidth(chars, i, j - i));
					if ((end >= i) && (end <= j)) {
						caretx = font.charsWidth(chars, i, end - i);
						carety = height;
					}
					height += font.getHeight();
					i = j + 1;
				}
			}
			layoutScroll(component, width + 2, height + 2, 0, 0, 0, 0, true, 0);
			scrollToVisible(component, caretx, carety, 2, font.getHeight() + 2); //?
		} 
		else if ("tabbedpane" == classname) {
			// tabbedpane (not selected) tab padding are 1, 3, 1, and 3 pt
			int[] bounds = getRectangle(component, "bounds");
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
                        boolean stacked = (placement == "stacked");
			int selected = getInteger(component, "selected", 0);
                        int cnt = getItemCountImpl(component, ":comp");
			// draw up tabs in row/column
			int tabd = 0; int[] first = null; // x/y location of tab left/top
			int tabsize = 0; // max height/width of tabs
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
                                int[] d;
                                if (stacked) {
                                        d = getSize(tab, 9, 8);
                                } else d = getSize(tab, horizontal ? 12 : 9, horizontal ? 5 : 8);
				if ((tabd == 0) && ((first = getRectangle(tab, "bounds")) != null)) {
					tabd = horizontal && !stacked? first[2] : first[3]; // restore previous offset
				}
				setRectangle(tab, "bounds",
					horizontal? tabd : 0, horizontal? 0 : tabd, d[0], d[1]);
				tabd += (horizontal ? d[0] : d[1]) - 3;
				tabsize = Math.max(tabsize, (horizontal || stacked)? d[1] : d[0]);
			}
			// match tab height/width, set tab content size
			int cx = (placement == "left") ? (tabsize + 1) : stacked ? 2 : 2;
			int cy = (placement == "top")? (tabsize + 1) : stacked ? (tabsize + 2) : 2;
			int cwidth = bounds[0] - ((horizontal || stacked)? 4 : (tabsize + 3));
			int cheight = bounds[1] - (horizontal ? (tabsize + 3) : stacked? (tabsize + 2)* cnt: 4);
                        if (cheight < 0) cheight = 0;
                        tabd = tabsize;
                        int i = 0;
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				int[] r = getRectangle(tab, "bounds");
                                if (r == null) {
                                    r = new Rectangle();
                                    set(tab, "bounds", r);
                                }
				if (horizontal || stacked) {
					if (placement == "bottom") { r[3] = bounds[1] - tabsize; }
					else if (stacked) {
                                            r[3] = tabd; r[2] = 2;
                                            //r[0] = cwidth - 4;
                                            tabd += tabsize;
                                            if (i == selected) tabd += cheight + 3;
                                            cy += tabsize + 2;
                                            i++;
                                        }
					r[1] = tabsize;
				} else {
					if (placement == "right") { r[2] = bounds[0] - tabsize; }
					r[0] = tabsize;
				}
				
				Object comp = get(tab, ":comp"); // relative to the tab location
				if ( (comp != null) && getBoolean(comp, "visible", true)) {
					setRectangle(comp, "bounds", cx - r[2], cy - r[3], cwidth, cheight);
					doLayout(comp);
				}
			}
			checkOffset(component);
		}
		else if (("panel" == classname) || (classname == "dialog")) {
			int gap = getInteger(component, "gap", 0);
			int[][] grid = getGrid(component, gap);
			int top = 0; int left = 0;
			int contentwidth = 0; int contentheight = 0;
			if (grid != null) { // has subcomponents
				top = getInteger(component, "top", 0);
				left = getInteger(component, "left", 0);
				int bottom = getInteger(component, "bottom", 0);
				int right = getInteger(component, "right", 0);
				// sums the preferred size of cell widths and heights, gaps
				contentwidth = left + getSum(grid[0], 0, grid[0].length, gap, false) + right;
				contentheight = top + getSum(grid[1], 0, grid[1].length, gap, false) + bottom;
			}
			
			int[] title = getSize(component, 0, 0); // title text and icon
			setInteger(component, ":titleheight", title[1], 0);
			boolean scrollable = getBoolean(component, "scrollable", false);
			boolean border = ("panel" == classname) && getBoolean(component, "border", false);
			int iborder = (border ? 1 : 0);
			if (scrollable) { // set scrollpane areas
				if ("panel" == classname) {
					int head = title[1] / 2;
					int headgap = (title[1] > 0) ? (title[1] - head - iborder) : 0;
					scrollable = layoutScroll(component, contentwidth, contentheight,
						head, 0, 0, 0, border, headgap);
				}
				else { // dialog
					scrollable = layoutScroll(component, contentwidth, contentheight,
						3 + title[1], 3, 3, 3, true, 0);
				}
			}
			if (!scrollable) { // clear scrollpane bounds //+
				set(component, ":view", null); set(component, ":port", null);
			}
			
			if (grid != null) {
				int areax = 0; int areay = 0; int areawidth = 0; int areaheight = 0;
				if (scrollable) {
					// components are relative to the viewport
					int[] view = getRectangle(component, ":view");
					areawidth = view[0]; areaheight = view[1];
				}
				else { // scrollpane isn't required
					// components are relative to top/left corner
					int[] bounds = getRectangle(component, "bounds");
					areawidth = bounds[0]; areaheight = bounds[1];
					if ("panel" == classname) {
						areax = iborder; areay = Math.max(iborder, title[1]);
						areawidth -= 2 * iborder; areaheight -= areay + iborder;
					}
					else { // dialog
						areax = 4; areay = 4 + title[1];
						areawidth -= 8; areaheight -= areay + 4;
					}
				}
			
				for (int i = 0; i < 2; i++) { // i=0: horizontal, i=1: vertical
					// remaining space
					int d = ((i == 0) ? (areawidth - contentwidth) : (areaheight - contentheight));
					if (d != 0) { //+ > 0
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
					if (!getBoolean(comp, "visible", true)) { continue; }
					int ix = areax + left + getSum(grid[0], 0, grid[4][i], gap, true);
					int iy = areay + top + getSum(grid[1], 0, grid[5][i], gap, true);
					int iwidth = getSum(grid[0], grid[4][i], grid[6][i], gap, false);
					int iheight = getSum(grid[1], grid[5][i], grid[7][i], gap, false);
					String halign = getString(comp, "halign", "fill");
					String valign = getString(comp, "valign", "fill");
					if ((halign != "fill") || (valign != "fill")) {
						int[] d = getPreferredSize(comp);
						if (halign != "fill") {
							int dw = Math.max(0, iwidth - d[0]);
							if (halign == "center") { ix += dw / 2; }
								else if (halign == "right") { ix += dw; }
							iwidth -= dw;
						}
						if (valign != "fill") {
							int dh = Math.max(0, iheight - d[1]);
							if (valign == "center") { iy += dh / 2; }
								else if (valign == "bottom") { iy += dh; }
							iheight -= dh;
						}
					}
					setRectangle(comp, "bounds", ix, iy, iwidth, iheight);
					doLayout(comp);
					i++;
				}
			}
		}
		else if ("desktop" == classname) {
			int[] bounds = getRectangle(component, "bounds");
			for (Object comp = get(component, ":comp");
					comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if (iclass == "dialog") {
					int[] d = getPreferredSize(comp);
					if (get(comp, "bounds") == null)
					setRectangle(comp, "bounds",
						Math.max(0, (bounds[0] - d[0]) / 2),
						Math.max(0, (bounds[1] - d[1]) / 2),
						Math.min(d[0], bounds[0]), Math.min(d[1], bounds[1]));
				} else if ((iclass == ":combolist") || (iclass == ":popup")) {
						iclass = iclass; //compiler bug
				} else {
					setRectangle(comp, "bounds", 0, 0, bounds[0], bounds[1]);
				}
				doLayout(comp);
			}
		}
		else if ("spinbox" == classname) {
			layoutField(component, block, false, 0);
		}
		else if ("splitpane" == classname) {
			int[] bounds = getRectangle(component, "bounds");
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			int maxdiv = Math.max(0, (horizontal ? bounds[0] : bounds[1]) - 5);

			Object comp1 = get(component, ":comp");
			boolean visible1 = (comp1 != null) && getBoolean(comp1, "visible", true);
			if (divider == -1) {
				int d1 = 0;
				if (visible1) {
					int[] d = getPreferredSize(comp1);
					d1 = horizontal ? d[0] : d[1];
				}
				divider = Math.min(d1, maxdiv);
				setInteger(component, "divider", divider, -1);
			}
			else if (divider > maxdiv) {
				setInteger(component, "divider", divider = maxdiv, -1);
			}

			if (visible1) {
				setRectangle(comp1, "bounds", 0, 0, horizontal ? divider : bounds[0],
					horizontal ? bounds[1] : divider);
				doLayout(comp1);
			}
			Object comp2 = (comp1 != null) ? get(comp1, ":next") : null;
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				setRectangle(comp2, "bounds", horizontal ? (divider + 5) : 0,
					horizontal ? 0 : (divider + 5),
					horizontal ? (bounds[0] - 5 - divider) : bounds[0],
					horizontal ? bounds[1] : (bounds[1] - 5 - divider));
				doLayout(comp2);
			}
		} 
		else if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
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
						if (i != 0) { column = get(column, ":next"); }
						columnwidths[i] = getInteger(column, "width", 80);
						width += columnwidths[i];
						int[] d = getSize(column, 2, 2);
						columnheight = Math.max(columnheight, d[1]);
					}
				}
				set(component, ":widths", columnwidths);
			}
			int y = 0;
			int level = 0;
			for (Object item = get(component, ":comp"); item != null;) {
				int x = 0;
				int iwidth = 0; int iheight = 0;
				if ("table" == classname) {
					iwidth = width;
					for (Object cell = get(item, ":comp"); cell != null; cell = get(cell, ":next")) {
						int[] d = getSize(cell, 2, 2);
						iheight = Math.max(iheight, d[1]);
					}
				}
				else {
					if ("tree" == classname) {
						x = (level + 1) * block;
					}
					int[] d = getSize(item, ITEM.left + ITEM.right, ITEM.top + ITEM.bottom);
					iwidth = d[0]; iheight = d[1];
					width = Math.max(width, x + d[0]);
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
		}
		else if ("menubar" == classname) { 
			int[] bounds = getRectangle(component, "bounds");
			int x = 0;
			for (Object menu = get(component, ":comp");
					menu != null; menu = get(menu, ":next")) {
				int[] d = getSize(menu, 8, 4);
				setRectangle(menu, "bounds", x, 0, d[0], bounds[1]);
				x += d[0];
			}
		}
		else if ("bean" == classname) {
			int[] r = getRectangle(component, "bounds");
			((Component) get(component, "bean")).setBounds(r);
		}
	}
	
	/**
	 * Scroll tabs to make the selected one visible
	 * @param component a tabbedpane
	 */
	private void checkOffset(Object component) {
		String placement = getString(component, "placement", "top");
		boolean horizontal = ((placement == "top") || (placement == "bottom"));
                boolean stacked = (placement == "stacked");
		int selected = getInteger(component, "selected", 0);
		int[] bounds = getRectangle(component, "bounds");
		int panesize = horizontal ? bounds[0] : bounds[1];
		int i = 0; int first = 0; int last = 0; int d = 0;
		for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
			int[] r = getRectangle(tab, "bounds");
			if (i == 0) { first = (horizontal ? r[2] : r[3]); }
			last = (horizontal ? (r[2] + r[0]) : (r[3] + r[1]));
			if (i == selected) {
				int ifrom = (horizontal ? r[2] : r[3]) - 6;
				int ito = (horizontal ? (r[2] + r[0]) : (r[3] + r[1])) + 6;
				if (ifrom < 0) { d = -ifrom; }
				else if (ito > panesize) { d = panesize - ito; }
			}
			i++;
		}
		d = Math.min(-first, Math.max(d, panesize - last));
		if (d != 0) {
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				int[] r = getRectangle(tab, "bounds");
				if (horizontal) { r[2] += d; } else { r[3] += d; }
				Object comp = get(tab, ":comp"); // relative to the tab location
				if ( (comp != null) && getBoolean(comp, "visible", true)) {
					int[] rc = getRectangle(comp, "bounds");
					if (horizontal) { rc[2] -= d; } else { rc[3] -= d; }
				}
			}
			if (mouseinside == component) { // layout changed, check the hovered tab
				checkLocation();
			}
		}
	}
	
	/**
	 *
	 */
	private char[] getChars(Object component,
			String text, boolean wrap, int width, int height) {
		char[] chars = (char[]) get(component, ":text");
		if ((chars == null) || (chars.length != text.length())) {
			chars = text.toCharArray();
			set(component, ":text", chars);
		}
		else text.getChars(0, chars.length, chars, 0);
		
		if (wrap) {
			Font currentfont = (Font) get(component, "font");
			FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
			int lines = (height - 4) / font.getHeight();
			boolean prevletter = false; int n = chars.length; int linecount = 0;
			for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last space index (before k)
				if (((k == n) || (chars[k] == '\n') || (chars[k] == ' ')) &&
						(j  > i) && (font.charsWidth(chars, i, k - i) > width)) {
					chars[j] = '\n';
					k--; // draw line to the begin of the current word (+ spaces) if it is out of width
				}
				else if ((k == n) || (chars[k] == '\n')) { // draw line to the text/line end
					j = k; prevletter = false;
				}
				else {
					if ((chars[k] == ' ') && (prevletter || (j > i))) { j = k; } // keep spaces starting the line
					prevletter = (chars[k] != ' ');
					continue;
				}
				linecount++;
				if ((lines != 0) && (linecount == lines)) { return null; }
				i = j + 1;
			}
		}
		return chars;
	}
	
	/**
	 *
	 */
	/*private boolean wrap(char[] chars, int width, int lines) {
		
		boolean prevletter = false; int n = chars.length; int linecount = 0;
		for (int i = 0, j = -1, k = 0; k <= n; k++) { // j is the last space index (before k)
			if (((k == n) || (chars[k] == '\n') || (chars[k] == ' ')) &&
					(j  > i) && (font.charsWidth(chars, i, k - i) > width)) {
				chars[j] = '\t';
				k--; // draw line to the begin of the current word (+ spaces) if it is out of width
			}
			else if ((k == n) || (chars[k] == '\n')) { // draw line to the text/line end
				j = k; prevletter = false;
			}
			else {
				if (chars[k] == '\t') { chars[k] = ' '; }
				if ((chars[k] == ' ') && (prevletter || (j > i))) { j = k; } // keep spaces starting the line
				prevletter = (chars[k] != ' ');
				continue;
			}
			linecount++;
			if ((lines != 0) && (linecount == lines)) { return false; }
			i = j + 1;
		}
		return true;
	}*/
	
	/**
	 * Pop up the list of choices for the given combobox
	 * @param combobox
	 * @return the created combolist
	 */
	private Object popupCombo(Object combobox) {
		// combobox bounds relative to the root desktop
		int combox = 0, comboy = 0, combowidth = 0, comboheight = 0;
		for (Object comp = combobox; comp != content; comp = getParent(comp)) {
			int[] r = getRectangle(comp, "bounds");
			combox += r[2]; comboy += r[3];
			if (comp == combobox) { combowidth = r[0]; comboheight = r[1]; }
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
		int pw = 0; int ph = 0;
		for (Object item = get(combobox, ":comp");
				item != null; item = get(item, ":next")) {
			int[] d = getSize(item, 8 , 4);
			setRectangle(item, "bounds", 0, ph, d[0], d[1]);
			pw = Math.max(pw, d[0]);
			ph += d[1];
		}
		// set :combolist bounds
		int listy = 0, listheight = 0;
		int bellow = getRectangle(content, "bounds")[1] - comboy - comboheight - 1;
		if ((ph + 2 > bellow) && (comboy - 1 > bellow)) { // popup above combobox
			listy = Math.max(0, comboy - 1 - ph - 2);
			listheight = Math.min(comboy - 1, ph + 2);
		}
		else { // popup bellow combobox
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
	 * @param component menubar or :popup
	 * @return the created popupmenu
	 */
	private Object popupMenu(Object component) {
		Object popup = get(component, ":popup"); // first :popup child
		Object selected = get(component, "selected"); // selected menu in of the component
		if (popup != null) { // remove its current :popup
			if (get(popup, "menu") == selected) { return null; } // but the currect one
			set(popup, "selected", null);
			set(popup, "menu", null);
			repaint(popup);
			removeItemImpl(content, popup);
			set(popup, ":parent", null);
			set(component, ":popup", null);
			if (mouseinside == popup) {
				checkLocation();
			}
			popupMenu(popup); // remove recursively
		}
		// pop up the selected menu only 
		if ((selected == null) || (getClass(selected) != "menu")) { return null; }
		// create the :popup, :popup.menu -> menu,
		// menubar|:popup.:popup -> :popup, menubar|:popup.selected -> menu
		popup = createImpl(":popup");
		set(popup, "menu", selected);
		set(component, ":popup", popup);
		insertItem(content, ":comp", popup, 0);
		set(popup, ":parent", content);
		// layout menuitems and calculate :popup content size
		int pw = 0; int ph = 0;
		for (Object item = get(selected, ":comp"); item != null; item = get(item, ":next")) {
			String itemclass = getClass(item);
			int[] d = (itemclass == "separator") ? new int[] { 1, 1 } :
				getSize(item, 8 , 4);
			if (itemclass == "checkboxmenuitem") {
				d[0] = d[0] + block + 3;
				d[1] = Math.max(block, d[1]);
			}
			else if (itemclass == "menu") {
				d[0] += block;
			}
			
			// add accelerator width
			String accelerator = getAccelerator(item);
			if (accelerator != null) {
				d[0] += 4 + getFontMetrics(font).stringWidth(accelerator); //+ font, height and gap
			}
			
			setRectangle(item, "bounds", 1, 1 + ph, d[0], d[1]);
			pw = Math.max(pw, d[0]);
			ph += d[1];
		}
		// calculates the bounds of the previous menubar/:popup relative to the root desktop
		int menux = 0, menuy = 0, menuwidth = 0, menuheight = 0;
		for (Object comp = component; comp != content; comp = getParent(comp)) {
			int[] r = getRectangle(comp, "bounds");
			menux += r[2]; menuy += r[3];
			if (comp == component) { menuwidth = r[0]; menuheight = r[1]; }
		}
		// set :popup bounds
		String classname = getClass(component);
		int[] menubounds = getRectangle(selected, "bounds");
                if ("menubar" == classname) { // below or above the menubar
			boolean below = getString(component, "placement", "bottom") == "bottom";
			popupowner = component;
			if (below) {
				setRectangle(popup, "bounds",
					menux + menubounds[2], menuy + menuheight - 1, pw + 2, ph + 2);
			} else {
				setRectangle(popup, "bounds",
					menux + menubounds[2], menuy - ph - 1, pw + 2, ph + 2);
			}
		} else { // right to the previous :popup
			setRectangle(popup, "bounds",
				menux + menuwidth - 3, menuy + menubounds[3], pw + 2, ph + 2);
		}
		repaint(popup);
		return popup;
	}
	
	/**
	 * @param component a menuitem
	 * @return key modifier strings and key text
	 */
	private String getAccelerator(Object component) {
		Object accelerator = get(component, "accelerator");
		if (accelerator != null) {
			long keystroke = ((Long) accelerator).longValue();
			int keycode = (int) (keystroke >> 32);
			int modifiers = (int) (keystroke & 0xffff);
			return KeyEvent.getKeyModifiersText(keycode) + " " +
				KeyEvent.getKeyText(modifiers);
		}
		return null;
	}
	
	/*
	
	/**
	 * @param popupmenu
	 */
	private void popupPopup(Object popupmenu, int x, int y) {
		// :popup.menu -> popupmenu, popupmenu.:popup -> :popup
		Object popup = createImpl(":popup");
		set(popup, "menu", popupmenu);
		set(popupmenu, ":popup", popup);
		// add :popup to the root desktop and set the combobox as popupowner
		popupowner = popupmenu;
		insertItem(content, ":comp", popup, 0);
		set(popup, ":parent", content);
		// lay out
		int pw = 0; int ph = 0;
		for (Object item = get(popupmenu, ":comp"); item != null; item = get(item, ":next")) {
			String itemclass = getClass(item);
			int[] d = (itemclass == "separator") ? new int[] { 1, 1 } :
				getSize(item, 8 , 4);
			if (itemclass == "checkboxmenuitem") {
				d[0] = d[0] + block + 3;
				d[1] = Math.max(block, d[1]);
			}
			else if (itemclass == "menu") {
				d[0] += block;
			}
			setRectangle(item, "bounds", 1, 1 + ph, d[0], d[1]);
			pw = Math.max(pw, d[0]);
			ph += d[1];
		}
		// if popup would fall outside desktop bounds, shift it
		int[] bounds = getRectangle(content, "bounds");
		if (x + pw + 2 > bounds[2] + bounds[0]) x -= (pw + 2);
		if (y + ph + 2 > bounds[3] + bounds[1]) y -= (ph + 2);
		// set :popup bounds
		setRectangle(popup, "bounds", x, y, pw + 2, ph + 2);
		repaint(popup);
	}

	/**
	 * * @param item //FIX can be scrollbar string
	 */
	private void closeCombo(Object combobox, Object combolist, Object item) {
		if ((item != null) && getBoolean(item, "enabled", true)) {
			String text = getString(item, "text", "");
			set(combobox, "text", text); // if editable
                        putProperty(combobox, "i18n.text", null);
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
		if (mouseinside == combolist) {
			checkLocation();
		}
	}

	/**
	 *
	 */
	private void closeup() {
		if (popupowner != null) {
			String classname = getClass(popupowner);
			if ("menubar" == classname) {
				set(popupowner, "selected", null);
				popupMenu(popupowner);
				repaint(popupowner); // , selected
			}
			else if ("combobox" == classname) {
				closeCombo(popupowner, get(popupowner, ":combolist"), null);
			}
			else { // "popupmenu"
				popupMenu(popupowner);
			}
			popupowner = null;
		}
	}

	/**
	 *
	 */
	private void showTip() {
		String text = null;
		tooltipowner = null;
		String classname = getClass(mouseinside);
		if ((classname == "tabbedpane") || (classname == "menubar") || (classname == ":popup")) {
			if (insidepart != null) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		else if (classname == ":combolist") {
			if (insidepart instanceof Object[]) {
				text = getString(insidepart, "tooltip", null);
			}
		}
		//list table tree
		if (text == null) { text = getString(mouseinside, "tooltip", null); }
			else { tooltipowner = insidepart; }
		if (text != null) {
			
			int width = font.stringWidth(text) + 4;
			int height = font.getHeight() + 4;
			if (tooltipowner == null) { tooltipowner = mouseinside; }
			int[] bounds = getRectangle(content, "bounds");
			int tx = Math.max(0, Math.min(mousex + 10, bounds[0] - width));
			int ty = Math.max(0, Math.min(mousey + 10, bounds[1] - height));
			setRectangle(tooltipowner, ":tooltipbounds", tx, ty, width, height);
			repaint(tx, ty, width, height);
		}
	}

	/**
	 *
	 */
	private void hideTip() {
		if (tooltipowner != null) {
			int[] bounds = getRectangle(tooltipowner, ":tooltipbounds");
			set(tooltipowner, ":tooltipbounds", null);
			tooltipowner = null;
			repaint(bounds[2], bounds[3], bounds[0], bounds[1]);
		}
	}

	/**
	 *
	 */
	private void layoutField(Object component, int dw, boolean hidden, int left) {
		int width = getRectangle(component, "bounds")[0] - left -dw;
		String text = getString(component, "text", "");
		int start = getInteger(component, "start", 0);
		if (start > text.length()) { setInteger(component, "start", start = text.length(), 0); }
		int end = getInteger(component, "end", 0);
		if (end > text.length()) { setInteger(component, "end", end = text.length(), 0); }
		int offset = getInteger(component, ":offset", 0);
		int off = offset;
		Font currentfont = (Font) get(component, "font");
		FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
		int caret = hidden ? (font.charWidth('*') * end) :
			font.stringWidth(text.substring(0, end));
		if (off > caret) {
			off = caret;
		}
		else if (off < caret - width + 4) {
			off = caret - width + 4;
		}
		off = Math.max(0, Math.min(off, (hidden ? (font.charWidth('*') *
			text.length()) : font.stringWidth(text)) - width + 4)); 
		if (off != offset) {
			setInteger(component, ":offset", off, 0);
		}
	}
	
	/**
	 * Set viewport (:port) bounds excluding borders, view position and content
	 * size (:view), horizontal (:horizontal), and vertical (:vertical) scrollbar
	 * bounds
	 *
	 * @param component scrollable widget
	 * @param contentwidth preferred component width
	 * @param contentheight preferred component height
	 * @param top top inset (e.g. table header, dialog title, half of panel title)
	 * @param left left inset (e.g. dialog border)
	 * @param bottom bottom inset (e.g. dialog border)
	 * @param right right inset (e.g. dialog border)
	 * @param topgap (lower half of panel title)
	 * @return true if scrollpane is required, otherwise false
	 *
	 * list: 0, 0, 0, 0, true, 0 | table: header, ... | dialog: header, 3, 3, 3, true, 0
	 * title-border panel: header / 2, 0, 0, 0, true, head
	 */
	private boolean layoutScroll(Object component,
			int contentwidth, int contentheight,
			int top, int left, int bottom, int right, boolean border, int topgap) {
		int[] bounds = getRectangle(component, "bounds");
		int iborder = border ? 1 : 0; int iscroll = block + 1 - iborder;
		int portwidth = bounds[0] - left - right - 2 * iborder; // available horizontal space
		int portheight = bounds[1] - top - topgap - bottom - 2 * iborder; // vertical space
		boolean hneed = contentwidth > portwidth; // horizontal scrollbar required
		boolean vneed = contentheight > portheight - (hneed ? iscroll : 0); // vertical scrollbar needed
		if (vneed) { portwidth -= iscroll; } // subtract by vertical scrollbar width
		hneed = hneed || (vneed && (contentwidth > portwidth));
		if (hneed) { portheight -= iscroll; } // subtract by horizontal scrollbar height
		
		setRectangle(component, ":port", left + iborder, top + iborder + topgap, portwidth, portheight);
		if (hneed) { 
			setRectangle(component, ":horizontal", left, bounds[1] - bottom - block - 1,
				bounds[0] - left - right - (vneed ? block : 0), block + 1);
		} else { set(component, ":horizontal", null); }
		if (vneed) {
			setRectangle(component, ":vertical", bounds[0] - right - block - 1, top,
				block + 1, bounds[1] - top - bottom - (hneed ? block : 0));
		} else { set(component, ":vertical", null); }
		
		contentwidth = Math.max(contentwidth, portwidth);
		contentheight = Math.max(contentheight, portheight);
		int viewx = 0, viewy = 0;
		int[] view = getRectangle(component, ":view");
		if (view != null) { // check the previous location
			viewx = Math.max(0, Math.min(view[2], contentwidth - portwidth));
			viewy = Math.max(0, Math.min(view[3], contentheight - portheight));
		}
		setRectangle(component, ":view", viewx, viewy, contentwidth, contentheight);
		return vneed || hneed;
	}

	/**
	 *
	 */
	private void scrollToVisible(Object component,
			int x, int y, int width, int height) {
		int[] view = getRectangle(component, ":view");
		int[] port = getRectangle(component, ":port");
		int vx = Math.max(x + width - port[0], Math.min(view[2], x));
		int vy = Math.max(y + height - port[1], Math.min(view[3], y));
		if ((view[2] != vx) || (view[3] != vy)) {
			repaint(component); // horizontal | vertical
			view[2] = vx; view[3] = vy;
		}
	}
	
	/**
	 * Gets the preferred size of the root component
	 *
	 * @return a dimension object indicating the root component's preferred size 
	 */
	public int[] getPreferredSize() {
		return getPreferredSize(content);
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	private int[] getPreferredSize(Object component) {
		int width = getInteger(component, "width", 0);
		int height = getInteger(component, "height", 0);
		if ((width > 0) && (height > 0)) {
			return new int[] { width, height };
		}
		String classname = getClass(component);
		if ("label" == classname) {
			return getSize(component, 0, 0);
		} 
		if (("button" == classname) || ("togglebutton" == classname)) {
			if ("button" == classname && getChoice(component, "type") == "link")
				return getSize(component, ITEM.left + ITEM.right,
					ITEM.top + ITEM.bottom);
			else
				return getSize(component, BUTTON.left + BUTTON.right,
					BUTTON.top + BUTTON.bottom);
		} 
		if ("checkbox" == classname) {
			int[] d = getSize(component, 0, 0);
			d[0] = d[0] + block + 3;
			d[1] = Math.max(block, d[1]);
			return d;
		}
		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				int[] size = getFieldSize(component);
				Image icon = getIcon(component, "icon", null);
				if (icon != null) {
					size[0] += icon.getWidth();
					size[1] = Math.max(size[1], icon.getHeight() + 2);
				}
				size[0] += block;
				return size;
			} else {
				// maximum size of current values and choices including 2-2-2-2 insets
				int[] size = getSize(component, 4 , 4);
				for (Object item = get(component, ":comp"); item != null; item = get(item, ":next")) {
					int[] d = getSize(item, 4 , 4);
					size[0] = Math.max(d[0], size[0]); size[1] = Math.max(d[1], size[1]);
				}
				size[0] += block;
				if (size[1] == 4) { // no content nor items, set text height
					Font customfont = (Font) get(component, "font");
					FontMetrics fm = getFontMetrics((customfont != null) ? customfont : font);
					size[1]  = font.getHeight() + 4;
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
			FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
			return new int[] { 
				((columns > 0) ? (columns * font.charWidth('e') + 2) : 76) + 2 + block,
				((rows > 0) ? (rows * font.getHeight() + 2) : 76) + 2 + block };
		}
		if ("tabbedpane" == classname) {
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
                        boolean stacked = (placement == "stacked");
                        int cnt = getItemCountImpl(component, ":comp");
			int tabsize = 0; // max tab height/width
			int contentwidth = 0; int contentheight = 0; // max content size
			for (Object tab = get(component, ":comp");
					tab != null; tab = get(tab, ":next")) {
				int[] d = getSize(tab, horizontal ? 12 : 9, horizontal ? 5 : 8);
				tabsize = Math.max(tabsize, horizontal ? d[1] : d[0]);
				
				Object comp = get(tab, ":comp");
				if ((comp != null) && getBoolean(comp, "visible", true)) {
					int[] dc = getPreferredSize(comp);
					contentwidth = Math.max(contentwidth, dc[0]);
					contentheight = Math.max(contentheight, dc[1]);
				}
			}
			return new int[] { contentwidth + ((horizontal || stacked)? 4 : (tabsize + 3)),
				contentheight + (horizontal? (tabsize + 3) : stacked? (tabsize + 3) * cnt : 4) };
		}
		if (("panel" == classname) || (classname == "dialog")) {
			// title text and icon height
			int[] size = getSize(component, 0, 0);
			// add border size
			if (classname == "dialog") {
				size[0] = 8; size[1] += 8; // title width neglected
			}
			else if (getBoolean(component, "border", false)) { // bordered panel
				size[0] = 2; size[1] += (size[1] > 0) ? 1 : 2; // title includes line
			}
			else { size[0] = 0; } // title width is clipped
			// add paddings
			size[0] += getInteger(component, "left", 0) + getInteger(component, "right", 0);
			size[1] += getInteger(component, "top", 0) + getInteger(component, "bottom", 0);
			// add content preferred size
			int gap = getInteger(component, "gap", 0);
			int[][] grid = getGrid(component, gap);
			if (grid != null) { // has components
				size[0] += getSum(grid[0], 0, grid[0].length, gap, false);
				size[1] += getSum(grid[1], 0, grid[1].length, gap, false);
			}
			return size;
		}
		else if ("desktop" == classname) {
			int[] size = new int[] { 0, 0 };
			for (Object comp = get(component, ":comp");
					comp != null; comp = get(comp, ":next")) {
				String iclass = getClass(comp);
				if ((iclass != "dialog") && (iclass != ":popup") &&
						(iclass != ":combolist")) {
					int[] d = getPreferredSize(comp);
					size[0] = Math.max(d[0], size[0]);
					size[1] = Math.max(d[1], size[1]);
				}
			}
			return size;
		}
		if ("spinbox" == classname) {
			int[] size = getFieldSize(component);
			size[0] += block;
			return size;
		}
		if ("progressbar" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			return new int[] { horizontal ? 76 : 6, horizontal ? 6 : 76 };
		}
		if ("slider" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			return new int[] { horizontal ? 76 : 10, horizontal ? 10 : 76 };
		}
		if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			Object comp1 = get(component, ":comp");
			int[] size = ((comp1 == null) || !getBoolean(comp1, "visible", true)) ?
				new int[] { 0, 0 } : getPreferredSize(comp1);
			Object comp2 = get(comp1, ":next");
			if ((comp2 != null) && getBoolean(comp2, "visible", true)) {
				int[] d = getPreferredSize(comp2);
				size[0] = horizontal ? (size[0] + d[0]) :
					Math.max(size[0], d[0]);
				size[1] = horizontal ? Math.max(size[1], d[1]) :
					(size[1] + d[1]);
			}
			if (horizontal) { size[0] += 5; } else { size[1] += 5; }
			return size;
		}
		if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
			return new int[] { 76 + 2 + block, 76 + 2 + block };
		}
		if ("separator" == classname) {
			return new int[] { 1, 1 };
		}
		if ("menubar" == classname) { 
			int[] size = new int[] { 0, 0 };
			for (Object menu = get(component, ":comp");
					menu != null; menu = get(menu, ":next")) {
				int[] d = getSize(menu, 8, 4);
				size[0] += d[0];
				size[1] = Math.max(size[1], d[1]);
			}
			return size;
		}
		if ("bean" == classname) {
				return ((Component) get(component, "bean")).getPreferredSize();
		}
		throw new IllegalArgumentException((String) classname);
	}

	/**
	 * @param component a container
	 * @param gap space between components
	 * @return null for zero visible subcomponent, otherwise an array contains the following lists:
	 * <ul><li>columnwidths, preferred width of grid columns</li>
	 * <li>rowheights, preferred heights of grid rows</li>
	 * <li>columnweights, grid column-width weights</li>
	 * <li>rowweights, grid row-height weights</li>
	 * <li>gridx, horizontal location of the subcomponents</li>
	 * <li>gridy, vertical locations</li>
	 * <li>gridwidth, column spans</li>
	 * <li>gridheight, row spans</li></ul>
	 */
	private int[][] getGrid(Object component, int gap) {
		int count = 0; // count of the visible subcomponents
		for (Object comp = get(component, ":comp"); comp != null;
				comp = get(comp, ":next")) {
			if (getBoolean(comp, "visible", true)) { count++; }
		}
		if (count == 0) { return null; } // zero subcomponent
		int columns = getInteger(component, "columns", 0);
		int icols = (columns != 0) ? columns : count;
		int irows = (columns != 0) ? ((count + columns - 1) / columns) : 1;
		int[][] grid = {
			new int[icols], new int[irows], // columnwidths, rowheights
			new int[icols], new int[irows], // columnweights, rowweights
			new int[count], new int[count], // gridx, gridy
			new int[count], new int[count] }; // gridwidth, gridheight
		int[] columnheight = new int[icols];
		int[][] cache = null; // preferredwidth, height, columnweight, rowweight

		int i = 0; int x = 0; int y = 0;
		int nextsize = 0;
		for (Object comp = get(component, ":comp");
				comp != null; comp = get(comp, ":next")) {
			if (!getBoolean(comp, "visible", true)) { continue; }
			int colspan = ((columns != 0) && (columns < count)) ?
				Math.min(getInteger(comp, "colspan", 1), columns) : 1;
			int rowspan = (columns != 1) ? getInteger(comp, "rowspan", 1) : 1;
			
			for (int j = 0; j < colspan; j++) {
				if ((columns != 0) && (x + colspan > columns)) {
					x = 0; y++; j = -1;
				}
				else if (columnheight[x + j] > y) {
					x += (j + 1); j = -1;
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
			int[] d = getPreferredSize(comp);

			if (colspan == 1) {
				grid[0][x] = Math.max(grid[0][x], d[0]); // columnwidths
				grid[2][x] = Math.max(grid[2][x], weightx); // columnweights
			}
			else {
				if (cache == null) { cache = new int[4][count]; }
				cache[0][i] = d[0];
				cache[2][i] = weightx;
				if ((nextsize == 0) || (colspan < nextsize)) { nextsize = colspan; }
			}
			if (rowspan == 1) {
				grid[1][y] = Math.max(grid[1][y], d[1]); // rowheights 
				grid[3][y] = Math.max(grid[3][y], weighty); // rowweights
			}
			else {
				if (cache == null) { cache = new int[4][count]; }
				cache[1][i] = d[1];
				cache[3][i] = weighty;
				if ((nextsize == 0) || (rowspan < nextsize)) { nextsize = rowspan; }
			}
			grid[4][i] = x; //gridx
			grid[5][i] = y; //gridy
			grid[6][i] = colspan; //gridwidth
			grid[7][i] = rowspan; //gridheight
			
			x += colspan;
			i++;
		}

		while (nextsize != 0) {
			int size = nextsize; nextsize = 0;
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
					}
					else if ((grid[6 + j][k] > size) &&
							((nextsize == 0) || (grid[6 + j][k] < nextsize))) {
						nextsize = grid[6 + j][k];
					}
				}
			}
		}
		return grid;
	}

	/**
	 *
	 */
	private int getSum(int[] values,
			int from, int length, int gap, boolean last) {
		if (length <= 0) { return 0; }
		int value = 0;
		for (int i = 0; i < length; i++) {
			value += values[from + i];
		}
		return value + (length - (last ? 0 : 1)) * gap;
	}

	/**
	 *
	 */
	private int[] getFieldSize(Object component) {
		String text = getString(component, "text", "");
		int columns = getInteger(component, "columns", 0);
		Font currentfont = (Font) get(component, "font");
		FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
		return new int[] { ((columns > 0) ?
			(columns * font.charWidth('e')) : 76) + 4,
			font.getHeight() + 4 }; // font.stringWidth(text)
	}

	/**
	 * @param component a widget including the text and icon parameters
	 * @param dx increase width by this value
	 * @param dy increase height by this value
	 * @return size of the text and the image (plus a gap) including the given offsets
	 */
	private int[] getSize(Object component, int dx, int dy) {
		String text = getString(component, "text", null);
		int tw = 0; int th = 0;
		if (text != null) {
			Font customfont = (Font) get(component, "font");
			FontMetrics fm = getFontMetrics((customfont != null) ? customfont : font);
			tw = font.stringWidth(text);
			th = font.getHeight();
		}
		Image icon = getIcon(component, "icon", null);
		int iw = 0; int ih = 0;
		if (icon != null) {
			iw = icon.getWidth();
			ih = icon.getHeight();
			if (text != null) { iw += 2; }
		}
		return new int[] { tw + iw + dx, Math.max(th, ih) + dy };
	}

	/**
	 * Invokes the paint method
	 */
	public void update(Graphics g) {
		paint(g);
	}

	/*public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if (infoflags == ImageObserver.ALLBITS) {
			validate(content);
		}
		return super.imageUpdate(img, infoflags, x, y, width, height);
	}*/

	/**
	 * Paints the components inside the graphics clip area
	 */
	public void paint(Graphics g) {
		g.setFont(font);
		if (hgradient == null) {
			int[][] pix = new int[2][block * block];
			int r1 = c_bg.getRed(); int r2 = c_press.getRed();
			int g1 = c_bg.getGreen(); int g2 = c_press.getGreen();
			int b1 = c_bg.getBlue(); int b2 = c_press.getBlue();
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
			hgradient = createImage(new MemoryImageSource(block, block, pix[0], 0, block));
			vgradient = createImage(new MemoryImageSource(block, block, pix[1], 0, block));
			pix = new int[1][block * block];
			r1 = c_select.getRed(); r2 = c_select.getRed();
			g1 = c_select.getGreen(); g2 = c_select.getGreen();
			b1 = c_select.getBlue(); b2 = c_select.getBlue();
			for (int i = 0; i < block; i++) {
				int cr = r1 - (r1 - r2) * i / block;
				int cg = g1 - (g1 - g2) * i / block;
				int cb = b1 - (b1 - b2) * i / block;
				int color = (255 << 24) | (cr << 16) | (cg << 8) | cb;
				for (int j = 0; j < block; j++) {
					pix[0][i * block + j] = color;
				}
			}
			hmodal = createImage(new MemoryImageSource(block, block, pix[0], 0, block));
		}
		//g.setColor(Color.orange);
		//g.fillRect(0, 0, getSize()[0], getSize()[1]);
		//long time = System.currentTimeMillis();
		int[] clip = g.getClipBounds();
		///dg.setClip(r[2], r[3], r[0], r[1]);
		paint(g, clip[2], clip[3], clip[0], clip[1], content, isEnabled());
		//System.out.println(System.currentTimeMillis() - time);
		//g.setClip(0, 0, getSize()[0], getSize()[1]);
		//g.setColor(Color.red); g.drawRect(clip[2], clip[3], clip[0] - 1, clip[1] - 1);
	}
	
	/**
	 * Fill a given part of a MemoryImageSource with gradient
	 * @param pix arrays of source pixels
	 * @param offset image width 
	 * @param x gradient left location
	 * @param y fill top location
	 * @param width gradient width size
	 * @param height fill height size
	 * @param color1 top/left color
	 * @param color2 bottom/right color
	 * @param horizontal horizontal if true, vertical otherwise
	 */
	private static void fillGradient(int[] pix, int offset, int x, int y, int width, int height,
			int color1, int color2, boolean horizontal) {
		int r1 = (color1 >> 16) & 0xff; int rd = ((color2 >> 16) & 0xff) - r1;
		int g1 = (color1 >> 8) & 0xff; int gd = ((color2 >> 8) & 0xff) - g1;
		int b1 = color1 & 0xff; int bd = (color2 & 0xff) - b1;
		int gs = horizontal ? width : height; int fs = horizontal ? height : width;
		for (int i = 0; i < gs; i++) {
			int color = 0xff000000 | ((r1 + rd * i / gs) << 16) |
				((g1 + gd * i / gs) << 8) | (b1 + bd * i / gs);
			for (int j = 0; j < fs; j++) {
				pix[x + (horizontal ? i : j) + (y + (horizontal ? j : i)) * offset] = color;
			}
		}
	}

	/**
	 * @param clipx the cliping rectangle is relative to the component's
	 * parent location similar to the component's bounds rectangle
	 * @param clipy
	 * @param clipwidth
	 * @param clipheight
	 * @throws java.lang.IllegalArgumentException
	 */
	private void paint(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight,
			Object component, boolean enabled) {
		if (!getBoolean(component, "visible", true)) { return; }
		int[] bounds = getRectangle(component, "bounds");
		if (bounds == null) { return; }
		// negative component width indicates invalid component layout
		if (bounds[0] < 0) {
			bounds[0] = Math.abs(bounds[0]);
			doLayout(component);
		}
		// return if the component was out of the cliping rectangle
		if ((clipx + clipwidth < bounds[2]) ||
				(clipx > bounds[2] + bounds[0]) ||
				(clipy + clipheight < bounds[3]) ||
				(clipy > bounds[3] + bounds[1])) {
			return;
		}
		// set the clip rectangle relative to the component location
		clipx -= bounds[2]; clipy -= bounds[3];
		g.translate(bounds[2], bounds[3]); 
		//g.setClip(0, 0, bounds[0], bounds[1]);
		String classname = getClass(component);
		boolean pressed = (mousepressed == component);
		boolean inside = (mouseinside == component) &&
			((mousepressed == null) || pressed);
		boolean focus = focusinside && (focusowner == component);
		enabled = getBoolean(component, "enabled", true); //enabled &&

		if ("label" == classname) {
                        int bg = (Color) get(component, "background");
                        if (bg != null) {
                            g.setColor(bg);
                            g.fillRect(0, 0, bounds[0], bounds[1]);
                        }
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				0, 0, bounds[0], bounds[1],
				enabled ? c_text : c_disable, "left", true);
		}
		else if (("button" == classname) || ("togglebutton" == classname)) {
			boolean toggled = ("togglebutton" == classname) && getBoolean(component, "selected", false);
			boolean link = ("button" == classname) && (getChoice(component, "type") == "link");
			if (link) {
                            g.setColor(enabled ? ((inside != pressed) ? c_hover : (pressed ? c_press : c_bg)) : c_bg);
                            g.fillRect(0, 0, bounds[0], bounds[1]);
                        } else {
                                int bg = (Color) get(component, "background");
                                if (bg == null) bg = enabled ? ((inside != pressed) ? c_hover : ((pressed || toggled) ? c_press : c_ctrl)) :
					(toggled ? c_press : c_bg);
                                else bg = enabled ? ((inside != pressed) ? bg.brighter() : ((pressed || toggled) ? bg.darker() : bg)) :
					(toggled ? bg.darker() : bg);
				paintRect(g, 0, 0, bounds[0], bounds[1],
					enabled ? c_border : c_disable,
					bg, true, true, true, true, true);
			}
			if (focus) {
				g.setColor(c_focus);
				g.drawRect(2, 2, bounds[0] - 5, bounds[1] - 5);
			}
			if (enabled && ("button" == classname) && get(component, "type") == "default") {
				g.setColor(c_border);
				g.drawRect(1, 1, bounds[0] - 3, bounds[1] - 3);
			}
			if (link)
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					ITEM.left, ITEM.top, bounds[0] - ITEM.left - ITEM.right,
					bounds[1] - ITEM.top - ITEM.bottom,
					enabled ? c_text : c_disable, "left", true);
			else
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					BUTTON.left, BUTTON.top, bounds[0] - BUTTON.left - BUTTON.right,
					bounds[1] - BUTTON.top - BUTTON.bottom,
					enabled ? c_text : c_disable, "center", true);
		}
		else if ("checkbox" == classname) {
			boolean selected = getBoolean(component, "selected", false);
			String group = getString(component, "group", null);
			int border = enabled ? c_border : c_disable;
			int foreground = enabled ? ((inside != pressed) ? c_hover :
				(pressed ? c_press : c_ctrl)) : c_bg;
			int dy = (bounds[1] - block + 2) / 2;
			if (group == null) {
				paintRect(g, 1, dy + 1, block - 2, block - 2,
					border, foreground, true, true, true, true, true);
			} else {
				g.setColor((foreground != c_ctrl) ? foreground : c_bg);
				g.fillOval(1, dy + 1, block - 3 + evm, block - 3 + evm);
				g.setColor(border);
				g.drawOval(1, dy + 1, block - 3, block - 3);
			}
			if (focus) {
				g.setColor(c_focus);
				if (group == null) {
					g.drawRect(3, dy + 3, block - 7, block - 7);
				} else {
					g.drawOval(3, dy + 3, block - 7, block - 7);
				}
			}
			if((!selected && inside && pressed) ||
					(selected && (!inside || !pressed))) {
				g.setColor(enabled ? c_text : c_disable);
				if (group == null) {
					g.fillRect(3, dy + block - 9, 2 + evm, 6 + evm);
					g.drawLine(3, dy + block - 4, block - 4, dy + 3);
					g.drawLine(4, dy + block - 4, block - 4, dy + 4);
				} else {
					g.fillOval(5, dy + 5, block - 10 + evm, block - 10 + evm);
					g.drawOval(4, dy + 4, block - 9, block - 9);
				}
			}
			paintContent(component, g, clipx, clipy, clipwidth, clipheight,
				block + 3, 0, bounds[0] - block - 3, bounds[1],
				enabled ? c_text : c_disable, "left", true);
		}
		else if ("combobox" == classname) {
			if (getBoolean(component, "editable", true)) {
				Image icon = getIcon(component, "icon", null);
				int left = (icon != null) ? icon.getWidth() : 0;
				paintField(g, clipx, clipy, clipwidth, clipheight, component,
					bounds[0] - block, bounds[1],
					inside, pressed, focus, enabled, false, left);
				if (icon != null) {
					g.drawImage(icon, 2, (bounds[1] - icon.getHeight()) / 2, this);
				}
				paintArrow(g, bounds[0] - block, 0, block, bounds[1],
					'S', enabled, inside, pressed, "down", true, false, true, true, true);
			} else {
				paintRect(g, 0, 0, bounds[0], bounds[1],
					enabled ? c_border : c_disable,
					enabled ? ((inside != pressed) ? c_hover :
						(pressed ? c_press : c_ctrl)) : c_bg, true, true, true, true, true);
				if (focus) {
					g.setColor(c_focus);
					g.drawRect(2, 2, bounds[0] - block - 5, bounds[1] - 5);
				}
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					2, 2, bounds[0] - block - 4, bounds[1] - 4,
					enabled ? c_text : c_disable, "left", false);
				g.setColor(enabled ? c_text : c_disable);
				paintArrow(g, bounds[0] - block, 0, block, bounds[1], 'S');
			}
		}
		else if (":combolist" == classname) {
			paintScroll(component, classname, bounds, pressed, inside, focus, enabled,
				g, clipx, clipy, clipwidth, clipheight);
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds[0], bounds[1],
				inside, pressed, focus, enabled, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			paintScroll(component, classname, bounds, pressed, inside, focus, enabled,
				g, clipx, clipy, clipwidth, clipheight);
		}
		else if ("tabbedpane" == classname) {
			int i = 0; int idx = 0; Object selectedtab = null;
			int selected = getInteger(component, "selected", 0);
			String placement = getString(component, "placement", "top");
			boolean horizontal = ((placement == "top") || (placement == "bottom"));
                        boolean stacked = (placement == "stacked");
                        int cnt = getItemCountImpl(component, ":comp");
			int bx = horizontal ? 2 : 1, by = horizontal ? 1 : 2, bw = 2 * bx, bh = 2 * by;
			int dx = horizontal ? 6 : ((placement == "left") ? 5 : 4),
				dy = horizontal ? ((placement == "top") ? 3 : 2) : 4,
				dw = horizontal ? 12 : 9, dh = horizontal ? 5 : 8;
			// paint tabs except the selected one
			g.clipRect(0, 0, bounds[0], bounds[1]); //+clip
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				if (selected != i) {
					boolean hover = inside && (mousepressed == null) && (insidepart == tab);
					boolean tabenabled = enabled && getBoolean(tab, "enabled", true);
					int[] r = getRectangle(tab, "bounds");
                                        if (r == null) {
                                            r = new Rectangle();
                                            set(tab, "bounds", r);
                                        }
                                        int bg = (Color) get(tab, "background");
                                        if (bg == null) bg = tabenabled ? (hover ? c_hover : c_ctrl) : c_ctrl;
                                        else bg = tabenabled ? (hover ? bg.brighter() : bg.darker()) : bg.brighter();
					paintRect(g, r[2] + bx, r[3] + by, r[0] - bw, r[1] - bh,
						enabled ? c_border : c_disable,
						bg,
						(placement != "bottom"), (placement != "right"),
						stacked ? false : (placement != "top"), (placement != "left"), true);
                                        if (stacked) {
                                            g.setColor(enabled ? c_border : c_disable);
                                            g.drawLine(1, r[3] + r[1] - 3, bounds[0] - 1, r[3] + r[1] - 3);
                                            g.drawLine(1, r[3] + r[1] - 3, 1, r[3] + r[1] );
                                            g.drawLine(bounds[0] - 1, r[3] + r[1] - 3, bounds[0] - 1, r[3] + r[1] + 2);
                                            /* cut corners ... uncomment if you like them
                                            int x = r[2] + bx + r[0] - bw - r[1] / 3;
                                            int y = r[3] + by;
                                            g.drawLine(x, y, x + r[1] / 3 - 1, y + r[1] / 3 - 1);
                                            g.setColor(c_bg);
                                            g.fillPolygon(
                                                new int[]{x + 1, x + r[1] / 3, x + r[1] / 3},
                                                new int[]{y, y, y + r[1] / 3 - 1},
                                                3);
                                             */
                                        }
					paintContent(tab, g, clipx, clipy, clipwidth, clipheight,
						r[2] + dx, r[3] + dy, r[0] - dw, r[1] - dh,
						tabenabled ? c_text : c_disable, "left", true);
				}
				else {
                                    selectedtab = tab;
                                    idx = i;
                                }
				i++;
			}
			
			// paint selected tab and its content
			if (selectedtab != null) {
				int[] r = getRectangle(selectedtab, "bounds");
                                if (r == null) {
                                    r = new Rectangle();
                                    set(selectedtab, "bounds", r);
                                }
				// paint tabbedpane border
                                if (stacked) {
                                    g.setColor(enabled ? c_border : c_disable);
                                    g.drawLine(0, r[3] + r[1] - 1, bounds[0] - 1, r[3] + r[1] - 1);
                                } else
				paintRect(g, (placement == "left") ? r[0] - 1 : 0,
					(placement == "top") ? r[1] - 1 : 0,
					horizontal ? bounds[0] : (bounds[0] - r[0] + 1),
					horizontal ? (bounds[1] - r[1] + 1) : bounds[1],
					enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
                                int bg = (Color) get(selectedtab, "background");
                                if (bg == null) bg = c_bg;
				// paint selected tab
				paintRect(g, r[2], r[3], stacked ? r[0]: r[0], r[1], enabled ? c_border : c_disable, bg,
					(placement != "bottom"), (placement != "right"),
					(placement != "top") && !stacked, (placement != "left"), true);
				if (focus) {
					g.setColor(c_focus);
					g.drawRect(r[2] + ((placement != "right") ? 2 : 0), r[3] + ((placement != "bottom") ? 2 : 0),
						r[0] - ((horizontal || stacked)? 5 : 3), r[1] - ((horizontal || stacked) ? 3 : 5));
				}
				paintContent(selectedtab, g, clipx, clipy, clipwidth, clipheight,
					r[2] + dx, r[3] + dy, r[0] - dw, r[1] - dh,
					enabled ? c_text : c_disable, "left", true);
					
				Object comp = get(selectedtab, ":comp");
				if ((comp != null) && getBoolean(comp, "visible", true)) {
                                        if (stacked) {
                                                int[] cr = getRectangle(comp, "bounds");
                                                if (cr == null) {
                                                    cr = new Rectangle();
                                                    set(comp, "bounds", cr);
                                                }
                                                cr[3] = r[1] + 1;
                                                doLayout(comp);
                                                g.setColor(enabled ? c_border : c_disable);
                                                g.drawLine(0, r[3] + r[1] - 1, 0, r[3] + r[1] - 1 + cr[1] + 3);
                                                g.drawLine(bounds[0] - 1, r[3] + r[1] - 1, bounds[0] - 1, r[3] + r[1] - 1 + cr[1] + 3);
                                                g.drawLine(0, r[3] + r[1] - 1 + cr[1] + 3, bounds[0] - 1, r[3] + r[1] - 1 + cr[1] + 3);
                                        }
					clipx -= r[2]; clipy -= r[3]; g.translate(r[2], r[3]); // relative to tab
					paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
					clipx += r[2]; clipy += r[3]; g.translate(-r[2], -r[3]);
				}
			}
			g.setClip(clipx, clipy, clipwidth, clipheight); //+clip
		}
		else if (("panel" == classname) || ("dialog" == classname)) {
			int titleheight = getInteger(component, ":titleheight", 0);
                        int bg = (Color) get(component, "background");
                        if (bg == null) bg = c_bg;
			if ("dialog" == classname) {
				paintRect(g, 0, 0, bounds[0], 3 + titleheight, // dialog titlebar
					c_border, c_ctrl, true, true, false, true, true);
				paintRect(g, 0, 3 + titleheight, bounds[0], bounds[1] - 3 - titleheight,
					c_border, c_press, false, true, true, true, true); // lower part excluding titlebar
				paintContent(component, g, clipx, clipy, clipwidth, clipheight,
					3, 2, bounds[0] - 6, titleheight, c_text, "left", false);
				paintRect(g, 3, 3 + titleheight, bounds[0] - 6, bounds[1] - 6 - titleheight, // content area
					c_border, bg, true, true, true, true, true);
			} else { // panel
				boolean border = getBoolean(component, "border", false);
				paintRect(g, 0, titleheight / 2, bounds[0], bounds[1] - (titleheight / 2),
					enabled ? c_border : c_disable, bg, border, border, border, border, true);
				paintContent(component, g, clipx, clipy, clipwidth, clipheight, // panel title
					3, 0, bounds[0] - 6, titleheight, enabled ? c_text : c_disable, "left", false);
			}
			
			if (get(component, ":port") != null) {
				paintScroll(component, classname, bounds, pressed, inside, focus, enabled,
					g, clipx, clipy, clipwidth, clipheight);
			}
			else {
				for (Object comp = get(component, ":comp");
						comp != null; comp = get(comp, ":next")) {
					paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
					}
			}
		}
		else if ("desktop" == classname) {
			paintRect(g, 0, 0, bounds[0], bounds[1],
				c_border, c_bg, false, false, false, false, true);
			paintReverse(g, clipx, clipy, clipwidth, clipheight,
				get(component, ":comp"), enabled);
			//g.setColor(Color.red); if (clip != null) g.drawRect(clipx, clipy, clipwidth, clipheight);
			if ((tooltipowner != null) && (component == content)) {
				int[] r = getRectangle(tooltipowner, ":tooltipbounds");
				paintRect(g, r[2], r[3], r[0], r[1],
					c_border, c_textbg, true, true, true, true, true);
				String text = getString(tooltipowner, "tooltip", null);
				g.setColor(c_text);
				g.drawString(text, r[2] + 2, r[3] + g.getFontMetrics().getAscent() + 2); //+nullpointerexception
			}			
		}
		else if ("spinbox" == classname) {
			paintField(g, clipx, clipy, clipwidth, clipheight, component,
				bounds[0] - block, bounds[1],
				inside, pressed, focus, enabled, false, 0);
			paintArrow(g, bounds[0] - block, 0, block, bounds[1] / 2,
					'N', enabled, inside, pressed, "up", true, false, false, true, true);
			paintArrow(g, bounds[0] - block, bounds[1] / 2,
				block, bounds[1] - (bounds[1] / 2),
				'S', enabled, inside, pressed, "down", true, false, true, true, true);
		}
		else if ("progressbar" == classname) {
			int minimum = getInteger(component, "minimum", 0);
			int maximum = getInteger(component, "maximum", 100);
			int value = getInteger(component, "value", 0);
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int length = (value - minimum) *
				((horizontal ? bounds[0] : bounds[1]) - 1) / (maximum - minimum);
			paintRect(g, 0, 0, horizontal ? length : bounds[0],
				horizontal ? bounds[1] : length, enabled ? c_border : c_disable,
				c_select, true, true, horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? (bounds[0] - length) : bounds[0]	,
				horizontal ? bounds[1] : (bounds[1] - length),
				enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
		}
		else if ("slider" == classname) {
			int minimum = getInteger(component, "minimum", 0);
			int maximum = getInteger(component, "maximum", 100);
			int value = getInteger(component, "value", 0);
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int length = (value - minimum) *
				((horizontal ? bounds[0] : bounds[1]) - block) /
				(maximum - minimum);
			paintRect(g, horizontal ? 0 : 3, horizontal ? 3 : 0,
				horizontal ? length : (bounds[0] - 6),
				horizontal ? (bounds[1] - 6) : length,
				enabled ? c_border : c_disable,
				c_bg, true, true, horizontal, !horizontal, true);
			paintRect(g, horizontal ? length : 0, horizontal ? 0 : length,
				horizontal ? block : bounds[0], horizontal ? bounds[1] : block,
				enabled ? c_border : c_disable,
				enabled ? c_ctrl : c_bg, true, true, true, true, true);
			if (focus) {
				g.setColor(c_focus);
				g.drawRect(horizontal ? (length + 2) : 2, horizontal ? 2 : (length + 2),
					(horizontal ? block : bounds[0]) - 5,
					(horizontal ? bounds[1] : block) - 5);
				//g.drawRect(length + 1, 1, block - 3, bounds[1] - 3);
			}
			paintRect(g, horizontal ? (block + length) : 3,
				horizontal ? 3 : (block + length),
				bounds[0] - (horizontal ? (block + length) : 6),
				bounds[1] - (horizontal ? 6 : (block + length)),
				enabled ? c_border : c_disable,
				c_bg, horizontal, !horizontal, true, true, true);
		}
		else if ("splitpane" == classname) {
			boolean horizontal = ("vertical" != get(component, "orientation"));
			int divider = getInteger(component, "divider", -1);
			paintRect(g, horizontal ? divider : 0, horizontal ? 0 : divider,
				horizontal ? 5 : bounds[0], horizontal ? bounds[1] : 5,
				c_border, c_bg, false, false, false, false, true);
			g.setColor(enabled ? (focus ? c_focus : c_border) : c_disable);
			int xy = horizontal ? bounds[1] : bounds[0];
			int xy1 = Math.max(0, xy / 2 - 12);
			int xy2 = Math.min(xy / 2 + 12, xy - 1);
			for (int i = divider + 1; i < divider + 4; i += 2) {
				if (horizontal) { g.drawLine(i, xy1, i, xy2); }
					else { g.drawLine(xy1, i, xy2, i); }
			}
			Object comp1 = get(component, ":comp");
			if (comp1 != null) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp1, enabled);
				Object comp2 = get(comp1, ":next");
				if (comp2 != null) {
					paint(g, clipx, clipy, clipwidth, clipheight, comp2, enabled);
				}
			}
		}
		else if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
			paintScroll(component, classname, bounds, pressed, inside, focus, enabled,
				g, clipx, clipy, clipwidth, clipheight);
		}
		else if ("separator" == classname) {
			g.setColor(enabled ? c_border : c_disable);
			g.fillRect(0, 0, bounds[0] + evm, bounds[1] + evm);
		}
		else if ("menubar" == classname) {
			Object selected = get(component, "selected");
			int lastx = 0;
                        boolean above = getString(component, "placement", "bottom") == "top";
			for (Object menu = get(component, ":comp");
					menu != null; menu = get(menu, ":next")) {
				int[] mb = getRectangle(menu, "bounds");
				if (clipx + clipwidth <= mb[2]) { break; }
				if (clipx >= mb[2] + mb[0]) { continue; }
				boolean menuenabled = enabled && getBoolean(menu, "enabled", true);
				boolean armed = (selected == menu);
				boolean hover = (selected == null) && (insidepart == menu);
                                int bg = (Color) get(menu, "background");
                                if (bg == null) bg = enabled ? (menuenabled ? (armed ? c_select : (hover ? c_hover : c_ctrl)) : c_ctrl) : c_bg;
                                else bg = enabled ? (menuenabled ? (armed ? c_select : (hover ? c_hover : bg)) : c_ctrl) : bg;
				paintRect(g, mb[2], 0, mb[0], bounds[1], enabled ? c_border : c_disable,
					bg,
					armed || above, armed, armed || !above, armed, true);
				paintContent(menu, g, clipx, clipy, clipwidth, clipheight,
					mb[2] + 4, 1, mb[0], bounds[1],
					menuenabled ? c_text : c_disable, "left", true);
				lastx = mb[2] + mb[0];
			}
			paintRect(g, lastx, 0, bounds[0] - lastx, bounds[1],
				enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg,
				above, false, true, false, true);
                        //g.setColor(enabled ? c_border : c_disable);
                        //g.drawLine(bounds[0], 0, bounds[0], bounds[1] - 1);
		}
		else if (":popup" == classname) {
			paintRect(g, 0, 0, bounds[0], bounds[1],
				c_border, c_textbg, true, true, true, true, true);
			Object selected = get(component, "selected");
			for (Object menu = get(get(component, "menu"), ":comp");
					menu != null; menu = get(menu, ":next")) {
				int[] r = getRectangle(menu, "bounds");
				if (clipy + clipheight <= r[3]) { break; }
				if (clipy >= r[3] + r[1]) { continue; }
				String itemclass = getClass(menu);
				if (itemclass == "separator") {
					g.setColor(c_border);
					g.fillRect(r[2], r[3], bounds[0] - 2 + evm, r[1] + evm);
				} else {
					boolean armed = (selected == menu);
					boolean menuenabled = getBoolean(menu, "enabled", true);
					paintRect(g, r[2], r[3], bounds[0] - 2, r[1], c_border,
						armed ? c_select : c_textbg, false, false, false, false, true);
					int tx = r[2];
					if (itemclass == "checkboxmenuitem") {
						tx += block + 3;
						boolean checked = getBoolean(menu, "selected", false);
						String group = getString(menu, "group", null);
						g.translate(r[2] + 4, r[3] + 2);
						g.setColor(menuenabled ? c_border : c_disable);
						if (group == null) {
							g.drawRect(1, 1, block - 3, block - 3);
						} else {
							g.drawOval(1, 1, block - 3, block - 3);
						}
						if (checked) {
							g.setColor(menuenabled ? c_text : c_disable);
							if (group == null) {
								g.fillRect(3, block - 9, 2 + evm, 6 + evm);
								g.drawLine(3, block - 4, block - 4, 3);
								g.drawLine(4, block - 4, block - 4, 4);
							} else {
								g.fillOval(5, 5, block - 10 + evm, block - 10 + evm);
								g.drawOval(4, 4, block - 9, block - 9);
							}
						}
						g.translate(-r[2] - 4, -r[3] - 2);
					}
					paintContent(menu, g, clipx, clipy, clipwidth, clipheight,
						tx + 4, r[3] + 2, bounds[0] - 10,
						r[1] - 4, menuenabled ? c_text : c_disable, "left", true);
					if (itemclass == "menu") {
						paintArrow(g, r[2] + bounds[0] - block, r[3], block, r[1], 'E');
					}
					else {
						String accelerator = getAccelerator(menu);
						if (accelerator != null) {
							g.drawString(accelerator, bounds[0] - 4 -
								getFontMetrics(font).stringWidth(accelerator), r[3] + 2 + 10);
						}
					}
				}
			}
		}
		else if ("bean" == classname) {
				g.clipRect(0, 0, bounds[0], bounds[1]);
				((Component) get(component, "bean")).paint(g);
				g.setClip(clipx, clipy, clipwidth, clipheight);
		}
		else throw new IllegalArgumentException((String) classname);
		g.translate(-bounds[2], -bounds[3]);
		clipx += bounds[2]; clipy += bounds[3];
	}

	/**
	 *
	 */
	private void paintReverse(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight,
			Object component, boolean enabled) {
		if (component != null) {
			int[] bounds = getRectangle(component, "bounds");
			if ((clipx < bounds[2]) ||
					(clipx + clipwidth > bounds[2] + bounds[0]) ||
					(clipy < bounds[3]) ||
					(clipy + clipheight > bounds[3] + bounds[1])) {
				paintReverse(g, clipx, clipy, clipwidth, clipheight,
					get(component, ":next"), enabled);
			}
			paint(g, clipx, clipy, clipwidth, clipheight, component, enabled);
		}
	}

	/**
	 *
	 */
	private void paintField(Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight, Object component,
			int width, int height, boolean inside, boolean pressed,
			boolean focus, boolean enabled, boolean hidden, int left) {
		boolean editable = getBoolean(component, "editable", true);
		paintRect(g, 0, 0, width, height, enabled ? c_border : c_disable,
			editable ? c_textbg : c_bg, true, true, true, true, true);
		g.clipRect(1 + left, 1, width - left - 2, height - 2);

		String text = getString(component, "text", "");
		int offset = getInteger(component, ":offset", 0);
		Font currentfont = (Font) get(component, "font");
		if (currentfont != null) { g.setFont(currentfont); }
		FontMetrics fm = g.getFontMetrics();

		int caret = 0;
		if (focus) { 
			int start = getInteger(component, "start", 0); 
			int end = getInteger(component, "end", 0);
			caret = hidden ? (font.charWidth('*') * end) :
				font.stringWidth(text.substring(0, end));
			if (start != end) {
				int is = hidden ? (font.charWidth('*') * start) :
					font.stringWidth(text.substring(0, start));
				g.setColor(c_select);
				g.fillRect(2 + left - offset + Math.min(is, caret), 1,
					Math.abs(caret - is) + evm, height - 2 + evm);
			}
		}

		if (focus) {
			g.setColor(c_focus);
			g.fillRect(1 + left - offset + caret, 1, 1 + evm, height - 2 + evm);
		}
                int fg = (Color) get(component, "foreground");
                if (fg == null) fg = c_text;
		g.setColor(enabled ? fg : c_disable);
		int fx = 2 + left - offset;
		int fy = (height + fm.getAscent() - fm.getDescent()) / 2;
		if (hidden) {
			int fh = font.charWidth('*');
			for (int i = text.length(); i > 0; i--) {
				g.drawString("*", fx, fy);
				fx += fh;
			}
		} else {
			g.drawString(text, fx, fy);
		}
		if (currentfont != null) { g.setFont(font); }
		g.setClip(clipx, clipy, clipwidth, clipheight);
	}
	
	/**
	 * @param component scrollable widget
	 * @param classname
	 * @param bounds
	 * @param pressed
	 * @param inside
	 * @param focus
	 * @param enabled
	 * @param g grahics context
	 * @param clipx current cliping x location relative to the component
	 * @param clipy y location of the cliping area relative to the component
	 * @param clipwidth width of the cliping area
	 * @param clipheight height of the cliping area
	 * @param header column height
	 * @param topborder bordered on the top if true
	 * @param border define left, bottom, and right border if true
	 */
	private void paintScroll(Object component,
			String classname, int[] bounds,
			boolean pressed, boolean inside, boolean focus, boolean enabled,
			Graphics g, int clipx, int clipy, int clipwidth, int clipheight) {
		int[] port = getRectangle(component, ":port");
		int[] horizontal = getRectangle(component, ":horizontal");
		int[] vertical = getRectangle(component, ":vertical");
		int[] view = getRectangle(component, ":view");
		
		if (horizontal != null) { // paint horizontal scrollbar
			int x = horizontal[2]; int y = horizontal[3]; int width = horizontal[0]; int height = horizontal[1];
			paintArrow(g, x, y, block, height,
				'W', enabled, inside, pressed, "left", true, true, true, false, true);
			paintArrow(g, x + width - block, y, block, height,
				'E', enabled, inside, pressed, "right", true, false, true, true, true);
				
			int track = width - (2 * block);
			if (track < 10) {
				paintRect(g, x + block, y, track, height,
					enabled ? c_border : c_disable, c_bg, true, true, true, true, true);
			}
			else {
				int knob = Math.max(track * port[0] / view[0], 10);
				int decrease = view[2] * (track - knob) / (view[0] - port[0]);
				paintRect(g, x + block, y, decrease, height,
					enabled ? c_border : c_disable, c_bg, false, true, true, false, true);
				paintRect(g, x + block + decrease, y, knob, height,
					enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, true, true, true, true, true);
				int n = Math.min(5, (knob - 4) / 3);
				g.setColor(enabled ? c_border : c_disable);
				int cx = (x + block + decrease) + (knob + 2 - n * 3) / 2;
				for (int i = 0; i < n; i++ ) {
					g.drawLine(cx + i * 3, y + 3, cx + i * 3, y + height - 5);
				}
				int increase = track - decrease - knob;
				paintRect(g, x + block + decrease + knob, y, increase, height,
					enabled ? c_border : c_disable, c_bg, false, false, true, true, true);
			}
		}
			
		if (vertical != null) { // paint vertical scrollbar
			int x = vertical[2]; int y = vertical[3]; int width = vertical[0]; int height = vertical[1];
			paintArrow(g, x, y, width, block,
				'N', enabled, inside, pressed, "up", true, true, false, true, false);
			paintArrow(g, x, y + height - block, width, block,
				'S', enabled, inside, pressed, "down", false, true, true, true, false);
				
			int track = height - (2 * block);
			if (track < 10) {
				paintRect(g, x, y + block, width, track,
					enabled ? c_border : c_disable, c_bg, true, true, true, true, false);
			}
			else {
				int knob = Math.max(track * port[1] / view[1], 10);
				int decrease = view[3] * (track - knob) / (view[1] - port[1]);
				paintRect(g, x, y + block, width, decrease,
					enabled ? c_border : c_disable, c_bg, true, false, false, true, false);
				paintRect(g, x, y + block + decrease, width, knob,
					enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg, true, true, true, true, false);
				int n = Math.min(5, (knob - 4) / 3);
				g.setColor(enabled ? c_border : c_disable);
				int cy = (y + block + decrease) + (knob + 2 - n * 3) / 2;
				for (int i = 0; i < n; i++ ) {
					g.drawLine(x + 3, cy + i * 3, x + width - 5, cy + i * 3);
				}
				int increase = track - decrease - knob;
				paintRect(g, x, y + block + decrease + knob, width, increase,
					enabled ? c_border : c_disable, c_bg, false, false, true, true, false);
			}
		}
		
		boolean hneed = (horizontal != null); boolean vneed = (vertical != null);
		if (("panel" != classname) && ("dialog" != classname)) {
                        boolean border = ("textarea" == classname) ? getBoolean(component, "border", true) : true;
                        int bg = (Color)get(component, "background");
                        if (bg == null) bg = border ? c_textbg : c_bg;
			paintRect(g, port[2] - 1, port[3] - 1, 	port[0] + (vneed ? 1 : 2), port[1] + (hneed ? 1 : 2),
				enabled ? c_border : c_disable, bg, border, border, !hneed && border, !vneed && border, true);
			if ("table" == classname) {
				Object header = get(component, "header");
				if (header != null) {
					int[] columnwidths = (int []) get(component, ":widths");
					Object column = get(header, ":comp"); int x = 0;
					for (int i = 0; i < columnwidths.length; i++) {
						if (i != 0) { column = get(column, ":next"); }
						boolean lastcolumn = (i == columnwidths.length - 1);
						int width = lastcolumn ? Math.max(port[0] - x, columnwidths[i]) + 2 : columnwidths[i];
						if (x - view[2] + Math.min(port[0] - (x - view[2]), width) > 0 &&
							Math.max(0, x - view[2]) <= port[0]) {
							int truncwidth = (port[0] - (x - view[2])) + 2;
							if (truncwidth > width) truncwidth = -1;
							paintRect(g, Math.max(0, x - view[2]), 0, (truncwidth > 0)? truncwidth: (lastcolumn? width : Math.min(port[0] - (x - view[2]), width)), port[3] - 1,
								enabled ? c_border : c_disable, enabled ? c_ctrl : c_bg,
								true, true, false, lastcolumn || (truncwidth > 0), true);
							paintContent(column, g, clipx, clipy, clipwidth, clipheight,
								x + 2 - view[2], 1, width - 2,
								port[3] - 3, enabled ? c_text : c_disable, "left", false);
						
							Object sort = get(column, "sort"); // "none", "ascent", "descent"
							if (sort != null) {
								paintArrow(g, x - view[2] + width - block, 0, block, port[3],
									(sort == "ascent") ? 'S' : 'N');
							}
						}
						x += width;
					}
				}
			}
		}
		int x1 = Math.max(clipx, port[2]);
		int x2 = Math.min(clipx + clipwidth, port[2] + port[0]);
		int y1 = Math.max(clipy, port[3]);
		int y2 = Math.min(clipy + clipheight, port[3] + port[1]);
		if ((x2 > x1) && (y2 > y1)) {
			g.clipRect(x1, y1, x2 - x1, y2 - y1);
			g.translate(port[2] - view[2], port[3] - view[3]);
			
			paint(component, classname, focus, enabled,
				g, view[2] - port[2] + x1, view[3] - port[3] + y1, x2 - x1, y2 - y1, port[0], port[1], view[0]);
			
			g.translate(view[2] - port[2], view[3] - port[3]);
			g.setClip(clipx, clipy, clipwidth, clipheight);
		}
	}
	
	/**
	 * Paint scrollable content
	 * @param component a panel
	 */
	private void paint(Object component,
			String classname, boolean focus, boolean enabled,
			Graphics g, int clipx, int clipy, int clipwidth, int clipheight,
			int portwidth, int portheight, int viewwidth) {
		if ("textarea" == classname) {
			char[] chars = (char[]) get(component, ":text");
			int start = focus ? getInteger(component, "start", 0) : 0;
			int end = focus ? getInteger(component, "end", 0) : 0;
                        boolean border = getBoolean(component, "border", true);
                        boolean editable = getBoolean(component, "editable", true);
			int is = Math.min(start, end); int ie = Math.max(start, end);
			Font currentfont = (Font) get(component, "font");
			if (currentfont != null) { g.setFont(currentfont); }
			FontMetrics fm = g.getFontMetrics();
			int fontascent = fm.getAscent(); int fontheight = font.getHeight();
			int ascent = 1;
                        int fg = (Color) get(component, "foreground");
                        if (fg == null) fg = c_text;
			
			for (int i = 0, j = 0; j <= chars.length; j++) {
				if ((j == chars.length) || (chars[j] == '\n')) {
					if (clipy + clipheight <= ascent) { break; } // the next lines are bellow paint rectangle
					if (clipy < ascent + fontheight) { // this line is not above painting area
						if (focus && (is != ie) && (ie >= i) && (is <= j)) {
							int xs = (is < i) ? -1 : ((is > j) ? (viewwidth - 1) :
								font.charsWidth(chars, i, is - i));
							int xe = ((j != -1) && (ie > j)) ? (viewwidth - 1) :
								font.charsWidth(chars, i, ie - i);
							g.setColor(c_select);
							g.fillRect(1 + xs, ascent, xe - xs + evm, fontheight + evm);
						}
						g.setColor(enabled ? fg : c_disable);
						g.drawChars(chars, i, j - i, 1, ascent + fontascent);
						if (focus && (end >= i) && (end <= j) && editable) {
							int caret = font.charsWidth(chars, i, end - i);
							g.setColor(c_focus);
							g.fillRect(caret, ascent, 1 + evm, fontheight + evm);
						}
					}
					ascent += fontheight;
					i = j + 1;
				}
			}
			if (currentfont != null) { g.setFont(font); }
		}
		else if (":combolist" == classname) {
			Object lead = get(component, ":lead");
			for (Object choice = get(get(component, "combobox"), ":comp");
					choice != null; choice = get(choice, ":next")) {
				int[] r = getRectangle(choice, "bounds");
				if (clipy + clipheight <= r[3]) { break; }
				if (clipy >= r[3] + r[1]) { continue; }
				paintRect(g, r[2], r[3], portwidth, r[1], c_border,
					(lead == choice) ? c_select : c_textbg, false, false, false, false, true);
				paintContent(choice, g, clipx, clipy, clipwidth, clipheight,
					r[2] + 4, r[3] + 2, portwidth - 8, r[1] - 4,
					getBoolean(choice, "enabled", true) ? c_text : c_disable, "left", false);
			}
		}
		else if (("panel" == classname) || ("dialog" == classname)) {
			for (Object comp = get(component, ":comp");
					comp != null; comp = get(comp, ":next")) {
				paint(g, clipx, clipy, clipwidth, clipheight, comp, enabled);
			}
		}
		else { //if (("list" == classname) || ("table" == classname) || ("tree" == classname))
			Object lead = get(component, ":lead");
			int[] columnwidths = ("table" == classname) ? ((int []) get(component, ":widths")) : null;
			boolean line = getBoolean(component, "line", true); int iline =  line ? 1 : 0;
			for (Object item = get(component, ":comp"), next = null; item != null; item = next) {
				if (focus && (lead == null)) {
					set(component, ":lead", lead = item); // draw first item focused when lead is null
				}
				int[] r = getRectangle(item, "bounds");
                                if (r == null) {
                                    r = new Rectangle();
                                    set(item, "bounds", r);
                                }
				if (clipy + clipheight <= r[3]) { break; } // clip rectangle is above
				boolean subnode = false; boolean expanded = false;
				if ("tree" != classname) {
					next = get(item, ":next");
				}
				else {
					subnode = (next = get(item, ":comp")) != null;
					expanded = subnode && getBoolean(item, "expanded", true);
					if (!expanded) {
						for (Object node = item; (node != component) &&
							((next = get(node, ":next")) == null); node = getParent(node));
					}
				}
				if (clipy >= r[3] + r[1] + iline) { continue; } // clip rectangle is bellow
				
				boolean selected = getBoolean(item, "selected", false);
				boolean focused = focus && (lead == item);
				paintRect(g, ("tree" != classname) ? 0 : r[2], r[3],
					("tree" != classname) ? viewwidth : r[0], r[1], c_focus,
					selected ? c_select : c_textbg, focused, focused, focused, focused, true);
				if (line) {
					g.setColor(c_bg);
					g.drawLine(0, r[3] + r[1], viewwidth, r[3] + r[1]);
				}
				if ("table" != classname) {
					boolean itemenabled = enabled && getBoolean(item, "enabled", true);
					paintContent(item, g, clipx, clipy, clipwidth, clipheight,
						r[2] + ITEM.left, r[3] + ITEM.top, viewwidth - ITEM.left - ITEM.right, r[1] - ITEM.top - ITEM.bottom,
						itemenabled ? c_text : c_disable, "left", false);
					if ("tree" == classname) {
						int x = r[2] - block / 2; int y = r[3] + (r[1] - 1) / 2;
						g.setColor(c_bg);
						//g.drawLine(x, r[3], x, y); g.drawLine(x, y, r[2], y);
						if (subnode) {
							paintRect(g, x - 4, y - 4, 9, 9, itemenabled ? c_border : c_disable,
								itemenabled ? c_ctrl : c_bg, true, true, true, true, true);
							g.setColor(itemenabled ? c_text : c_disable);
							g.drawLine(x - 2, y, x + 2, y);
							if (!expanded) { g.drawLine(x, y - 2, x, y + 2); }
						}
					}
				}
				else { // tree
					int i = 0; int x = 0;
					for (Object cell = get(item, ":comp"); cell != null; cell = get(cell, ":next")) {
						if (clipx + clipwidth <= x) { break; }
						int iwidth = columnwidths[i];
						boolean lastcolumn = (i == columnwidths.length - 1);
						if (lastcolumn) { iwidth = Math.max(iwidth, viewwidth - x); }
						if (clipx < x + iwidth) {
							boolean cellenabled = enabled && getBoolean(cell, "enabled", true);
							paintContent(cell, g, clipx, clipy, clipwidth, clipheight,
								r[2] + x + 1, r[3] + 1, iwidth - 2, r[1] - 3,
								cellenabled ? c_text : c_disable, "left", false);
						}
						i++; x += iwidth;
					}
				}
			}
		}
	}

	/**
	 *
	 */
	private void paintRect(Graphics g, int x, int y, int width, int height,
			int border, int bg,
			boolean top, boolean left, boolean bottom, boolean right, boolean horizontal) {
		if ((width <= 0) || (height <= 0)) return;
		g.setColor(border);
		if (top) {
			g.drawLine(x + width - 1, y, x, y);
			y++; height--; if (height <= 0) return;
		}
		if (left) {
			g.drawLine(x, y, x, y + height - 1);
			x++; width--; if (width <= 0) return;
		}
		if (bottom) {
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			height--; if (height <= 0) return;
		}
		if (right) {
			g.drawLine(x + width - 1, y + height - 1, x + width - 1, y);
			width--; if (width <= 0) return;
		}

		if (bg == c_ctrl) {
			if (horizontal) {
				if (height > block) {
					g.setColor(c_bg);
					g.fillRect(x, y, width + evm, height - block + evm);
				}
				for (int i = 0; i < width; i += block) {
					g.drawImage(hgradient, x + i, (height > block) ? (y + height - block) : y,
						x + Math.min(i + block, width) + evm, y + height + evm,
						0, 0, Math.min(block, width - i) + evm, Math.min(block, height) + evm, null);
				}
			}
			else {
				if (width > block) {
					g.setColor(c_bg);
					g.fillRect(x, y, width - block + evm, height + evm);
				}
				for (int i = 0; i < height; i += block) {
					g.drawImage(vgradient, (width > block) ? (x + width - block) : x, y + i,
						x + width + evm, y + Math.min(i + block, height) + evm,
						0, 0, Math.min(block, width) + evm, Math.min(block, height - i) + evm, null);
				}
			}
		}
		else {
			g.setColor(bg);
			g.fillRect(x, y, width + evm, height + evm);
		}
	}

	/**
	 *
	 */
	private void paintArrow(Graphics g, int x, int y, int width, int height,
			char dir, boolean enabled, boolean inside, boolean pressed, String part,
			boolean top, boolean left, boolean bottom, boolean right, boolean horizontal) {
		inside = inside && (insidepart == part);
		pressed = pressed && (pressedpart == part);
		paintRect(g, x, y, width, height, enabled ? c_border : c_disable,
			enabled ? ((inside != pressed) ? c_hover :
				(pressed ? c_press : c_ctrl)) : c_bg,
			top, left, bottom, right, horizontal);
		g.setColor(enabled ? c_text : c_disable);
		paintArrow(g, x + (left ? 1 : 0), y + (top ? 1 : 0),
			width - (left ? 1 : 0) - (right ? 1 : 0), height - (top ? 1 : 0) - (bottom ? 1 : 0), dir);
	}

	/**
	 *
	 */
	private void paintArrow(Graphics g,
			int x, int y, int width, int height, char dir) {
		int cx = x + width / 2 - 2;
		int cy = y + height / 2 - 2;
		for (int i = 0; i < 4; i++) {
			if (dir == 'N') { // north
				g.drawLine(cx + 1 - i, cy + i, cx + 1/*2*/ + i, cy + i);
			}
			else if (dir == 'W') { // west
				g.drawLine(cx + i, cy + 1 - i, cx + i, cy + 1/*2*/ + i);
			}
			else if (dir == 'S') { // south
				g.drawLine(cx + 1 - i, cy + 4 - i, cx + 1/*2*/ + i, cy + 4 - i);
			}
			else { // east
				g.drawLine(cx + 4 - i, cy + 1 - i, cx + 4 - i, cy + 1/*2*/ + i);
			}
		}
	}

	/**
	 * Paint component icon and text (using default or custom font)
	 * @param component
	 * @param g
	 * @param clipx
	 * @param clipy
	 * @param clipwidth
	 * @param clipheight
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param foreground default text color if custom foreground is null
	 * @param defaultalignment
	 * @param checkmnemonic find mnemonic index and underline text
	 */
	private void paintContent(Object component, Graphics g,
			int clipx, int clipy, int clipwidth, int clipheight,
			int x, int y, int width, int height, int foreground, String defaultalignment,
			boolean checkmnemonic) {
		String text = getString(component, "text", null);
		Image icon = getIcon(component, "icon", null);
		if ((text == null) && (icon == null)) { return; }
                boolean link = (getClass(component) == "button") && (getChoice(component, "type") == "link");
		String alignment = getString(component, "alignment", defaultalignment);
		Font customfont = (text != null) ? (Font) get(component, "font") : null;
		if (customfont != null) { g.setFont(customfont); }

		FontMetrics fm = null;
		int tw = 0, th = 0;
		int ta = 0;
		if (text != null) {
			fm = g.getFontMetrics();
			tw = font.stringWidth(text);
			ta = fm.getAscent();
			th = fm.getDescent() + ta;
			int customfg = (Color) get(component, "foreground"); //+no if disabled
                        if (link && customfg == null) customfg = Color.blue;
			g.setColor((customfg != null) ? customfg : foreground);
		}
		int iw = 0, ih = 0;
		if (icon != null) {
			iw = icon.getWidth();
			ih = icon.getHeight();
			if (text != null) { iw += 2; }
		}

		boolean clipped = (tw + iw > width) || (th > height) || (ih > height);
		int cx = x;
		if ("center" == alignment) { cx += (width - tw - iw) / 2; }
			else if ("right" == alignment) { cx += width - tw - iw; }

		if (clipped) { g.clipRect(x, y, width, height); }
		if (icon != null) {
			g.drawImage(icon, cx, y + (height - ih) / 2, this);
			cx += iw;
		}
		if (text != null) {
			int ty = y + (height - th) / 2 + ta;
			g.drawString(text, cx, ty);
                        if (link) g.drawLine(cx, ty + 1, cx + tw, ty + 1);
			if (checkmnemonic) {
				int mnemonic = getInteger(component, "mnemonic", -1);
				if ((mnemonic != -1) && (mnemonic < text.length())) {
					int mx = cx + font.stringWidth(text.substring(0, mnemonic));
					g.drawLine(mx, ty + 1, mx + font.charWidth(text.charAt(mnemonic)), ty + 1);
				}
			}
		}
		if (clipped) { g.setClip(clipx, clipy, clipwidth, clipheight); }
		if (customfont != null) { g.setFont(font); } //restore the default font
	}

	/**
	 * A second thread is used to repeat value change events for scrollbar or spinbox
	 * during the mouse is pressed, or to pop up tooltip
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
						if ((watchdelay == 300L) || (watchdelay == 60L)) {
							if (processScroll(mousepressed, pressedpart)) { setTimer(60L); }
						} else if ((watchdelay == 375L) || (watchdelay == 75L)) {
							if (processSpin(mousepressed, pressedpart)) { setTimer(75L); }
						} else if (watchdelay == 750L) {
							showTip();
						}
					}
				}
			} catch (InterruptedException ie) {} //ie.printStackTrace();
		}
	}

	/**
	 *
	 */
	private void setTimer(long delay) {
		watchdelay = delay;
		if (delay == 0) {
			watch = 0;
		} else {
			long prev = watch;
			watch = System.currentTimeMillis() + delay;
			if (timer == null) {
				timer = new Thread(this);
				timer.setPriority(Thread.MIN_PRIORITY);
				timer.setDaemon(true);
				timer.start();
			}
			if ((prev == 0) || (watch < prev)) {
				synchronized (this) { notify(); }
				//synchronized (this) { try { notify(); }catch (IllegalMonitorStateException imse) {} }
			}
		}
	}

	/**
	 * This component can be traversed using Tab or Shift-Tab keyboard focus traversal,
	 * although 1.4 replaced this method by <i>isFocusable</i>,
	 * so 1.4 compilers write deprecation warning
	 *
	 * @return true as focus-transverable component, overwrites the default false value
	 */
	public boolean isFocusTraversable() {
		return true;
	}

	/**
	 * Dispatches mouse, key, focus, and component events occurring on the
	 * <i>Thinlet</i> component internally
	 */
	protected void processEvent(AWTEvent e) {
		// evm (touchscreen) events: entered/moved/pressed -> dragged -> dragged/released/exited
		int id = e.getID();
		if ((id == MOUSE_ENTERED) || (id == 0) ||
				(id == MOUSE_EXITED) || (id == MOUSE_PRESSED) ||
				(id == MOUSE_DRAGGED) || (id == MOUSE_RELEASED)) {
			MouseEvent me = (MouseEvent) e;
			int x = me.getX();
			int y = me.getY();
			int clickcount = me.getClickCount();
			boolean shiftdown = me.isShiftDown();
			boolean controldown = me.isControlDown();
			boolean popuptrigger = me.isPopupTrigger();
			if (id == MOUSE_ENTERED) {
				if (mousepressed == null) {
					findComponent(content, x, y);
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_ENTERED, mouseinside, insidepart);
				}
			}
			else if (id == 0) {
				Object previnside = mouseinside;
				Object prevpart = insidepart;
				findComponent(content, x, y);
				if ((previnside == mouseinside) && (prevpart == insidepart)) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						0, mouseinside, insidepart);
				}
				else {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_EXITED, previnside, prevpart);
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_ENTERED, mouseinside, insidepart);
				}
			}
			else if (id == MOUSE_EXITED) {
				if (mousepressed == null) {
					Object mouseexit = mouseinside;
					Object exitpart = insidepart;
					mouseinside = insidepart = null;
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_EXITED, mouseexit, exitpart);
				}
			}
			else if (id == MOUSE_PRESSED) {
				if (popupowner != null) {
					String classname = getClass(mouseinside);
					if ((popupowner != mouseinside) &&
							(classname != ":popup") && (classname != ":combolist")) {
						closeup();
					}
				}
				mousepressed = mouseinside;
				pressedpart = insidepart;
				handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
					MOUSE_PRESSED, mousepressed, pressedpart);
			}
			else if (id == MOUSE_DRAGGED) {
				Object previnside = mouseinside;
				Object prevpart = insidepart;
				findComponent(content, x, y);
				boolean same = (previnside == mouseinside) && (prevpart == insidepart);
				boolean isin = (mousepressed == mouseinside) && (pressedpart == insidepart);
				boolean wasin = (mousepressed == previnside) && (pressedpart == prevpart);
				
				if (wasin && !isin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_EXITED, mousepressed, pressedpart);
				}
				else if (!same && (popupowner != null) && !wasin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						DRAG_EXITED, previnside, prevpart);
				}
				if (isin && !wasin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_ENTERED, mousepressed, pressedpart);
				}
				else if (!same && (popupowner != null) && !isin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						DRAG_ENTERED, mouseinside, insidepart);
				}
				if (isin == wasin) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_DRAGGED, mousepressed, pressedpart);
				}
			}
			else if (id == MOUSE_RELEASED) {
				Object mouserelease = mousepressed;
				Object releasepart = pressedpart;
				mousepressed = pressedpart = null;
				handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
					MOUSE_RELEASED, mouserelease, releasepart);
				if ((mouseinside != null) &&
						((mouserelease != mouseinside) || (releasepart != insidepart))) {
					handleMouseEvent(x, y, clickcount, shiftdown, controldown, popuptrigger,
						MOUSE_ENTERED, mouseinside, insidepart);
				}
			}
		}
		else if (id == MOUSE_WHEEL) {
			int[] port = getRectangle(mouseinside, ":port");
			if (port != null) { // is scrollable
				int[] bounds = getRectangle(mouseinside, "bounds");	
				try { // mouse wheel is supported since 1.4 thus it use reflection
					if (wheelrotation == null) {
						wheelrotation = e.getClass().getMethod("getWheelRotation", null);
					}
					int rotation = ((Integer) wheelrotation.invoke(e, null)).intValue();
					
					if (port[2] + port[0] < bounds[0]) { // has vertical scrollbar
						processScroll(mouseinside, (rotation > 0) ? "down" : "up"); //TODO scroll panels too
					}
					else if (port[3] + port[1] < bounds[1]) { // has horizontal scrollbar
						processScroll(mouseinside, (rotation > 0) ? "right" : "left");
					}
				} catch (Exception exc) { /* never */ }
			}
		}
		else if ((id == KeyEvent.KEY_PRESSED) || (id == KeyEvent.KEY_TYPED)) {
			if (focusinside && ((popupowner != null) || (focusowner != null))) {
				hideTip();
				KeyEvent ke = (KeyEvent) e;
				int keychar = ke.getKeyChar();
				boolean control = (keychar <= 0x1f) ||
					((keychar >= 0x7f) && (keychar <= 0x9f)) ||
					(keychar >= 0xffff) || ke.isControlDown();
				int keycode = control ? ke.getKeyCode() : 0;
				
				if ((control == (id == KeyEvent.KEY_PRESSED)) &&
					processKeyPress((popupowner != null) ? popupowner : focusowner,
						ke.isShiftDown(), ke.isControlDown(), ke.getModifiers(),
						control ? 0 : keychar, keycode)) {
					ke.consume();
				}
				else if ((keycode == '\t') ||
						((keycode == 0x75) && (ke.isAltDown() || ke.isControlDown()))) {
					boolean outgo = (keycode == 0x75);
					if (!ke.isShiftDown() ? setNextFocusable(focusowner, outgo) :
							setPreviousFocusable(focusowner, outgo)) {
						ke.consume();
					} else if (MOUSE_WHEEL != 0) { // 1.4
						if (!ke.isShiftDown()) {
							transferFocus();
						}
						else { try {
								getClass().getMethod("transferFocusBackward", null). invoke(this, null);
						} catch (Exception exc) { /* never */ } }
					}
					repaint(focusowner);
					closeup();
				}
				else if (keycode == 0x77) {
					for (Object splitpane = focusowner;
							splitpane != null; splitpane = getParent(splitpane)) {
						if (getClass(splitpane) == "splitpane") {
							setFocus(splitpane); repaint(splitpane); ke.consume(); break; //middle
						}
					}
				}
				else if ((id == KeyEvent.KEY_PRESSED) && ((keychar != 0) || ke.isActionKey()) &&
						checkMnemonic(focusowner, true, null, ke.getKeyCode(), ke.getModifiers())) {
					ke.consume();
				}
			}
		}
		else if (id == FocusEvent.FOCUS_LOST) {
			focusinside = false;
			if (focusowner != null) { repaint(focusowner); }
			closeup();
		}
		else if (id == FocusEvent.FOCUS_GAINED) {
			focusinside = true;
			if (focusowner == null) { setFocus(content); }
				else { repaint(focusowner); }
		}
		else if ((id == ComponentEvent.COMPONENT_RESIZED) ||
				(id == ComponentEvent.COMPONENT_SHOWN)) {
			int[] d = getSize();
			setRectangle(content, "bounds", 0, 0, d[0], d[1]);
			validate(content);
			closeup();
			if (!focusinside)  { requestFocus(); }
		}
	}
	
	/**
	 * Check the previous mouse location again because of a possible layout change
	 */
	private void checkLocation() {
		findComponent(content, mousex, mousey);
		handleMouseEvent(mousex, mousex, 1, false, false, false,
			MOUSE_ENTERED, mouseinside, insidepart);
	}

	/**
	 *
	 */
	private boolean processKeyPress(Object component,
			boolean shiftdown, boolean controldown, int modifiers, int keychar, int keycode) {
		String classname = getClass(component);
		if ("button" == classname) {
			if (keychar == 0x20 ||
					((keycode == '\n') &&
						(getString(component, "type", null) == "default")) ||
					((keycode == 0x1B) && //...
						(getString(component, "type", null) == "cancel"))) {
				//pressedkey = keychar;
				invoke(component, null, "action");
				repaint(component);
				return true;
			}
		}
		else if (("checkbox" == classname) || ("togglebutton" == classname)) {
			if (keychar == 0x20) {
				changeCheck(component, true);
				repaint(component);
				return true;
			}
		}
		else if ("combobox" == classname) {
			Object combolist = get(component, ":combolist");
			if (combolist == null) { // the drop down list is not visible
				boolean editable = getBoolean(component, "editable", true);
				if (editable && processField(component, shiftdown, controldown, modifiers,
							keychar, keycode, false, false)) {
					setInteger(component, "selected", -1, -1);
					return true;
				}
				if ((keychar == 0x20) || (keycode == 0x28)) {
					popupCombo(component);
				}
				//+findText
				else return false;
			}
			else {
				if ((keycode == 0x26) ||
						(keycode == 0x28) || (keycode == 0x21) ||
						(keycode == 0x22) ||
						(keycode == 0x24) || (keycode == 0x23)) {
					Object next = getListItem(component, combolist, keycode,
						get(combolist, ":lead"), false);
					if (next != null) {
						setInside(combolist, next, true);
					}
				}
				else if ((keycode == '\n') || (keychar == 0x20)) {
					closeCombo(component, combolist, get(combolist, ":lead")); //Alt+Up
				}
				else if (keycode == 0x1B) {
					closeCombo(component, combolist, null);
				}
				else if (!processField(component, shiftdown, controldown, modifiers,
						keychar, keycode, false, false)) {
					Object item = findText((char) keychar, component, combolist, false);
					if (item != null) {
						setInside(combolist, item, true);
					}
					else return false;
				}
			}
			return true;
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			return processField(component, shiftdown, controldown, modifiers,
				keychar, keycode, false, ("passwordfield" == classname));
		}
		else if ("textarea" == classname) {
			char[] chars = (char[]) get(component, ":text");
			int start = getInteger(component, "start", 0);
			int end = getInteger(component, "end", 0);

			int istart = start;
			int iend = end;
			String insert = null;
			if ((keycode == 0x24) && !controldown) {
				while ((iend > 0) && (chars[iend - 1] != '\n')) { iend--; }
				if (!shiftdown) { istart = iend; }
			}
			else if ((keycode == 0x23) && !controldown) {
				while ((iend < chars.length) && (chars[iend] != '\n')) { iend++; }
				if (!shiftdown) { istart = iend; }
			}
			else if ((keycode == 0x26) || (keycode == 0x21) ||
					(keycode == 0x28) || (keycode == 0x22)) {
				Font currentfont = (Font) get(component, "font");
				FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
				int fh = font.getHeight();
				int y = 0; int linestart = 0;
				for (int i = 0; i < iend; i++) {
					if ((chars[i] == '\n') || (chars[i] == '\t')) {
						linestart = i + 1; y += fh;
					}
				}
				if (keycode == 0x26) { y -= fh; }
				else if (keycode == 0x28) { y += fh; }
				else {
					int dy = getRectangle(component, ":port")[1];
					y += (keycode == 0x21) ? -dy : dy; // VK_PAGE_DOWN
				}
				int x = font.charsWidth(chars, linestart, iend - linestart);
				iend = getCaretLocation(component, x, y, true, false);
				if (!shiftdown) { istart = iend; }
			}
			else return processField(component, shiftdown, controldown, modifiers,
					keychar, keycode, true, false);
			return changeField(component,
				getString(component, "text", ""), insert, istart, iend, start, end);
		}
		else if ("tabbedpane" == classname) {
			if ((keycode == 0x27) || (keycode == 0x28) ||
					(keycode == 0x25) || (keycode == 0x26)) {
				int selected = getInteger(component, "selected", 0);
				boolean increase = (keycode == 0x27) || (keycode == 0x28);
				int newvalue = selected;
				int n = increase ? getItemCountImpl(component, ":comp") : 0;
				int d = (increase ? 1 : -1);						
				for (int i = selected + d; increase ? (i < n)  : (i >= 0); i += d) {
					if (getBoolean(getItem(component, i), "enabled", true)) {
						newvalue = i; break;
					}	
				}
				if (newvalue != selected) {
					setInteger(component, "selected", newvalue, 0);
                                        String placement = getString(component, "placement", "top");
                                        if (placement == "stacked") doLayout(component);
					checkOffset(component);
					repaint(component);
					invoke(component, getItem(component, newvalue), "action");
				}
			}
		}
		else if ("spinbox" == classname) {
			if ((keycode == 0x26) || (keycode == 0x28)) {
				processSpin(component, (keycode == 0x26)? "up" : "down");
				return true;
			}
			return processField(component, shiftdown, controldown, modifiers,
				keychar, keycode, false, false);
		}
		else if ("slider" == classname) {
			int value = getInteger(component, "value", 0);
			int d = 0;
			if ((keycode == 0x24) || (keycode == 0x25) ||
					(keycode == 0x26) || (keycode == 0x21)) {
				d = getInteger(component, "minimum", 0) - value;
				if ((keycode == 0x25) || (keycode == 0x26)) {
					d = Math.max(d, -getInteger(component, "unit", 5));
				}
				else if (keycode == 0x21) {
					d = Math.max(d, -getInteger(component, "block", 25));
				}
			}
			else if ((keycode == 0x23) || (keycode == 0x27) ||
					(keycode == 0x28) || (keycode == 0x22)) {
				d = getInteger(component, "maximum", 100) - value;
				if ((keycode == 0x27) || (keycode == 0x28)) {
					d = Math.min(d, getInteger(component, "unit", 5));
				}
				else if (keycode == 0x22) {
					d = Math.min(d, getInteger(component, "block", 25));
				}
			}
			if (d != 0) {
				setInteger(component, "value", value + d, 0);
				repaint(component);
				invoke(component, null, "action");
			}
		}
		else if ("splitpane" == classname) {
			int divider = getInteger(component, "divider", -1);
			int d = 0;
			if (keycode == 0x24) {
				d = -divider;
			}
			else if ((keycode == 0x25) || (keycode == 0x26)) {
				d = Math.max(-10, -divider);
			}
			else if ((keycode == 0x23) ||
					(keycode == 0x27) || (keycode == 0x28)) {
				boolean horizontal = ("vertical" != get(component, "orientation"));
				int[] bounds = getRectangle(component, "bounds");
				int max = (horizontal ? bounds[0] : bounds[1]) - 5;				
				d = max - divider;
				if (keycode != 0x23) {
					d = Math.min(d, 10);
				}
			}
			if (d != 0) {
				setInteger(component, "divider", divider + d, -1);
				validate(component);
			}
		}
		else if (("list" == classname) || ("table" == classname)) {
			return processList(component, shiftdown, controldown, keychar, keycode, modifiers, false);
		}
		else if ("tree" == classname) {
			//? clear childs' selection, select this is its 	subnode was selected
			if (keycode == 0x25) {
				Object lead = get(component, ":lead");
				if ((get(lead, ":comp") != null) && getBoolean(lead, "expanded", true)) { // collapse
					setBoolean(lead, "expanded", false, true);
					selectItem(component, lead, true);
					validate(component);
					invoke(component, lead, "collapse"); //lead
					return true;
				}
				else { // select parent
					Object parent = getParent(lead);
					if (parent != component) {
						selectItem(component, parent, true);
						setLead(component, lead, parent);
						return true;
					}
				}
			}
			//? for interval mode select its all subnode or deselect all after
			else if (keycode == 0x27) {
				Object lead = get(component, ":lead");
				Object node = get(lead, ":comp");
				if (node != null) {
					if (getBoolean(lead, "expanded", true)) { // select its first subnode
						selectItem(component, node, true);
						setLead(component, lead, node);
					}
					else { // expand
						setBoolean(lead, "expanded", true, true);
						selectItem(component, lead, true);
						validate(component);
						invoke(component, lead, "expand"); //lead
					}
					return true;
				}
			}
			return processList(component, shiftdown, controldown, keychar, keycode, modifiers, true);
		}
		else if (("menubar" == classname) || ("popupmenu" == classname)) {
			// find the last open :popup and the previous one
			Object previous = null; Object last = null;
			for (Object i = get(component, ":popup");
					i != null; i = get(i, ":popup")) {
				previous = last; last = i;
			}
			//selected is the current item of the last, or the previous :popup, or null
			Object selected = get(last, "selected");
			Object hotpopup = ((selected != null) || (previous == null)) ?
				last : previous;
			if ((selected == null) && (previous != null)) {
				selected = get(previous, "selected");
			}

			if ((keycode == 0x26) || (keycode == 0x28)) {
				Object next = getMenu(hotpopup,
					selected, keycode == 0x28, true);
				if (next != null) {
					set(hotpopup, "selected", null);
					popupMenu(hotpopup);
					set(hotpopup, "selected", next);
					repaint(hotpopup);
				}
			}
			else if (keycode == 0x25) {
				if (previous != null) { // close the last :popup
					selected = get(previous, "selected");
					set(previous, "selected", null);
					popupMenu(previous);
					set(previous, "selected", selected);
					repaint(previous); // , selected
				}
				else if ("menubar" == classname) { // select the previous menubar menu
					Object next = getMenu(component, get(component, "selected"), false, false);
					if (next != null) {
						set(component, "selected", next);
						Object popup = popupMenu(component);
						set(popup, "selected", getMenu(popup, null, true, true));
						repaint(component); // , selected
					}
				}
			}
			else if (keycode == 0x27) {
				if ((previous != null) && (selected == null)) { // ?
					set(last, "selected", get(get(last, "menu"), ":comp"));
					repaint(last); // , selected
				}
				else if ((selected != null) && (getClass(selected) == "menu")) { // expand menu
					Object popup = popupMenu(last);
					set(popup, "selected", getMenu(popup, null, true, true));
				}
				else if ("menubar" == classname) { // select the next menubar menu
					Object next = getMenu(component, get(component, "selected"), true, false);
					if (next != null) {
						set(component, "selected", next);
						Object popup = popupMenu(component);
						set(popup, "selected", getMenu(popup, null, true, true));
						repaint(component); // , selected
					}
				}
			}
			else if ((keycode == '\n') ||
					(keychar == 0x20) || (keycode == 0x1B)) {
				if ((keycode != 0x1B) &&
						getBoolean(selected, "enabled", true)) {
					if ((selected != null) && (getClass(selected) == "checkboxmenuitem")) {
						changeCheck(selected, false);
					}
					else invoke(selected, null, "action");
				}
				closeup();
			}
			else return false;
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	private boolean changeCheck(Object component, boolean box) {
		String group = getString(component, "group", null);
		if (group != null) {
			if (getBoolean(component, "selected", false)) { return false; }
			for (Object comp = get(getParent(component), ":comp");
					comp != null; comp = get(comp, ":next")) {
				if (comp == component) {
					setBoolean(component, "selected", true);
				}
				else if (group.equals(get(comp, "group")) &&
						getBoolean(comp, "selected", false)) {
					setBoolean(comp, "selected", false);
					if (box) { repaint(comp); } //checkbox only
				}
			}
		}
		else {
			setBoolean(component, "selected",
				!getBoolean(component, "selected", false), false);
		}
		invoke(component, null, "action");
		return true;
	}

	/**
	 * @param component a :popup or a menubar
	 * @param part the currently selected item, return the first/last if null
	 * @param forward find the next item if true, the previous otherwise
	 * @param popup the given component is :popup if true, menubar otherwise
	 * @return the next/previous item relative to the current one excluding separators, or null
	 */
	private Object getMenu(Object component, Object part,
			boolean forward, boolean popup) {
		Object previous = null;
		for (int i = 0; i < 2; i++) { // 0: next to last, 1: first to previous
			for (Object item = (i == 0) ? get(part, ":next") :
						get(popup ? get(component, "menu") : component, ":comp");
					(i == 0) ? (item != null) : (item != part); item = get(item, ":next")) {
				if ((getClass(item) != "separator") && getBoolean(item, "enabled", true)) {
					if (forward) { return item; }
					previous = item;
				}
			}
		}
		return previous;
	}

	/**
	 *
	 */
	private boolean processField(Object component,
			boolean shiftdown, boolean controldown, int modifiers,
			int keychar, int keycode, boolean multiline, boolean hidden) {
		String text = getString(component, "text", ""); 
		int start = getInteger(component, "start", 0);
		int end = getInteger(component, "end", 0);
		boolean editable = getBoolean(component, "editable", true);

		int istart = start;
		int iend = end;
		String insert = null;
		if (editable && (keychar != 0) &&
			( (modifiers == 0) ||
			  (modifiers == InputEvent.SHIFT_MASK) ||
			  (modifiers == InputEvent.ALT_GRAPH_MASK))) {
			insert = String.valueOf((char) keychar);
		}
		else if (editable && (keycode == '\n')) {
			if (multiline) { insert = "\n"; }
				else { return invoke(component, null, "perform"); }
		}
		else if (editable && (keycode == '\b')) {
			insert = "";
			if (start == end) { istart -= 1; }
		}
		else if (keycode == 0x23) {
			iend = text.length();
			if (!shiftdown) { istart = iend; }
		}
		else if (keycode == 0x24) {
			iend = 0;
			if (!shiftdown) { istart = iend; }
		}
		else if (keycode == 0x25) {
			if (controldown) {
				for (int i = 0; i < 2; i++) {
					while ((iend > 0) && ((i != 0) ==
						Character.isLetterOrDigit(text.charAt(iend - 1)))) { iend--; }	
				}
			} else {
				iend -= 1;
			}
			if (!shiftdown) { istart = iend; }
		}
		else if (keycode == 0x27) {
			if (controldown) {
				for (int i = 0; i < 2; i++) {
					while ((iend < text.length()) && ((i == 0) ==
						Character.isLetterOrDigit(text.charAt(iend)))) { iend++; }
				}
			} else {
				iend += 1;
			}
			if (!shiftdown) { istart = iend; }
		}
		else if (editable && (keycode == 0x7F)) {
			insert = "";
			if (start == end) { iend += 1; }
		}
		else if (controldown &&
				((keycode == 0x41) || (keycode == 0xBF))) {
			istart = 0; // KeyEvent.VK_SLASH
			iend = text.length();
		}
		else if (controldown && (keycode == 0xDC)) {
			istart = iend = text.length(); // KeyEvent.VK_BACK_SLASH
		}
		else if ((editable && !hidden && controldown && (keycode == 0x58)) ||
				(!hidden && controldown && (keycode == 0x43))) {
			if (start != end) {
				clipboard = text.substring(
					Math.min(start, end), Math.max(start, end));
				try {				
					getToolkit().getSystemClipboard().setContents(
						new StringSelection(clipboard), null);
				} catch (Exception exc) {}
				if (keycode == 0x58) { insert = ""; } else { return true; }
			}
		}
		else if (editable && controldown && (keycode == 0x56)) {
			try {
				insert = (String) getToolkit().getSystemClipboard().
					getContents(this).getTransferData(DataFlavor.stringFlavor);
			} catch (Exception exc) {
				insert = clipboard;
			}
			if (insert != null) { // no text on system clipboard nor internal clipboard text
				insert = filter(insert, multiline);
			}
		}
		return changeField(component, text, insert, istart, iend, start, end);
	}
	
	/**
	 * @param text
	 * @param multiline
	 * @return
	 */
	private static String filter(String text, boolean multiline) {
		StringBuffer filtered = new StringBuffer(text.length());
		for (int i = 0; i < text.length(); i++) {
			char ckey = text.charAt(i);
			if (((ckey > 0x1f) && (ckey < 0x7f)) ||
					((ckey > 0x9f) && (ckey < 0xffff)) ||
					(multiline && (ckey == '\n'))) {
				filtered.append(ckey);
			}
		}
		return (filtered.length() != text.length()) ? filtered.toString() : text;
	}

	/**
	 * @param component a textfield, passwordfield, textarea, combobox, or spinbox
	 * @param text current text
	 * @param insert a string to replace thr current selection 
	 * @param movestart new selection start position
	 * @param moveend new caret (selection end) position
	 * @param start current selection start position
	 * @param end current caret position
	 * @return true if selection, caret location, or text content changed
	 */
	private boolean changeField(Object component, String text, String insert,
			int movestart, int moveend, int start, int end) {
		movestart = Math.max(0, Math.min(movestart, text.length()));
		moveend = Math.max(0, Math.min(moveend, text.length()));
		if ((insert == null) && (start == movestart) && (end == moveend)) {
			return false;
		}
		if (insert != null) {
			int min = Math.min(movestart, moveend);
			set(component, "text", text.substring(0, min) + insert +
				text.substring(Math.max(movestart, moveend)));
			movestart = moveend = min + insert.length();
			invoke(component, null, "action"); // deprecated
		}
		if (start != movestart) { setInteger(component, "start", movestart, 0); }
		if (end != moveend) { setInteger(component, "end", moveend, 0); }
		validate(component);
		invoke(component, null, (insert != null) ?
			((insert.length() > 0) ? "insert" : "remove") : "caret");
		return true;
	}

	/**
	 *
	 */
	private boolean processList(Object component, boolean shiftdown, boolean controldown,
			int keychar, int keycode, int modifiers, boolean recursive) {
		if ((keycode == 0x26) || // select previous/next/first/... item
				(keycode == 0x28) || (keycode == 0x21) ||
				(keycode == 0x22) ||
				(keycode == 0x24) || (keycode == 0x23)) {
			Object lead = get(component, ":lead");
			Object row = getListItem(component, component, keycode, lead, recursive);
			if (row != null) {
				String selection = getString(component, "selection", "single");
				if (shiftdown && (selection != "single") && (lead != null)) {
					extend(component, lead, row, recursive);
				}
				else if (!controldown) {
					selectItem(component, row, recursive);
				}
				setLead(component, lead, row);
				return true;
			}
		}
		else if (keycode == 0x25) {
			processScroll(component, "left");
			return true;
		}
		else if (keycode == 0x27) {
			processScroll(component, "right");
			return true;
		}
		else if (keychar == 0x20) { // select the current item
			select(component, get(component, ":lead"), recursive, shiftdown, controldown); //...
			return true;
		}
		else if (controldown) {
			if (((keycode == 0x41) || (keycode == 0xBF)) && //KeyEvent.VK_SLASH
					(getString(component, "selection", "single") != "single")) { // select all
				selectAll(component, true, recursive);
				return true;
			}
			else if (keycode == 0xDC) { //KeyEvent.VK_BACK_SLASH // deselect all
				selectAll(component, false, recursive);
				return true;
			}
		}
		else {
			Object item = findText((char) keychar, component, component, recursive);
			if (item != null) {
				select(component, item, recursive, false, false);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Search for the next/first appropriate item starting with the collected string
	 * or the given single character
	 * @param keychar the last typed character
	 * @param component a list, tree, table, or combobox
	 * @param leadowner the list, tree, table, or the combobox's drop down list
	 * @param recursive if the component is a tree
	 * @return the appropriate item or null
	 */
	private Object findText(char keychar, Object component,
			Object leadowner, boolean recursive) {
		if (keychar != 0) {
			long current = System.currentTimeMillis();
			int i = (current > findtime + 1000) ? 1 : 0; // clear the starting string after a second
			findtime = current;
			Object lead = get(leadowner, ":lead");
			for (; i < 2; i++) { // 0: find the long text, 1: the stating character only
				findprefix = (i == 0) ? (findprefix + keychar) : String.valueOf(keychar);
				for (int j = 0; j < 2; j++) { // 0: lead to last, 1: first to lead
					for (Object item = (j == 0) ? ((i == 0) ? lead : getNextItem(component, lead, recursive)) :
							get(component, ":comp"); (j == 0) ? (item != null) : (item != lead);
							item = getNextItem(component, item, recursive)) {
						if (getString(item, "text", "").regionMatches(true,
								0, findprefix, 0, findprefix.length())) { //table first column...
							return item;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 *
	 */
	private Object getListItem(Object component, Object scrollpane,
			int keycode, Object lead, boolean recursive) {
		Object row = null;
		if (keycode == 0x26) {
			for (Object prev = get(component, ":comp"); prev != lead;
					prev = getNextItem(component, prev, recursive)) {
				row = prev; // component -> getParent(lead)
			}
		}
		else if (keycode == 0x28) {
			row = (lead == null) ? get(component, ":comp") :
				getNextItem(component, lead, recursive);
		}
		else if ((keycode == 0x21) ||
				(keycode == 0x22)) {
			int[] view = getRectangle(scrollpane, ":view");
			int[] port = getRectangle(scrollpane, ":port");
			int[] rl = (lead != null) ? getRectangle(lead, "bounds") : null;
			int vy = (keycode == 0x21) ?
				view[3] : (view[3] + port[1]);
			if ((keycode == 0x21) &&
					(rl != null) && (rl[3] <= view[3])) {
				vy -= port[1];
			}
			if ((keycode == 0x22) &&
					(rl != null) && (rl[3] + rl[1] >= view[3] + port[1])) {
				vy += port[1];
			}
			for (Object item = get(component, ":comp"); item != null;
					item = getNextItem(component, item, recursive)) {
				int[] r = getRectangle(item, "bounds");
				if (keycode == 0x21) {
					row = item;
					if (r[3] + r[1] > vy) { break; }
				} else {
					if (r[3] > vy) { break; }
					row = item;
				}
			}
		}
		else if (keycode == 0x24) {
			row = get(component, ":comp");
		}
		else if (keycode == 0x23) {
			for (Object last = lead; last != null;
					last = getNextItem(component, last, recursive)) {
				row = last;
			}
		}
		return row;
	}

	/**
	 * Select all the items
	 * @param component a list/tree/table
	 * @param selected selects or deselects items
	 * @param recursive true for tree
	 */
	private void selectAll(Object component,
			boolean selected, boolean recursive) {
		boolean changed = false;
		for (Object item = get(component, ":comp");
				item != null; item = getNextItem(component, item, recursive)) {
			if (setBoolean(item, "selected", selected, false)) {
				repaint(component, null, item); changed = true;
			}
		}
		set(component, ":anchor", null);
		if (changed) { invoke(component, null, "action"); }
	}

	/**
	 * Select a single given item, deselect others
	 * @param component a list/tree/table
	 * @param row the item/node/row to select
	 * @param recursive true for tree
	 */
	private void selectItem(Object component, Object row, boolean recursive) {
		boolean changed = false;
		for (Object item = get(component, ":comp");
				item != null; item = getNextItem(component, item, recursive)) {
			if (setBoolean(item, "selected", (item == row), false)) {
				repaint(component, null, item); changed = true;
			}
		}
		set(component, ":anchor", null);
		if (changed) { invoke(component, row, "action"); }
	}

	/**
	 *
	 */
	private void extend(Object component, Object lead,
			Object row, boolean recursive) {
		Object anchor = get(component, ":anchor");
		if (anchor == null) { set(component, ":anchor", anchor = lead); }
		char select = 'n'; boolean changed = false;
		for (Object item = get(component, ":comp"); // anchor - row
				item != null; item = getNextItem(component, item, recursive)) {
			if (item == anchor) select = (select == 'n') ? 'y' : 'r';
			if (item == row) select = (select == 'n') ? 'y' : 'r';
			if (setBoolean(item, "selected", (select != 'n'), false)) {
				repaint(component, null, item); changed = true;
			}
			if (select == 'r') select = 'n';
		}
		if (changed) { invoke(component, row, "action"); }
	}

	/**
	 * Update the lead item of a list/tree/table, repaint, and scroll
	 * @param component a list, tree, or table
	 * @param oldlead the current lead item
	 * @param lead the new lead item
	 */
	private void setLead(Object component, Object oldlead, Object lead) {
		if (oldlead != lead) { //?
			if (oldlead != null) { repaint(component, null, oldlead); }
			set(component, ":lead", lead);
			repaint(component, null, lead);
			
			int[] r = getRectangle(lead, "bounds");
			scrollToVisible(component, r[2], r[3], 0, r[1]);
		}
	}

	/**
	 * Update the lead item of a combolist, repaint, and scroll
	 * @param component a combobox drop down list
	 * @param part the current hotspot item
	 * @param scroll scroll to the part if true
	 */
	private void setInside(Object component, Object part, boolean scroll) {
			Object previous = get(component, ":lead");
			if (previous != null) {
				repaint(component, ":combolist", previous);
			}
			set(component, ":lead", part);
			if (part != null) {
				repaint(component, ":combolist", part);
				if (scroll) {
					int[] r = getRectangle(part, "bounds");
					scrollToVisible(component, r[2], r[3], 0, r[1]);
				}
			}
	}
			
	/**
	 * @param x mouse x position relative to thinlet component
	 * @param y mouse y position relative to the main desktop
	 */
	private void handleMouseEvent(int x, int y, int clickcount,
			boolean shiftdown, boolean controldown, boolean popuptrigger,
			int id, Object component, Object part) {
		if (id == MOUSE_ENTERED) {
			setTimer(750L);
		}
		else if (id == MOUSE_EXITED || id == MOUSE_PRESSED) {
			hideTip();
		}
		if (!getBoolean(component, "enabled", true)) { return; }
		String classname = getClass(component);
		if (("button" == classname) ||
				("checkbox" == classname) || ("togglebutton" == classname)) {
			if ((id == MOUSE_ENTERED) ||
					(id == MOUSE_EXITED) ||
					(id == MOUSE_PRESSED) ||
					(id == MOUSE_RELEASED)) {
				if (id == MOUSE_PRESSED) {
					setFocus(component);
				}
				else if ((id == MOUSE_RELEASED) &&
						(mouseinside == component)) {
					if ("button" != classname) {
						changeCheck(component, true);
					}
					else invoke(component, null, "action");
				}
				// hand cursor for links ...
				else if ( "button" == classname && (getChoice(component, "type") == "link") ) {
					if ((id == MOUSE_ENTERED) && (mousepressed == null) ) {
						setCursor(Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
					}
					else if (((id == MOUSE_EXITED) && (mousepressed == null)) ||
							((id == MOUSE_RELEASED) && (mouseinside != component))) {
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
				repaint(component);
			}
		}
		else if ("combobox" == classname) {
			boolean editable = getBoolean(component, "editable", true);
			if (editable && (part == null)) { // textfield area
				Image icon = null;
				int left = ((id == MOUSE_PRESSED) &&
					((icon = getIcon(component, "icon", null)) != null)) ?
						icon.getWidth() : 0;
				processField(x, y, clickcount, id, component, part, false, false, left);
			}
			else if (part != "icon") { // part = "down"
				if (((id == MOUSE_ENTERED) ||
						(id == MOUSE_EXITED)) && (mousepressed == null)) {
					if (editable) { repaint(component, "combobox", part); } // hover the arrow button
						else { repaint(component); } // hover the whole combobox
				}
				else if (id == MOUSE_PRESSED) {
					Object combolist = get(component, ":combolist");
					if (combolist == null) { // combolist is closed
						setFocus(component);
						repaint(component);
						popupCombo(component);
					} else { // combolist is visible
						closeCombo(component, combolist, null);
					}
				}
				else if (id == MOUSE_RELEASED) {
					if (mouseinside != component) {
						Object combolist = get(component, ":combolist");
						closeCombo(component, combolist,
							((mouseinside == combolist) && (insidepart instanceof Object[])) ? insidepart : null);
					} else {
						repaint(component);
					}
				}
			}
		}
		else if (":combolist" == classname) {
			if (!processScroll(x, y, id, component, part)) {
				if ((id == MOUSE_ENTERED) || (id == DRAG_ENTERED)) {
					if (part != null) { //+ scroll if dragged
						setInside(component, part, false);
					}
				}
				else if (id == MOUSE_RELEASED) {
					closeCombo(get(component, "combobox"), component, part);
				}
			}
		}
		else if (("textfield" == classname) || ("passwordfield" == classname)) {
			processField(x, y, clickcount, id, component, part,
				false, ("passwordfield" == classname), 0);
		}
		else if ("textarea" == classname) {
			if (!processScroll(x, y, id, component, part)) {
				processField(x, y, clickcount, id, component, part, true, false, 0);
			}
		}
		else if ("panel" == classname) {
			processScroll(x, y, id, component, part);
		}
		else if ("desktop" == classname) {
			if (part == "modal") {
				if (id == MOUSE_ENTERED) {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
				else if (id == MOUSE_EXITED) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
		else if ("spinbox" == classname) {
			if (part == null) {
				processField(x, y, clickcount, id, component, part, false, false, 0);
			}
			else { // part = "up" || "down"
				if ((id == MOUSE_ENTERED) ||
						(id == MOUSE_EXITED) ||
						(id == MOUSE_PRESSED) ||
						(id == MOUSE_RELEASED)) {
					if (id == MOUSE_PRESSED) {
						setFocus(component);
						if (processSpin(component, part)) { setTimer(375L); }
						//settext: start end selection, parse exception...
					}
					else {
						if (id == MOUSE_RELEASED) {
							setTimer(0L);
						}
					}
					repaint(component, classname, part);
				}
			}
		}
		else if ("tabbedpane" == classname) {
			if ((id == MOUSE_ENTERED) ||
					(id == MOUSE_EXITED)) {
				if ((part != null) && getBoolean(part, "enabled", true) &&
						(getInteger(component, "selected", 0) != getIndex(component, part))) {
					repaint(component, "tabbedpane", part);
				}
			}
			else if ((part != null) && (id == MOUSE_PRESSED) &&
					getBoolean(part, "enabled", true)) {
				int selected = getInteger(component, "selected", 0);
				int current = getIndex(component, part);
				if (selected == current) {
					setFocus(component);
					repaint(component, "tabbedpane", part);
				}
				else {
					setInteger(component, "selected", current, 0);
                                        String placement = getString(component, "placement", "top");
                                        if (placement == "stacked") doLayout(component);
					//Object tabcontent = getItem(component, current);
					//setFocus((tabcontent != null) ? tabcontent : component);
					setNextFocusable(component, false);
					checkOffset(component);
					repaint(component);
					invoke(component, part, "action");
				}
			}
		}
		else if ("slider" == classname) {
			if ((id == MOUSE_PRESSED) ||
					(id == MOUSE_DRAGGED)) {
				if (id == MOUSE_PRESSED) {
					setReference(component, block / 2, block / 2);
					setFocus(component);
				}
				int minimum = getInteger(component, "minimum", 0);
				int maximum = getInteger(component, "maximum", 100);
				int value = getInteger(component, "value", 50);
				int[] bounds = getRectangle(component, "bounds");
				boolean horizontal = ("vertical" != get(component, "orientation"));
				int newvalue = minimum +
					(horizontal ? (x - referencex) : (y - referencey)) *
					(maximum - minimum) /
					((horizontal ? bounds[0] : bounds[1]) - block); //... +0.5
				newvalue = Math.max(minimum, Math.min(newvalue, maximum));
				if (value != newvalue) {
					setInteger(component, "value", newvalue, 50);
					invoke(component, null, "action");
				}
				if ((value != newvalue) || (id == MOUSE_PRESSED)) {
					repaint(component);
				}
			}
		}
		else if ("splitpane" == classname) {
			if (id == MOUSE_PRESSED) {
				setReference(component, 2, 2);
			}
			else if (id == MOUSE_DRAGGED) {
				int divider = getInteger(component, "divider", -1);
				boolean horizontal = ("vertical" != get(component, "orientation"));
				int moveto = horizontal ? (x - referencex) : (y - referencey);
				int[] bounds = getRectangle(component, "bounds");
				moveto = Math.max(0, Math.min(moveto,
					Math.abs(horizontal ? bounds[0] : bounds[1]) - 5));
				if (divider != moveto) {
					setInteger(component, "divider", moveto, -1);
					validate(component);
				}
			}
			else if ((id == MOUSE_ENTERED) && (mousepressed == null)) {
				boolean horizontal = ("vertical" != get(component, "orientation"));
				setCursor(Cursor.getPredefinedCursor(horizontal ?
					Cursor.E_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR));
			}
			else if (((id == MOUSE_EXITED) && (mousepressed == null)) ||
					((id == MOUSE_RELEASED) && (mouseinside != component))) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
		else if (("list" == classname) ||
				("table" == classname) || ("tree" == classname)) {
			if (!processScroll(x, y, id, component, part)) {
				if (((id == MOUSE_PRESSED)||
						((id == MOUSE_DRAGGED) &&
							!shiftdown && !controldown))) { 
					//int[] view = getRectangle(component, ":view");
					int[] port = getRectangle(component, ":port");
					int my = y + port[3] - referencey;
					for (Object item = get(component, ":comp"); item != null;) {
						int[] r = getRectangle(item, "bounds");
						if (my < r[3] + r[1]) {
							if (id == MOUSE_DRAGGED) { //!!!
								scrollToVisible(component, r[2], r[3], 0, r[1]);
							}
							else if ("tree" == classname) {
								int mx = x + port[2] - referencex;
								if (mx < r[2]) {
									if ((mx >= r[2] - block) && (get(item, ":comp") != null)) {
										boolean expanded = getBoolean(item, "expanded", true);
										setBoolean(item, "expanded", !expanded, 	true);
										selectItem(component, item, true);
										setLead(component, get(component, ":lead"), item);
										setFocus(component);
										validate(component);
										invoke(component, item, expanded ? "collapse" : "expand"); //item
									}
									break;
								}
							}
							if ((id != MOUSE_DRAGGED) ||
									!getBoolean(item, "selected", false)) {
								if (id != MOUSE_DRAGGED) {
									if (setFocus(component)) { repaint(component, classname, item); } //?
								}
								select(component, item, ("tree" == classname), shiftdown, controldown);
								if (clickcount == 2) { invoke(component, item, "perform"); }
							}
							break;
						}
						item = getNextItem(component, item, ("tree" == classname));
					}
				}
		 	}
		}
		else if ("menubar" == classname) {
			Object selected = get(component, "selected");
			if (((id == MOUSE_ENTERED) || (id == MOUSE_EXITED)) &&
					(part != null) && (selected == null) && getBoolean(part, "enabled", true)) {
				repaint(component, classname, part);
			}
			else if ((part != null) && ((selected == null) ?
					(id == MOUSE_PRESSED) :
						((id == MOUSE_ENTERED) || (id == DRAG_ENTERED))) &&
					getBoolean(part, "enabled", true)) {
					// || ((id == MOUSE_PRESSED) && (insidepart != part))
				set(component, "selected", part);
				popupMenu(component);
				repaint(component, classname, part);
			}
			else if ((id == MOUSE_PRESSED) && (selected != null)) {
				closeup();
			}
			else if (id == MOUSE_RELEASED) {
				if ((part != insidepart) && ((insidepart == null) ||
						((insidepart instanceof Object[]) && (getClass(insidepart) != "menu")))) {
					if ((insidepart != null) && getBoolean(insidepart, "enabled", true)) {
						if (getClass(insidepart) == "checkboxmenuitem") {
							changeCheck(insidepart, false);
						}
						else invoke(insidepart, null, "action");
					}
					closeup();
				}
			}
		}
		else if (":popup" == classname) {
			if (part != null) {
				if (((id == MOUSE_ENTERED) || (id == DRAG_ENTERED)) &&
						getBoolean(part, "enabled", true)) {
					set(component, "selected", part);
					popupMenu(component);
					repaint(component, classname, part);
				}
				else if (id == MOUSE_RELEASED) {
					if ((insidepart == null) || (getClass(insidepart) != "menu")) {
						if ((insidepart != null) && getBoolean(insidepart, "enabled", true)) {
							if (getClass(insidepart) == "checkboxmenuitem") {
								changeCheck(insidepart, false);
							}
							else invoke(insidepart, null, "action");
						}
						closeup();
					}
				}
				else if (((id == MOUSE_EXITED) || (id == DRAG_EXITED)) &&
						getBoolean(part, "enabled", true)) {
					if (getClass(part) != "menu") {
						set(component, "selected", null);
					}
					repaint(component, classname, part);
				}
			}
		}
		else if ("dialog" == classname) {
			if (part == "header") {
				if (id == MOUSE_PRESSED) {
					int[] bounds = getRectangle(component, "bounds");
					referencex = x - bounds[2]; referencey = y - bounds[3];
					Object parent = getParent(component);
					if (get(parent, ":comp") != component) { // to front
						removeItemImpl(parent, component);
						insertItem(parent, ":comp", component, 0);
						set(component, ":parent", parent);
						repaint(component); // to front always...
						setNextFocusable(component, false);
					}
				}
				else if (id == MOUSE_DRAGGED) {
					int[] bounds = getRectangle(component, "bounds");
					int[] parents = getRectangle(getParent(component), "bounds");
					//int mx = Math.max(5 - bounds[0], Math.min(x - referencex, parents[0] - 5));
					//int my = Math.max(5 - bounds[1], Math.min(y - referencey, parents[1] - 5));
					int mx = Math.max(0, Math.min(x - referencex, parents[0] - bounds[0]));
					int my = Math.max(0, Math.min(y - referencey, parents[1] - bounds[1]));
					if ((bounds[2] != mx) || (bounds[3] != my)) {
						// repaint the union of the previous and next bounds
						repaint(component, Math.min(bounds[2], mx), Math.min(bounds[3], my),
							bounds[0] + Math.abs(mx - bounds[2]), bounds[1] + Math.abs(my - bounds[3]));
						bounds[2] = mx; bounds[3] = my;
					}
				}
			}
			else {
				processScroll(x, y, id, component, part);
			}
		}
		
		if (popuptrigger) {// && (id == MOUSE_RELEASED)) {
			Object popupmenu = get(component, "popupmenu");
			if (popupmenu != null) {
				popupPopup(popupmenu, x, y);
			}
		}
	}

	/**
	 * Calculate the given point in a component relative to the thinlet desktop and
	 * set as reference value
	 * @param component a widget
	 * @param x reference point relative to the component left edge 
	 * @param y relative to the top edge
	 */
	private void setReference(Object component, int x, int y) {
		referencex = x; referencey = y;
		for (; component != null; component = getParent(component)) {
			int[] bounds = getRectangle(component, "bounds");
			referencex += bounds[2]; referencey += bounds[3];
			
			int[] port = getRectangle(component, ":port");
			if (port != null) { // content scrolled
					int[] view = getRectangle(component, ":view");
					referencex -= view[2] - port[2]; referencey -= view[3] - port[3];
				}
		}
	}

	/**
	 *
	 */
	private void select(Object component, Object row,
			boolean recursive, boolean shiftdown, boolean controldown) {
		String selection = getString(component, "selection", "single");
		Object lead = null;
		if (shiftdown && (selection != "single") &&
				((lead = get(component, ":lead")) != null)) {
			extend(component, lead, row, recursive);
		}
		else {
			if (controldown && (selection == "multiple")) {
				setBoolean(row, "selected",
					!getBoolean(row, "selected", false), false);
				repaint(component, null, row);
				invoke(component, row, "action");
				set(component, ":anchor", null);
			}
			else if (controldown && getBoolean(row, "selected", false)) {
				for (Object item = row;
						item != null; item = getNextItem(component, item, recursive)) {
					if (setBoolean(item, "selected", false, false)) {
						repaint(component, null, item);
					}
				}
				invoke(component, row, "action");
				set(component, ":anchor", null);
			}
			else {
				selectItem(component, row, recursive);
			}
		}
		setLead(component, (lead != null) ? lead : get(component, ":lead"), row);
	}

	/**
	 * Find the next item after the given
	 * @param component a list/tree/table widget
	 * @param item the next item after this, or the first if null
	 * @param recursive true if tree
	 * @return next (or first) item
	 */
	private Object getNextItem(Object component,
			Object item, boolean recursive) {
		if (!recursive) { return get(item, ":next"); }
		Object next = get(item, ":comp");
		if ((next == null) || !getBoolean(item, "expanded", true)) {
			while ((item != component) && ((next = get(item, ":next")) == null)) {
				item = getParent(item);
			}
		}
		return next;
	}
	
	/**
	 *
	 */
	private void processField(int x, int y, int clickcount,
			int id, Object component,
			Object part, boolean multiline, boolean hidden, int left) {
		if (id == MOUSE_PRESSED) {
			//+ middle=alt paste clipboard content
			setReference(component, 2 + left, 2);
			int mx = x - referencex;
			int my = 0;
			if (!multiline) {
				mx += getInteger(component, ":offset", 0);
			} else {
				int[] view = getRectangle(component, ":view");
				mx += view[2] - 1;
				my = y - referencey + view[3] - 1;
			}
			int caretstart = getCaretLocation(component, mx, my, multiline, hidden);
			int caretend = caretstart;
			if (clickcount > 1) {
				String text = getString(component, "text", "");
				while ((caretstart > 0) && ((clickcount == 2) ?
					Character.isLetterOrDigit(text.charAt(caretstart - 1)) :
						(text.charAt(caretstart - 1) != '\n'))) { caretstart--; }
				while ((caretend < text.length()) && ((clickcount == 2) ?
					Character.isLetterOrDigit(text.charAt(caretend)) :
						(text.charAt(caretend) != '\n'))) { caretend++; }
			}
			setInteger(component, "start", caretstart, 0);
			setInteger(component, "end", caretend, 0);
			setFocus(component);
			validate(component); // caret check only
		}
		else if (id == MOUSE_DRAGGED) {
			int mx = x - referencex;
			int my = 0;
			if (!multiline) {
				mx += getInteger(component, ":offset", 0);
			} else {
				int[] view = getRectangle(component, ":view");
				mx += view[2] - 1;
				my = y - referencey + view[3] - 1;
			}
			int dragcaret = getCaretLocation(component, mx, my, multiline, hidden);
			if (dragcaret != getInteger(component, "end", 0)) {
				setInteger(component, "end", dragcaret, 0);
				validate(component); // caret check only
			}
		}
		else if ((id == MOUSE_ENTERED) && (mousepressed == null)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}
		else if (((id == MOUSE_EXITED) && (mousepressed == null)) ||
			((id == MOUSE_RELEASED) &&
				((mouseinside != component) || (insidepart != null)))) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/**
	 *
	 */
	private int getCaretLocation(Object component,
			int x, int y, boolean multiline, boolean hidden) {
		Font currentfont = (Font) get(component, "font");
		FontMetrics fm = getFontMetrics((currentfont != null) ? currentfont : font);
		char[] chars = multiline ? ((char[]) get(component, ":text")) :
			getString(component, "text", "").toCharArray(); // update it
		int linestart = 0;
		if (multiline) {
			int height = font.getHeight(); // find the line start by y value
			for (int i = 0; (y >= height) && (i < chars.length); i++) {
				if ((chars[i] == '\n') || (chars[i] == '\t')) {
					linestart = i + 1; y -= height;
				}
			}
		}
		for (int i = linestart; i < chars.length; i++) {
			if ((chars[i] == '\n') || (chars[i] == '\t')) { return i; }
			int charwidth = font.charWidth(hidden ? '*' : chars[i]);
			if (x <= (charwidth / 2)) { return i; }
			x -= charwidth;
		}
		return chars.length;
	}

	/**
	 *
	 */
	private boolean processScroll(int x, int y,
			int id, Object component, Object part) {
		if ((part == "up") || (part == "down") ||
				(part == "left") || (part == "right")) {
			if ((id == MOUSE_ENTERED) ||
					(id == MOUSE_EXITED) ||
					(id == MOUSE_PRESSED) ||
					(id == MOUSE_RELEASED)) {
				if (id == MOUSE_PRESSED) {
					if (processScroll(component, part)) {
						setTimer(300L); return true;
					}
				}
				else {
					if (id == MOUSE_RELEASED) { setTimer(0L); }
					repaint(component, null, part);
				}
			}
		}
		else if ((part == "uptrack") || (part == "downtrack") ||
				(part == "lefttrack") || (part == "righttrack")) {
			if (id == MOUSE_PRESSED) {
				if (processScroll(component, part)) {
					setTimer(300L);
				}
			}
			else if (id == MOUSE_RELEASED) {
				setTimer(0L);
			}
		}
		else if ((part == "vknob") || (part == "hknob")) {
			if (id == MOUSE_PRESSED) {
				int[] port = getRectangle(component, ":port");
				int[] view = getRectangle(component, ":view");
				if (part == "hknob") {
					referencex = x - view[2] * (port[0] - 2 * block) / view[0];
				} else {
					referencey = y - view[3] * (port[1] - 2 * block) / view[1];
				}
			}
			else if (id == MOUSE_DRAGGED) {
				int[] port = getRectangle(component, ":port");
				int[] view = getRectangle(component, ":view");
				if (part == "hknob") {
					int viewx = (x - referencex) * view[0] / (port[0] - 2 * block);
					viewx = Math.max(0, Math.min(viewx, view[0] - port[0]));
					if (view[2] != viewx) {
						view[2] = viewx;
						repaint(component, null, "horizontal");
                        			// XXX needed for tables, because table headers are not included in the
                        			// XXX repaint area. Needs to be fixed in repaint(Object, Object, Object).
						repaint(component);
					}
				}
				else { // (part == "vknob")
					int viewy = (y - referencey) * view[1] / (port[1] - 2 * block);
					viewy = Math.max(0, Math.min(viewy, view[1] - port[1]));
					if (view[3] != viewy) {
						view[3] = viewy;
						repaint(component, null, "vertical");
					}
				}
			}
		}
		else if (part == "corner") {
				part = "corner"; // compiler bug
		}
		else { //?
			if (id == MOUSE_PRESSED) {
				int[] port = getRectangle(component, ":port");
				if (port != null) { setReference(component, port[2], port[3]); }
			}
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	private boolean processScroll(Object component, Object part) {
		int[] view = getRectangle(component, ":view");
		int[] port = ((part == "left") || (part == "up")) ? null :
			getRectangle(component, ":port");
		int dx = 0; int dy = 0;
		if (part == "left") { dx = -10; }
		else if (part == "lefttrack") { dx = -port[0]; }
		else if (part == "right") { dx = 10; }
		else if (part == "righttrack") { dx = port[0]; }
		else if (part == "up") { dy = -10; }
		else if (part == "uptrack") { dy = -port[1]; }
		else if (part == "down") { dy = 10; }
		else if (part == "downtrack") { dy = port[1]; }
		if (dx != 0) {
			dx = (dx < 0) ? Math.max(-view[2], dx) :
				Math.min(dx, view[0] - port[0] - view[2]);
		}
		else if (dy != 0) {
			dy = (dy < 0) ? Math.max(-view[3], dy) :
				Math.min(dy, view[1] - port[1] - view[3]);
		}
		else return false;
		if ((dx == 0) && (dy == 0)) { return false; }
		view[2] += dx; view[3] += dy;
		repaint(component, null, (dx != 0) ? "horizontal" : "vertical");
                // XXX needed for tables, because table headers are not included in the
                // XXX repaint area. Needs to be fixed in repaint(Object, Object, Object).
		repaint(component);
		return (((part == "left") || (part == "lefttrack")) && (view[2] > 0)) ||
			(((part == "right") || (part == "righttrack")) &&
				(view[2] < view[0] - port[0])) ||
			(((part == "up") || (part == "uptrack")) && (view[3] > 0)) ||
			(((part == "down") || (part == "downtrack")) &&
				(view[3] < view[1] - port[1]));
	}

	/**
	 *
	 */
	private boolean processSpin(Object component, Object part) {
		String text = getString(component, "text", null);
		if (text != null) {
			try {
                                int max = getInteger(component, "maximum");
                                int min = getInteger(component, "minimum");
                                int step = getInteger(component, "step");
                                int val = Integer.parseInt(text);
                                if (part == "up") {
                                    if (val + step <= max) val += step;
                                } else {
                                    if (val - step >= min) val -= step;
                                }
				String value = String.valueOf(val);
                                setInteger(component, "value", val);
				setString(component, "text", value, null);
				setInteger(component, "start", value.length(), 0);
				setInteger(component, "end", 0, 0);
				repaint(component, "spinbox", "text");
				invoke(component, null, "action");
				return true;
			} catch (NumberFormatException nfe) {}
		}
		return false;
	}

	/**
	 *
	 */
	protected boolean invoke(Object component, Object part, String event) {
		Object method = get(component, event);
		if (method != null) {
			invokeImpl(method, component, part);
			return true;
		}
		return false;
	}
	
	/**
	 *
	 */
	private void invokeImpl(Object method, Object component, Object part) {
		Object[] data = (Object[]) method;
		Object[] args = (data.length > 2) ? new Object[(data.length - 2) / 3] : null;
		if (args != null) for (int i = 0; i < args.length; i++) {
			Object target = data[2 + 3 * i];
			if ("thinlet" == target) {
				args[i] = this;
			}
			else {
				if ("item" == target) { target = part; }
				Object parametername = data[2 + 3 * i + 1];
				if (parametername == null) {
					args[i] = target;
					//args[i] = new Widget(this, target);
				}
				else {
					args[i] = (target != null) ? get(target, parametername) : null;
					if (args[i] == null) { args[i] = data[2 + 3 * i + 2]; }
				}
			}
		}
		try {
			((Method) data[1]).invoke(data[0], args);
		} catch (InvocationTargetException ite) {
			handleException(ite.getTargetException());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Overwrite this method to handle exceptions thrown
	 * by the invoked custom methods
	 *
	 * @param throwable the thrown exception by the bussiness logic
	 */
	protected void handleException(Throwable throwable) {
		throwable.printStackTrace();
	}

	/**
	 *
	 */
	private boolean findComponent(Object component, int x, int y) {
		if (component == content) {
			mouseinside = insidepart = null;
			mousex = x; mousey = y;
		}
		if (!getBoolean(component, "visible", true)) { return false; }
		int[] bounds = getRectangle(component, "bounds");
		if ((bounds == null) || !(bounds.contains(x, y))) { return false; }
		mouseinside = component;
		x -= bounds[2]; y -= bounds[3];
		String classname = getClass(component);

		if ("combobox" == classname) {
			if (getBoolean(component, "editable", true) && (x <= bounds[0] - block)) {
				Image icon = getIcon(component, "icon", null);
				insidepart = ((icon != null) && (x <= 2 + icon.getWidth())) ?
					"icon" : null;
			} else {
				insidepart = "down";
			}
		}
		else if (":combolist" == classname) {
			if (!findScroll(component, x, y)) {
				y += getRectangle(component, ":view")[3];
				for (Object choice = get(get(component, "combobox"), ":comp");
						choice != null; choice = get(choice, ":next")) {
					int[] r = getRectangle(choice, "bounds");
					if ((y >= r[3]) && (y < r[3] + r[1])) {
						insidepart = choice; break;
					}
				}
			}
		}
		else if ("textarea" == classname) {
			findScroll(component, x, y);
		}
		else if ("tabbedpane" == classname) {
			int selected = getInteger(component, "selected", 0);
			int i = 0;
			for (Object tab = get(component, ":comp"); tab != null; tab = get(tab, ":next")) {
				int[] r = getRectangle(tab, "bounds");
				if (i == selected) {
					Object tabcontent = get(tab, ":comp");
					if ((tabcontent != null) && findComponent(tabcontent, x - r[2], y - r[3])) {
						break;
					}
				}
				if (r.contains(x, y)) {
					insidepart = tab; break;
				}
				i++;
			}
		}
		else if (("panel" == classname) || ("desktop" == classname) ||
				("dialog" == classname)) {
			if (("dialog" == classname) &&
					(y < 4 + getInteger(component, ":titleheight", 0))) {
				insidepart = "header";
			}
			else if (!findScroll(component, x, y)) {
				int[] port = getRectangle(component, ":port");
				if (port != null) { // content scrolled
					int[] view = getRectangle(component, ":view");
					x += view[2] - port[2]; y += view[3] - port[3];
				}
				for (Object comp = get(component, ":comp");
						comp != null; comp = get(comp, ":next")) {
					if (findComponent(comp, x, y)) { break; }
					if (("desktop" == classname) &&
							getBoolean(comp, "modal", false)) { insidepart = "modal"; break; } // && dialog
				}
			}
		}
		else if ("spinbox" == classname) {
			insidepart = (x <= bounds[0] - block) ? null :
				((y <= bounds[1] / 2) ? "up" : "down");
		}
		else if ("splitpane" == classname) { 
			Object comp1 = get(component, ":comp");
			if (comp1 != null) {
				if (!findComponent(comp1, x, y)) {
					Object comp2 = get(comp1, ":next");
					if (comp2 != null) {
						findComponent(comp2, x, y);
					}
				}
			}
		}
		else if ("list" == classname) {
			findScroll(component, x, y);
		}
		else if ("table" == classname) {
			if (!findScroll(component, x, y)) {
			}
		}
		else if ("tree" == classname) {
			findScroll(component, x, y);
		}
		else if ("menubar" == classname) {
			for (Object menu = get(component, ":comp");
					menu != null; menu = get(menu, ":next")) {
				int[] r = getRectangle(menu, "bounds");
				if ((x >= r[2]) && (x < r[2] + r[0])) {
					insidepart = menu; break;
				}
			}
		}
		else if (":popup" == classname) {
			for (Object menu = get(get(component, "menu"), ":comp");
					menu != null; menu = get(menu, ":next")) {
				int[] r = getRectangle(menu, "bounds");
				if ((y >= r[3]) && (y < r[3] + r[1])) {
					insidepart = menu; break;
				}
			}
		}
		return true;
	}

	/**
	 * @param component a scrollable widget
	 * @param x point x location
	 * @param y point y location
	 * @return true if the point (x, y) is inside scroll-control area
	 * (scrollbars, corners, borders), false otherwise (vievport, header, or no scrollpane)
	 */
	private boolean findScroll(Object component, int x, int y) {
		int[] port = getRectangle(component, ":port");
		if ((port == null) || port.contains(x, y)) { return false; }
		int[] view = getRectangle(component, ":view");
		int[] horizontal = getRectangle(component, ":horizontal");
		int[] vertical = getRectangle(component, ":vertical");
		if ((horizontal != null) && horizontal.contains(x, y)) {
			findScroll(x - horizontal[2], horizontal[0], port[0], view[2], view[0], true);
		}
		else if ((vertical != null) && vertical.contains(x, y)) {
			findScroll(y - vertical[3], vertical[1], port[1], view[3], view[1], false);
		}
		else { insidepart = "corner"; }
		return true;
	}
	
	/**
	 * @param p x or y relative to the scrollbar begin
	 * @param size scrollbar width or height
	 * @param portsize viewport width or height
	 * @param viewp view x or y
	 * @param viewsize view width or height
	 * @param horizontal if true horizontal, vertical otherwise
	 */
	private void findScroll(int p, int size, int portsize, int viewp, int viewsize, boolean horizontal) {
		if (p < block) { insidepart = horizontal ? "left" : "up"; }
		else if (p > size - block) { insidepart = horizontal ? "right" : "down"; }
		else {
			int track = size - 2 * block;
			if (track < 10) { insidepart = "corner"; return; } // too small
			int knob = Math.max(track * portsize / viewsize, 10);
			int decrease = viewp * (track - knob) / (viewsize - portsize);
			if (p < block + decrease) { insidepart = horizontal ? "lefttrack" : "uptrack"; }
			else if (p < block + decrease + knob) { insidepart = horizontal ? "hknob" : "vknob"; }
			else { insidepart = horizontal ? "righttrack" : "downtrack"; }
		}
	}

	/**
	 *
	 */
	private void repaint(Object component, Object classname, Object part) {
		int[] b = getRectangle(component, "bounds");
		if (classname == "combobox") { // combobox down arrow
			repaint(component, b[2] + b[0] - block, b[3], block, b[1]); // icon?+
		}
		else if (classname == "spinbox") {
			if (part == "text") { // spinbox textfield content
				repaint(component, b[2], b[3], b[0] - block, b[1]);
			}
			else { // spinbox increase or decrease button
				repaint(component, b[2] + b[0] - block,
					(part == "up") ? b[3] : (b[3] + b[1] - b[1] / 2), block, b[1] / 2);
			}
		}
		//else if (classname == "dialog") {}
			//int titleheight = getInteger(component, ":titleheight", 0);
		//else if (classname == "splitpane") {}
		else if ((classname == "tabbedpane") || // tab
				(classname == "menubar") || (classname == ":popup")) { // menuitem
			int[] r = getRectangle(part, "bounds");
			repaint(component, b[2] + r[2], b[3] + r[3],
				(classname == ":popup") ? b[0] : r[0], r[1]);
		}
		// classname: ":combolist" "textarea" "list" "table" "tree"
		else if ((part == "left") || (part == "right")) { // horizontal scrollbar button
			int[] r = getRectangle(component, ":horizontal");
			repaint(component, b[2] + ((part == "left") ? r[2] : (r[2] + r[0] - block)), b[3] + r[3], block, r[1]);
		}
		else if ((part == "up") || (part == "down")) { // vertical scrollbar button
			int[] r = getRectangle(component, ":vertical");
			repaint(component, b[2] + r[2], b[3] + ((part == "up") ? r[3] : (r[3] + r[1] - block)), r[0], block);
		}
		else if ((part == "text") || (part == "horizontal") || (part == "vertical")) {
			int[] port = getRectangle(component, ":port"); // textarea or content
			repaint(component, b[2] + port[2], b[3] + port[3], port[0], port[1]);
			if (part == "horizontal") {
				int[] r = getRectangle(component, ":horizontal");
				repaint(component, b[2] + r[2], b[3] + r[3], r[0], r[1]);
			}
			else if (part == "vertical") {
				int[] r = getRectangle(component, ":vertical");
				repaint(component, b[2] + r[2], b[3] + r[3], r[0], r[1]);
			}
		}
		else { // repaint the whole line of its subcomponent
			int[] port = getRectangle(component, ":port");
			int[] view = getRectangle(component, ":view");
			int[] r = getRectangle(part, "bounds");
			if ((r[3] + r[1] >= view[3]) && (r[3] <= view[3] + port[1])) {
				repaint(component, b[2] + port[2], b[3] + port[3] - view[3] + r[3],
					port[0], r[1]);
				//? need cut item rectangle above/bellow viewport
			}
		}
	}
	
	/**
	 * Layout and paint the given component later
	 * @param component
	 */
	private void validate(Object component) {
		repaint(component);
		int[] bounds = getRectangle(component, "bounds");
		if (bounds != null) { bounds[0] = -1 * Math.abs(bounds[0]); }
	}
	
	/**
	 * Repaint the given component's area later
	 * @param component
	 */
	private void repaint(Object component) {
		int[] bounds = getRectangle(component, "bounds");
		if (bounds != null) {
			repaint(component, bounds[2], bounds[3], bounds[0], bounds[1]);
		}
	}

	/**
	 * Repaint the given component's area later
	 * @param component
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void repaint(Object component, int x, int y, int width, int height) {
		while ((component = getParent(component)) != null) {
			int[] bounds = getRectangle(component, "bounds");
			x += bounds[2]; y += bounds[3];
			int[] view = getRectangle(component, ":view");
			if (view != null) {
				int[] port = getRectangle(component, ":port");
				x += -view[2] + port[2]; y += -view[3] + port[3]; //+ clip :port
			}
		}
		repaint(x, y, width, height);
	}

	/**
	 * Requests that both the <i>Thinlet</i> component,
	 * and the given widget get the input focus
	 *
	 * @param component a focusable widget inside
	 * visible and enabled parents, and tabbedpane's selected tab
	 * @return true, if the given component was focusable
	 */
	public boolean requestFocus(Object component) { //#
		if (isFocusable(component, true)) {
			setFocus(component); return true; //repaint too!
		}
		return false;
	}

	/**
	 *
	 */
	private boolean setFocus(Object component) {
		if (!focusinside) {
			requestFocus();
		}
		if (focusowner != component) {
			Object focused = focusowner;
			focusowner = component;
			if (focused != null) {
				//mouseEvent(null, FocusEvent.FOCUS_LOST, focused, null, null);
				repaint(focused);
				//focusGained(component);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @return next focusable component is found (not the first of the desktop/dialog)
	 */
	private boolean setNextFocusable(Object current, boolean outgo) {
		boolean consumed = true;
		for (Object next = null, component = current; true; component = next) {
			next = get(component, ":comp"); // check first subcomponent
			if (next == null) { next = get(component, ":next"); } // check next component
			while (next == null) { // find the next of the parents, or the topmost
				component = getParent(component); // current is not on the desktop
				if (component == null) { return false; }
				if ((component == content) || ((getClass(component) == "dialog") &&
						(!outgo  || getBoolean(component, "modal", false)))) {
					consumed = false; // find next focusable but does not consume event
					next = component; // the topmost (desktop or modal dialog)
				}
				else {
					next = get(component, ":next");
				}
			}
			if (next == current) { return false; } // one fucusable, no loop
			if (isFocusable(next, false)) {
				setFocus(next);
				return consumed;
			}
		}
	}

	/**
	 * @return previous focusable component is found (not the last of the desktop/dialog)
	 */
	private boolean setPreviousFocusable(Object component, boolean outgo) {
		for (int i = 0; i < 2; i++) { // 0 is backward direction
			Object previous = getPreviousFocusable(component, null, true, false, (i == 0), outgo);
			if (previous != null) {
				setFocus(previous);
				return (i == 0);
			}
		}
		return false;
	}
	
	/**
	 * For the starting component search its parent direction for a focusable component, and then
	 * its next component (if not search backward from the component).<br>
	 * For its parent components check its first component, the current one, and its parent direction
	 * (backward search), or its parent, then next component (forward direction).<br>
	 * For the rest components check the next, then the first subcomponent direction, and finally
	 * check whether the component is focusable.
	 */
	private Object getPreviousFocusable(Object component,
			Object block, boolean start, boolean upward, boolean backward, boolean outgo) {
		Object previous = null;
		if ((component != null) && (component != block)) {
			boolean go = ((getClass(component) != "dialog") ||
				(outgo && !getBoolean(component, "modal", false)));
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
	
	/**
	 * Check whether the given widget can become focusowner
	 * @param component check this widget
	 * @param forced splitpane is also checked
	 * (e.g. false for tab navigating, and true for mouse selection or application request)
	 * @return true if focusable, otherwise false
	 */
	private boolean isFocusable(Object component, boolean forced) {
		String classname = getClass(component);
		if ((classname == "button") || (classname == "checkbox") || ("togglebutton" == classname) ||
				(classname == "combobox") || (classname == "textfield") ||
				(classname == "passwordfield") || (classname == "textarea") ||
				(classname == "spinbox") || (classname == "slider") ||
				(classname == "list") || (classname == "table") || (classname == "tree") ||
				(classname == "tabbedpane") || (forced && (classname == "splitpane"))) {
			for (Object comp = component; comp != null;) {
				// component and parents are enabled and visible
				if (!getBoolean(comp, "enabled", true) || !getBoolean(comp, "visible", true)) {
					return false;
				}
				Object parent = getParent(comp);
				// inside the selected tabbedpane tab
				if ((getClass(comp) == "tab") && (getItem(parent,
					getInteger(parent, "selected", 0)) != comp)) { return false; }
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
	 * @param classname the widget type (e.g. <i>button</i>)
	 * @return a new component, every component is simply an <i>Object</i>
	 * @throws java.lang.IllegalArgumentException for unknown widget type
	 */
	public static Object create(String classname) { //#
		for (int i = 0; i < dtd.length; i += 3) {
			if (dtd[i].equals(classname)) {
				return createImpl((String) dtd[i]);
			}
		}
		throw new IllegalArgumentException("unknown " + classname);
	}
	
	/**
	 * Gets the type of the given component
	 *
	 * @param component a widget
	 * @return the class name of the component (e.g. <i>button</i>)
	 */
	public static String getClass(Object component) { //#
		return (String) get(component, ":class");
	}

	/**
	 * Get the topmost component
	 *
	 * @return the root object (it is a <i>desktop</i>), never <i>null</i>
	 */
	public Object getDesktop() {//#
		return content;
	}

	/**
	 *
	 */
	private static Object createImpl(String classname) {
		return new Object[] { ":class", classname, null };
	}
	
	/**
	 *
	 */
	private static boolean set(Object component, Object key, Object value) {
		Object[] previous = (Object[]) component;
		for (Object[] entry = previous; entry != null;
				entry = (Object[]) entry[2]) {
			if (entry[0] == key) {
				if (value != null) { // set the row's value
					Object oldvalue = entry[1];
					entry[1] = value;
					return !value.equals(oldvalue);
				}
				else { // remove the row
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

	/**
	 *
	 */
	private static Object get(Object component, Object key) {
		for (Object[] entry = (Object[]) component; entry != null;
				entry = (Object[]) entry[2]) {
			if (entry[0] == key) {
				return entry[1];
			}
		}
		return null;
	}

	/**
	 * Gets the count of subcomponents in the list of the given component
	 *
	 * @param component a widget
	 * @return the number of components in this component
	 */
	public int getCount(Object component) {
		return getItemCountImpl(component, ":comp");
	}
	
	/**
	 * Gets the parent of this component
	 *
	 * @param component a widget
	 * @return the parent container of this component or item
	 */
	public Object getParent(Object component) {
		return get(component, ":parent");
	}

	/**
	 * Gets the index of the first selected item in the given component
         * (list, table, tree, tabbedpane or combobox)
	 *
	 * @param component a widget
	 * @return the first selected index or -1
	 */
	public int getSelectedIndex(Object component) {
		String classname = getClass(component);
		if (classname == "tabbedpane" || classname == "combobox") {
			return getInteger(component, "selected");
		}
		Object item = get(component, ":comp");
		for (int i = 0; item != null; i++) {
			if (getBoolean(item, "selected", false)) { return i; }
			item = get(item, ":next");
		}
		return -1;
	}
	
	/**
	 * Gets the first selected item of the given component
         * (list, table, tree, tabbedpane or combobox)
	 *
	 * @param component a widget
	 * @return the first selected item or null
	 */
	public Object getSelectedItem(Object component) {
		String classname = getClass(component);
		if (classname == "tabbedpane" || classname == "combobox") {
			return getItemImpl(component, ":comp", getInteger(component, "selected"));
		}
		for (Object item = findNextItem(component, classname, null); item != null;
				item = findNextItem(component, classname, item)) {
			if (getBoolean(item, "selected", false)) { return item; }
		}
		return null;
	}

	/**
	 * Gets the selected item of the given component (list, table, or tree)
	 * when multiple selection is allowed
	 *
	 * @param component a widget
	 * @return the array of selected items, or a 0 length array
	 */
	public Object[] getSelectedItems(Object component) {
		String classname = getClass(component);
		Object[] selecteds = new Object[0];
		for (Object item = findNextItem(component, classname, null); item != null;
				item = findNextItem(component, classname, item)) {
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
	 * @return the first or the next item of the (list, table, or tree) component
	 */
	private Object findNextItem(Object component, String classname, Object item) {
		if (item == null) { // first item
			return get(component, ":comp");
		}
		else if ("tree" == classname) { // next tree node
			Object next = get(item, ":comp");
			if ((next == null) || !getBoolean(item, "expanded", true)) { // no subnode or collapsed
				while ((item != component) && ((next = get(item, ":next")) == null)) {
					item = getParent(item); //next node of in backward path
				}
			}
			return next;
		}
		else { //next list or tree item
			return get(item, ":next");
		}
	}

	/**
	 * Removes all the components from this container
	 *
	 * @param component the specified container
	 */
	public void removeAll(Object component) {
		if (get(component, ":comp") != null) {
			set(component, ":comp", null);
			update(component, "validate");
		}
	}

	/**
	 *
	 */
	private static int getItemCountImpl(Object component, String key) {
		int i = 0;
		for (Object comp = get(component, key); comp != null; comp = get(comp, ":next")) {
			i++;
		}
		return i;
	}

	/**
	 * Returns the subcomponent of the given component at the given index
	 *
	 * @param component a specified container
	 * @param index the index of the component to get
	 * @return the index<sup>th</sup> component in this container
	 */
	public Object getItem(Object component, int index) {
		return getItemImpl(component, ":comp", index);
	}
	
	/**
	 * Gets all the components in this container
	 *
	 * @param component a specified container
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
	private static Object getItemImpl(Object component, Object key, int index) {
		int i = 0;
		for (Object item = get(component, key); item != null; item = get(item, ":next")) {
			if (i == index) { return item; }
			i++;
		}
		return null;
	}

	/**
	 *
	 */
	private int getIndex(Object component, Object value) {
		int index = 0;
		for (Object item = get(component, ":comp"); item != null; item = get(item, ":next")) {
			if (value == item) { return index; }
			index++;
		}
		return -1;
	}

	/**
	 * Adds the specified component to the root desktop
	 *
	 * @param component a widget to be added
	 */
	public void add(Object component) {
		add(content, component, 0);
	}

	/**
	 * Adds the specified component to the end of the specified container
	 *
	 * @param parent a container
	 * @param component a component to be added
	 */
	public void add(Object parent, Object component) {
		add(parent, component, -1);
	}

	/**
	 * Adds the specified component to the container at the given position
	 *
	 * @param parent a container
	 * @param component a component to be inserted
	 * @param index the position at which to insert the component, 
	 * or -1 to insert the component at the end
	 */
	public void add(Object parent, Object component, int index) {
		addImpl(parent, component, index);
		update(component, "validate");
		if (parent == content) {
			setNextFocusable(component, false);
		}
	}

	/**
	 * Referenced by DOM
	 */
	private void insertItem(Object parent,
			Object key, Object component, int index) {
		Object target = get(parent, key);
		if (index == -1) {
			while (target != null) {
				target = get(parent = target, key = ":next");
			}
		}
		else {
			for (int i = 0; i < index; i++) {
				target = get(parent = target, key = ":next");
			}
			set(component, ":next", get(parent, key));
		}
		set(parent, key, component);
	}

	/**
	 * Removes the specified component from its parent
	 *
	 * @param component the component to be removed
	 */
	public void remove(Object component) {
		Object parent = getParent(component);
		update(component, "validate");
		removeItemImpl(parent, component);
		// reuest focus for its parent if the component (or subcomponent) is currently focused
		for (Object comp = focusowner; comp != null; comp = getParent(comp)) {
			if (comp == component) {
				setNextFocusable(parent, false); break;
			}
		}
	}

	/**
	 *
	 */
	private void removeItemImpl(Object parent, Object component) {
		String key = ":comp";
		Object target = get(parent, key);
		while (target != component) {
			target = get(parent = target, key = ":next"); // (target != null)
			if (target == null) return;
		}
		set(parent, key, get(target, ":next"));
		set(target, ":next", null);
		set(target, ":parent", null);
	}

	/**
	 * Finds the first component from the root desktop by a specified name value
	 *
	 * @param name parameter value identifies the widget
	 * @return the first suitable component, or null
	 */
	public Object find(String name) {
		return find(content, name);
	}

	/**
	 * Finds the first component from the specified component by a name
	 *
	 * @param component the widget is searched inside this component
	 * @param name parameter value identifies the widget
	 * @return the first suitable component, or null
	 */
	public Object find(Object component, String name) {
		if (name.equals(get(component, "name"))) { return component; }
		for (Object comp = get(component, ":comp"); 	comp != null; comp = get(comp, ":next")) {
			Object found = find(comp, name);
			if (found != null) { return found; }
		}
		// header popupmenu ...
		return null;
	}
	
	/**
	 * mnemonic (e.g. Alt-X):
	 * - check: label, button, checkbox, togglebutton, menubar menus, tabbedpane tabs
	 * - path: panel, desktop, dialog, splitpane components, tabbedpane selected component 
	 * accelerator (e.g. Ctrl-Shift-X, F4):
	 * - check: menuitem, checkboxmenuitem
	 * - path: see above, and menubar, and menu items
	 * menubar F10: check menubar only
	 * button enter, escape: check button only
	 * @param component
	 * @param parent check upwards if true
	 * @param checked this leaf is already checked
	 * @param mnemonic
	 * @return true if the char was consumed
	 */
	private boolean checkMnemonic(Object component,
			boolean parent, Object checked, int keycode, int modifiers) {
		if ((component == null) || !getBoolean(component, "visible", true) ||
				!getBoolean(component, "enabled", true)) { //+ enabled comp in disabled parent
			return false;
		}
		String classname = getClass(component);
		if ("label" == classname) {
			if (hasMnemonic(component, keycode, modifiers)) {
				Object labelfor = get(component, "for");
				if (labelfor != null) {
					// focus labelfor
				}
			}
		}
		else if ("button" == classname) {
			if (((modifiers == 0) &&
				(((keycode == '\n') && (get(component, "type") == "default")) ||
				((keycode == 0x1B) && (get(component, "type") == "cancel")))) ||
					hasMnemonic(component, keycode, modifiers)) {
				invoke(component, null, "action");
				repaint(component);
				return true;
			}
		}
		else if (("checkbox" == classname) || ("togglebutton" == classname)) {
			if (hasMnemonic(component, keycode, modifiers)) {
				changeCheck(component, true);
				repaint(component);
				return true;
			}
		}
		else if ("menubar" == classname) {
			for (Object menu = get(component, ":comp"); menu != null; menu = get(menu, ":next")) {
				if (hasMnemonic(menu, keycode, modifiers) ||
						((modifiers == 0) && (keycode == KeyEvent.VK_F10))) {
					closeup();
					set(component, "selected", menu);
					popupMenu(component);
					repaint(component, "menubar", menu);
					return true;
				}
			}
		}
		else if (("menuitem" == classname) || ("checkboxmenuitem" == classname)) {
			if (hasAccelerator(component, keycode, modifiers)) {
				invoke(component, null, "action");
			}
		}
		else if ("tabbedpane" == classname) {
			int selected = getInteger(component, "selected", 0); int i = 0;
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
			if ((comp != null) && (comp != checked) &&
					checkMnemonic(comp, false, null, keycode, modifiers)) {
				return true;
			}
		}
		// check subcomponents
		if (("panel" == classname) || ("desktop" == classname) ||
				("dialog" == classname) || ("splitpane" == classname) ||
				("menubar" == classname) || ("menu" == classname)) {
			for (Object comp = get(component, ":comp"); comp != null; comp = get(comp, ":next")) {
				if ((comp != checked) && checkMnemonic(comp, false, null, keycode, modifiers)) { return true; }
			}
		}
		// check parent
		if (parent && (("dialog" != classname) || !getBoolean(component, "modal", false))) {
			if (checkMnemonic(getParent(component), true,
					("tab" == classname) ? checked : component, keycode, modifiers)) { return true; }
		}
		return false;
	}
	
	/**
	 * @param component
	 * @param keycode
	 * @param modifiers
	 * @return true if the component has the given mnemonic
	 */
	private boolean hasMnemonic(Object component, int keycode, int modifiers) {
		if (modifiers == InputEvent.ALT_MASK) {
			int index = getInteger(component, "mnemonic", -1);
			if (index != -1) {
				String text = getString(component, "text", null);
				return (text != null) && (text.length() > index) &&
					(Character.toUpperCase(text.charAt(index)) == keycode);
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
	private boolean hasAccelerator(Object component, int keycode, int modifiers) {
		Object accelerator = get(component, "accelerator");
		if (accelerator != null) {
			long keystroke = ((Long) accelerator).longValue();
			return ((keystroke >> 32) == modifiers) && ((keystroke & 0xffff) == keycode);
		}
		return false;
	}
	
	/**
	 * Binds the specified key to the specified value, and stores in this component.
	 * <i>Null</i> value removes the property. Previously existing value, if any, is
         * silently replaced.
         * <p>Use the <code>property</code> tag in the XML
	 * resource to bind a string value, the format is: <i>property='key=value'</i>.
         * For multiple key/value pairs use the format:
         * <i>property='key1=value1';key2=value2'</i>.
	 *
	 * @param component the property is bound to this component
	 * @param key the client property key
	 * @param value the new client property value
	 */
	public void putProperty(Object component, Object key, Object value) {
		Object table = get(component, ":bind");
		if (value != null) {
			if (table == null) {
				set(component, ":bind", table = new Hashtable());
			}
			((Hashtable) table).put(key, value);
		}
		else if (table != null) {
			((Hashtable) table).remove(key);
		}
	}
	
	/**
	 * Returns the value of the property with the specified key.
	 *
	 * @param component use this component
	 * @param key the client property key
	 * @return the value to which the key is mapped or null if the key is not mapped to any value
	 */
	public Object getProperty(Object component, Object key) {
		Object table = get(component, ":bind");
		return (table != null) ? ((Hashtable) table).get(key) : null;
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Creates a component (and its subcomponents, and properties)
	 * from the given xml resource
	 *
	 * @param path is relative to your thinlet instance or the classpath
	 * (if the path starts with an <i>/</i> character), or a full url
	 * @return the root component of the parsed resource
	 * @throws java.io.IOException
	 */
	public Object parse(String path) throws IOException {
		return parse(path, this);
	}

	/**
	 * Creates a component from the given xml resource using the
	 * specified event handler
	 *
	 * @param path is relative to your application package or the classpath, or an url
	 * @param handler bussiness methods are implemented in this object 
	 * @return the parsed components' root
	 * @throws java.io.IOException
	 */
	public Object parse(String path, Object handler) throws IOException {
		InputStream inputstream = null;
		try {
			inputstream = getClass().getResourceAsStream(path);
			if (inputstream == null) {
				try {
					inputstream = new URL(path).openStream();
				} catch (MalformedURLException mfe) { /* thows nullpointerexception*/ }
			}
		} catch (Throwable e) {}
		return parse(inputstream, handler);
	}

	/**
	 * Creates a component from the given stream
	 *
	 * @param inputstream e.g. <i>new URL("http://myserver/myservlet").openStream()</i>
	 * @return the root component of the parsed stream
	 * @throws java.io.IOException
	 */
	public Object parse(InputStream inputstream) throws IOException {
		return parse(inputstream, this);
	}

	/**
	 * Creates a component from the given stream and event handler
	 *
	 * @param inputstream read xml from this stream
	 * @param handler event handlers are implemented in this object
	 * @return the parsed components' root
	 * @throws java.io.IOException
	 */
	public Object parse(InputStream inputstream, Object handler) throws IOException {
		return parse(inputstream, true, false, handler);
	}

	/**
	 * You can use the internal xml parser as a simple SAX-like parser,
	 * during the process it calls the <i>startElement</i>, <i>characters</i>,
	 * and <i>endElement</i> methods
	 *
	 * @param inputstream e.g. <i>new URL("http://myserver/myservlet").openStream()</i>
	 * @throws java.io.IOException
	 */
	protected void parseXML(InputStream inputstream) throws IOException {
		parse(inputstream, false, false, null);
	}

	/**
	 * The SAX-like parser calls this method, you have to overwrite it
	 *
	 * @param name of the tag
	 * @param attributelist a list of attributes including keys and value pairs
	 */
	protected void startElement(String name, Hashtable attributelist) {}

	/**
	 * The SAX-like parser calls this method, you have to overwrite it
	 *
	 * @param text the content of a tag
	 */
	protected void characters(String text) {}

	/**
	 * The SAX-like parser calls this method, you have to overwrite it
	 */
	protected void endElement() {}
	
	/**
	 * You can use the internal xml parser as a simple DOM-like parser,
	 * use the <i>getDOMAttribute</i>, <i>getDOMText</i>,
	 * <i>getDOMCount</i>, <i>getDOMNode</i>, <i>getClass</i>,
	 * and <i>getParent</i> methods to analise the document
	 *
	 * @param inputstream e.g. <i>new URL("http://myserver/myservlet").openStream()</i>
	 * @return the root tag
	 * @throws java.io.IOException
	 */
	protected Object parseDOM(InputStream inputstream) throws IOException {
		return parse(inputstream, false, true, null);
	}
	
	/**
	 * Gets the attribute value by the specified key for a DOM tag
	 *
	 * @param node a specified tag
	 * @param key a string to identify the value pair
	 * @return the value, or null
	 */
	protected static String getDOMAttribute(Object node, String key) {
		return (String) get(node, key);
	}
	
	/**
	 * Gets the content string of a tag
	 *
	 * @param node a specified tag
	 * @return the value, or null
	 */
	protected static String getDOMText(Object node) {
		return (String) get(node, ":text");
	}
	
	/**
	 * Gets the number of tags in a tag by a specified tagname 
	 *
	 * @param node a specified tag
	 * @param key the searched tagname
	 * @return the number of tags
	 */
	protected static int getDOMCount(Object node, String key) {
		return getItemCountImpl(node, key);
	}
	
	/**
	 * Gets the subtag of the specified tag by tagname and index 
	 *
	 * @param node a specified tag
	 * @param key the searched tagname
	 * @param index the index of the requested subtag
	 * @return the found tag, or null
	 */
	protected static Object getDOMNode(Object node, String key, int index) {
		return getItemImpl(node, key, index);
	}

	/**
	 *
	 * @param inputstream
	 * @param validate parse GUI from xml if true
	 * @param dom parse an xml resoource
	 * @param handler
	 * @return
	 * @throws java.io.IOException
	 * @throws java.lang.IllegalArgumentException
	 */
	private Object parse(InputStream inputstream,
			boolean validate, boolean dom, Object handler) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(inputstream));
		try {
			Object[] parentlist = null;
			Object current = null;
			Hashtable attributelist = null;
			Vector methods = (validate && !dom) ? new Vector() : null;
			StringBuffer text = new StringBuffer();
			for (int c = reader.read(); c != -1;) {
				if (c == '<') {
					if ((c = reader.read()) == '/') { //endtag
						if (text.length() > 0) {
							if (text.charAt(text.length() - 1) == ' ') {
								text.setLength(text.length() - 1);
							}
							if (!validate) {
								if (dom) {
									set(current, ":text", text.toString());
								} else {
									characters(text.toString());
								}
							}
							// else {
								//addContent(current, text.toString());
							//}
							text.setLength(0);
						}
						String tagname = (String) parentlist[2]; //getClass(current);
						for (int i = 0; i < tagname.length(); i++) { // current-tag
							if ((c = reader.read()) != tagname.charAt(i)) {
								throw new IllegalArgumentException(tagname);
							}
						}
						while (" \t\n\r".indexOf(c = reader.read()) != -1); // whitespace
						if (c != '>') throw new IllegalArgumentException(); // '>'
						c = reader.read();
						if (!validate && !dom) { endElement(); }
						if (parentlist[0] == null) {
							reader.close();
							finishParse(methods, current, handler);
							return current;
						}
						current = parentlist[0];
						parentlist = (Object[]) parentlist[1];
					}
					else if (c == '!') { // DOCTYPE
						while ((c = reader.read()) != '>'); //+(-1)
					}
					else if (c == '?') { // Processing Instructions
						boolean question = false; // read until '?>'
						while (((c = reader.read()) != '>') || !question) { question = (c == '?'); }
					}
					else { //start or standalone tag
						text.setLength(0);
						boolean iscomment = false;
						while (">/ \t\n\r".indexOf(c) == -1) {
							text.append((char) c);
							if ((text.length() == 3) && (text.charAt(0) == '!') &&
									(text.charAt(1) == '-') && (text.charAt(2) == '-')) {
								int m = 0;
								while (true) {
									c = reader.read();
									if (c == '-') { m++; }
									else if ((c == '>') && (m >= 2)) { break; }
									else { m = 0; }
								}
								iscomment = true;
							}
							c = reader.read();
						}
						if (iscomment) { continue; }
						String tagname = text.toString();
						parentlist = new Object[] { current, parentlist, tagname };
						if (validate) {
							current = (current != null) ?
								addElement(current, tagname) : create(tagname);
						} else {
							if (dom) {
								Object parent = current;
								current = createImpl(tagname = tagname);
								if (parent != null) {
									insertItem(parent, tagname, current, -1);
									//set(current, ":parent", parent);
								}
							} else {
								current = tagname;
							}
						}
						text.setLength(0);
						while (true) {
							boolean whitespace = false;
							while (" \t\n\r".indexOf(c) != -1) {
								c = reader.read();
								whitespace = true;
							}
							if (c == '>') {
								if (!validate && !dom) {
									startElement((String) current, attributelist); attributelist = null;
								}
								c = reader.read();
								break;
							}
							else if (c == '/') {
								if ((c = reader.read()) != '>') {
									throw new IllegalArgumentException(); // '>'
								}
								if (!validate && !dom) {
									startElement((String) current, attributelist); attributelist = null;
									endElement();
								}
								if (parentlist[0] == null) {
									reader.close();
									finishParse(methods, current, handler);
									return current;
								}
								current = parentlist[0];
								parentlist = (Object[]) parentlist[1];
								c = reader.read();
								break;
							}
							else if (whitespace) {
								while ("= \t\n\r".indexOf(c) == -1) {
									text.append((char) c);
									c = reader.read();
								}
								String key = text.toString();
								text.setLength(0);
								while (" \t\n\r".indexOf(c) != -1) c = reader.read();
								if (c != '=') throw new IllegalArgumentException();
								while (" \t\n\r".indexOf(c = reader.read()) != -1);
								char quote = (char) c;
								if ((c != '\"') && (c != '\'')) throw new IllegalArgumentException();
								while (quote != (c = reader.read())) {
									if (c == '&') {
										StringBuffer eb = new StringBuffer();
										while (';' != (c = reader.read())) { eb.append((char) c); }
										String entity = eb.toString();
										if ("lt".equals(entity)) { text.append('<'); }
										else if ("gt".equals(entity)) { text.append('>'); }
										else if ("amp".equals(entity)) { text.append('&'); }
										else if ("quot".equals(entity)) { text.append('"'); }
										else if ("apos".equals(entity)) { text.append('\''); }
										else if (entity.startsWith("#")) {
											boolean hexa = (entity.charAt(1) == 'x');
											text.append((char) Integer.parseInt(entity.substring(hexa ? 2 : 1), hexa ? 16 : 10));
										}
										else throw new IllegalArgumentException("unknown " + "entity " + entity);
									}
									else text.append((char) c);
								}
								if (validate) {
									addAttribute(current, key, text.toString(), methods);
								} else {
									if (dom) {
										set(current, key, text.toString());
									} else {
										if (attributelist == null) { attributelist = new Hashtable(); }
										attributelist.put(key, text.toString());
									}
								}
								//'<![CDATA[' ']]>'
								text.setLength(0);
								c = reader.read();
							}
							else throw new IllegalArgumentException();
						}
					}
				}
				else {
					if (" \t\n\r".indexOf(c) != -1) {
						if ((text.length() > 0) && (text.charAt(text.length() - 1) != ' ')) {
							text.append(' ');
						}
					}
					else {
						text.append((char) c);
					}
					c = reader.read();
				} 
			}
			throw new IllegalArgumentException();
		}
		finally {
			if (reader != null) { reader.close(); }
		}
	}
	
	/**
	 *
	 */
	private void finishParse(Vector methods, Object root, Object handler) {
		if (methods != null) {
			for (int i = 0; i < methods.size(); i += 3) {
				Object component = methods.elementAt(i);
				Object[] definition = (Object[]) methods.elementAt(i + 1);
				String value = (String) methods.elementAt(i + 2);
				
				if ("method" == definition[0]) {
					Object[] method = getMethod(component, value, root, handler);
					if ("init" == definition[1]) {
						invokeImpl(method, component, null);
					}
					else {
						set(component, definition[1], method);
					}
				}
				else { // ("component" == definition[0])
					Object reference = find(root, value); //+start find from the component
					if (reference == null) throw new IllegalArgumentException(value + " not found"); 
					set(component, definition[1], reference);
				}
			}
		}
	}

	/**
	 * Add the component to the parent's ':comp' list, and set its ':parent'
	 * or set single components
	 *
	 * @param index add at the specified index
	 * @throws java.lang.IllegalArgumentException
	 */
	private void addImpl(Object parent, Object component, int index) {
		String parentclass = getClass(parent);
		String classname = getClass(component);
		if ((("combobox" == parentclass) && ("choice" == classname)) ||
				(("tabbedpane" == parentclass) && ("tab" == classname)) ||
				(("list" == parentclass) && ("item" == classname)) ||
				(("table" == parentclass) && ("row" == classname)) ||
				(("header" == parentclass) && ("column" == classname)) ||
				(("row" == parentclass) && ("cell" == classname)) ||
				((("tree" == parentclass) || ("node" == parentclass)) && ("node" == classname)) ||
				(("menubar" == parentclass) && ("menu" == classname)) ||
				((("menu" == parentclass) || ("popupmenu" == parentclass)) &&
					(("menu" == classname) || ("menuitem" == classname) ||
					("checkboxmenuitem" == classname) || ("separator" == classname))) ||
				((("panel" == parentclass) || ("desktop" == parentclass) ||
					("splitpane" == parentclass) || ("dialog" == parentclass) ||
					("tab" == parentclass)) && instance(classname, "component") && (classname != "popupmenu"))) {
			insertItem(parent, ":comp", component, index);
			set(component, ":parent", parent);
		}
		else if ((("table" == parentclass) && ("header" == classname)) ||
				(("popupmenu" == classname) && instance(parentclass, "component"))) {
			set(parent, classname, component);
			set(component, ":parent", parent);
		}
		else throw new IllegalArgumentException(classname + " add " + parentclass);
	}
	
	/**
	 *
	 */
	private boolean instance(Object classname, Object extendclass) {
		if (classname == extendclass) { return true; }
		for (int i = 0; i < dtd.length; i += 3) {
				if (classname == dtd[i]) {
					return instance(dtd[i + 1], extendclass);
				}
		}
		return false;
	}

	/**
	 *
	 */
	private Object addElement(Object parent, String name) {
		Object component = create(name);
		addImpl(parent, component, -1);
		return component;
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	private void addAttribute(Object component, String key, String value, Vector lasts) {
		Object[] definition = getDefinition(getClass(component), key, null);
		key = (String) definition[1];
		if ("string" == definition[0]) {
			setString(component, key, value, (String) definition[3]);
		}
		else if ("choice" == definition[0]) {
			String[] values = (String[]) definition[3];
			setChoice(component, key, value, values, values[0]);
		}
		else if ("boolean" == definition[0]) {
			if ("true".equals(value)) {
				if (definition[3] == FALSE) {
					set(component, key, TRUE);
			 	}
			}
			else if ("false".equals(value)) {
				if (definition[3] == TRUE) {
					set(component, key, FALSE);
			 	}
			}
			else throw new IllegalArgumentException(value);
		}
		else if ("integer" == definition[0]) {
			set(component, key, Integer.valueOf(value));
		}
		else if ("icon" == definition[0]) {
			set(component, key, getIcon(value));
		}
		else if (("method" == definition[0]) || ("component" == definition[0])) {
			lasts.addElement(component);
			lasts.addElement(definition);
			lasts.addElement(value);
		}
		else if ("property" == definition[0]) {
			StringTokenizer st = new StringTokenizer(value, ";");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int equals = token.indexOf('=');
				if (equals == -1) { throw new IllegalArgumentException(token); }
				putProperty(component, token.substring(0, equals), token.substring(equals + 1));
			}
		}
		else if ("font" == definition[0]) {
			String name = null;
			boolean bold = false; boolean italic = false;
			int size = 0;
			StringTokenizer st = new StringTokenizer(value);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if ("bold".equalsIgnoreCase(token)) { bold = true; }
				else if ("italic".equalsIgnoreCase(token)) { italic = true; }
				else {
					try {
						size = Integer.parseInt(token);
					} catch (NumberFormatException nfe) {
						name = (name == null) ? token : (name + " " + token);
					}
				}
			}
			if (name == null) { name = font.getName(); }
			if (size == 0) { size = font.getSize(); }
			set(component, key, new Font(name,
				(bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), size));
		}
		else if ("color" == definition[0]) {
			int color = 0;
			if (value.startsWith("#")) { color = Integer.parseInt(value.substring(1), 16); }
			else if (value.startsWith("0x")) { color = Integer.parseInt(value.substring(2), 16); }
			else { // three separated integer including red, green, and blue
				StringTokenizer st = new StringTokenizer(value, " \r\n\t,");
				color = 0xff000000 | ((Integer.parseInt(st.nextToken()) & 0xff) << 16) |
					((Integer.parseInt(st.nextToken()) & 0xff) << 8) |
					(Integer.parseInt(st.nextToken()) & 0xff);
			}				
			set(component, key, color);
		}
		else if ("keystroke" == definition[0]) {
			String token = value;
			try {
				int keycode = 0, modifiers = 0;
				StringTokenizer st = new StringTokenizer(value, " \r\n\t+");
				while (st.hasMoreTokens()) {
					token = st.nextToken().toUpperCase();
					try {
							modifiers = modifiers | InputEvent.class.getField(token + "_MASK").getInt(null);
					} catch (Exception exc) { // not mask value
						keycode = KeyEvent.class.getField("VK_" + token).getInt(null);
					}
				}
				set(component, key, new Long(((long) modifiers) << 32 | keycode));
			} catch (Exception exc) { throw new IllegalArgumentException(token); } 
		}
		else if ("bean" == definition[0]) {
			try {
				set(component, key, (Component) Class.forName(value).newInstance());
			} catch (Exception exc) { throw new IllegalArgumentException(value); }
		}
		else throw new IllegalArgumentException((String) definition[0]);
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	private static Object[] getDefinition(Object classname, String key, String type) {
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
		throw new IllegalArgumentException("unknown " + key + " " + type +
			" for " + currentname);
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Sets the given attribute (String value) for the component
	 */
	public void setString(Object component, String key, String value) {
		Object[] definition = getDefinition(getClass(component), key, "string");
		if (setString(component, (String) definition[1],
				value, (String) definition[3])) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the String attribute value of the given component by the attribute key
	 */
	public String getString(Object component, String key) {
		Object[] definition = getDefinition(getClass(component), key, "string");
		return getString(component, (String) definition[1],
			(String) definition[3]);
	}

	/**
	 * Sets the given attribute (String value selected from available choices) for the component
	 */
	public void setChoice(Object component, String key, String value) {
		Object[] definition = getDefinition(getClass(component), key, "choice");
		String[] values = (String[]) definition[3];
		if (setChoice(component, (String) definition[1],
				value, values, values[0])) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the String attribute value of the given component by the attribute key. The value
         * returned is equal to one of available choices.
	 */
	public String getChoice(Object component, String key) {
		Object[] definition = getDefinition(getClass(component), key, "choice");
		return getString(component, (String) definition[1],
			((String[]) definition[3])[0]);
	}

	/**
	 * Sets the given attribute (boolean value) for the component
	 */
	public void setBoolean(Object component, String key, boolean value) {
		Object[] definition = getDefinition(getClass(component), key, "boolean");
		if (setBoolean(component, (String) definition[1],
				value, (definition[3] == TRUE))) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the boolean attribute value of the given component by the attribute key
	 */
	public boolean getBoolean(Object component, String key) {
		return get(component, key, "boolean") == TRUE;
	}

	/**
	 * Sets the given attribute (int value) for the component
	 */
	public void setInteger(Object component, String key, int value) {
		Object[] definition = getDefinition(getClass(component), key, "integer");
		if (setInteger(component, (String) definition[1],
				value, ((Integer) definition[3]).intValue())) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the int attribute value of the given component by the attribute key
	 */
	public int getInteger(Object component, String key) {
		return ((Integer) get(component, key, "integer")).intValue();
	}

	/**
	 * Sets the given attribute (java.awt.Image value) for the component
	 */
	public void setIcon(Object component, String key, Image icon) {
		Object[] definition = getDefinition(getClass(component), key, "icon");
		if (set(component, (String) definition[1], icon)) {
			update(component, definition[2]);
		}
	}

	/**
	 * Gets the java.awt.Image attribute value of the given component by the attribute key
	 */
	public Image getIcon(Object component, String key) {
		return (Image) get(component, key, "icon");
	}
	
	/**
	 *
	 */
	private static Object get(Object component, String key, String type) {
		Object[] definition = getDefinition(getClass(component), key, type);
		Object value = get(component, (String) definition[1]);
		return (value != null) ? value : definition[3];
	}
	
	/*TODO method get; component, font, color, keystroke, bean set/get*/
	
	/**
	 * Sets a new event handler method for a component
	 *
	 * @param component the target component
	 * @param key the key name of the parameter (e.g. <i>action</i>)
	 * @param value the method name and parameters
	 * (e.g. <i>foo(this, this.text, mybutton, mybutton.enabled)</i>
	 * for <i>public void foo(Object component, String text, Object mybutton, boolean enabled)</i>)
	 * @param root the search starting component for name components in the arguments
	 * @param handler the target event handler object including the method
	 * @throws java.lang.IllegalArgumentException
	 */
	public void setMethod(Object component, String key, String value, Object root, Object handler) {
		key = (String) getDefinition(getClass(component), key, "method")[1];
		Object[] method = getMethod(component, value, root, handler);
		set(component, key, method);
	}
	
	/**
	 * @return an object list including as follows:
	 * - handler object,
	 * - method,
	 * - list of parameters including 3 values:
	 * - ("thinlet", null, null) for the single thinlet component,
	 * - (target component, null, null) for named widget as parameter, e.g. mybutton,
	 * - (target, parameter name, default value) for a widget's given property, e.g. mylabel.enabled,
	 * - ("item", null, null) for an item of the target component as parameter, e.g. tree node,
	 * - ("item", parameter name, default value) for the item's given property e.g. list item's text.
	 */
	private Object[] getMethod(Object component, String value, Object root, Object handler) {
		StringTokenizer st  = new StringTokenizer(value, "(, )");
		String methodname = st.nextToken();
		int n = st.countTokens();
		Object[] data = new Object[2 + 3 * n];
		Class[] parametertypes = (n > 0) ? new Class[n] : null;
		for (int i = 0; i < n; i++) {
			String arg = st.nextToken();
			if ("thinlet".equals(arg)) {
				data[2 + 3 * i] = "thinlet"; // the target component
				parametertypes[i] = Thinlet.class;
			}
			else {
				int dot = arg.indexOf('.');
				String compname = (dot == -1) ? arg : arg.substring(0, dot);
				Object comp = null;
				String classname = null;
				if ("item".equals(compname)) {
					comp = "item";
					String parentclass = getClass(component);
					if ("list" == parentclass) { classname = "item"; }
					else if ("tree" == parentclass) { classname = "node"; }
					else if ("table" == parentclass) { classname = "row"; }
					else throw new IllegalArgumentException(parentclass + " has no item");
				}
				else {
					comp = ("this".equals(compname)) ? component : find(root, compname);
					classname = getClass(comp);
				}
				data[2 + 3 * i] = comp; // the target component
				if (dot == -1) {
					parametertypes[i] = Object.class; // Widget.class
				}
				else {
					Object[] definition = getDefinition(classname, arg.substring(dot + 1), null);
					data[2 + 3 * i + 1] = definition[1]; // parameter name, e.g. enabled
					data[2 + 3 * i + 2] = definition[3]; // default value, e.g. TRUE
					Object fieldclass = definition[0];
					if ((fieldclass == "string") || (fieldclass == "choice")) {
						parametertypes[i] = String.class;
					}
					else if (fieldclass == "boolean") {
						parametertypes[i] = Boolean.TYPE;
					}
					else if (fieldclass == "integer") {
						parametertypes[i] = Integer.TYPE;
					}
					else if (fieldclass == "icon") {
						parametertypes[i] = Image.class;
					}
					else throw new IllegalArgumentException((String) fieldclass);
				}
			}
		}
		data[0] = handler;
		try {
			data[1] = handler.getClass().getMethod(methodname, parametertypes);
			return data;
		} catch (Exception exc) {
			throw new IllegalArgumentException(value + " " + exc.getMessage());
		}
	}

	/**
	 *
	 */
	private void update(Object component, Object mode) {
		if ("parent" == mode) {
			component = getParent(component);
			mode = "validate";
		}
		boolean firstpaint = true;
		int x = 0; int y = 0; int width = 0; int height = 0;
		while (component != null) {
			if (!getBoolean(component, "visible", true)) { break; }
			if ("paint" == mode) {//|| (firstpaint && (component == content))
				int[] bounds = getRectangle(component, "bounds");
				if (bounds == null) { return; }
				if (firstpaint) {
					x = bounds[2]; y = bounds[3];
					width = Math.abs(bounds[0]); height = bounds[1];
					firstpaint = false;
				} else {
					x += bounds[2]; y += bounds[3];
				}
				if (component == content) {
					repaint(x, y, width, height);
				}
			}
			Object parent = getParent(component);
			String classname = getClass(parent);
			if ("combobox" == classname) {
				parent = get(parent, ":combolist");
			}
			else if ("menu" == classname) {
				parent = get(parent, ":popup");
			}
			else if (("paint" == mode) && ("tabbedpane" == classname)) {
				if (getItem(parent, getInteger(parent, "selected", 0)) != component) { break; }
			}
			if (("layout" == mode) || (("validate" == mode) &&
					(("list" == classname) || ("table" == classname) ||
					("tree" == classname) || ("dialog" == classname) || (parent == content)))) {
				int[] bounds = getRectangle(parent, "bounds");
				if (bounds == null) { return; }
				bounds[0] = -1 * Math.abs(bounds[0]);
				mode = "paint";
			}
			component = parent;
		}
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 *
	 */
	private boolean setString(Object component,
			String key, String value, String defaultvalue) {
                if (key.equals("text") || key.equals("tooltip")) {
                    putProperty(component, "i18n." + key, null);
                }
		return set(component, key, value);
	}

	/**
	 *
	 */
	private String getString(Object component,
			String key, String defaultvalue) {
		String text = (String)get(component, key);
                if (text == null) return defaultvalue;
                if (!(key.equals("text") || key.equals("tooltip"))) return text;
                if (langResource != null) {
                    boolean i18n = getBoolean(component, "i18n", true) && allI18n;
                    if (i18n) {
                        String ikey = (String)getProperty(component, "i18n." + key);
                        // initialize
                        if (ikey == null) {
                            ikey = text;
                            putProperty(component, "i18n." + key, ikey);
                        } else if (ikey.equals("__NONE__")) return text;
                        String itext = null;
                        try {
                            itext = langResource.getString(ikey);
                            if (itext != null) {
                                text = itext;
                            }
                        } catch (Exception e) {
                            // not found. Try default
                            if (langResourceDefault != null) {
                                try {
                                    itext = langResourceDefault.getString(ikey);
                                    if (itext != null) {
                                        text = itext;
                                    };
                                } catch (Exception e1) {
                                    putProperty(component, "i18n." + key, "__NONE__");
                                }
                            }
                        }
                    }
                }
		return text;
	}

	/**
	 *
	 * @throws java.lang.IllegalArgumentException
	 */
	private boolean setChoice(Object component,
			String key, String value, String[] values, String defaultvalue) {
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

	/**
	 *
	 */
	private boolean setIcon(Object component,
			String key, String path, Image defaultvalue) {
		return set(component, key, (path != null) ? getIcon(path) : defaultvalue);
	}

	/**
	 *
	 */
	private Image getIcon(Object component, String key, Image defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : (Image) value;
	}

	/**
	 *
	 */
	private boolean setBoolean(Object component,
			String key, boolean value, boolean defaultvalue) {
		return set(component, key, (value == defaultvalue) ? null :
			(value ? TRUE : FALSE));
	}

	/**
	 *
	 */
	private boolean getBoolean(Object component, 
			String key, boolean defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : ((Boolean) value).booleanValue();
	}

	/**
	 *
	 */
	private boolean setInteger(Object component,
			String key, int value, int defaultvalue) {
		return set(component, key, (value == defaultvalue) ? null : new Integer(value));
	}

	/**
	 *
	 */
	private int getInteger(Object component, String key, int defaultvalue) {
		Object value = get(component, key);
		return (value == null) ? defaultvalue : ((Integer) value).intValue();
	}

	/**
	 *
	 */
	private void setRectangle(Object component,
			String key, int x, int y, int width, int height) {
		int[] rectangle = getRectangle(component, key);
		if (rectangle != null) {
			rectangle[2] = x; rectangle[3] = y;
			rectangle[0] = width; rectangle[1] = height;
		}
		else {
			set(component, key, new Rectangle(x, y, width, height));
		}
	}

	/**
	 *
	 */
	private int[] getRectangle(Object component, String key) {
		return (Rectangle) get(component, key);
	}

	// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	/**
	 * Creates an image, and loads it immediately by default
	 *
	 * @param path is relative to your thinlet instance or the classpath
	 * (if the path starts with <i>'/'</i> character), or a full url
	 * @return the loaded image or null
	 */
	public Image getIcon(String path) {
		return getIcon(path, true);
	}

	/**
	 * Creates an image from the specified resource.
	 * To speed up loading the same images use a cache (a simple hashtable).
	 * And flush the resources being used by an image when you won't use it henceforward
	 *
	 * @param path is relative to your thinlet instance or the classpath, or an url
	 * @param preload waits for the whole image if true, starts loading
	 * (and repaints, and updates the layout) only when required (painted, or size requested) if false
	 * @return the loaded image or null
	 */
	public Image getIcon(String path, boolean preload) {
		if ((path == null) || (path.length() == 0)) {
			return null;
		}
		Image image = null; //(Image) imagepool.get(path);
		try {
			URL url = getClass().getResource(path); //ClassLoader.getSystemResource(path)
			if (url != null) { // contributed by Stefan Matthias Aust
				image = Toolkit.getDefaultToolkit().getImage(url);
			}
		} catch (Throwable e) {}
		if (image == null) {
			try {
				InputStream is = getClass().getResourceAsStream(path);
				//InputStream is = ClassLoader.getSystemResourceAsStream(path);
				if (is != null) {
					byte[] data = new byte[is.available()];
					is.read(data, 0, data.length);
					image = getToolkit().createImage(data);
					is.close();
				}
				else { // contributed by Wolf Paulus
					image = Toolkit.getDefaultToolkit().getImage(new URL(path));
				}
			} catch (Throwable e) {}
		}
		if (preload && (image != null)) {
			MediaTracker mediatracker = new MediaTracker(this);
			mediatracker.addImage(image, 1);
			try {
				mediatracker.waitForID(1, 50);
			} catch (InterruptedException ie) { }
			//imagepool.put(path, image);
		} 
		return image;
	}

	/**
	 * This method is called by the FrameLauncher if the window was closing,
	 * or AppletLauncher's destroy method. Overwrite it to e.g. save the application changes.
	 *
	 * @return true to exit, and false to keep the frame and continue the application
	 */
	public boolean destroy() {
		return true;
	}

	private static Object[] dtd;
	static {
		Integer integer_1 = new Integer(-1);
		Integer integer0 = new Integer(0);
		Integer integer1 = new Integer(1);
		String[] orientation = { "horizontal", "vertical" };
		String[] leftcenterright = { "left", "center", "right" };
		String[] selections = { "single", "interval", "multiple" }; //+none
		dtd = new Object[] {
			"component", null, new Object[][] {
				{ "string", "name", "", null },
				{ "boolean", "enabled", "paint", TRUE },
				{ "boolean", "visible", "parent", TRUE },
				{ "boolean", "i18n", "validate", FALSE },
				{ "string", "tooltip", "", null },
				{ "font", "font", "validate", null },
				{ "color", "foreground", "paint", null },
				{ "color", "background", "paint", null },
				{ "integer", "width", "validate", integer0 },
				{ "integer", "height", "validate", integer0 },
				{ "integer", "colspan", "validate", integer1 },
				{ "integer", "rowspan", "validate", integer1 },
				{ "integer", "weightx", "validate", integer0 },
				{ "integer", "weighty", "validate", integer0 },
				{ "choice", "halign", "validate",
					new String[] { "fill", "center", "left", "right" } },
				{ "choice", "valign", "validate",
				new String[] { "fill", "center", "top", "bottom" } },
				// component class String null*
				// parent Object null
				// (bounds) int[] 0 0 0 0
				{ "property", "property", "", null },
				{ "method", "init", "", null }
			},
			"label", "component", new Object[][] {
				{ "string", "text", "validate", null },
				{ "icon", "icon", "validate", null },
				{ "boolean", "i18n", "validate", FALSE },
			 	{ "choice", "alignment", "validate", leftcenterright },
			 	{ "integer", "mnemonic", "paint", integer_1 },
			 	{ "component", "for", "", null } },
			"button", "label", new Object[][] {
			 	{ "choice", "alignment", "validate", new String[] { "center", "left", "right" } },
				{ "method", "action", "", null },
				{ "choice", "type", "", new String[] { "normal", "default", "cancel", "link" } }
			},
			"checkbox", "label", new Object[][] {
				{ "boolean", "selected", "paint", FALSE }, //...group
				{ "string", "group", "paint", null }, //...group
				{ "method", "action", "", null } },
			"togglebutton", "checkbox", null,
			"combobox", "textfield", new Object[][] {
				{ "icon", "icon", "validate", null },
				{ "integer", "selected", "layout", integer_1 } },
			"choice", null, new Object[][] {
				{ "string", "name", "", null },
				{ "boolean", "enabled", "paint", TRUE },
				{ "boolean", "i18n", "validate", FALSE },
				{ "string", "text", "", null },
				{ "icon", "icon", "", null },
				{ "choice", "alignment", "", leftcenterright },
				{ "string", "tooltip", "", null },
				{ "font", "font", "validate", null },
				{ "color", "foreground", "paint", null },
				{ "color", "background", "paint", null },
				{ "property", "property", "", null } },
			"textfield", "component", new Object[][] {
				{ "string", "text", "layout", "" },
				{ "integer", "columns", "validate", integer0 },
				{ "boolean", "editable", "paint", TRUE },
				{ "boolean", "i18n", "validate", FALSE },
				{ "integer", "start", "layout", integer0 },
				{ "integer", "end", "layout", integer0 },
				{ "method", "action", "", null },
				{ "method", "insert", "", null },
				{ "method", "remove", "", null },
				{ "method", "caret", "", null },
				{ "method", "perform", "", null } },
			"passwordfield", "textfield", null,
			"textarea", "textfield", new Object[][] {
				{ "integer", "rows", "validate", integer0 },
				{ "boolean", "border", "validate", TRUE },
				{ "boolean", "wrap", "layout", FALSE } },
			"tabbedpane", "component", new Object[][] {
				{ "choice", "placement", "validate",
					new String[] { "top", "left", "bottom", "right", "stacked" } },
				{ "integer", "selected", "paint", integer0 },
				{ "method", "action", "", null } }, //...focus
			"tab", "choice", new Object[][] {
				{ "integer", "mnemonic", "paint", integer_1 } },
			"panel", "component", new Object[][] {
				{ "integer", "columns", "validate", integer0 },
				{ "integer", "top", "validate", integer0 },
				{ "integer", "left", "validate", integer0 },
				{ "integer", "bottom", "validate", integer0 },
				{ "integer", "right", "validate", integer0 },
				{ "integer", "gap", "validate", integer0 },
				{ "string", "text", "validate", null },
				{ "icon", "icon", "validate", null },
				{ "boolean", "i18n", "validate", FALSE },
				{ "boolean", "border", "validate", FALSE },
				{ "boolean", "scrollable", "validate", FALSE } },
			"desktop", "component", null,
			"dialog", "panel", new Object[][] {
				{ "string", "text", "", null },
				{ "icon", "icon", "", null },
				{ "boolean", "modal", "", FALSE } },
			"spinbox", "textfield", new Object[][] {
				{ "integer", "minimum", "paint", integer0 }, //...checkvalue
				{ "integer", "maximum", "paint", new Integer(100) },
				{ "integer", "step", "paint", integer1 },
				{ "integer", "value", "paint", integer0 } },
			"progressbar", "component", new Object[][] {
				{ "choice", "orientation", "validate", orientation },
				{ "integer", "minimum", "paint", integer0 }, //...checkvalue
				{ "integer", "maximum", "paint", new Integer(100) },
				{ "integer", "value", "paint", integer0 } },
				// change stringpainted
			"slider", "progressbar", new Object[][] {
				{ "integer", "unit", "", new Integer(5) },
				{ "integer", "block", "", new Integer(25) },
				{ "method", "action", "", null } },
				// minor/majortickspacing
				// inverted
				// labelincrement labelstart
			"splitpane", "component", new Object[][] {
				{ "choice", "orientation", "validate", orientation },
				{ "integer", "divider", "layout", integer_1 } },
			"list", "component", new Object[][] {
				{ "choice", "selection", "paint", selections },
				{ "method", "action", "", null },
				{ "method", "perform", "", null }, //...?
				{ "boolean", "line", "validate", TRUE } },
			"item", "choice", new Object[][] {
				{ "boolean", "selected", "", FALSE } },
			"table", "list", new Object[][] {
				/*{ "choice", "selection",
					new String[] { "singlerow", "rowinterval", "multiplerow",
						"cell", "cellinterval",
						"singlecolumn", "columninterval", "multiplecolumn" } }*/ },
			"header", null, null,
				// reordering allowed
				// autoresize mode: off next (column boundries) subsequents last all columns
				// column row selection
				// selection row column cell
				// editing row/column
			"column", "choice", new Object[][] {
				{ "integer", "width", "", new Integer(80) },
				{ "choice", "sort", "", new String[] { "none", "ascent", "descent" } } },
			"row", null, new Object[][] {
				{ "boolean", "selected", "", FALSE } },
			"cell", "choice", null,
			"tree", "list", new Object[][] {
				{ "method", "expand", "", null },
				{ "method", "collapse", "", null } },
			"node", "choice", new Object[][] {
				{ "boolean", "selected", "", FALSE },
				{ "boolean", "expanded", "", TRUE } },
			"separator", "component", null,
			"menubar", "component", new Object[][] {
				{ "choice", "placement", "validate",
					new String[] { "top", "bottom"} }
			},
			"menu", "choice", new Object[][] {
				{ "integer", "mnemonic", "paint", integer_1 } },
			"menuitem", "choice", new Object[][] {
				{ "keystroke", "accelerator", "", null },
				{ "method", "action", "", null },
				{ "integer", "mnemonic", "paint", integer_1 }
				//... KeyStroke=keyCode+modifiers(SHIFT CTRL META ALT_MASK)
			},
			"checkboxmenuitem", "menuitem", new Object[][] {
				{ "boolean", "selected", "paint", FALSE }, //...group
				{ "string", "group", "paint", null } }, //...group
			"popupmenu", "component", null, // Post menu: Shift+F10
			"bean", "component", new Object[][] {
				{ "bean", "bean", "", null }
			}
		};
	}	
}
