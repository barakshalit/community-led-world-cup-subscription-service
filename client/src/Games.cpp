#include <Games.h>



Games::Games(): currString(""),map(){};

void Games::MapEventToGame(std::string username, Event event){
    std::string teamA = event.get_team_a_name();
    std::string teamB = event.get_team_b_name();
    std::string gameName = teamA + "_" + teamB;



    if(map.count(username) == 0){ //this is the first report for this user
        std::map<std::string,std::list<Event>> gameToEventsMap;
        map.insert({username,gameToEventsMap}); //inserting user key to the map
        std::list<Event> eventList; //init list of all the events for the specific game
        eventList.insert(eventList.end(), event);//inserting the new Event to the end of the list
        map[username].insert({gameName,eventList});//maping the event in the user map

    }
    else{
        std::list<Event> eventList  = map[username][gameName];
        eventList.insert(eventList.end(),event);

    }
}




