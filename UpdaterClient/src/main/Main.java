package main;

import client.Client;
import window.Window;

public class Main implements Runnable {

	// Thread
	private Thread t;

	// Display
	private Window window;

	// Client
	private Client client;

	public Main() {
		// Create client and window
		this.client = new Client();
		this.window = new Window(600, 400, client);
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
		int updatesPerSecond = 60;
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
				update();
				updates++;
				delta -= 1;
			}

			// Main render method. Always try to render
			render();
			frames++;

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
	 * Main update method. Happens 60 times per second
	 */
	private void update() {
		client.update();
	}

	/**
	 * Start the application
	 */
	public static void main(String args[]) {
		new Main();
	}

}
