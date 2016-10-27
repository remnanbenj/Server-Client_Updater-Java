// Base on work from Lawrence Buck (Co-student)
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Maze {

	private List<Tile> mazeTiles = Collections.synchronizedList(new ArrayList<Tile>());
	private Point entry;
	public int size;
	public int tileSize;
	private List<Group> groups = Collections.synchronizedList(new ArrayList<Group>());
	private int initialPeople;
	private int returnedPeople;
	public boolean allFinished = false;

	public Maze(String fileName, int initialPeople) {
		this.initialPeople = initialPeople;
		InitialiseMaze(fileName);
	}

	// INIT METHODS

	/**
	 * Creates the maze tiles and puts them into the maze ArrayList
	 */
	private void InitialiseMaze(String fileName) {
		try {
			File file = new File(fileName);
			Scanner scan = new Scanner(file);

			// Get size and entry points
			size = scan.nextInt();
			tileSize = 600 / size;
			entry = new Point(scan.nextInt(), scan.nextInt());
			scan.nextLine(); // clear rest of line

			// Loop to create tiles in the maze
			for (int j = 0; j < size; ++j) {
				String row = scan.nextLine();
				for (int i = 0; i < size; ++i) {
					char c = row.charAt(i);
					if (i == entry.y && j == entry.x) {
						mazeTiles.add(new Tile(i, j, Tile.Type.Entry)); // is the entry
					} else if (c == 'X') {
						mazeTiles.add(new Tile(i, j, Tile.Type.Wall)); // is a wall
					} else {
						mazeTiles.add(new Tile(i, j, Tile.Type.Hallway)); // is NOT a wall (could be junction or dead end)
					}
				}
			}

			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Setup junction, path and dead end tiles (can't do these while creating the maze)
		CheckHallways();
	}

	/**
	 * Checks hallway tiles to see if they're a junction or a dead end and tags them so
	 */
	private void CheckHallways() {
		for (Tile t : mazeTiles) {
			// if t.type == Tile.Type.Hallway
			// Check NSEW for paths and count how many
			// if paths > 2
			// t.type == Tile.Type.Junction
			// set NSEW that were paths t.type == Tile.Type.Path
			// else if paths < 2
			// t.type == Tile.Type.Deadend
			if (t.type == Tile.Type.Hallway) {
				int paths = 0;
				Tile.PathState nsew[] = new Tile.PathState[4];
				if (t.x > 0) {
					// Check west
					if (getTileAt(t.x - 1, t.y).type != Tile.Type.Wall) {
						paths++;
						nsew[3] = Tile.PathState.Unexplored;
					}
				}
				if (t.x < size - 1) {
					// Check east
					if (getTileAt(t.x + 1, t.y).type != Tile.Type.Wall) {
						paths++;
						nsew[2] = Tile.PathState.Unexplored;
					}
				}
				if (t.y > 0) {
					// Check North
					if (getTileAt(t.x, t.y - 1).type != Tile.Type.Wall) {
						paths++;
						nsew[0] = Tile.PathState.Unexplored;
					}
				}
				if (t.y < size - 1) {
					// Check South
					if (getTileAt(t.x, t.y + 1).type != Tile.Type.Wall) {
						paths++;
						nsew[1] = Tile.PathState.Unexplored;
					}
				}
				if (paths > 2) {
					t.type = Tile.Type.Junction;
					t.setNSEW(0, nsew[0]);
					t.setNSEW(1, nsew[1]);
					t.setNSEW(2, nsew[2]);
					t.setNSEW(3, nsew[3]);
					t.paths = paths;
				} else if (paths < 2) {
					if (t.x == 0 || t.x == size - 1 || t.y == 0 || t.y == size - 1)
						t.type = Tile.Type.Exit;
					else
						t.type = Tile.Type.Deadend;
				}
			}
		}
	}

	// PUBLIC

	/**
	 * Returns tile at position x, y. If there is no tile or the arguments are either negative or bigger than the size of the maze this will return null.
	 */
	public Tile getTileAt(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size) {
			return null;
		}
		return mazeTiles.get(x + y * size);
	}

	public Point getEntry() {
		return entry;
	}

	public void addGroup(Group group) {
		groups.add(group);
	}

	public void returnPeople(int amt) {
		returnedPeople += amt;
		if (returnedPeople == initialPeople) {
			System.out.println("All " + returnedPeople + "/" + initialPeople + " people have made it out.");
			allFinished = true;
		} else {

			System.out.println(returnedPeople + "/" + initialPeople + " people have made it out.");
		}
	}

	// RENDER METHODS

	public void render(int pixels[]) {

		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				drawTile(getTileAt(i, j), pixels, i * tileSize, j * tileSize);
			}
		}

		// Draw groups
		for (Group g : groups) {
			for (int j = g.y * tileSize; j < g.y * tileSize + tileSize; j++) {
				for (int i = g.x * tileSize; i < g.x * tileSize + tileSize; i++) {
					if (i < g.x * tileSize + 5 || i > g.x * tileSize + tileSize - 6 || j < g.y * tileSize + 5 || j > g.y * tileSize + tileSize - 6)
						continue;
					pixels[i + j * tileSize * size] = 0xffa0a0a0;
				}
			}
		}

	}

	private void drawTile(Tile t, int pixels[], int xw, int yw) {
		if (t.type == Tile.Type.Wall) {
			for (int j = yw; j < yw + tileSize; j++) {
				for (int i = xw; i < xw + tileSize; i++) {
					pixels[i + j * tileSize * size] = 0xff000000;
				}
			}
		} else if (t.type == Tile.Type.Entry) {
			for (int j = yw; j < yw + tileSize; j++) {
				for (int i = xw; i < xw + tileSize; i++) {
					pixels[i + j * tileSize * size] = 0xff00ffaa;
				}
			}
		} else if (t.type == Tile.Type.Junction) {
			for (int j = yw; j < yw + tileSize; j++) {
				for (int i = xw; i < xw + tileSize; i++) {
					if (j < yw + 3) {
						if (t.getNSEW(0) == Tile.PathState.Live) {
							pixels[i + j * tileSize * size] = 0xffFFC1E7;
							continue;
						} else if (t.getNSEW(0) == Tile.PathState.Unexplored) {
							pixels[i + j * tileSize * size] = 0xff0008ff;
							continue;
						} else if (t.getNSEW(0) == Tile.PathState.Dead || t.getNSEW(0) == Tile.PathState.Wall) {
							pixels[i + j * tileSize * size] = 0xff000000;
							continue;
						} else if (t.getNSEW(0) == Tile.PathState.Exit) {
							pixels[i + j * tileSize * size] = 0xffCEC029;
							continue;
						}
					} else if (j > yw + tileSize - 4) {
						if (t.getNSEW(1) == Tile.PathState.Live) {
							pixels[i + j * tileSize * size] = 0xffFFC1E7;
							continue;
						} else if (t.getNSEW(1) == Tile.PathState.Unexplored) {
							pixels[i + j * tileSize * size] = 0xff0008ff;
							continue;
						} else if (t.getNSEW(1) == Tile.PathState.Dead || t.getNSEW(1) == Tile.PathState.Wall) {
							pixels[i + j * tileSize * size] = 0xff000000;
							continue;
						} else if (t.getNSEW(1) == Tile.PathState.Exit) {
							pixels[i + j * tileSize * size] = 0xffCEC029;
							continue;
						}
					} else if (i > xw + tileSize - 4) {
						if (t.getNSEW(2) == Tile.PathState.Live) {
							pixels[i + j * tileSize * size] = 0xffFFC1E7;
							continue;
						} else if (t.getNSEW(2) == Tile.PathState.Unexplored) {
							pixels[i + j * tileSize * size] = 0xff0008ff;
							continue;
						} else if (t.getNSEW(2) == Tile.PathState.Dead || t.getNSEW(2) == Tile.PathState.Wall) {
							pixels[i + j * tileSize * size] = 0xff000000;
							continue;
						} else if (t.getNSEW(2) == Tile.PathState.Exit) {
							pixels[i + j * tileSize * size] = 0xffCEC029;
							continue;
						}
					} else if (i < xw + 3) {
						if (t.getNSEW(3) == Tile.PathState.Live) {
							pixels[i + j * tileSize * size] = 0xffFFC1E7;
							continue;
						} else if (t.getNSEW(3) == Tile.PathState.Unexplored) {
							pixels[i + j * tileSize * size] = 0xff0008ff;
							continue;
						} else if (t.getNSEW(3) == Tile.PathState.Dead || t.getNSEW(3) == Tile.PathState.Wall) {
							pixels[i + j * tileSize * size] = 0xff000000;
							continue;
						} else if (t.getNSEW(3) == Tile.PathState.Exit) {
							pixels[i + j * tileSize * size] = 0xffCEC029;
							continue;
						}
					}
					pixels[i + j * tileSize * size] = 0xffff00ff;
				}
			}
		} else if (t.type == Tile.Type.Deadend) {
			for (int j = yw; j < yw + tileSize; j++) {
				for (int i = xw; i < xw + tileSize; i++) {
					pixels[i + j * tileSize * size] = 0xff00aaff;
				}
			}
		} else if (t.type == Tile.Type.Exit) {
			for (int j = yw; j < yw + tileSize; j++) {
				for (int i = xw; i < xw + tileSize; i++) {
					pixels[i + j * tileSize * size] = 0xffff0000;
				}
			}
		} else {
			for (int j = yw; j < yw + tileSize; j++) {
				for (int i = xw; i < xw + tileSize; i++) {
					pixels[i + j * tileSize * size] = 0xffffffff;
				}
			}
		}
	}
}