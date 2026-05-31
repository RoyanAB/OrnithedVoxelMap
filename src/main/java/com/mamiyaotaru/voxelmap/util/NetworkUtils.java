package com.mamiyaotaru.voxelmap.util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

@SuppressWarnings("unused")
public class NetworkUtils {
	private static ArrayList<InterfaceAddress> interfaceAddresses;

	public static void enumerateInterfaces() throws SocketException {
		interfaceAddresses = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {
			try {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface != null && !networkInterface.isLoopback() && networkInterface.isUp()) {
					Enumeration<NetworkInterface> subInterfaces = networkInterface.getSubInterfaces();

					while (subInterfaces.hasMoreElements()) {
						try {
							NetworkInterface subNetworkInterface = subInterfaces.nextElement();

							for (InterfaceAddress interfaceAddress : subNetworkInterface.getInterfaceAddresses()) {
								if (interfaceAddress != null) {
									interfaceAddresses.add(interfaceAddress);
								}
							}
						} catch (Exception ignored) {
						}
					}

					for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
						if (interfaceAddress != null) {
							interfaceAddresses.add(interfaceAddress);
						}
					}
				}
			} catch (Exception ignored) {
			}
		}
	}

	public static boolean isOnLan(InetAddress serverAddress) {
		for (InterfaceAddress address : interfaceAddresses) {
			try {
				if (onSameNetwork(serverAddress, address.getAddress(), address.getNetworkPrefixLength())) {
					return true;
				}
			} catch (Exception ignored) {
			}
		}

		return false;
	}

	private static boolean onSameNetwork(InetAddress a, InetAddress b, int mask) {
		return onSameNetwork(a.getAddress(), b.getAddress(), mask);
	}

	private static boolean onSameNetwork(byte[] x, byte[] y, int mask) {
		if (x == y) {
			return true;
		}

		if (x != null && y != null) {
			if (x.length != y.length) {
				return false;
			}

			int bits = mask & 7;
			int bytes = mask >>> 3;

			for (int i = 0; i < bytes; i++) {
				if (x[i] != y[i]) {
					return false;
				}
			}

			int shift = 8 - bits;
			return bits == 0 || x[bytes] >>> shift == y[bytes] >>> shift;
		} else {
			return false;
		}
	}
}
