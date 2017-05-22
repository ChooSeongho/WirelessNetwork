import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelectFile extends JFrame implements ActionListener {

	static public InetAddress serverIp;
	static public int serverPort;
	JLabel jLabel;
	JTextArea logArea;
	JScrollPane scrollPane;
	JPopupMenu popup = new JPopupMenu();
	JMenuItem upLoad = new JMenuItem("파일 전송");
	private JFileChooser fileChooser = new JFileChooser();
	String name;
	Client client;
	MyDatagramSocket datagramSocket;
	byte[] sendBuffer = null;
	File file = null;
	Thread thread;

	public SelectFile(Client client) {
		this.client = client;
		try {
			datagramSocket = new MyDatagramSocket(8888, this); // client port = 8888
			datagramSocket.connect(client.getServerIp(), client.getServerPort());
			thread = new Thread(new Runnable() { // file send thread

				@Override
				public void run() {
					
					while (true) {
						try {
							String[] checkACK = datagramSocket.receiveMessage().split("_");
							System.out.println("ACK = " + checkACK[0]);
							addLog("ACK = " + checkACK[0]);
							if (datagramSocket.getFinalSequenceNumber() > 0
									&& Integer.parseInt(checkACK[0]) > datagramSocket.getFinalSequenceNumber() 
									|| datagramSocket.getSequenceNumber() == datagramSocket.getFinalSequenceNumber()) {
								datagramSocket.closeFile();
								datagramSocket.close();
								System.exit(0);
								break;
							} else if(checkACK[0].trim().equals("time out")){
								// time out
							} else {
								datagramSocket.sendFile(serverIp, serverPort, Integer.parseInt(checkACK[0]));
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		jLabel = new JLabel();
//		jLabel.setBounds(0, 0, 483, 262);
		logArea = new JTextArea();
		scrollPane = new JScrollPane(logArea);
		scrollPane.setBounds(0,0,483,262);
//		jLabel.add(scrollPane);
		popup.add(upLoad); // add popup menu
		upLoad.addActionListener(this); // add click listener
//		jLabel.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent e) {
//				if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
//					popup.show((Component) e.getSource(), e.getX(), e.getY());
//				}
//			}
//		});
//		add(jLabel);
		logArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
					popup.show((Component) e.getSource(), e.getX(), e.getY());
				}
			}
		});
		add(scrollPane);
		setLayout(null);
		setSize(500, 300);
		getContentPane().setBackground(Color.decode("#bdb67b"));
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setVisible(true);
	}
	
	public void addLog(String log){
		logArea.append(log + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == upLoad) {
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				System.out.println("directory : " + fileChooser.getSelectedFile().toString());
				addLog("directory : " + fileChooser.getSelectedFile().toString());
				file = new File(fileChooser.getSelectedFile().toString());
				System.out.println((int) file.length());
				addLog(Integer.toString((int) file.length()));
				name = fileChooser.getSelectedFile().getName();
				try {
					datagramSocket.sendMessage("fileName_" + name + "_");
					datagramSocket.readFile(file);
					datagramSocket.sendFile(client.getServerIp(), client.getServerPort(), 0);
					thread.start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
