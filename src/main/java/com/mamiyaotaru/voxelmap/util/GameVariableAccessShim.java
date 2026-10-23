package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import net.minecraft.client.Minecraft;

public class GameVariableAccessShim {
	public static int xCoord() {
		return (int) (VoxelConstants.getMinecraft().getRenderViewEntity().posX < 0.0 ? VoxelConstants.getMinecraft().getRenderViewEntity().posX - 1.0 : VoxelConstants.getMinecraft().getRenderViewEntity().posX);
	}

	public static int zCoord() {
		return (int) (VoxelConstants.getMinecraft().getRenderViewEntity().posZ < 0.0 ? VoxelConstants.getMinecraft().getRenderViewEntity().posZ - 1.0 : VoxelConstants.getMinecraft().getRenderViewEntity().posZ);
	}

	public static int yCoord() {
		return (int) Math.ceil(VoxelConstants.getMinecraft().getRenderViewEntity().posY);
	}

	public static double xCoordDouble() {
		return VoxelConstants.getMinecraft().getRenderViewEntity().prevPosX
			+ (VoxelConstants.getMinecraft().getRenderViewEntity().posX - VoxelConstants.getMinecraft().getRenderViewEntity().prevPosX) * VoxelConstants.getMinecraft().getRenderPartialTicks();
	}

	public static double zCoordDouble() {
		return VoxelConstants.getMinecraft().getRenderViewEntity().prevPosZ
			+ (VoxelConstants.getMinecraft().getRenderViewEntity().posZ - VoxelConstants.getMinecraft().getRenderViewEntity().prevPosZ) * VoxelConstants.getMinecraft().getRenderPartialTicks();
	}

	public static float rotationYaw() {
		return VoxelConstants.getMinecraft().getRenderViewEntity().prevRotationYaw
			+ (VoxelConstants.getMinecraft().getRenderViewEntity().rotationYaw - VoxelConstants.getMinecraft().getRenderViewEntity().prevRotationYaw) * VoxelConstants.getMinecraft().getRenderPartialTicks();
	}
}
