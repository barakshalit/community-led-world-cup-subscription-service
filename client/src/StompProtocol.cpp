
#include "StompProtocol.h"





StompProtocol:: StompProtocol (std::string host , short port):id(0), recipt(0), login(false),  handler(host,port) , con(Connections()), game(Games()) {
}



bool StompProtocol::start(){
   
    if (!handler.connect()) {
			std::cerr << "Cannot connect" << std::endl;
			return false;
	}
    return true;
}

int StompProtocol::ReadfromKeyboard(){

	login = false;

	
	//int len=line.length();

		
	while (1) { //waiting for next line loop
        

        const short bufsize = 1024;
        char buf[bufsize];

        std::cin.getline(buf, bufsize);
	    std::string line(buf);

		std::vector<std::string> seglist = split(line, ' ');

        
		if(seglist[0] == "login" || login){ //waiting for login
                                

				if(seglist[0] == "login" && login){//cleint trying to log in when already logged in
					std::cout << "Client already logged in, log out before trying again\n" << std::endl;
				}
                else{
                    login = true;
                }

				
               
                    

				if(seglist[0] == "report"){
					std::string filePath = seglist[1];
					names_and_events afterPharse = parseEventsFile(filePath);
					
					std::vector<Event> eventsVector = afterPharse.events;
                    int eventsVectorSize = eventsVector.size();

					for(int i = 0; i<eventsVectorSize; i++){
						std::string gamename = eventsVector[i].get_team_a_name() + "_" + eventsVector[i].get_team_b_name();

						//checking if the client is subscribed to the game:
						if(con.channeltoUniqueID.count(gamename) != 0){

							//documenting the event:
							game.MapEventToGame(con.username, eventsVector[i]);
							

							//sending the current event as repors (SEND frame) to the server
							std::string msg = processEvent(eventsVector[i]);
                      

							if (!handler.sendFrameAscii(msg, '\0') ){ //sending proccesed message to handelr to be sent to the server
							std::cout << "Disconnected. Exiting...\n" << std::endl;
							
							break;
							
						}

						}
						
					}	
						


				}

				else if(seglist[0] == "summary"){
					handleSummary(seglist, game);

				}
                else{
                    //regular
                   

                    std::string msg = processKeyboard(line); //processing the message
        

                    if (!handler.sendFrameAscii(msg,'\0') ){ //sending proccesed message to handelr to be sent to the server
                        std::cout << "Disconnected. Exiting...\n" << std::endl;
                        
                        break;
                        
                    }

                }


				
				// connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
				//std::cout << "Sent " << len+1 << " bytes to server" << std::endl;


		}

        	
	       


	}
    

    return 0;
		
        
		

}


int StompProtocol::ReadfromSocket(){

	std::string answer;
	int len;

	while(1){
   
        answer = "";
		if (!handler.getFrameAscii(answer,'\0')) { //gettiing answer from the server
			std::cout << "Disconnected. Exiting...\n" << std::endl;
			break;
    	}
        
        
		len=answer.length();

		answer.resize(len-1);
 


		std::string procceesedAnswer = processServerMessage(answer);
		
		
		if (procceesedAnswer == "DISCONNECT") {
			handler.close();
			std::cout << "Exiting...\n" << std::endl;
			
			break;

			
		}


	}

    

    return 0;
}


std::string StompProtocol::processKeyboard(std::string message){
    std::vector<std::string> seglist = split(message, ' ');
    std::string command = seglist[0];
   
    if(seglist[0] == "login"){
        return handleLogin(seglist);

    }

    if(seglist[0] == "join"){
        
        return handleSubscribe(seglist);

    }

    if(seglist[0] == "exit"){	
         

        
        return handleUnSubscribe(seglist);

    }

    if(seglist[0] == "report"){
        

    }

    if(seglist[0] == "logout"){
        
        return handleLogout(seglist);

    }


    return "";
    
}

