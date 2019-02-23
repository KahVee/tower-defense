package towerdefense

class Game(val grid: Grid, var resX: Int, var rexY: Int, var buildableBuildings: Vector[Building], private var enemies: Vector[Enemy], var waves: Vector[Wave], var health: Int) {

  private var builtBuildings = Vector[Building]()

  private var startTime = 0L
  private val timer = new java.util.Timer()
  private val updateTask = new java.util.TimerTask {
    def run() = update()
  }

  //Main game loop starting method
  def start() = {
    timer.scheduleAtFixedRate(updateTask, 1000 / Tickrate, 1000 / Tickrate)
    println("started")
    startTime = System.currentTimeMillis()
  }

  //Main loop, gets called constants.Tickrate times a second
  def update() = {
    println(System.currentTimeMillis() - startTime)
    drawInConsole
  }

  private var outOfHealth = health <= 0
  def isLost = outOfHealth

  def drawInConsole = {
    grid.grid.foreach(x => println(x.mkString))
  }
}

