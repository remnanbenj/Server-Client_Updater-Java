package window.components;

import client.Client;
import util.ImageToPixels;

public class DLBarCurrent {
	private int x;
	private int y;
	private int width;
	private int height;
	private int[] dlpixels;
	private ImageToPixels image = new ImageToPixels("dlbar");

	// Downloading client
	private Client client;

	public DLBarCurrent() {
		this.x = 168;
		this.y = 307;
		this.width = image.getWidth();
		this.height = image.getHeight();
		dlpixels = image.getPixels();
	}

	private void SetupPixels() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x < 2 || x > width - 3 || y < 2 || y > height - 3)
					continue;
				if (client.checkIfUpdateNeeded() && client.getPercent() != 100 && x > width / 100 * client.getPercent()) {
					continue;
				}
				dlpixels[x + y * width] = 0xffBFFFFC;
			}
		}
	}

	public void render(int pixels[], int screenWidth, int screenHeight) {
		SetupPixels();
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

	public void giveClient(Client client) {
		this.client = client;
	}
}
