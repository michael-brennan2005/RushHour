import javalib.worldimages.*;

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
    MT() {}

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
        //return this.overlapsHelper(this.startPosition, this.endPosition, that.startPosition, that.endPosition);
        return true;
    }

    public boolean overlapsHelper(Vec2D start1, Vec2D end1,
                                  Vec2D start2, Vec2D end2) {
        return true;
    }
}
class PlaceVehiclesOntoImage implements BiFunction<Vehicle, WorldImage, WorldImage> {
    PlaceVehiclesOntoImage() {}

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
    RushHourUtils utils;

    RushHour(IList<Vehicle> vehicles, Grid board) {
        this.vehicles = vehicles;
        this.grid = board;
        this.utils = new RushHourUtils();
    }
/*
    RushHour(String level, RushHourUtils utils) {
        this(utils.getVehiclesList(level, level.length(),
                0, 0, 0, new MT<Vehicle>(), this.tileSize),
                utils.getGrid(level, 0, 1, 0, this.tileSize));
    }

 */

    WorldImage toImage() {
        return this.vehicles
                .fold(new PlaceVehiclesOntoImage(), this.grid.toImage());
    }
}

class RushHourUtils {
    public IList<Vehicle> getVehiclesList(String level, int length,
                                   int i, int r, int c, IList<Vehicle> currList, int tileSize) {
        String currLetter = level.substring(i, i+1);
        if (i == length - 1) {
            return currList;
        } else if (currLetter.equals("T")) {
            return this.getVehiclesList(level, length,
                    i + 1, r, c + 1, new Cons<Vehicle>(
                            new Vehicle(r,
                                    c,
                                    r + 3,
                                    c,
                                    Color.YELLOW,
                                    tileSize),
                            currList), tileSize);
        } else if (currLetter.equals("t")) {
            return this.getVehiclesList(level, length,
                    i + 1, r, c + 1, new Cons<Vehicle>(
                            new Vehicle(
                                    r,
                                    c,
                                    r,
                                    c + 3,
                                    Color.GREEN,
                                    tileSize),
                            currList), tileSize);
        } else if (currLetter.equals("C")) {
            return this.getVehiclesList(level, length,
                    i + 1, r, c + 1, new Cons<Vehicle>(
                            new Vehicle(
                                    r,
                                    c,
                                    r + 2,
                                    c,
                                    Color.BLUE,
                                    tileSize),
                            currList), tileSize);
        } else if (currLetter.equals("c")) {
            return this.getVehiclesList(level, length,
                    i + 1, r, c + 1, new Cons<Vehicle>(
                            new Vehicle(
                                    r,
                                    c,
                                    r,
                                    c + 2,
                                    Color.MAGENTA,
                                    tileSize),
                            currList), tileSize);
        } else if (currLetter.equals("-")
                || currLetter.equals("+")
                || currLetter.equals("|")
                || currLetter.equals("\\")) {
            return this.getVehiclesList(level, length,
                    i + 1, r, c + 1, currList, tileSize);
        } else if (currLetter.equals("n")) {
            return this.getVehiclesList(level, length,
                    i + 1, r + 1, 0, currList, tileSize);
        }
        return currList;
    }

    public Grid getGrid(String level, int i, int r, int c, int tileSize) {
        String currLetter = level.substring(i, i + 1);
        if (i == level.length() - 1) {
            return new Grid(r, c, tileSize);
        } else if (!currLetter.equals("n") && r == 1) {
            return this.getGrid(level, i + 1, r, c + 1, tileSize);
        } else if (!currLetter.equals("n")) {
            return this.getGrid(level, i + 1, r, c, tileSize);
        } else {
            return this.getGrid(level, i + 1, r + 1, c, tileSize);
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
                this.top).movePinholeTo(new Posn(0,0));
    }
}

class Main {
    public static void main(String[] ar gs) {
        IList<Vehicle> vehicles = new Cons<>(
                new Vehicle(1, 1, 1, 2, Color.RED, 20),
                new Cons<>(
                        new Vehicle(2,3,4,3,Color.BLUE,20),
                        new Cons<>(
                                new Vehicle(1,3,1,4,Color.YELLOW,20),
                                new MT<>())));
        Grid grid = new Grid(6,6, 20);

        RushHour game = new RushHour(vehicles, grid);
        game.toImage().saveImage("final.png");
    }
}

class ExamplesRushHour {
}