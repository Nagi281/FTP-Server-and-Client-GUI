package controller.client;

//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import config.Config;
import utility.FilesUtil;
import utility.common_util;

/**
 * @author Administrator
 */
public class ClientDTP {
	@SuppressWarnings("unused")
	private int portDTPServer;
	private Socket clientSocket;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	// private BufferedOutputStream bos = null;
	// private BufferedInputStream bis = null;
	String user_session = null;
	String user_token = null;
	public static int blockSize = 1024;

	private enum transferType {
		ASCII, BINARY
	}

	private transferType transferMode = transferType.ASCII;

	public ClientDTP(String host, int portDTPServer, String user_session, String user_token) throws IOException {
		this.portDTPServer = portDTPServer;
		clientSocket = new Socket(host, portDTPServer);
		this.br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		this.bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		// this.bos = new BufferedOutputStream(clientSocket.getOutputStream());
		// this.bis = new BufferedInputStream(clientSocket.getInputStream());
		this.user_session = user_session;
		this.user_token = user_token;
	}

	public ClientDTP() {

	}

	private boolean verify() throws IOException {
		if (user_session == null || user_token == null) {
			return false;
		}

		Config.print("ClientDTP verify");
		HashMap<String, String> pairs = new HashMap<>();
		pairs.put("action", "verify");
		pairs.put("user_session", user_session);
		pairs.put("user_token", user_token);

		common_util.write(bw, new Gson().toJson(pairs));
		String res = br.readLine();
		return res.equals("success");
	}

	public boolean changeTransferType(String mode) throws IOException {
		Config.print("ClientDTP changeTransferType to + " + mode);
		HashMap<String, String> pairs = new HashMap<>();
		pairs.put("action", "transfer");
		pairs.put("payload", mode);
		common_util.write(bw, new Gson().toJson(pairs));

		String res = br.readLine();
		if (res.equals("success")) {
			if (mode.toUpperCase().equals("A")) {
				transferMode = transferType.ASCII;
				return true;
			} else if (mode.toUpperCase().equals("I")) {
				transferMode = transferType.BINARY;
				return true;
			} else
				return false;
		} else {
			return false;
		}
	}

	private boolean checkPathServerIsFolder(String pathServer) throws IOException {
		Config.print("ClientDTP checkPathIsFolderInServer");
		HashMap<String, String> pairs = new HashMap<>();
		pairs.put("action", "isFolder");
		pairs.put("payload", pathServer);

		common_util.write(bw, new Gson().toJson(pairs));
		String res = br.readLine();
		return res.equals("success");
	}

	private boolean checkPathServerIsFile(String pathServer) throws IOException {
		Config.print("ClientDTP checkPathServerIsFile");
		HashMap<String, String> pairs = new HashMap<>();
		pairs.put("action", "isFile");
		pairs.put("payload", pathServer);

		common_util.write(bw, new Gson().toJson(pairs));
		String res = br.readLine();
		return res.equals("success");
	}

