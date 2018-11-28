
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sayantan
 */
public class ChatOperation {
    
    int port;
    String ip_addr;
    String nickName = "";
    Socket socket = null;
    
    boolean preMatureDisconnect = false;
    
    // this flag, when set will prevent transfer of text messages during file transfer
    // this is of object of Boolean type so that it can be set from different threads like the incoming message tracker
    Boolean isTransferingFile = false;
    
    public ChatOperation(int port, String ip_addr, char type, String nickName){
        this.ip_addr = ip_addr;
        this.port = port;
        this.nickName = nickName;
        
        if (type == 's'){
            // this connection is a server.
            // run a new thread to wait for a client to connect to this server.
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerSocket serverSocket = new ServerSocket(port);
                        System.out.println("Waiting at port: " + port);
                        socket = serverSocket.accept();
                        
                        // start tracking for incoming text messages or file.
                        startTracker();
                        
                        // close the connection as soon as client connects, if preMatureDisconnect is set.
                        if (preMatureDisconnect){
                            close();
                        }
                        else {
                            System.out.println("Connected at port: " + port);
                        }
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });
            thread.start();
        }
        else {
            // this connection is a client.
            try {
                socket = new Socket(ip_addr, port);
                startTracker();
                System.out.println("Connected at port: " + port);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        
        
    }
    
    private void startTracker(){
        
        // run a thread to listen for incoming messages and files
        Thread tracker = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                IncomingMessageTracker incomingMessageTracker = new IncomingMessageTracker(socket, port, nickName, isTransferingFile);
            } catch (IOException ignored) {}
            }
        });
        tracker.start();
    }
    
    void sendFile(String filePath){
        
        // thread to send file over socket
        Thread fileSendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                
                File file = new File(filePath);
                if (file.canRead()){
                    
                    // set file transfer flag to prevent text messages
                    isTransferingFile = true;
                    byte[] buffer = new byte[4096];
                    long read, sentAmount = 0, totalSize = file.length();
                
                    // send a message to client to initialise it to receive a file
                    // this message is of the format <file:[FILE_NAME]:[FILE_SIZE]>
                    sendMessage("<file:" + file.getName()+":" + totalSize + ">");
                
                    try {
                        
                        System.out.println("Sending file: " + file.getName());
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                        OutputStream outputStream = socket.getOutputStream();
                        
                        // read from input stream and write to output stream of socket
                        while ((read = bufferedInputStream.read(buffer)) > 0){
                            outputStream.write(buffer, 0, buffer.length);
                            sentAmount = sentAmount + read;
                            System.out.println("Sent: " + (int)((sentAmount * 100.0)/totalSize) + "%");
                        }
                    
                        // close the input stream after file is sent
                        bufferedInputStream.close();
                        System.out.println("File sent!");
                    
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        System.out.println("File send failed!");
                    }
                    
                    // unset file transfer flag
                    isTransferingFile = false;
                }
                else {
                    System.out.println("Cannot read file: " + file.getName());
                }
            }
        });
        fileSendingThread.start();
    }
    
    void sendMessage(String msg){
        
        // method to send a text essage
        if (socket == null && !msg.equals("<exit>")){
            System.out.println("Connection at port: " + port + " is not ready.");
        }
        else {
            DataOutputStream dos;
            try {
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException ex) {
                if (!msg.equals("<exit>")) System.out.println(ex.getMessage());
            }
        }
    }
    
    void close(){
        
        // send message to client to close the connection from clients side.
        sendMessage("<exit>");
        
        // close connection from this server side
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
        // remove the chat from chat list
        try {
            ChatApp.removeChat(port);
        } catch (Exception ignored) {}
    }
    
}
