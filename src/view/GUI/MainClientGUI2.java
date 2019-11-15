package view.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.google.gson.Gson;

import config.Config;
import controller.client.ClientPI;
import view.eventListener.ClientEventHandler;
import view.eventListener.ServerEventHanlder;

import java.awt.Font;

public class MainClientGUI2 extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ClientEventHandler actionClient;
	private ServerEventHanlder actionServer;
	private boolean isLoggedIn = false;

	public MainClientGUI2(ClientPI clientPI) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GUI();
		eventListening();
		if (Config.DEBUG) {
			tf_Host.setText("localhost");
			tf_Username.setText("admin");
			tf_Password.setText("123");
			tf_Port.setText("2121");
		}
		actionClient = new ClientEventHandler(clientPI);
		actionServer = new ServerEventHanlder(clientPI);
		actionClient.setJTreeDirs(tree_LocalDir);
		actionClient.setJTreeFilesFolders(tree_LocalFile);
		actionClient.setTf_LocalFile(tf_LocalFile);
		actionClient.setTf_LocalDir(tf_LocalDir);
		actionClient.initJTreeClientDirs();
		actionClient.initJTreeFilesFoldersClient();
	}

	public void GUI() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		getContentPane().setLayout(null);
		JPanel panel_Info = new JPanel();
		panel_Info.setBounds(0, 0, 1147, 47);
		panel_Info.setLayout(null);

		JLabel lb_Host = new JLabel("Host");
		lb_Host.setBounds(27, 15, 40, 25);
		panel_Info.add(lb_Host);

		tf_Host = new JTextField();
		tf_Host.setBounds(71, 15, 131, 25);
		panel_Info.add(tf_Host);
		tf_Host.setColumns(10);

		JLabel lb_Username = new JLabel("Username");
		lb_Username.setBounds(214, 15, 70, 25);
		panel_Info.add(lb_Username);

		tf_Username = new JTextField();
		tf_Username.setColumns(10);
		tf_Username.setBounds(285, 15, 152, 25);
		panel_Info.add(tf_Username);

		JLabel lb_Password = new JLabel("Password");
		lb_Password.setBounds(451, 15, 70, 25);
		panel_Info.add(lb_Password);

		tf_Password = new JPasswordField();
		tf_Password.setBounds(519, 15, 141, 25);
		panel_Info.add(tf_Password);

		JLabel lb_Port = new JLabel("Port");
		lb_Port.setBounds(697, 15, 40, 25);
		panel_Info.add(lb_Port);
		getContentPane().add(panel_Info);

		tf_Port = new JTextField();
		tf_Port.setBounds(735, 15, 79, 25);
		panel_Info.add(tf_Port);
		tf_Port.setColumns(10);

		btn_Connect = new JButton("Connect");
		btn_Connect.setBounds(873, 15, 97, 25);
		panel_Info.add(btn_Connect);

		btn_Logout = new JButton("Logout");
		btn_Logout.setBounds(1000, 15, 97, 25);
		panel_Info.add(btn_Logout);

		btn_TypeAscii = new JButton("ASCII");
		btn_TypeAscii.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btn_TypeAscii.setBounds(530, 225, 69, 47);
		getContentPane().add(btn_TypeAscii);

		btn_TypeImage = new JButton("Binary");
		btn_TypeImage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btn_TypeImage.setBounds(530, 337, 69, 47);
		getContentPane().add(btn_TypeImage);

		JPanel panel_Status = new JPanel();
		panel_Status.setBounds(0, 48, 1147, 133);
		getContentPane().add(panel_Status);
		panel_Status.setLayout(null);

		JScrollPane sp_Status = new JScrollPane();
		sp_Status.setBounds(6, 0, 1127, 133);
		panel_Status.add(sp_Status);

		ta_Status = new JTextArea();
		ta_Status.setFont(new Font("Tahoma", Font.PLAIN, 14));
		sp_Status.setViewportView(ta_Status);

		JPanel panel_LocalDir = new JPanel();
		panel_LocalDir.setBounds(10, 180, 513, 260);
		getContentPane().add(panel_LocalDir);
		panel_LocalDir.setLayout(null);

		JLabel lb_LocalDir = new JLabel("Local Directory");
		lb_LocalDir.setBounds(4, 6, 89, 25);
		panel_LocalDir.add(lb_LocalDir);

		tf_LocalDir = new JTextField();
		tf_LocalDir.setEditable(false);
		tf_LocalDir.setBounds(97, 4, 415, 28);
		panel_LocalDir.add(tf_LocalDir);
		tf_LocalDir.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 32, 512, 222);
		panel_LocalDir.add(scrollPane);

		tree_LocalDir = new JTree();
		DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("root");
		tree_LocalDir.setModel(new DefaultTreeModel(treeNode1));
		scrollPane.setViewportView(tree_LocalDir);

		JPanel panel_RemoteDir = new JPanel();
		panel_RemoteDir.setLayout(null);
		panel_RemoteDir.setBounds(607, 180, 525, 254);
		getContentPane().add(panel_RemoteDir);

		JLabel lb_Remote = new JLabel("Remote Directory");
		lb_Remote.setBounds(6, 6, 106, 25);
		panel_RemoteDir.add(lb_Remote);

		tf_RemoteDir = new JTextField();
		tf_RemoteDir.setBounds(111, 4, 415, 28);
		panel_RemoteDir.add(tf_RemoteDir);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(0, 32, 525, 222);
		panel_RemoteDir.add(scrollPane_1);

		tree_RemoteDir = new JTree();
		treeNode1 = new DefaultMutableTreeNode("root");
		tree_RemoteDir.setModel(new DefaultTreeModel(treeNode1));
		scrollPane_1.setViewportView(tree_RemoteDir);

		JPanel panel_LocalFile = new JPanel();
		panel_LocalFile.setLayout(null);
		panel_LocalFile.setBounds(10, 468, 513, 255);
		getContentPane().add(panel_LocalFile);

		JLabel lb_LocalFile = new JLabel("Local File");
		lb_LocalFile.setBounds(4, 6, 89, 25);
		panel_LocalFile.add(lb_LocalFile);

		tf_LocalFile = new JTextField();
		tf_LocalFile.setEditable(false);
		tf_LocalFile.setColumns(10);
		tf_LocalFile.setBounds(97, 4, 415, 28);
		panel_LocalFile.add(tf_LocalFile);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(0, 32, 512, 222);
		panel_LocalFile.add(scrollPane_2);

		tree_LocalFile = new JTree();
		treeNode1 = new DefaultMutableTreeNode("root");
		tree_LocalFile.setModel(new DefaultTreeModel(treeNode1));
		scrollPane_2.setViewportView(tree_LocalFile);

		JPanel panel_RemoteFile = new JPanel();
		panel_RemoteFile.setLayout(null);
		panel_RemoteFile.setBounds(607, 468, 525, 255);
		getContentPane().add(panel_RemoteFile);

		JLabel lblRemoteFile = new JLabel("Remote File");
		lblRemoteFile.setBounds(6, 6, 106, 25);
		panel_RemoteFile.add(lblRemoteFile);

		tf_RemoteFile = new JTextField();
		tf_RemoteFile.setBounds(111, 4, 415, 28);
		panel_RemoteFile.add(tf_RemoteFile);

		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(0, 32, 525, 222);
		panel_RemoteFile.add(scrollPane_3);

		tree_RemoteFile = new JTree();
		treeNode1 = new DefaultMutableTreeNode("root");
		tree_RemoteFile.setModel(new DefaultTreeModel(treeNode1));
		scrollPane_3.setViewportView(tree_RemoteFile);

		btn_Upload = new JButton(new ImageIcon("ICON/ic_arrow_right.png"));
		btn_Upload.setBounds(535, 520, 57, 47);
		getContentPane().add(btn_Upload);

		btn_Download = new JButton(new ImageIcon("ICON/ic_arrow_left.png"));
		btn_Download.setBounds(535, 609, 60, 47);
		getContentPane().add(btn_Download);

		setSize(1160, 800);
		setVisible(true);
	}

	private void eventListening() {
		btn_Connect.setActionCommand("Login");
		btn_Connect.addActionListener(this);
		btn_Logout.setActionCommand("Logout");
		btn_Logout.addActionListener(this);
		btn_TypeAscii.setActionCommand("Ascii");
		btn_TypeAscii.addActionListener(this);
		btn_TypeImage.setActionCommand("Image");
		btn_TypeImage.addActionListener(this);
		btn_Download.setActionCommand("Download");
		btn_Download.addActionListener(this);
		btn_Upload.setActionCommand("Upload");
		btn_Upload.addActionListener(this);
		lockButton();
	}

	public void lockButton() {
		btn_Connect.setEnabled(!isLoggedIn);
		btn_Logout.setEnabled(isLoggedIn);
		btn_TypeAscii.setEnabled(isLoggedIn);
		btn_TypeImage.setEnabled(isLoggedIn);
		btn_Upload.setEnabled(isLoggedIn);
		btn_Download.setEnabled(isLoggedIn);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch (command) {
		case "Login":
			btnConnectActionPerformed();
			break;
		case "Ascii":
			btnTypeHandle("a");
			break;
		case "Image":
			btnTypeHandle("i");
			break;
		case "Download":
			btnDownloadActionPerformed();
			break;
		case "Upload":
			btnUploadActionPerformed();
			break;
		case "Logout":
			btnLogoutActionPerformed();
			break;
		default:
			JOptionPane.showMessageDialog(null, "Oops! Something went wrong!", "Error",
					JOptionPane.INFORMATION_MESSAGE);
			break;
		}
	}

	private void btnConnectActionPerformed() {
		String host = tf_Host.getText();
		String username = tf_Username.getText();
		String password = String.valueOf(tf_Password.getPassword());
		System.out.println(password);
		String port = tf_Port.getText();

		// NOTE: Middle-wares
		HashMap<String, String> message = actionServer.handleConnect(host, username, password, port);
		String value = message.get("error");
		if (value != null) {
			ta_Status.append(">> Status: \t" + value + "\n");
		} else {
			isLoggedIn = true;
			lockButton();
			ta_Status.append(">> Status: \tConnect to host " + host + " successfull\n");
			btn_Logout.setVisible(true);
			btn_Connect.setEnabled(false);

			// NOTE: Show folders and files from server
			// Server know folder belong to which users?
			tf_RemoteDir.setText("/");
			tf_RemoteFile.setText("/");

			actionServer.setTreeDirs(tree_RemoteDir);
			actionServer.setTreeFilesFolders(tree_RemoteFile);
			actionServer.setTextFieldRemoteFile(tf_RemoteFile);
			actionServer.setTextFieldRemoteDir(tf_RemoteDir);
			actionServer.initJTreeFilesFoldersServer(tree_RemoteFile);

			actionServer.showJTreeServerDirs(tree_RemoteDir, "/");
			tree_RemoteDir.addTreeWillExpandListener(actionServer.handleTreeDirsWillExpand);
			tree_RemoteDir.addTreeSelectionListener(actionServer.handleTreeDirsSelection);
		}
	}

	private void btnTypeHandle(String type) {
		Config.print("Change Transfer Type to " + (type.equals("a") ? "ASCII" : "Image"));
		String message = actionClient.changeTransfertype(type);
		if (message.equals("success")) {
			ta_Status.append(">> Status: \t" + "Change type successfully" + "\n");
		} else {
			ta_Status.append(">> Status: \t" + "Change type successfully" + "\n");
		}
	}

	@SuppressWarnings("unchecked")
	private void btnUploadActionPerformed() {
		String pathClient = tf_LocalFile.getText();
		String pathServer = tf_RemoteFile.getText();
		Config.print("UPLOAD: " + pathClient + " -> " + pathServer);

		ta_Status.append(">> Status: \t" + "Checking for upload...\n");

		String res = actionServer.upload(pathClient, pathServer);
		HashMap<String, String> pairs = new HashMap<>();
		pairs = new Gson().fromJson(res, pairs.getClass());
		ta_Status.append(">> Status: \t" + pairs.get("message") + "\n");
	}

	@SuppressWarnings("unchecked")
	private void btnDownloadActionPerformed() {
		String pathClient = tf_LocalDir.getText();
		String pathServer = tf_RemoteFile.getText();
		Config.print("DOWNLOAD: " + pathClient + " -> " + pathServer);

		ta_Status.append(">> Status: \t" + "Checking for download...\n");

		String res = actionClient.download(pathClient, pathServer);
		HashMap<String, String> pairs = new HashMap<>();
		pairs = new Gson().fromJson(res, pairs.getClass());
		ta_Status.append(">> Status: \t" + pairs.get("message") + "\n");
	}

	private void btnLogoutActionPerformed() {
		String res = actionServer.logout();
		if (res.equals("success")) {
			ta_Status.append(">> Status: \t" + "Client logged out" + "\n");
			tf_RemoteDir.setText("");
			tf_RemoteFile.setText("");
			tree_RemoteDir.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
			tree_RemoteFile.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
			isLoggedIn = false;
			lockButton();
		}
	}

	// View components
	private JTextField tf_Host;
	private JTextField tf_Username;
	private JPasswordField tf_Password;
	private JTextField tf_Port;
	private JButton btn_Connect;
	private JButton btn_Logout;
	private JButton btn_TypeAscii;
	private JButton btn_TypeImage;
	private JTextArea ta_Status;
	private JTree tree_LocalDir;
	private JTree tree_LocalFile;
	private JTree tree_RemoteDir;
	private JTree tree_RemoteFile;
	private JTextField tf_LocalDir;
	private JTextField tf_RemoteDir;
	private JTextField tf_LocalFile;
	private JTextField tf_RemoteFile;
	private JButton btn_Upload;
	private JButton btn_Download;
}
