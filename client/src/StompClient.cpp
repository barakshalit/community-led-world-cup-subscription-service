#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include "../include/Connections.h"
#include <thread>
#include <../include/event.h>
#include <../include/Games.h>
#include <string>
#include <vector>


// std::vector<std::string>  split (std::string x,char splitby){
//     std::stringstream message(x);
//     std::string segment;
//     std::vector<std::string> seglist;

//     while(std::getline(message, segment, splitby))
//     {
//         seglist.push_back(segment);
//     }

//     return seglist;

// }

StompProtocol protocol("127.0.0.1",7777);
int main(int argc, char *argv[]) {
	// std::string temp = argv[1];
	// std::vector<std::string> splitted = split(temp, ':');

	// std::string hosti = splitted[0];
	// short port = std::stoi(splitted[1]);

	// StompProtocol protocol(hosti,port);
	if(!protocol.start()){
		return 0;
	}
		
	
	// std::thread keyboardThread([](StompProtocol protocols){
	// 	while(1){
	// 		if(!protocols.ReadfromKeyboard()){
	// 			break;
	// 		}
				
	// 	}

	// 	},protocol);

	std::thread keyboardThread([](){
		while(1){
			if(!protocol.ReadfromKeyboard()){
				break;
			}
				
		}

		});
		

		while(1){
			if(!protocol.ReadfromSocket()){
				

					break;
				}
		}

		

		return 0;
	

		
	
		
		
   
}
