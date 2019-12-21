package controller.client;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import config.Config;

/**
 *
 * @author Administrator
 */
public class ClientPI {
	private ClientDTP clientDTP;
	private Socket clientSocket;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private String hostDTP;
	private int portDTP;
	private String user_token;
	private String user_session;

	public String getUserSession() {
		return this.user_session;
	}

	public String getUserToken() {
		return this.user_token;
	}

	public ClientDTP getClientDTP() {
		return this.clientDTP;
	}

	public static void write(BufferedWriter bw, String res) throws IOException {
		bw.write(res);
		bw.newLine();
		bw.flush();
	}

	public HashMap<String, String> connect(String host, String username, String password, String port) {
		HashMap<String, String> message = new HashMap<String, String>();

		try {
			Config.print("connect " + host + ": " + port);
			clientSocket = new Socket(host, Integer.parseInt(port));
			this.br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

			// NOTE: Send username/password to server check login
			verifyLogin(username, password);

			// NOTE: Create Client DTP to handle file transfer
			hostDTP = host;
			clientDTP = new ClientDTP(hostDTP, portDTP, user_session, user_token);
			message.put("OK", "Done");
		} catch (Exception ex) {
			Config.print(ex.getMessage());
			message.put("error", ex.getMessage());
			closeConnect();
		}

		return message;
	}

	@SuppressWarnings("unchecked")
	public void verifyLogin(String username, String password) throws Exception {
		// NOTE: Send user info to server check login
		HashMap<String, String> pairs = new HashMap<String, String>();
		pairs.put("username", username);
		pairs.put("password", password);
		String json = new Gson().toJson(pairs);
		ClientPI.write(bw, json);

		// NOTE: Get response from server about login fail/success
		String res = br.readLine();
		HashMap<String, String> resPairs = new HashMap<String, String>();
		resPairs = new Gson().fromJson(res, resPairs.getClass());

		String status = resPairs.get("status");
		if (status.equals("fail")) {
			throw new Exception(resPairs.get("message"));
		} else {
			user_token = resPairs.get("user_token");
			user_session = username;

			// NOTE: Receive portDTP from handshake
			portDTP = Integer.parseInt(resPairs.get("message"));
			Config.print("verifyLogin port: " + portDTP);
			Config.print("verifyLogin user_token: " + user_token);
		}
	}

	@SuppressWarnings("unchecked")
	public String logout() {
		try {
			// NOTE: Send user info to server check login
			HashMap<String, String> pairs = new HashMap<String, String>();
			pairs.put("user_token", user_token);
			pairs.put("action", "logout");
			String json = new Gson().toJson(pairs);
			ClientPI.write(bw, json);

			// NOTE: Get response from server about logout fail/success
			String res = br.readLine();
			HashMap<String, String> resPairs = new HashMap<String, String>();
			resPairs = new Gson().fromJson(res, resPairs.getClass());

			String status = resPairs.get("status");
			if (status.equals("failed")) {
				return resPairs.get("message");
			} else {
				user_token = null;
				user_session = null;
				Config.print("Logout confirmed");
				this.closeConnect();
				if (clientDTP.closeConnect())
					return "success";
				else {
					return "failed";
				}
			}
		} catch (IOException e) {
			return "failed";
		}
	}

	public String listFilesAndFoldersFromServer(String path) {
		String res = "";

		try {
			HashMap<String, String> pairs = new HashMap<>();
			pairs.put("user_token", user_token);
			pairs.put("action", "listFilesAndFolders");
			pairs.put("payload", path);
			ClientPI.write(bw, new Gson().toJson(pairs));

			// NOTE: Receive response for request
			res = br.readLine();
		} catch (IOException ex) {
			Logger.getLogger(ClientPI.class.getName()).log(Level.SEVERE, null, ex);
		}

		return res;
	}

	public String listDirsFromServer(String path) {
		String res = "";
		try {
			HashMap<String, String> pairs = new HashMap<>();
			pairs.put("user_token", user_token);
			pairs.put("action", "listDirs");
			pairs.put("payload", path);
			ClientPI.write(bw, new Gson().toJson(pairs));
			// NOTE: Receive response for request
			res = br.readLine();
		} catch (IOException ex) {
			Logger.getLogger(ClientPI.class.getName()).log(Level.SEVERE, null, ex);
		}
		return res;
	}

	private void closeConnect() {
		try {
			this.br.close();
			this.bw.flush();
			this.bw.close();
			this.clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
