package fileparser

import java.io._
import towerdefense._
import scala.collection.mutable.Buffer
import scalafx.scene.image._

class FileParser {

  def loadLevel(filepath: String): Game = {
    /*
    val grid = new Grid()
    val game = new Game()
*/

    //METADATA
    var name = DefaultLevelName
    var startingResources = DefaultStartingResources
    var difficulty = DefaultDifficulty
    var size = DefaultLevelSize
    var health = DefaultPlayerHealth

    //TILES
    val tiles = Buffer[Tile]()
    var entryTile: Option[Tile] = None
    var exitTile: Option[Tile] = None

    //MAP
    //idGrid is a 2d array of ids, grid is the game grid itself
    val idGrid = Buffer[Array[Int]]()
    var grid: Option[Grid] = None

    //ENEMIES
    val enemies = Buffer[Enemy]()
    val waves = Buffer[Wave]()

    val fileReader = try {
      new FileReader(new File(filepath))
    } catch {
      case e: FileNotFoundException =>
        throw new MapFileException("File not found")
    }

    //BufferedReader includes readLine method
    val lineReader = new BufferedReader(fileReader)

    //Main block of the reading
    try {

      //Reads the header ("tode 1.0 map file")
      var rawLine = lineReader.readLine()
      var curLine = rawLine.trim.toLowerCase

      //Checks the header matches the current version of the reader
      if (!curLine.startsWith("tode 1.0 map file")) {
        throw new MapFileException("Unknown file type")
      }

      //Conditions that need to be met in order for the loading to be completed
      var metadataRead = false
      var tilesRead = false
      var mapRead = false
      var enemiesRead = false
      var wavesRead = false
      var endReached = false

      //Main loading loop
      while (!endReached) {
        readNextLine()

        curLine match {
          case curLine if curLine.startsWith("metadata") => readMetadata()
          case curLine if curLine.startsWith("tiles")    => readTileData()
          case curLine if curLine.startsWith("map")      => readMapData()
          case curLine if curLine.startsWith("enemies")  => readEnemyData()
          case curLine if curLine.startsWith("waves")    => readWaveData()
          case curLine if curLine.startsWith("end")      => endReached = true
        }
      }

      if (!(metadataRead && tilesRead && mapRead && enemiesRead && wavesRead))
        throw new MapFileException("Missing data block")

      //Reads the next line, and repeats, if there is only whitespace
      def readNextLine(): Unit = {
        rawLine = lineReader.readLine
        curLine = rawLine.trim.toLowerCase
        if (curLine == "" || curLine.startsWith("//")) readNextLine()
      }

      //Splits a string at every colon, and takes the second result
      def split(string: String) = {
        string.split(':')(1).trim
      }

      //Returns an image from maps folder with "name"
      def loadImage(name: String, imageType: String) = {
        val imageSize = if (imageType == "tile") TileSize else EnemySize
        try {
          new Image(new FileInputStream("./pics/" + name), imageSize, imageSize, true, false)
        } catch {
          case e: FileNotFoundException =>
            throw new MapFileException(s"Error finding $name")
        }
      }

      //creates a final Grid object for the game
      def createGrid() = {
        val arr = Array.ofDim[Tile](size._1, size._2)
        val transposedIdGrid = idGrid.transpose
        for (
          i <- 0 until size._1;
          j <- 0 until size._2
        ) {
          arr(i)(j) = tiles(transposedIdGrid(i)(j)).clone(i, j)
          if (tiles(transposedIdGrid(i)(j)) == entryTile.getOrElse(throw new MapFileException("Error readimg map file")))
            entryTile = Some(arr(i)(j))

          else if (tiles(transposedIdGrid(i)(j)) == exitTile.getOrElse(throw new MapFileException("Error readimg map file")))
            exitTile = Some(arr(i)(j))
        }

        //val arr = idGrid.map(x => x.map(y => tiles(y).clone(idGrid.indexOf(x), x.indexOf(y)))).toArray
        grid = Some(new Grid(arr, entryTile.getOrElse(throw new MapFileException("Error readimg map file")), exitTile.getOrElse(throw new MapFileException("Error readimg map file"))))
      }

      //Reads the METADATA block until ENDMETADATA is reached
      def readMetadata(): Unit = {

        readNextLine()

        curLine match {
          case curLine if curLine.startsWith("name")           => readName()
          case curLine if curLine.startsWith("startresources") => readResources()
          case curLine if curLine.startsWith("difficulty")     => readDifficulty()
          case curLine if curLine.startsWith("size")           => readSize()
          case curLine if curLine.startsWith("endmetadata")    => metadataRead = true
        }

        //All the helper functions call the main function recursively, until "ENDMETADATA" is reached

        //Reads the name from a line "Name: [name]"
        def readName() = {
          name = split(rawLine)
        }

        //Reads the starting resources from a line "StartResources: X20 Y20"
        def readResources() = {
          val rawResources = split(curLine).split(' ').map(_.replaceAll("\\D", "").toInt)
          startingResources = (rawResources(0), rawResources(1))
        }

        //Reads the difficulty from a line "Difficulty: 3"
        def readDifficulty() = {
          difficulty = split(curLine).toInt
        }

        //Reads the level size from a line "Size: 10x10"
        def readSize() = {
          val rawSize = split(curLine).split('x')
          size = (rawSize(0).toInt, rawSize(1).toInt)
        }

        if (!metadataRead) readMetadata()
      }

      //Reads the TILES block until ENDTILES is reached
      def readTileData(): Unit = {

        readNextLine()

        //Tile data
        var name = DefaultTileName
        var image = DefaultImage
        var tileType: Option[String] = None
        var id: Option[Int] = None

        var price = DefaultBuildingPrice
        var damage = DefaultTowerDamage
        var reload = DefaultReload
        var range = DefaultRange

        curLine match {
          case curLine if curLine.startsWith("tile")     => nextTile()
          case curLine if curLine.startsWith("endtiles") => tilesRead = true

        }

        //Creates a tile based on the parameters read and adds it to the tiles Buffer
        def nextTile() = {
          matchTileProperties()
          if (tileType.isEmpty || id.isEmpty) {
            throw new MapFileException("Error reading tile data")
          } else {
            val tile = tileType.get
            tile match {
              case "empty" => createEmptyTile()
              case tile if tile.startsWith("entry") =>
                createPath(findDirection(tile.split('-')(1)), None); entryTile = Some(tiles.last)
              case tile if tile.startsWith("exit") =>
                createPath(None, findDirection(tile.split('-')(1))); exitTile = Some(tiles.last)
              case "path"     => createPath(None, None)
              case "tower"    => createTower()
              case "building" => createBuilding()
              case _          => throw new MapFileException("Invalid tile type")
            }
          }

          //TODO: "center" => dir(0, 0)
          def findDirection(input: String) = {
            input match {
              case input if input.startsWith("up")     => Some(Up)
              case input if input.startsWith("down")   => Some(Down)
              case input if input.startsWith("left")   => Some(Left)
              case input if input.startsWith("right")  => Some(Right)
              case input if input.startsWith("center") => Some(Identity)
              case _                                   => throw new MapFileException("Couldn't determine entry/exit direction")
            }
          }

          def createEmptyTile() = tiles += new Tile(name, image, (0, 0))
          def createPath(entryDir: Option[Direction], exitDir: Option[Direction]) = tiles += new TraversableTile(name, image, (0, 0), entryDir, exitDir)
          def createTower() = tiles += new Tower(name, image, (0, 0), price, damage, reload, range)
          def createBuilding() = tiles += new Building(name, image, (0, 0), price)
        }

        //When a new TILE: block is reached, loop this method until the next one
        def matchTileProperties(): Unit = {
          readNextLine()
          curLine match {
            case curLine if curLine.startsWith("id")      => id = Some(split(curLine).toInt)
            case curLine if curLine.startsWith("pic")     => image = loadImage(split(curLine), "tile")
            case curLine if curLine.startsWith("type")    => tileType = Some(split(curLine))
            case curLine if curLine.startsWith("price")   => price = readPrice
            case curLine if curLine.startsWith("damage")  => damage = split(curLine).toInt
            case curLine if curLine.startsWith("reload")  => reload = split(curLine).toInt
            case curLine if curLine.startsWith("range")   => range = split(curLine).toInt
            case curLine if curLine.startsWith("endtile") => ()
          }
          if (!curLine.startsWith("endtile")) matchTileProperties()
        }

        //Reads the price, formatted as "X20 Y20"
        def readPrice = {
          val rawPrice = split(curLine).split(' ').map(_.replaceAll("\\D", "").toInt)
          (rawPrice(0), rawPrice(1))
        }

        //Loop this method recursively until ENDTILE reached. After that, if MAP is also read, create a Grid
        if (!tilesRead) readTileData() else if (mapRead) createGrid()
      } //readTileData()

      //Reads the MAP block recursively until ENDMAP is reached, stores the IDs of every tile into a 2D-collection
      def readMapData(): Unit = {

        readNextLine()

        if (!curLine.startsWith("endmap")) {
          idGrid += curLine.split(' ').map(_.toInt)
          readMapData()
        } else {
          mapRead = true
          if (tilesRead) createGrid()
        }
      }

      //Reads the ENEMIES block until ENDENEMIES is reached,
      def readEnemyData(): Unit = {

        readNextLine()

        var name = DefaultEnemyName
        var speed = DefaultEnemySpeed
        var health = DefaultEnemyHealth
        var image = EnemyImage

        curLine match {
          case curLine if curLine.startsWith("enemy") =>
            name = split(curLine); nextEnemy()
          case curLine if curLine.startsWith("endenemies") => enemiesRead = true
        }

        //Creates a new enemy object based on information found in the file
        //Note that both TILES and MAP need to be read before ENEMIES (Enemy class needs a Grid as a parameter)
        def nextEnemy(): Unit = {
          if (!tilesRead || !mapRead)
            throw new MapFileException("Incorrect placement of ENEMIES block")

          readNextLine()

          curLine match {
            case curLine if curLine.startsWith("speed")    => speed = split(curLine).toInt
            case curLine if curLine.startsWith("health")   => health = split(curLine).toInt
            case curLine if curLine.startsWith("pic")      => image = loadImage(split(curLine), "enemy")
            case curLine if curLine.startsWith("endenemy") => ()
          }

          if (!curLine.startsWith("endenemy")) nextEnemy() else enemies += new Enemy(name, image, speed, health, (0, 0), grid.getOrElse(throw new MapFileException("Error reading the map file: map data not found")))
        }

        if (!enemiesRead) readEnemyData()
      }

      //Reads the WAVES block until ENDWAVES is reached
      def readWaveData(): Unit = {

        readNextLine()

        val enemiesWithAmounts = Buffer[(Enemy, Int)]()
        var time = 0;

        curLine match {
          case curLine if curLine.startsWith("wave") =>
            time = split(curLine).toInt; nextWave()
          case curLine if curLine.startsWith("endwaves") => wavesRead = true
        }

        def nextWave(): Unit = {

          readNextLine()
          if (!curLine.startsWith("endwave")) {
            val line = curLine.split(' ')
            val amount = line(0).toInt
            val enemy = enemies.find(_.name == line(1)).getOrElse(throw new MapFileException("Invalid enemy name"))
            enemiesWithAmounts += ((enemy, amount))
            nextWave()
          }
        }
        waves += new Wave(enemiesWithAmounts.toVector, time)
        if (!wavesRead) readWaveData()
      }

      //Returns a game
      new Game(name, grid.get, startingResources._1, startingResources._2, tiles.filter(_.isInstanceOf[Building]).map(_.asInstanceOf[Building]).toVector, waves.toVector, health)

    } catch {
      case e: IOException =>
        throw new MapFileException("Error reading the map file")

      case e: NumberFormatException =>
        throw new MapFileException("Error reading the map file")

      case e: MatchError =>
        throw new MapFileException("Error reading the map file")

    }
  }
}



