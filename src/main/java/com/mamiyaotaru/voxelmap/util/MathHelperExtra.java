package com.mamiyaotaru.voxelmap.util;

public class MathHelperExtra {
	public static int ceil(float value) {
		int i = (int)value;
		return value > i ? i + 1 : i;
	}

	public static int ceil(double value) {
		int i = (int)value;
		return value > i ? i + 1 : i;
	}

	public static int smallestEncompassingPowerOfTwo(int value) {
		int i = value - 1;
		i |= i >> 1;
		i |= i >> 2;
		i |= i >> 4;
		i |= i >> 8;
		i |= i >> 16;
		return i + 1;
	}
}
