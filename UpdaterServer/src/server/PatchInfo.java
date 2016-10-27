package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class PatchInfo {

	public int packetAmt;
	public String currentVersion;
	public int packetSize = 1024;
	public ArrayList<byte[]> dataPackets = new ArrayList<byte[]>();

	public PatchInfo() {
		Scanner sc;
		try {
			sc = new Scanner(new File("info"));
			currentVersion = sc.nextLine();
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		getPatchData();
	}

	public String toString() {
		return currentVersion + " " + packetAmt + " " + packetSize;
	}

	private void getPatchData() {
		try {
			// Open the file contains this patches data
			File file = new File(currentVersion + ".zip");
			byte[] data = Files.readAllBytes(file.toPath());
			double d = data.length;
			double d2 = d / packetSize;
			double d3 = d % packetSize;
			packetAmt = (int) (d2 + 1);

			// Split into packets;
			for (int i = 0; i < packetAmt - 1; i++) {
				byte[] packet = new byte[packetSize];
				System.arraycopy(data, i * packetSize, packet, 0, packetSize);
				dataPackets.add(packet);
			}
			byte[] packet = new byte[(int) d3];
			System.arraycopy(data, (packetAmt - 1) * packetSize, packet, 0, (int) d3);
			dataPackets.add(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * double d = str.length(); double d2 = d / packetSize; double d3 = d % packetSize; packetAmt = (int) d2 + 1;
	 */
}
