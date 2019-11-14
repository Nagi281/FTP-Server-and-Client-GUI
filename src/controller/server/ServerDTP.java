package controller.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import config.Config;

/**
 *
 * @author Administrator
 */
public class ServerDTP {
	private ServerSocket serverSocket;
	private Socket socket = null;

	public ServerDTP(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		Config.print("Server DTP started at port: " + port);
	}

	public void listen() throws IOException {
		while (true) {
			socket = serverSocket.accept();
			Config.print("ServerDTP: New client connected!");
			ServerThreadDTP st = new ServerThreadDTP(socket);
			st.start();
		}
	}

	public static void write(BufferedWriter bw, String res) throws IOException {
		bw.write(res);
		bw.newLine();
		bw.flush();
	}
}
