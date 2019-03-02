package towerdefense

import scalafx.scene.image._

class Tile(val image: Image, val coords: (Int, Int)) {
  override def toString = "tile at " + coords.toString
}

class Building(image: Image, coords: (Int, Int)) extends Tile(image, coords) {
  //override def toString = "building at " + coords.toString
}

class TraversableTile(image: Image, coords: (Int, Int)) extends Tile(image, coords) {
  override def toString = "path at " + coords.toString
}