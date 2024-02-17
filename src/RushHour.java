import javalib.funworld.World;
import javalib.funworld.WorldScene;
import javalib.worldimages.*;
import tester.Tester;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

// represents a value that may or may not exist
interface Optional<T> {
  // Is there some value?
  boolean isSome();

  // Unwraps the value, throwing a runtime exception if there is no value.
  T unwrap();
}

// Represents a value that doesn't exist.
class None<T> implements Optional<T> {
  None() {}

  /*
  M:
  isSome() - boolean
  unwrap() - T
   */

  /*
  M:
  isSome() - boolean
  unwrap() - T
   */
  // Always returns false.
  public boolean isSome() {
    return false;
  }

  /*
  M:
  isSome() - boolean
  unwrap() - T
   */
  // Throws an exception (there is no value).
  public T unwrap() {
    throw new RuntimeException("Cannot unwrap a None value.");
  }
}


// Represents a value that doesn't exist.
class Some<T> implements Optional<T> {
  T value;


  Some(T value) {
    this.value = value;
  }

  /*
  F:
  value - T
  M:
  isSome() - boolean
  unwrap() - T
   */

  /*
  F:
  value - T
  M:
  isSome() - boolean
  unwrap() - T
   */
  // Always returns true.
  public boolean isSome() {
    return true;
  }

  /*
  F:
  value - T
  M:
  isSome() - boolean
  unwrap() - T
   */
  // Returns the value.
  public T unwrap() {
    return this.value;
  }
}

//represents a list of elements
interface IList<T> {
  // Applies the func to all elements in the list.
  <U> IList<U> map(Function<T, U> func);

  // Produces a list only containing elements for which func returns true.
  IList<T> filter(Function<T, Boolean> func);

  // Applies the function from left to right to each item in the list and combines them.
  <U> U fold(BiFunction<T, U, U> func, U value);

  // Appends that list to this one, creating a new list.
  IList<T> append(IList<T> that);
}

//represents a nonempty IList<T>
class Cons<T> implements IList<T> {
  T first;
  IList<T> rest;

  Cons(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  /*
  Fields:
  first -- T
  rest -- IList<T>
  Methods:
  map -- IList<U>
  filter -- IList<U>
  fold -- U
  Methods on Fields
  this.rest.map
  this.rest.filter
  this.rest.fold
   */

  /*
  Parameters:
  func -- Function<T, U>
  Methods on Parameters;
  func.apply
   */

  // Applies the func to all elements in the list.
  public <U> IList<U> map(Function<T, U> func) {
    return new Cons<U>(func.apply(this.first), this.rest.map(func));
  }

  /*
  Parameters:
  func -- Function<T, U>
  Methods on Parameters;
  func.apply
   */

  // Produces a list only containing elements for which func returns true.
  public IList<T> filter(Function<T, Boolean> func) {
    if (func.apply(this.first)) {
      return new Cons<T>(this.first, this.rest.filter(func));
    } else {
      return this.rest.filter(func);
    }
  }

  /*
  Parameters:
  func -- Function<T, U, U>
  value -- U
  Methods on Parameters;
  func.apply
   */

  // Applies the function from left to right to each item in the list and combines them.
  public <U> U fold(BiFunction<T, U, U> func, U value) {
    return this.rest.fold(func, func.apply(this.first, value));
  }

  // Appends that list to this one, creating a new list.
  public IList<T> append(IList<T> that) {
    return new Cons<T>(this.first, this.rest.append(that));
  }
}

//represents an empty IList<T>
class MT<T> implements IList<T> {

  /*
  Fields:
  Methods:
  map -- IList<U>
  filter -- IList<U>
  fold -- U
  Methods on Fields
   */

  /*
  Parameters:
  func -- Function<T, U>
  Methods on Parameters;
  func.apply
   */

  // A mapping of any empty list is another empty list, so this produces an empty list.
  public <U> IList<U> map(Function<T, U> func) {
    return new MT<>();
  }

  /*
  Parameters:
  func -- Function<T, U>
  Methods on Parameters;
  func.apply
   */

  // There are no elements for which the func could return true, so this produces an empty list.
  public IList<T> filter(Function<T, Boolean> func) {
    return new MT<>();
  }

