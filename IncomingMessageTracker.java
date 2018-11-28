
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author SayantanRC
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
        
        // method to track incoming messages from server
        String msg = "";
        while (true){
            DataInputStream din = new DataInputStream(socket.getInputStream());
            msg = din.readUTF();
            
            // in case message is null, continue the loop
            if (msg == null) continue;
            
            if (msg.startsWith("<file")){
                
                // message sent by server to initialise file transfer
                // message is of the format <file:[FILE_NAME]:[FILE_SIZE]>
                String fileName = msg.substring(msg.indexOf(":")+1, msg.lastIndexOf(":"));
                long fileSize = Long.parseLong(msg.substring(msg.lastIndexOf(":")+1, msg.lastIndexOf(">")));
                
                // method to start receiving incoming file
                receiveIncomingFile(fileName, fileSize);
            }
            else if (!msg.equals("<exit>")){
                
                // display a simple text message
                System.out.println(nickName + "(" + port + "): " + msg);
            }
            else {
                
                // <exit> message received. Close the connection.
                System.out.println(nickName + "(" + port + ") disconnected!");
                din.close();
                break;
            }
        }
        
        // loop ends only when connection is closed. Hence close the socket
        socket.close();
        
        // remove the chat from chat list
        try {
            ChatApp.removeChat(port);
        } catch (Exception ignored) {}
    }
    
    private void receiveIncomingFile(String fileName , long fileSize){
        
        // method to receive a file
        
        // set flag transfer flag
        isTransferingFile = true;
        File file = new File(fileName);
        
        try {
            
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
            
            // if file size is too small, set the buffer size equal to file size
            byte[] buffer = new byte[Math.min(4096, (int) fileSize)];
            long read, receivedAmount = 0, receivedPercent = 0;
            
            // read from socket input stream and write to file
            while ((read = bufferedInputStream.read(buffer)) > 0){
                bufferedOutputStream.write(buffer, 0, buffer.length);
                receivedAmount = receivedAmount + read;
                System.out.println("Received: " + (receivedPercent = (int)((receivedAmount * 100.0)/fileSize)) + "%");
                
                // break if received percentage reaches 100
                if (receivedPercent >= 100)
                    break;
            }
            
            // close the output stream
            bufferedOutputStream.close();
            System.out.println("Received file at: " + file.getAbsolutePath());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("File receive failed!");
        }
        
        // set flag transfer flag
        isTransferingFile = false;
    }
}
