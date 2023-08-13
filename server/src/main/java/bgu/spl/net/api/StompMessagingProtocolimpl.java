package bgu.spl.net.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.connectionsImpl;

public class StompMessagingProtocolimpl<T> implements StompMessagingProtocol<T> {

    public connectionsImpl<T> con;
    public int connectionId;
    public ConnectionHandler handler;
    public boolean terminate = false;
    

    /**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    public void start(int connectionId, Connections<T> connections, ConnectionHandler _handler){
        con = (connectionsImpl) connections;
        this.connectionId = connectionId;
        this.handler = _handler;
        
    }
    // should use this connectionid insted the connectionid we generated in connections

    
    public T process(T message){
        Frame frameToRead = (Frame) message;
        String respons;
        Frame FrameToRespons;
        //System.out.println("IM IN PROTOCOLL");


        //CONNECT
        if(frameToRead.map.get("StompCommand") .equals("CONNECT")){
            //checking if the user already created in the past:
            //System.out.println("I GOT TO CONNECTED IN PROTOCOL");
            //System.out.println("inside connect server");
           

                switch(checkValid(frameToRead)){
                    case 1: //client already connected
    
                    respons = "ERROR" + '\n';

                    if(frameToRead.map.get("receipt") != null){
                        respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                    }

                    respons += "message:" + "Client is already logged in" + '\n';
                    respons += "The Massge" + '\n' + "-----";
                    respons += message.toString() + '\n';
                    respons += "-----" + '\n';
                    respons += "Cant connect already connected user";

                    FrameToRespons = new Frame(respons);
                    terminate = true;
                    
                    return (T) FrameToRespons;

                    case 2://login did not match passcode
                    respons = "ERROR" + '\n';

                    if(frameToRead.map.get("receipt") != null){
                        respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                    }

                    respons += "message:" + "Wrong Password" + '\n';
                    respons += "The Massge" + '\n' + "-----";
                    respons += message.toString() + '\n';
                    respons += "-----" + '\n';
                    respons += "The login did not matched the passcode";

                    FrameToRespons = new Frame(respons);

                    //close connection?
                     terminate = true;

                    return (T) FrameToRespons;


                    case 0: // all good

                    //mapping the connection id to its handler
                    con.connectionIdToHandler.put(connectionId,handler);
                    
                    if(con.loginPasscode.get(frameToRead.map.get("login")) != null){
                    

                        con.loginConnectionId.put(frameToRead.map.get("login"), connectionId);                   
                        respons = "CONNECTED" + '\n' + "version:1.2" + '\n' + '\n';
                        Frame responsFrame = new Frame(respons);
                        return (T) responsFrame;


                    }
                    else{ //needs to create new user
                        
                        con.loginConnectionId.put(frameToRead.map.get("login"), connectionId) ;
                        con.connectionIdTologin.put(connectionId, frameToRead.map.get("login"));

                        con.loginPasscode.put(frameToRead.map.get("login"), frameToRead.map.get("passcode"));
                        respons = "CONNECTED" + '\n';
                        respons += "version:1.2" + '\n';
                        respons += '\n';
                        Frame responsFrame = new Frame(respons);
                        return (T) responsFrame;
        
    
                    }

                        

            }
            
        }

        // SEND
        if(frameToRead.map.get("StompCommand") .equals("SEND")){
            switch(checkValid(frameToRead)){
                case 3: //no destination
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }

                respons += "message:" + "No destination header" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "the client did not add a destination header to the frame";

                FrameToRespons = new Frame(respons);
                 terminate = true;

                return (T) FrameToRespons;

                case 4: //client does not subscribed to the channel
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }

                respons += "message:" + "The client is not subscribed to the channel" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "The client is not subscribed to the channel";

                FrameToRespons = new Frame(respons);
                 terminate = true;

                return (T) FrameToRespons;

                case 0:
                String channel = (String) frameToRead.map.get("destination");
                //String channel = channel.substring(1); // see if thats the way to do it
                Frame toReturn = new Frame("");
                toReturn.map.put("StompCommand","MESSAGE");
                HashMap<String,Integer> temp = (HashMap<String,Integer>) con.topicToChannelId.get(connectionId);
                toReturn.map.put("subscription",temp.get(channel).toString());
                toReturn.map.put("message-id",con.messageId.toString());
                con.messageId ++;

                toReturn.map.put("destination",frameToRead.map.get("destination"));
                toReturn.map.put("FrameBody",frameToRead.map.get("FrameBody"));

                //System.out.println("the actual frame body :" + frameToRead.map.get("FrameBody"));
                
                System.out.println("frame tostring send:\n" + toReturn.toString() );
                con.send(channel, (T) toReturn);

                if(frameToRead.map.get("receipt") != null){
                    respons = "RECEIPT" + '\n';
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                    FrameToRespons = new Frame(respons);
                    return (T) FrameToRespons;

                }




            }



        }
        // SUBSCRIBE
        if(frameToRead.map.get("StompCommand").equals("SUBSCRIBE") ){
            switch(checkValid(frameToRead)){
                case 5: //no destination
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }

                respons += "message:" + "No destination header" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "the client did not add a destination header to the frame";

                FrameToRespons = new Frame(respons);
                terminate = true;

                return (T) FrameToRespons;

                case 6: //client does not subscribed to the channel
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }
 
                respons += "message:" + "The client is not subscribed to the channel" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "The client is not subscribed to the channel";

                FrameToRespons = new Frame(respons);
                terminate = true;

                return (T) FrameToRespons;

                

                case 7:
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }
 
                respons += "message:" + "The client did not send id header" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "The client did not send id header";

                FrameToRespons = new Frame(respons);
                terminate = true;

                return (T) FrameToRespons;

                case 0:
                String Stringid = (String) frameToRead.map.get("id");
                Integer id = Integer.valueOf(Stringid);

                String channel = (String) frameToRead.map.get("destination");
                //String channel = channel.substring(1); // see if thats the way to do it

                con.addToMapchannelToConnectionId(channel, connectionId);
                if(con.ConnectionIdToChannel.get(connectionId) ==null){
                    List channels = new LinkedList<String>();
                    channels.add(channel);
                    con.ConnectionIdToChannel.put(connectionId, channels);
                    

                }
                else{
                    List channels = (LinkedList<String>) con.ConnectionIdToChannel.get(connectionId);
                    channels.add(channel);

                }
                if(con.clientidToIdTopicMap.get(connectionId) == null){
                    HashMap idToTopicMap = new HashMap();
                    idToTopicMap.put(id, channel);
                    con.clientidToIdTopicMap.put(connectionId,idToTopicMap);

                    HashMap topicToIdMap = new HashMap();
                    topicToIdMap.put(channel,id);
                    con.topicToChannelId.put(connectionId,topicToIdMap);

                    
                }
                else{
                    ((HashMap) con.clientidToIdTopicMap.get(connectionId)).put(id,channel);

                }
                

                if(frameToRead.map.get("receipt") != null){
                    respons = "RECEIPT" + '\n';
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                    respons += '\n';
                    FrameToRespons = new Frame(respons);
                    //System.out.println("FRAME FROM THE SERVER:" + FrameToRespons.toString());
                    return (T) FrameToRespons;

                }
               



            }



        }

        if(frameToRead.map.get("StompCommand") .equals("UNSUBSCRIBE") ){
            

            switch(checkValid(frameToRead)){
                case 8: //no id
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }

                respons += "message:" + "No id header" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "the client did not add a id header to the frame";

                FrameToRespons = new Frame(respons);
                terminate = true;

                return (T) FrameToRespons;

                case 9: //client does not subscribed to the channel
                respons = "ERROR" + '\n';

                if(frameToRead.map.get("receipt") != null){
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                }
 
                respons += "message:" + "The client is not subscribed to the channel" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "The client is not subscribed to the channel";

                FrameToRespons = new Frame(respons);
                terminate = true;

                return (T) FrameToRespons;

                case 0:
                String Stringid = (String) frameToRead.map.get("id");
                Integer id = Integer.valueOf(Stringid);
                HashMap<Integer,String> temp = (HashMap<Integer,String>)con.clientidToIdTopicMap.get(connectionId);
                String channel = temp.get(id); //(String) frameToRead.map.get("destination");
                //String channel = Stringid.substring(1); // see if thats the way to do it
                LinkedList<Integer> subscribers = (LinkedList<Integer>) con.channelToConnectionId.get(channel);

                Iterator iterator = subscribers.iterator();
                int index = 0;
                while(iterator.hasNext()){
                    if(((Integer) iterator.next()) == connectionId){
                        subscribers.remove(index);

                    }
                    index++;
                }
                //((HashMap)con.clientidToIdTopicMap.get(connectionId)).remove(id);

            

                if(frameToRead.map.get("receipt") != null){
                    respons = "RECEIPT" + '\n';
                    respons = respons + "receipt-id:" + frameToRead.map.get("receipt") + '\n';
                    respons += '\n';
                    FrameToRespons = new Frame(respons);
                    //System.out.println(FrameToRespons.toString());
                    return (T) FrameToRespons;

                }

               


            }



        }
        if(frameToRead.map.get("StompCommand") .equals("DISCONNECT") ){
            switch(checkValid(frameToRead)){

                case 10://no receipt
                respons = "ERROR" + '\n';
 
                respons += "message:" + "The client is not send a receipt id when asked to disconnect" + '\n';
                respons += "The Massge" + '\n' + "-----";
                respons += message.toString() + '\n';
                respons += "-----" + '\n';
                respons += "message:" + "The client is not send a receipt id when asked to disconnect";

                FrameToRespons = new Frame(respons);
                terminate = true;

                return (T) FrameToRespons;
                

                case 0:
                //connectiong all the 
                con.disconnect(connectionId);


            }

        }




        //no response from the server
        return null;  

    }




    /*
    0 - all good
    CONNECTED:
    1 - client already connected
    2 - incorrect passcode
    SEND:
    3 - no destination header
    4 - the client is not subscribed to the channel
    SUBSCRIBE:
    5 - no destination header
    6 - the client is not subscribed to the channel
    7 - no id header
    UNSUBSCRIBE:
    8 - no id header
    9 - asked for unsub when client is not subbed
    DISCONNECT:
    10 - no receipt id
    */

