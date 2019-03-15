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
import scalafx.scene.control._
import scalafx.scene.paint.{ Stops, LinearGradient }
import scalafx.scene.layout._
import scalafx.scene.effect.DropShadow
import scalafx.geometry.Insets
import scalafx.scene.effect._

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

  //Full contents of the window: Canvas, button grid...
  private val centerContentList: List[Node] = List(canvas, ButtonGrid.makeGrid(10, 10))

  //Main game loop starting
  start()
  def start() = {
    game = new Game(Temp.makeGrid, 10, 10, Vector(), Vector(), Vector(Temp.makeWave(0), Temp.makeWave(6)), 10)
    game.start()

    stage = new JFXApp.PrimaryStage {
      title.value = "Tower Defense"
      width = TileSize * 10 + 80
      height = TileSize * 10 + 100
      resizable = false
      onCloseRequest = e => quit()

      //The whole scene of the window
      scene = new Scene {

        //Background of the window
        fill = new LinearGradient(
          endX = 0,
          stops = Stops(color(0.23, 0.23, 0.27), color(0.13, 0.13, 0.15)))

        //Window is split into top, bottom, left, center and right using BorderPane
        content = new BorderPane {

          //Center contains the canvas and button grid
          center = new Group(centerContentList: _*)

          //Bottom contains the  pause button
          bottom = new HBox {
            children = pauseButton
          }

          //Left contains a vertical stack of building buttons
          left = new VBox {
            padding = new javafx.geometry.Insets(SideBarPadding)
            spacing = SideBarPadding
            children = for (i <- 0 to 7) yield sidebarButton(PathImage)
          }
        }
      }
    }

    updateScene()
    timer.start
    println("started")
  }

  //Main loop, gets called by AnimationTimer
  def update(): Unit = {
    if (!game.isLost) {
      dt = time.deltaTime
      if (!isPaused) {
        game.step(dt)

        updateScene()
      }
    } else {
      timer.stop()
      stage.scene = losingScene
    }
  }

  //This method updates the scene, which gets drawn automatically. The parameter is a list of the contents for the scene
  def updateScene() = {
    //getDrawables returns a vector of images, each of which has its coordinates stored in the tuple's second and third slot
    game.getDrawables.foreach(x => gc.drawImage(x._1, x._2, x._3))
    gc.fillText(fpsText, 0, 10)
    gc.fillText(healthText, 0, 20)
  }

  //Application exit method, gets called when main window is closed
  def quit() = {
    timer.stop()
  }

  //Gets called when a tile is clicked. Finds out which tile was clicked
  def mouseClickOnGrid(button: Button) = {
    val x = (button.layoutX.value / TileSize).round.toInt
    val y = (button.layoutY.value / TileSize).round.toInt
    val grid = game.grid.grid
    if (x < grid.size && y < grid(0).size) {
      //TODO: ???
    }
  }

  private def fpsText = "FPS: " + time.fps.round
  private def healthText = "Health: " + game.health

  private def losingScene = {
    new Scene {
      fill = Black
      content = new Pane {
        children = List(
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

  private def pauseButton = new ToggleButton("Pause") {
    prefWidth = 100
    prefHeight = 64
    onAction = e => {
      isPaused = !isPaused
    }
  }

  //A button which only shows an image and has a glow on hover
  private def sidebarButton(image: Image) = new ToggleButton {
    
    val normalGraphic = new ImageView(image)
    val hoverGraphic = new ImageView(image) {
      effect = new ColorAdjust {
        brightness = 0.1
      }
    }    
    style = """-fx-background-color: transparent;
               -fx-padding: 0;"""
    graphic <== when(hover) choose hoverGraphic otherwise normalGraphic
    
    onAction = e => {
      //TODO: Make the button choose a tower to be built
    }
  }
}
