package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;

import util.PatchInfo;

public class Client {

	// Client fields
	private String currentVersion;
	private boolean newVersion = false;
	private boolean updating = false;

	// Network fields
	private DatagramSocket socket;
	private int port = 8197;
	private InetAddress ip;
	private Thread send;
	private boolean connected;
	private PatchInfo patchInfo;

	public Client() {
		currentVersion = getCurrentVersion();
		if (patchInfo != null) {
			// We are part way through DLing next patch
			PatchInfo checkConnected = openConnection("192.168.1.201");
			if (checkConnected != null) {
				// if the servers patch is higher than the patch we're downloading
				if (!checkConnected.patchNumber.equals(patchInfo.patchNumber)) {
					System.out.println("TODO: SERVER IS FURTHER THAN CURRENT HALF UPDATE. DITCH DLed FILES AND DL NEW PATCH");
					File file = new File(patchInfo.patchNumber);
					try {
						Files.delete(file.toPath());
					} catch (NoSuchFileException x) {
						System.err.format("%s: no such" + " file or directory%n", file.toPath());
					} catch (DirectoryNotEmptyException x) {
						System.err.format("%s not empty%n", file.toPath());
					} catch (IOException x) {
						// File permission problems are caught here.
						System.err.println(x);
					}
					patchInfo = checkConnected;
					newVersion = true;
					connected = true;
				} else {
					newVersion = true;
					connected = true;
					patchInfo.load();
				}
			} else {
				connected = false;
			}
		} else {
			// Connect to server and get version from confirmation packet
			patchInfo = openConnection("192.168.1.201");
			if (patchInfo == null) {
				connected = false;
			} else {
				newVersion = latestUpdateOnline();
				connected = true;
			}
		}
	}

	private boolean latestUpdateOnline() {
		if (Integer.parseInt(currentVersion.split("\\.")[0]) < Integer.parseInt(patchInfo.patchNumber.split("\\.")[0])) {
			return true;
		} else if (Integer.parseInt(currentVersion.split("\\.")[1]) < Integer.parseInt(patchInfo.patchNumber.split("\\.")[1])) {
			return true;
		} else if (Integer.parseInt(currentVersion.split("\\.")[2]) < Integer.parseInt(patchInfo.patchNumber.split("\\.")[2])) {
			return true;
		}
		return false;
	}

	public void update() {
		if (updating) {
			if (patchInfo.getPercent() == 100) {
				handleDLFinished();
			} else {
				receivePatchPacket();
			}
		}
	}

	public void receivePatchPacket() {
		send("/u/" + ((int) patchInfo.packetsRecieved) + "/e/");
		byte[] result = receive2(2000);
		if (result == null) {
			System.err.println("Failed to receive last packet");
			return;
		}
		patchInfo.dataPackets.add(result);
		patchInfo.packetsRecieved++;
	}

	private void handleDLFinished() {
		if (newVersion) {
			updating = false;
			newVersion = false;
			String temp = currentVersion;
			currentVersion = patchInfo.patchNumber;
			patchInfo.packetsRecieved = 0;
			patchInfo.writeNewData(temp);
			updateInfoVersion();
		}
	}

	// GETTERS

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public boolean isUpdating() {
		return updating;
	}

	public boolean checkIfUpdateNeeded() {
		return newVersion;
	}

	public String getVersion() {
		return currentVersion;
	}

	public String getNewVersion() {
		return patchInfo.patchNumber;
	}

	public int getPercent() {
		if (patchInfo != null)
			return (int) patchInfo.getPercent();
		return 0;
	}

	// VERSION STUFF

	private String getCurrentVersion() {
		File file = new File("info");
		if (file.exists()) {
			String cVer = null;
			try {
				Scanner sc = new Scanner(file);
				// Current version to return
				cVer = sc.nextLine();

				// if the next version is here then we must be halfway through downloading it
				if (sc.hasNextLine()) {
					String newVer;
					double packetsRecieved;
					double packetAmt;
					int packetSize;
					newVer = sc.nextLine();
					packetsRecieved = Double.parseDouble(sc.nextLine());
					packetAmt = Double.parseDouble(sc.nextLine());
					packetSize = Integer.parseInt(sc.nextLine());
					patchInfo = new PatchInfo(newVer, packetsRecieved, packetAmt, packetSize);
				}

				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return cVer;
		} else {
			FileOutputStream out;
			try {
				String str = "0.0.0";
				out = new FileOutputStream("info");
				out.write(str.getBytes());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return "0.0.0";
		}

	}

	private void updateInfoVersion() {
		File file = new File("info");
		FileOutputStream out;
		try {
			String str = patchInfo.patchNumber;
			out = new FileOutputStream("info");
			out.write(str.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// NETWORK STUFF

	/**
	 * Attempts to connect to the server. Returns servers version if connected. Null if not.
	 */
	private PatchInfo openConnection(String address) {
		try {
			// obtain socket of OS's choosing
			socket = new DatagramSocket();
			ip = InetAddress.getByName(address);

			// Send connection packet
			send("/c/" + "nameOfClient" + "/e/");

			// Receive patch version info packet
			String result = receive(2000);
			if (result == null) {
				return null;
			}
			return new PatchInfo(result);
		} catch (UnknownHostException e) {
			return null;
		} catch (SocketException e) {
			return null;
		}
	}

	/**
	 * Sends a packet to the server
	 */
	public void send(String message) {
		byte[] data = message.getBytes();
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	/**
	 * Blocks until it receives a packet or if the timeout parameter clocks out. Set timeout to 0 if you want to block infinitely until it receives a packet.
	 */
	public String receive(int timeout) {
		byte[] data = new byte[1024]; // Creates byte array
		DatagramPacket packet = new DatagramPacket(data, data.length); // Creates empty packet
		try {
			socket.setSoTimeout(timeout); // Set timeout time
			socket.receive(packet); // Fills packet
		} catch (SocketTimeoutException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData()); // Turns the packet into a String
		return message;
	}

	/**
	 * Blocks until it receives a packet or if the timeout parameter clocks out. Set timeout to 0 if you want to block infinitely until it receives a packet.
	 */
	public byte[] receive2(int timeout) {
		byte[] data = new byte[patchInfo.packetSize]; // Creates byte array
		DatagramPacket packet = new DatagramPacket(data, data.length); // Creates empty packet
		try {
			socket.setSoTimeout(timeout); // Set timeout time
			socket.receive(packet); // Fills packet
		} catch (SocketTimeoutException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return packet.getData();
	}

	public boolean isConnected() {
		return connected;
	}

	public void exit() {
		if (patchInfo != null && patchInfo.packetsRecieved > 0) {
			save();
		}
		System.exit(0);
	}

	public void save() {
		patchInfo.save();
		File file = new File("info");
		FileOutputStream out;
		try {
			String str = currentVersion + "\n" + patchInfo.patchNumber + "\n" + patchInfo.packetsRecieved + "\n" + patchInfo.packetAmt + "\n" + patchInfo.packetSize;
			out = new FileOutputStream("info");
			out.write(str.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
