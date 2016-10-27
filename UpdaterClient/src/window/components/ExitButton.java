package window.components;

import util.ImageToPixels;
import client.Client;

public class ExitButton {
	private int x;
	private int y;
	private int width;
	private int height;
	private ImageToPixels image = new ImageToPixels("exit");

	// Downloading client
	private Client client;

	public ExitButton() {
		this.x = 566;
		this.y = 3;
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

	/** Returns true if x and y are contained in this button */
	public boolean contains(int x, int y) {
		if (x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height)
			return true;
		return false;
	}

	public void giveClient(Client client) {
		this.client = client;
	}

	public void click() {
		client.exit();
	}
}
