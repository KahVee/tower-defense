package towerdefense

import scalafx.scene.image._

class Tile(val image: Image, val coords: (Int, Int)) {

  override def toString = "x"
}

class Building(image: Image, coords: (Int, Int)) extends Tile(image, coords) {

}

class TraversableTile(image: Image, coords: (Int, Int)) extends Tile(image, coords) {
  override def toString = "o"
}