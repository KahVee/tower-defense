package towerdefense

import scala.math.Numeric.Implicits.infixNumericOps

//Grid represents the background tiles and acts as their "storage" object.
class Grid(val grid: Array[Array[Tile]], val entryTile: Tile, val exitTile: Tile) {
  
  //Barebones tile object that enemies use for pathfinding
  val referenceEmptyTile = new Tile("referencetile", DefaultImage, (0, 0))

  //Finds the tile next to given coordinates towards dir and returns it in an Option
  def nextTile(dir: Direction, coords: (Int, Int)) = {

    val next = dir + coords

    if (grid.indices.contains(next._1) && grid(0).indices.contains(next._2)) {
      Some(grid(next._1)(next._2))
    } else {
      None
    }
  }

  //Converts given world coordinates to grid coordinates and returns the corresponding tile in an Option
  def currentTile(coords: (Float, Float)) = {
    val rounded = (coords._1.round, coords._2.round)
    if (grid.indices.contains(rounded._1) && grid(0).indices.contains(rounded._2)) {
      Some(grid(rounded._1)(rounded._2))
    } else {
      None
    }
  }
}

//Simple class that has five enumerations for each direction, as well as "identity"
//Some helper methods are added to ease the multiplication and addition of vectors
sealed abstract class Direction(val dir: (Int, Int)) {
  def *(tuple: (Float, Float)) = (dir._1 * tuple._1, dir._2 * tuple._2)
  def *(f: Float) = (dir._1 * f, dir._2 * f)
  def +[A: Numeric, B: Numeric](tuple: (A, B)): (A, B) = (implicitly[Numeric[A]].fromInt(dir._1) + tuple._1, implicitly[Numeric[B]].fromInt(dir._2) + tuple._2)

  def clockwise = this match {
    case Up    => Right
    case Right => Down
    case Down  => Left
    case Left  => Up
    case Identity => Identity
  }

}

case object Left extends Direction((-1, 0))
case object Right extends Direction((1, 0))
case object Up extends Direction((0, -1))
case object Down extends Direction((0, 1))
case object Identity extends Direction((0, 0))