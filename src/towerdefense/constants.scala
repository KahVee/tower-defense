import java.io.FileInputStream
import scalafx.scene.image._

package object towerdefense {
  val Tickrate = 40L
  val TileWidth = 64
  
  //TEMPORARY
  val DefaultImage = new Image(new FileInputStream("D:\\Common\\School\\Programming\\ScalaKansio\\Tornipuolustus\\pics\\grass.png"), TileWidth, TileWidth, true, false)
  val PathImage = new Image(new FileInputStream("D:\\Common\\School\\Programming\\ScalaKansio\\Tornipuolustus\\pics\\path.png"), TileWidth, TileWidth, true, false)
}