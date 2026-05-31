package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.textures.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import java.util.UUID;

@SuppressWarnings("unused")
public class Contact {
	public double x;
	public double z;
	public int y;
	public int yFudge = 0;
	public float angle;
	public double distance;
	public float brightness;
	public EnumMobs type;
	public UUID uuid;
	public String name = "_";
	public int rotationFactor = 0;
	public Entity entity;
	public Sprite[] icons;
	public Sprite[] armorIcons = new Sprite[]{null, null};
	public int armorColor = -1;

	public Contact(Entity entity, EnumMobs type) {
		this.entity = entity;
		this.type = type;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRotationFactor(int rotationFactor) {
		this.rotationFactor = rotationFactor;
	}

	public void setArmorColor(int armorColor) {
		this.armorColor = armorColor;
	}

	public void updateLocation() {
		this.x = this.entity.prevPosX + (this.entity.posX - this.entity.prevPosX) * Minecraft.getMinecraft().getRenderPartialTicks();
		this.y = (int) this.entity.posY + this.yFudge;
		this.z = this.entity.prevPosZ + (this.entity.posZ - this.entity.prevPosZ) * Minecraft.getMinecraft().getRenderPartialTicks();
	}
}
