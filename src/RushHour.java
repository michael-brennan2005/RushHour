import javalib.funworld.World;
import javalib.funworld.WorldScene;
import javalib.worldimages.*;
import tester.Tester;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

//represents a list of elements
interface IList<T> {
  // Applies the func to all elements in the list.
  <U> IList<U> map(Function<T, U> func);

  // Produces a list only containing elements for which func returns true.
  IList<T> filter(Function<T, Boolean> func);

  // Applies the function from left to right to each item in the list and combines them.
  <U> U fold(BiFunction<T, U, U> func, U value);
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

  WorldImage draw(int tileSize) {
    return new VisiblePinholeImage(
            new RectangleImage(
                    tileSize,
                    tileSize,
                    OutlineMode.SOLID,
                    this.color
            ).movePinhole(-0.5 * tileSize, -0.5 * tileSize)
    );
  }

  WorldScene drawOntoScene(WorldScene scene, int tileSize) {
    return scene.placeImageXY(this.draw(tileSize),
            this.col * tileSize, this.row * tileSize);
  }
}

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
  int tileSize;
  IList<Tile> tiles;

  /*
  WorldScene makeScene() {
    WorldScene base = new WorldScene(
            this.cols * this.tileSize,
            this.rows * this.tileSize)
            .placeImageXY(
                    new RectangleImage(
                            this.cols * this.tileSize,
                            this.rows * this.tileSize,
                            OutlineMode.SOLID,
                            Color.WHITE),
                    0,
                    0);
    return ;
  };
  */
}

/*
class Vec2D {
  int x;
  int y;

  Vec2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public boolean isStraight(Vec2D that) {
    return this.y == that.y
            || this.x == that.x;
  }
}
 */

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

  // Produces an image of the vehicle, given a tile size (how big 1 grid square is on
  // each side)
  WorldImage toImage() {
    return new RectangleImage(
            this.width() * this.tileSize,
            this.height() * this.tileSize,
            OutlineMode.SOLID,
            this.color);
  }

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
}

class PlaceVehiclesOntoImage implements BiFunction<Vehicle, WorldImage, WorldImage> {

  public WorldImage apply(Vehicle vehicle, WorldImage image) {
    return new PlaceImage(image, vehicle.toImage(), vehicle.x1, vehicle.y1).toImage();
  }
}

//represents the playing board of RushHour
class Grid {
  int rows;
  int cols;
  int tileSize;

  public Grid(int rows, int cols, int tileSize) {
    this.rows = rows;
    this.cols = cols;
    this.tileSize = tileSize;
  }

  /*
  F:
  rows -- int
  cols -- int
  tileSize -- int
  M:
  totalWidth -- int
  totalHeight -- int
  toImage -- WorldImage
  MoF:
   */

  /*
  P:
  MoP:
   */

  //calculates the totalWidth of this Grid
  int totalWidth() {
    return cols * tileSize;
  }

  /*
  P:
  MoP:
   */

  //calculates the totalHeight of this Grid
  int totalHeight() {
    return rows * tileSize;
  }

  /*
  P:
  MoP:
   */

  // Produces an image of an empty grid.
  WorldImage toImage() {
    WorldImage base = new VisiblePinholeImage(
            new RectangleImage(
                    this.tileSize * this.cols,
                    this.tileSize * this.rows,
                    OutlineMode.SOLID,
                    Color.WHITE)
                    .movePinholeTo(
                            new Posn(
                                    (int)((this.tileSize * this.cols) / -2.0),
                                    (int)((this.tileSize * this.rows) / -2.0))));
    WorldImage addTopBorder = new PlaceImage(
            base,
            new RectangleImage(
                    this.tileSize * this.cols,
                    this.tileSize,
                    OutlineMode.SOLID,
                    Color.GRAY),
            0,
            0).toImage();
    return addTopBorder;
  }
}

//represents the game RushHour with all its components
class RushHour extends World {
  IList<Vehicle> vehicles;
  Grid grid;
  RushHourUtils utils;
  int tileSize;
  Vehicle targetVehicle;
  int endX;
  int endY;


  RushHour(IList<Vehicle> vehicles, Grid board, int tileSize,
           Vehicle targetVehicle, int endX, int endY) {
    super();
    this.vehicles = vehicles;
    this.grid = board;
    this.tileSize = tileSize;
    this.utils = new RushHourUtils();
    this.targetVehicle = targetVehicle;
    this.endX = endX;
    this.endY = endY;
  }

  /*
  F:
  vehicles -- IList<Vehicle>
  grid -- Grid
  utils -- RushHourUtils
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
            utils.getGrid(level, 0, 0, 0, tileSize),
            tileSize,
    //below vehicle seems to be the same target everytime
    new Vehicle(1, 3, 2, 3, Color.RED, tileSize),
            utils.getEndX(level, 0, 0, 0, tileSize),
            utils.getEndY(level, 0, 0, 0, tileSize));
  }

  /*
  P:
  MoP;
   */

  //converts RushHour to a WorldImage
  WorldImage toImage() {
    return this.vehicles
            .fold(new PlaceVehiclesOntoImage(), this.grid.toImage());
  }

  /*
  P:
  MoP;
   */

  //creates a WorldScene
  public WorldScene makeScene() {
    return new WorldScene(grid.totalWidth(), grid.totalHeight());
  }

