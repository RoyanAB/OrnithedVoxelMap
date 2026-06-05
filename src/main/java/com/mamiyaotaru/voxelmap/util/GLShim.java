package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLShim {
	public static final int
		GL11_GL_ALPHA_TEST = 0xBC0,
		GL11_GL_DEPTH_BUFFER_BIT = 0x100,
		GL11_GL_LEQUAL = 0x203,
		GL11_GL_GREATER = 0x204,
		GL11_GL_ALWAYS = 0x207,
		GL11_GL_SRC_ALPHA = 0x302,
		GL11_GL_ONE_MINUS_SRC_ALPHA = 0x303,
		GL11_GL_DST_ALPHA = 0x304,
		GL11_GL_ONE_MINUS_DST_ALPHA = 0x305,
		GL11_GL_DST_COLOR = 0x306,
		GL11_GL_FOG = 0xB60,
		GL11_GL_CULL_FACE = 0xB44,
		GL11_GL_LIGHTING = 0xB50,
		GL11_GL_DEPTH_TEST = 0xB71,
		GL11_GL_NORMALIZE = 0xBA1,
		GL11_GL_BLEND = 0xBE2,
		GL11_GL_SCISSOR_TEST = 0xC11,
		GL11_GL_COLOR_CLEAR_VALUE = 0xC22,
		GL11_GL_UNPACK_ROW_LENGTH = 0xCF2,
		GL11_GL_UNPACK_SKIP_ROWS = 0xCF3,
		GL11_GL_UNPACK_SKIP_PIXELS = 0xCF4,
		GL11_GL_UNPACK_ALIGNMENT = 0xCF5,
		GL11_GL_FLAT = 0x1D00,
		GL11_GL_SMOOTH = 0x1D01,
		GL11_GL_PACK_ALIGNMENT = 0xD05,
		GL11_GL_TEXTURE_2D = 0xDE1,
		GL11_GL_TRANSFORM_BIT = 0x1000,
		GL11_GL_TEXTURE_HEIGHT = 0x1001,
		GL11_GL_BYTE = 0x1400,
		GL11_GL_UNSIGNED_BYTE = 0x1401,
		GL11_GL_MODELVIEW = 0x1700,
		GL11_GL_PROJECTION = 0x1701,
		GL11_GL_RGBA = 0x1908,
		GL11_GL_NEAREST = 0x2600,
		GL11_GL_LINEAR = 0x2601,
		GL11_GL_LINEAR_MIPMAP_LINEAR = 0x2703,
		GL11_GL_TEXTURE_MAG_FILTER = 0x2800,
		GL11_GL_TEXTURE_MIN_FILTER = 0x2801,
		GL11_GL_TEXTURE_WRAP_S = 0x2802,
		GL11_GL_TEXTURE_WRAP_T = 0x2803,
		GL11_GL_CLAMP = 0x2900,
		GL11_GL_COLOR_BUFFER_BIT = 0x4000,
		GL11_GL_POLYGON_OFFSET_FILL = 0x8037,
		GL11_GL_TEXTURE_BINDING_2D = 0x8069;

	public static final int
		GL12_GL_UNSIGNED_INT_8_8_8_8 = 0x8035,
		GL12_GL_BGRA = 0x80E1,
		GL12_GL_CLAMP_TO_EDGE = 0x812F,
		GL12_GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367;

	public static final int
		GL14_GL_GENERATE_MIPMAP = 0x8191;

	public static void glEnable(int attrib) {
		switch (attrib) {
			case GL11_GL_CULL_FACE:
				GlStateManager.enableCull();
				break;
			case GL11_GL_LIGHTING:
				GlStateManager.enableLighting();
				break;
			case GL11_GL_FOG:
				GlStateManager.enableFog();
				break;
			case GL11_GL_DEPTH_TEST:
				GlStateManager.enableDepth();
				break;
			case GL11_GL_NORMALIZE:
				GlStateManager.enableNormalize();
				break;
			case GL11_GL_ALPHA_TEST:
				GlStateManager.enableAlpha();
				break;
			case GL11_GL_BLEND:
				GlStateManager.enableBlend();
				break;
			case GL11_GL_SCISSOR_TEST:
				GL11.glEnable(GLShim.GL11_GL_SCISSOR_TEST);
				break;
			case GL11_GL_TEXTURE_2D:
				GlStateManager.enableTexture2D();
				break;
			case GL11_GL_POLYGON_OFFSET_FILL:
				GlStateManager.enablePolygonOffset();
		}
	}

	public static void glDisable(int attrib) {
		switch (attrib) {
			case GL11_GL_CULL_FACE:
				GlStateManager.disableCull();
				break;
			case GL11_GL_LIGHTING:
				GlStateManager.disableLighting();
				break;
			case GL11_GL_FOG:
				GlStateManager.disableFog();
				break;
			case GL11_GL_DEPTH_TEST:
				GlStateManager.disableDepth();
				break;
			case GL11_GL_NORMALIZE:
				GlStateManager.disableNormalize();
				break;
			case GL11_GL_ALPHA_TEST:
				GlStateManager.disableAlpha();
				break;
			case GL11_GL_BLEND:
				GlStateManager.disableBlend();
				break;
			case GL11_GL_SCISSOR_TEST:
				GL11.glDisable(GL11_GL_SCISSOR_TEST);
				break;
			case GL11_GL_TEXTURE_2D:
				GlStateManager.disableTexture2D();
				break;
			case GL11_GL_POLYGON_OFFSET_FILL:
				GlStateManager.disablePolygonOffset();
		}
	}

	public static void glAlphaFunc(int func, float ref) {
		GlStateManager.alphaFunc(func, ref);
	}

	public static void glBlendFunc(int sfactor, int dfactor) {
		GlStateManager.blendFunc(sfactor, dfactor);
	}

	public static void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
		GlStateManager.tryBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
	}

	public static void glCallList(int list) {
		GlStateManager.callList(list);
	}

	public static void glClear(int mask) {
		GlStateManager.clear(mask);
	}

	public static void glClearColor(float red, float green, float blue, float alpha) {
		GlStateManager.clearColor(red, green, blue, alpha);
	}

	public static void glClearDepth(double depth) {
		GlStateManager.clearDepth(depth);
	}

	public static void glColor3f(float red, float green, float blue) {
		GlStateManager.color(red, green, blue, 1.0F);
	}

	public static void glColor4f(float red, float green, float blue, float alpha) {
		GlStateManager.color(red, green, blue, alpha);
	}

	public static void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GlStateManager.colorMask(red, green, blue, alpha);
	}

	public static void glColorMaterial(int face, int mode) {
		GlStateManager.colorMaterial(face, mode);
	}

	public static void glDepthFunc(int func) {
		GlStateManager.depthFunc(func);
	}

	public static void glDepthMask(boolean flag) {
		GlStateManager.depthMask(flag);
	}

	public static int glGenTextures() {
		return GlStateManager.generateTexture();
	}

	public static void glGetFloat(int pname, FloatBuffer params) {
		GlStateManager.getFloat(pname, params);
	}

	public static void glLoadIdentity() {
		GlStateManager.loadIdentity();
	}

	public static void glLogicOp(int opcode) {
		GlStateManager.colorLogicOp(opcode);
	}

	public static void glMatrixMode(int mode) {
		GlStateManager.matrixMode(mode);
	}

	public static void glMultMatrix(FloatBuffer m) {
		GlStateManager.multMatrix(m);
	}

	public static void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
		GlStateManager.ortho(left, right, bottom, top, zNear, zFar);
	}

	public static void glPixelStorei(int int_1, int int_2) {
		GlStateManager.glPixelStorei(int_1, int_2);
	}

	public static void glPolygonOffset(float factor, float units) {
		GlStateManager.doPolygonOffset(factor, units);
	}

	public static void glPopAttrib() {
		GlStateManager.popAttrib();
	}

	public static void glPopMatrix() {
		GlStateManager.popMatrix();
	}

	public static void glPushAttrib() {
		GlStateManager.pushAttrib();
	}

	public static void glPushMatrix() {
		GlStateManager.pushMatrix();
	}

	public static void glRotatef(float angle, float x, float y, float z) {
		GlStateManager.rotate(angle, x, y, z);
	}

	public static void glScaled(double x, double y, double z) {
		GlStateManager.scale(x, y, z);
	}

	public static void glScalef(float x, float y, float z) {
		GlStateManager.scale(x, y, z);
	}

	public static void glSetActiveTextureUnit(int texture) {
		GlStateManager.setActiveTexture(texture);
	}

	public static void glShadeModel(int mode) {
		GlStateManager.shadeModel(mode);
	}

	public static void glTranslated(double x, double y, double z) {
		GlStateManager.translate(x, y, z);
	}

	public static void glTranslatef(float x, float y, float z) {
		GlStateManager.translate(x, y, z);
	}

	public static void glVertex3f(float x, float y, float z) {
		GlStateManager.glVertex3f(x, y, z);
	}

	public static void glViewport(int x, int y, int width, int height) {
		GlStateManager.viewport(x, y, width, height);
	}

	public static void glBegin(int mode) {
		GL11.glBegin(mode);
	}

	public static void glBindTexture(int target, int texture) {
		if (target == GL11_GL_TEXTURE_2D)
			GlStateManager.bindTexture(texture);
		else
			GL11.glBindTexture(target, texture);
	}

	public static void glEnd() {
		GL11.glEnd();
	}

	public static int glGetInteger(int pname) {
		return GL11.glGetInteger(pname);
	}

	public static void glGetTexImage(int tex, int level, int format, int type, ByteBuffer pixels) {
		GL11.glGetTexImage(tex, level, format, type, pixels);
	}

	public static int glGetTexLevelParameteri(int target, int level, int pname) {
		return GL11.glGetTexLevelParameteri(target, level, pname);
	}

	public static void glLoadMatrix(FloatBuffer buf) {
		GL11.glLoadMatrix(buf);
	}

	public static void glNormal3f(float nx, float ny, float nz) {
		GL11.glNormal3f(nx, ny, nz);
	}

	public static void glPushAttrib(int mask) {
		GL11.glPushAttrib(mask);
	}

	public static void glScissor(int x, int y, int width, int height) {
		GL11.glScissor(x, y, width, height);
	}

	public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels) {
		GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	public static void glTexParameteri(int target, int pname, int param) {
		GL11.glTexParameteri(target, pname, param);
	}

	public static void glVertex2f(float x, float y) {
		GL11.glVertex2f(x, y);
	}
}
