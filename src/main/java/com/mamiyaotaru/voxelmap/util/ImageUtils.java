package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unused")
public class ImageUtils {
	public static void saveImage(String name, int glid, int maxMipmapLevel, int width, int height) {
		Logger logger = LogManager.getLogger();
		GLShim.glBindTexture(3553, glid);
		GL11.glPixelStorei(3333, 1);
		GL11.glPixelStorei(3317, 1);

		for (int mipmapLevel = 0; mipmapLevel <= maxMipmapLevel; mipmapLevel++) {
			File file = new File(name + "_" + mipmapLevel + ".png");
			int destWidth = width >> mipmapLevel;
			int destHeight = height >> mipmapLevel;
			int numPixels = destWidth * destHeight;
			IntBuffer pixelBuffer = BufferUtils.createIntBuffer(numPixels);
			int[] pixelArray = new int[numPixels];
			GL11.glGetTexImage(3553, mipmapLevel, 32993, 33639, pixelBuffer);
			pixelBuffer.get(pixelArray);
			BufferedImage bufferedImage = new BufferedImage(destWidth, destHeight, 2);
			bufferedImage.setRGB(0, 0, destWidth, destHeight, pixelArray, 0, destWidth);

			try {
				ImageIO.write(bufferedImage, "png", file);
				logger.debug("Exported png to: {}", new Object[]{file.getAbsolutePath()});
			} catch (IOException var14) {
				logger.debug("Unable to write: ", var14);
			}
		}
	}

	public static BufferedImage getFaceForGameProfile(GameProfile gameProfile) {
		BufferedImage image = blankImage("textures/entity/bat.png", 2, 2);
		ResourceLocation resourcelocation = null;
		Minecraft minecraft = Minecraft.getMinecraft();
		Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(gameProfile);
		if (map.containsKey(Type.SKIN)) {
			resourcelocation = minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
		}

		if (resourcelocation != null) {
			ITextureObject textureObject = GLUtils.textureManager.getTexture(resourcelocation);
			if (textureObject != null) {
				image = createBufferedImageFromGLID(textureObject.getGlTextureId());
			}
		}

		return image;
	}

	public static BufferedImage createBufferedImageFromResourceLocation(ResourceLocation resourceLocation) {
		try {
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
			BufferedImage image = ImageIO.read(is);
			is.close();
			if (image.getType() != 6) {
				BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), 6);
				Graphics2D g2 = temp.createGraphics();
				g2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				g2.dispose();
				image = temp;
			}

