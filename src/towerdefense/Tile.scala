package towerdefense

import scalafx.scene.image._

class Tile(val image: Image, val coords: (Int, Int)) {
  val buildable = true
  override def toString = "tile at " + coords.toString
}

class TraversableTile(image: Image, coords: (Int, Int)) extends Tile(image, coords) {
  override val buildable = false
  override def toString = "path at " + coords.toString
}

class Building(image: Image, coords: (Int, Int), val price: (Int, Int)) extends Tile(image, coords) {

  override val buildable = false
  var isActive = false
  protected var game = gui.GUI.game

  def step(now: Float) = {

  }

  override def toString = "building at " + coords.toString
}

class Tower(image: Image, coords: (Int, Int), price: (Int, Int), val damage: Int = DefaultTowerDamage, val speed: Float = DefaultShootingSpeed, val range: Int = DefaultTowerRange) extends Building(image, coords, price) {

  override val buildable = false

  private var target: Option[Enemy] = None
  private var lastShotTime = 0F

  //TODO: (Maybe at some point) make a more accurate way of checking when to shoot, this method works well enough for slow towers, but not for fast ones.
  override def step(now: Float) = {
    if (isActive) {
      if (now > lastShotTime + speed) {
        target = findClosestTarget(game.enemies)
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

//Helper object to copy "sleeping" reference buildings into new active ones
object Building {
  def apply(other: Building, coords: (Int, Int)) = {
    if (other.isInstanceOf[Tower]) {
      val tower = other.asInstanceOf[Tower]
      val newTower = new Tower(tower.image, coords, tower.price, tower.damage)
      newTower
    } else {
      val building = new Building(other.image, coords, other.price)
      building
    }
  }
}