  /*
  Parameters:
  func -- Function<T, U, U>
  value -- U
  Methods on Parameters;
  func.apply
   */

  // There are no elements to fold upon, so this produces whatever value was passed to it.
  public <U> U fold(BiFunction<T, U, U> func, U value) {
    return value;
  }

  // Appending any list to an empty list results in just that list.
  public IList<T> append(IList<T> that) {
    return that;
  }
}

// MARK: Drawing
// Represents a tile to draw on the screen.
class Tile {
  int col;
  int row;
  Color color;

  public Tile(int col, int row, Color color) {
    this.col = col;
    this.row = row;
    this.color = color;
  }

  /*
  F:
  col - int
  row - int
  color - Color
  M:
  draw - WorldImage
  drawOntoScene - WorldScene
   */

  /*
  F:
  col - int
  row - int
  color - Color
  M:
  draw - WorldImage
  drawOntoScene - WorldScene
  P:
  tileSize - int
   */
  // Draw an individual tile, given a tile size.
  WorldImage draw(int tileSize) {
    return new RectangleImage(
                  tileSize,
                  tileSize,
                  OutlineMode.SOLID,
                  this.color
          ).movePinhole(-0.5 * tileSize, -0.5 * tileSize);
  }

  /*
  F:
  col - int
  row - int
  color - Color
  M:
  draw - WorldImage
  drawOntoScene - WorldScene
  P:
  scene - WorldScene
  tileSize - int
  MoP:
  scene.placeImageXY - WorldScene
   */
  // Draw an individual tile onto a given scene, given a tile size.
  WorldScene drawOntoScene(WorldScene scene, int tileSize) {
    return scene.placeImageXY(this.draw(tileSize),
            this.col * tileSize, this.row * tileSize);
  }
}

// Used to draw tiles onto a world scene.
class DrawTilesOntoGrid implements BiFunction<Tile, WorldScene, WorldScene> {
  int tileSize;

  DrawTilesOntoGrid(int tileSize) {
    this.tileSize = tileSize;
  }

  public WorldScene apply(Tile tile, WorldScene sceneSoFar) {
    return tile.drawOntoScene(sceneSoFar, tileSize);
  }
}

// Represents a collection of tiles to draw
class TileGrid {
  int cols;
  int rows;
  int exitCol;
  int exitRow;
  int tileSize;

  public TileGrid(int cols, int rows, int tileSize, int exitCol, int exitRow) {
    this.cols = cols;
    this.rows = rows;
    this.exitCol = exitCol;
    this.exitRow = exitRow;
    this.tileSize = tileSize;
  }

  /*
  F:
  cols - int
  rows - int
  exitCol - int
  exitRow - int
  tileSize - int
  M:
  totalWidth - int
  totalHeight - int
  makeScene - WorldScene
   */

  /*
  F:
  cols - int
  rows - int
  exitCol - int
  exitRow - int
  tileSize - int
  M:
  totalWidth - int
  totalHeight - int
  makeScene - WorldScene
   */
  // Get the width in pixels of the tile grid.
  int totalWidth() {
    return this.cols * this.tileSize;
  }

  /*
  F:
  cols - int
  rows - int
  exitCol - int
  exitRow - int
  tileSize - int
  M:
  totalWidth - int
  totalHeight - int
  makeScene - WorldScene
   */
  // Get the height in pixels of the tile grid.
  int totalHeight() {
    return this.rows * this.tileSize;
  }

  /*
  F:
  cols - int
  rows - int
  exitCol - int
  exitRow - int
  tileSize - int
  M:
  totalWidth - int
  totalHeight - int
  makeScene - WorldScene
  P:
  tiles - IList<Tile>
  tileSize - int
  MoP:
  tiles.map - IList<U>
  tiles.filter - IList<Tile>
  tiles.fold - IList<U>
  tiles.append - IList<T>
   */
  // Create a scene from the tile grid, the given tiles to draw, and a size for each tile.
  WorldScene makeScene(IList<Tile> tiles, int tileSize) {
    IList<Tile> tilesWithBorder = tiles.append(this.createBorderTiles(tileSize));
    WorldScene base = new WorldScene(
            this.cols * tileSize,
            this.rows * tileSize)
            .placeImageXY(
                    new RectangleImage(
                            this.cols * tileSize,
                            this.rows * tileSize,
                            OutlineMode.SOLID,
                            Color.WHITE),
                    (this.cols * tileSize) / 2,
                    (this.rows * tileSize) / 2);
    return tilesWithBorder.fold(new DrawTilesOntoGrid(tileSize), base);
  }

