import java.io.*;
import java.net.*;

// This class is the active client that initiates a udp connection on port 40004
public class client{
	static Runtime shell;

	public static void main(String[] argv){
		shell = Runtime.getRuntime();
		int port = 40004;
		
		try {
			InetAddress address = InetAddress.getByName(argv[0]);
			DatagramSocket dfd = new DatagramSocket();

			byte[] Aname = new byte[32];
		
			Netutils.StringToBuf(Aname, argv[1], 0);
		
			// send Aname in 32 byte buffer to RKS
			DatagramPacket outpacket = new DatagramPacket(Aname, Aname.length, address, port);
			dfd.send(outpacket);
			
			byte[] inbuf = new byte[160];

			// get 160 byte packet with AK(newAK) and AK(Aname)
			DatagramPacket inpacket = new DatagramPacket(inbuf, inbuf.length);
			dfd.receive(inpacket);

			byte[] bufAKnewAK = new byte[128];
			byte[] bufAKAname = new byte[32];

			// seperate AK(newAK) from AK(Aname) to differnt buffers
			ttpapRKS.memcpy(bufAKnewAK, inbuf, 0, 0, 128);
			ttpapRKS.memcpy(bufAKAname, inbuf, 0, 128, 32);

			byte[] outnewAK = new byte[128];
			byte[] outAname = new byte[32];
			
			byte[] AKey = new byte[128];

			boolean aok = ttpapRKS.readkey(argv[1], AKey);
			if (!aok) throw new Exception("Failed to read key for " + argv[1]);

			Feistel EA = new Feistel(AKey);

			// decrypt newAK and Aname using AK
			EA.crypt(bufAKnewAK, outnewAK, true);
			EA.crypt(bufAKAname, outAname, true);

			String stroutAname = Netutils.bufToString(outAname, 0, 32);

			// if decrypted Aname is correct
			if(stroutAname.equals(argv[1])){
				Feistel ES = new Feistel(outnewAK);

				byte[] outnewAKAname = new byte[32];

				// encrypt newAK(Aname)
				ES.crypt(outAname, outnewAKAname, false);

				// send newAK(Aname) to RKS
				outpacket = new DatagramPacket(outnewAKAname, outnewAKAname.length, address, port);
				dfd.send(outpacket);
				
				// replace AK with new newAK
				savenewkey(argv[1], AKey, outnewAK);

				System.out.println("New key saved successfully");
			} else{
				System.out.println("Failed to save new key");
			}

		}catch (Exception ee) { 
			ee.printStackTrace();
			System.exit(1);
		} // catch
	} // main
	
	public static void savenewkey(String id, byte[] oldkey, byte[] newkey){
		if (oldkey.length!=128 || newkey.length!=128)
			throw new RuntimeException("wrong key lengths");
		try {
			DataOutputStream dout = new DataOutputStream(new FileOutputStream(id+".prevkey")); 
			dout.write(oldkey,0,128);
			dout.close();
			dout = new DataOutputStream(new FileOutputStream(id+".newkey"));
			dout.write(newkey,0,128); // save new key
			dout.close();
			String cmd = "/usr/bin/flock -x lockfile mv "+id+".newkey "+id+".key";
			Process pcmd = shell.exec(cmd); // execute command
			int r = pcmd.waitFor();
		} catch (Exception e) { e.printStackTrace(); }
	 }//savenew key
} // class
