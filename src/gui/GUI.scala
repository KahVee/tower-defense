package gui

import scalafx.application.JFXApp
import scalafx.Includes._
import javafx.animation.AnimationTimer
import towerdefense._
import fileparser._
import java.io.File
import scalafx.beans.property._

import scalafx.scene.image._
import scalafx.scene._
import scalafx.scene.input.MouseButton
import scalafx.scene.paint.Color._
import scalafx.scene.shape._
import scalafx.scene.text._
import scalafx.scene.canvas._
import scalafx.scene.control._
import scalafx.scene.paint.{ Stops, LinearGradient }
import scalafx.scene.layout._
import scalafx.scene.effect._
import scalafx.scene.input.KeyCode
import javafx.beans.value.ObservableStringValue

object GUI extends JFXApp {

  var game: Game = null

  val time = new Time
  var dt = time.deltaTime
  var isPaused = false

  val timer = new AnimationTimer {
    override def handle(now: Long) = update()
  }

  //Canvas that the tiles and enemies are drawn on using the graphicsContext
  private var mainCanvas = new Canvas(TileSize, TileSize)
  private var gc = mainCanvas.graphicsContext2D

  private val bottomCanvas = new Canvas(120, 64)
  private val bottomGc = bottomCanvas.graphicsContext2D

  //Full contents of the window: Canvas, button grid...
  private var centerContentVector: Vector[Node] = Vector()
  private var sidebarButtons = Vector[Button]()

  //Stores an inactive version of a building the player has selected. If the player clicks a tile and building is possible, this building will be built.
  private var selectedBuilding: Option[Building] = None

  val parser = new FileParser

  //Main game loop starting, creates the game, window and the layout within the window
  start()
  def start() = {
    game = parser.loadLevel("maps/bigmap.map")
    mainCanvas = new Canvas(TileSize * game.grid.grid.size, TileSize * game.grid.grid(0).size)
    gc = mainCanvas.graphicsContext2D
    centerContentVector = Vector(mainCanvas, ButtonGrid.makeGrid(game.grid.grid.size, game.grid.grid(0).size))

    bottomGc.setFill(White)
    bottomGc.setFont(Font.font(20))

    stage = new JFXApp.PrimaryStage {
      title.value = "Tower Defense"
      width = TileSize * game.grid.grid.size + 80
      height = TileSize * game.grid.grid(0).size + 100
      resizable = false
      onCloseRequest = e => quit()

      //The whole scene of the window
      scene = new Scene {

        //Discards selected building and "clears" the highlight effect when mouse is clicked outside any button
        def discardSelectedBuilding() = {
          selectedBuilding = None
          //This line "removes" the highlight effect from the previously selected button
          mainCanvas.requestFocus()
        }

        onMousePressed = e => {
          discardSelectedBuilding()
        }

        //Background of the window
        fill = new LinearGradient(
          endX = 0,
          stops = Stops(color(0.23, 0.23, 0.27), color(0.13, 0.13, 0.15)))

        //Window is split into top, bottom, left, center and right using BorderPane
        content = new BorderPane {

          //Center contains the canvas and button grid
          center = new Group(centerContentVector: _*)

          //Bottom contains the pause button
          bottom = new HBox {
            spacing = BottomPadding
            //TODO: Fix the resourceText, currently it doesn't update properly
            children = Vector(pauseButton, bottomCanvas)
          }

          //Left contains a vertical stack of building buttons
          left = new VBox {
            padding = new javafx.geometry.Insets(SidebarPadding)
            spacing = SidebarPadding
            children = game.buildableBuildings.map(sidebarButton(_)) //for (building <- game.buildableBuildings) yield sidebarButton(building)
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
    if(DebugMode) gc.fillText(fpsString, 0, 10)
    bottomGc.clearRect(0, 0, 120, 64)
    bottomGc.fillText(healthString, 0, 15)
    bottomGc.strokeText(resourceString, 0, 40)
    bottomGc.fillText(resourceString, 0, 40)
  }

  //Application exit method, gets called when main window is closed
  def quit() = {
    timer.stop()
  }

  def updateSelectedBuilding(building: Option[Building]) = {
    selectedBuilding = building
  }

  //Returns the "game space" coordinates of a button
  def getButtonCoordinates(button: Button) = {
    val x = (button.layoutX.value / TileSize).round.toInt
    val y = (button.layoutY.value / TileSize).round.toInt
    (x, y)
  }

  //Gets called when a tile is clicked. Finds out which tile was clicked
  def mouseClickOnGrid(button: Button) = {
    val grid = game.grid.grid
    val coords = getButtonCoordinates(button)

    //This if-block checks if all the conditions for building to be built are checked, i.e. there is a building selected, the clicked tile is empty, and there is enough resources
    if (selectedBuilding.isDefined) {
      val building = selectedBuilding.get
      if (coords._1 < grid.size && coords._2 < grid(0).size) {
        if (grid(coords._1)(coords._2).buildable) {
          if (game.resX >= building.price._1 && game.resY >= building.price._2) {
            game.buildBuilding(building, coords)
            updateScene()
          }
        }
      }
    }
    updateSelectedBuilding(None)
  }

  private def fpsString = "FPS: " + time.fps.round
  private def healthString = "Health: " + game.health
  private def resourceString = "X: " + game.resX + "\nY: " + game.resY

  //Scene that replaces the game when it's over
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
    style = "-fx-padding: 0;"
    onAction = e => {
      isPaused = !isPaused
    }
  }

  //A button which only shows an image and has a glow on hover
  private def sidebarButton(building: Building) = {
    new ToggleButton {
      val number = sidebarButtons.size
      val normalGraphic = new ImageView(building.image) {
        effect = new ColorAdjust {
          saturation = -0.3
          brightness = -0.2
        }
      }
      val highlightGraphic = new ImageView(building.image) {
        effect = new DropShadow {
          color = White
        }
      }
      style = """-fx-background-color: transparent;
                 -fx-padding: 0;"""
      graphic <== when(focused) choose highlightGraphic otherwise normalGraphic

      onAction = e => {
        if (!focused.value) {
          updateSelectedBuilding(None)
        } else {
          updateSelectedBuilding(Some(building))
        }
      }
    }
  }
}
