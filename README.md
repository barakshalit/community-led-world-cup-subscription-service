# community-led-world-cup-subscription-service
STOMP server/client protocol project with both **TPC** and **Reactor** server implementaiton

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/132adb04-345e-4c3e-ba99-f4257114c0cd)


# General Description
In this assignment iv'e implemented a ”community-led” world cup update subscription service. Users can subscribe to a game channel and report and receive reports 
about the game to and from the other subscribed users.

For the above-mentioned purpose, iv'e implemented both a server, which will provide STOMP server services and a client, which a user can use in order to interact with the rest of the users.
The server will be implemented in Java and will support both Thread-Per-Client (TPC) and the Reactor, choosing which one according to arguments given on startup. The client will be implemented in
C++ and will hold the required logic as described below. 
All communication between the clients and the server will be according to STOMP ‘Simple-Text-Oriented-Messaging-Protocol’.


# Simple-Text-Oriented-Messaging-Protocol (STOMP)
## Overview
STOMP is a simple inter-operable protocol designed for asynchronous message
passing between clients via mediating servers. It defines a text based wireformat for messages passed between these clients and servers. We will use the
STOMP protocol in our assignment for passing messages between the client
and the server. This section describes the format of STOMP messages/data
packets, as well as the semantics of the data packet exchanges. For a complete
specification of STOMP, read: [STOMP 1.2](https://stomp.github.io/stomp-specification-1.2.html).

##  STOMP Frame format
The STOMP specification defines the term frame to refer to the data packets
transmitted over a STOMP connection. A STOMP frame has the following
general format:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/1f086b85-6b71-4cc2-a40a-72bff0e74c47)

A STOMP frame always starts with a STOMP command (for example, SEND)
on a line by itself. The STOMP command may then be followed by zero or
more header lines. Each header is in a <key>:<value> format and terminated by
a newline. The order of the headers shouldn’t matter, that is, the frame:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/dd83b1fc-fa02-4237-b3b4-7a122010b9a8)

should be handled the same as:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/c2e86d92-b7fb-4787-9ccd-b550dcab7dab)

and not cause an error due to permutation of the headers.
A blank line indicates the end of the headers and the beginning of the body
<FrameBody> (which can be empty for some commands, as in the example of
SUBSCRIBE above). The frame is terminated by the null character, whichis
represented as ^@ above (Ctrl + @ in ASCII, ’\u0000’ in Java, and ’\0’ in
C++).

## STOMP Server

A STOMP server is modeled as a set of topics (queues) to which messages can
be sent. Each client can subscribe to one topic or more and it can send messages
to any of the topics. Every message sent to a topic is being forwarded by the
server to all clients registered to that topic.

## Connecting
A STOMP client initiates the stream or TCP connection to the server by sending
the CONNECT frame:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/294990ab-4a12-4d2a-ada7-da62ab639cb2)


• **accept-version:** The versions of the STOMP protocol the client supports. In this case it will be version 1.2. 

• **host:** The name of a virtual host that the client wishes to connect to. 

• **login:** The user identifier used to authenticate against a secured STOMP server. Should be unique for every user.

• **passcode:** The password used to authenticate against a secured STOMP server.

The **CONNECT** sets <FrameBody> as empty.
The sever may either response with a CONNECTED frame:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/6bcebfb6-37b9-464d-b21b-9c2bf2fa944a)

## Stomp frames
In addition to the above defined **CONNECT** and **CONNECTED** frames, we define a few
more STOMP frames to be used in this implementation.

The following is a summary of the frames we will define:
Server frames:
• CONNECTED (as defined above)

• MESSAGE

• RECEIPT

• ERROR

Client frames:
• CONNECT (as defined above)

• SEND

• SUBSCRIBE

• UNSUBSCRIBE

• DISCONNECT

#### Server frames:

• **MESSAGE**:

The **MESSAGE** command conveys messages from a subscription to the client.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/60cd54fc-406a-468d-b02d-b7adca2f9829)

