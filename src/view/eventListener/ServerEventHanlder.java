package view.eventListener;

import controller.client.ClientPI;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import config.Config;

/**
 * @author Administrator
 */
public class ServerEventHanlder {
	private ClientPI clientPI;
	private JTree treeDirs;
	private JTree treeFilesFolders;
	private JTextField tf_RemoteDir;
	private JTextField tf_RemoteFile;
	DefaultTreeModel modelDirs;
	DefaultTreeModel modelFilesFolders;
	private boolean isLogged = false;;

	public ServerEventHanlder(ClientPI _clientPI) {
		this.clientPI = _clientPI;
	}

	public void setTreeDirs(JTree treeDirs) {
		this.treeDirs = treeDirs;
	}

	public void setTreeFilesFolders(JTree treeFilesFolders) {
		this.treeFilesFolders = treeFilesFolders;
	}

	public void setTextFieldRemoteDir(JTextField tf_RemoteDir) {
		this.tf_RemoteDir = tf_RemoteDir;
	}

	public void setTextFieldRemoteFile(JTextField tf_RemoteFile) {
		this.tf_RemoteFile = tf_RemoteFile;
	}

	public void setRemoteFilePath(String path) {
		tf_RemoteFile.setText(path);
	}

	public HashMap<String, String> handleConnect(String host, String username, String password, String port) {
		HashMap<String, String> message = new HashMap<>();
		if (host.equals("")) {
			message.put("error", "Host can't be empty!");
			return message;
		}
		if (username.equals("")) {
			message.put("error", "Username can't be empty!");
			return message;
		}
		if (password.equals("")) {
			message.put("error", "Password can't be empty!");
			return message;
		}
		if (port.equals("")) {
			message.put("error", "Port can't be empty!");
			return message;
		}
		Config.print(host + " " + username + " " + password + " " + port);
		HashMap<String, String> result = this.clientPI.connect(host, username, password, port);
		if (result.get("OK") != null) {
			if (result.get("OK").equals("Done"))
				isLogged = true;
		}
		return result;
	}

	public String upload(String pathClient, String pathServer) {
		if (!isLogged) {
			HashMap<String, String> pairs = new HashMap<>();
			pairs.put("status", "fail");
			pairs.put("message", "Haven't login yet!");
			return new Gson().toJson(pairs);
		}

		String res = this.clientPI.getClientDTP().upload(pathClient, pathServer);
		showJTreeServerFilesFolders(pathServer);

		return res;
	}

	public void download(String pathClient, String pathServer) {
		if (isLogged) {
			this.clientPI.getClientDTP().download(pathClient, pathServer);
		}
	}

	public String logout() {
		if (!isLogged) {
			HashMap<String, String> pairs = new HashMap<>();
			pairs.put("status", "fail");
			pairs.put("message", "Haven't login yet!");
			return new Gson().toJson(pairs);
		}
		String res = this.clientPI.logout();
		if (res.equals("success")) {
			isLogged = false;
			treeDirs.removeAll();
			treeFilesFolders.removeAll();
		}
		return res;
	}

	private String getStringPath(Object[] paths) {
		if (paths.length == 1) {
			return paths[0].toString();
		}

		String curPath = "";
		for (int i = 0; i < paths.length; i++) {
			if (i != 0) {
				curPath += "/";
			}
			if (paths[i].toString().equals("/") == false)
				curPath += paths[i];
		}

		return curPath;
	}

