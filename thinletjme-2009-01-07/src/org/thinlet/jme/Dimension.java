package org.thinlet.jme;

public class Dimension {

	public int width;
	public int height;

	public Dimension() {
		width = 0;
		height = 0;
	}

	public Dimension(Dimension d) {
		this.width = d.width;
		this.height = d.height;
	}

	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
