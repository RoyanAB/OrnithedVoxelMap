package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IGLBufferedImage;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@SuppressWarnings("unused")
public class GLBufferedImage extends BufferedImage implements IGLBufferedImage {
	protected final Object bufferLock = new Object();
	protected ByteBuffer buffer;
	protected byte[] bytes;
	protected int index;

	public GLBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
		this.bytes = ((DataBufferByte) this.getRaster().getDataBuffer()).getData();
		this.buffer = ByteBuffer.allocateDirect(this.bytes.length).order(ByteOrder.nativeOrder());
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public void baleet() {
		int currentIndex = this.index;
		this.index = 0;
		if (currentIndex != 0) {
			GL11.glDeleteTextures(currentIndex);
		}
	}

	@Override
	public void write() {
		if (this.index == 0) {
			this.index = GL11.glGenTextures();
		}

		((Buffer) this.buffer).clear();
		synchronized (this.bufferLock) {
			this.buffer.put(this.bytes);
		}

		((Buffer) this.buffer).position(0).limit(this.bytes.length);
		GL11.glBindTexture(GLShim.GL11_GL_TEXTURE_2D, this.index);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10241, 9728);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10240, 9728);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10242, 33071);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10243, 33071);
		GL11.glTexImage2D(GLShim.GL11_GL_TEXTURE_2D, 0, 6408, this.getWidth(), this.getHeight(), 0, 6408, 32821, this.buffer);
	}

	@Override
	public void blank() {
		Arrays.fill(this.bytes, (byte) 0);
		this.write();
	}

	@Override
	public void setRGB(int x, int y, int color24) {
		int index = (x + y * this.getWidth()) * 4;
		synchronized (this.bufferLock) {
			this.bytes[index] = (byte) (color24 >> 24);
			this.bytes[index + 1] = (byte) (color24 >> 0);
			this.bytes[index + 2] = (byte) (color24 >> 8);
			this.bytes[index + 3] = (byte) (color24 >> 16);
		}
	}

	@Override
	public void moveX(int offset) {
		synchronized (this.bufferLock) {
			if (offset > 0) {
				System.arraycopy(this.bytes, offset * 4, this.bytes, 0, this.bytes.length - offset * 4);
			} else if (offset < 0) {
				System.arraycopy(this.bytes, 0, this.bytes, -offset * 4, this.bytes.length + offset * 4);
			}
		}
	}

	@Override
	public void moveY(int offset) {
		synchronized (this.bufferLock) {
			if (offset > 0) {
				System.arraycopy(this.bytes, offset * this.getWidth() * 4, this.bytes, 0, this.bytes.length - offset * this.getWidth() * 4);
			} else if (offset < 0) {
				System.arraycopy(this.bytes, 0, this.bytes, -offset * this.getWidth() * 4, this.bytes.length + offset * this.getWidth() * 4);
			}
		}
	}
}
