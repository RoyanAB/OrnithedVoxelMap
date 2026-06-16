package com.mamiyaotaru.voxelmap.util;

public class Dimension {
	private String name;
	private final int ID;

	public Dimension(String name, int ID) {
		this.name = name;
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
