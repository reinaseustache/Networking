import java.io.*;
import java.net.*;

// This program implements a tcp client that allows a client behind a NAT box to dynamically request
// the forwarding of external ports. It accepts the following information: argv[0] is server ip addr, 
// argv[1] is server port, argv[2] is internal client host ip argv[3] is internal host port, argv[4] 
// is requested port, argv[5] is protocol 6 or 17. It prints the external port and IP address of the 
// NAT box it receives back from the server if the mapping was successful.
public class client{
	
	public static void main(String[] argv){
		if (argv.length != 6){ // check if correct number of arguments were passed in
			System.out.println("ERROR: This program accepts 6 arguments ----- Server IP address" +
				       " | Server port | Internal client host IP | Internal host port | Requested port " +
				       " | Protocol 6 or 17");
			System.exit(1);
		}

		Socket cfd;      // communication socket "fd" is carry-over from C
		DataInputStream din;   // binary communicators
		DataOutputStream dout;  
		
		try{
			// connect to server:
			cfd = new Socket(argv[0],Integer.parseInt(argv[1]));
			din = new DataInputStream(cfd.getInputStream());
			dout = new DataOutputStream(cfd.getOutputStream());
			
			// now in ESTABLISHED state
			
			byte[] ipAddr = new byte[4]; // internal client host ip
			int internalPort = Integer.parseInt(argv[3]); // internal host port
			int requestedPort = Integer.parseInt(argv[4]); // requested port
			byte protocol = (byte)(Integer.parseInt(argv[5])); // protocol 6 or 17
			
			int externalPort = 0; // external port mapped to
			
			ipAddr = ipbytes(argv[2]);
			dout.write(ipAddr,0,4); // send IP address
			
			dout.writeShort((short)internalPort); // send internal port
			
			dout.writeShort((short)requestedPort); // send external port
			
			dout.writeByte(protocol); // send protocol
			
			externalPort = din.readUnsignedShort(); // read external port mapped
			
			if (externalPort != 0){ // if mapping is successful
				System.out.println("External port that was successfully mapped ----- " + externalPort);

				byte[] natAddr = new byte[4];
				readFully(din, natAddr, 0, 4); // read NAT box IP
				
				System.out.println("External IP address of NAT box ----------------- " + ipstring(natAddr));
			} else{ // mapping is unsuccessful
				byte[] F = new byte[4];
				readFully(din, F, 0, 4); // read error codes
				
				for(int i = 0; i < 4; i++) 
					if (F[i] > 0) 
						System.out.println("error code: "+ F[i]);
			} //else
			
			din.close();
			dout.close();
			cfd.close();
		} catch (Exception ee) {
			ee.printStackTrace(); 
			System.exit(1);
		} // catch
	} // main


	// the following function only returns after EXACTLY n bytes are read:
	static void readFully(DataInputStream din, byte[] buffer, int start, int n) throws IOException{
		int r = 0; // number of bytes read
		
		while (r<n) 
			r += din.read(buffer,start+r,n-r);
	}//readFully
	
	public static byte[] ipbytes(String addr){
		byte[] B =  new byte[4];
		String[] S = addr.split("\\p{Punct}",4);
		
		for(int i = 0; i < 4; i++)	
			B[i] = (byte)Integer.parseInt(S[i]);
		
		return B;
	} // ipbytes 
	
	public static String ipstring(byte[] B){
		String addr = ""; 
		
		for(int i = 0; i < 4; i++){
			int x = B[i];
			
			if (x < 0) x = x + 256;
			
			addr = addr + "" + x;
			
			if (i < 3) addr = addr + ".";
		}

		return addr;
	} // ipstring
} // client
