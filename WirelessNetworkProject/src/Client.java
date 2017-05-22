import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
//import com.sun.xml.tree.XmlDocument;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class Client extends JFrame {
	private JPanel centerPanel, ipPanel, portPanel;
	private JLabel ipName, portName;
	private JTextField serverip_input, serverPort_input;
	private String deviceName;
	private JButton submit;
	private boolean keepListening;
	private String serverIp;
	private int port = 0;
	private MyDatagramSocket dataSocket;
	private SelectFile selectFile;

	public Client() { // create GUI
		super("Client");

		Container c = getContentPane();

		centerPanel = new JPanel(new GridLayout(3, 1));

		ipPanel = new JPanel();
		portPanel = new JPanel();

		ipName = new JLabel("서버 IP를 입력하세요.");
		ipPanel.add(ipName);
		portName = new JLabel("서버 PORT를 입력하세요.(default = 7777)");
		portPanel.add(portName);

		serverip_input = new JTextField(25);
		serverip_input.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					submit.doClick();
				}
			}
		});
		serverPort_input = new JTextField(25);
		serverPort_input.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					submit.doClick();
				}
			}
		});
		ipPanel.add(serverip_input);
		portPanel.add(serverPort_input);

		centerPanel.add(ipPanel);
		centerPanel.add(portPanel);

		submit = new JButton("Submit");
		centerPanel.add(submit);

		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					deviceName = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				serverIp = serverip_input.getText();
				if (serverPort_input.getText().equals("")) {
					port = 7777;
				} else {
					port = Integer.parseInt(serverPort_input.getText());
				}
				if (serverIp.equals("")) {
					JOptionPane.showMessageDialog(Client.this, "서버 ip를 입력해 주세요.");
				} else {
					selectFile();
					System.out.println("server ip = " + serverIp + "server port = " + port);
					System.out.println("device Name = " + deviceName);
				}
				dispose();
			}
		});

		c.add(centerPanel, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setSize(300, 300);
		show();
	}

	public void selectFile() {
		selectFile = new SelectFile(this);
	}

	public InetAddress getServerIp() throws UnknownHostException {
		return InetAddress.getByName(serverIp);
	}

	public int getServerPort() {
		return port;
	}

	public static void main(String[] args) {
		new Client();
	}

}
