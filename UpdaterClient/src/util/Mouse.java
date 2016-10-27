package util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import window.Window;

public class Mouse implements MouseListener {

	private Window window;

	public Mouse(Window window) {
		this.window = window;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		window.clicked(e.getX(), e.getY());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
