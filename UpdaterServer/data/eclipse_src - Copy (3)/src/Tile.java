public class Tile {

	public enum Type {
		Hallway, Junction, Deadend, Wall, Entry, Exit
	}

	public enum PathState {
		Wall, Unexplored, Live, Dead, Exit
	}

	// For init
	public int x, y;
	public Type type;
	private PathState[] nsew = new PathState[4];
	public int paths = 0;

	public Tile(int x, int y, Type type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	@Override
	public String toString() {
		if (type == Type.Junction)
			return "Junction (" + x + ", " + y + ") " + type;
		else if (type == Type.Deadend)
			return "Deadend (" + x + ", " + y + ") " + type;
		else if (type == Type.Wall)
			return "Wall (" + x + ", " + y + ") " + type;
		else if (type == Type.Entry)
			return "Entry (" + x + ", " + y + ") " + type;
		else if (type == Type.Exit)
			return "Exit (" + x + ", " + y + ") " + type;
		else
			return "Hallway (" + x + ", " + y + ") " + type;

	}

	public int getUnexploredPaths() {
		int count = 0;
		if (nsew[0] == PathState.Unexplored) {
			count++;
		}
		if (nsew[1] == PathState.Unexplored) {
			count++;
		}
		if (nsew[2] == PathState.Unexplored) {
			count++;
		}
		if (nsew[3] == PathState.Unexplored) {
			count++;
		}
		return count;
	}

	public int getLivePaths() {
		int count = 0;
		if (nsew[0] == PathState.Live) {
			count++;
		}
		if (nsew[1] == PathState.Live) {
			count++;
		}
		if (nsew[2] == PathState.Live) {
			count++;
		}
		if (nsew[3] == PathState.Live) {
			count++;
		}
		return count;
	}

	public int getExitPaths() {
		if (nsew[0] == PathState.Exit) {
			return 0;
		} else if (nsew[1] == PathState.Exit) {
			return 1;
		} else if (nsew[2] == PathState.Exit) {
			return 2;
		} else if (nsew[3] == PathState.Exit) {
			return 3;
		}
		return -1;
	}

	/**
	 * Changes the PathState of the direction supplied. n=0, s=1, e=2, w=3
	 */
	public void setNSEW(int dir, Tile.PathState ps) {
		synchronized (this) {
			nsew[dir] = ps;
		}
	}

	/**
	 * Returns the PathState of the direction supplied. n=0, s=1, e=2, w=3
	 */
	public Tile.PathState getNSEW(int dir) {
		synchronized (this) {
			return nsew[dir];
		}
	}

}
