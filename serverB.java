import java.io.*;
import java.net.*;
import java.util.*;

// The program is a server that will listen on port 30003 for ClientA.java
public class serverB{
	public static final int SRVPORT = 30003;

	public static void main(String[] argv) throws Exception {
		ServerSocket sfd;
		Socket cfd;
		DataInputStream din;
		DataOutputStream dout;

		sfd = new ServerSocket(SRVPORT);
		sfd.setReuseAddress(true);
		System.out.println("Server B started on Port " + SRVPORT);

		while (true) {
			try{
				cfd = sfd.accept();
				System.out.println("Connection from " + cfd.getInetAddress() + ":" + cfd.getPort());
				cfd.setSoTimeout(5000);

				din = new DataInputStream(cfd.getInputStream());
				dout = new DataOutputStream(cfd.getOutputStream());


				byte[] BKSK = new byte[128];
				byte[] BKAname = new byte[32];
				byte[] SKAname = new byte[32];

				// read BK(SK), BK(Aname) and SK(Aname) from client
				Netutils.readFully(din, BKSK);
				Netutils.readFully(din, BKAname);
				Netutils.readFully(din, SKAname);

				byte[] BKey = new byte[128];

				boolean bok = ttpapASMT.readkey(argv[0], BKey);
				if (!bok) throw new Exception("failed to read key for " + argv[0]);

				Feistel EB = new Feistel(BKey);

				byte[] outSK = new byte[128];
				byte[] outAname = new byte[32];

				// decrypt SK and Aname using BK
				EB.crypt(BKSK, outSK, true);
				EB.crypt(BKAname, outAname, true);

				Feistel ES = new Feistel(outSK);

				// decrypt Aname with SK
				byte[] outAname2 = new byte[32];
				ES.crypt(SKAname, outAname2, true);

				String Aname1 = Netutils.bufToString(outAname, 0, 32);
				String Aname2 = Netutils.bufToString(outAname2, 0, 32);

				byte[] Bname = new byte[32];
				Netutils.StringToBuf(Bname, argv[0], 0);

				// encrypt -- SK(Bname)
				byte[] SKBname = new byte[32];
				ES.crypt(Bname, SKBname, false);

				// send SK(Bname)
				dout.write(SKBname, 0, 32);

				byte[] msg = new byte[128];

				// compare the two A names
				if(Aname1.equals(Aname2)){
					Netutils.StringToBuf(msg, "You're authentic", 0);
				} else {
					Netutils.StringToBuf(msg, "Hacker go away", 0);
				}

				// send message to client
				dout.write(msg, 0, 128);

				byte[] finMsg = new byte[128];

				// read final message from client
				Netutils.readFully(din, finMsg);
				String strFinMsg = Netutils.bufToString(finMsg, 0, 128);

				System.out.println(strFinMsg);

				cfd.close();
			} catch(Exception ee){
				ee.printStackTrace();
				//System.exit(1);
			} // catch
		} // while
	} // main
}//class


