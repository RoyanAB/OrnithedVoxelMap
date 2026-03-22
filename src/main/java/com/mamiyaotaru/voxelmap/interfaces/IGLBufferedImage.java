package com.mamiyaotaru.voxelmap.interfaces;

@SuppressWarnings("unused")
public interface IGLBufferedImage {
	int getIndex();

	int getWidth();

	int getHeight();

	void baleet();

	void write();

	void blank();

	void setRGB(int r, int g, int b);

	void moveX(int offsetX);

	void moveY(int offsetY);
}
