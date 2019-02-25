package towerdefense

object Temp {

  //REMOVE LATER
  def makeGrid = {
    val x = 10
    val y = 10
    val array = Array.ofDim[Tile](x, y)
    for (i <- 0 until y) {
      for (j <- 0 until x) {

        var newTile = new Tile(DefaultImage)

        i match {
          case 3 if (j < 4)                      => newTile = new TraversableTile(PathImage)
          case x if (x >= 3 && x <= 8 && j == 4) => newTile = new TraversableTile(PathImage)
          case 8 if (j > 4)                      => newTile = new TraversableTile(PathImage)
          case _                                 => ()
        }

        array(i)(j) = newTile
      }
    }
    array
  }
}