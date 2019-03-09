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
  protected var game: Option[Game] = None
  def initGame(in: Game) = game = Some(in)

  def step(now: Float) = {

  }

  override def toString = "building at " + coords.toString
}

class Tower(image: Image, coords: (Int, Int), price: (Int, Int)) extends Building(image, coords, price) {

  val damage = DefaultTowerDamage
  val speed = DefaultShootingSpeed
  val range = DefaultTowerRange

  private var target: Option[Enemy] = None
  private var lastShotTime = 0F

  override def step(now: Float) = {
    target = findClosestTarget(game.get.enemies)
    if (now > lastShotTime + speed) {
      if(target.isDefined) {
        target.get.takeDamage(damage)
        lastShotTime = now
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