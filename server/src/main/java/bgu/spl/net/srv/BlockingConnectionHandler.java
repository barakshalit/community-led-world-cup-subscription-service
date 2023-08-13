package bgu.spl.net.srv;

import bgu.spl.net.api.Frame;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocolimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final StompMessagingProtocolimpl<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    public int connectionId;
    public connectionsImpl<T> con;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, StompMessagingProtocolimpl<T> protocol, connectionsImpl<T> con) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol =  protocol;
        this.con = con;
        connectionId = this.con.addToMapconnectionIdToHandler(this);
        protocol.start(connectionId,con,this);
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream()); //msg from the client
            out = new BufferedOutputStream(sock.getOutputStream()); //msg to the client
            
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                

                if (nextMessage != null) {
                    //System.out.println("next message is" + nextMessage);
                    T response = protocol.process(nextMessage);
                    if (response != null) {
                        
                        out.write(encdec.encode(response));
                        out.flush();
                        //System.out.println("SERVER SENT MESSAGE FROM ITS SIDE");
                        //checking if the server sent an error message, and if so - close the connection:
                        if(((Frame)response).map.get("StompCommand") == "ERROR"){
                            System.out.println("SERVER got error while trying to send");

                            close();
                        }
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        try {
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
}