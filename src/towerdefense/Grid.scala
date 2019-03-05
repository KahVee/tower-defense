package towerdefense

import scala.math.Numeric.Implicits.infixNumericOps

class Grid(val grid: Array[Array[Tile]], val entryTile: Tile, val exitTile: Tile) {

  def nextTile(dir: Direction, coords: (Int, Int)) = {
    val next = dir + coords

    if (grid.indices.contains(next._1) && grid(0).indices.contains(next._2)) {
      Some(grid(next._1)(next._2))
    } else {
      None
    }
  }

  def currentTile(coords: (Float, Float)) = {
    val rounded = (coords._1.round, coords._2.round)
    if (grid.indices.contains(rounded._1) && grid(0).indices.contains(rounded._2)) {
      Some(grid(rounded._1)(rounded._2))
    } else {
      None
    }
  }
}

sealed abstract class Direction(val dir: (Int, Int)) {
  def *(tuple: (Float, Float)) = (dir._1 * tuple._1, dir._2 * tuple._2)
  def *(f: Float) = (dir._1 * f, dir._2 * f)
  def +[A: Numeric, B: Numeric](tuple: (A, B)): (A, B) = (implicitly[Numeric[A]].fromInt(dir._1) + tuple._1, implicitly[Numeric[B]].fromInt(dir._2) + tuple._2)

  def clockwise = this match {
    case Up    => Right
    case Right => Down
    case Down  => Left
    case Left  => Up
  }

}

case object Left extends Direction((-1, 0)) {}
case object Right extends Direction((1, 0)) {}
case object Up extends Direction((0, -1)) {}
case object Down extends Direction((0, 1)) {}