    public int checkValid(Frame message){

        //CONNECT - checks for incorrect login - passcode or another client trying to connect with existing login
        Frame frameToRead = (Frame) message;
        //System.out.println("im inside checkvalid");

        if(frameToRead.map.get("StompCommand").equals("CONNECT") ){
            //System.out.println("im inside checkvalid CONNECT");
            //Checking if username + passcode is correct
            if(con.loginPasscode.get(frameToRead.map.get("login")) != null){ //user is already created
                if(con.connectionIdToHandler.get(con.loginConnectionId.get(frameToRead.map.get("login"))) != null){ //client is already connected (has active handler)
                    return 1;
                }
                if(con.loginPasscode.get(frameToRead.map.get("login")) != frameToRead.map.get("passcode")){ //passcode is incorrect
                    return 2;
                }



            }

            //client already connected
            
        }
        if(frameToRead.map.get("StompCommand").equals("SEND") ){
            //no destination header
            if(message.map.get("destination") == null){
                return 3;

            }
            else{
                if(con.channelToConnectionId.get(frameToRead.map.get("destination")) != null){
                LinkedList<Integer> subscribers = (LinkedList<Integer>) con.channelToConnectionId.get(frameToRead.map.get("destination"));
                //List subscribers = (List) con.channelToConnectionId.get(frameToRead.map.get("destination"));
                boolean exist = false;
                Iterator iter = subscribers.iterator();
                while(iter.hasNext() & !exist){
                    if(((Integer) iter.next()) == this.connectionId){
                        exist = true;
                    }
                }
                
                if(!exist){
                    return 4;
                }

                }
                
            }
           
        }
        if(frameToRead.map.get("StompCommand") .equals("SUBSCRIBE") ){
            //no destination header
            if(message.map.get("destination") == null){
                return 5;

            }
            // else{
            //     if(con.channelToConnectionId.get(frameToRead.map.get("destination")) != null){
            //         //System.out.println("destination is + " + frameToRead.map.get("destination") );
            //         LinkedList<Integer> subscribers = (LinkedList<Integer>) con.channelToConnectionId.get(frameToRead.map.get("destination"));
            //         boolean exist = false;
            //         //System.out.println(subscribers==null);
            //         Iterator iter = subscribers.iterator();
            //         while(iter.hasNext() & !exist){
            //             if(((Integer) iter.next()) == this.connectionId){
            //                 //System.out.println();
            //                 exist = true;
            //             }
            //         }
                    
            //         if(!exist){
            //             return 6;
            //         }
            //     }
              
               
            // }

            if(message.map.get("id") == null){
                return 7;
    
            }
            

            
            
           
        }

        if(frameToRead.map.get("StompCommand") .equals("UNSUBSCRIBE")){
            if(frameToRead.map.get("id") == null){
                return 8;
            }

            Integer idTEST = Integer.valueOf((String) frameToRead.map.get("id"));
            HashMap<Integer,HashMap<Integer,String>> temp =(HashMap<Integer,HashMap<Integer,String>>) con.clientidToIdTopicMap.get(connectionId);
            if(temp.get(idTEST) == null){ //checking if the client is not subscribed to the id
                return 9;
            }

        }

        if(frameToRead.map.get("StompCommand").equals("DISCONNECT") ){
            if(frameToRead.map.get("receipt") == null){
                return 10;
            }
            

        }

        

        return 0;
    }
	
	/**
     * @return true if the connection should be terminated
     */
    public boolean shouldTerminate(){

        return terminate;
    }



    
}
