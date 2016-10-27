package server;

import java.net.InetAddress;

public class ServerClient {

	public String name;
	public String password;
	public InetAddress address;
	public int port;
	private final int ID; // ID for current session
	public int attempt = 0; // attempts to ping client

	public ServerClient(String name, String password, InetAddress address, int port, final int ID) {
		this.name = name;
		this.password = password;
		this.address = address;
		this.port = port;
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}

}
