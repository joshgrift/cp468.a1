import java.util.LinkedList;
import java.util.PriorityQueue;

class Assignment {
  public static void main(String[] args) {
    int[][] startingMap = { { 7, 2, 4 }, { 5, 0, 6 }, { 8, 3, 1 } };
    State state = new State(startingMap);

    State sucessState = AI.solve(state);
    System.out.println(sucessState);
    System.out.println("Solved in " + sucessState.history.size() + " moves");
  }
}

/**
 * Our problem Solving Class
 */
class AI {
  /**
   * Switch to true to output status updates
   */
  private static Boolean DEBUG = false;

  private static PriorityQueue<State> frontier = new PriorityQueue<State>();
  private static LinkedList<State> visitedStates = new LinkedList<State>();

  /**
   * Find the solved State
   * 
   * @param state state to solve
   * @return solved State
   */
  public static State solve(State state) {
    if (DEBUG) {
      System.out.println("Testing State:");
      System.out.println(state);
    }

    if (state.isFinished()) {
      if (DEBUG)
        System.out.println("Found Successful State");
      return state;
    }

    if (DEBUG)
      System.out.println("Failed. Generating derivative");

    addDerivatives(state);

    return AI.solve(frontier.poll());
  }

  private static void addDerivatives(State state) {
    for (State.Direction dir : State.Direction.values()) {
      if (state.canSlide(dir)) {
        if (DEBUG) {
          System.out.println("Adding Direction " + dir);
        }

        State newstate = state.duplicate();
        newstate.slide(dir);
        Boolean found = false;

        for (State checkState : visitedStates) {
          if (checkState.equalTo(newstate)) {
            found = true;
            break;
          }
        }

        if (!found) {
          newstate.history.add(dir);
          visitedStates.add(newstate);
          frontier.add(newstate);
        }
      }
    }
  }
}

/**
 * State Object. Programic representation of board
 */
class State implements Comparable<State> {
  /**
   * Slide Direction
   */
  public enum Direction {
    RIGHT, LEFT, UP, DOWN,
  }

  /**
   * List of actions we took to get here
   */
  public LinkedList<Direction> history = new LinkedList<Direction>();

  /**
   * 2D map of current board
   */
  public int[][] grid;

  /**
   * X position of the empty tile.
   */
  private int x;

  /**
   * Y position of the empty tile.
   */
  private int y;

  /**
   * Previous Direction
   */
  private Direction direction;

  /**
   * Pass 2D array of ints to create a State
   * 
   * @param grid 2d array of ints
   */
  public State(int[][] grid) {
    this.grid = grid;

    // Set the value of
    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        if (this.grid[y][x] == 0) {
          this.x = x;
          this.y = y;
          break;
        }
      }
    }
  }

  /**
   * Return string version of State
   */
  public String toString() {
    String result = "\n ╔═══╦═══╦═══╗\n";

    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        result += " ║ ";
        result += this.grid[y][x];
      }
      result += " ║ \n";

      if (y != this.grid.length - 1) {
        result += " ╠═══╬═══╬═══╣\n";
      }
    }

    return result + " ╚═══╩═══╩═══╝\n";
  }

  /**
   * Returns true if the state is finished
   */
  public Boolean isFinished() {
    int value = 0;
    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        if (this.grid[y][x] != value) {
          return false;
        }
        value++;
      }
    }

    return true;
  }

  /**
   * Move a tile in this direction into the empty spot
   * 
   * @param direction
   */
  public State slide(Direction direction) {
    int x = this.x;
    int y = this.y;

    switch (direction) {
      case DOWN:
        y += 1;
        break;

      case UP:
        y -= 1;
        break;

      case RIGHT:
        x += 1;
        break;

      case LEFT:
        x -= 1;
        break;
    }

    // Move the tile to the empty spot
    grid[this.y][this.x] = grid[y][x];

    // Make the tile's original position the new empty spot
    grid[y][x] = 0;

    // Reset this state's empty spot cordinantes
    this.x = x;
    this.y = y;

    this.direction = direction;

    // return this for easy chaining
    return this;
  }

  /**
   * Can a tile be moved in this direction. Checks - Is it out of bounds - Would
   * this negate the previous move
   * 
   * @param direction
   * @return
   */
  public Boolean canSlide(Direction direction) {
    switch (direction) {
      case DOWN:
        if (this.direction == Direction.UP) {
          return false;
        }
        return y + 1 < grid.length;

      case UP:
        if (this.direction == Direction.DOWN) {
          return false;
        }
        return y - 1 >= 0;

      case RIGHT:
        if (this.direction == Direction.LEFT) {
          return false;
        }
        return x + 1 < grid[y].length;

      case LEFT:
        if (this.direction == Direction.RIGHT) {
          return false;
        }
        return x - 1 >= 0;

      default:
        throw new Error("That Direction is invalid");
    }
  }

  /**
   * Get a clone!
   * 
   * @return the clone
   */
  public State duplicate() {
    int[][] newGrid = new int[this.grid.length][this.grid[y].length];

    // Set the value of
    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        newGrid[y][x] = this.grid[y][x];
      }
    }

    State newState = new State(newGrid);
    for (int h = 0; h < this.history.size(); h++) {
      newState.history.add(this.history.get(h));
    }
    return newState;
  }

  /**
   * Is this state equal to another state>
   */
  public Boolean equalTo(State state) {
    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        if (state.grid[y][x] != this.grid[y][x]) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Optional Heuristics function: total number of misplaced tiles
   */
  public Integer getTotalMisplacedTiles() {
    int mem = 0;
    int misplaced = 0;
    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        if (this.grid[y][x] != mem) {
          misplaced++;
        }
        mem++;
      }
    }

    return misplaced;
  }

  /**
   * Get total Manhattan distance (sum of the horizontal and vertical distance)
   * 
   * @return
   */
  public Integer getManhattan() {
    // value is the correct value for this tile
    int distance = 0;

    for (int y = 0; y < this.grid.length; y++) {
      for (int x = 0; x < this.grid[y].length; x++) {
        if (this.grid[y][x] != 0) {
          distance += Math.abs(x - (this.grid[y][x] % 3)) + Math.abs(y - Math.floor(this.grid[y][x] / 3));
        }
      }
    }

    return distance;
  }

  @Override
  public int compareTo(State s) {
    return this.getManhattan() > s.getManhattan() ? 1 : -1;
  }
}