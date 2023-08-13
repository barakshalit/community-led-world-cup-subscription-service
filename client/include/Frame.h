#pragma once
#include <string>
#include <map>
#include <stdlib.h>
#include <vector>
#include <sstream>

class Frame
{
private:

public:
std::string currString;
std::map<std::string,std::string> map;

Frame(std::string message);
void mapStompCommand();
void mapHeaders();
void mapBody();
std::string getNextLine();
std::vector<std::string> split (std::string x,char splitby);


};
