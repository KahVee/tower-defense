package towerdefense

class Grid(val grid: Array[Array[Tile]], val entryTile: Tile, val exitTile: Tile) {
  
}

sealed abstract class Direction(private val dir: (Int, Int)) {
  def *(tuple: (Float, Float)) = (dir._1 * tuple._1, dir._2 * tuple._2)
  def *(f: Float) = (dir._1 * f, dir._2 * f)
  def +(tuple: (Float, Float)) = (dir._1 + tuple._1, dir._2 + tuple._2)
}
case object Left extends Direction((-1, 0)) {}
case object Right extends Direction((1, 0)) {}
case object Up extends Direction((0, -1)) {}
case object Down extends Direction((0, 1)) {}