			return image;
		} catch (Exception e) {
			return null;
		}
	}

	public static BufferedImage createBufferedImageFromGLID(int id) {
		GLShim.glBindTexture(3553, id);
		return createBufferedImageFromCurrentGLImage();
	}

	public static BufferedImage createBufferedImageFromCurrentGLImage() {
		int imageWidth = GLShim.glGetTexLevelParameteri(3553, 0, 4096);
		int imageHeight = GLShim.glGetTexLevelParameteri(3553, 0, 4097);
		long size = (long) imageWidth * imageHeight * 4L;
		BufferedImage image;
		if (size < 2147483647L) {
			image = new BufferedImage(imageWidth, imageHeight, 6);
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * 4).order(ByteOrder.nativeOrder());
			GLShim.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
			((Buffer) byteBuffer).position(0);
			byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);

			for (int x = 0; x < imageWidth; x++) {
				for (int y = 0; y < imageHeight; y++) {
					int index = y * imageWidth * 4 + x * 4;
					byte var8 = 0;
					int color24 = var8 | (bytes[index + 2] & 255);
					color24 |= (bytes[index + 1] & 255) << 8;
					color24 |= (bytes[index] & 255) << 16;
					color24 |= (bytes[index + 3] & 255) << 24;
					image.setRGB(x, y, color24);
				}
			}
		} else if (!GLUtils.fboEnabled) {
			VoxelMapMod.LOGGER.error("Resource Pack too big for minimap");
			image = new BufferedImage(8, 8, 6);
		} else {
			while (size > 2147483647L) {
				imageWidth /= 2;
				imageHeight /= 2;
				size = (long) imageWidth * imageHeight * 4L;
			}

			int glid = GLShim.glGetInteger(32873);
			image = new BufferedImage(imageWidth, imageHeight, 6);
			int fboWidth = GLUtils.fboSize;
			int fboHeight = GLUtils.fboSize;
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fboWidth * fboHeight * 4).order(ByteOrder.nativeOrder());
			byte[] bytes = new byte[byteBuffer.remaining()];
			GLShim.glPushAttrib(4096);
			GLShim.glViewport(0, 0, fboWidth, fboHeight);
			GLShim.glMatrixMode(5889);
			GLShim.glPushMatrix();
			GLShim.glLoadIdentity();
			GLShim.glOrtho(0.0, fboWidth, fboHeight, 0.0, 1000.0, 3000.0);
			GLShim.glMatrixMode(5888);
			GLShim.glPushMatrix();
			GLShim.glLoadIdentity();
			GLShim.glTranslatef(0.0F, 0.0F, -2000.0F);
			GLUtils.bindFrameBuffer();

			for (int startX = 0; startX + fboWidth < imageWidth; startX += fboWidth) {
				for (int startY = 0; startY + fboWidth < imageHeight; startY += fboHeight) {
					GLUtils.disp(glid);
					GLShim.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
					GLShim.glClear(16640);
					GLUtils.drawPre();
					GLUtils.ldrawthree(0.0, fboHeight, 1.0, (float) startX / imageWidth, (float) startY / imageHeight);
					GLUtils.ldrawthree(fboWidth, fboHeight, 1.0, ((float) startX + fboWidth) / imageWidth, (float) startY / imageHeight);
					GLUtils.ldrawthree(fboWidth, 0.0, 1.0, ((float) startX + fboWidth) / imageWidth, ((float) startY + fboHeight) / imageHeight);
					GLUtils.ldrawthree(0.0, 0.0, 1.0, (float) startX / imageWidth, ((float) startY + fboHeight) / imageHeight);
					GLUtils.drawPost();
					GLUtils.disp(GLUtils.fboTextureID);
					((Buffer) byteBuffer).position(0);
					GLShim.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
					((Buffer) byteBuffer).position(0);
					byteBuffer.get(bytes);

					for (int x = 0; x < fboWidth && startX + x < imageWidth; x++) {
						for (int y = 0; y < fboHeight && startY + y < imageHeight; y++) {
							int index = y * fboWidth * 4 + x * 4;
							byte var8 = 0;
							int color24 = var8 | (bytes[index + 2] & 255);
							color24 |= (bytes[index + 1] & 255) << 8;
							color24 |= (bytes[index] & 255) << 16;
							color24 |= (bytes[index + 3] & 255) << 24;
							image.setRGB(startX + x, startY + y, color24);
						}
					}
				}
			}

			GLUtils.unbindFrameBuffer();
			GLShim.glMatrixMode(5889);
			GLShim.glPopMatrix();
			GLShim.glMatrixMode(5888);
			GLShim.glPopMatrix();
			GLShim.glPopAttrib();
			GLShim.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		}

		return image;
	}

	public static BufferedImage blankImage(String path, int w, int h) {
		return blankImage(path, w, h, 64, 32);
	}

	public static BufferedImage blankImage(String path, int w, int h, int imageWidth, int imageHeight) {
		return blankImage(path, w, h, imageWidth, imageHeight, 0, 0, 0, 0);
	}

	public static BufferedImage blankImage(String path, int w, int h, int r, int g, int b, int a) {
		return blankImage(path, w, h, 64, 32, r, g, b, a);
	}

	public static BufferedImage blankImage(String path, int w, int h, int imageWidth, int imageHeight, int r, int g, int b, int a) {
		try {
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(path)).getInputStream();
			BufferedImage mobSkin = ImageIO.read(is);
			is.close();
			BufferedImage temp = new BufferedImage(w * mobSkin.getWidth() / imageWidth, h * mobSkin.getWidth() / imageWidth, 6);
			Graphics2D g2 = temp.createGraphics();
			g2.setColor(new Color(r, g, b, a));
			g2.fillRect(0, 0, temp.getWidth(), temp.getHeight());
			g2.dispose();
			return temp;
		} catch (Exception e) {
			VoxelMapMod.LOGGER.error("Failed getting mob: {} - {}", path, e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedImage blankImage(BufferedImage mobSkin, int w, int h) {
		return blankImage(mobSkin, w, h, 64, 32);
	}

	public static BufferedImage blankImage(BufferedImage mobSkin, int w, int h, int imageWidth, int imageHeight) {
		return blankImage(mobSkin, w, h, imageWidth, imageHeight, 0, 0, 0, 0);
	}

	public static BufferedImage blankImage(BufferedImage mobSkin, int w, int h, int r, int g, int b, int a) {
		return blankImage(mobSkin, w, h, 64, 32, r, g, b, a);
	}

	public static BufferedImage blankImage(BufferedImage mobSkin, int w, int h, int imageWidth, int imageHeight, int r, int g, int b, int a) {
		BufferedImage temp = new BufferedImage(w * mobSkin.getWidth() / imageWidth, h * mobSkin.getWidth() / imageWidth, 6);
		Graphics2D g2 = temp.createGraphics();
		g2.setColor(new Color(r, g, b, a));
		g2.fillRect(0, 0, temp.getWidth(), temp.getHeight());
		g2.dispose();
		return temp;
	}

	public static BufferedImage addCharacter(BufferedImage image, String character) {
		Graphics2D g2 = image.createGraphics();
		g2.setColor(new Color(0, 0, 0, 255));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(new Font("Arial", 0, image.getHeight()));
		FontMetrics fm = g2.getFontMetrics();
		int x = (image.getWidth() - fm.stringWidth("?")) / 2;
		int y = fm.getAscent() + (image.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
		g2.drawString("?", x, y);
		g2.dispose();
		return image;
	}

	public static BufferedImage eraseArea(BufferedImage image, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		float scaleX = (float) image.getWidth() / imageWidth;
		float scaleY = (float) image.getHeight() / imageHeight;
		x = (int) (x * scaleX);
		y = (int) (y * scaleY);
		w = (int) (w * scaleX);
		h = (int) (h * scaleY);
		int[] blankPixels = new int[w * h];
		Arrays.fill(blankPixels, 0);
		image.setRGB(x, y, w, h, blankPixels, 0, w);
		return image;
	}

	public static BufferedImage loadImage(ResourceLocation resourceLocation, int x, int y, int w, int h) {
		return loadImage(resourceLocation, x, y, w, h, 64, 32);
	}

	public static BufferedImage loadImage(ResourceLocation resourceLocation, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		try {
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
			BufferedImage mobSkin = ImageIO.read(is);
			is.close();
			return loadImage(mobSkin, x, y, w, h, imageWidth, imageHeight);
		} catch (Exception e) {
			VoxelMapMod.LOGGER.error("Failed getting mob: {} - {}", resourceLocation.toString(), e.getLocalizedMessage());
			return null;
		}
	}

	public static BufferedImage loadImage(BufferedImage mobSkin, int x, int y, int w, int h) {
		return loadImage(mobSkin, x, y, w, h, 64, 32);
	}

	public static BufferedImage loadImage(BufferedImage mobSkin, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		if (mobSkin.getType() != 6) {
			BufferedImage temp = new BufferedImage(mobSkin.getWidth(), mobSkin.getHeight(), 6);
			Graphics2D g2 = temp.createGraphics();
			g2.drawImage(mobSkin, 0, 0, mobSkin.getWidth(), mobSkin.getHeight(), null);
			g2.dispose();
			mobSkin = temp;
		}

		float scale = (float) mobSkin.getWidth(null) / imageWidth;
		x = (int) (x * scale);
		y = (int) (y * scale);
		w = (int) (w * scale);
		h = (int) (h * scale);
		w = Math.max(1, w);
		h = Math.max(1, h);
		x = Math.min(mobSkin.getWidth(null) - w, x);
		y = Math.min(mobSkin.getHeight(null) - h, y);
		return mobSkin.getSubimage(x, y, w, h);
	}

	public static BufferedImage addImages(BufferedImage base, BufferedImage overlay, float x, float y, int baseWidth, int baseHeight) {
		int scale = base.getWidth() / baseWidth;
		Graphics gfx = base.getGraphics();
		gfx.drawImage(overlay, (int) (x * scale), (int) (y * scale), null);
		gfx.dispose();
		return base;
	}

	public static BufferedImage scaleImage(BufferedImage image, float scaleBy) {
		if (scaleBy == 1.0F) {
			return image;
		}

		int type = image.getType();
		if (type == 13) {
			type = 6;
		}

		int newWidth = Math.max(1, (int) (image.getWidth() * scaleBy));
		int newHeight = Math.max(1, (int) (image.getHeight() * scaleBy));
		BufferedImage tmp = new BufferedImage(newWidth, newHeight, type);
		Graphics2D g2 = tmp.createGraphics();
		g2.drawImage(image, 0, 0, newWidth, newHeight, null);
		g2.dispose();
		return tmp;
	}

	public static BufferedImage flipHorizontal(BufferedImage image) {
		AffineTransform tx = AffineTransform.getScaleInstance(-1.0, 1.0);
		tx.translate(-image.getWidth(null), 0.0);
		AffineTransformOp op = new AffineTransformOp(tx, 1);
		return op.filter(image, null);
	}

	public static BufferedImage into128(BufferedImage base) {
		BufferedImage frame = new BufferedImage(128, 128, base.getType());
		Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, 64 - base.getWidth() / 2, 64 - base.getHeight() / 2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}

	public static BufferedImage intoSquare(BufferedImage base) {
		int dim = Math.max(base.getWidth(), base.getHeight());
		int t = 1;

		while (Math.pow(2.0, t - 1) < dim) {
			t++;
		}

		int size = (int) Math.pow(2.0, t);
		BufferedImage frame = new BufferedImage(size, size, base.getType());
		Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, (size - base.getWidth()) / 2, (size - base.getHeight()) / 2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}

	public static BufferedImage pad(BufferedImage base) {
		int dim = Math.max(base.getWidth(), base.getHeight());
		int size = dim + 4;
		BufferedImage frame = new BufferedImage(size, size, base.getType());
		Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, (size - base.getWidth()) / 2, (size - base.getHeight()) / 2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}

	public static BufferedImage fillOutline(BufferedImage image, boolean outline) {
		return fillOutline(image, outline, false, 0, 0);
	}

	public static BufferedImage fillOutline(BufferedImage image, boolean outline, boolean armor, int intendedWidth, int entry) {
		if (outline && entry != 2 && entry != 3) {
			image = fillOutline(image, true, armor, intendedWidth);
		}

		return fillOutline(image, false, armor, intendedWidth);
	}

	private static BufferedImage fillOutline(BufferedImage image, boolean solid, boolean armor, int intendedWidth) {
		float armorOutlineFraction = intendedWidth / 2.0F;
		BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics gfx = temp.getGraphics();
		gfx.drawImage(image, 0, 0, null);
		gfx.dispose();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		for (int t = 0; t < image.getWidth(); t++) {
			for (int s = 0; s < image.getHeight(); s++) {
				int color = image.getRGB(s, t);
				if ((color >> 24 & 0xFF) == 0) {
					int newColor = getNonTransparentPixel(s, t, image);
					if (newColor != -420) {
						if (solid) {
							if (armor
								&& !(t <= (float) imageWidth / 2 - armorOutlineFraction)
								&& !(t >= (float) imageWidth / 2 + armorOutlineFraction - 1.0F)
								&& !(s <= (float) imageHeight / 2 - armorOutlineFraction)
								&& !(s >= (float) imageHeight / 2 + armorOutlineFraction - 1.0F)) {
								newColor = 0;
							} else {
								newColor = -16777216;
							}
						} else {
							int red = newColor >> 16 & 0xFF;
							int green = newColor >> 8 & 0xFF;
							int blue = newColor & 0xFF;
							newColor = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
						}

						temp.setRGB(s, t, newColor);
					}
				}
			}
		}

		return temp;
	}

	public static int getNonTransparentPixel(int x, int y, BufferedImage image) {
		if (x > 0) {
			int color = image.getRGB(x - 1, y);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (x < image.getWidth() - 1) {
			int color = image.getRGB(x + 1, y);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (y > 0) {
			int color = image.getRGB(x, y - 1);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (y < image.getHeight() - 1) {
			int color = image.getRGB(x, y + 1);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (x > 0 && y > 0) {
			int color = image.getRGB(x - 1, y - 1);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (x > 0 && y < image.getHeight() - 1) {
			int color = image.getRGB(x - 1, y + 1);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (x < image.getWidth() - 1 && y > 0) {
			int color = image.getRGB(x + 1, y - 1);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		if (x < image.getWidth() - 1 && y < image.getHeight() - 1) {
			int color = image.getRGB(x + 1, y + 1);
			if ((color >> 24 & 0xFF) > 50) {
				return color;
			}
		}

		return -420;
	}

	public static BufferedImage trim(BufferedImage image) {
		int left = -1;
		int right = image.getWidth();
		int top = -1;
		int bottom = image.getHeight();
		boolean foundColor = false;
		int color;

		while (!foundColor && left < right - 1) {
			left++;

			for (int t = 0; t < image.getHeight(); t++) {
				color = image.getRGB(left, t);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}
		}

		foundColor = false;

		while (!foundColor && right > left + 1) {
			right--;

			for (int tx = 0; tx < image.getHeight(); tx++) {
				color = image.getRGB(right, tx);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}
		}

		foundColor = false;

		while (!foundColor && top < bottom - 1) {
			top++;

			for (int txx = 0; txx < image.getWidth(); txx++) {
				color = image.getRGB(txx, top);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}
		}

		foundColor = false;

		while (!foundColor && bottom > top + 1) {
			bottom--;

			for (int txxx = 0; txxx < image.getWidth(); txxx++) {
				color = image.getRGB(txxx, bottom);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}
		}

		return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
	}

	public static BufferedImage trimCentered(BufferedImage image) {
		int height = image.getHeight();
		int width = image.getWidth();
		int left = -1;
		int right = width;
		int top = -1;
		int bottom = height;
		boolean foundColor = false;
		int color;

		while (!foundColor && left < width / 2 - 1 && top < height / 2 - 1) {
			left++;
			right--;
			top++;
			bottom--;

			for (int y = top; y < bottom; y++) {
				color = image.getRGB(left, y);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}

			for (int yx = top; yx < bottom; yx++) {
				color = image.getRGB(right, yx);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}

			for (int x = left; x < right; x++) {
				color = image.getRGB(x, top);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}

			for (int xx = left; xx < right; xx++) {
				color = image.getRGB(xx, bottom);
				if (color >> 24 != 0) {
					foundColor = true;
				}
			}
		}

		return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
	}

	public static float percentageOfEdgePixelsThatAreSolid(BufferedImage image) {
		float edgePixels = image.getWidth() * 2 + image.getHeight() * 2 - 2;
		float edgePixelsWithColor = 0.0F;
		int color;

		for (int t = 0; t < image.getHeight(); t++) {
			color = image.getRGB(0, t);
			if (color >> 24 != 0) {
				edgePixelsWithColor++;
			}

			color = image.getRGB(image.getWidth() - 1, t);
			if (color >> 24 != 0) {
				edgePixelsWithColor++;
			}
		}

		for (int t = 1; t < image.getWidth() - 1; t++) {
			color = image.getRGB(t, 0);
			if (color >> 24 != 0) {
				edgePixelsWithColor++;
			}

			color = image.getRGB(t, image.getHeight() - 1);
			if (color >> 24 != 0) {
				edgePixelsWithColor++;
			}
		}

		return edgePixelsWithColor / edgePixels;
	}
}
