import java.io.FileInputStream
import scalafx.scene.image._

package object towerdefense {

  val Tickrate = 40L
  val TileSize = 64

  val EnemySize = 32
  val DefaultEnemySpeed = 2
  val DefaultEnemyHealth = 10

  //TEMPORARY
  val DefaultImage = new Image(new FileInputStream("pics/grass.png"), TileSize, TileSize, true, false)
  val PathImage = new Image(new FileInputStream("pics/path.png"), TileSize, TileSize, true, false)
  val EntryImage = new Image(new FileInputStream("pics/entry.png"), TileSize, TileSize, true, false)
  val ExitImage = new Image(new FileInputStream("pics/exit.png"), TileSize, TileSize, true, false)
  val EnemyImage = new Image(new FileInputStream("pics/enemy.png"), EnemySize, EnemySize, true, false)
  
}