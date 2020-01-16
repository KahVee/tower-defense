import java.io.FileInputStream
import scalafx.scene.image._

package object towerdefense {

  //GENERAL
  val TileSize = 64
  val TileGridLinesVisible = false
  val SidebarPadding = 2
  val BottomPadding = 10
  val DefaultPlayerHealth = 10
  val DebugMode = false

  //MAIN MENU UI
  val MainMenuSize = 300
  val MainMenuButtonHeight = 30
  val MainMenuMenuBarHeight = 25

  //ENEMIES
  val EnemySize = TileSize / 2
  val DefaultEnemyName = "Enemy"
  val DefaultEnemySpeed = 2
  val DefaultEnemyHealth = 2
  val EnemySpawnInterval = 0.5
  val DefaultEnemyKillReward = (2, 0)

  //BUILDINGS
  val DefaultTileName = "Tile"
  val DefaultBuildingPrice = (3, 3)
  val DefaultTowerDamage = 1
  val DefaultReload = 1F
  val DefaultRange = 3
  val DefaultBuildingProductionSpeed = 60
  val DefaultBuildingProductionAmount = (0, 1)

  //LEVEL
  val DefaultLevelName = "Map"
  val DefaultStartingResources = (20, 20)
  val DefaultDifficulty = 3
  val DefaultLevelSize = (10, 10)

  //IMAGES
  val DefaultImage = new Image(new FileInputStream("pics/grass.png"), TileSize, TileSize, true, false)
  val EnemyImage = new Image(new FileInputStream("pics/enemy.png"), EnemySize, EnemySize, true, false)
}