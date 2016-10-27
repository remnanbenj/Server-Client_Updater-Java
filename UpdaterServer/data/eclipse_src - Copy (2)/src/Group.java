import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class Group implements Runnable {

	public int x;
	public int y;
	private int people;
	private int dir;
	private Maze maze;
	private Thread t;
	private boolean hitDead = false;
	// private Map<Tile, Integer> juncts;
	private Stack<Tile> junctsTiles;
	private Stack<Integer> junctsEntry;
	private boolean traceBackExit = false;
	private boolean followingExit = false;
	private boolean atExit = false;
	private static int waitTime = 0;

	public Group(int x, int y, int dir, int people, Maze maze, Stack<Tile> junctsTiles, Stack<Integer> junctsEntry, int waitTime) {
		// init x, y and people
		this.x = x;
		this.y = y;
		this.people = people;
		this.dir = dir;
		this.maze = maze;
		// this.juncts = juncts;
		this.junctsTiles = new Stack<Tile>();
		this.junctsEntry = new Stack<Integer>();
		for (int i = 0; i < junctsTiles.size(); i++) {
			this.junctsTiles.add(junctsTiles.get(i));
			this.junctsEntry.add(junctsEntry.get(i));
		}
		Group.waitTime = waitTime;
		maze.addGroup(this);
	}

	public void start() {
		t = new Thread(this);
		t.start();
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			move();
		}
	}

	public void move() {
		if (atExit)
			return;
		// Try go north
		if (dir == 0) {
			if (maze.getTileAt(x, y - 1).type == Tile.Type.Entry && traceBackExit) {
				traceBackExit = false;
			} else if (maze.getTileAt(x, y - 1).type == Tile.Type.Exit) {
				y--;
				if (!followingExit) {
					System.out.println("Found Exit. Tracing path back.");
					traceBackExit = true;
					dir = oppDir(dir);
				} else {
					maze.returnPeople(people);
					atExit = true;
				}
			} else if (maze.getTileAt(x, y - 1).type == Tile.Type.Junction) {
				// Move north into the junction
				y--;
				handleJunct(maze.getTileAt(x, y));
			} else if (maze.getTileAt(x, y - 1).type == Tile.Type.Deadend || maze.getTileAt(x, y - 1).type == Tile.Type.Entry) {
				// Move north into the dead end
				y--;
				// Return to last junction
				dir = oppDir(dir);
				hitDead = true;
			} else if (maze.getTileAt(x, y - 1).type != Tile.Type.Wall) {
				// Just move north
				y--;
			} else {
				getNextHallway();
			}
		} else if (dir == 2) {
			if (maze.getTileAt(x + 1, y).type == Tile.Type.Entry && traceBackExit) {
				traceBackExit = false;
			} else if (maze.getTileAt(x + 1, y).type == Tile.Type.Exit) {
				x++;
				if (!followingExit) {
					System.out.println("Found Exit. Tracing path back.");
					traceBackExit = true;
					dir = oppDir(dir);
				} else {
					maze.returnPeople(people);
					atExit = true;
				}
			} else if (maze.getTileAt(x + 1, y).type == Tile.Type.Junction) {
				// Move east
				x++;
				handleJunct(maze.getTileAt(x, y));
			} else if (maze.getTileAt(x + 1, y).type == Tile.Type.Deadend || maze.getTileAt(x + 1, y).type == Tile.Type.Entry) {
				x++;
				// Return to last junction
				dir = oppDir(dir);
				hitDead = true;
			} else if (maze.getTileAt(x + 1, y).type != Tile.Type.Wall) {
				// Move east
				x++;
			} else {
				getNextHallway();
			}
		} else if (dir == 1) {
			if (maze.getTileAt(x, y + 1).type == Tile.Type.Entry && traceBackExit) {
				traceBackExit = false;
			} else if (maze.getTileAt(x, y + 1).type == Tile.Type.Exit) {
				y++;
				if (!followingExit) {
					System.out.println("Found Exit. Tracing path back.");
					traceBackExit = true;
					dir = oppDir(dir);
				} else {
					maze.returnPeople(people);
					atExit = true;
				}
			} else if (maze.getTileAt(x, y + 1).type == Tile.Type.Junction) {
				// Move south
				y++;
				handleJunct(maze.getTileAt(x, y));
			} else if (maze.getTileAt(x, y + 1).type == Tile.Type.Deadend || maze.getTileAt(x, y + 1).type == Tile.Type.Entry) {
				y++;
				// Return to last junction
				dir = oppDir(dir);
				hitDead = true;
			} else if (maze.getTileAt(x, y + 1).type != Tile.Type.Wall) {
				// Move south
				y++;
			} else {
				getNextHallway();
			}
		} else if (dir == 3) {
			if (maze.getTileAt(x - 1, y).type == Tile.Type.Entry && traceBackExit) {
				traceBackExit = false;
			} else if (maze.getTileAt(x - 1, y).type == Tile.Type.Exit) {
				x--;
				if (!followingExit) {
					System.out.println("Found Exit. Tracing path back.");
					traceBackExit = true;
					dir = oppDir(dir);
				} else {
					maze.returnPeople(people);
					atExit = true;
				}
			} else if (maze.getTileAt(x - 1, y).type == Tile.Type.Junction) {
				// Move west
				x--;
				handleJunct(maze.getTileAt(x, y));
			} else if (maze.getTileAt(x - 1, y).type == Tile.Type.Deadend || maze.getTileAt(x - 1, y).type == Tile.Type.Entry) {
				x--;
				// Return to last junction
				dir = oppDir(dir);
				hitDead = true;
			} else if (maze.getTileAt(x - 1, y).type != Tile.Type.Wall) {
				// Move west
				x--;
			} else {
				getNextHallway();
			}
		}
	}

	private void getNextHallway() {
		// If we're facing north
		if (dir == 0) {
			// Move east
			if (maze.getTileAt(x + 1, y).type != Tile.Type.Wall) {
				dir = 2;
			}
			// Move west
			else if (maze.getTileAt(x - 1, y).type != Tile.Type.Wall) {
				dir = 3;
			}
		}
		// If we're facing south
		else if (dir == 1) {
			// Move west
			if (maze.getTileAt(x - 1, y).type != Tile.Type.Wall) {
				dir = 3;
			}
			// Move east
			else if (maze.getTileAt(x + 1, y).type != Tile.Type.Wall) {
				dir = 2;
			}
		}
		// If we're facing east
		else if (dir == 2) {
			// Move south
			if (maze.getTileAt(x, y + 1).type != Tile.Type.Wall) {
				dir = 1;
			}
			// Move north
			else if (maze.getTileAt(x, y - 1).type != Tile.Type.Wall) {
				dir = 0;
			}
		}
		// If we're facing west
		else if (dir == 3) {
			// Move north
			if (maze.getTileAt(x, y - 1).type != Tile.Type.Wall) {
				dir = 0;
			}
			// Move south
			else if (maze.getTileAt(x, y + 1).type != Tile.Type.Wall) {
				dir = 1;
			}
		}
		move();
	}

	private void handleJunct(Tile t) {
		// We're tracing back to the exit. Lets follow our path back
		if (traceBackExit) {
			t.setNSEW(oppDir(dir), Tile.PathState.Exit);
			dir = junctsEntry.pop();
			junctsTiles.pop();
			return;
		}

		// if junction has exit. Just follow it
		if (t.getExitPaths() != -1) {
			followingExit = true;
			dir = t.getExitPaths();
			return;
		}

		// If we hit a dead end where we came from then set that direction as dead
		if (hitDead) {
			t.setNSEW(oppDir(dir), Tile.PathState.Dead);
			hitDead = false;
		}

		// If we haven't yet met this junct then note the entry we came in from and mark that entry with live
		/*
		 * if (!juncts.containsKey(t)) { t.setNSEW(oppDir(dir), Tile.PathState.Live); juncts.put(t, oppDir(dir)); }
		 */

		if (!junctsTiles.contains(t)) {
			t.setNSEW(oppDir(dir), Tile.PathState.Live);
			junctsEntry.push(oppDir(dir));
			junctsTiles.push(t);
		}

		// If we have 3 total paths we need to split into 2 groups to explore the new paths
		if (t.getUnexploredPaths() == 2) {
			if (people < 2) {
				// not enough to split up
				this.dir = getNextPath(t);
			} else {
				if (people % 2 == 1) {
					int newPeople1 = people / 2 + 1;
					int newPeople2 = people / 2;
					this.people = newPeople1;
					this.dir = getNextPath(t);
					Group g = new Group(x, y, getNextPath(t), newPeople2, maze, junctsTiles, junctsEntry, waitTime);
					g.start();
				} else {
					int newPeople1 = people / 2;
					int newPeople2 = people / 2;
					this.people = newPeople1;
					this.dir = getNextPath(t);
					Group g = new Group(x, y, getNextPath(t), newPeople2, maze, junctsTiles, junctsEntry, waitTime);
					g.start();
				}
			}
		}
		// Else if we have 4 total paths we need to split into 3 groups
		else if (t.getUnexploredPaths() == 3) {
			if (people < 2) {
				// not enough to split up
				this.dir = getNextPath(t);
			} else if (people < 2) {
				int newPeople1 = people / 2 + 1;
				int newPeople2 = people / 2;
				this.people = newPeople1;
				this.dir = getNextPath(t);
				Group g = new Group(x, y, getNextPath(t), newPeople2, maze, junctsTiles, junctsEntry, waitTime);
				g.start();
			} else {
				if (people % 3 == 1) {
					int newPeople1 = people / 3 + 1;
					int newPeople2 = people / 3;
					int newPeople3 = people / 3;
					this.people = newPeople1;
					this.dir = getNextPath(t);
					Group g = new Group(x, y, getNextPath(t), newPeople2, maze, junctsTiles, junctsEntry, waitTime);
					Group g2 = new Group(x, y, getNextPath(t), newPeople3, maze, junctsTiles, junctsEntry, waitTime);
					g.start();
					g2.start();
				} else if (people % 3 == 2) {
					int newPeople1 = people / 3 + 1;
					int newPeople2 = people / 3 + 1;
					int newPeople3 = people / 3;
					this.people = newPeople1;
					this.dir = getNextPath(t);
					Group g = new Group(x, y, getNextPath(t), newPeople2, maze, junctsTiles, junctsEntry, waitTime);
					Group g2 = new Group(x, y, getNextPath(t), newPeople3, maze, junctsTiles, junctsEntry, waitTime);
					g.start();
					g2.start();
				} else {
					int newPeople1 = people / 3;
					int newPeople2 = people / 3;
					int newPeople3 = people / 3;
					this.people = newPeople1;
					this.dir = getNextPath(t);
					Group g = new Group(x, y, getNextPath(t), newPeople2, maze, junctsTiles, junctsEntry, waitTime);
					Group g2 = new Group(x, y, getNextPath(t), newPeople3, maze, junctsTiles, junctsEntry, waitTime);
					g.start();
					g2.start();
				}
			}
		} else {
			this.dir = getNextPath(t);
		}
	}

	private int getNextPath(Tile t) {
		// If any are unexplored follow them and set them to live
		if (t.getNSEW(0) == Tile.PathState.Unexplored && junctsEntry.peek() != 0) {
			t.setNSEW(0, Tile.PathState.Live);
			return 0;
		} else if (t.getNSEW(2) == Tile.PathState.Unexplored && junctsEntry.peek() != 2) {
			t.setNSEW(2, Tile.PathState.Live);
			return 2;
		} else if (t.getNSEW(1) == Tile.PathState.Unexplored && junctsEntry.peek() != 1) {
			t.setNSEW(1, Tile.PathState.Live);
			return 1;
		} else if (t.getNSEW(3) == Tile.PathState.Unexplored && junctsEntry.peek() != 3) {
			t.setNSEW(3, Tile.PathState.Live);
			return 3;
		}

		// Else if there are any live paths follow one at random
		ArrayList<Integer> count = new ArrayList<Integer>();
		if (t.getNSEW(0) == Tile.PathState.Live && junctsEntry.peek() != 0 && oppDir(dir) != 0) {
			count.add(0);
		}
		if (t.getNSEW(3) == Tile.PathState.Live && junctsEntry.peek() != 3 && oppDir(dir) != 3) {
			count.add(3);
		}
		if (t.getNSEW(1) == Tile.PathState.Live && junctsEntry.peek() != 1 && oppDir(dir) != 1) {
			count.add(1);
		}
		if (t.getNSEW(2) == Tile.PathState.Live && junctsEntry.peek() != 2 && oppDir(dir) != 2) {
			count.add(2);
		}
		if (count.size() > 0) {
			Random rand = new Random();
			int result = count.get(rand.nextInt(count.size()));
			// System.out.println("Count: " + count.size() + ", Entry: " + junctsEntry.peek());
			// System.out.println();
			return result;
		}

		// Else return the way you came and mark this as a dead junction
		hitDead = true;

		t.setNSEW(junctsEntry.peek(), Tile.PathState.Dead);
		junctsTiles.pop();
		return junctsEntry.pop();
	}

	/** Return the opposite direction */
	private int oppDir(int dir) {
		if (dir == 0)
			return 1;
		else if (dir == 1)
			return 0;
		else if (dir == 2)
			return 3;
		else if (dir == 3)
			return 2;
		return dir;
	}
}
