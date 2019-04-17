package fileparser

import java.io._
import towerdefense._
import scala.collection.mutable.Buffer
import scalafx.scene.image._

class FileParser {

  def loadLevel(filepath: String) = {
    /*
    val grid = new Grid()
    val game = new Game()
*/

    //METADATA
    var name = DefaultLevelName
    var startingResources = DefaultStartingResources
    var difficulty = DefaultDifficulty
    var size = DefaultLevelSize

    //TILES
    val tiles = Buffer[Tile]()

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
          case curLine if curLine.startsWith("//")       => ()
          case curLine if curLine.startsWith("end")      => endReached = true
          case _                                         => throw new MapFileException("Error reading file at " + rawLine)
        }
      }

      //Reads the next line, and repeats, if there is only whitespace
      def readNextLine(): Unit = {
        rawLine = lineReader.readLine
        curLine = rawLine.trim.toLowerCase
        if (curLine == "") readNextLine()
      }

      //Splits a string at every colon, and takes the second result
      def split(string: String) = {
        string.split(':')(1).trim
      }

      //Reads the METADATA block until ENDMETADATA is reached
      def readMetadata(): Unit = {

        readNextLine()

        curLine match {
          case curLine if curLine.startsWith("name")           => readName()
          case curLine if curLine.startsWith("startresources") => readResources()
          case curLine if curLine.startsWith("difficulty")     => readDifficulty()
          case curLine if curLine.startsWith("size")           => readSize()
          case curLine if curLine.startsWith("//")             => readMetadata()
          case curLine if curLine.startsWith("endmetadata")    => metadataRead = true
        }

        //All the helper functions call the main function recursively, until "ENDMETADATA" is reached

        //Reads the name from a line "Name: [name]"
        def readName() = {
          name = split(rawLine)
          readMetadata()
        }

        //Reads the starting resources from a line "StartResources: X20 Y20"
        def readResources() = {
          val rawResources = split(curLine).split(' ').map(_.replaceAll("\\D", "").toInt)
          startingResources = (rawResources(0), rawResources(1))
          readMetadata()
        }

        //Reads the difficulty from a line "Difficulty: 3"
        def readDifficulty() = {
          difficulty = split(curLine).toInt
          readMetadata()
        }

        //Reads the level size from a line "Size: 10x10"
        def readSize() = {
          val rawSize = split(curLine).split('x')
          size = (rawSize(0).toInt, rawSize(1).toInt)
          readMetadata()
        }
      }

      //Reads the TILES block until ENDTILES is reached
      def readTileData(): Unit = {

        readNextLine()
        println(rawLine)

        //Tile data
        var name = DefaultTileName
        var image = DefaultImage
        var tiletype: Option[String] = None
        var id: Option[Int] = None

        var price = DefaultBuildingPrice
        var damage = DefaultTowerDamage
        var reload = DefaultReload
        var range = DefaultRange

        curLine match {
          case curLine if curLine.startsWith("tile")     => nextTile()
          case curLine if curLine.startsWith("id")       => id = Some(split(curLine).toInt)
          case curLine if curLine.startsWith("pic")      => image = loadImage(split(curLine))
          case curLine if curLine.startsWith("type")     => tiletype = Some(split(curLine))
          case curLine if curLine.startsWith("price")    => price = readPrice
          case curLine if curLine.startsWith("damage")   => damage = split(curLine).toInt
          case curLine if curLine.startsWith("reload")   => reload = split(curLine).toInt
          case curLine if curLine.startsWith("range")    => range = split(curLine).toInt
          case curLine if curLine.startsWith("//")       => readTileData()
          case curLine if curLine.startsWith("endtiles") => tilesRead = true

        }

        //Creates a tile based on the parameters read and adds it to the tiles Buffer
        def nextTile() = {
          if (tiles.isEmpty) {
            ()
          } else if (tiletype.isEmpty || id.isEmpty) {
            throw new MapFileException("Error reading tile data")
          } else {
            tiletype.get match {
              case "empty"    => createEmptyTile()
              case "path"     => createPath()
              case "tower"    => createTower()
              case "building" => createBuilding()
              case _          => throw new MapFileException("Invalid tile type")
            }
          }

          def createEmptyTile() = tiles += new Tile(name, image, (0, 0))
          def createPath() = tiles += new TraversableTile(name, image, (0, 0))
          def createTower() = tiles += new Tower(name, image, (0, 0), price, damage, reload, range)
          def createBuilding() = tiles += new Building(name, image, (0, 0), price)
        }

        //Returns an image from maps folder with "name"
        def loadImage(name: String) = {
          try {
            new Image(new FileInputStream("pics/" + name), TileSize, TileSize, true, false)
          } catch {
            case e: FileNotFoundException =>
              throw new MapFileException(s"Error finding $name")
          }
        }

        //Reads the price, formatted as "X20 Y20"
        def readPrice = {
          val rawPrice = split(curLine).split(' ').map(_.replaceAll("\\D", "").toInt)
          (rawPrice(0), rawPrice(1))
        }

        if (!tilesRead) readTileData()

      } //readTileData()

      def readMapData() = {
        ???
      }

      def readEnemyData() = {
        ???
      }

      def readWaveData() = {
        ???
      }

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



