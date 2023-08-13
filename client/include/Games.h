#pragma once
#include <stdlib.h>
#include <string>
#include <vector>
#include <map>
#include <../include/event.h>
#include <list>
#include <iterator>


class Games
{
private:

public:

std::string currString;
std::map<std::string,std::map<std::string,std::list<Event>>> map;

Games();

void MapEventToGame(std::string username, Event event);



};
