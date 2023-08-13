package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.Frame;

public class tests {
    public static void main(String[] args) {

        String msg = "SEND\ndestination:/topics/a\n\nHello Topic A!\n\u0000";
        Frame frame = new Frame(msg);
        System.out.println(frame.map.get("destination"));
        
        
    }
    
}
