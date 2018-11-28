# ChatApp
A simple Java socket program based chat app  

### Get the project
1. Install Java if not present  
Check if Java was installed by: `java -version`  
If not:  
```
sudo apt update  
sudo apt install default-jdk  
```
> <b>Optional</b>  
> JVMs are located under `/usr/lib/jvm`. You can add a line under `/etc/environment` file:  
> `JAVA_HOME="/usr/lib/jvm/default-java"`  
> Also enter the command `source /etc/environment`  

2. Clone or download:  
```
mkdir -p ~/ChatApp  
cd ~/ChatApp  
git clone https://github.com/SayantanRC/ChatApp.git  
```

3. Compile  
```
javac ChatApp.java && javac ChatOperation.java && javac IncomingMessageTracker.java
```

4. Run
```
java ChatApp
```

### Usage

- On running the program, you will get a blank screen. To send messages or files, you need to open a connection by typing commands.  
  1. First you need to open a port for a client to connect to. Say you want to open a port 2500. Type:
  `<add:2500:nickname_client>` or simply `<add:2500>`  
  2. Now, you need to connect a client to this port. On another instance of the program and type:
  `<connect:2500:nickname_server>` or simply `<connect:2500>`  
  Now the two running instances can talk to each other.  
  
- You can connect as many instances as you want. One instance of the program can be a server for one or many instance, as well as client for some other instances. But a single port can only facilitate communication between a single server-client pair. For example, an instance can issue the following commands one after another:
```
<add:3000>  
<add:4000:client_nickname>  
<connect:3500:server_name>  
<add:5000>
<connect:6000>
```

- Connect to a server instance not running on the same machine:
  To connect to a server instance running on the network, we need IP address of the server. Say the server opens a port 1211, the IP address can be found by `<my-ip>` command.  
  ```
  <add:1211>  
  <my-ip>  
  ```
  This give a list network interface addresses. To connect to the server, use the following command:  
  `<connect:[PORT]> <ip_addr:[IP_ADDRESS]>`  
  Say the IP address of server is 192.168.0.107:
  ```
  <connect:1211> <ip_addr:192.168.0.107>
  ```
  or  
  ```
  <connect:1211:server_name> <ip_addr:192.168.0.107>  
  ```
  
- Disconnect a port:  
  To disconnect a connection over a port, use the `<disconnect:[PORT]>` command. Example, disconnect port 2500:
  ```
  <disconnect:2500>  
  ```
  
- Send message to a particular peer over a particular port:
  By default, if you are connected to several peers (independant of you being the server or the client) any message typed by you will go to all peers. To selectively send a message specify the port by `<port:[PORT]>` before the message. See the following example:  
  > Say I am connecting to ports 2100, 2200, 2300:  
  > ```
  > <connect:2200>  
  > <connect:2100>  
  > <connect:2300>  
  > ```
  > To send a global message, just type your message:
  > ```
  > Global message for all. Hello everyone!  
  > ```
  > To send a message to only port 2200  
  > ```
  > <port:2200> Hello port 2200!
  > ```

- Send a file: (Warning: you will not be able to send or receive messages during file transfer)
  To send a file to all your peers, use the `<file:[COMPLETE_FILE_PATH]>` command. Example:
  ```
  <file:/home/sayantan/cat.jpg>
  ```
  To send to a particular port, append `<port:[PORT]>` to the file command as done previously for sending normal text messages. Example:
  ```
  <port:3000> Sending ice cream to you!  
  <port:3000> <file:/home/sayantan/ice-cream.jpg>  
  ```
  
- Close all connections and exit the program:
  ```
  <exit>
  ```
