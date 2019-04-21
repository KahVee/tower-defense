package gui

import scalafx.scene.layout.GridPane
import scalafx.scene.control.Button
import scalafx.Includes._

import towerdefense._

// Helper object for GUI containing some methods regarding the grid of buttons that provides the tiles with functionality
object ButtonGrid {

  //Makes a grid of buttons of dimensions x and y
  def makeGrid(x: Int, y: Int) = {
    new GridPane {

      for (
        j <- 0 until y;
        i <- 0 until x
      ) {

        //Drawing the button names drops fps by over 60%
        add(new Button(if (DebugMode) s"($i, $j)" else "") {
          prefWidth = TileSize
          prefHeight = TileSize

          gridLinesVisible = TileGridLinesVisible

          //style = "-fx-padding: 3 30 3 30;"

          //Makes a "highlight" on the tile the mouse is currently hovering on
          style <== when(hover) choose "-fx-background-color: rgba(0, 0, 0, 0.05);" otherwise "-fx-background-color: rgba(0, 0, 0, 0);"

          onAction = e => {
            GUI.mouseClickOnGrid(this)
          }
        }, i, j)
      }
    }
  }
}