package gui

class Time {

  //Stores the last 60 frame times to calculate average frames per second
  var frameTimes = Vector.fill(60)(0D)
  
  //Calculates time difference since last frame as seconds to be used as dt
  private var lastTime = System.nanoTime  
  def deltaTime = {
    val out = 0.000000001 * (System.nanoTime - lastTime)
    lastTime = System.nanoTime
    frameTimes = frameTimes.tail :+ out
    out.toFloat
  }
  
  //Average frames per second during the last 60 frames
  def fps = {
    60/frameTimes.sum
  }
}