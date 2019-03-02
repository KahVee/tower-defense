package towerdefense

import scala.collection.mutable.Buffer
import scalafx.scene.image._

class Game(private val grid: Grid, var resX: Int, var rexY: Int, var buildableBuildings: Vector[Building], var enemies: Vector[Enemy], private var waves: Vector[Wave], var health: Int) {

  private var timePassed = 0F

  private var builtBuildings = Vector[Building]()

  private var outOfHealth = health <= 0
  def isLost = outOfHealth

  def step(dt: Float) = {
    enemies.foreach(_.step(dt))
    health -= enemies.filter(_.reachedTarget).size
    enemies = enemies.filterNot(_.reachedTarget)
    if (!waves.isEmpty) {
      if (timePassed > waves(0).time) {
        spawnWave(grid.entryTile, waves(0))
        waves = waves.tail
      }
    }

    //DEBUG if (!enemies.isEmpty) enemies.foreach(x => println(x.coords))

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

  //TODO: Make the wave spawning work on a timer
  def spawnWave(tile: Tile, wave: Wave) = {
    enemies = enemies :+ Enemy(wave.enemies(0)._1)
    enemies.foreach(_.isActive = true)
  }
}

