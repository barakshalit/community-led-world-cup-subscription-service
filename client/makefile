CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

all: StompWCIClient

EchoClient: bin/ConnectionHandler.o bin/echoClient.o
	g++ -o bin/EchoClient bin/ConnectionHandler.o bin/echoClient.o $(LDFLAGS)

StompWCIClient: clean bin/ConnectionHandler.o bin/StompClient.o bin/event.o bin/StompProtocol.o bin/Connections.o bin/Games.o bin/Frame.o
	g++ -o bin/StompWCIClient bin/ConnectionHandler.o bin/StompClient.o bin/event.o bin/StompProtocol.o bin/Connections.o bin/Games.o bin/Frame.o $(LDFLAGS)

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/event.o: src/event.cpp
	g++ $(CFLAGS) -o bin/event.o src/event.cpp

bin/StompClient.o: src/StompClient.cpp
	g++ $(CFLAGS) -o bin/StompClient.o src/StompClient.cpp

bin/StompProtocol.o: src/StompProtocol.cpp
	g++ $(CFLAGS) -o bin/StompProtocol.o src/StompProtocol.cpp

bin/Connections.o: src/Connections.cpp
	g++ $(CFLAGS) -o bin/Connections.o src/Connections.cpp  

bin/Games.o: src/Games.cpp
	g++ $(CFLAGS) -o bin/Games.o src/Games.cpp  

bin/Frame.o: src/Games.cpp
	g++ $(CFLAGS) -o bin/Frame.o src/Frame.cpp       
      


.PHONY: clean
clean:
	rm -f bin/*
	