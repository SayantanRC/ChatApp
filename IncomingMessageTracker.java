
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class IncomingMessageTracker {
    
    Socket socket;
    int port;
    String nickName;
    Boolean isTransferingFile;
    
    public IncomingMessageTracker(Socket socket, int port, String nickName, Boolean isTransferingFile) throws IOException{
        this.socket = socket;
        this.port = port;
        this.nickName = nickName;
        this.isTransferingFile = isTransferingFile;
        trackIncomingMessage();
    }
    
    private void trackIncomingMessage() throws IOException{
        
        String msg = "";
        while (true){
            DataInputStream din = new DataInputStream(socket.getInputStream());
            msg = din.readUTF();
            if (msg == null) continue;
            if (msg.startsWith("<file")){
                String fileName = msg.substring(msg.indexOf(":")+1, msg.lastIndexOf(":"));
                long fileSize = Long.parseLong(msg.substring(msg.lastIndexOf(":")+1, msg.lastIndexOf(">")));
                receiveIncomingFile(fileName, fileSize);
            }
            else if (!msg.equals("<exit>")){
                System.out.println(nickName + "(" + port + "): " + msg);
            }
            else {
                System.out.println(nickName + "(" + port + ") disconnected!");
                din.close();
                break;
            }
        }
        socket.close();
        
        try {
            ChatApp.removeChat(port);
        } catch (Exception ignored) {}
    }
    
    private void receiveIncomingFile(String fileName , long fileSize){
        isTransferingFile = true;
        File file = new File(fileName);
        
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
            byte[] buffer = new byte[Math.min(4096, (int) fileSize)];
            long read, receivedAmount = 0, receivedPercent = 0;
            while ((read = bufferedInputStream.read(buffer)) > 0){
                bufferedOutputStream.write(buffer, 0, buffer.length);
                receivedAmount = receivedAmount + read;
                System.out.println("Received: " + (receivedPercent = (int)((receivedAmount * 100.0)/fileSize)) + "%");
                if (receivedPercent >= 100)
                    break;
            }
            bufferedOutputStream.close();
            System.out.println("Received file at: " + file.getAbsolutePath());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("File receive failed!");
        }
        isTransferingFile = false;
    }
}
