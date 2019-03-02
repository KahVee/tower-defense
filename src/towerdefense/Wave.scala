package towerdefense

class Wave(val enemies: Vector[(Enemy, Int)], val time: Int) {
  
  
  override def toString = {
    var out = ""
    enemies.foreach(x => out += x._1.toString() + " amount: " + x._2.toString() + " time: " + time + "\n")
    out
  }
}