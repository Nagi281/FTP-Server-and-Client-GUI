package main;

import controller.client.ClientPI;
import view.GUI.MainClientGUI2;

/**
 * @author Administrator
 */
public class MainClient {
	public static void main(String[] args) {
		ClientPI clientPI = new ClientPI();
		new MainClientGUI2(clientPI).setVisible(true);
	}
}