  /*
  F:
  cols - int
  rows - int
  exitCol - int
  exitRow - int
  tileSize - int
  M:
  totalWidth - int
  totalHeight - int
  makeScene - WorldScene
   */
  // Create the border tiles for the grid.
  IList<Tile> createBorderTiles(int tileSize) {
    // assume x is always on right side
    IList<Tile> topBorder = createBorderTilesHelper(
            0,
            0,
            this.cols - 1,
            0,
            Color.DARK_GRAY,
            tileSize);
    IList<Tile> bottomBorder = createBorderTilesHelper(
            0,
            this.rows - 1,
            this.cols - 1,
            this.rows - 1,
            Color.DARK_GRAY,
            tileSize);
    IList<Tile> leftBorder = createBorderTilesHelper(
            0,
            0,
            0,
            this.rows - 1,
            Color.DARK_GRAY,
            tileSize
    );
    IList<Tile> topRightBorder = createBorderTilesHelper(
            this.cols - 1,
            0,
            this.cols - 1,
            this.exitRow - 1,
            Color.DARK_GRAY,
            tileSize
    );
    IList<Tile> bottomRightBorder = createBorderTilesHelper(
            this.cols - 1,
            this.exitRow + 1,
            this.cols - 1,
            this.rows - 1,
            Color.DARK_GRAY,
            tileSize
    );
    return topBorder
            .append(bottomBorder)
            .append(leftBorder)
            .append(topRightBorder)
            .append(bottomRightBorder);
  }

  /*
  F:
  cols - int
  rows - int
  exitCol - int
  exitRow - int
  tileSize - int
  M:
  totalWidth - int
  totalHeight - int
  makeScene - WorldScene
  P:
  x1 - int
  y1 - int
  x2 - int
  y2 - int
  color - Color
   */
  // Create a line of tiles, from one point to the next point.
  IList<Tile> createBorderTilesHelper(int x1, int y1, int x2, int y2, Color color, int tileSize) {
    if (x1 != x2 && y1 != y2) {
      throw new IllegalArgumentException("createBorderTiles is only for straight lines");
    }

    if ((x2 - x1) > 0) {
      return new Cons<>(
              new Tile(x1,y1, color),
              createBorderTilesHelper(x1 + 1, y1, x2, y2, color, tileSize));
    } else if ((y2 - y1) > 0) {
      return new Cons<>(
              new Tile(x1,y1, color),
              createBorderTilesHelper(x1,y1 + 1, x2, y2, color, tileSize));
    } else {
      return new Cons<>(new Tile(x1,y1, color), new MT<>());
    }
  }
}

//represents a vehicle in the RushHour game
class Vehicle {
  int x1;
  int y1;
  int x2;
  int y2;
  Color color;
  int tileSize;

  public Vehicle(int x1, int y1, int x2, int y2, Color color, int tileSize) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.color = color;
    this.tileSize = tileSize;
  }

  /*
  F:
  x1 -- int,
  y1 -- int
  x2 -- int
  y2 -- int
  color -- Color
  tileSize -- int
  M:
  toImage -- WorldImage
  width -- int
  height -- int
  overlaps -- boolean
  overlapsHelper -- boolean
  atLocation -- boolean
  MoF:
  n/a
   */


  /*
  P:
  MoP:
   */

  //calculates the width of this vehicle
  int width() {
    return Math.abs(this.x1 - this.x2) + 1;
  }

  /*
  P:
  MoP:
   */

  //calculates the height of this vehicle
  int height() {
    return Math.abs(this.y1 - this.y2) + 1;
  }

  /*
  P:
  that -- Vehicle
  MoP:
   */

  //determines if this Vehicle overlaps with that Vehicle
  public boolean overlaps(Vehicle that) {
    return this.overlapsHelper(this.x1, this.y1, this.x2, this.y2,
            that.x1, that.y1, that.x2, that.y2);
  }