The MESSAGE frame should contain the following headers:

○ destination: the subscription to which the message is sent.

○ subscription: a client-unique id that specifies the subscription from which the message was received. This id will be supplied by the client, more on that in the SUBSCRIBE client frame.

○ message-id: a server-unique id that for the message. To be picked by the server.


The frame body contains the message contents.

• RECEIPT:
A RECEIPT frame is sent from the server to the client once a server has
successfully processed a client frame that requests a receipt.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/659e305c-9c45-4814-b28f-16953e455d5f)

The RECEIPT fame should contain the single header receipt-id, and it’s
value should be the value specified by the frame that requested the receipt.
The frame body should be empty.
A RECEIPT frame is an acknowledgment that the corresponding client frame
has been processed by the server. Since STOMP is stream based, the
receipt is also a cumulative acknowledgment that all the previous frames
have been received by the server. However, these previous frames may
not yet be fully processed. If the client disconnects, previously received
frames SHOULD continue to get processed by the server.
NOTE: the receipt header can be added to ANY client frame
which requires a response. Thus, ANY frame received from the
client that specified such header should be sent back a receipt
with the corresponding receipt-id.

• ERROR:
The server MAY send ERROR frames if something goes wrong. In this case,
it MUST then close the connection just after sending the ERROR frame.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/2dfc4e70-310b-46b2-aad1-2ea5f59ab98b)

The ERROR frame SHOULD contain a message header with a short description of the error, and the body MAY contain more detailed information
(as in the example above) or MAY be empty.
If the error is related to a specific frame sent from the client, the server
SHOULD add additional headers to help identify the original frame that
caused the error. For example, if the frame included a receipt header, the
ERROR frame SHOULD set the receipt-id header to match the value of the receipt header of the frame to which the error is related (as in the
above frame example).

#### Client frames

• SEND:
The SEND command sends a message to a destination - a topic in the
messaging system.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/40440551-68be-4a00-ab20-bbd54b33089b)

The SEND frame should contain a single header, destination, which indicates which topic to send the message to.
The body of the frame should contain the message to be sent to the topic.
Every subscriber of this topic should receive the content of the body as
the content of a MESSAGE frame’s body, sent by the server.
If the server cannot successfully process the SEND frame for any reason, the
server MUST send the client an ERROR frame and then close the connection.

In this implementation, if a client is not subscribed to a topic it is not allowed to send messages to it, and the server should send back an ERROR frame.

• SUBSCRIBE:
The SUBSCRIBE command registers a client to a specific topic.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/3c22e177-fdfd-4388-b4bc-67f4f3ed388b)

The SUBSCRIBE frame should contain the following headers:

  ○ destination: Similar to the destination header of SEND. This header will indicate to the server to which topic the client wants to subscribe.
  
  ○ id: specify an ID to identify this subscription. Later, we will use the ID if client will UNSUBSCRIBE. When an id header is supplied in the SUBSCRIBE frame, the server must append the 
    subscription header to any MESSAGE frame sent to the client. For example, if clients a and b are subscribed to /topic/foo with the id 0 and 1 respectively, and someone sends a message to
    that topic, then client a will receive the message with the id header equal to 0 and client b will receive the message with the id header equals to 1.
    Thus, this ID is generated uniquely in the client before subscribing to a topic.

The body of the frame should be empty.
After this frame was processed by the server, any messages received on the
destination subscription are delivered as MESSAGE frames from the server
to the client.
If the server cannot successfully create the subscription, the server MUST
send the client an ERROR frame and then close the connection.


• UNSUBSCRIBE:
The UNSUBSCRIBE command removes an existing subscription, so that the
client no longer receives messages from that destination.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/5bdc392e-6f46-4a67-bb8b-91e5a7baec93)

The UNSUBSCRIBE should contain a single header, id, which is the subscription ID supplied to the server with the SUBSCRIBE frame in the header with
the same name.
The body of the frame should be empty.


