# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer: My implementation was much less modular than the given solution; as a result, my implementation would result in a much higher level of complexity. 
The main lesson to be learned is that I need to make sure to use helper functions and keep my implementations simple for this type of work.

-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer: The hexagons are analogous to the rooms in project 3, and tessellating them is the equivalent of connecting rooms using hallways.

-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer: I think that the first method I would develop would determine how many rooms would exist, and where they would be located.

-----
**What distinguishes a hallway from a room? How are they similar?**

Answer: Rooms can be thought of more as the main "thing" that is being tessellated, and hallways can be used to connect them to each other.
