import java.net.*;
import java.io.*;

public class MyDatagramSocket extends DatagramSocket {
	static final int MAX_LEN = 100;
	InetAddress serverIp = null;
	int serverPort = 0;
	byte[] sendBuffer;
	int length = 50;
	FileInputStream fileBytes;
	BufferedInputStream fileBuffer;
	int sequenceNum = 0;
	int finalSequenceNum = 0;
	SelectFile sf;
	MyDatagramSocket() throws SocketException {
		super();
	}

	MyDatagramSocket(int myPort) throws SocketException {
		super(myPort);
	}
	
	MyDatagramSocket(int myPort, SelectFile sf) throws SocketException {
		super(myPort);
		this.sf = sf;
	}

	public void sendMessage(String message) throws IOException {
		byte[] sendBuffer = message.getBytes();
		DatagramPacket datagram = new DatagramPacket(sendBuffer, sendBuffer.length, serverIp, serverPort);
		System.out.println("send file name = "+message);
		sf.addLog("send file name = "+message);
		this.send(datagram);
	}

	public void sendFile(InetAddress receiverHost, int receiverPort, int offset) throws IOException {
		if (offset * 50 + length >= sendBuffer.length) { // check final packet
			System.out.println("last");
			sf.addLog("last");
			length = sendBuffer.length - (offset * 50);
			if (length < 0) {
				return;
			}
			finalSequenceNum = sequenceNum + 1;
			sendMessage("endFile_" + finalSequenceNum + "_");
		}
		sequenceNum = offset;
		serverIp = receiverHost;
		serverPort = receiverPort;
		String headerData = sequenceNum + "_";
		byte[] header = headerData.getBytes();
		byte[] packet = new byte[header.length + length];
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(sendBuffer, offset * 50, packet, header.length, length);
		DatagramPacket datagram = new DatagramPacket(packet, 0, packet.length, serverIp, serverPort);
		System.out.println("data length = " + datagram.getLength());
//		this.send(datagram);
		makeError(datagram);
		sequenceNum++;
		this.setSoTimeout(1000);
	}
	
	public void makeError(DatagramPacket datagram) throws IOException{ // make error in random
		int random = (int) (Math.random()*100);
		System.out.println("randomNum = "+random);
		sf.addLog("randomNum = "+random);
		if(random>75){
			// send skip
		} else if(random>50){ // send duplication data
			System.out.println("send sequence = "+sequenceNum);
			sf.addLog("send sequence = "+sequenceNum);
			this.send(datagram);
			System.out.println("send sequence = "+sequenceNum);
			sf.addLog("send sequence = "+sequenceNum);
			this.send(datagram);
		} else{ // send normal data
			System.out.println("send sequence = "+sequenceNum);
			sf.addLog("send sequence = "+sequenceNum);
			this.send(datagram);
		}
	}
	
	public void solveTimeOut() throws IOException {
		sendFile(serverIp, serverPort, sequenceNum);
	}

	public void readFile(File file) throws IOException { // read file in buffer
		sendBuffer = new byte[(int) file.length()];
		fileBytes = new FileInputStream(file);
		fileBuffer = new BufferedInputStream(fileBytes);
		fileBuffer.read(sendBuffer, 0, sendBuffer.length);
		sf.addLog("buffer length = " + sendBuffer.length);
		System.out.println("buffer length = " + sendBuffer.length);
	}

	public void closeFile() throws IOException { // close buffer used to read file 
		fileBytes.close();
		fileBuffer.close();
	}
	
	public int getSequenceNumber(){
		return sequenceNum;
	}

	public int getFinalSequenceNumber() {
		return finalSequenceNum;
	}

	public String receiveMessage() throws IOException {
		try{
			byte[] receiveBuffer = new byte[MAX_LEN];
			DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
			this.receive(datagram);
			String message = new String(receiveBuffer);
			return message;
		} catch(Exception e){
			solveTimeOut();
		}
		return "time out";
	}
} // end class