• DISCONNECT: The DISCONNECT command declares to the server that the
client wants to disconnect from it.

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/fc4ed48e-fc3b-4e16-8112-e92bc460dae6)

The DISCONNECT should contain a single header, receipt, which contains
a the recipt-id the client expects on the receipt returned by the server.
This number should be generated uniquely by the client.
The bodey of the frame should be empty.
A client can disconnect from the server at any time by closing the socket
but there is no guarantee that the previously sent frames have been received by the server. To do a graceful shutdown, where the client is assured
that all previous frames have been received by the server, the client should:

1. Send a DISCONNECT frame. For example, the one shown above.
2. Wait for the RECEIPT frame response to the DISCONNECT. For example:


![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/57c8c432-4223-4588-9dab-710c67ff7b86)

3. close the socket.
This is graceful since after receiving the response, the client can be
sure that every message he sent (barring packet losses, which is a
subject not covered in this course) was received and processed by the
server, and thus it can close its socket and no messages will be lost.


The receipt header:
As mentioned before, the receipt header can be added to ANY client
frame which requires a response. The DISCONNECT frame MUST contain
it, but it is not unique in that regard. We specify, in the client
implementation section, some cases in which the client will be required to
get a receipt on frames, so we will have to use this header.
In addition, the client can decide to use the receipt header discriminately,
for ANY frame instance he send, if the client wish to receive a RECEIPT
frame for it from the server.

# Implementation Details

## General Guidelines
• The server is written in Java. The client is written in C++. Both run on Linux installed at CS computer labs or the VM.

• Use maven as the build tool for the server and makefile for the c++ client.

## Server

Iv'e implement a single protocol, supporting both the ThreadPer-Client and Reactor server patterns presented in class. 
for both servers is included in the template. 
Iv'e been also provided with 3 new or changed interfaces:

• Connections

This interface should map a unique ID for each active client connected to the server. The implementation of Connections is part of the server pattern and not part of the protocol. It has 3 functions that iv'e implement:
  ○ boolean send(int connectionId, T msg);
    sends a message T to client represented by the given connectionId.
    
  ○ void send(String channel, T msg);
    Sends a message T to clients subscribed to channel.
    
  ○ void disconnect(int connectionId);
    Removes an active client connectionId from the map

• ConnectionHandler<T>
A function was added to the existing interface

  ○ void send(T msg);
  sends msg T to the client. Should be used by the send commands in
  the Connections implementation.


• StompMessagingProtocol
This interface replaces the MessagingProtocol interface. It exists to support p2p (peer-to-peer) messaging via the Connections interface. It contains 3 functions:
  ○ void start(int connectionId, Connections<String> connections); 
    Initiate the protocol with the active connections structure of the server and saves the owner client’s connection id.
    
  ○ void process(String message);
    As in MessagingProtocol, processes a given message. Unlike MessagingProtocol, responses are sent via the connections object send functions (if needed).
    
  ○ boolean shouldTerminate();
  true if the connection should be terminated.

## Client
An echo client was provided, but it is a single-threaded client. While it is blocking on stdin (read from keyboard) it does not read messages from the socket. 
Iv'e improve the client so that it will run 2 threads. One should read from the keyboard while the other should read from the socket.
The client should receive commands using the standard input (terminal). 
The client will translate the keyboard commands it receives to local behavior and network messages (frames) to implement the desired behavior.


#### The STOMP World Cup Informer
This section describes the commands the client will receive from the console, and
what it will do with them - namely, what frames it will send to the server and
what possible responses the client may receive. Please note that all commands
can be processed only if the user is logged in (apart from login). In all these
commands, any error (whether an ERROR frame or an error in the client side)
should produce an appropriate message to the client stdout. In case of an error
frame, the message header will be printed.

#### Client commands for all users
For any command below requiring a game_name input: game_name for a game
between some Team A and some Team B should always be of the form

