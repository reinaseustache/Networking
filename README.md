# Networking

Projects

### Project 1
* client.java

This program implements a TCP client that allows a client behind a NAT box to dynamically request the forwarding of external ports. It prints the external port and IP address of the NAT box it receives back from the server if the mapping was successful.

### Project 2
* clientA.java
* serverB.java

After contacting the trusted authentication server on port 20002, clientA contacts serverB on TCP port 30003. Both the client and server will print a message indicating whether their connection is authentic and can be trusted based on information received from the authentication server who has the real keys. 

### Project 3
* udpClient.java

This is an active client that initiates a UDP connection on port 40004 and allows a client to change their key to a new one as long as they can provide the correct previous key.
