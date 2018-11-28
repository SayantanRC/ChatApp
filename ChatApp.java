
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iemcse
 */

public class ChatApp {
    
    static char type = 's';
    
    static ArrayList<ChatOperation> chats = new ArrayList<>(0);
    
    //static ServerSocket serverSocket;
    //static ChatOperations chatOperations = null;
    
    public static void main(String[] args) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        
        while (true){
            
            String cmd;
            try {
                cmd = br.readLine();
                if (cmd.startsWith("<add:") || cmd.startsWith("<connect:")){
                    try {
                        String ip_addr = "localhost";
                        if (cmd.contains("<ip_addr:")){
                            ip_addr = cmd.substring(cmd.lastIndexOf(":")+1, cmd.lastIndexOf(">"));
                            cmd = cmd.substring(0, cmd.lastIndexOf("<")).trim();
                        }
                        String pnString = cmd.substring(cmd.indexOf(":") + 1, cmd.lastIndexOf(">"));
                        String nickName = "";
                        if (pnString.contains(":")){
                            nickName = pnString.substring(pnString.indexOf(":")+1);
                            pnString = pnString.substring(0, pnString.indexOf(":"));
                        }
                        int port = Integer.parseInt(pnString);
                        if (cmd.startsWith("<add:")) type = 's';
                        else type = 'c';
                    
                        ChatOperation chat = new ChatOperation(port, ip_addr, type, nickName);
                    
                        chats.add(chat);
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    
                }
                else if (cmd.startsWith("<disconnect:")){
                    int disconnectingPort = -1;
                    try {
                        String pString = cmd.substring(cmd.indexOf(":") + 1, cmd.lastIndexOf(">"));
                        disconnectingPort = Integer.parseInt(pString);
                        disconnectChat(disconnectingPort);
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
                else if (cmd.equals("<my-ip>")){
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
                    closeAllChats();
                }
                else {
                    int port = -1;
                    if (cmd.startsWith("<port:")){
                        try {
                            port = Integer.parseInt(cmd.substring(cmd.indexOf(":")+1, cmd.indexOf(">")).trim());
                            cmd = cmd.substring(cmd.indexOf(">") + 1).trim();
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                            continue;
                        }
                    }
                    
                    if (cmd.startsWith("<file:")){
                        String fileName = cmd.substring(cmd.indexOf(":")+1, cmd.lastIndexOf(">"));
                        publishFile(fileName, port);
                    }
                    else {
                        publishMessage(cmd, port);
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            
        }
            
    }
    
    static void disconnectChat(int port){
        for (ChatOperation chat : chats){
            if (chat.port == port) {
                if (chat.socket != null) {
                    System.out.println("Disconnected: " + chat.nickName + "(" + chat.port + ")");
                    chat.close();
                }
                else {
                    chat.preMatureDisconnect = true;
                }
                break;
            }
        }
    }
    
    static void closeAllChats(){
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
        System.exit(0);
    }
    
    static void publishFile(String filePath, int port){
        boolean nonNull = true, nonPort = true;
        for (ChatOperation chat : chats){
            if (chat != null && chat.socket != null) {
                nonNull = false;
                if (port == -1){
                    chat.sendFile(filePath);
                    nonPort = false;
                }
                else if (chat.port == port){
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
        for (ChatOperation chat : chats) {
            if (chat.port == port){
                chats.remove(chat);
                break;
            }
        }
    }

}

