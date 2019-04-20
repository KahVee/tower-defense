package towerdefense

import scalafx.scene.image._
import scalafx.scene.SnapshotParameters
import scalafx.scene.paint._
import scalafx.scene.transform.Rotate

class Enemy(val name: String, var image: Image, var coords: (Float, Float), private val grid: Grid) {

  private var speed = DefaultEnemySpeed
  private var health = DefaultEnemyHealth
  private var direction: Direction = Down
  private var imageDirection: Direction = Up //Always Up, determined by the image file rotation

  private var lastTile: Option[Tile] = None
  private var currentTile: Option[Tile] = None

  var isActive = false
  var reachedTarget = false

  def isDead = health <= 0
  def takeDamage(dmg: Int) = health -= dmg

  //Moves the enemy forward by one step. Length of the step is determined by the speed of the enemy and dt.
  //Also acts as simple pathfinding, as in checks if the next tile is accessible, and if not, rotates the enemy.
  def step(dt: Float) = {
    if (isDead) {
      isActive = false
    }
    if (isActive) {

      //Checks, which tile the enemy is currently on
      updateCurrentTile()

      var nextTile = grid.nextTile(direction, (coords._1.toInt, coords._2.toInt))
      if (lastTile.getOrElse(null) == grid.exitTile) {
        reachedTarget = true
        isActive = false
      } else {
        //Checks if next tile forward is a path or if it was the tile the enemy came from and rotates if necessary
        //Uses .toInt instead of .round to stay close to the centers of the tiles
        //TODO: Make infinite loop not possible
        if (currentTile != lastTile) {
          while ((!nextTile.getOrElse(null).isInstanceOf[TraversableTile]) || nextTile == lastTile) {
            direction = direction.clockwise
            nextTile = grid.nextTile(direction, (coords._1.toInt, coords._2.toInt))
          }
        }

        //Match the rotation of the image with the actual rotation of the enemy
        if (imageDirection != direction) rotateImage

        //Move the enemy
        val dcoords = direction * (speed * dt)
        coords = (coords._1 + dcoords._1, coords._2 + dcoords._2)
      }
    }
  }

  //Finds the current tile, and if it's changed from the previous step, updates the lastTile and currentTile variables
  def updateCurrentTile() = {
    val current = grid.currentTile(coords)
    if (current != currentTile) {
      lastTile = currentTile
      currentTile = current
    }
  }

  //Hackiest of hacks to rotate the "raw" image to match the actual direction of the enemy. Converts the image into ImageView, which is rotated, and then converted back to a raw image.
  def rotateImage() = {
    val iv = new ImageView(image)

    def rotation = {
      var rot = 0
      while (imageDirection != direction) {
        imageDirection = imageDirection.clockwise
        rot += 90
      }
      rot
    }

    iv.rotate = rotation
    image = iv.snapshot(new SnapshotParameters { fill = Color.Transparent }, new WritableImage(EnemySize, EnemySize))
  }

  override def toString = s"Enemy(s$speed h$health)(coords: $coords)"
}

//helper object to make a new Enemy with copied parameters from another
object Enemy {
  def apply(other: Enemy) = {
    val newEnemy = new Enemy(other.name, other.image, other.coords, other.grid)
    newEnemy.speed = other.speed
    newEnemy.health = other.health
    newEnemy.direction = other.direction
    newEnemy.rotateImage
    newEnemy
  }
}
