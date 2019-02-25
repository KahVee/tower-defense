package towerdefense

import scalafx.scene.image._

class TraversableTile(val image: Image) extends Tile(image){
  override def toString = "o"
}