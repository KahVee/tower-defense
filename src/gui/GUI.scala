package gui

import scalafx.application.JFXApp
import scalafx.Includes._
import scalafx.scene.image._
import scalafx.scene._
import scalafx.animation._
import scalafx.scene.paint.Color._
import scalafx.scene.shape._
import javafx.animation.AnimationTimer
import scalafx.scene.text.Text
import scalafx.scene.canvas._
import towerdefense._

object GUI extends JFXApp {

  private var tick = 0
  private var dt = 0.0

  /*private val timer = new java.util.Timer()
  private val updateTask = new java.util.TimerTask {
    def run() = {
      dt = deltaTime
      update()
    }
  }*/
  val timer = new AnimationTimer {
    override def handle(now: Long) = update()
  }

  var game: Game = null
  private var tiles: Vector[(Image, Int, Int)] = null
  private val canvas = new Canvas(TileWidth * 10 + 6, TileWidth * 10 + 100)
  private val gc = canvas.graphicsContext2D
  private def fpsText = new Text {
    text = "FPS: " + (1.0 / dt).toInt
    y = 10
  }
  start()

  //Main game loop starting
  def start() = {
    game = new Game(new Grid(Temp.makeGrid), 10, 10, Vector(), Vector(), Vector(), 10)
    tiles = tileArrayToVector(game.grid.grid)

    stage = new JFXApp.PrimaryStage {
      title.value = "Tower Defense"
      width = TileWidth * 10 + 6
      height = TileWidth * 10 + 100
      resizable = false
      onCloseRequest = e => quit()
      scene = new Scene {
        content = List(canvas, fpsText)
        tiles.foreach(x => gc.drawImage(x._1, x._2, x._3))
      }
    }
    timer.start
    //timer.scheduleAtFixedRate(updateTask, 1000 / Tickrate, 1000 / Tickrate)
    println("started")
  }

  //Main loop, gets called constants.Tickrate times a second
  def update() = {
    dt = deltaTime
    if (tick % 60 == 0) {
      stage.scene = new Scene {
        content = List(canvas, fpsText)
        tiles.foreach(x => gc.drawImage(x._1, x._2, x._3))
      }
    }
    tick += 1
  }

  //Application exit method, gets called when main window is closed
  def quit() = {
    timer.stop()
  }

  //Calculates time difference since last frame as seconds to be used as dt
  private var lastTime = System.nanoTime
  private def deltaTime = {
    val out = 0.000000001 * (System.nanoTime - lastTime)
    lastTime = System.nanoTime
    out
  }

  //Converts a 2D tile array to Vector[ImageView]
  def tileArrayToVector(array: Array[Array[towerdefense.Tile]]) = {
    var vector = Vector[(Image, Int, Int)]()
    for (i <- 0 until array.length) {
      for (j <- 0 until array(0).length) {
        vector = vector :+ (array(i)(j).pic, (i * TileWidth), (j * TileWidth))
      }
    }
    vector
  }
}
