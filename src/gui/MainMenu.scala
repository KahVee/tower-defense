package gui

import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.scene.canvas._
import towerdefense._
import scalafx.scene.paint._
import scalafx.scene.paint.Color._
import scalafx.scene.image._
import scalafx.scene.SnapshotParameters

class MainMenu(val xSize: Int, val ySize: Int) {

  var selectedGame: Option[Game] = None
  val canvas = new Canvas {
    width = MainMenuSize
    height = MainMenuSize - MainMenuMenuBarHeight - MainMenuButtonHeight
  }
  val gc = canvas.graphicsContext2D

  lazy val mapList: ComboBox[String] = {
    new ComboBox(GUI.games.map(_.name)) {
      prefWidth = 2 * MainMenuSize / 3
      prefHeight = MainMenuButtonHeight
      value = "Choose Map"

      onAction = event => {
        selectedGame = GUI.games.find(_.name.equals(mapList.value.apply))
        updateCanvas()
      }
    }
  }

  val scene = new Scene(xSize, ySize) {
    fill = new LinearGradient(
      endX = 0,
      stops = Stops(color(0.23, 0.23, 0.27), color(0.13, 0.13, 0.15)))

    content = new VBox {

      children = List(
        new MenuBar {
          prefWidth = MainMenuSize
          prefHeight = MainMenuMenuBarHeight

          val fileMenu = new Menu("File")
          val exitItem = new MenuItem("Exit") {
            onAction = event => {
              System.exit(1)
            }
          }

          fileMenu.items = List(exitItem)
          menus = List(fileMenu)
        },
        canvas,
        new HBox {
          children = List(
            mapList,
            mainMenuButton("Play", playButtonAction))
        })

    }
  }

  //returns a generic Button for the menu
  def mainMenuButton(text: String, onEvent: () => Unit) = {
    new Button(text) {
      prefWidth = MainMenuSize / 6 
      prefHeight = MainMenuButtonHeight
      onAction = event => {
        onEvent()
      }
    }
  }

  //Starts the game on the selected map
  def playButtonAction() = {
    if (selectedGame.isDefined) {
      GUI.startGame(selectedGame.get)
    }
  }

  //Updates the preview canvas
  def updateCanvas() = {
    gc.clearRect(0, 0, MainMenuSize, MainMenuSize - MainMenuMenuBarHeight - MainMenuButtonHeight)
    val tileSize = Math.min(MainMenuSize / selectedGame.get.grid.grid.size, (MainMenuSize - MainMenuMenuBarHeight - MainMenuButtonHeight) / selectedGame.get.grid.grid(0).size)
    
    //If a game is selected, draws its tiles scaled appropriately
    if (selectedGame.isDefined) {
      val tiles = GUI.rawTileDrawables(selectedGame.get)
      
      val fullWidth = tileSize * selectedGame.get.grid.grid.size      
      val horizontalOffset = (MainMenuSize - fullWidth) / 2
      
      val fullHeight = tileSize * selectedGame.get.grid.grid(0).size      
      val verticalOffset = (MainMenuSize - MainMenuMenuBarHeight - MainMenuButtonHeight - fullHeight) / 2
      
      tiles.foreach(x => gc.drawImage(scaleImage(x._1), x._2 * tileSize + horizontalOffset, x._3 * tileSize + verticalOffset))
    }

    def scaleImage(image: Image) = {
      val iv = new ImageView(image)
      iv.preserveRatio = true
      iv.setFitWidth(tileSize)
      iv.snapshot(new SnapshotParameters { fill = Color.Transparent }, new WritableImage(tileSize, tileSize))
    }
  }
}