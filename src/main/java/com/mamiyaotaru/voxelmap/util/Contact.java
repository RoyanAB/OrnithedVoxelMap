package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.textures.Sprite;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class Contact {
    public double x;
    public double z;
    public int y;
    public int yFudge = 0;
    public float angle;
    public double distance;
    public float brightness;
    public EnumMobs type;
    public UUID uuid = null;
    public String name = "_";
    public int rotationFactor = 0;
    public String skinURL = "";
    public Entity entity = null;
    public Sprite[] icons = null;
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
        this.x = this.entity.prevX + (this.entity.x - this.entity.prevX) * MinecraftClient.getInstance().getTickDelta();
        this.y = (int) this.entity.y + this.yFudge;
        this.z = this.entity.prevZ + (this.entity.z - this.entity.prevZ) * MinecraftClient.getInstance().getTickDelta();
    }
}
