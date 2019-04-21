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
  protected val game = gui.GUI.game

  def step(now: Float) = ()
  
  override def clone(newCoords: (Int, Int)) = new Building(this.name, this.image, newCoords, this.price)

  override def toString = "building at " + coords.toString
}

//"Production" is how many times per minute a building produces 1x of the resource
class ProductionBuilding(name: String, image: Image, coords: (Int, Int), price: (Int, Int), private val productionAmount: (Int, Int) = DefaultBuildingProductionAmount, private val productionSpeed: Int = DefaultBuildingProductionSpeed) extends Building(name, image, coords, price) {

  private var lastProductionTime = 0F

  override def step(now: Float) = {
    if (isActive) {
      if (now > lastProductionTime + (60.0 / productionSpeed)) {
        game.resX += productionAmount._1
        game.resY += productionAmount._2
        lastProductionTime = now
      }
    }
  }

  override def clone(newCoords: (Int, Int)) = new ProductionBuilding(this.name, this.image, newCoords, this.price, this.productionAmount, this.productionSpeed)
}

class Tower(name: String, image: Image, coords: (Int, Int), price: (Int, Int), private val damage: Int = DefaultTowerDamage, private val reload: Float = DefaultReload, private val range: Int = DefaultRange) extends Building(name, image, coords, price) {

  private var target: Option[Enemy] = None
  private var lastShotTime = 0F

  override def clone(newCoords: (Int, Int)) = new Tower(this.name, this.image, newCoords, this.price, this.damage, this.reload, this.range)

  //TODO: (Maybe at some point) make a more accurate way of checking when to shoot, this method works well enough for slow towers, but not for fast ones.
  //Current time is passed into this method as seconds. After "reload" seconds have passed, the tower can fire again.
  override def step(now: Float) = {
    if (isActive) {
      if (now > lastShotTime + reload) {
        target = findClosestTarget(game.enemies.filterNot(_.isDead))
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