package gui

import scalafx.application.JFXApp
import scalafx.Includes._
import javafx.animation.AnimationTimer
import towerdefense._

import scalafx.scene.image._
import scalafx.scene._
import scalafx.scene.paint.Color._
import scalafx.scene.shape._
import scalafx.scene.text._
import scalafx.scene.canvas._
import scalafx.scene.control.Button
import scalafx.scene.paint.{ Stops, LinearGradient }
import scalafx.scene.layout.Pane
import scalafx.scene.effect.DropShadow



object GUI extends JFXApp {

  val time = new Time
  var dt = time.deltaTime
  val timer = new AnimationTimer {
    override def handle(now: Long) = update()
  }
  var isPaused = false

  var game: Game = null

  //Canvas that the tiles and enemies are drawn on using the graphicsContext
  private val canvas = new Canvas(TileSize * 10, TileSize * 10)
  private val gc = canvas.graphicsContext2D

  //All tiles, enemies and other objects that have an image and are drawn on the canvas.
  //The numbers in the tuple represent the coordinates of the image in "grid space"
  private var drawables = Vector[(Image, Int, Int)]()

  //Full contents of the window: Canvas, buttons, etc
  private val contentList: List[Node] = List(canvas, pauseButton, ButtonGrid.makeGrid(10, 10))

  //Main game loop starting
  start()
  def start() = {
    game = new Game(Temp.makeGrid, 10, 10, Vector(), Vector(), Vector(Temp.makeWave(0), Temp.makeWave(6)), 10)
    game.start()

    stage = new JFXApp.PrimaryStage {
      title.value = "Tower Defense"
      width = TileSize * 10
      height = TileSize * 10 + 100
      resizable = false
      onCloseRequest = e => quit()
      scene = new Scene {
        content = contentList
      }
    }

    updateScene(contentList)
    timer.start
    println("started")
  }

  //Main loop, gets called by AnimationTimer
  def update(): Unit = {
    if (!game.isLost) {
      dt = time.deltaTime
      if (!isPaused) {
        game.step(dt)
        drawables = game.getDrawables

        updateScene(contentList)
      }
    } else {
      timer.stop()
      stage.scene = losingScene
    }
  }

  //This method updates the scene, which gets drawn automatically. The parameter is a list of the contents for the scene
  def updateScene(list: List[Node]) = {
    drawables.foreach(x => gc.drawImage(x._1, x._2, x._3))
    gc.fillText(fpsText, 0, 10)
    gc.fillText(healthText, 0, 20)
  }

  //Application exit method, gets called when main window is closed
  def quit() = {
    timer.stop()
  }

  private def fpsText = "FPS: " + time.fps.round
  private def healthText = "Health: " + game.health

  private def losingScene = {
    new Scene {
      fill = Black
      content = new Pane {
        children = Seq(
          new Text {
            text = "GAME OVER"
            style = "-fx-font-size: 36pt"
            x = (stage.width.value / 2) - 140
            y = 100
            textAlignment = TextAlignment.Center
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(Crimson, OrangeRed))
            effect = new DropShadow {
              color = Red
              radius = 30
              spread = 0.2
            }
          },
          new Button("Play Again") {
            layoutX = (stage.width.value / 2) - 80
            layoutY = 150
            prefWidth = 150
            prefHeight = 50
            onAction = e => {
              start()
            }
          })
      }
    }
  }

  private def pauseButton = new Button("Pause") {
    layoutX = 0
    layoutY = TileSize * 10
    prefWidth = 100
    prefHeight = 70
    onAction = e => {
      isPaused = !isPaused
    }
  }
}
