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
public class ServerPI {
	private ServerSocket serverSocket = null;
	private Socket socket = null;

	public ServerPI(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		Config.print("Server PI started at port: " + port);
	}

	public void listen() throws IOException {
		while (true) {
			socket = serverSocket.accept();
			ServerThreadPI st = new ServerThreadPI(socket);
			st.start();
		}
	}

	public static void write(BufferedWriter bw, String res) throws IOException {
		bw.write(res);
		bw.newLine();
		bw.flush();
	}
}
