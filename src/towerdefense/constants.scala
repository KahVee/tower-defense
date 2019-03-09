import java.io.FileInputStream
import scalafx.scene.image._

package object towerdefense {

  val Tickrate = 40L
  val TileSize = 64
  val TileGridLinesVisible = false

  val EnemySize = 32
  val DefaultEnemySpeed = 1
  val DefaultEnemyHealth = 10
  val EnemySpawnInterval = 0.5
  
  val DefaultBuildingPrice = (3, 3)
  val DefaultTowerDamage = 10
  val DefaultShootingSpeed = 1
  val DefaultTowerRange = 3

  //TEMPORARY
  val DefaultImage = new Image(new FileInputStream("pics/grass.png"), TileSize, TileSize, true, false)
  val PathImage = new Image(new FileInputStream("pics/path.png"), TileSize, TileSize, true, false)
  val EntryImage = new Image(new FileInputStream("pics/entry.png"), TileSize, TileSize, true, false)
  val ExitImage = new Image(new FileInputStream("pics/exit.png"), TileSize, TileSize, true, false)
  val FarmImage = new Image(new FileInputStream("pics/farm.png"), TileSize, TileSize, true, false)
  val TowerImage = new Image(new FileInputStream("pics/tower.png"), TileSize, TileSize, true, false)
  val EnemyImage = new Image(new FileInputStream("pics/enemy.png"), EnemySize, EnemySize, true, false)  
}