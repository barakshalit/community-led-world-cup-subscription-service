#include <Frame.h>
#include <iostream>







Frame::Frame(std::string message): currString(message),map() {

    mapStompCommand();
    if (currString.size() != 0){
         mapHeaders();
    }
    if (currString.size() != 0){
        mapBody();
    }
}





void Frame::mapStompCommand(){
    map.insert({"StompCommand",getNextLine()});
    
    
    
    

}
void Frame::mapHeaders(){
      std::string currentLine = getNextLine();
      
        while(currentLine != ""){
            std::vector<std::string> splitted = split(currentLine,':');
            
            map.insert({splitted[0],splitted[1]});
            currentLine = getNextLine();

            

        }

    
}
void Frame::mapBody(){

    map.insert({"FrameBody",currString});

    
}

std::string Frame::getNextLine(){
    int i = 0;
    std::string line= "";

    if(currString != ""){
        while (currString.at(i) != '\n'){

        line =  line + currString.at(i);
        i++;
        }

        currString = currString.substr(i+1);
        return line;

    }
    else{
        //currString = currString.substr(1);
        return "";
    }
    

    
}

std::vector<std::string> Frame::split (std::string x,char splitby){
    std::stringstream message(x);
    std::string segment;
    std::vector<std::string> seglist;

    while(std::getline(message, segment, splitby))
    {
        seglist.push_back(segment);
    }

    return seglist;

}


