package towerdefense

import scalafx.scene.image._

class Tile(val name: String, val image: Image, val coords: (Int, Int)) {
  val buildable = true

  def clone(newCoords: (Int, Int)) = new Tile(this.name, this.image, newCoords)

  override def toString = "tile at " + coords.toString
}

class TraversableTile(name: String, image: Image, coords: (Int, Int), val entryDirection: Option[Direction], val exitDirection: Option[Direction]) extends Tile(name, image, coords) {
  override val buildable = false

  override def clone(newCoords: (Int, Int)) = new TraversableTile(this.name, this.image, newCoords, this.entryDirection, this.exitDirection)

  override def toString = "path at " + coords.toString
}

class Building(name: String, image: Image, coords: (Int, Int), val price: (Int, Int)) extends Tile(name, image, coords) {

  override val buildable = false
  var isActive = false
  protected var game = gui.GUI.game

  override def clone(newCoords: (Int, Int)) = new Building(this.name, this.image, newCoords, this.price)

  def step(now: Float) = {

  }

  override def toString = "building at " + coords.toString
}

class Tower(name: String, image: Image, coords: (Int, Int), price: (Int, Int), val damage: Int = DefaultTowerDamage, val reload: Float = DefaultReload, val range: Int = DefaultRange) extends Building(name, image, coords, price) {

  override val buildable = false

  private var target: Option[Enemy] = None
  private var lastShotTime = 0F

  override def clone(newCoords: (Int, Int)) = new Tower(this.name, this.image, newCoords, this.price, this.damage, this.reload, this.range)

  //TODO: (Maybe at some point) make a more accurate way of checking when to shoot, this method works well enough for slow towers, but not for fast ones.
  //Current time is passed into this method as seconds. After "reload" seconds have passed, the tower can fire again.
  override def step(now: Float) = {
    if (isActive) {
      if (now > lastShotTime + reload) {
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
      val newTower = new Tower(tower.name, tower.image, coords, tower.price, tower.damage)
      newTower
    } else {
      val building = new Building(other.name, other.image, coords, other.price)
      building
    }
  }
}