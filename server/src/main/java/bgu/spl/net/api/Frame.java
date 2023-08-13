package bgu.spl.net.api;

import java.util.HashMap;

import javax.print.DocFlavor.STRING;

public class Frame {

    public String originalStrg;
    public String currString;
    public HashMap map; 
     
    
    public Frame (String result){
        this.originalStrg = result; //keeping an original copy of the string for now, might delete later...
        this.currString = originalStrg;
        this.map = new HashMap();
        mapMsgtoFrame();
      

    }
    

    public void mapMsgtoFrame(){
        if(this.currString != ""){
            mapCommand();
            mapHeaders();
            mapBody();
        }
       
 
    }

    public void mapCommand(){
        //System.out.println(currString);
        
        map.put("StompCommand",getNextLine());
        //return getNextLine(currString);

        
    }
    public void mapHeaders(){
        
        String currentLine = getNextLine();
        //System.out.println(currentLine);
        //System.out.println("IM IN MAP HEADERS and curr string is: " + currString);
        while(currentLine != ""){
            //System.out.println(currentLine);
            String [] splitted = currentLine.split(":");
            
            map.put(splitted[0],splitted[1]);
            currentLine = getNextLine();
            

        } 
        //return currentLine;

    }

    public void mapBody(){
        map.put("FrameBody",this.currString);
    }



    public String getNextLine(){
        int i = 0;
        String line= "";
        while (this.currString.charAt(i) != '\n'){
            line =  line + this.currString.charAt(i);
            i++;
        }
        this.currString = this.currString.substring(i+1);

        return line;

    }


    public String toString(){
        String toReturn = "";
       

        if(this.map.get("StompCommand").equals("CONNECTED")){
            toReturn = toReturn + (String) this.map.get("StompCommand") + '\n';
            toReturn = toReturn + "version:" + (String) this.map.get("version") + '\n';
            toReturn = toReturn + '\n';

        }

        if(this.map.get("StompCommand").equals("MESSAGE")){
            toReturn = toReturn + (String) this.map.get("StompCommand") + '\n';
            toReturn = toReturn + addReceipt();
            toReturn = toReturn + "subscription:" + (String) this.map.get("subscription") + '\n';
            System.out.println(this.map.get("message-id").toString());
            toReturn = toReturn + "message-id:" + (String) this.map.get("message-id") + '\n';
            toReturn = toReturn +  "destination:" +(String) this.map.get("destination") + '\n';
            toReturn = toReturn + '\n';
            toReturn = toReturn + (String) this.map.get("FrameBody") + '\n';

            
        }

        if(this.map.get("StompCommand").equals("RECEIPT")){
            toReturn = toReturn + (String) this.map.get("StompCommand") + '\n';
            toReturn = toReturn + addReceipt();
            toReturn = toReturn + '\n';
            
        }


        if(this.map.get("StompCommand").equals("ERROR") ){
            toReturn = toReturn + (String) this.map.get("StompCommand") + '\n';
            toReturn = toReturn + addReceipt();
            toReturn = toReturn + (String) this.map.get("message") + '\n';
            toReturn = toReturn + "The Message:" + '\n';
            toReturn = toReturn + "-----" + '\n';
            toReturn = toReturn + (String) this.map.get("clientError") + '\n';
            toReturn = toReturn + "-----" + '\n';
            toReturn = toReturn + (String) this.map.get("reason") + '\n';

        }


       
        return toReturn;

    }

    public String addReceipt(){
        if(this.map.get("receipt-id") != null){
            return "receipt-id:" + (String) this.map.get("receipt-id") + '\n';

        }
        return "";
    }


}
