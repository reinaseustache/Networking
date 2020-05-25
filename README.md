# Networking

Projects

### Project 1
* client.java

This program implements a TCP client that allows a client behind a NAT box to dynamically request the forwarding of external ports. It prints the external port and IP address of the NAT box it receives back from the server if the mapping was successful. It accepts the following information: argv[0] is server ip addr, argv[1] is server port, argv[2] is internal client host ip argv[3] is internal host port, argv[4] is requested port, argv[5] is protocol 6 or 17.

### Project 2
* clientA.java
* serverB.java

After contacting the trusted authentication server on port 20002, clientA contacts serverB on TCP port 30003. Both the client and server will print a message indicating whether their connection is authentic and can be trusted based on information received from the authentication server who has the real keys. argv[0] is the ip of TAS, argv[1] is client A's name, argv[2] is server B's name, argv[3] is the ip of the server.

### Project 3
* udpClient.java

This is an active client that initiates a UDP connection on port 40004 and allows a client to change their key to a new one as long as they can provide the correct previous key. argv[0] is the ip of RKS, argv[1] is client A's name.
