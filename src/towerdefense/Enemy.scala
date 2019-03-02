package towerdefense

import scalafx.scene.image.Image

class Enemy(val image: Image, var coords: (Float, Float), private var grid: Grid) {

  private var speed = DefaultEnemySpeed
  private var health = DefaultEnemyHealth
  private var direction: Direction = Down

  var isActive = false

  def isDead = health <= 0

  def takeDamage(dmg: Int) = health -= dmg

  def step(dt: Float) = {
    if (isActive) {
      //TODO: Make a detection "ray" to check the tile in front
      if(!currentTile.isInstanceOf[TraversableTile]) direction = Up
      
      val dcoords = direction * (speed * dt)
      coords = (coords._1 + dcoords._1, coords._2 + dcoords._2)
    }
  }

  //TODO: Make an actual version of this instead of just this proof.-of-concept
  def currentTile = if (coords._1.round > 0 && coords._1.round < grid.grid.size && coords._2.round > 0 && coords._2.round < grid.grid.size) grid.grid(coords._1.round)(coords._2.round) else grid.entryTile
  
  override def toString = s"Enemy(s$speed h$health)(coords: $coords)"
}

//helper object to make a new Enemy with copied parameters from another
object Enemy {
  def apply(other: Enemy) = {
    val newEnemy = new Enemy(other.image, other.coords, other.grid)
    newEnemy.speed = other.speed
    newEnemy.health = other.health
    newEnemy.direction = other.direction
    newEnemy
  }
}
