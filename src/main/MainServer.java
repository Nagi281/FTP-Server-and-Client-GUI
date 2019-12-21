package main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import config.Config;
import controller.server.ServerDTP;
import controller.server.ServerPI;

/**
 * @author Administrator
 */
public class MainServer {
	public static void main(String[] args) {
		try {
			InetAddress myHost = InetAddress.getLocalHost();
			Config.print("Server is running on: "+ myHost.getHostAddress());
			Config.print("Server's Name"
					+ ": "+ myHost.getHostName());
			ServerPI serverPI = new ServerPI(Config.PORT_PI);
			ServerDTP serverDTP = new ServerDTP(Config.PORT_DTP);
			new Thread() {
				@Override
				public void run() {
					try {
						serverDTP.listen();
					} catch (IOException ex) {
						Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}.start();

			new Thread() {
				@Override
				public void run() {
					try {
						serverPI.listen();
					} catch (IOException ex) {
						Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}.start();
		} catch (IOException ex) {
			Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
