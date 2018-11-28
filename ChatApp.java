
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author SayantanRC
 */

public class ChatApp {
    
    static char type = 's';
    
    // List of chats. Chat with as many as you want!
    static ArrayList<ChatOperation> chats = new ArrayList<>(0);
    
    public static void main(String[] args) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        
        while (true){
           
            // constantly listen to keyboard
            
            String cmd;
            try {
                cmd = br.readLine();
                
                if (cmd.startsWith("<add:") || cmd.startsWith("<connect:")){
                    
                    // new chat connection
                    try {
                        String ip_addr = "localhost";
                        if (cmd.contains("<ip_addr:")){
                            
                            // if IP address is present in the format <port:[PORT]> <ip_addr:[IP_ADDRESS]>
                            ip_addr = cmd.substring(cmd.lastIndexOf(":")+1, cmd.lastIndexOf(">"));
                            
                            // remove the IP address part
                            cmd = cmd.substring(0, cmd.lastIndexOf("<")).trim();
                        }
                        
                        // cmd is in format <port:[PORT]> or <port:[PORT]:[NICKNAME]>
                        
                        String pnString = cmd.substring(cmd.indexOf(":") + 1, cmd.lastIndexOf(">"));
                        // pnString is in format [PORT] or [PORT]:[NICKNAME]
                        
                        String nickName = "";
                        if (pnString.contains(":")){
                            
                            // for second case with NICKNAME
                            nickName = pnString.substring(pnString.indexOf(":")+1);
                            
                            // pnString is now in format [PORT]
                            pnString = pnString.substring(0, pnString.indexOf(":"));
                        }
                        int port = Integer.parseInt(pnString);
                        
                        // connection type is server if "add", client if "connect"
                        if (cmd.startsWith("<add:")) type = 's';
                        else type = 'c';
                    
                        ChatOperation chat = new ChatOperation(port, ip_addr, type, nickName);
                    
                        // add to chat list
                        chats.add(chat);
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    
                }
                else if (cmd.startsWith("<disconnect:")){
                    
                    // disconnect a PORT
                    int disconnectingPort = -1;
                    
                    // cmd is in format <disconnect:[PORT]>
                    try {
                        String pString = cmd.substring(cmd.indexOf(":") + 1, cmd.lastIndexOf(">"));
                        disconnectingPort = Integer.parseInt(pString);
                        
                        // call function to disconnect PORT
                        disconnectChat(disconnectingPort);
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
                else if (cmd.equals("<my-ip>")){
                    
                    // list all IP addresses from all Network Interfaces
                    Enumeration e = NetworkInterface.getNetworkInterfaces();
                    while(e.hasMoreElements())
                    {
                        NetworkInterface n = (NetworkInterface) e.nextElement();
                        Enumeration ee = n.getInetAddresses();
                        while (ee.hasMoreElements())
                        {
                            InetAddress i = (InetAddress) ee.nextElement();
                            System.out.println(i.getHostAddress());
                        }
                    }
                }
                else if (cmd.equals("<exit>")){
                    // close the program
                    closeAllChats();
                }
                else {
                    
                    // block to execute to send a text message or a file.
                    int port = -1;  // port = -1 means a global message
                    if (cmd.startsWith("<port:")){
                        
                        // a specific port has been mentioned.
                        // cmd is in format <port:[PORT]> [MESSAGE]
                        try {
                            
                            // extract the PORT
                            port = Integer.parseInt(cmd.substring(cmd.indexOf(":")+1, cmd.indexOf(">")).trim());
                            
                            // cmd now only has the text message
                            cmd = cmd.substring(cmd.indexOf(">") + 1).trim();
                        }
                        catch (Exception e){
                            
                            System.out.println(e.getMessage());
                            
                            // in case PORT could not be extracted, continue the loop and dont send a global message
                            continue;
                        }
                    }
                    
                    if (cmd.startsWith("<file:")){
                        
                        // [MESSAGE] is to send a file.
                        // cmd = [MESSAGE] is in format <file:[FULL_FILE_PATH]>
                        String filePath = cmd.substring(cmd.indexOf(":")+1, cmd.lastIndexOf(">"));
                        publishFile(filePath, port);
                    }
                    else {
                        
                        // if not a file, send a text message
                        publishMessage(cmd, port);
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            
        }
            
    }
    
    static void disconnectChat(int port){
        
        // disconnect a PORT
        for (ChatOperation chat : chats){
            if (chat.port == port) {
                if (chat.socket != null) {
                    System.out.println("Disconnected: " + chat.nickName + "(" + chat.port + ")");
                    chat.close();
                }
                else {
                    
                    // use this field if client has not yet connected to server.
                    // whenever this field is set and a client tries to connect to the port, connection is dropped.
                    chat.preMatureDisconnect = true;
                    
                    // remove chat 
                    chats.remove(chat);
                }
                break;
            }
        }
    }
    
    static void closeAllChats(){
        
        // similar to disconnectChat(). Closes all connections.
        try {
            while (!chats.isEmpty()){
                ChatOperation chat = chats.get(0);
                
                if (chat.socket != null) {
                    System.out.println("Disconnected: " + chat.nickName + "(" + chat.port + ")");
                    chat.close();
                }
                else {
                    chat.preMatureDisconnect = true;
                    chats.remove(chat);
                }
                
            }
        }
        catch (Exception e){}
        
        // also exit the program.
        System.exit(0);
    }
    
    static void publishFile(String filePath, int port){
        
        // method to send a file
        boolean nonNull = true, nonPort = true;
        for (ChatOperation chat : chats){
            if (chat != null && chat.socket != null) {
                nonNull = false;
                if (port == -1){
                    
                    // default port -1 means a global file
                    chat.sendFile(filePath);
                    nonPort = false;
                }
                else if (chat.port == port){
                    
                    // send file to a specific port
                    chat.sendFile(filePath);
                    nonPort = false;
                    break;
                }
            }
        }
        if (nonNull)
            System.out.println("No peer connected!");
        else if (nonPort)
            System.out.println("No such port!");
    }
    
    static void publishMessage(String msg, int port){
        
        // method to send a text message
        boolean nonNull = true, nonPort = true;
        for (ChatOperation chat : chats){
            if (chat != null && chat.socket != null && !chat.isTransferingFile) {
                nonNull = false;
                if (port == -1){
                    chat.sendMessage(msg);
                    nonPort = false;
                }
                else if (chat.port == port){
                    chat.sendMessage(msg);
                    nonPort = false;
                    break;
                }
            }
        }
        if (nonNull)
            System.out.println("No peer connected!");
        else if (nonPort)
            System.out.println("No such port!");
    }
    
    static void removeChat(int port){
        
        // method to remove a specific chat from chat list
        for (ChatOperation chat : chats) {
            if (chat.port == port){
                chats.remove(chat);
                break;
            }
        }
    }

}

