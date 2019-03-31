package towerdefense

import scala.collection.mutable.Buffer
import scalafx.scene.image._

class Game(val grid: Grid, var resX: Int, var resY: Int, var buildableBuildings: Vector[Building], var enemies: Vector[Enemy], private var waves: Vector[Wave], var health: Int) {

  private var timePassed = 0F

  var builtBuildings = Vector[Building]()
  private var queuedEnemies = Vector[Enemy]()
  private var lastSpawnedEnemyTime = 0F

  def isLost = health <= 0

  //DEBUGGING METHOD FOR NOW
  def start() = {
    val farm = new Building(FarmImage, (5,5), DefaultBuildingPrice)
    val tower = new Tower(TowerImage, (6,5), DefaultBuildingPrice)
    grid.grid(5)(5) = farm
    grid.grid(6)(5) = tower
    buildableBuildings = Vector(Building(farm, (0, 0)), Building(tower, (0,0)))
    builtBuildings = Vector(farm, tower)
    builtBuildings.foreach(_.isActive = true)
  }
  
  def step(dt: Float) = {
    enemies.foreach(_.step(dt))
    health -= enemies.filter(_.reachedTarget).size
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

    timePassed += dt
  }

  //Returns all the tiles and enemies of the game in a Vector
  //TODO: store the "canvas space" coordinates somewhere so that this method won't have to calculate them every frame
  def getDrawables: Vector[(Image, Int, Int)] = {
    //Converts a 2D tile array to Vector[Image, Int, Int)], where the Ints are X and Y coords of the image
    def tileArrayToVector(array: Array[Array[towerdefense.Tile]]) = {
      val buffer = Buffer[(Image, Int, Int)]()
      array.flatten.foreach(tile => buffer += ((tile.image, tile.coords._1 * TileSize, tile.coords._2 * TileSize)))
      buffer.toVector
    }

    //Converts Vector[Enemy] into Vector[(Image, Int, Int)], where the Ints are X and Y coords of the image
    def enemyVectorToImageVector(vector: Vector[Enemy]) = {
      val buffer = Buffer[(Image, Int, Int)]()
      vector.foreach(enemy => buffer += ((enemy.image, (enemy.coords._1 * TileSize + EnemySize / 2).round, (enemy.coords._2 * TileSize + EnemySize / 2).round)))
      buffer.toVector
    }

    tileArrayToVector(grid.grid) ++ enemyVectorToImageVector(enemies)
  }

  //Activates the next enemy from the queue, adds it to the active enemies Vector, and updates the last spawned time
  def spawnEnemyFromQueue = {
      queuedEnemies(0).isActive = true
      enemies = enemies :+ queuedEnemies(0)
      queuedEnemies = queuedEnemies.tail
      lastSpawnedEnemyTime = timePassed
  }

  //Loads a wave of enemies into the spawn queue. 
  def spawnWave(wave: Wave) = {
    wave.enemies.foreach( x =>
    for (i <- 0 until x._2) {
      queuedEnemies = queuedEnemies :+ Enemy(x._1)
    })
  }
  
  //Builds a given building in the given coordinates and activates it. Updates the resources, grid and list of built buildings.
  def buildBuilding(building: Building, coords: (Int, Int)) = {
    val newBuilding = Building(building, coords)
    resX -= newBuilding.price._1
    resY -= newBuilding.price._2
    grid.grid(coords._1)(coords._2) = newBuilding
    builtBuildings = builtBuildings :+ newBuilding
    newBuilding.isActive = true
  }

}

