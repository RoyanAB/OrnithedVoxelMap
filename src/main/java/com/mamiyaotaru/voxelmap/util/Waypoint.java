package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import java.io.Serializable;
import java.util.Locale;
import java.util.TreeSet;

public class Waypoint implements Serializable, Comparable<Waypoint> {
	private static final long serialVersionUID = 8136790917447997951L;
	public String name;
	public String imageSuffix;
	public String world;
	public TreeSet<Integer> dimensions;
	public int x;
	public int z;
	public int y;
	public boolean enabled;
	public boolean inWorld = true;
	public boolean inDimension = true;
	public float red;
	public float green;
	public float blue;

	public Waypoint(
		String name, int x, int z, int y, boolean enabled, float red, float green, float blue, String suffix, String world, TreeSet<Integer> dimensions
	) {
		this.name = name;
		this.x = x;
		this.z = z;
		this.y = y;
		this.enabled = enabled;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.imageSuffix = suffix.toLowerCase(Locale.ROOT);
		this.world = world;
		this.dimensions = dimensions;
	}

	public int getUnified() {
		return -16777216 + ((int) (this.red * 255.0F) << 16) + ((int) (this.green * 255.0F) << 8) + (int) (this.blue * 255.0F);
	}

	public boolean isActive() {
		return this.enabled && this.inWorld && this.inDimension;
	}

	public int getX() {
		return Minecraft.getMinecraft().player.dimension == -1 ? this.x / 8 : this.x;
	}

	public void setX(int x) {
		this.x = Minecraft.getMinecraft().player.dimension == -1 ? x * 8 : x;
	}

	public int getZ() {
		return Minecraft.getMinecraft().player.dimension == -1 ? this.z / 8 : this.z;
	}

	public void setZ(int z) {
		this.z = Minecraft.getMinecraft().player.dimension == -1 ? z * 8 : z;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int compareTo(Waypoint arg0) {
		double myDistance = this.getDistanceSqToEntity(Minecraft.getMinecraft().player);
		double comparedDistance = arg0.getDistanceSqToEntity(Minecraft.getMinecraft().player);
		return Double.compare(myDistance, comparedDistance);
	}

	public double getDistanceSqToEntity(Entity par1Entity) {
		double var2 = this.getX() + 0.5 - par1Entity.posX;
		double var4 = this.getY() + 0.5 - par1Entity.posY;
		double var6 = this.getZ() + 0.5 - par1Entity.posZ;
		return var2 * var2 + var4 * var4 + var6 * var6;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject) {
			return true;
		}

		if (!(otherObject instanceof Waypoint)) {
			return false;
		}

		Waypoint otherWaypoint = (Waypoint) otherObject;
		return this.name.equals(otherWaypoint.name)
			&& this.imageSuffix.equals(otherWaypoint.imageSuffix)
			&& this.world.equals(otherWaypoint.world)
			&& this.x == otherWaypoint.x
			&& this.y == otherWaypoint.y
			&& this.z == otherWaypoint.z
			&& this.red == otherWaypoint.red
			&& this.green == otherWaypoint.green
			&& this.blue == otherWaypoint.blue
			&& this.dimensions.equals(otherWaypoint.dimensions);
	}
}
