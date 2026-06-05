package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.VoxelMap;

import java.nio.Buffer;

@SuppressWarnings("unused")
public class LiveGLBufferedImage extends GLBufferedImage {
	public LiveGLBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	@Override
	public void write() {
		if (this.index == 0) {
			this.index = GLShim.glGenTextures();
		}

		((Buffer) this.buffer).clear();
		synchronized (this.bufferLock) {
			this.buffer.put(this.bytes);
		}

		((Buffer) this.buffer).position(0).limit(this.bytes.length);
		if (!GLUtils.hasAlphaBits && !GLUtils.fboEnabled) {
			if (MapSettingsManager.instance.squareMap) {
				for (int t = 0; t < this.getWidth(); t++) {
					this.buffer.put(t * 4, (byte) 0);
					this.buffer.put(t * this.getWidth() * 4, (byte) 0);
				}
			}

			if (MapSettingsManager.instance.squareMap && (MapSettingsManager.instance.zoom > 0 || VoxelMap.instance.getMap().getPercentX() > 1.0F)) {
				for (int t = 0; t < this.getWidth(); t++) {
					this.buffer.put(t * this.getWidth() * 4 + 4, (byte) 0);
				}
			}

			if (MapSettingsManager.instance.squareMap && (MapSettingsManager.instance.zoom > 0 || VoxelMap.instance.getMap().getPercentY() > 1.0F)) {
				for (int t = 0; t < this.getWidth(); t++) {
					this.buffer.put(t * 4 + this.getWidth() * 4, (byte) 0);
				}
			}
		}

		GLShim.glBindTexture(GLShim.GL11_GL_TEXTURE_2D, this.index);
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_NEAREST);
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_NEAREST);
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_WRAP_S, GLShim.GL12_GL_CLAMP_TO_EDGE);
		GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_WRAP_T, GLShim.GL12_GL_CLAMP_TO_EDGE);
		if (GLUtils.openGL14Enabled) {
			GLShim.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL14_GL_GENERATE_MIPMAP, 1);
		}

		GLShim.glPixelStorei(GLShim.GL11_GL_UNPACK_ROW_LENGTH, 0);
		GLShim.glPixelStorei(GLShim.GL11_GL_UNPACK_SKIP_PIXELS, 0);
		GLShim.glPixelStorei(GLShim.GL11_GL_UNPACK_SKIP_ROWS, 0);
		GLShim.glTexImage2D(GLShim.GL11_GL_TEXTURE_2D, 0, GLShim.GL11_GL_RGBA, this.getWidth(), this.getHeight(), 0, GLShim.GL11_GL_RGBA, GLShim.GL12_GL_UNSIGNED_INT_8_8_8_8, this.buffer);
	}

	@Override
	public void setRGB(int x, int y, int color24) {
		int index = (x + y * this.getWidth()) * 4;
		synchronized (this.bufferLock) {
			int alpha = color24 >> 24 & 0xFF;
			this.bytes[index] = -1;
			this.bytes[index + 1] = (byte) ((color24 & 0xFF) * alpha / 255);
			this.bytes[index + 2] = (byte) ((color24 >> 8 & 0xFF) * alpha / 255);
			this.bytes[index + 3] = (byte) ((color24 >> 16 & 0xFF) * alpha / 255);
		}
	}
}