  /*
  P:
  v1x1 ...
  v1y1 ...
  v1x2
  v1y2
  v2x1
  v2y1
  v2x2 ...
  v2y2 -- int
  MoP:
   */

  //determines if four intervals overlap, two horizontal and two vertical
  public boolean overlapsHelper(int v1x1, int v1y1, int v1x2, int v1y2,
                                int v2x1, int v2y1, int v2x2, int v2y2) {
    //case where same line and overlaps
    if (v1y1 == v1y2
            && v1y2 == v2y1
            && v2y1 == v2y2) {
      return Math.max(v1x1, v2x1) <= Math.min(v1x2, v2x2);
    } else if (v1x1 == v1x2
            && v1x2 == v2x1
            && v2x1 == v2x2) {
      return Math.max(v1y1, v2y1) <= Math.min(v1y2, v2y2);
      //vertical x horizontal cross case
    } else {
      return ((v1y1 == v1y2
              && v2x1 == v2x2
              && v2x1 >= v1x1 && v2x1 <= v1x2
              && v1y1 >= v2y1 && v1y1 <= v2y2)
              || (v2y1 == v2y2
              && v1x1 == v1x2
              && v1x1 >= v2x1 && v1x1 <= v2x2
              && v2y1 >= v1y1 && v2y1 <= v1y2));
    }
  }

  /*
  P:
  x -- int
  y -- int
  MoP:
   */

  //determines if this vehicle has endpoints at x and y
  public boolean atLocation(int x, int y) {
    return ((this.x1 == x && this.y1 == y)
            || (this.x2 == x && this.y2 == y));
  }

  /*
  P:
  MoP:
   */
  // creates the tiles of this vehicle, to be used for drawing it on a gameboard.
  public IList<Tile> toTiles() {
    if (this.width() > this.height()) { // horizontal car/truck
      return toTilesHelper(new MT<>(), this.x1, this.y1, 1, 0);
    } else { // vertical car/truck
      return toTilesHelper(new MT<>(), this.x1, this.y1, 0, 1);
    }
  }

  /*
  P:
  newColor - Color
  MoP:
   */
  // creates the tiles of this vehicle, to be used for drawing it on a gameboard,
  // and specifies a specific color to draw as.
  public IList<Tile> toTilesWithColor(Color newColor) {
    return new Vehicle(this.x1, this.y1, this.x2, this.y2, newColor, this.tileSize).toTiles();
  }

  /*
  P:
  tilesSoFar - IList<Tile>
  currentX - int
  currentY - int
  deltaX - int
  deltaY - int
  MoP:
  tilesSoFar.map - IList<U>
  tilesSoFar.filter - IList<Tile>
  tilesSoFar.fold - IList<U>
  tilesSoFar.append - IList<T>
   */
  // helper for toTiles; adds the tiles to the work list and checks if more tiles need to be added.
  public IList<Tile> toTilesHelper(
          IList<Tile> tilesSoFar,
          int currentX,
          int currentY,
          int deltaX,
          int deltaY) {
    IList<Tile> tiles = new Cons<>(new Tile(currentX, currentY, this.color), tilesSoFar);
    if (currentX == this.x2 && currentY == this.y2) {
      return tiles;
    } else {
      return this.toTilesHelper(
              tiles,
              currentX + deltaX,
              currentY + deltaY,
              deltaX,
              deltaY);
    }
  }

  /*
  P:
  col - int
  row - int
  MoP:
   */
  // Does this vehicle occupy this given tile?
  public boolean inTile(int col, int row) {
    // horizontal car
    if ((x2 - x1) == 0 && col == x1 && y1 <= row && row <= y2) { // vertical car
      return true;
    } else {
      return (y2 - y1) == 0 && row == y1 && x1 <= col && col <= x2;
    }
  }
}

// Convert a list of vehicles into a list of tile lists
// (each tile list representing the tiles to draw for each vehicle).
class VehiclesToTileLists implements Function<Vehicle, IList<Tile>> {
  public IList<Tile> apply(Vehicle vehicle) {
    return vehicle.toTiles();
  }
}

class WasVehicleClicked implements BiFunction<Vehicle, Optional<Vehicle>, Optional<Vehicle>> {
  int tileX;
  int tileY;