	public String upload(String pathClient, String pathServer) {
		HashMap<String, String> pairs = new HashMap<>();

		try {
			if (!verify()) {
				pairs.put("status", "fail");
				pairs.put("message", "Verify fail!");
				return new Gson().toJson(pairs);
			}
			if (!FilesUtil.isFile(pathClient)) {
				pairs.put("status", "fail");
				pairs.put("message", "Path client isn't a file!");
				return new Gson().toJson(pairs);
			}
			if (!checkPathServerIsFolder(pathServer)) {
				pairs.put("status", "fail");
				pairs.put("message", "Path server isn't a folder!");
				return new Gson().toJson(pairs);
			}

			File f = new File(pathClient);
			// long length = (long) Math.ceil((double) f.length() / blockSize);
			long length = f.length();
			if (length > 1024 * blockSize && transferMode == transferType.ASCII) {
				pairs.put("status", "fail");
				pairs.put("message", "File too large for ASCII, Change to Binary!");
				return new Gson().toJson(pairs);
			}
			FileInputStream fin = new FileInputStream(f);
			pairs.put("action", "upload");
			pairs.put("pathServer", pathServer);
			pairs.put("filename", f.getName());
			pairs.put("length", length + "");
			common_util.write(bw, new Gson().toJson(pairs));

			if (transferMode == transferType.BINARY) {
				Config.print("Uploading in Binary mode: .... ");
				byte[] bytes = new byte[blockSize];
				InputStream in = new FileInputStream(f);
				OutputStream out = clientSocket.getOutputStream();

				int count, index = 0;
				while ((count = in.read(bytes)) > 0) {
					out.write(bytes, 0, count);
					Config.print(index++ + " " + count);
				}
				// while ((count = in.read(bytes)) > 0) {
				// bos.write(bytes, 0, count);
				// Config.print(index++ + " " + count);
				// }
				// Config.print("Package length: " + length);
				in.close();

				Config.print("Ending process!");
			} else {
				Config.print("Uploading in ASCII mode: .... ");
				int c;
				do {
					c = fin.read();
					common_util.write(bw, String.valueOf(c));
				} while (c != -1);
				fin.close();

			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String res = br.readLine();
			Config.print("DTP upload: " + res);
			return res;
		} catch (IOException ex) {
			Logger.getLogger(ClientDTP.class.getName()).log(Level.SEVERE, null, ex);

			pairs.put("status", "fail");
			pairs.put("message", ex.getMessage());
			return new Gson().toJson(pairs);
		}
	}

	public String download(String pathClient, String pathServer) {
		HashMap<String, String> pairs = new HashMap<>();
		try {
			if (verify() == false) {
				pairs.put("status", "fail");
				pairs.put("message", "Verify fail!");
				return new Gson().toJson(pairs);
			}

			if (FilesUtil.isFolder(pathClient) == false) {
				pairs.put("status", "fail");
				pairs.put("message", "Path client isn't a folder!");
				return new Gson().toJson(pairs);
			}

			if (checkPathServerIsFile(pathServer) == false) {
				pairs.put("status", "fail");
				pairs.put("message", "Path client isn't a file!");
				return new Gson().toJson(pairs);
			}

			Config.print("ClientDTP download");
			pairs.put("action", "download");
			pairs.put("pathServer", pathServer);
			common_util.write(bw, new Gson().toJson(pairs));

			String info = br.readLine();
			Config.print(info);
			String[] fileInfo = info.split(":");
			File f = new File(pathClient + "/" + fileInfo[0]);

			FileOutputStream fout = new FileOutputStream(f);
			if (transferMode == transferType.BINARY) {
				Config.print("Downloading in Binary mode: .... ");
				InputStream in = clientSocket.getInputStream();
				OutputStream out2 = new FileOutputStream(f);
				byte[] bytes = new byte[blockSize];
				int count, index = 0, total = 0;
				long size = Long.parseLong(fileInfo[1]);
				while ((total <= size - blockSize) && (count = in.read(bytes)) > 0) {
					out2.write(bytes, 0, count);
					Config.print(index++ + " " + count);
					total += count;
				}
				count = in.read(bytes);
				if (count > 0) {
					out2.write(bytes, 0, count);
					Config.print("Last Package " + count);
				}

				// while ((total <= size) && (count = bis.read(bytes)) > 0) {
				// out2.write(bytes, 0, count);
				// Config.print(index++ + " " + count);
				// total += count;
				// }

				Config.print("File length: " + size);
				out2.close();
				Config.print("Ending Process2!");
			} else {
				Config.print("Downloading in ASCII mode: .... ");
				int ch;
				String temp;
				do {
					temp = br.readLine();
					if (temp.equals("")) {
						break;
					}
					ch = Integer.parseInt(temp);
					if (ch != -1) {
						fout.write(ch);
					}
				} while (ch != -1);
				fout.close();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String res = br.readLine();
			Config.print("DTP upload: " + res);
			return res;
		} catch (IOException ex) {
			Logger.getLogger(ClientDTP.class.getName()).log(Level.SEVERE, null, ex);

			pairs.put("status", "fail");
			pairs.put("message", ex.getMessage());
			return new Gson().toJson(pairs);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean closeConnect() {
		try {
			HashMap<String, String> pairs = new HashMap<String, String>();
			pairs.put("action", "logout");
			String json = new Gson().toJson(pairs);
			ClientPI.write(bw, json);

			String res = br.readLine();
			HashMap<String, String> resPairs = new HashMap<String, String>();
			resPairs = new Gson().fromJson(res, resPairs.getClass());
			if (!resPairs.get("status").equals("success")) {
				return false;
			} else {
				this.br.close();
				this.bw.flush();
				this.bw.close();
				this.clientSocket.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
