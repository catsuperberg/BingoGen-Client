
# BingoGen
This app lets you generate and play bingo sheets for any video game (actually there's no limitation and any task sheet could be used). 
<p align="center">
  <img src="https://github.com/KEALHOVIK/BingoGen-Client/assets/9151200/43af9e3f-bafc-44a0-83e5-eb4ea26a45f9" width=25% />
</p>

You simply select a game from a list, a task sheet from available options for this game, select the size and start the game. Game will provide a playable bingo sheet and a timer will start to advance, finish a row or a column of tasks to get a bingo. See how fast you could get it. 

<p align="center">
  <img src="https://github.com/KEALHOVIK/BingoGen-Client/assets/9151200/d31f73fb-9163-4653-833b-0630442a3f98)" width=25% />
  <img src="https://github.com/KEALHOVIK/BingoGen-Client/assets/9151200/8a83fbb4-bce1-4d2b-98c6-145add0a257e" width=25% />
  <img src="https://github.com/KEALHOVIK/BingoGen-Client/assets/9151200/1a26e237-62d4-4b41-b0dd-3d530fb4fcf4" width=25% />
</p>


Main mode would be a multiplayer game, as I created this app to use it myself. Idea is to give an option of competitive play to any single player games. So instead of finding some multiplayer games, me and my friend would like, we could just create a fun task sheet for our favorite games and see who could get a bingo first. As of now only the single player component is done and i choose it as a first thing to complete as it is obviously much simpler. But also I used this project to get familiar and try out a vertical slice of mobile app development, i feel that this portion of an app in combination with server application covers most essential aspects of mobile development.

# Server
Deploying a server application is needed to actually use this app. Only way to add task sheets is to add them to the ingest folder of a server. More info on https://github.com/catsuperberg/BingoGen-Server page.

# Plans
- Implement multiplayer;
- Lobby selection and creation with simple nickname only authentication;
- Multiplayer modes: mirror board individual completion, mirror board swap task if opponent finished set task, individual boards.
