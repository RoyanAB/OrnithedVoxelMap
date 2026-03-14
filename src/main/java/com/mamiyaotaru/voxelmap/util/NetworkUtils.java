package com.mamiyaotaru.voxelmap.util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

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
                        } catch (Exception var6) {
                        }
                    }

                    for (InterfaceAddress interfaceAddressx : networkInterface.getInterfaceAddresses()) {
                        if (interfaceAddressx != null) {
                            interfaceAddresses.add(interfaceAddressx);
                        }
                    }
                }
            } catch (Exception var7) {
            }
        }
    }

    public static boolean isOnLan(InetAddress serverAddress) {
        for (int t = 0; t < interfaceAddresses.size(); t++) {
            try {
                InterfaceAddress interfaceAddress = interfaceAddresses.get(t);
                if (onSameNetwork(serverAddress, interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength())) {
                    return true;
                }
            } catch (Exception var3) {
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