  public WasVehicleClicked(int tileX, int tileY) {
    this.tileX = tileX;
    this.tileY = tileY;
  }

  public Optional<Vehicle> apply(Vehicle vehicle, Optional<Vehicle> vehicleOptional) {
    if (vehicle.inTile(tileX, tileY)) {
      return new Some<>(vehicle);
    } else {
      return vehicleOptional;
    }
  }
}

// Flattens a 2d list of tiles into a 1d list of tiles.
class FoldTileLists implements BiFunction<IList<Tile>, IList<Tile>, IList<Tile>> {
  public IList<Tile> apply(IList<Tile> tilesSoFar, IList<Tile> tilesToAdd) {
    return tilesSoFar.append(tilesToAdd);
  }
}

//represents the game RushHour with all its components
class RushHour extends World {
  IList<Vehicle> vehicles;
  Optional<Vehicle> currentVehicleClicked;
  TileGrid tileGrid;
  RushHourUtils utils;
  int tileSize;
  Vehicle targetVehicle;
  int endX;
  int endY;


  RushHour(IList<Vehicle> vehicles, TileGrid board, int tileSize,
           Vehicle targetVehicle, int endX, int endY) {
    super();
    this.vehicles = vehicles;
    this.tileGrid = board;
    this.tileSize = tileSize;
    this.utils = new RushHourUtils();
    this.targetVehicle = targetVehicle;
    this.endX = endX;
    this.endY = endY;
    this.currentVehicleClicked = new None<>();
  }

  /*
  F:
  vehicles -- IList<Vehicle>
  grid -- tileGrid
  utils -- RushHourUtils
  currentVehicleClicked -- Optional<Vehicle>
  tileSize -- int
  targetVehicle -- Vehicle
  endX -- int
  endY -- int
  M:
  toImage -- WorldImage
  makeScene -- WorldScene
  winCheck -- boolean
   */

  /*
  P:
  level -- String
  utils -- RushHourUtils
  tileSize -- int
  MoP:
  utils.getVehiclesList
  utils.getGrid
  utils.getEndX
  utils.getEndY
   */

  //constructs RushHour from a string which represents the level
  RushHour(String level, RushHourUtils utils, int tileSize) {
    this(utils.getVehiclesList(level, level.length(),
                    0, 0, 0, new MT<Vehicle>(), tileSize),
            utils.getTileGrid(level, 0, 0, 0, tileSize),
            tileSize,
    //below vehicle seems to be the same target everytime
    new Vehicle(1, 3, 2, 3, Color.RED, tileSize),
                utils.getEndX(level, 0, 0, 0),
                utils.getEndY(level, 0, 0, 0));
  }

  /*
  P:
  MoP;
   */

  //creates a WorldScene
  public WorldScene makeScene() {
    if (this.currentVehicleClicked.isSome()) {
      IList<Tile> vehicleTiles =
              this.vehicles.map(new VehiclesToTileLists()).fold(new FoldTileLists(), new MT<>())
                      .append(this.currentVehicleClicked.unwrap().toTilesWithColor(Color.YELLOW));
      return this.tileGrid.makeScene(vehicleTiles, this.tileSize);
    } else {
      IList<Tile> vehicleTiles = this.vehicles.map(
              new VehiclesToTileLists()).fold(new FoldTileLists(), new MT<>());
      return this.tileGrid.makeScene(vehicleTiles, this.tileSize);
    }
  }

  /*
  P:
  MoP;
   */

  //determines if this RushHour has been won
  public boolean winCheck() {
    return this.targetVehicle.atLocation(this.endX, this.endY);
  }

  // handles mouse clicks.
  // EFFECT: alters the currentClickedVehicle field (will become a Some value if a vehicle was
  // clicked, None if no vehicle was).
  public World onMouseClicked(Posn mouse) {
    int tileX = mouse.x / this.tileSize;
    int tileY = mouse.y / this.tileSize;

    this.currentVehicleClicked = this.vehicles.fold(
            new WasVehicleClicked(tileX, tileY),
            new None<>());
    return this;
  }
}

//represents a utility class with helper methods for RushHour
class RushHourUtils {

  /*
  F:
  M:
  getVehiclesList -- IList<Vehicle>
  getGrid -- Grid
  getEndX -- int
  getEndY -- int
  MoF:
   */

