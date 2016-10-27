import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Window extends Canvas {

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

	// Maze object
	private Maze maze;

	/**
	 * Create the application.
	 */
	public Window(int width, int height, Maze maze) {
		this.width = width;
		this.height = height;
		this.maze = maze;
		windowSetup();
		canvasSetup();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void windowSetup() {
		frame = new JFrame();
		frame.setBounds(cetrePoint.x - width / 2, cetrePoint.y - height / 2, width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
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
				pixels[x + y * getWidth()] = 0xffff0000;
			}
		}

		// Render on top of the background here
		maze.render(pixels);

		// Draws the pixels to the buffered image
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, width, height, null);

		// Draw text on top here

		// Rid the graphics and show buffered image
		g.dispose();
		bs.show();
	}

}
