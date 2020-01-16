package towerdefense

import scala.collection.mutable.Buffer
import scalafx.scene.image._

class Game(val name: String, val grid: Grid, var resX: Int, var resY: Int, var buildableBuildings: Vector[Building], private var waves: Vector[Wave], var health: Int) {

  private var timePassed = 0F

  var builtBuildings = Vector[Building]()
  var enemies = Vector[Enemy]()
  private var queuedEnemies = Vector[Enemy]()
  private var lastSpawnedEnemyTime = 0F

  def isLost = health <= 0

  //Starting method, which initializes buildings that were already present on the loaded map
  def start() = {
    builtBuildings = grid.grid.flatten.filter(_.isInstanceOf[Building]).map(_.asInstanceOf[Building]).toVector
    builtBuildings.foreach(_.isActive = true)
  }

  //Gets called every tick. Updates all enemies and buildings, and calls enemy spawning methods if necessary
  def step(dt: Float) = {
    enemies.foreach(_.step(dt))
    health -= enemies.filter(_.reachedTarget).size
    enemies.foreach(x => if (x.isDead) addKillReward(x))
    enemies = enemies.filter(_.isActive)
    builtBuildings.foreach(_.step(timePassed))

    if (!waves.isEmpty) {
      if (timePassed > waves(0).time) {
        spawnWave(waves(0))
        waves = waves.tail
      }
    }

    //If there are enemies in the spawn queue, spawn a new one when the appropriate time has passed
    if (!queuedEnemies.isEmpty && timePassed > lastSpawnedEnemyTime + EnemySpawnInterval) spawnEnemyFromQueue

    //update the game's current "time" with deltaTime
    timePassed += dt
  }

  //Returns images and game space coordinates of all tiles in one Vector
  def tileDrawables = {
    val buffer = Buffer[(Image, Int, Int)]()
    grid.grid.flatten.foreach(tile => buffer += ((tile.image, tile.coords._1, tile.coords._2)))
    buffer.toVector
  }

  //Returns images and game space coordinates of all enemies in one Vector
  def enemyDrawables = {
    val buffer = Buffer[(Image, Float, Float)]()
    enemies.foreach(enemy => buffer += ((enemy.image, enemy.coords._1 , enemy.coords._2)))
    buffer.toVector
  }

  //Activates the next enemy from the queue, adds it to the active enemies Vector, and updates the last spawned time
  def spawnEnemyFromQueue = {
    queuedEnemies(0).coords = ((grid.entryTile.asInstanceOf[TraversableTile].entryDirection.get + (grid.entryTile.coords._1.toFloat, grid.entryTile.coords._2.toFloat)))
    queuedEnemies(0).isActive = true
    enemies = enemies :+ queuedEnemies(0)
    queuedEnemies = queuedEnemies.tail
    lastSpawnedEnemyTime = timePassed
  }

  //Loads a wave of enemies into the spawn queue.
  def spawnWave(wave: Wave) = {
    wave.enemies.foreach(x =>
      for (i <- 0 until x._2) {
        queuedEnemies = queuedEnemies :+ Enemy(x._1)
      })
  }

  //Builds a given building in the given coordinates and activates it. Updates the resources, grid and list of built buildings.
  def buildBuilding(building: Building, coords: (Int, Int)) = {
    val newBuilding = building.clone(coords)
    resX -= newBuilding.price._1
    resY -= newBuilding.price._2
    grid.grid(coords._1)(coords._2) = newBuilding
    builtBuildings = builtBuildings :+ newBuilding
    newBuilding.isActive = true
  }

  def addKillReward(enemy: Enemy) = {
    resX += enemy.killReward._1
    resY += enemy.killReward._2
  }

}