  /*
  P:
  level -- String
  length -- int
  i -- int
  r -- int
  c -- int
  currList -- IList<Vehicle> currList
  tileSize -- int
  MoP:
  ...
   */

  //gathers all vehicles in level into an IList<Vehicle>
  public IList<Vehicle> getVehiclesList(String level, int length,
                                        int i, int r, int c, IList<Vehicle> currList,
                                        int tileSize) {
    String currLetter = level.substring(i, i + 1);
    if (i == length - 1) {
      return currList;
    } else if (currLetter.equals("T")) {
      return this.getVehiclesList(level, length,
              i + 1, r, c + 1, new Cons<Vehicle>(
                      new Vehicle(c,
                              r,
                              //length = 3, but add 2 because starting posn = 1 block
                              c,
                              r + 2,
                              Color.ORANGE,
                              tileSize),
                      currList), tileSize);
    } else if (currLetter.equals("t")) {
      return this.getVehiclesList(level, length,
              i + 1, r, c + 1, new Cons<Vehicle>(
                      new Vehicle(
                              c,
                              r,
                              c + 2,
                              r,
                              Color.GREEN,
                              tileSize),
                      currList), tileSize);
    } else if (currLetter.equals("C")) {
      return this.getVehiclesList(level, length,
              i + 1, r, c + 1, new Cons<Vehicle>(
                      new Vehicle(
                              c,
                              r,
                              c,
                              r + 1,
                              Color.BLUE,
                              tileSize),
                      currList), tileSize);
    } else if (currLetter.equals("c")) {
      return this.getVehiclesList(level, length,
              i + 1, r, c + 1, new Cons<Vehicle>(
                      new Vehicle(
                              c,
                              r,
                              c + 1,
                              r,
                              Color.MAGENTA,
                              tileSize),
                      currList), tileSize);
    } else if (c == 0
            || currLetter.equals("-")
            || (!currLetter.equals("+")
            && !currLetter.equals("|")
            && !currLetter.equals("X"))) {
      return this.getVehiclesList(level, length,
              i + 1, r, c + 1, currList, tileSize);
    } else {
      return this.getVehiclesList(level, length,
              i + 1, r + 1, 0, currList, tileSize);
    }
  }

  /*
  P:
  level -- String
  i -- int
  r -- int
  currCol -- int
  tileSize -- int
  MoP:
   */

  //determines the dimensions of level and creates a Grid from them
  public TileGrid getTileGrid(String level, int i, int r, int currCol, int tileSize) {
    String currLetter = level.substring(i, i + 1);
    if (i == level.length() - 1) {
      //0 index
      return new TileGrid(
              currCol + 1,
              r + 1,
              tileSize,
              this.getEndX(level, 0, 0, 0),
              this.getEndY(level, 0, 0, 0));
    } else if (currCol == 0
            || (!currLetter.equals("+")
            && !currLetter.equals("|")
            && !currLetter.equals("X"))) {
      return this.getTileGrid(level, i + 1, r, currCol + 1, tileSize);
    } else {
      return this.getTileGrid(level, i + 1, r + 1, 0, tileSize);
    }
  }

  /*
  P:
  level -- String
  i -- int
  r -- int
  currCol -- int
  tileSize -- int
  MoP:
   */

  //find the end position and returns the x value of that coordinate
  public int getEndX(String level, int i, int r, int currCol) {
    String currLetter = level.substring(i, i + 1);
    if (currLetter.equals("X")) {
    return currCol;
    } else if (currCol == 0
              || (!currLetter.equals("+")
              && !currLetter.equals("|"))) {
      return this.getEndX(level, i + 1, r, currCol + 1);
    } else {
      return this.getEndX(level, i + 1, r + 1, 0);
    }
  }

  /*
  P:
  level -- String
  i -- int
  r -- int
  currCol -- int
  tileSize -- int
  MoP:
   */

  //find the end position and returns the y value of that coordinate
  public int getEndY(String level, int i, int r, int currCol) {
    String currLetter = level.substring(i, i + 1);
    if (currLetter.equals("X")) {
      return r;
    } else if (currCol == 0
            || (!currLetter.equals("+")
            && !currLetter.equals("|"))) {
      return this.getEndY(level, i + 1, r, currCol + 1);
    } else {
      return this.getEndY(level, i + 1, r + 1, 0);
    }
  }
}