  /*
  P:
  MoP;
   */

  //determines if this RushHour has been won
  public boolean winCheck() {
    return this.targetVehicle.atLocation(this.endX, this.endY);
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
                              Color.YELLOW,
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
  public Grid getGrid(String level, int i, int r, int currCol, int tileSize) {
    String currLetter = level.substring(i, i + 1);
    if (i == level.length() - 1) {
      //0 index
      return new Grid(r, currCol, tileSize);
    } else if (currCol == 0
            || (!currLetter.equals("+")
            && !currLetter.equals("|")
            && !currLetter.equals("X"))) {
      return this.getGrid(level, i + 1, r, currCol + 1, tileSize);
    } else {
      return this.getGrid(level, i + 1, r + 1, 0, tileSize);
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
  public int getEndX(String level, int i, int r, int currCol, int tileSize) {
    String currLetter = level.substring(i, i + 1);
    if (currLetter.equals("X")) {
    return currCol;
    } else if (currCol == 0
            || (!currLetter.equals("+")
            && !currLetter.equals("|"))) {
      return this.getEndX(level, i + 1, r, currCol + 1, tileSize);
    } else {
      return this.getEndX(level, i + 1, r + 1, 0, tileSize);
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
  public int getEndY(String level, int i, int r, int currCol, int tileSize) {
    String currLetter = level.substring(i, i + 1);
    if (currLetter.equals("X")) {
      return r;
    } else if (currCol == 0
            || (!currLetter.equals("+")
            && !currLetter.equals("|"))) {
      return this.getEndY(level, i + 1, r, currCol + 1, tileSize);
    } else {
      return this.getEndY(level, i + 1, r + 1, 0, tileSize);
    }
  }
}

//represents a place image function
class PlaceImage {
  WorldImage base;
  WorldImage top;
  double offsetX;
  double offsetY;

  public PlaceImage(WorldImage base, WorldImage top, int offsetX, int offsetY) {
    this.base = base;
    this.top = top;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  WorldImage toImage() {
    return new OverlayImage(
            this.base.movePinholeTo(
                    new Posn(
                            (int)((-1.0 * this.base.getWidth() / 2.0)),
                            (int)((-1.0 * this.base.getHeight() / 2.0))))
                    .movePinhole(this.offsetX, this.offsetY),
            this.top.movePinholeTo(
                    new Posn(
                            (int)((-1.0 * this.top.getWidth() / 2.0)),
                            (int)((-1.0 * this.top.getHeight() / 2.0))
                    )
            )).movePinholeTo(new Posn(0, 0));
  }
}

class Main {
  public static void main(String[] args) {
    IList<Vehicle> vehicles = new Cons<>(
            new Vehicle(1, 1, 1, 2, Color.RED, 20),
            new Cons<>(
                    new Vehicle(2, 3, 4, 3, Color.BLUE, 20),
                    new Cons<>(
                            new Vehicle(1, 3, 1, 4, Color.YELLOW, 20),
                            new MT<>())));
    Grid grid = new Grid(6, 6, 20);

    Tile tile1 = new Tile(1,2,Color.RED);
    tile1.drawOntoScene(new WorldScene(120,120), 10).saveImage("final.png");
    //RushHour game = new RushHour(vehicles, grid, 20);
    //grid.toImage().saveImage("final.png");
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
          this.game2.grid,
          20,
          new Vehicle(5, 3, 6, 3,
                  Color.RED, 20),
          this.game2.endX,
          this.game2.endY);
  Vehicle car1 = new Vehicle(1, 2, 2, 2, Color.BLUE, 20);
  Vehicle car2 = new Vehicle(2, 2, 3, 2, Color.BLUE, 20);
  Vehicle truck1 = new Vehicle(1, 1, 1, 3, Color.BLUE, 20);
  Vehicle truck2 = new Vehicle(1, 2, 1, 4, Color.BLUE, 20);
  Grid gridEx = new Grid(8, 7, 20);

  boolean testConstructor(Tester t) {
    return t.checkExpect(game1.grid,
            //we are going to zero index
            new Grid(7, 7, 20))
            && t.checkExpect(game2.grid,
            new Grid(5, 6, 20))
            && t.checkExpect(game2.vehicles,
            new Cons<Vehicle>(
                    new Vehicle(1, 4, 3, 4,
                            Color.green,
                            20),
                    new Cons<Vehicle>(
                            new Vehicle(1, 3, 2, 3,
                                    Color.magenta,
                                    20),
                            new Cons<Vehicle>(
                                    new Vehicle(5, 2, 5, 4,
                                            Color.yellow,
                                            20),
                                    new Cons<Vehicle>(
                                            new Vehicle(3, 2, 3, 3,
                                                    Color.blue,
                                                    20),
                                            new MT<Vehicle>())))));
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
            && t.checkExpect(gridEx.totalHeight(), 160)
            && t.checkExpect(gridEx.totalWidth(), 140);
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
            new MT<Vehicle>(), 20),
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
            && t.checkExpect(utils.getGrid(level, 0, 0, 0, 20),
            new Grid(7, 7, 20))
            && t.checkExpect(utils.getEndX(level, 0, 0, 0, 20),
            7)
            && t.checkExpect(utils.getEndY(level, 0, 0, 0, 20),
            3);
  }
}