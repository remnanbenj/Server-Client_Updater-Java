import java.awt.Point;
import java.util.Stack;

public class Main implements Runnable {

	// Thread
	private Thread t;

	// Display
	private Window window;

	// Maze
	private Maze maze;
	private long startTime;

	public Main(String filename, int maxPeople) {
		System.out.println("Opening file: '" + filename + "'");
		System.out.println("Initial people: '" + maxPeople + "'");
		// Create maze
		this.maze = new Maze(filename, maxPeople);
		// Create window
		this.window = new Window(600, 600, maze);

		// Initial group
		Point entry = maze.getEntry();
		int dir = -1;
		if (entry.y == 0) {
			dir = 2;
		} else if (entry.y == maze.size - 1) {
			dir = 3;
		} else if (entry.x == 0) {
			dir = 1;
		} else if (entry.x == maze.size - 1) {
			dir = 0;
		}
		Group g = new Group(entry.y, entry.x, dir, maxPeople, maze, new Stack<Tile>(), new Stack<Integer>(), 0);
		startTime = System.currentTimeMillis();
		g.start();

		// Start main game loop
		start();
	}

	public Main(String filename, int maxPeople, int waitTime) {
		System.out.println("Opening file: '" + filename + "'");
		System.out.println("Initial people: '" + maxPeople + "'");
		System.out.println("Wait time: '" + waitTime + "ms'");
		// Create maze
		this.maze = new Maze(filename, maxPeople);
		// Create window
		this.window = new Window(600, 600, maze);

		// Initial group
		Point entry = maze.getEntry();
		int dir = -1;
		if (entry.y == 0) {
			dir = 2;
		} else if (entry.y == maze.size - 1) {
			dir = 3;
		} else if (entry.x == 0) {
			dir = 1;
		} else if (entry.x == maze.size - 1) {
			dir = 0;
		}
		Group g = new Group(entry.y, entry.x, dir, maxPeople, maze, new Stack<Tile>(), new Stack<Integer>(), waitTime);
		startTime = System.currentTimeMillis();
		g.start();

		// Start main game loop
		start();
	}

	/**
	 * Starts thread run loop
	 */
	public void start() {
		t = new Thread(this, "Main");
		t.start();
	}

	/**
	 * Main loop
	 */
	@Override
	public void run() {

		// Initialization fields
		long lastTime = System.nanoTime();
		int updatesPerSecond = 4;
		final double nanoSeconds = 1000000000.0 / updatesPerSecond;
		long timer = System.currentTimeMillis();
		double delta = 0;
		int frames = 0;
		int updates = 0;

		// Keep going until running is false
		while (true) {

			// Add to delta
			long now = System.nanoTime();
			delta += (now - lastTime) / nanoSeconds;
			lastTime = now;

			// When delta is equal or above one then an update is needed
			while (delta >= 1) {
				// Main update method
				updates++;
				delta -= 1;
			}

			// Main render method. Always try to render
			render();
			frames++;

			if (maze.allFinished) {
				maze.allFinished = !maze.allFinished;
				long result = (System.currentTimeMillis() - startTime);
				System.out.println("Time to complete: " + result + "ms");
			}

			// Print ups/fps 
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				// System.out.println(updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
		}
	}

	/**
	 * Main render method. Happens as much as possible
	 */
	private void render() {
		window.render();
	}

	/**
	 * Start the application
	 */
	public static void main(String args[]) {
		if (args.length == 2) {
			new Main(args[0], Integer.parseInt(args[1]));
		} else if (args.length == 3) {
			new Main(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else {
			System.out.println("Must supply 'filename' and 'amount of people' arguments.");
			System.out.println("And an optional 'waitTime' (ms) argument for adding a wait time between frames");
		}
	}

}