package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.GlStateManager.FogMode;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@SuppressWarnings("unused")
public class GLShim {
	public static final int GL_ALPHA_TEST = 3008;
	public static final int GL_BLEND = 3042;
	public static final int GL_CLAMP = 10496;
	public static final int GL_CLAMP_TO_EDGE = 33071;
	public static final int GL_COLOR_BUFFER_BIT = 16384;
	public static final int GL_COLOR_CLEAR_VALUE = 3106;
	public static final int GL_CULL_FACE = 2884;
	public static final int GL_DEPTH_BUFFER_BIT = 256;
	public static final int GL_DST_ALPHA = 772;
	public static final int GL_DST_COLOR = 774;
	public static final int GL_FOG = 2912;
	public static final int GL_DEPTH_TEST = 2929;
	public static final int GL_FLAT = 7424;
	public static final int GL_FOG_DENSITY = 2914;
	public static final int GL_FOG_END = 2916;
	public static final int GL_FOG_MODE = 2917;
	public static final int GL_FOG_START = 2915;
	public static final int GL_GENERATE_MIPMAP = 33169;
	public static final int GL_GREATER = 516;
	public static final int GL_LIGHTING = 2896;
	public static final int GL_LINEAR = 9729;
	public static final int GL_LINES = 1;
	public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
	public static final int GL_LINEAR_MIPMAP_NEAREST = 9985;
	public static final int GL_MODELVIEW = 5888;
	public static final int GL_NEAREST = 9728;
	public static final int GL_NEAREST_MIPMAP_LINEAR = 9986;
	public static final int GL_NEAREST_MIPMAP_NEAREST = 9984;
	public static final int GL_NORMALIZE = 2977;
	public static final int GL_ONE = 1;
	public static final int GL_ONE_MINUS_DST_ALPHA = 773;
	public static final int GL_ONE_MINUS_DST_COLOR = 775;
	public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
	public static final int GL_ONE_MINUS_SRC_COLOR = 769;
	public static final int GL_PACK_ALIGNMENT = 3333;
	public static final int GL_POLYGON_OFFSET_FILL = 32823;
	public static final int GL_PROJECTION = 5889;
	public static final int GL_QUADS = 7;
	public static final int GL_RGBA = 6408;
	public static final int GL_SMOOTH = 7425;
	public static final int GL_SCISSOR_TEST = 3089;
	public static final int GL_SRC_ALPHA = 770;
	public static final int GL_TEXTURE_2D = 3553;
	public static final int GL_TEXTURE_BINDING_2D = 32873;
	public static final int GL_TEXTURE_HEIGHT = 4097;
	public static final int GL_TEXTURE_MAG_FILTER = 10240;
	public static final int GL_TEXTURE_MIN_FILTER = 10241;
	public static final int GL_TEXTURE_WIDTH = 4096;
	public static final int GL_TEXTURE_WRAP_S = 10242;
	public static final int GL_TEXTURE_WRAP_T = 10243;
	public static final int GL_TRUE = 1;
	public static final int GL_TRANSFORM_BIT = 4096;
	public static final int GL_UNPACK_ALIGNMENT = 3317;
	public static final int GL_UNPACK_ROW_LENGTH = 3314;
	public static final int GL_UNPACK_SKIP_PIXELS = 3316;
	public static final int GL_UNPACK_SKIP_ROWS = 3315;
	public static final int GL_UNSIGNED_BYTE = 5121;
	public static final int GL_UNSIGNED_INT_8_8_8_8 = 32821;
	public static final int GL_VIEWPORT_BIT = 2048;
	public static final int GL_ZERO = 0;
	public static final int GL_BGRA = 32993;
	public static final int GL_RESCALE_NORMAL = 32826;
	public static final int GL_UNSIGNED_INT_8_8_8_8_REV = 33639;

	public static void glEnable(int attrib) {
		switch (attrib) {
			case 2884:
				GlStateManager.enableCull();
				break;
			case 2896:
				GlStateManager.enableLighting();
				break;
			case 2912:
				GlStateManager.enableFog();
				break;
			case 2929:
				GlStateManager.enableDepth();
				break;
			case 2977:
				GlStateManager.enableNormalize();
				break;
			case 3008:
				GlStateManager.enableAlpha();
				break;
			case 3042:
				GlStateManager.enableBlend();
				break;
			case 3089:
				GL11.glEnable(3089);
				break;
			case 3553:
				GlStateManager.enableTexture2D();
				break;
			case 32823:
				GlStateManager.enablePolygonOffset();
				break;
			case 32826:
				GlStateManager.enableRescaleNormal();
		}
	}

	public static void glDisable(int attrib) {
		switch (attrib) {
			case 2884:
				GlStateManager.disableCull();
				break;
			case 2896:
				GlStateManager.disableLighting();
				break;
			case 2912:
				GlStateManager.disableFog();
				break;
			case 2929:
				GlStateManager.disableDepth();
				break;
			case 2977:
				GlStateManager.disableNormalize();
				break;
			case 3008:
				GlStateManager.disableAlpha();
				break;
			case 3042:
				GlStateManager.disableBlend();
				break;
			case 3089:
				GL11.glDisable(3089);
				break;
			case 3553:
				GlStateManager.disableTexture2D();
				break;
			case 32823:
				GlStateManager.disablePolygonOffset();
				break;
			case 32826:
				GlStateManager.disableRescaleNormal();
		}
	}

	public static void glFogi(int pname, int param) {
		if (pname == 2917) {
			FogMode fogMode = FogMode.EXP;
			switch (param) {
				case 2048:
					break;
				case 2049:
					fogMode = FogMode.EXP2;
					break;
				case 9729:
					fogMode = FogMode.LINEAR;
			}

			GlStateManager.setFog(fogMode);
		}
	}

	public static void glFogf(int pname, float param) {
		switch (pname) {
			case 2914:
				GlStateManager.setFogDensity(param);
				break;
			case 2915:
				GlStateManager.setFogStart(param);
				break;
			case 2916:
				GlStateManager.setFogEnd(param);
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

	public static void glCullFace(int mode) {
		CullFace cullFace = CullFace.FRONT_AND_BACK;
		switch (mode) {
			case 1028:
				cullFace = CullFace.FRONT;
				break;
			case 1029:
				cullFace = CullFace.BACK;
			case 1030:
			case 1031:
			default:
				break;
		}

		GlStateManager.cullFace(cullFace);
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
		if (target == 3553) {
			GlStateManager.bindTexture(texture);
		} else {
			GL11.glBindTexture(target, texture);
		}
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
