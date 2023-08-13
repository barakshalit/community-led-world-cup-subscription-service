package bgu.spl.net.srv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import bgu.spl.net.api.Frame;

import bgu.spl.net.api.StompMessagingProtocol;

public class connectionsImpl<T> implements Connections<T> {

    public StompMessagingProtocol<T> protocol;
    public HashMap connectionIdToHandler; //maps server generated connectionId to its Handler

    public HashMap channelToConnectionId; //maps channel to list of all the connectionId's that are subscribed to this channel
    public HashMap ConnectionIdToChannel;

    public HashMap loginPasscode; //maps login username to passcode
    public HashMap loginConnectionId; //maps login username to clientId (to check if the user is connected)
    public HashMap connectionIdTologin; //maps login username to clientId (to check if the user is connected)
    
    public HashMap clientidToIdTopicMap; //
    public HashMap topicToChannelId; //



    public int connectionId ;
    public Integer messageId ;

    public connectionsImpl(StompMessagingProtocol<T> stompMessagingProtocol){
        this.connectionIdToHandler = new HashMap();
        this.channelToConnectionId = new HashMap<String,LinkedList<Integer>>();
        this.ConnectionIdToChannel = new HashMap<Integer,LinkedList<String>>();
        this.loginPasscode = new HashMap();
        this.loginConnectionId = new HashMap();
        this.connectionIdTologin = new HashMap<Integer,String>();
        this.clientidToIdTopicMap = new HashMap<Integer,HashMap<Integer,String>>();
        this.topicToChannelId = new HashMap<Integer,HashMap<String,Integer>>();
        

        this.protocol = stompMessagingProtocol;
        this.connectionId = 0;
        this.messageId = 0;
    }
    
    // maping connectionId to his handler in order for the send operation
    public boolean send(int connectionId, T msg){
        ConnectionHandler handler = (ConnectionHandler) connectionIdToHandler.get(connectionId);
        Frame frame = (Frame) msg;
        if (frame.map.get("StompCommand").equals("MESSAGE")){
            HashMap<String,Integer> topictoidmap =  (HashMap) topicToChannelId.get(connectionId);
            String chan=  (String)frame.map.get("destination");
            Integer uniqueID = topictoidmap.get(chan);
            System.out.println(uniqueID.toString());
            frame.map.put("subscribtion",uniqueID.toString());
            frame.map.put("message-id", messageId.toString());
            this.messageId ++;


        }

        handler.send(frame.toString());


        return true; //might change late?....


    }
    // maping subscribers of a channel to theirs handlers in order for the send operation
    public void send(String channel, T msg){
        List subscribers = new LinkedList<Integer>();
        subscribers = (LinkedList<Integer>) channelToConnectionId.get(channel);
        Iterator iter = subscribers.iterator();
        while(iter.hasNext()){
            //System.out.println(iter.next().);
            send((Integer) iter.next(),msg);
        }

    }

    public void disconnect(int connectionId){

        //disconnecting the client:
        try {
            ((ConnectionHandler) connectionIdToHandler.get(connectionId)).close();
            
        } catch (IOException ex ) {
        }

        //removing the client from all the hash maps:
        removeFromMaps(connectionId);

    }
    // add method for MapconnectionIdToHandler
    public int addToMapconnectionIdToHandler(ConnectionHandler<T> blockingConnectionHandler){
        connectionIdToHandler.put(connectionId, blockingConnectionHandler);
        connectionId++;

        return connectionId - 1 ;
    }
    // add method for MapconnectionIdToHandler
    public void addToMapchannelToConnectionId(String channel,int connectionId){
        if(channelToConnectionId.get(channel) == null){ // if the channel isnt exsist yet
            List subscribers = new LinkedList<Integer>();
            channelToConnectionId.put(channel, subscribers);
        }
        List subscribers = (LinkedList<Integer>) channelToConnectionId.get(channel);
        
        subscribers.add(connectionId);
        List temp = (LinkedList<Integer>) channelToConnectionId.get(channel);
       
        System.out.println("THE SUBSCRIBER INDEED IS:" +  temp.get(0));

       
    }

    public void removeFromMaps(Integer connectionId){
        //removing handler from connectionIdToHandler:
        connectionIdToHandler.remove(connectionId); 

        //removing all the client unique id's hashmap from clientidToIdTopicMap:
        ((HashMap)clientidToIdTopicMap).remove(connectionId); 

        //removing from connectionIdTologin and loginconnectionid maps:
        String username = (String) connectionIdTologin.get(connectionId);
        connectionIdTologin.remove(connectionId);//removing from the map
        loginConnectionId.remove(username);

        //removing the client from connectionidtochennel and channeltoconnectionid:
        LinkedList<String> channels = (LinkedList<String>) ConnectionIdToChannel.get(connectionId); //getting all the channels the client is subscribed to
        for (String channel : channels) {
            LinkedList<Integer> connectionsId = (LinkedList<Integer>) channelToConnectionId.get(channel); //getting all the channel subscribers from channelToConnectionId to remove the client
            int index = 0;
            for(Integer currConnectionId: connectionsId){

                if(currConnectionId == connectionId){ //we found our user
                    connectionsId.remove(index);

                }

                index ++;
            }

        }

        ConnectionIdToChannel.remove(connectionId);

    }

    
}
