package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.textures.Sprite;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class GLUtils {
	private static final Tessellator tessellator = Tessellator.getInstance();
	private static final BufferBuilder vertexBuffer = tessellator.getBuffer();
	public static TextureManager textureManager;
	public static int fboID = 0;
	public static boolean openGL14Enabled = GLContext.getCapabilities().OpenGL14;
	public static boolean fboEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object && openGL14Enabled;
	public static int fboTextureID = 0;
	public static boolean hasAlphaBits = GL11.glGetInteger(3413) > 0;
	public static int fboSize = 512;
	public static int fboRad = 256;
	private static int previousFBOID = 0;

	public static void setupFBO() {
		previousFBOID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
		fboID = EXTFramebufferObject.glGenFramebuffersEXT();
		fboTextureID = GL11.glGenTextures();
		int width = fboSize;
		int height = fboSize;
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID);
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
		GL11.glBindTexture(GLShim.GL11_GL_TEXTURE_2D, fboTextureID);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_WRAP_S, GLShim.GL11_GL_CLAMP);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_WRAP_T, GLShim.GL11_GL_CLAMP);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MIN_FILTER, GLShim.GL11_GL_LINEAR);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, GLShim.GL11_GL_TEXTURE_MAG_FILTER, GLShim.GL11_GL_LINEAR);
		GL11.glTexImage2D(GLShim.GL11_GL_TEXTURE_2D, 0, GLShim.GL11_GL_RGBA, width, height, 0, GLShim.GL11_GL_RGBA, GLShim.GL11_GL_BYTE, byteBuffer);
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GLShim.GL11_GL_TEXTURE_2D, fboTextureID, 0);
		int depthRenderBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
		EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRenderBufferID);
		EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GLShim.GL14_GL_DEPTH_COMPONENT24, fboSize, fboSize);
		EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRenderBufferID);
		EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previousFBOID);
	}

	public static void bindFrameBuffer() {
		previousFBOID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID);
	}

	public static void unbindFrameBuffer() {
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previousFBOID);
	}

	public static void setMapWithScale(int x, int y, float scale) {
		setMap(x, y, (int) (128.0F * scale));
	}

	public static void setMap(int x, float y, int imageSize) {
		float scale = imageSize / 4.0F;
		ldrawthree(x - scale, y + scale, 1.0, 0.0, 1.0);
		ldrawthree(x + scale, y + scale, 1.0, 1.0, 1.0);
		ldrawthree(x + scale, y - scale, 1.0, 1.0, 0.0);
		ldrawthree(x - scale, y - scale, 1.0, 0.0, 0.0);
	}

	public static void setMap(Sprite icon, int x, float y, float imageSize) {
		float scale = imageSize / 4.0F;
		ldrawthree(x - scale, y + scale, 1.0, icon.getMinU(), icon.getMaxV());
		ldrawthree(x + scale, y + scale, 1.0, icon.getMaxU(), icon.getMaxV());
		ldrawthree(x + scale, y - scale, 1.0, icon.getMaxU(), icon.getMinV());
		ldrawthree(x - scale, y - scale, 1.0, icon.getMinU(), icon.getMinV());
	}

	public static int tex(BufferedImage paramImg) {
		int glid = GL11.glGenTextures();
		TextureUtil.uploadTextureImage(glid, paramImg);
		return glid;
	}

	public static void img(String paramStr) {
		textureManager.bindTexture(new ResourceLocation(paramStr));
	}

	public static void img(ResourceLocation paramResourceLocation) {
		textureManager.bindTexture(paramResourceLocation);
	}

	public static void disp(int paramInt) {
		GlStateManager.bindTexture(paramInt);
	}

	public static void drawPre() {
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
	}

	public static void drawPre(VertexFormat vertexFormat) {
		vertexBuffer.begin(GL11.GL_QUADS, vertexFormat);
	}

	public static void drawPost() {
		tessellator.draw();
	}

	public static void glah(int g) {
		GL11.glDeleteTextures(g);
	}

	public static void ldrawone(int x, int y, double z, double u, double v) {
		vertexBuffer.pos(x, y, z).tex(u, v).endVertex();
	}

	public static void ldrawtwo(double x, double y, double z) {
		vertexBuffer.pos(x, y, z).endVertex();
	}

	public static void ldrawthree(double x, double y, double z, double u, double v) {
		vertexBuffer.pos(x, y, z).tex(u, v).endVertex();
	}
}