class ExamplesRushHour {
  RushHour game1 = new RushHour(
          "+------+"
                  + "|      |"
                  + "|  C T |"
                  + "|c    CX"
                  + "|t     |"
                  + "|CCC c |"
                  + "|    c |"
                  + "+------+",
          new RushHourUtils(),
          20);
  RushHour game2 = new RushHour(
               "+-----+"
                  + "|     |"
                  + "|  C T|"
                  + "|c    X"
                  + "|t    |"
                  + "+-----+",
          new RushHourUtils(),
          20);
  RushHour game3 = new RushHour(
          this.game2.vehicles,
          this.game2.tileGrid,
          20,
          new Vehicle(5, 3, 6, 3,
                  Color.RED, 20),
          this.game2.endX,
          this.game2.endY);
  Vehicle car1 = new Vehicle(1, 2, 2, 2, Color.BLUE, 20);
  Vehicle car2 = new Vehicle(2, 2, 3, 2, Color.BLUE, 20);
  Vehicle truck1 = new Vehicle(1, 1, 1, 3, Color.BLUE, 20);
  Vehicle truck2 = new Vehicle(1, 2, 1, 4, Color.BLUE, 20);
  TileGrid gridEx = new TileGrid(8, 7, 20, 7, 3);

  boolean testConstructor(Tester t) {
    return t.checkExpect(game1.tileGrid,
            //we are going to zero index
            new TileGrid(8, 8, 20, 7, 3))
            && t.checkExpect(game2.tileGrid,
            new TileGrid(7, 6, 20, 6, 3))
            && t.checkExpect(game2.vehicles,
            new Cons<>(
                    new Vehicle(1, 4, 3, 4,
                            Color.green,
                            20),
                    new Cons<>(
                            new Vehicle(1, 3, 2, 3,
                                    Color.magenta,
                                    20),
                            new Cons<>(
                                    new Vehicle(5, 2, 5, 4,
                                            Color.ORANGE,
                                            20),
                                    new Cons<>(
                                            new Vehicle(3, 2, 3, 3,
                                                    Color.blue,
                                                    20),
                                            new MT<>())))));
  }

  boolean testOverlap(Tester t) {
    return t.checkExpect(car1.overlaps(car2), true)
            && t.checkExpect(car1.overlaps(truck1), true)
            && t.checkExpect(truck1.overlaps(truck2), true)
            && t.checkExpect(car2.overlaps(truck2), false);
  }

  boolean testOverlapHelper(Tester t) {
    return t.checkExpect(car1.overlapsHelper(1, 1, 1, 2,
            1, 1, 1, 2), true)
            && t.checkExpect(car1.overlapsHelper(100, 100, 101, 101,
            1, 1, 1, 2), false);
  }

  boolean testWin(Tester t) {
    return t.checkExpect(game3.winCheck(), true)
            && t.checkExpect(game2.winCheck(), false)
            && t.checkExpect(game1.winCheck(), false);
  }

  boolean testVehicleWidthAndHeight(Tester t) {
    return t.checkExpect(car1.width(), 2)
            && t.checkExpect(truck1.width(), 1)
            && t.checkExpect(truck2.height(), 3)
            && t.checkExpect(truck1.height(), 3)
            && t.checkExpect(gridEx.totalHeight(), 140)
            && t.checkExpect(gridEx.totalWidth(), 160);
  }

  boolean testAtLocation(Tester t) {
    return t.checkExpect(car1.atLocation(2, 2), true)
            && t.checkExpect(car1.atLocation(2, 3), false);
  }

