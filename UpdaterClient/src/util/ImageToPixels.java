package util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImageToPixels {
	private int[] imagePixels;
	private int imageWidth;
	private int imageHeight;

	private String dir = "resource\\";
	private String type = ".png";
	private String path;

	public ImageToPixels(String path) {
		this.path = path;
		get(getFile());
	}

	private File getFile() {
		File result = new File(dir + path + type);
		if (result.exists())
			return result;
		else
			return null;
	}

	private void get(File file) {
		try {
			if (!file.exists()) {
				System.out.println("FILE DOES NOT EXIST");
				return;
			}
			BufferedImage image = ImageIO.read(file);
			imageWidth = image.getWidth();
			imageHeight = image.getHeight();
			imagePixels = new int[imageWidth * imageHeight];
			for (int y = 0; y < imageHeight; y++) {
				for (int x = 0; x < imageWidth; x++) {
					int color = image.getRGB(x, y);
					imagePixels[x + y * imageWidth] = color;
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR LOADING IMAGE: " + path);
		}
	}

	public int[] getPixels() {
		return imagePixels;
	}

	public int getWidth() {
		return imageWidth;
	}

	public int getHeight() {
		return imageHeight;
	}

}
