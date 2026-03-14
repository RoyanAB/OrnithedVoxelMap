package com.mamiyaotaru.voxelmap.textures;

import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class FontRendererWithAtlas extends FontRenderer implements IResourceManagerReloadListener {
	private final ResourceLocation locationFontTexture;
	private final TextureManager renderEngine;
	private final int[] charWidthArray = new int[256];
	private final int[] colorCode = new int[32];
	public int FONT_HEIGHT = 9;
	public Random fontRandom = new Random();
	private Sprite fontIcon = null;
	private int ref = 0;
	private float posX;
	private float posY;
	private float red;
	private float blue;
	private float green;
	private float alpha;
	private int textColor;
	private boolean randomStyle;
	private boolean boldStyle;
	private boolean italicStyle;
	private boolean underlineStyle;
	private boolean strikethroughStyle;

	public FontRendererWithAtlas(GameSettings gameSettings, ResourceLocation locationFontTexture, TextureManager renderEngine, boolean unicodeFlag) {
		super(gameSettings, locationFontTexture, renderEngine, unicodeFlag);
		this.locationFontTexture = locationFontTexture;
		this.renderEngine = renderEngine;
		renderEngine.bindTexture(this.locationFontTexture);

		for (int colorCodeIndex = 0; colorCodeIndex < 32; colorCodeIndex++) {
			int var6 = (colorCodeIndex >> 3 & 1) * 85;
			int red = (colorCodeIndex >> 2 & 1) * 170 + var6;
			int green = (colorCodeIndex >> 1 & 1) * 170 + var6;
			int blue = (colorCodeIndex >> 0 & 1) * 170 + var6;
			if (colorCodeIndex == 6) {
				red += 85;
			}

			if (gameSettings.anaglyph) {
				int var10 = (red * 30 + green * 59 + blue * 11) / 100;
				int var11 = (red * 30 + green * 70) / 100;
				int var12 = (red * 30 + blue * 70) / 100;
				red = var10;
				green = var11;
				blue = var12;
			}

			if (colorCodeIndex >= 16) {
				red /= 4;
				green /= 4;
				blue /= 4;
			}

			this.colorCode[colorCodeIndex] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
		}
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.readFontTexture();
	}

	private void readFontTexture() {
		BufferedImage fontImage;
		try {
			fontImage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(this.locationFontTexture).getInputStream());
		} catch (IOException var17) {
			throw new RuntimeException(var17);
		}

		if (fontImage.getWidth() > 1024 || fontImage.getHeight() > 1024) {
			int maxDim = Math.max(fontImage.getWidth(), fontImage.getHeight());
			float scaleBy = 1024.0F / maxDim;
			int type = fontImage.getType();
			if (type == 13) {
				type = 6;
			}

			int newWidth = Math.max(1, (int) (fontImage.getWidth() * scaleBy));
			int newHeight = Math.max(1, (int) (fontImage.getHeight() * scaleBy));
			BufferedImage tmp = new BufferedImage(newWidth, newHeight, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.drawImage(fontImage, 0, 0, newWidth, newHeight, null);
			g2.dispose();
			fontImage = tmp;
		}

		int sheetWidth = fontImage.getWidth();
		int sheetHeight = fontImage.getHeight();
		int[] sheetImageData = new int[sheetWidth * sheetHeight];
		fontImage.getRGB(0, 0, sheetWidth, sheetHeight, sheetImageData, 0, sheetWidth);
		int characterHeight = sheetHeight / 16;
		int characterWidth = sheetWidth / 16;
		byte padding = 1;
		float scale = 8.0F / characterWidth;

		for (int characterIndex = 0; characterIndex < 256; characterIndex++) {
			int characterX = characterIndex % 16;
			int characterY = characterIndex / 16;
			if (characterIndex == 32) {
				this.charWidthArray[characterIndex] = 3 + padding;
			}

			int thisCharacterWidth = characterWidth - 1;
			boolean onlyBlankPixels = true;

			while (thisCharacterWidth >= 0 && onlyBlankPixels) {
				int pixelX = characterX * characterWidth + thisCharacterWidth;

				for (int characterPixelYPos = 0; characterPixelYPos < characterHeight && onlyBlankPixels; characterPixelYPos++) {
					int pixelY = (characterY * characterWidth + characterPixelYPos) * sheetWidth;
					if ((sheetImageData[pixelX + pixelY] >> 24 & 0xFF) != 0) {
						onlyBlankPixels = false;
						break;
					}
				}

				if (onlyBlankPixels) {
					thisCharacterWidth--;
				}
			}

			this.charWidthArray[characterIndex] = (int) (0.5 + ++thisCharacterWidth * scale) + padding;
		}
	}

	public void setFontSprite(Sprite icon) {
		this.fontIcon = icon;
	}

	public void setFontRef(int ref) {
		this.ref = ref;
	}

	private float renderCharAtPos(int charIndex, char character, boolean shadow) {
		return character == ' ' ? 4.0F : this.renderDefaultChar(charIndex, shadow);
	}

	protected float renderDefaultChar(int charIndex, boolean shadow) {
		float sheetWidth = (this.fontIcon.originX + this.fontIcon.width) / this.fontIcon.getMaxU();
		float sheetHeight = (this.fontIcon.originY + this.fontIcon.height) / this.fontIcon.getMaxV();
		float fontScaleX = (this.fontIcon.width - 2) / 128.0F;
		float fontScaleY = (this.fontIcon.height - 2) / 128.0F;
		float charXPosInSheet = charIndex % 16 * 8 * fontScaleX + this.fontIcon.originX + 1.0F;
		float charYPosInSheet = charIndex / 16 * 8 * fontScaleY + this.fontIcon.originY + 1.0F;
		float shadowOffset = shadow ? 1.0F : 0.0F;
		float charWidth = this.charWidthArray[charIndex] - 0.01F;
		GL11.glBegin(5);
		GL11.glTexCoord2f(charXPosInSheet / sheetWidth, charYPosInSheet / sheetHeight);
		GL11.glVertex3f(this.posX + shadowOffset, this.posY, 0.0F);
		GL11.glTexCoord2f(charXPosInSheet / sheetWidth, (charYPosInSheet + 7.99F * fontScaleY) / sheetHeight);
		GL11.glVertex3f(this.posX - shadowOffset, this.posY + 7.99F, 0.0F);
		GL11.glTexCoord2f((charXPosInSheet + (charWidth - 1.0F) * fontScaleX) / sheetWidth, charYPosInSheet / sheetHeight);
		GL11.glVertex3f(this.posX + charWidth - 1.0F + shadowOffset, this.posY, 0.0F);
		GL11.glTexCoord2f((charXPosInSheet + (charWidth - 1.0F) * fontScaleX) / sheetWidth, (charYPosInSheet + 7.99F * fontScaleY) / sheetHeight);
		GL11.glVertex3f(this.posX + charWidth - 1.0F - shadowOffset, this.posY + 7.99F, 0.0F);
		GL11.glEnd();
		return this.charWidthArray[charIndex];
	}

	public int drawStringWithShadow(String text, float x, float y, int color) {
		return this.drawString(text, x, y, color, true);
	}

	public int drawString(String text, int x, int y, int color) {
		return this.drawString(text, x, y, color, false);
	}

	public int drawString(String text, float x, float y, int color, boolean shadow) {
		GLShim.glEnable(3008);
		this.resetStyles();
		int var6;
		if (shadow) {
			var6 = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
			var6 = Math.max(var6, this.renderString(text, x, y, color, false));
		} else {
			var6 = this.renderString(text, x, y, color, false);
		}

		return var6;
	}

	private void resetStyles() {
		this.randomStyle = false;
		this.boldStyle = false;
		this.italicStyle = false;
		this.underlineStyle = false;
		this.strikethroughStyle = false;
	}

	private void renderStringAtPos(String text, boolean shadow) {
		for (int textIndex = 0; textIndex < text.length(); textIndex++) {
			char character = text.charAt(textIndex);
			if (character == 167 && textIndex + 1 < text.length()) {
				int formatCode = "0123456789abcdefklmnor".indexOf(text.toLowerCase().charAt(textIndex + 1));
				if (formatCode < 16) {
					this.randomStyle = false;
					this.boldStyle = false;
					this.strikethroughStyle = false;
					this.underlineStyle = false;
					this.italicStyle = false;
					if (formatCode < 0 || formatCode > 15) {
						formatCode = 15;
					}

					if (shadow) {
						formatCode += 16;
					}

					int color = this.colorCode[formatCode];
					this.textColor = color;
					GLShim.glColor4f((color >> 16) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, this.alpha);
				} else if (formatCode == 16) {
					this.randomStyle = true;
				} else if (formatCode == 17) {
					this.boldStyle = true;
				} else if (formatCode == 18) {
					this.strikethroughStyle = true;
				} else if (formatCode == 19) {
					this.underlineStyle = true;
				} else if (formatCode == 20) {
					this.italicStyle = true;
				} else if (formatCode == 21) {
					this.randomStyle = false;
					this.boldStyle = false;
					this.strikethroughStyle = false;
					this.underlineStyle = false;
					this.italicStyle = false;
					GLShim.glColor4f(this.red, this.blue, this.green, this.alpha);
				}

				textIndex++;
			} else {
				int charIndex = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000"
					.indexOf(character);
				if (charIndex != -1) {
					if (this.randomStyle) {
						int randomCharIndex;
						do {
							randomCharIndex = this.fontRandom.nextInt(this.charWidthArray.length);
						} while (this.charWidthArray[charIndex] != this.charWidthArray[randomCharIndex]);

						charIndex = randomCharIndex;
					}

					float offset = 1.0F;
					float widthOfRenderedChar = this.renderCharAtPos(charIndex, character, this.italicStyle);
					if (this.boldStyle) {
						this.posX += offset;
						this.renderCharAtPos(charIndex, character, this.italicStyle);
						this.posX -= offset;
						widthOfRenderedChar++;
					}

					if (this.strikethroughStyle) {
						Tessellator tessellator = Tessellator.getInstance();
						VertexBuffer vertexBuffer = tessellator.getBuffer();
						GLShim.glDisable(3553);
						vertexBuffer.begin(7, DefaultVertexFormats.POSITION);
						vertexBuffer.pos(this.posX, this.posY + this.FONT_HEIGHT / 2, 0.0).endVertex();
						vertexBuffer.pos(this.posX + widthOfRenderedChar, this.posY + this.FONT_HEIGHT / 2, 0.0).endVertex();
						vertexBuffer.pos(this.posX + widthOfRenderedChar, this.posY + this.FONT_HEIGHT / 2 - 1.0F, 0.0).endVertex();
						vertexBuffer.pos(this.posX, this.posY + this.FONT_HEIGHT / 2 - 1.0F, 0.0).endVertex();
						tessellator.draw();
						GLShim.glEnable(3553);
					}

					if (this.underlineStyle) {
						Tessellator tessellator = Tessellator.getInstance();
						VertexBuffer vertexBuffer = tessellator.getBuffer();
						GLShim.glDisable(3553);
						vertexBuffer.begin(7, DefaultVertexFormats.POSITION);
						int l = this.underlineStyle ? -1 : 0;
						vertexBuffer.pos(this.posX + l, this.posY + this.FONT_HEIGHT, 0.0).endVertex();
						vertexBuffer.pos(this.posX + widthOfRenderedChar, this.posY + this.FONT_HEIGHT, 0.0).endVertex();
						vertexBuffer.pos(this.posX + widthOfRenderedChar, this.posY + this.FONT_HEIGHT - 1.0F, 0.0).endVertex();
						vertexBuffer.pos(this.posX + l, this.posY + this.FONT_HEIGHT - 1.0F, 0.0).endVertex();
						tessellator.draw();
						GLShim.glEnable(3553);
					}

					this.posX += (int) widthOfRenderedChar;
				}
			}
		}
	}

	private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow) {
		return this.renderString(text, x, y, color, dropShadow);
	}

	private int renderString(String text, float x, float y, int color, boolean shadow) {
		if (text == null) {
			return 0;
		}

		if ((color & -67108864) == 0) {
			color |= -16777216;
		}

		if (shadow) {
			color = (color & 16579836) >> 2 | color & 0xFF000000;
		}

		this.red = (color >> 16 & 0xFF) / 255.0F;
		this.blue = (color >> 8 & 0xFF) / 255.0F;
		this.green = (color & 0xFF) / 255.0F;
		this.alpha = (color >> 24 & 0xFF) / 255.0F;
		GLShim.glColor4f(this.red, this.blue, this.green, this.alpha);
		this.posX = x;
		this.posY = y;
		this.renderStringAtPos(text, shadow);
		return (int) this.posX;
	}

	public int getStringWidth(String string) {
		if (string == null) {
			return 0;
		}

		int totalWidth = 0;
		boolean includeSpace = false;

		for (int charIndex = 0; charIndex < string.length(); charIndex++) {
			char character = string.charAt(charIndex);
			int characterWidth = this.getCharWidth(character);
			if (characterWidth < 0 && charIndex < string.length() - 1) {
				character = string.charAt(++charIndex);
				if (character == 'l' || character == 'L') {
					includeSpace = true;
				} else if (character == 'r' || character == 'R') {
					includeSpace = false;
				}

				characterWidth = 0;
			}

			totalWidth += characterWidth;
			if (includeSpace && characterWidth > 0) {
				totalWidth++;
			}
		}

		return totalWidth;
	}

	public int getCharWidth(char character) {
		if (character == 167) {
			return -1;
		}

		if (character == ' ') {
			return 4;
		}

		int indexInDefaultSheet = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000"
			.indexOf(character);
		return character > 0 && indexInDefaultSheet != -1 ? this.charWidthArray[indexInDefaultSheet] : 0;
	}
}
