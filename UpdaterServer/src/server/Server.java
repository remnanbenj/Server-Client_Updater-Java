package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {

	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponce = new ArrayList<Integer>();

	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private boolean raw = false;

	private PatchInfo currentPatch;

	private final int MAX_ATTEMPTS = 5;

	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		currentPatch = new PatchInfo();
		run = new Thread(this, "Server");
		run.start();
	}

	public void run() {
		running = true;
		System.out.println("Server Started on port: " + port);
		// manageClients();
		receive();

		// Parses system in
		Scanner scanner = new Scanner(System.in);
		while (running) {
			String text = scanner.nextLine(); // Just waits and listens
			if (text.length() < 1) {
				continue;
			}

			// If it doesn't start with a forward slash send it as a message to all clients from the server
			if (!text.startsWith("/")) {
				sendToAll("/m/Server: " + text + "/e/");
				continue;
			}

			// Forward slash commands
			text = text.substring(1);
			if (text.equals("raw")) {
				if (raw)
					System.out.println("Raw mode off.");
				else
					System.out.println("Raw mode on");
				raw = !raw;
			} else if (text.equals("clients")) {
				System.out.println("=======================");
				for (int i = 0; i < clients.size(); i++) {
					ServerClient c = clients.get(i);
					System.out.println(c.name + ", (" + c.getID() + ")");
				}
				System.out.println("=======================");
			} else if (text.startsWith("kick")) {
				String toKick = text.split(" ")[1];

				// Check if toKick is an ID or a user name
				int id = -1;
				boolean number = true;
				try {
					id = Integer.parseInt(toKick);
				} catch (NumberFormatException e) {
					number = false;
				}

				// If it is a number then disconnect it else find the ID with the username
				if (number) {
					disconnect(id, true);
				} else {
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						if (toKick.equals(c.name)) {
							disconnect(c.getID(), true);
							break;
						}
					}
					System.out.println("Client " + toKick + " doesn't exist to disconnect");
				}
			} else if (text.equals("quit")) {
				quit();
			} else if (text.equals("help")) {
				printHelp();
			} else {
				System.out.println("Unknown Command.");
				printHelp();
			}
		}
		scanner.close();
	}

	/**
	 * Prints all the help commands to system.out
	 */
	private void printHelp() {
		System.out.println("Here is a list of all avaliable commands:");
		System.out.println("=========================================");
		System.out.println("/raw - enables/disables raw mode.");
		System.out.println("/clients - shows all clients connected.");
		System.out.println("/kick [user ID or username] - kicks specified user.");
		System.out.println("/help - prints this list");
		System.out.println("/quit - shuts down server");
	}

	/**
	 * Ping clients and send then the connected user list
	 */
	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {

					// Send a ping to clients
					sendToAll("/i/server/e/");

					// Send connected users
					sendConnectedUsers();

					// Sleep 2 seconds
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// for each client
					for (int i = 0; i < clients.size(); i++) {
						ServerClient client = clients.get(i);
						// If clientResponce doesn't contain current clients ID (if it does it means it returned the ping)
						if (!clientResponce.contains(client.getID())) {
							// If the attempts are at or over the max attempts disconnect them. Else add an attempt
							if (client.attempt >= MAX_ATTEMPTS) {
								disconnect(client.getID(), false);
							} else {
								client.attempt++;
							}
						} else {
							// Else remove it and set the clients attempts to 0
							clientResponce.remove(new Integer(client.getID()));
							client.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}

	/**
	 * Send a string to all users containing all current connected users
	 */
	private void sendConnectedUsers() {
		// If no users are connected then don't send anything
		if (clients.size() <= 0)
			return;

		// Build a string of users names to send out
		String users = "/u/";
		for (int i = 0; i < clients.size() - 1; i++) {
			users += clients.get(i).name + "/n/";
		}
		users += clients.get(clients.size() - 1).name + "/e/";

		// Send the users list to all clients
		sendToAll(users);
	}

	/**
	 * Receive a packet and process it. This continues in a loop until the server is closed
	 */
	private void receive() {

		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (SocketException e) {
					} catch (IOException e) {
						e.printStackTrace();
					}

					process(packet);
				}
			}
		};
		receive.start();
	}

	/**
	 * After receiving a packet this is where it is processed
	 */
	private void process(DatagramPacket packet) {
		String string = new String(packet.getData());

		// If raw is enabled then print out all messages sent to the server
		if (raw)
			System.out.println(string);

		// Process the received packet
		if (string.startsWith("/c/")) {
			// Client attempting to connect
			int id = UniqueIdentifier.getIdentifier();
			// Extract clients name
			String name = string.split("/c/|/e/")[1];
			System.out.println(name + " ID: " + " (" + id + ") connected");
			// Add client to list of connected clients
			clients.add(new ServerClient(name, "", packet.getAddress(), packet.getPort(), id));
			// Send the connection approved response along with the newest version
			String ver = "/c/" + currentPatch.toString() + "/e/";
			send(ver.getBytes(), packet.getAddress(), packet.getPort());
		}
		if (string.startsWith("/u/")) {
			int nextPacket = Integer.parseInt(string.split("/u/|/e/")[1]);
			send(currentPatch.dataPackets.get(nextPacket), packet.getAddress(), packet.getPort());
		}
	}

	/**
	 * Checks if a client with specified name is connected
	 */
	private boolean checkClientConnected(String name) {
		for (ServerClient client : clients) {
			if (client.name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private int getIDFromName(String name) {
		for (ServerClient client : clients) {
			if (client.name.equals(name)) {
				return client.getID();
			}
		}
		return -1;
	}

	/**
	 * Sends a message to all clients
	 */
	public void sendToAll(String message) {
		// Send to clients
		for (ServerClient client : clients) {
			send(message.getBytes(), client.address, client.port);
		}
	}

	/**
	 * Sends a message to a specified address
	 */
	private void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port); // Letter: where it is to go (Address)
				try {
					socket.send(packet); // Post office
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	/**
	 * Disconnects all clients and shuts down the server
	 */
	private void quit() {
		// Disconnect all clients
		for (int i = 0; i < clients.size(); i++) {
			disconnect(clients.get(i).getID(), true);
		}
		// Stop all threads
		running = false;
		// Close socket
		socket.close();
		System.out.println("Server Shut Down...");
	}

	private void disconnect(int id, boolean status) {
		// Find and remove client
		ServerClient client = null;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				client = clients.get(i);
				clients.remove(i);
				break;
			}
		}

		// If client doesn't exist then don't worry about disconnection message
		if (client == null) {
			System.out.println("Client " + id + " doesn't exist to disconnect");
			return;
		}

		send(("/d/Disconnected:" + client.name + "/e/").getBytes(), client.address, client.port);

		// Print disconnection message
		if (status) {
			System.out.println("Client " + "(" + client.getID() + ") @" + client.address.toString() + ":" + client.port + " disconnected: " + client.name);
		} else {
			System.out.println("Client " + "(" + client.getID() + ") @" + client.address.toString() + ":" + client.port + " timed out: " + client.name);
		}
	}

}
