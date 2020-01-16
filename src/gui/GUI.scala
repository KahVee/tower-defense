package gui

import scalafx.application.JFXApp
import scalafx.Includes._
import javafx.animation.AnimationTimer
import towerdefense._
import fileparser._
import java.io.File
import scalafx.beans.property._
import scala.collection.mutable.Buffer

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
import scalafx.scene.input._

object GUI extends JFXApp {

  var activeGame: Game = null
  var games: Vector[Game] = Vector()

  val time = new Time
  var dt = time.deltaTime
  var isPaused = false

  var mainMenu: MainMenu = null

  val timer = new AnimationTimer {
    override def handle(now: Long) = update()
  }

  //Canvas that the tiles and enemies are drawn on using the graphicsContext
  private var mainCanvas = new Canvas(TileSize, TileSize)
  private var gc = mainCanvas.graphicsContext2D
  private var mouseX: Double = 0
  private var mouseY: Double = 0

  private var tiles: Vector[(Image, Int, Int)] = Vector()
  private var enemies: Vector[(Image, Int, Int)] = Vector()

  private val bottomCanvas = new Canvas(120, 64)
  private val bottomGc = bottomCanvas.graphicsContext2D

  //Full contents of the window: Canvas, button grid...
  private var centerContentVector: Vector[Node] = Vector()
  private var sidebarButtons = Vector[Button]()

  //Stores an inactive version of a building the player has selected. If the player clicks a tile and building is possible, this building will be built.
  private var selectedBuilding: Option[Building] = None

  val parser = new FileParser

  start()

  def start() = {

    //Loads all maps from the folder into games array
    try {
      val folder = new File("maps/")
      if (folder.exists && folder.isDirectory) {
        val pathList = folder.listFiles.map(_.getPath).toVector
        for (path <- pathList) {
          games = games :+ parser.loadLevel(path)
        }
      }
    } catch {
      case e: MapFileException =>
        println("ERROR: " + e.getMessage)
        println("Quitting...")
        System.exit(0)
    }

    mainMenu = new MainMenu(MainMenuSize, MainMenuSize)

    //Main menu window
    stage = new JFXApp.PrimaryStage {
      title.value = "Tower Defense"
      width = MainMenuSize
      height = MainMenuSize
      resizable = false
      onCloseRequest = e => quit()
      scene = mainMenu.scene
    }
    stage.sizeToScene()
  }

  //Main game loop starting, creates the game, window and the layout within the window
  def startGame(game: Game): Unit = {

    activeGame = game

    mainCanvas = new Canvas(TileSize * activeGame.grid.grid.size, TileSize * activeGame.grid.grid(0).size)

    gc = mainCanvas.graphicsContext2D
    centerContentVector = Vector(mainCanvas, ButtonGrid.makeGrid(activeGame.grid.grid.size, activeGame.grid.grid(0).size))

    bottomGc.setFill(White)
    bottomGc.setFont(Font.font("Calibri", 20))

    //The whole scene of the window
    stage.scene = new Scene {

      //Discards selected building and "clears" the highlight effect when mouse is clicked outside any button
      def discardSelectedBuilding() = {
        selectedBuilding = None
        //This line "removes" the highlight effect from the previously selected button
        mainCanvas.requestFocus()
      }

      onMousePressed = e => {
        discardSelectedBuilding()
      }

      onMouseEntered = e => {
        mouseX = -1
        mouseY = -1
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
          children = Vector(pauseButton, bottomCanvas)
        }

        //Left contains a vertical stack of building buttons
        left = new VBox {
          padding = new javafx.geometry.Insets(SidebarPadding)
          spacing = SidebarPadding
          children = activeGame.buildableBuildings.map(sidebarButton(_)) //for (building <- game.buildableBuildings) yield sidebarButton(building)
        }
      }
    }

