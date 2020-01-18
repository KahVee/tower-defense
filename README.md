# Tower Defense
A simple 2D tower defense game with easily modifiable map files.

Waves of enemies make their way through a path. The player's objective is to stop them from getting to the last tile by
building shooting towers and resource-producing buildings. There are two resources (cleverly named X and Y) the player
accumulates by destroying enemies and from production buildings.

A big focus of the project was to create a level file type that would be easy for humans to edit and customize. The .map 
file specifies the layout of the map, graphics used by each tile and enemy, the different statistics (speed, damage, etc.)
of all entities and more. The customizability and expandability are quite decent. Two example maps are found in /maps and the
associated graphics in /pics. The file reading system means that the game can be configured in many ways. Different maps can
have different graphical themes, difficulty can be anything between a peaceful idling/town-building game to a brutally hard
and fast-paced tower defense.

The enemies find their way to the goal independently, if there are no dead ends or loops. The towers shoot the closest enemy inside
their shooting range.
