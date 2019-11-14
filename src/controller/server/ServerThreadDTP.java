package controller.server;

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
 *
 * @author Administrator
 */
public class ServerThreadDTP extends Thread {
	private Socket socket = null;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	// private BufferedOutputStream bos = null;
	// private BufferedInputStream bis = null;
	private String user_session = null;
	@SuppressWarnings("unused")
	private String user_token = null;
	public static int blockSize = 1024;

	private enum transferType {
		ASCII, BINARY
	}

	private transferType transferMode = transferType.ASCII;

	public ServerThreadDTP(Socket _socket) throws IOException {
		this.socket = _socket;
		this.br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		this.bw = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
		// this.bos = new BufferedOutputStream(_socket.getOutputStream());
		// this.bis = new BufferedInputStream(_socket.getInputStream());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			while (true) {
				String req = br.readLine();
				HashMap<String, String> reqPairs = new HashMap<>();
				reqPairs = new Gson().fromJson(req, reqPairs.getClass());
				Config.print("DTP while(true): " + req);

				String res = "";
				String payload = reqPairs.get("payload");
				switch (reqPairs.get("action").trim()) {
				case "transfer":
					res = changeTransferType(payload);
					break;
				case "isFile": {
					res = isFile(payload);
					break;
				}
				case "isFolder": {
					res = isFolder(payload);
					break;
				}

				case "upload": {
					res = upload(reqPairs.get("filename"), reqPairs.get("pathServer"), reqPairs.get("length"));
					break;
				}

				case "download": {
					res = download(reqPairs.get("pathServer"));
					break;
				}

				case "verify": {
					res = verify(reqPairs.get("user_session"), reqPairs.get("user_token"));
					if (res.equals("fail")) {
						socket.close();
					}
					break;
				}
				case "logout": {
					Config.print("logging out 2");
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

	private String verify(String session, String token) throws IOException {
		// NOTE: Send request to database then verify
		user_session = session;
		user_token = token;
		return "success"; // fail
	}

	private String changeTransferType(String payload) {
		if (payload.toUpperCase().equals("A")) {
			transferMode = transferType.ASCII;
			return "success";
		} else if (payload.toUpperCase().equals("I")) {
			transferMode = transferType.BINARY;
			return "success";
		} else
			return "fail";
	}

	private String isFolder(String path) throws IOException {
		boolean res = FilesUtil.isFolder(Config.PATH_UPLOAD + "/" + user_session + path);
		Config.print("DTP: isFolder: " + Config.PATH_UPLOAD + "/" + user_session + path + ": " + res);
		return res ? "success" : "fail";
	}

	private String isFile(String path) {
		boolean res = FilesUtil.isFile(Config.PATH_UPLOAD + "/" + user_session + path);
		Config.print("DTP: isFile: " + Config.PATH_UPLOAD + "/" + user_session + path + ": " + res);
		return res ? "success" : "fail";
	}

	private String upload(String filename, String pathServer, String length) {
		HashMap<String, String> pairs = new HashMap<>();
		try {
			String name = Config.PATH_UPLOAD + "/" + user_session + pathServer + "/" + filename;
			File f = new File(name);

			// NOTE: Create other file if file already exist
			while (true) {
				if (!f.exists()) {
					break;
				}
				name = Config.PATH_UPLOAD + "/" + user_session + pathServer + "/" + filename;
			}

			FileOutputStream fout = new FileOutputStream(f);
			String tmp;
			if (transferMode == transferType.BINARY) {
				Config.print("Tranfering in Binary mode: .... ");
				InputStream in = socket.getInputStream();
				OutputStream out2 = new FileOutputStream(f);
				byte[] bytes = new byte[blockSize];
				int count, index = 0, total = 0;
				long size = Long.parseLong(length);
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
				int c;
				System.out.println("Hello!!!!");
				do {
					tmp = br.readLine();
					c = Integer.parseInt(tmp);
					if (c != -1) {
						fout.write(c);
					}
				} while (c != -1);
				fout.close();
				Config.print("Ending process!");
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Config.print("DTP: upload file done! ");
			pairs.put("status", "success");
			pairs.put("message", "Upload file done");
			return new Gson().toJson(pairs);
		} catch (Exception ex) {
			Logger.getLogger(ServerThreadDTP.class.getName()).log(Level.SEVERE, null, ex);

			pairs.put("status", "fail");
			pairs.put("message", ex.getMessage());
			return new Gson().toJson(pairs);
		}
	}

	private String download(String pathServer) {
		HashMap<String, String> pairs = new HashMap<>();

		try {
			String name = Config.PATH_UPLOAD + "/" + user_session + pathServer;
			File f = new File(name);
			// long len = (long) Math.ceil((double) f.length() / blockSize);
			long len = f.length();
			// NOTE: Send file name to client save
			common_util.write(bw, f.getName() + ":" + len);

			// NOTE: Send file
			FileInputStream fin = new FileInputStream(f);
			if (transferMode == transferType.BINARY) {
				Config.print("Uploading in Binary mode: .... ");
				byte[] bytes = new byte[blockSize];
				InputStream in = new FileInputStream(f);
				OutputStream out = socket.getOutputStream();
				int count, index = 0;
				while ((count = in.read(bytes)) > 0) {
					out.write(bytes, 0, count);
					Config.print(index++ + " " + count);
				}
				// while ((count = in.read(bytes)) > 0) {
				// bos.write(bytes, 0, count);
				// Config.print(index++ + " " + count);
				// }
				Config.print("Number of package: " + len);
				in.close();
				Config.print("Ending process!");
			} else {
				int c;
				do {
					c = fin.read();
					common_util.write(bw, String.valueOf(c));
				} while (c != -1);
				Config.print("DTP download done: " + c);
				fin.close();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pairs.put("status", "success");
			pairs.put("message", "Download file done");
			return new Gson().toJson(pairs);

		} catch (IOException ex) {
			Logger.getLogger(ServerThreadDTP.class.getName()).log(Level.SEVERE, null, ex);

			pairs.put("status", "fail");
			pairs.put("message", ex.getMessage());
			return new Gson().toJson(pairs);
		}
	}
}
