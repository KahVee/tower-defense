package towerdefense

class Game(val grid: Grid, var resX: Int, var rexY: Int, var buildableBuildings: Vector[Building], private var enemies: Vector[Enemy], var waves: Vector[Wave], var health: Int) {

  private var builtBuildings = Vector[Building]()

  private var outOfHealth = health <= 0
  def isLost = outOfHealth
}

