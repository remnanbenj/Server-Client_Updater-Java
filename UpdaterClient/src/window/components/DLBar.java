package window.components;

import util.ImageToPixels;

public class DLBar {
	private int x;
	private int y;
	private int width;
	private int height;
	private ImageToPixels image = new ImageToPixels("dlbar");

	public DLBar() {
		this.x = 168;
		this.y = 307;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}

	public void render(int pixels[], int screenWidth, int screenHeight) {
		int xW = 0;
		int yW = 0;
		for (int j = y; j < y + height; j++) {
			for (int i = x; i < x + width; i++) {
				pixels[i + j * screenWidth] = image.getPixels()[xW + yW * width];
				xW++;
			}
			xW = 0;
			yW++;
		}
	}
}
