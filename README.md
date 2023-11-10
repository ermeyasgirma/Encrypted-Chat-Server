# Encrypted-Chat-Server

As a fun little project I have created a chat server with the following functionality:
  - end to end encryption
  - graphical user interface
  - direct messaging

How to run:
  Note: you must have java installed

  1. open your command line terminal and enter the /src directory
  2. open as many terminal tabs as you would like chatroom users (if you want to have n chatroom users, open n+1 terminals, the extra being for the server), for this demo have 4 tabs open 
  3. in one tab we will run the server by the following two commands: 1. $javac Server.java 2. $java Server
  4. now the server is running, lets start connecting some users, in the rest of the tabs run the following two commands: 1. $javac ClientController.java  2. $java ClientController
  5. as instructed, enter a username with no spaces in the text space from the interactive window that has just appeared

Special Commands:

  - "/exit": sending this message causes you to exit the chat, broadcasting a message to the other users saying "<username> has left the chat :(("

  - "/pm <recipient_username> <message>": as you may have noticed, messages are broadcasted to the entire chat, so to privately message following the aforementioned format


Extensions:

  1. Implement some sort of login system where users have username and password, and upon login, previous messages are loaded from persistent storage
  2. Allow private messaging with more than one user, a private groupchat of sorts
  3. Allow users to have a separate panel for private messaging 
