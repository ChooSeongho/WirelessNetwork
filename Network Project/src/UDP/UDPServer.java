package UDP;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UDPServer extends JFrame {
	JPanel centerPanel, portPanel;
	JLabel portName;
	JTextField serverPort_input;
	JButton start;
	int port;
	public final static int default_Port = 7777;
	static byte[] buffer = new byte[5555555];
	static DatagramSocket sokect;
	static DatagramPacket packet;
	static DataOutputStream data_output = null;
	static int Port = 0;
	static File file = null;
	static String fileName = null;
	static BufferedOutputStream bos = null;
	static int sequenceNum = 0, finalSequenceNum = 0;
	
	public UDPServer() {
		super("Server");

		Container c = getContentPane();

		centerPanel = new JPanel(new GridLayout(2, 1));

		portPanel = new JPanel();

		portName = new JLabel("서버 PORT를 입력하세요.(default = 7777)");
		portPanel.add(portName);

		serverPort_input = new JTextField(25);
		serverPort_input.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					start.doClick();
				}
			}
		});
		portPanel.add(serverPort_input);

		centerPanel.add(portPanel);

		start = new JButton("Start Server");
		centerPanel.add(start);

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (serverPort_input.getText().equals("")) {
					port = 7777;
				} else {
					port = Integer.parseInt(serverPort_input.getText());
				}
				startServer();
			}
		});

		c.add(centerPanel, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setSize(300, 200);
		show();
	}
	

	public static void main(String[] args) {
		new UDPServer();
	}
	
	public void startServer(){
		try {
			sokect = new DatagramSocket(Port);
			System.out.println("Server Start");
			System.out.println("receiving data...");

//			packet = new DatagramPacket(buffer, buffer.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				packet = new DatagramPacket(buffer, buffer.length);
				sokect.receive(packet);
				int random = (int) (Math.random() * 100);
				System.out.println("random num = "+random);
				if (random > 75) {
					// receive skip
					receive(null);
				} else if (random > 50) { 
					// receive duplication data
					receive(packet);
					receive(packet);
				} else { 
					// receive normal data
					receive(packet);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void receive(DatagramPacket receivedData){
		try {
			String data;
			if(receivedData != null){
				data = new String(receivedData.getData()).trim();
				String[] checkHeader = data.split("_"); // divide header and data
				if (checkHeader[0].trim().equals("fileName")) {
					System.out.println("file name = " + checkHeader[1]);
					fileName = checkHeader[1].trim();
					makeDirectory();
					file = new File("c://Network/" + fileName + ".SAVE");
					data_output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				} else if (checkHeader[0].trim().equals("endFile")) {
					finalSequenceNum = Integer.parseInt(checkHeader[1]);
					System.out.println("endSequence = " + finalSequenceNum);
				} else if (sequenceNum != Integer.parseInt(checkHeader[0].trim())) {
					System.out.println("sequenceNum = " + sequenceNum);
					byte[] sendBuffer = (sequenceNum + "_NACK").getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, receivedData.getAddress(), receivedData.getPort());
					sokect.send(sendPacket);
				} else {
					System.out.println("length = " + data.getBytes().length);
					data_output.write(checkHeader[1].getBytes());
					sequenceNum++;
					System.out.println("sequenceNum = " + sequenceNum);
					if (finalSequenceNum > 0 && sequenceNum >= finalSequenceNum) {
						System.out.println("success");
						sequenceNum = 0;
						data_output.close();
					} else {
						byte[] sendBuffer = (sequenceNum + "_ACK").getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, receivedData.getAddress(), receivedData.getPort());
						sokect.send(sendPacket);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void makeDirectory() {
		// make directory for save file
		File desti = new File("c://Network");
		if (!desti.exists()) {
			desti.mkdirs();
		} else {
			File[] destroy = desti.listFiles();
			for (File des : destroy) {
				des.delete();
			}
		}
		System.out.println("directory = " + "c://Network");
	}
}
