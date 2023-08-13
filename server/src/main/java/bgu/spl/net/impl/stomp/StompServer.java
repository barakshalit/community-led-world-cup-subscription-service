package bgu.spl.net.impl.stomp;

import java.util.Arrays;

import bgu.spl.net.api.Frame;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.StompMessagingProtocolimpl;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if(args.length == 0){
            Server<String> server = Server.threadPerClient(7777, () -> new StompMessagingProtocolimpl(), () -> new MessageEncoderDecoderImpl());
            server.serve();
        }

        int port = Integer.decode(args[0]).intValue();

        if(args[1].equals("tpc")){
            Server<String> server = Server.threadPerClient(port, () -> new StompMessagingProtocolimpl(), () -> new MessageEncoderDecoderImpl());
            server.serve();
            
        }
        if(args[1].equals("reactor")){
            Server<String> server = Server.reactor(3,port, () -> new StompMessagingProtocolimpl(), () -> new MessageEncoderDecoderImpl());
            server.serve();
            
        }
        



        }
       
      
        
        




        


    }


   

    

