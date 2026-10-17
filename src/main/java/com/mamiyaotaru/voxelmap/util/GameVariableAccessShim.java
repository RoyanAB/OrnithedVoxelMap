package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import net.minecraft.client.Minecraft;

public class GameVariableAccessShim {
	private static final Minecraft minecraft = VoxelConstants.getMinecraft();

	public static int xCoord() {
		return (int) (minecraft.getRenderViewEntity().posX < 0.0 ? minecraft.getRenderViewEntity().posX - 1.0 : minecraft.getRenderViewEntity().posX);
	}

	public static int zCoord() {
		return (int) (minecraft.getRenderViewEntity().posZ < 0.0 ? minecraft.getRenderViewEntity().posZ - 1.0 : minecraft.getRenderViewEntity().posZ);
	}

	public static int yCoord() {
		return (int) Math.ceil(minecraft.getRenderViewEntity().posY);
	}

	public static double xCoordDouble() {
		return minecraft.getRenderViewEntity().prevPosX
			+ (minecraft.getRenderViewEntity().posX - minecraft.getRenderViewEntity().prevPosX) * minecraft.getRenderPartialTicks();
	}

	public static double zCoordDouble() {
		return minecraft.getRenderViewEntity().prevPosZ
			+ (minecraft.getRenderViewEntity().posZ - minecraft.getRenderViewEntity().prevPosZ) * minecraft.getRenderPartialTicks();
	}

	public static float rotationYaw() {
		return minecraft.getRenderViewEntity().prevRotationYaw
			+ (minecraft.getRenderViewEntity().rotationYaw - minecraft.getRenderViewEntity().prevRotationYaw) * minecraft.getRenderPartialTicks();
	}
}
