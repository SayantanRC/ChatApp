
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    Boolean isTransferingFile = false;
    
    public ChatOperation(int port, String ip_addr, char type, String nickName){
        this.ip_addr = ip_addr;
        this.port = port;
        this.nickName = nickName;
        if (type == 's'){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerSocket serverSocket = new ServerSocket(port);
                        System.out.println("Waiting at port: " + port);
                        socket = serverSocket.accept();
                        startTracker();
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
        
        Thread fileSendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                if (file.canRead()){
                isTransferingFile = true;
                byte[] buffer = new byte[4096];
                long read, sentAmount = 0, totalSize = file.length();
                sendMessage("<file:" + file.getName()+":" + totalSize + ">");
                try {
                    System.out.println("Sending file: " + file.getName());
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                    OutputStream outputStream = socket.getOutputStream();
                    while ((read = bufferedInputStream.read(buffer)) > 0){
                        outputStream.write(buffer, 0, buffer.length);
                        sentAmount = sentAmount + read;
                        System.out.println("Sent: " + (int)((sentAmount * 100.0)/totalSize) + "%");
                    }
                    bufferedInputStream.close();
                    System.out.println("File sent!");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        System.out.println("File send failed!");
                    }
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
        sendMessage("<exit>");
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
        try {
            ChatApp.removeChat(port);
        } catch (Exception ignored) {}
    }
    
}
