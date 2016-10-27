package window;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import util.ImageToPixels;
import util.Mouse;
import window.components.DLBar;
import window.components.DLBarCurrent;
import window.components.ExitButton;
import window.components.MaxButton;
import window.components.MinButton;
import window.components.PlayButton;
import client.Client;

public class Window extends Canvas {
	private static final long serialVersionUID = 1L;

	// Screen Size
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenW = (int) screenSize.getWidth();
	private int screenH = (int) screenSize.getHeight();
	private Point cetrePoint = new Point(screenW / 2, screenH / 2);

	// Frame for window
	private JFrame frame;
	private int width;
	private int height;

	// Pixels to render into
	private BufferedImage image;
	private int[] pixels;
	private int backgroundColor = 0xffff0000;

	// Assets to render
	private ImageToPixels background = new ImageToPixels("background");
	private PlayButton playButton = new PlayButton();
	private ExitButton exitButton = new ExitButton();
	private MaxButton maxButton = new MaxButton();
	private MinButton minButton = new MinButton();
	private DLBar dlbar = new DLBar();
	private DLBarCurrent dlbarc = new DLBarCurrent();

	// Network
	private Client client;

	public Window(int width, int height, Client client) {
		this.client = client;
		dlbarc.giveClient(client);
		playButton.giveClient(client);
		exitButton.giveClient(client);
		this.width = width;
		this.height = height;
		windowSetup();
		canvasSetup();
	}

	/** Initialize the contents of the frame. */
	private void windowSetup() {
		frame = new JFrame();
		frame.setBounds(cetrePoint.x - width / 2, cetrePoint.y - height / 2, width, height);
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height);
		frame.add(this);
		frame.setVisible(true);
		Mouse mouse = new Mouse(this);
		addMouseListener(mouse);
	}

	/** Sets up canvas with width and height and with black as the background color */
	private void canvasSetup() {
		// Setup pixels array
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		// Set pixels as background color
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				pixels[x + y * getWidth()] = backgroundColor;
			}
		}

		requestFocus();
	}

	/**
	 * Renders all the components to the image and displays them
	 */
	public void render() {
		// Get buffer strategy
		BufferStrategy bs = getBufferStrategy();

		// If BufferStrategy doesn't exist yet make one
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		// Set pixels as background image
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				pixels[x + y * getWidth()] = background.getPixels()[x + y * getWidth()];
			}
		}

		// Render on top of the background here
		playButton.render(pixels, width, height);
		exitButton.render(pixels, width, height);
		maxButton.render(pixels, width, height);
		minButton.render(pixels, width, height);
		dlbar.render(pixels, width, height);
		dlbarc.render(pixels, width, height);

		// Draws the pixels to the buffered image
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, width, height, null);

		// Draw text on top here
		g.setColor(new Color(0xff000000));

		g.setFont(new Font("Arial", 0, 10));
		String text = "Current Version: " + client.getVersion();
		int stringWidth = g.getFontMetrics().stringWidth(text);
		drawText(g, getWidth() - stringWidth - 30, getHeight() - 46, text);

		g.setFont(new Font("Arial", 0, 14));
		if (!client.isConnected()) {
			text = "Could not connect to server";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, getWidth() / 2 - 30, getHeight() - 106, text);

			g.setColor(new Color(0xff303030));
			g.setFont(new Font("Arial Bold", 0, 30));
			text = "Launch";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, 82 - stringWidth / 2, getHeight() - 74, text);
		} else if (client.isUpdating()) {
			text = client.getPercent() + "% complete";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, getWidth() / 2, getHeight() - 86, text);

			text = "Updating to version: " + client.getNewVersion();
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, getWidth() / 2 + 45 - stringWidth / 2, getHeight() - 106, text);

			g.setColor(new Color(0xff303030));
			g.setFont(new Font("Arial Bold", 0, 30));
			text = "Updating";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, 82 - stringWidth / 2, getHeight() - 74, text);
		} else if (client.checkIfUpdateNeeded()) {
			text = client.getPercent() + "% complete";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, getWidth() / 2, getHeight() - 86, text);

			text = "Update to version: " + client.getNewVersion();
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, getWidth() / 2 + 45 - stringWidth / 2, getHeight() - 106, text);

			g.setColor(new Color(0xff303030));
			g.setFont(new Font("Arial Bold", 0, 30));
			text = "Update";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, 82 - stringWidth / 2, getHeight() - 74, text);
		} else {
			text = "Up to date";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, getWidth() / 2 + 15, getHeight() - 106, text);

			g.setColor(new Color(0xff303030));
			g.setFont(new Font("Arial Bold", 0, 30));
			text = "Launch";
			stringWidth = g.getFontMetrics().stringWidth(text);
			drawText(g, 82 - stringWidth / 2, getHeight() - 74, text);
		}

		g.setColor(new Color(0xfffffaad));
		g.setFont(new Font("Arial", 0, 18));
		text = "Application Title";
		stringWidth = g.getFontMetrics().stringWidth(text);
		drawText(g, 10, -3, text);

		// Rid the graphics and show buffered image
		g.dispose();
		bs.show();
	}

	/**
	 * Draws text on graphics to render at x, y
	 */
	private void drawText(Graphics g, int x, int y, String string) {
		char[] chars = new char[string.length()];
		for (int i = 0; i < string.length(); i++) {
			chars[i] = string.charAt(i);
		}
		g.drawChars(chars, 0, chars.length, x + 10, y + 28);
	}

	public void clicked(int x, int y) {
		if (playButton.contains(x, y)) {
			playButton.click();
		} else if (exitButton.contains(x, y)) {
			exitButton.click();
		}
	}
}