<team_a_name>_<Team_b_name>

Client commands:
• Login command
  ○ Structure: login {host:port} {username} {password}
  
  ○ For this command a CONNECT frame is sent to the server.
  
  ○ The possible outputs the client can have for this command:
  
    ∎ Socket error: connection error. In this case, the output should
    be ”Could not connect to server”.
    
    ∎ Client already logged in: If the client has already logged into a
    server the client should not attempt to log in again. The client should
    simply print ”The client is already logged in, log out before trying
    again”.
    
    ∎ New user: If the server connection was successful and the server
    doesn’t find the username, then a new user is created, and the
    password is saved for that user. Then the server sends a
    CONNECTED frame to the client and the client will print ”Login
    successful”.
    
    ∎ User is already logged in: If the user is already logged in, then
    the server will respond with a STOMP error frame indicating
    the reason – the output, in this case, should be ”User already
    logged in”.
    
    ∎ Wrong password: If the user exists and the password doesn’t
    match the saved password, the server will send back an appropriate ERROR frame indicating the reason - the output, in this
    case, should be ”Wrong password”.
    
    ∎ User exists: If the server connection was successful, the server
    will check if the user exists in the users’ list and if the password
    matches, also the server will check that the user does not have an
    active connection already. In case these tests are OK, the server
    sends back a CONNECTED frame and the client will print to
    the screen ”Login successful”.

Example:

○ Command: login 1.1.1.1:2000 meni films

○ Frame sent:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/869192ad-362e-4bdb-b6df-da83921f5cff)


○ Frame received:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/b261c399-023e-4863-a676-771865213fdf)


• Join Game Channel command

  ○ Structure: join {game_name}
  
  ○ For this command a SUBSCRIBE frame is sent to the {game_name} topic.
  
  ○ As a result, a RECIEPT will be returned to the client. A message ”Joined channel {game_name}” will be displayed on the screen.
  
  ○ From now on, any message received by the client from {game_name} should be parsed and used to update the information of the game as specified in the game events section. As stated in the
  report command specification, each report will contain the name of the reporter, and the reports are saved from different users separately.

Example:

  ○ Command: join germany_spain
  
  ○ Frame sent:
  
  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/6b242602-2f4f-4e99-8317-5c901d2fdbbb)
  

  ○ Frame Received:
  
  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/cdc1ed94-d23d-43af-9d22-b903ccd7b586)
  

• Exit Game Channel command
  ○ Structure: exit {game_name}
  
  ○ For this command an UNSUBSCRIBE frame is sent to the {game_name} topic.
  
  ○ As a result, a RECIEPT will be returned to the client. A message ”Exited channel {game_name}” will be displayed on the screen.


Example:
  ○ Command: exit germany_spain
  
  ○ Frame sent:
  
  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/b2542add-0aa5-4415-9d7b-d7386487439f)
  

  ○ Frame received:
  
  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/720ff6c9-ca72-4989-8797-4cfd955acf24)
  

• Report to channel command
  ○ Structure: report {file}
  
  ○ For this command, the client will do the following:
  1. Read the provided {file} and parse the game name and events it contains (more on the file format in the game event section).
    
  2. Save each event on the client as a game update reported by the current logged-in user. in this implementation i saved the events ordered by the time specified in them, as in the end, i
     would like to summarize them in that order in the summary command.

  3. Send a {SEND} frame for each game event to the {game_name} topic (which, as mentioned, should be parsed from within the file), containing all the information of the game event in its body,
     as well as the name of the {user}.

An example of a SEND frame containing a report:
    
![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/b3513921-e7c7-4423-963c-43edca892ac8)

A client receiving such a message will have to parse the information of the game event from the body.


Example: (with the events1_partial.json file that is provided)
    
 ○ Command: report events1_partial.json
    
○ Frame sent:

![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/1571d593-e6ae-4a59-ad51-57f4d3e01b70)
    
![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/48fe038f-174a-4a4d-9efa-9f6023614ccf)