  boolean testRushHourUtils(Tester t) {
    RushHourUtils utils = new RushHourUtils();
    String level = "+------+"
            + "|      |"
            + "|  C   |"
            + "|c     X"
            + "|t     |"
            + "|C   c |"
            + "|    c |"
            + "+------+";
    return t.checkExpect(utils.getVehiclesList(level,
            //  T yellow, t green, C blue, c magenta
            level.length(), 0, 0, 0,
                    new MT<>(), 20),
            new Cons<>(
                    new Vehicle(5, 6, 6, 6,
                            Color.MAGENTA, 20),
                    new Cons<>(
                            new Vehicle(5, 5, 6, 5,
                                    Color.MAGENTA, 20),
                            new Cons<>(
                                    new Vehicle(1, 5, 1, 6,
                                            Color.BLUE, 20),
                                    new Cons<Vehicle>(
                                            new Vehicle(1, 4, 3, 4,
                                                    Color.GREEN, 20),
                                            new Cons<>(
                                                    new Vehicle(1, 3, 2, 3,
                                                            Color.MAGENTA, 20),
                                                    new Cons<>(
                                                            new Vehicle(3, 2, 3, 3,
                                                                    Color.BLUE, 20),
                                                            new MT<>())))))))
            && t.checkExpect(utils.getTileGrid(level, 0, 0, 0, 20),
            new TileGrid(8, 8, 20, 7, 3))
            && t.checkExpect(utils.getEndX(level, 0, 0, 0),
            7)
            && t.checkExpect(utils.getEndY(level, 0, 0, 0),
            3);
  }

  boolean testVehicleToTiles(Tester t) {
    Vehicle vehicle = new Vehicle(1,1,1,4,Color.RED, 20);
    return t.checkExpect(
            vehicle.toTiles(),
            new Cons<>(
                    new Tile(1,4, Color.RED),
                    new Cons<>(
                            new Tile(1, 3, Color.RED),
                            new Cons<>(
                                    new Tile(1, 2, Color.RED),
                                    new Cons<>(
                                            new Tile(1, 1, Color.RED),
                                            new MT<>()
                                    )
                            )
                    )));
  }

  boolean testVehicleToTiles2(Tester t) {
    Vehicle vehicle = new Vehicle(1,1,2,1,Color.RED, 20);
    return t.checkExpect(
            vehicle.toTiles(),
            new Cons<>(
                    new Tile(2,1, Color.RED),
                    new Cons<>(
                            new Tile(1, 1, Color.RED),
                            new MT<>())));
  }

  boolean testFoldTileLists(Tester t) {
    IList<IList<Tile>> tileLists = new Cons<>(
            new Cons<>(new Tile(1,1, Color.RED), new MT<>()),
            new Cons<>(
                    new Cons<>(new Tile(1, 2, Color.RED), new MT<>()),
                    new Cons<>(
                            new Cons<>(new Tile(1, 3, Color.RED), new MT<>()),
                            new MT<>())));
    IList<Tile> expectedTileList = new Cons<>(
            new Tile(1,3, Color.RED),
            new Cons<>(
                    new Tile(1, 2, Color.RED),
                    new Cons<>(
                            new Tile(1, 1, Color.RED),
                            new MT<>())));
    return t.checkExpect(tileLists.fold(new FoldTileLists(), new MT<>()), expectedTileList);
  }

  boolean testCreateBorderTilesHelper(Tester t) {
    TileGrid grid = new TileGrid(1,1,1,1,1);
    return t.checkExpect(
            grid.createBorderTilesHelper(0,0,2,0, Color.RED, 20),
            new Cons<>(
                    new Tile(0,0, Color.RED),
                    new Cons<>(
                            new Tile(1, 0, Color.RED),
                            new Cons<>(
                                    new Tile(2, 0, Color.RED),
                                    new MT<>()
                            )
                    )));
  }

  boolean testInTile(Tester t) {
    Vehicle vehicle = new Vehicle(1,1,3,1,Color.RED, 20);
    return t.checkExpect(vehicle.inTile(1,1), true)
            && t.checkExpect(vehicle.inTile(3,1), true)
            && t.checkExpect(vehicle.inTile(2,1), true)
            && t.checkExpect(vehicle.inTile(1, 2), false);
  }

  boolean testOnMouseClicked(Tester t) {
    RushHour game = new RushHour(
            "+------+"
                    + "|      |"
                    + "|  C T |"
                    + "|c    CX"
                    + "|t     |"
                    + "|CCC c |"
                    + "|    c |"
                    + "+------+",
            new RushHourUtils(),
            20);
    game.onMouseClicked(new Posn(30, 70));
    return t.checkExpect(
            game.currentVehicleClicked,
            new Some<>(new Vehicle(1, 3, 2, 3, Color.MAGENTA, 20)));
  }
}
