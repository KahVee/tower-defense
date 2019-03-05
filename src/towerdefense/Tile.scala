package towerdefense

import scalafx.scene.image._

class Tile(val image: Image, val coords: (Int, Int)) {
  override def toString = "tile at " + coords.toString
}

class TraversableTile(image: Image, coords: (Int, Int)) extends Tile(image, coords) {
  override def toString = "path at " + coords.toString
}

class Building(image: Image, coords: (Int, Int)) extends Tile(image, coords) {  
  override def toString = "building at " + coords.toString
}

class Tower(image: Image, coords: (Int, Int)) extends Building(image, coords) {
  
  val damage = DefaultTowerDamage
  val speed = DefaultShootingSpeed
  val range = DefaultTowerRange
  
  private var target: Option[Enemy] = None
  
  private def findClosestTarget(enemies: Vector[Enemy]) = {
    if(enemies.nonEmpty)
        enemies.minBy(x => math.pow((coords._1 - x.coords._1), 2) + math.pow((coords._2 - x.coords._2), 2))
        
  }
  
  override def toString = "tower at " + coords.toString
}