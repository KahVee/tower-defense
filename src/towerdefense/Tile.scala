package towerdefense

import scalafx.scene.image._

class Tile(val image: Image, val coords: (Int, Int)) {
  override def toString = "tile at " + coords.toString
}

class TraversableTile(image: Image, coords: (Int, Int)) extends Tile(image, coords) {
  override def toString = "path at " + coords.toString
}

class Building(image: Image, coords: (Int, Int), val price: (Int, Int)) extends Tile(image, coords) {

  var isActive = false
  protected var game = gui.GUI.game

  def step(now: Float) = {

  }

  override def toString = "building at " + coords.toString
}

object Building {
  def apply(other:Building) = {
    val building = new Building(other.image, other.coords, other.price)
    building
  }
}

class Tower(image: Image, coords: (Int, Int), price: (Int, Int)) extends Building(image, coords, price) {
  
  private var damage = DefaultTowerDamage
  private var speed = DefaultShootingSpeed
  private var range = DefaultTowerRange

  private var target: Option[Enemy] = None
  private var lastShotTime = 0F

  override def step(now: Float) = {
    if (isActive) {
      target = findClosestTarget(game.enemies)
      if (now > lastShotTime + speed) {
        if (target.isDefined) {
          target.get.takeDamage(damage)
          lastShotTime = now
        }
      }
    }
  }

  private def findClosestTarget(enemies: Vector[Enemy]) = {
    if (enemies.nonEmpty)
      Some(enemies.minBy(x => math.pow((coords._1 - x.coords._1), 2) + math.pow((coords._2 - x.coords._2), 2)))
    else
      None
  }

  override def toString = "tower at " + coords.toString
}

//helper object to make a new Tower with copied parameters from another
object Tower {
  def apply(other: Tower) = {
    val tower = new Tower(other.image, other.coords, other.price)
    tower.damage = other.damage
    tower.speed = other.speed
    tower.range = other.range
    tower
  }
}