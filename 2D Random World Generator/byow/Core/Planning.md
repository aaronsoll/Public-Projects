#Plans for Phase II:

##Persistence

- create a single .txt file which stores the serialized World object (world.txt)
- this world.txt file should be updated after every keystroke or character

##Interaction

When initialized, the engine will load the saved world from world.txt

InteractWithKeyboard() has 2 main steps which repeat:
1. each key is processed by dealWithKey(char key), which interacts with world appropriately
2. world is re-rendered to reflect appropriate UI
The world is saved within dealWithKey(char key) in the 'q' and 'Q' cases.

InteractWithInputString() will only loop through dealWithKey(char key)
until it has iterated through all characters in the input string. At this point, it will save 
the world and return the TETile[][] using world.getTiles(); 



## Remaining Tasks: 

- Heads Up Display + Mouse over feature (AARON)

- Ambition primary: Line of Sight / UI and Encounters (SAM)

- Ambition secondary: ?? (AARON)

- Make sure no functions in World class use real time (SAM)