std::string StompProtocol::processEvent(Event event){
    std::string response = "";
    response =  response +"SEND\n";
    response = response +"destination:/" + event.get_team_a_name() + "_" + event.get_team_b_name() + "\n";
    response = response +"\n";
    response = response +"user: " + con.username + "\n";
    response = response +"team a: " + event.get_team_a_name() + "\n";
    response = response +"team b: " + event.get_team_b_name() + "\n";
    response = response + "event name: " + event.get_name() + "\n";
    response = response +"time: " + std::to_string(event.get_time()) + "\n";

    std::map<std::string,std::string> generalGameupdates = event.get_game_updates();
    std::map<std::string,std::string> teamAupdates = event.get_team_a_updates();
    std::map<std::string,std::string> teamBupdates = event.get_team_b_updates();

    response = response + "general game updates: \n";
    if(generalGameupdates.count("active") != 0){
        response =  response +"   active: " + generalGameupdates["active"] + "\n";

    }
    if(generalGameupdates.count("before halftime") != 0){
        response = response +"   before halftime: " + generalGameupdates["before halftime"] + "\n";

    }

    response = response + "team a updates:\n";
    if(teamAupdates.count("goals") != 0){
        response =  response + "   goals: " + generalGameupdates["goals"] + "\n";

    }
    if(generalGameupdates.count("possession") != 0){
        response += "   possession: " + generalGameupdates["possession"] + "\n";

    }
    response = response + "team b updates:\n";

    if(teamBupdates.count("goals") != 0){
        response =  response + "   goals: " + generalGameupdates["goals"] + "\n";

    }
    if(generalGameupdates.count("possession") != 0){
        response += "   possession: " + generalGameupdates["possession"] + "\n";

    }
    response = response +"description: \n" ;
    response = response +event.get_discription() + "\n";
    response = response + "^@";


    return response;


}


std::string StompProtocol::processServerMessage(std::string message){

    Frame frame(message);
    std::string command = frame.map["StompCommand"];
    //delete frame;
 

    if(command == "CONNECTED"){
        std::cout << "login successful\n" << std::endl;

    }
     if(command == "MESSAGE"){
        std::string msg = "Message from the server:\n";
        msg = msg + message;
 

    }
    if(command == "RECEIPT"){
        
   

        //getting receipt id to string:
        int id = std::stoi(frame.map["receipt-id"]);

        std::string originalClientRequest = con.receiptToOriginalMsg[id];
      

        std::vector<std::string> seglist = split(originalClientRequest,':'); //seglist[0] - name of original request ("SUBSCIBE/UNSUBSCRIBE"), seglist[1] - name of the channel
        if(seglist[0] == "SUBSCRIBE"){
            std::cout << "Joined channel " + seglist[1] + '\n'  << std::endl;

        }
        if(seglist[0] == "UNSUBSCRIBE"){
            std::cout << "Exited channel " + seglist[1] + '\n'  << std::endl;

        }
        if(seglist[0] == "DISCONNECT"){
            return "DISCONNECT";
        }

    

    }

    
   
    return "";

    
    
}

std::string StompProtocol::handleLogin(std::vector<std::string> seglist){
    
    std::string message = "";
    message = message + "CONNECT" + '\n';
    message = message + "accept-version:1.2"  + '\n';
    message = message +"host:" + seglist[1]  + '\n';
    message = message +"login:" + seglist[2]  + '\n';
    con.username =  seglist[2];
    message = message + "passcode:" + seglist[3]  + '\n';
    message = message + '\n';
    message  = message + "^@";
    

    return message;
}

std::string StompProtocol::handleSubscribe(std::vector<std::string> seglist){

   
    std::string message = "";
    message = message +"SUBSCRIBE" + '\n';
    message = message + "destination:" + '/' + seglist[1]  + '\n';
    if(con.channeltoUniqueID.count(seglist[1]) != 0){
        message = message + "id:" + std::to_string(con.channeltoUniqueID[seglist[1]])  + '\n';

    }
    else{
        message = message + "id:" + std::to_string(id)  + '\n';
        con.channeltoUniqueID.insert({seglist[1],id});
        id++;
    }
   
    
    message = message + "receipt:" + std::to_string(recipt) + '\n';
    std::string channel =  seglist[1];
    con.receiptToOriginalMsg.insert({recipt,"SUBSCRIBE:" + channel});
    recipt++;
    message = message + '\n';
    message = message + "^@";
    return message;
}




