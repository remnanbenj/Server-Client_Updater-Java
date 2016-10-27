package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Scanner;

public class PatchInfo {

	public int packetSize;
	public double packetsRecieved = 0;
	public double packetAmt = 0;
	public String patchNumber;
	public ArrayList<byte[]> dataPackets = new ArrayList<byte[]>();

	public PatchInfo(String info) {
		Scanner sc = new Scanner(info.split("/c/|/e/")[1]);
		patchNumber = sc.next();
		packetAmt = Integer.parseInt(sc.next());
		packetSize = Integer.parseInt(sc.next());
		sc.close();
	}

	public PatchInfo(String patchNumber, double packetsRecieved, double packetAmt, int packetSize) {
		this.patchNumber = patchNumber;
		this.packetsRecieved = packetsRecieved;
		this.packetAmt = packetAmt;
		this.packetSize = packetSize;
	}

	public String toString() {
		return patchNumber + " " + packetAmt + " " + packetSize;
	}

	public double getPercent() {
		if (packetsRecieved == packetAmt)
			return 100;
		double packetsPerPercent = packetAmt / 100;
		if (packetsPerPercent == 0)
			return 0;
		return packetsRecieved / packetsPerPercent;
	}

	public void writeNewData(String oldPatch) {
		byte[] data = new byte[(int) (packetSize * packetAmt)];
		for (int i = 0; i < dataPackets.size(); i++) {
			System.arraycopy(dataPackets.get(i), 0, data, i * packetSize, packetSize);
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(patchNumber + ".zip");
			out.write(data);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(oldPatch + ".zip");
		try {
			Files.delete(file.toPath());
		} catch (NoSuchFileException x) {
			// System.err.format("%s: no such" + " file or directory%n", file.toPath());
		} catch (DirectoryNotEmptyException x) {
			// System.err.format("%s not empty%n", file.toPath());
		} catch (IOException x) {
			// File permission problems are caught here.
			// System.err.println(x);
		}
	}

	public void save() {
		byte[] data = new byte[(int) (packetSize * packetsRecieved)];
		for (int i = 0; i < dataPackets.size(); i++) {
			System.arraycopy(dataPackets.get(i), 0, data, i * packetSize, packetSize);
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(patchNumber);
			out.write(data);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		File file = new File(patchNumber);
		try {
			byte[] dataFull = Files.readAllBytes(file.toPath());
			int i = 0;
			while (dataFull.length > i * packetSize) {
				byte[] data = new byte[packetSize];
				System.arraycopy(dataFull, i * packetSize, data, 0, packetSize);
				dataPackets.add(data);
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

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

	}
}
