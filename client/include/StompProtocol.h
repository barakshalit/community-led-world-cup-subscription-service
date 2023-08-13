#pragma once

#include "ConnectionHandler.h"
#include "../include/Connections.h"
#include <../include/Frame.h>
#include <list>
#include <Games.h>
#include <vector>
#include <sstream>
#include <map>
#include <string>



// TODO: implement the STOMP protocol
class StompProtocol
{
private:

public:

int id;
int recipt;
bool login;
ConnectionHandler handler;
Connections con;
Games game;





StompProtocol (std::string host , short port);


bool start();
int ReadfromKeyboard();
int ReadfromSocket();

std::string getNextLine(std::string msg);


std::string handleLogin(std::vector<std::string> seglist);
std::string handleSubscribe(std::vector<std::string> seglist);
std::string handleUnSubscribe(std::vector<std::string> seglist);
std::string handleSend(std::vector<std::string> seglist);
std::string handleLogout(std::vector<std::string> seglist);
void handleSummary(std::vector<std::string> seglist, Games game);
std::vector<std::string> split (std::string x,char splitby);


std::string processKeyboard(std::string message);
std::string processEvent(Event event);
std::string processServerMessage(std::string message);



};
