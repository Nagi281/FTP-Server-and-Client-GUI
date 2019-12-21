package controller.server;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.dao.UserDAO;
import utility.FilesUtil;
import utility.common_util;
import config.Config;

/**
 *
 * @author Administrator
 */
public class ServerThreadPI extends Thread {
	private Socket socket = null;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private String user_token;
	private String user_session;

	private static int idListFile = 1;

	public ServerThreadPI(Socket _socket) throws IOException {
		this.socket = _socket;
		this.br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		this.bw = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
	}

	@SuppressWarnings("unchecked")
	public void run() {
		try {
			// NOTE: Get user info client send then check login
			String userInfo = br.readLine();
			HashMap<String, String> pairs = new HashMap<String, String>();
			pairs = new Gson().fromJson(userInfo, pairs.getClass());
			String username = pairs.get("username");
			String password = pairs.get("password");

			HashMap<String, String> resPairs = new HashMap<String, String>();
			if (checkLogin(username, password) == true) {
				user_session = username;
				user_token = common_util.md5(username + common_util.md5(password));

				// NOTE: Handshake Config.PORT_DTP
				resPairs.put("status", "success");
				resPairs.put("message", Config.PORT_DTP + "");
				resPairs.put("user_token", user_token);
				ServerPI.write(bw, new Gson().toJson(resPairs));
			} else {
				resPairs.put("status", "fail");
				resPairs.put("message", "Username/Password incorrect!");
				ServerPI.write(bw, new Gson().toJson(resPairs));
				this.br.close();
				this.bw.flush();
				this.bw.close();
				this.socket.close();
				return;
			}

			while (true) {
				String req = br.readLine();
				HashMap<String, String> reqPairs = new HashMap<String, String>();
				reqPairs = new Gson().fromJson(req, reqPairs.getClass());
				Config.print("PI while(true): " + req);

				if (reqPairs.get("user_token").equals(user_token) == false) {
					socket.close();
					break;
				}
				String res = "";
				String payload = reqPairs.get("payload");
				switch (reqPairs.get("action").trim()) {
				case "listFilesAndFolders": {
					res = listFilesAndFolders(payload);
					break;
				}
				case "listDirs": {
					res = listDirs(payload);
					break;
				}
				case "logout": {
					Config.print("logging out");
					res = logout();
					break;
				}
				}
				if (!res.equals("logged out")) {
					ServerPI.write(bw, res);
				} else {
					break;
				}
			}
		} catch (Exception ex) {
			Logger.getLogger(ServerThreadPI.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private String logout() {
		HashMap<String, String> res = new HashMap<String, String>();
		try {
			res.put("status", "success");
			res.put("message", "Server is closing connection");
			String resPairs = new Gson().toJson(res, res.getClass());
			common_util.write(bw, resPairs);
			this.br.close();
			this.bw.flush();
			this.bw.close();
			this.socket.close();
			return "logged out";
		} catch (Exception e) {
			res.put("status", "failed");
			res.put("message", e.getMessage());
			String resPairs = new Gson().toJson(res, res.getClass());
			return resPairs;
		}
	}

	private boolean checkLogin(String username, String password) throws Exception {
		UserDAO ud = new UserDAO();
		ResultSet rs = ud.get("*", "username = ? AND password = ?",
				new String[] { username, common_util.md5(password) });
		return rs.next() != false;
	}

	private String listDirs(String path) {
		Config.print("listDirs: " + Config.PATH_UPLOAD + "/" + user_session + path);
		idListFile = 1;
		ArrayList<HashMap<String, String>> result = new ArrayList<>();
		addNode(result, Config.PATH_UPLOAD + "/" + user_session + path, 0);
		return new Gson().toJson(result);
	}

	private void addNode(ArrayList<HashMap<String, String>> result, String path, int parentId) {
		File directory = new File(path);
		File[] fList = directory.listFiles();

		for (int i = 0; i < fList.length; i++) {
			if (fList[i].isDirectory()) {
				HashMap<String, String> pairs = new HashMap<>();
				result.add(pairs);
				result.get(parentId).put(idListFile + "", fList[i].getName());
				addNode(result, fList[i].getAbsolutePath(), idListFile++);
			}
		}
	}

	private String listFilesAndFolders(String path) {
		Config.print("listFilesAndFolders: " + Config.PATH_UPLOAD + "/" + user_session + path);
		ArrayList<String> listFiles = FilesUtil.listFiles(Config.PATH_UPLOAD + "/" + user_session + path);
		ArrayList<String> listFolderes = FilesUtil.listFolders(Config.PATH_UPLOAD + "/" + user_session + path);

		HashMap<String, ArrayList<String>> pairs = new HashMap<>();
		pairs.put("files", listFiles);
		pairs.put("folders", listFolderes);

		Config.print("listFilesAndFolders: " + new Gson().toJson(pairs));
		return new Gson().toJson(pairs);
	}
}
