package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.GlStateManager.FogMode;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    public static final int GL_PROJECTION_MATRIX = 2983;
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
                GlStateManager.enableDepthTest();
                break;
            case 2977:
                GlStateManager.enableNormalize();
                break;
            case 3008:
                GlStateManager.enableAlphaTest();
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
                GlStateManager.disableDepthTest();
                break;
            case 2977:
                GlStateManager.disableNormalize();
                break;
            case 3008:
                GlStateManager.disableAlphaTest();
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
        switch (pname) {
            case 2917:
                FogMode fogMode = FogMode.EXP;
                switch (param) {
                    case 2048:
                        fogMode = FogMode.EXP;
                        break;
                    case 2049:
                        fogMode = FogMode.EXP2;
                        break;
                    case 9729:
                        fogMode = FogMode.LINEAR;
                }

                GlStateManager.fogMode(fogMode);
        }
    }

    public static void glFogf(int pname, float param) {
        switch (pname) {
            case 2914:
                GlStateManager.fogDensity(param);
                break;
            case 2915:
                GlStateManager.fogStart(param);
                break;
            case 2916:
                GlStateManager.fogEnd(param);
        }
    }

    public static void glAlphaFunc(int func, float ref) {
        GlStateManager.alphaFunc(func, ref);
    }

    public static void glBlendFunc(int sfactor, int dfactor) {
        GlStateManager.blendFunc(sfactor, dfactor);
    }

    public static void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GlStateManager.blendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
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
        GlStateManager.color3f(red, green, blue);
    }

    public static void glColor4f(float red, float green, float blue, float alpha) {
        GlStateManager.color4f(red, green, blue, alpha);
    }

    public static void glColor3ub(int red, int green, int blue) {
        GlStateManager.color4f(red / 255.0F, green / 255.0F, blue / 255.0F, 1.0F);
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
            case 1032:
                cullFace = CullFace.FRONT_AND_BACK;
        }

        GlStateManager.cullFace(cullFace);
    }

    public static void glDeleteTextures(int id) {
        GlStateManager.deleteTexture(id);
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
        GlStateManager.getFloatv(pname, params);
    }

    public static void glGetTexImage(int tex, int level, int format, int type, long pixels) {
        GlStateManager.getTexImage(tex, level, format, type, pixels);
    }

    public static int glGetTexLevelParameteri(int target, int level, int pname) {
        return GlStateManager.glGetTexLevelParameteri(target, level, pname);
    }

    public static void glLoadIdentity() {
        GlStateManager.loadIdentity();
    }

    public static void glLogicOp(int opcode) {
        GlStateManager.logicOp(opcode);
    }

    public static void glMatrixMode(int mode) {
        GlStateManager.matrixMode(mode);
    }

    public static void glMultMatrix(FloatBuffer m) {
        GlStateManager.multMatrixf(m);
    }

    public static void glNormal3f(float nx, float ny, float nz) {
        GlStateManager.normal3f(nx, ny, nz);
    }

    public static void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
        GlStateManager.ortho(left, right, bottom, top, zNear, zFar);
    }

    public static void glPixelStorei(int parameterName, int parameter) {
        GlStateManager.pixelStorei(parameterName, parameter);
    }

    public static void glPolygonOffset(float factor, float units) {
        GlStateManager.polygonOffset(factor, units);
    }

    public static void glPopAttrib() {
        GlStateManager.popAttrib();
    }

    public static void glPopMatrix() {
        GlStateManager.popMatrix();
    }

    public static void glPushAttrib() {
        GlStateManager.pushLightingAttrib();
    }

    public static void glPushMatrix() {
        GlStateManager.pushMatrix();
    }

    public static void glRotatef(float angle, float x, float y, float z) {
        GlStateManager.rotatef(angle, x, y, z);
    }

    public static void glScaled(double x, double y, double z) {
        GlStateManager.scaled(x, y, z);
    }

    public static void glScalef(float x, float y, float z) {
        GlStateManager.scalef(x, y, z);
    }

    public static void glSetActiveTextureUnit(int texture) {
        GlStateManager.activeTexture(texture);
    }

    public static void glShadeModel(int mode) {
        GlStateManager.shadeModel(mode);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels) {
        GlStateManager.texImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexParameterf(int target, int pname, float param) {
        GlStateManager.texParameterf(target, pname, param);
    }

    public static void glTexParameteri(int target, int pname, int param) {
        GlStateManager.texParameteri(target, pname, param);
    }

    public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, long memAddress) {
        GlStateManager.texSubImage2D(target, level, xOffset, yOffset, width, height, format, type, memAddress);
    }

    public static void glTranslated(double x, double y, double z) {
        GlStateManager.translated(x, y, z);
    }

    public static void glTranslatef(float x, float y, float z) {
        GlStateManager.translatef(x, y, z);
    }

    public static void glViewport(int x, int y, int width, int height) {
        GlStateManager.viewport(x, y, width, height);
    }

    public static void glBegin(int mode) {
        GL11.glBegin(mode);
    }

    public static void glBindTexture(int target, int texture) {
        switch (target) {
            case 3553:
                GlStateManager.bindTexture(texture);
                break;
            default:
                GL11.glBindTexture(target, texture);
        }
    }

    public static void glEnd() {
        GL11.glEnd();
    }

    public static boolean glGetBoolean(int pname) {
        return GL11.glGetBoolean(pname);
    }

    public static int glGetInteger(int pname) {
        return GL11.glGetInteger(pname);
    }

    public static void glGetTexImage(int tex, int level, int format, int type, ByteBuffer pixels) {
        GL11.glGetTexImage(tex, level, format, type, pixels);
    }

    public static void glGetTexImage(int tex, int level, int format, int type, IntBuffer pixels) {
        GL11.glGetTexImage(tex, level, format, type, pixels);
    }

    public static void glLoadMatrix(FloatBuffer buf) {
        GL11.glLoadMatrixf(buf);
    }

    public static void glPushAttrib(int mask) {
        GL11.glPushAttrib(mask);
    }

    public static void glScissor(int x, int y, int width, int height) {
        GL11.glScissor(x, y, width, height);
    }

    public static void glTexImage2D(int glTexture2d, int level, int glRgba, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        GL11.glTexImage2D(glTexture2d, level, glRgba, width, height, border, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, IntBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glVertex2f(float x, float y) {
        GL11.glVertex2f(x, y);
    }

    public static void glVertex3f(float x, float y, float z) {
        GL11.glVertex3f(x, y, z);
    }
}
