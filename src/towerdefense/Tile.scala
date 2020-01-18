package towerdefense

import scalafx.scene.image._

//Grand parent of the Tile family. Note that this parent class should never have object instances
class Tile(val name: String, val image: Image, val coords: (Int, Int)) {
  val buildable = true

  def clone(newCoords: (Int, Int)) = new Tile(this.name, this.image, newCoords)

  override def toString = "tile at " + coords.toString
}

//Normal "path" tile, on which enemies move forward. If entryDirection is defined, the tile is treated as a spawn tile, next to which enemies spawn.
//Similarily exitDirection is defined on goal tiles, where enemies exit the map.
class TraversableTile(name: String, image: Image, coords: (Int, Int), val entryDirection: Option[Direction], val exitDirection: Option[Direction]) extends Tile(name, image, coords) {
  override val buildable = false

  override def clone(newCoords: (Int, Int)) = new TraversableTile(this.name, this.image, newCoords, this.entryDirection, this.exitDirection)

  override def toString = "path at " + coords.toString
}

//Parent for both types of buildable buildings (Tower, ProductionBuilding). There should never be any object instances of this class.
class Building(name: String, image: Image, coords: (Int, Int), val price: (Int, Int)) extends Tile(name, image, coords) {

  override val buildable = false
  var isActive = false
  protected val game = gui.GUI.activeGame

  def step(now: Float) = ()

  override def clone(newCoords: (Int, Int)) = new Building(this.name, this.image, newCoords, this.price)

  def tooltip = s"$name\nX: ${price._1} Y: ${price._2}"

  override def toString = "building at " + coords.toString
}

//Buildable tower tile
class Tower(name: String, image: Image, coords: (Int, Int), price: (Int, Int), private val damage: Int = DefaultTowerDamage, private val reload: Float = DefaultReload, val range: Int = DefaultRange) extends Building(name, image, coords, price) {

  private var target: Option[Enemy] = None
  private var lastShotTime = 0F

  override def clone(newCoords: (Int, Int)) = new Tower(this.name, this.image, newCoords, this.price, this.damage, this.reload, this.range)

  //TODO: (Maybe at some point) make a more accurate way of checking when to shoot, this method works well enough for slow towers, but not for fast ones.
  //Current time is passed into this method as seconds. After "reload" seconds have passed, the tower can fire again.
  override def step(now: Float) = {
    if (isActive) {
      if (now > lastShotTime + reload) {
        target = findClosestTarget(game.enemies.filterNot(_.isDead))
        if (target.isDefined && squareDistanceToEnemy(target.get) < range * range) {
          target.get.takeDamage(damage)
          lastShotTime = now
        }
      }
    }
  }

  private def squareDistanceToEnemy(enemy: Enemy) = {
    math.pow((coords._1 - enemy.coords._1), 2) + math.pow((coords._2 - enemy.coords._2), 2)
  }

  private def findClosestTarget(enemies: Vector[Enemy]) = {
    if (enemies.nonEmpty)
      Some(enemies.minBy(squareDistanceToEnemy))
    else
      None
  }

  override def tooltip = s"$name\nX: ${price._1} Y: ${price._2}\nDamage: $damage\nReload: $reload\nRange: $range"

  override def toString = "tower at " + coords.toString
}

//Buildable resource-producing tile
class ProductionBuilding(name: String, image: Image, coords: (Int, Int), price: (Int, Int), private val productionAmount: (Int, Int) = DefaultBuildingProductionAmount, private val productionSpeed: Int = DefaultBuildingProductionSpeed) extends Building(name, image, coords, price) {

  private var lastProductionTime = 0F

  override def clone(newCoords: (Int, Int)) = new ProductionBuilding(this.name, this.image, newCoords, this.price, this.productionAmount, this.productionSpeed)

  override def step(now: Float) = {
    if (isActive) {
      if (now > lastProductionTime + (60.0 / productionSpeed)) {
        game.resX += productionAmount._1
        game.resY += productionAmount._2
        lastProductionTime = now
      }
    }
  }

  override def tooltip = s"$name\nX: ${price._1} Y: ${price._2}\nProduction: ${productionAmount._1}/${productionAmount._2}\nSpeed: $productionSpeed"
}