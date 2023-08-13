#pragma once
#include <string>
#include <map>

class Connections
{
private:

public:
std::string username;
std::map<std::string,int> channeltoUniqueID;
std::map<int,std::string> receiptToOriginalMsg;

Connections();



};