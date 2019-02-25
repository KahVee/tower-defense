package towerdefense

import scalafx.scene.image._

class Building(val image: Image) extends Tile(image) {
  
}

object Building {
  def apply(other: Building) = {
    new Building(other.image)
  }
}