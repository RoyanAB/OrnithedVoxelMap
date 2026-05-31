package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import java.io.File;
import java.util.Objects;

@SuppressWarnings("unused")
public class GameVariableAccessShim {
	private static final Minecraft minecraft = Minecraft.getMinecraft();

	public static Minecraft getMinecraft() {
		return minecraft;
	}

	public static World getWorld() {
		return minecraft.world;
	}

	public static File getDataDir() {
		return minecraft.gameDir;
	}

	public static int xCoord() {
		return (int) (Objects.requireNonNull(minecraft.getRenderViewEntity()).posX < 0.0 ? minecraft.getRenderViewEntity().posX - 1.0 : minecraft.getRenderViewEntity().posX);
	}

	public static int zCoord() {
		return (int) (Objects.requireNonNull(minecraft.getRenderViewEntity()).posZ < 0.0 ? minecraft.getRenderViewEntity().posZ - 1.0 : minecraft.getRenderViewEntity().posZ);
	}

	public static int yCoord() {
		return (int) Math.ceil(Objects.requireNonNull(minecraft.getRenderViewEntity()).posY);
	}

	public static double xCoordDouble() {
		return Objects.requireNonNull(minecraft.getRenderViewEntity()).prevPosX
			+ (minecraft.getRenderViewEntity().posX - minecraft.getRenderViewEntity().prevPosX) * minecraft.getRenderPartialTicks();
	}

	public static double zCoordDouble() {
		return Objects.requireNonNull(minecraft.getRenderViewEntity()).prevPosZ
			+ (minecraft.getRenderViewEntity().posZ - minecraft.getRenderViewEntity().prevPosZ) * minecraft.getRenderPartialTicks();
	}

	public static float rotationYaw() {
		return Objects.requireNonNull(minecraft.getRenderViewEntity()).prevRotationYaw
			+ (minecraft.getRenderViewEntity().rotationYaw - minecraft.getRenderViewEntity().prevRotationYaw) * minecraft.getRenderPartialTicks();
	}
}
