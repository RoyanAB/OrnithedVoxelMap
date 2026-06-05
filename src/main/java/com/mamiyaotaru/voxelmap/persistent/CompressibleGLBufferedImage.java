package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IGLBufferedImage;
import com.mamiyaotaru.voxelmap.util.CompressionUtils;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.GLUtils;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

@SuppressWarnings("unused")
public class CompressibleGLBufferedImage implements IGLBufferedImage {
	private static final HashMap<Integer, ByteBuffer> byteBuffers = new HashMap<>(4);
	private static final ByteBuffer defaultSizeBuffer = ByteBuffer.allocateDirect(262144).order(ByteOrder.nativeOrder());

	private final int width;
	private final int height;
	private final Object bufferLock = new Object();
	private final boolean compressNotDelete;
	private byte[] bytes;
	private int index = 0;
	private boolean isCompressed = false;

	public CompressibleGLBufferedImage(int width, int height, int imageType) {
		this.width = width;
		this.height = height;
		this.bytes = new byte[width * height * 4];
		this.compressNotDelete = VoxelMap.getInstance().getPersistentMapOptions().outputImages;
	}

	public byte[] getData() {
		if (this.isCompressed) {
			this.decompress();
		}

		return this.bytes;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
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
		if (this.isCompressed) {
			this.decompress();
		}

		if (this.index == 0) {
			this.index = GL11.glGenTextures();
		}

		ByteBuffer buffer = byteBuffers.get(this.width * this.height);
		if (buffer == null) {
			buffer = ByteBuffer.allocateDirect(this.width * this.height * 4).order(ByteOrder.nativeOrder());
			byteBuffers.put(this.width * this.height, buffer);
		}

		((Buffer) buffer).clear();
		synchronized (this.bufferLock) {
			buffer.put(this.bytes);
		}

		((Buffer) buffer).position(0).limit(this.bytes.length);
		GL11.glBindTexture(GLShim.GL11_GL_TEXTURE_2D, this.index);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_NEAREST);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_NEAREST);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_WRAP_S, GLShim.GL12_GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_WRAP_T, GLShim.GL12_GL_CLAMP_TO_EDGE);
		if (GLUtils.openGL14Enabled) {
			GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 33169, 1);
		}

		GL11.glTexImage2D(GLShim.GL11_GL_TEXTURE_2D, 0, GLShim.GL11_GL_RGBA, this.getWidth(), this.getHeight(), 0, GLShim.GL11_GL_RGBA, 32821, buffer);
		this.compress();
	}

	@Override
	public void blank() {
		if (this.isCompressed) {
			this.decompress();
		}

		Arrays.fill(this.bytes, (byte) 0);
		this.write();
	}

	@Override
	public void setRGB(int x, int y, int color24) {
		if (this.isCompressed) {
			this.decompress();
		}

		int index = (x + y * this.getWidth()) * 4;
		synchronized (this.bufferLock) {
			int alpha = color24 >> 24 & 0xFF;
			this.bytes[index] = -1;
			this.bytes[index + 1] = (byte) ((color24 & 0xFF) * alpha / 255);
			this.bytes[index + 2] = (byte) ((color24 >> 8 & 0xFF) * alpha / 255);
			this.bytes[index + 3] = (byte) ((color24 >> 16 & 0xFF) * alpha / 255);
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

	private synchronized void compress() {
		if (!this.isCompressed) {
			if (this.compressNotDelete) {
				try {
					this.bytes = CompressionUtils.compress(this.bytes);
				} catch (IOException ignored) {
				}
			} else {
				this.bytes = null;
			}

			this.isCompressed = true;
		}
	}

	private synchronized void decompress() {
		if (this.isCompressed) {
			if (this.compressNotDelete) {
				try {
					this.bytes = CompressionUtils.decompress(this.bytes);
				} catch (IOException | DataFormatException ignored) {
				}
			} else {
				this.bytes = new byte[this.width * this.height * 4];
				this.isCompressed = false;
			}
		}
	}

	static {
		byteBuffers.put(65536, defaultSizeBuffer);
	}
}