○ Frame sent:

 ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/9ed5c6ed-6c33-4b65-b1ae-8edc511bd6f4)


• Summarize Game command

  ○ Structure: summary {game_name} {user} {file}
  
  ○ For this command the client will print the game updates it got from {user} for {game_name} into the provided {file}.
  
  The print format is as follows:
  
  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/5b5813b0-d0aa-4615-bd29-99df303c92b5)
  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/00397c90-8e83-4ea6-99dd-313f4b07df1b)

  The game event reports should be printed in the order that they happened in the game, and the stats should be printed ordered lexicographically by their name. More on game events and game stats
  in the Game Event section.

  ○ If {file} doesn’t exist, create it. Otherwise, write over its content.
  
  ○ Note that {user} can be the clients current active user. This should not cause a problem for this command since the client is saving every game event it sends



• Logout Command
  ○ Structure: logout
  
  ○ This command tells the client that the user wants to log out from the server. The client will send a DISCONNECT to the server.
  
  ○ The server will reply with a RECEIPT frame.
  
  ○ The logout command removes the current user from all the topics.
  
  ○ Once the client receives the RECEIPT frame, it should close the socket and await further user commands.

Example:
  ○ Command: logout
  
  ○ Frame sent:

  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/b98f2b6f-d6b9-4704-8426-8a1bca3e9254)

  ○ Frame received:

  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/e972e313-c469-48b9-a89c-308a05f80c78)


## Game event

A game event is the format clients use to report about the game. Each game
event has the following properties:
  
  • event name - The name of the game event. Does not have to be unique to the event. in this implementation i will not need to extract any information from this property, just to show it when
    reporting on the game.
  
  • description - A description of the game event. Can be anything, again, i will not need to extract information from this, just to save and display it in the game summary.
  
  • time - The time in the game, in seconds, when the event occurred. This will be used to keep the order of the events reported on the game. You can assume a game will not have 2 events 
    reported at the same time, however, two events can indeed have the same time property value, as described below.
    
    Note: since game halves can have a time extension, the time before the half
    can exceed 45 minutes (when translated to minutes), thus a game event can
    occur after another game event with a higher time. For example, if a goal
    was scored 1 minute into the time extension of the first half, the game
    event reporting it will have a time of 2760(seconds) = 45+1(minutes).
    However, the game event reporting the beginning of the second half will
    have a time of 2700(seconds) = 45+1(minutes).
    The above-mentioned can cause a problem with saving the game events
    in the correct order, which can be solved by keeping a flag noting whether
    the halftime event had occurred yet. More on this in the general game
    updates property.


  • Game updates properties:
    ○ general game updates - Any stat updates on the game that is not related to a particular team will be listed under this property.

    ○ team a updates - Any stat updates related to team a, such as ball possession, goals, etc.
    
    ○ team b updates - The same as for team a, but for team b.

  The client will receive game events to report from a JSON file. We provide a
  parser for game event files to C++ HashMap. The parser is given in the files
  event.h and event.cpp along with the class Event. To use the parser, simply call
  the parseEventsFile(std::string json_path) function with a path to an events
  file JSON as the argument. The parser returns a struct containing the names
  of both teams as well as a vector containing the parsed events.
  An example of the usage of the parser:
  names_and_events nne = parseEventsFile("data/events1_partial.json").


  An example of a game event in JSON format:

  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/6178da67-93ef-4f9f-858e-b85c490c2c77)


  An example of a game events JSON file: (the events1_partial.json file provided in the template)

  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/ea5affa5-db52-4795-8bed-6f9613b01477)

  ![image](https://github.com/barakshalit/community-led-world-cup-subscription-service/assets/76451972/8bc66a17-8e37-4af5-931f-6a5e8bfa1758)

  As you can see, you can determine the game_name reported on in the game events file from the properties team a and team b. The game events file also contains the events to report on in a list    corresponding with the property events.













  

    

    






































