import java.io.FileInputStream
import scalafx.scene.image._

package object towerdefense {

  //GENERAL
  val Tickrate = 40L
  val TileSize = 64
  val TileGridLinesVisible = false
  val SidebarPadding = 2
  val BottomPadding = 10

  //ENEMIES
  val EnemySize = TileSize / 2
  val DefaultEnemySpeed = 2
  val DefaultEnemyHealth = 2
  val EnemySpawnInterval = 0.5
  
  //BUILDINGS
  val DefaultTileName = "Tile"
  val DefaultBuildingPrice = (3, 3)
  val DefaultTowerDamage = 1
  val DefaultReload = 1F
  val DefaultRange = 3
  
  //LEVEL
  val DefaultLevelName = "Map"
  val DefaultStartingResources = (20, 20)
  val DefaultDifficulty = 3
  val DefaultLevelSize = (10, 10)
  

  //TEMPORARY
  val DefaultImage = new Image(new FileInputStream("pics/grass.png"), TileSize, TileSize, true, false)
  val PathImage = new Image(new FileInputStream("pics/path.png"), TileSize, TileSize, true, false)
  val EntryImage = new Image(new FileInputStream("pics/entry.png"), TileSize, TileSize, true, false)
  val ExitImage = new Image(new FileInputStream("pics/exit.png"), TileSize, TileSize, true, false)
  val FarmImage = new Image(new FileInputStream("pics/farm.png"), TileSize, TileSize, true, false)
  val TowerImage = new Image(new FileInputStream("pics/tower.png"), TileSize, TileSize, true, false)
  val EnemyImage = new Image(new FileInputStream("pics/enemy.png"), EnemySize, EnemySize, true, false)  
}