    updateTileDrawables(rawTileDrawables(activeGame))
    updateScene()
    timer.start
    println("Game started")
  }

  //Main loop, gets called by AnimationTimer
  def update(): Unit = {

    if (!activeGame.isLost) {
      dt = time.deltaTime

      if (!isPaused) {
        activeGame.step(dt)
        updateScene()
      }
    } else {
      timer.stop()
      stage.scene = losingScene
    }
  }

  //This method updates the scene, which gets drawn automatically. The parameter is a list of the contents for the scene
  def updateScene() = {

    //Enemy positions need to be updated each tick, tiles only when they are changed/built
    updateEnemyDrawables(rawEnemyDrawables(activeGame))

    //These draw the tiles on the screen, with the enemies on top
    tiles.foreach(tile => gc.drawImage(tile._1, tile._2, tile._3))
    enemies.foreach(enemy => gc.drawImage(enemy._1, enemy._2, enemy._3))

    if (selectedBuilding.isDefined && mouseX + mouseY > 0 && selectedBuilding.get.isInstanceOf[Tower]) {
      drawRangeCircle()
    }

    if (DebugMode) gc.fillText(fpsString, 0, 10)
    bottomGc.clearRect(0, 0, 120, 64)
    bottomGc.fillText(healthString, 0, 15)
    bottomGc.strokeText(resourceString, 0, 40)
    bottomGc.fillText(resourceString, 0, 40)
  }

  //Both return a vector of images, each of which has its coordinates stored in the tuple's second and third slot
  def rawTileDrawables(game: Game) = game.tileDrawables
  def rawEnemyDrawables(game: Game) = game.enemyDrawables

  //Updates the Vector that stores the currently active images of tiles and their coords in screen space
  def updateTileDrawables(raw: Vector[(Image, Int, Int)]) = {
    val buffer = Buffer[(Image, Int, Int)]()
    raw.foreach(image => buffer += ((image._1, image._2 * TileSize, image._3 * TileSize)))
    tiles = buffer.toVector
  }

  def updateEnemyDrawables(raw: Vector[(Image, Float, Float)]) = {
    val buffer = Buffer[(Image, Int, Int)]()
    raw.foreach(image => buffer += ((image._1, (image._2 * TileSize + EnemySize / 2).round, (image._3 * TileSize + EnemySize / 2).round)))
    enemies = buffer.toVector
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

  def mouseMoveOnGrid(event: MouseEvent, button: Button) = {
    val coords = getButtonCoordinates(button)
    mouseX = event.getX() + TileSize * coords._1
    mouseY = event.getY() + TileSize * coords._2
  }

  def drawRangeCircle() = {
    val tower = selectedBuilding.get.asInstanceOf[Tower]
    val radius = TileSize * tower.range
    gc.strokeOval(mouseX - radius, mouseY - radius, radius * 2, radius * 2)
  }

  //Gets called when a tile is clicked. Finds out which tile was clicked
  def mouseClickOnGrid(button: Button) = {
    val grid = activeGame.grid.grid
    val coords = getButtonCoordinates(button)

    //This if-block checks if all the conditions for building to be built are checked, i.e. there is a building selected, the clicked tile is empty, and there is enough resources
    if (selectedBuilding.isDefined) {
      val building = selectedBuilding.get
      if (coords._1 < grid.size && coords._2 < grid(0).size) {
        if (grid(coords._1)(coords._2).buildable) {
          if (activeGame.resX >= building.price._1 && activeGame.resY >= building.price._2) {
            activeGame.buildBuilding(building, coords)
            updateTileDrawables(rawTileDrawables(activeGame))
          }
        }
      }
    }
    updateSelectedBuilding(None)
  }

  private def fpsString = "FPS: " + time.fps.round
  private def healthString = "Health: " + activeGame.health
  private def resourceString = "X: " + activeGame.resX + "\nY: " + activeGame.resY

  //Scene that replaces the game when it's over
  //TODO: Fix sizeToScene
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

      tooltip = new Tooltip(building.tooltip) {
        autoHide = false
      }

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