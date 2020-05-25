import java.io.*;
import java.net.*;

// This class is the active client that wishes to initiate a connection with a server on TCP port 30003.
// It must first contact the trusted authentication server on port 20002. argv[0] is the ip of TAS, argv[1]
// is client A's name, argv[2] is server B's name, argv[3] is the ip of the server.
public class clientA{

	public static void main(String[] argv){
		Socket cfd;
		DataInputStream din;
		DataOutputStream dout;

		try{
			// connect to TAS
			cfd = new Socket(argv[0], 20002);
			din = new DataInputStream(cfd.getInputStream());
			dout = new DataOutputStream(cfd.getOutputStream());

			// now in established state

			byte[] Aname = new byte[32];
			byte[] Bname = new byte[32];

			Netutils.StringToBuf(Aname, argv[1], 0);
			Netutils.StringToBuf(Bname, argv[2], 0);

			// send A's name and B's name to TAS
			dout.write(Aname, 0, 32);
			dout.write(Bname, 0, 32);

			byte[] AKSK = new byte[128];
			byte[] BKSK = new byte[128];
			byte[] BKAname = new byte[32];

			// read AK(SK), BK(SK) and BK(Aname) from TAS
			Netutils.readFully(din, AKSK);
			Netutils.readFully(din, BKSK);
			Netutils.readFully(din, BKAname);

			byte[] AKey = new byte[128];

			boolean aok = ttpapASMT.readkey(argv[1], AKey);
			if (!aok) throw new Exception("failed to read key for " + argv[1]);

			Feistel EA = new Feistel(AKey);

			// decrypt SK using AK
			byte[] outSK = new byte[128];
			EA.crypt(AKSK, outSK, true);

			try {
				// connect to server B
				cfd =  new Socket(argv[3], 30003);
				din = new DataInputStream(cfd.getInputStream());
				dout = new DataOutputStream(cfd.getOutputStream());

				// send BK(SK) and BK(Aname)
				dout.write(BKSK, 0, 128);
				dout.write(BKAname, 0, 32);

				Feistel ES = new Feistel(outSK);

				byte[] outSKAname = new byte[32];

				// encrypt -- SK(Aname)
				ES.crypt(Aname, outSKAname, false);

				// send SK(Aname)
				dout.write(outSKAname, 0, 32);

				byte[] SKBname = new byte[32];
				byte[] msg = new byte[128];

				// read SK(Bname) and message
				Netutils.readFully(din, SKBname);
				Netutils.readFully(din, msg);

				String strMsg = Netutils.bufToString(msg, 0, 128);
				System.out.println(strMsg);

				// decrypt SK(Bname)
				byte[] outBname = new byte[32];
				ES.crypt(SKBname, outBname, true);

				String strOutBname = Netutils.bufToString(outBname, 0 , 32);

				byte[] finMsg = new byte[128];

				// compare with Bname A intended to connect with
				if(strOutBname.equals(argv[2])){
					Netutils.StringToBuf(finMsg, "You're the real " + argv[2], 0);
				} else {
					Netutils.StringToBuf(finMsg, "You're not the real " + argv[2], 0);
				}

				// send final message to B
				dout.write(finMsg, 0, 128);

				cfd.close();
			} catch (Exception ee) {
				ee.printStackTrace();
				System.exit(1);
			} // catch
		} catch (Exception ee) {
			ee.printStackTrace();
			System.exit(1);
		} // catch
	} // main

} // class