	public TreeWillExpandListener handleTreeDirsWillExpand = new TreeWillExpandListener() {
		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
			String curPath = "";
			Config.print("treeWillExpand: " + event.getPath().toString());

			Object[] paths = event.getPath().getPath();
			for (int i = 0; i < paths.length; i++) {
				if (i != 0) {
					curPath += "/";
				}
				if (paths[i].toString().equals("/") == false)
					curPath += paths[i];
			}
			tf_RemoteDir.setText(curPath);
			Config.print("treeWillExpand parent: " + curPath);
			showJTreeServerFilesFolders(curPath);
		}

		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
		}
	};

	public TreeWillExpandListener handleTreeFilesFoldersWillExpand = new TreeWillExpandListener() {
		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
			String curPath = "";
			Config.print("treeWillExpand: " + event.getPath().toString());

			Object[] paths = event.getPath().getPath();
			for (int i = 0; i < paths.length; i++) {
				if (i != 0) {
					curPath += "/";
				}
				if (paths[i].toString().equals("/") == false)
					curPath += paths[i];
			}
			Config.print("treeWillExpand parent: " + curPath);
			showJTreeServerFilesFolders(curPath);
		}

		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
		}
	};

	public TreeSelectionListener handleTreeFilesFoldersSelection = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (isLogged) {
				String curPath = "";
				Config.print("treeWillExpand1: " + e.getPath().toString());
				curPath = getStringPath(e.getPath().getPath());
				tf_RemoteFile.setText(curPath);
			}
		}
	};

	public TreeSelectionListener handleTreeDirsSelection = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (isLogged) {
				String curPath = "";
				Config.print("treeWillExpand2: " + e.getPath().toString());
				try {
					curPath = getStringPath(e.getPath().getPath());
					Config.print("Path: " + curPath);
					tf_RemoteDir.setText(curPath);
					tf_RemoteFile.setText(curPath);
					showJTreeServerFilesFolders(curPath);
				} catch (Exception ex) {
					Config.print("Tree dir cannot expand ");
				}
			}
		}

	};

	private DefaultMutableTreeNode addNodesServer(DefaultMutableTreeNode curTop,
			ArrayList<LinkedTreeMap<String, String>> list, int index) {
		DefaultMutableTreeNode curDir = null;

		if (list.size() > 0) {
			for (LinkedTreeMap.Entry<String, String> entry : list.get(index).entrySet()) {
				int key = Integer.parseInt(entry.getKey());
				String value = entry.getValue();

				curDir = new DefaultMutableTreeNode(value);
				if (curTop != null) {
					curTop.add(curDir);
				}
				if (key < list.size() && list.get(key).size() != 0) {
					addNodesServer(curDir, list, key);
				}
			}
		}

		return curTop;
	}

	@SuppressWarnings("unchecked")
	public void showJTreeServerDirs(JTree jTree, String path) {
		String res = clientPI.listDirsFromServer(path);
		ArrayList<LinkedTreeMap<String, String>> list = new ArrayList<>();
		list = new Gson().fromJson(res, list.getClass());
		Config.print("showJTreeServerDirs - res: " + res);

		DefaultMutableTreeNode rootNode = addNodesServer(new DefaultMutableTreeNode("/"), list, 0);
		DefaultTreeModel model = new DefaultTreeModel(rootNode, true);
		jTree.setModel(model);
	}

	@SuppressWarnings("unchecked")
	public void showJTreeServerFilesFolders(String parentPath) {
		String json = clientPI.listFilesAndFoldersFromServer(parentPath);
		Config.print("showJTreeServerFiles :" + parentPath + " ---- " + json);
		HashMap<String, ArrayList<String>> pairs = new HashMap<>();
		pairs = new Gson().fromJson(json, pairs.getClass());

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(parentPath);
		DefaultTreeModel model = new DefaultTreeModel(rootNode, true);
		// for (String folderName : pairs.get("folders")) {
		// rootNode.add(new DefaultMutableTreeNode(folderName));
		// }
		for (String fileName : pairs.get("files")) {
			rootNode.add(new DefaultMutableTreeNode(fileName, false));
		}
		treeFilesFolders.setModel(model);
	}

	@SuppressWarnings("unchecked")
	public void initJTreeFilesFoldersServer(JTree jTree) {
		String json = clientPI.listFilesAndFoldersFromServer("/");
		HashMap<String, ArrayList<String>> pairs = new HashMap<>();
		pairs = new Gson().fromJson(json, pairs.getClass());
		treeFilesFolders.addTreeSelectionListener(handleTreeFilesFoldersSelection);

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("/");
		DefaultTreeModel model = new DefaultTreeModel(rootNode, true);
		// for (String folderName : pairs.get("folders")) {
		// rootNode.add(new DefaultMutableTreeNode(folderName));
		// }
		for (String fileName : pairs.get("files")) {
			rootNode.add(new DefaultMutableTreeNode(fileName, false));
		}
		treeFilesFolders.setModel(model);

		if (!Config.DEBUG) {
			jTree.expandRow(0);
			jTree.setRootVisible(false);
		}
	}
}
