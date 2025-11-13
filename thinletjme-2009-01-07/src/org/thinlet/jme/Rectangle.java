package org.thinlet.jme;
public class Rectangle {
	public int x, y, width, height;

	public Rectangle() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
	}

	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean contains(int rx, int ry) {
		if ((x <= rx) && (x + width >= rx) && (y <= ry) && (y + height >= ry))
			return true;
		return false;
	}

	public boolean contains(Rectangle r) {
		if ((x <= r.x) && ((x + width) >= (r.x + r.width)) && (y <= r.y)
				&& ((y + height) >= (r.y + r.height))) {
			return true;
		}
		return false;
	}
}