std::string StompProtocol::handleUnSubscribe(std::vector<std::string> seglist){
    std::string message = "";
    message = message +  "UNSUBSCRIBE" + '\n';
    message = message + "id:" + std::to_string(con.channeltoUniqueID[seglist[1]]) + '\n';
    message = message + "receipt:" + std::to_string(recipt) + '\n';
    std::string channel =  seglist[1];
    con.receiptToOriginalMsg.insert({recipt,"UNSUBSCRIBE:" + channel});
    recipt++;
    message = message +'\n';
    message = message + "^@";
    return message;
}

 
std::string StompProtocol::handleSend(std::vector<std::string> seglist){
    std::string message = "";
    message += "SEND" + '\n';
    message += "/" + seglist[1] + '\n';
    message += '\n';



/// understand from where the file is coming
/// input the file as farmebody 
    
    message += '\n';
    message = message + "^@";
    return message;

}

std::string StompProtocol::handleLogout(std::vector<std::string> seglist){

   
    std::string message = "";
    message += "DISCONNECT" + '\n';
    message += "recipt:" + recipt + '\n';
    con.receiptToOriginalMsg.insert({recipt,"DISCONNECT:"});

    recipt++;
    message += '\n';
    message = message + "^@";
    return message;
}



void StompProtocol::handleSummary(std::vector<std::string> seglist, Games  game){
    std::string gameinput = seglist[1];
    std::vector<std::string> splitted = split(gameinput, '_');
    std::string gamename = splitted[0] + "vs" + splitted[1];

    std::string username = seglist[2];
    std::string filepath = seglist[3];

    std::string active ="";
    std::string beforeHalfTime ="";

    std::list<Event> eventlist = game.map[username][splitted[0] + "_" + splitted[1]];
    std::list<Event>::iterator iter;
    for (iter = eventlist.begin(); iter != eventlist.end(); ++iter){
        std::map<std::string, std::string> gameUpdates = iter->get_game_updates();
        if (gameUpdates.count("active") != 0){
            active = gameUpdates["active"];

        }
        if (gameUpdates.count("before halftime") != 0){
            beforeHalfTime = gameUpdates["before halftime"];

        }

    }

    std::list<Event>::iterator iter2;
    std::string teamAgoals ="";
    std::string teamApossetion ="";
    std::string teamBgoals ="";
    std::string teamBpossetion ="";


    for (iter2 = eventlist.begin(); iter2 != eventlist.end(); ++iter2){
        std::map<std::string, std::string> teamAUpdates = iter2->get_team_a_updates();
        std::map<std::string, std::string> teamBUpdates = iter2->get_team_b_updates();
        if (teamAUpdates.count("goals") != 0){
            teamAgoals = teamAUpdates["goals"];

        }
        if (teamAUpdates.count("possetion") != 0){
            teamApossetion = teamAUpdates["possetion"];

        }

        if (teamBUpdates.count("goals") != 0){
            teamBgoals = teamBUpdates["goals"];

        }
        if (teamBUpdates.count("possetion") != 0){
            teamBpossetion = teamBUpdates["possetion"];

        }

    }


    std::string message = "";
    message += gamename + '\n';
    message += "Game stats:"  + '\n';
    message += "General stats:"  + '\n';
    if(active != ""){
        message += active  + '\n';
    }
    if(beforeHalfTime != ""){
        message += beforeHalfTime  + '\n';
    }

    message += "Team " + seglist[0] + "stats:"  + '\n';
    if(teamAgoals != ""){
        message += teamAgoals  + '\n';
    }
    if(teamApossetion != ""){
        message += teamApossetion  + '\n';
    }

    message += "Team " + seglist[1] + "stats:"  + '\n';
    if(teamBgoals != ""){
        message += teamBgoals  + '\n';
    }
    if(teamBpossetion != ""){
        message += teamBpossetion  + '\n';
    }

    message += "Game events repotrs:" + '\n';
    std::list<Event>::iterator iter3;
    for (iter3 = eventlist.begin(); iter3 != eventlist.end(); ++iter3){
        message += iter3->get_time() + "-" + iter3->get_name() + '\n';
        message += '\n';
        message +=  iter3->get_discription() + '\n';
        message += '\n';

    }

    
}



// split fucntion for Strings
std::vector<std::string> StompProtocol::split (std::string x,char splitby){
    std::stringstream message(x);
    std::string segment;
    std::vector<std::string> seglist;

    while(std::getline(message, segment, splitby))
    {
        seglist.push_back(segment);
    }

    return seglist;

}

std::string StompProtocol::getNextLine(std::string msg){
        int i = 0;
        std::string line= "";
        while (msg.at(i) != '\n'){
            line =  line + msg.at(i);
            i++;
        }
        

        return line;

    }





