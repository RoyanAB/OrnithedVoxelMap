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

@SuppressWarnings("unused")
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
		previousFBOID = GL11.glGetInteger(36006);
		fboID = EXTFramebufferObject.glGenFramebuffersEXT();
		fboTextureID = GL11.glGenTextures();
		int width = fboSize;
		int height = fboSize;
		EXTFramebufferObject.glBindFramebufferEXT(36160, fboID);
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
		GL11.glBindTexture(GLShim.GL11_GL_TEXTURE_2D, fboTextureID);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10242, 10496);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10243, 10496);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10241, 9729);
		GL11.glTexParameteri(GLShim.GL11_GL_TEXTURE_2D, 10240, 9729);
		GL11.glTexImage2D(GLShim.GL11_GL_TEXTURE_2D, 0, 6408, width, height, 0, 6408, 5120, byteBuffer);
		EXTFramebufferObject.glFramebufferTexture2DEXT(36160, 36064, GLShim.GL11_GL_TEXTURE_2D, fboTextureID, 0);
		int depthRenderBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
		EXTFramebufferObject.glBindRenderbufferEXT(36161, depthRenderBufferID);
		EXTFramebufferObject.glRenderbufferStorageEXT(36161, 33190, fboSize, fboSize);
		EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, depthRenderBufferID);
		EXTFramebufferObject.glBindRenderbufferEXT(36161, 0);
		EXTFramebufferObject.glBindFramebufferEXT(36160, previousFBOID);
	}

	public static void bindFrameBuffer() {
		previousFBOID = GL11.glGetInteger(36006);
		EXTFramebufferObject.glBindFramebufferEXT(36160, fboID);
	}

	public static void unbindFrameBuffer() {
		EXTFramebufferObject.glBindFramebufferEXT(36160, previousFBOID);
	}

	public static void setMap(int x, int y) {
		setMap(x, y, 128);
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
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
	}

	public static void drawPre(VertexFormat vertexFormat) {
		vertexBuffer.begin(7, vertexFormat);
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
