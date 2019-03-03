package towerdefense

import scala.collection.mutable.Buffer
import scalafx.scene.image._

class Game(private val grid: Grid, var resX: Int, var rexY: Int, var buildableBuildings: Vector[Building], var enemies: Vector[Enemy], private var waves: Vector[Wave], var health: Int) {

  private var timePassed = 0F

  private var builtBuildings = Vector[Building]()
  private var queuedEnemies = Vector[Enemy]()
  private var lastSpawnedEnemyTime = 0F

  def isLost = health <= 0

  def step(dt: Float) = {
    enemies.foreach(_.step(dt))
    health -= enemies.filter(_.reachedTarget).size
    enemies = enemies.filterNot(_.reachedTarget)

    if (!waves.isEmpty) {
      if (timePassed > waves(0).time) {
        spawnWave(waves(0))
        waves = waves.tail
      }
    }

    //If there are enemies in the spawn queue, spawn a new one when the appropriate time has passed
    if (!queuedEnemies.isEmpty && timePassed > lastSpawnedEnemyTime + EnemySpawnInterval) spawnEnemyFromQueue

    // DEBUG if (!enemies.isEmpty) enemies.foreach(x => println(x.coords))

    timePassed += dt
  }

  //Returns all the tiles and enemies of the game in a Vector
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
}

