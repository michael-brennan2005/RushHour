import javalib.worldimages.*;
import tester.Tester;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

interface IList<T> {
  // Applies the func to all elements in the list.
  <U> IList<U> map(Function<T, U> func);

  // Produces a list only containing elements for which func returns true.
  IList<T> filter(Function<T, Boolean> func);

  // Applies the function from left to right to each item in the list and combines them.
  <U> U fold(BiFunction<T, U, U> func, U value);
}

class Cons<T> implements IList<T> {
  T first;
  IList<T> rest;

  Cons(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // Applies the func to all elements in the list.
  public <U> IList<U> map(Function<T, U> func) {
    return new Cons<U>(func.apply(this.first), this.rest.map(func));
  }

  // Produces a list only containing elements for which func returns true.
  public IList<T> filter(Function<T, Boolean> func) {
    if (func.apply(this.first)) {
      return new Cons<T>(this.first, this.rest.filter(func));
    } else {
      return this.rest.filter(func);
    }
  }

  // Applies the function from left to right to each item in the list and combines them.
  public <U> U fold(BiFunction<T, U, U> func, U value) {
    return this.rest.fold(func, func.apply(this.first, value));
  }
}

class MT<T> implements IList<T> {
  MT() {
  }

  // A mapping of any empty list is another empty list, so this produces an empty list.
  public <U> IList<U> map(Function<T, U> func) {
    return new MT<>();
  }

  // There are no elements for which the func could return true, so this produces an empty list.
  public IList<T> filter(Function<T, Boolean> func) {
    return new MT<>();
  }

  // There are no elements to fold upon, so this produces whatever value was passed to it.
  public <U> U fold(BiFunction<T, U, U> func, U value) {
    return value;
  }
}

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

  // Produces an image of the vehicle, given a tile size (how big 1 grid square is on
  // each side)
  WorldImage toImage() {
    return new RectangleImage(
            this.width() * this.tileSize,
            this.height() * this.tileSize,
            OutlineMode.SOLID,
            this.color);
  }

  int width() {
    return Math.abs(this.x1 - this.x2) + 1;
  }

  int height() {
    return Math.abs(this.y1 - this.y2) + 1;
  }

  public boolean overlaps(Vehicle that) {
    //this vehicle can overlap that horizontally or vertically
    //idea: make list of positions of this and that, contains on them
    //if disjoint, return false
    return this.overlapsHelper(this.x1, this.y1, this.x2, this.y2,
            that.x1, that.y1, that.x2, that.y2);
  }

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
      //cross case
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
}

class PlaceVehiclesOntoImage implements BiFunction<Vehicle, WorldImage, WorldImage> {
  PlaceVehiclesOntoImage() {
  }

  public WorldImage apply(Vehicle vehicle, WorldImage image) {
    return new PlaceImage(image, vehicle.toImage(), vehicle.x1, vehicle.y1).toImage();
  }
}

class Grid {
  int rows;
  int cols;
  int tileSize;
  //draw grid -- make beside/above squares

  public Grid(int rows, int cols, int tileSize) {
    this.rows = rows;
    this.cols = cols;
    this.tileSize = tileSize;
  }

  // Produces an image of an empty grid.
  WorldImage toImage() {
    WorldImage topAndBottomBorder = new RectangleImage(
            this.cols * this.tileSize,
            this.tileSize,
            OutlineMode.SOLID,
            Color.DARK_GRAY);
    WorldImage leftAndRightBorder = new RectangleImage(
            this.tileSize,
            (this.rows - 2) * this.tileSize,
            OutlineMode.SOLID,
            Color.DARK_GRAY
    );
    WorldImage playArea = new RectangleImage(
            (this.cols - 2) * this.tileSize,
            (this.rows - 2) * this.tileSize,
            OutlineMode.SOLID,
            Color.WHITE
    );

    return new AboveImage(
            topAndBottomBorder,
            new BesideImage(
                    leftAndRightBorder,
                    playArea,
                    leftAndRightBorder
            ),
            topAndBottomBorder
    );
  }
}

class RushHour {
  IList<Vehicle> vehicles;
  Grid grid;
  //make an exit posn
  RushHourUtils utils;
  int tileSize;
  //ideas for wincheck
  /*
  we add new fields to denote target vehicle and exit coords
  regular constructor:
  cons target vehicle to vehicles list
  make sure defined endx and endy are in the grid (helper method)
  string constructor:
  ermm find an arbitrary vehicle on same line as exit, set that as target vehicle
  dont add to the vehicles list (consing in in reg constructor)
  then find x and set those coords as endx endy
  then just wincheck by checking if any coordinate in target vehicle = endx endy
  1love
   */
  Vehicle targetVehicle;
  int endX;
  int endY;

  RushHour(IList<Vehicle> vehicles, Grid board, int tileSize,
           Vehicle targetVehicle, int endX, int endY) {
    this.vehicles = vehicles;
    this.grid = board;
    this.tileSize = tileSize;
    this.utils = new RushHourUtils();
  }

  RushHour(String level, RushHourUtils utils, int tileSize) {
    this(utils.getVehiclesList(level, level.length(),
                    0, 0, 0, new MT<Vehicle>(), tileSize),
            utils.getGrid(level, 0, 0, 0, tileSize),
            tileSize);
  }

  WorldImage toImage() {
    return this.vehicles
            .fold(new PlaceVehiclesOntoImage(), this.grid.toImage());
  }
}

class RushHourUtils {
  public IList<Vehicle> getVehiclesList(String level, int length,
                                        int i, int r, int c, IList<Vehicle> currList, int tileSize) {
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
}

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
            this.base.movePinhole(
                    (-1.0 * this.base.getWidth() / 2.0) + this.offsetX,
                    (-1.0 * this.base.getHeight() / 2.0) + this.offsetY),
            this.top).movePinholeTo(new Posn(0, 0));
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

    RushHour game = new RushHour(vehicles, grid, 20);
    game.toImage().saveImage("final.png");
  }
}

class ExamplesRushHour {
  //the \n were screwing everything up
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
  Vehicle car1 = new Vehicle(1, 2, 2, 2, Color.BLUE, 20);
  Vehicle car2 = new Vehicle(2, 2, 3, 2, Color.BLUE, 20);
  Vehicle truck1 = new Vehicle(1, 1, 1, 3, Color.BLUE, 20);
  Vehicle truck2 = new Vehicle(1, 2, 1, 5, Color.BLUE, 20);

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

    /*
    boolean testSubstring(Tester t) {
        String hi = "hi\nhi";
        System.out.println(hi);
        return t.checkExpect(hi.length(), 4);
    }
     */
}