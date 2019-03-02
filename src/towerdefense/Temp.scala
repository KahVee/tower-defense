package towerdefense

//Testing and debugging methods
object Temp {
  
  //REMOVE LATER
  var grid: Grid = null
  def makeGrid = {
    val x = 10
    val y = 10
    var entry: Tile = null
    var exit: Tile = null
    val array = Array.ofDim[Tile](x, y)
    for (i <- 0 until y) {
      for (j <- 0 until x) {

        var newTile = new Tile(DefaultImage, (i, j))

        i match {
          case 3 if (j < 4)                      => newTile = new TraversableTile(PathImage, (i, j))
          case x if (x >= 3 && x <= 8 && j == 4) => newTile = new TraversableTile(PathImage, (i, j))
          case 8 if (j > 4)                      => newTile = new TraversableTile(PathImage, (i, j))
          case _                                 => ()
        }

        if(i == 3 && j == 0) entry = newTile
        if(i == 8 && j == 9) exit = newTile
        array(i)(j) = newTile
      }
    }
    grid = new Grid(array, entry, exit)
    grid
  }
  
  def makeEnemy = {
    new Enemy(EnemyImage, (3, -1), grid)
  }
  
  def makeWave = {
    new Wave(Vector((makeEnemy, 5)), 3)
  }
}