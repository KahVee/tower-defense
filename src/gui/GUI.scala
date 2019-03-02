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

  val time = new Time
  var dt = time.deltaTime

  val timer = new AnimationTimer {
    override def handle(now: Long) = update()
  }

  var game: Game = null

  private val canvas = new Canvas(TileSize * 10, TileSize * 10)
  private val gc = canvas.graphicsContext2D

  private var drawables = Vector[(Image, Int, Int)]()

  //Main game loop starting
  start()
  def start() = {
    game = new Game(Temp.makeGrid, 10, 10, Vector(), Vector(), Vector(Temp.makeWave), 10)
    stage = new JFXApp.PrimaryStage {
      title.value = "Tower Defense"
      width = TileSize * 10
      height = TileSize * 10
      resizable = false
      onCloseRequest = e => quit()
    }
    updateScene(List(canvas))
    timer.start
    println("started")
  }

  //Main loop, gets called by AnimationTimer
  def update() = {
    dt = time.deltaTime
    game.step(dt)
    drawables = game.getDrawables

    updateScene(List(canvas, fpsText))
  }

  //Application exit method, gets called when main window is closed
  def quit() = {
    timer.stop()
  }

  //This method updates the scene, which gets drawn automatically. The parameter is a list of the contents for the scene
  def updateScene(list: List[Node]) = {
    stage.scene = new Scene {
      //Other content can be added to the List
      content = list
      drawables.foreach(x => gc.drawImage(x._1, x._2, x._3))
    }
  }

  private def fpsText = new Text {
    text = "FPS: " + time.fps.round
    y = 10
  }
}
