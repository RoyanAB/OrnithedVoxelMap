package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class GLUtils {
    private static final boolean fboEnabledArb = GL.getCapabilities().GL_ARB_framebuffer_object;
    private static final boolean fboEnabledBase = GL.getCapabilities().OpenGL30;
    private static final IntBuffer dataBuffer = GlAllocationUtils.allocateByteBuffer(16777216).asIntBuffer();
    public static TextureManager textureManager;
    public static int fboID = 0;
    public static boolean openGL14Enabled = GL.getCapabilities().OpenGL14;
    private static final boolean fboEnabledExt = GL.getCapabilities().GL_EXT_framebuffer_object && openGL14Enabled;
    public static final boolean fboEnabled = fboEnabledExt || fboEnabledArb || fboEnabledBase;
    public static int fboTextureID = 0;
    public static boolean hasAlphaBits = GL11.glGetInteger(3413) > 0;
    public static int fboSize = 512;
    public static int fboRad = 256;
    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final BufferBuilder vertexBuffer = tessellator.getBuffer();
    private static int previousFBOID = 0;

    public static void setupFrameBuffer() {
        if (fboEnabledBase) {
            setupFrameBufferBASE();
        } else if (fboEnabledArb) {
            setupFrameBufferARB();
        } else if (fboEnabledExt) {
            setupFrameBufferEXT();
        }
    }

    private static void setupFrameBufferBASE() {
        previousFBOID = GL11.glGetInteger(36006);
        fboID = GL30.glGenFramebuffers();
        fboTextureID = GL11.glGenTextures();
        int width = fboSize;
        int height = fboSize;
        GL30.glBindFramebuffer(36160, fboID);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
        GLShim.glBindTexture(3553, fboTextureID);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5120, byteBuffer);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, fboTextureID, 0);
        int depthRenderBufferID = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(36161, depthRenderBufferID);
        GL30.glRenderbufferStorage(36161, 33190, width, height);
        GL30.glFramebufferRenderbuffer(36160, 36096, 36161, depthRenderBufferID);
        GL30.glBindRenderbuffer(36161, 0);
        GL30.glBindFramebuffer(36160, previousFBOID);
    }

    private static void setupFrameBufferARB() {
        previousFBOID = GL11.glGetInteger(36006);
        fboID = ARBFramebufferObject.glGenFramebuffers();
        fboTextureID = GL11.glGenTextures();
        int width = fboSize;
        int height = fboSize;
        ARBFramebufferObject.glBindFramebuffer(36160, fboID);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
        GLShim.glBindTexture(3553, fboTextureID);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5120, byteBuffer);
        ARBFramebufferObject.glFramebufferTexture2D(36160, 36064, 3553, fboTextureID, 0);
        int depthRenderBufferID = ARBFramebufferObject.glGenRenderbuffers();
        ARBFramebufferObject.glBindRenderbuffer(36161, depthRenderBufferID);
        ARBFramebufferObject.glRenderbufferStorage(36161, 33190, width, height);
        ARBFramebufferObject.glFramebufferRenderbuffer(36160, 36096, 36161, depthRenderBufferID);
        ARBFramebufferObject.glBindRenderbuffer(36161, 0);
        ARBFramebufferObject.glBindFramebuffer(36160, previousFBOID);
    }

    private static void setupFrameBufferEXT() {
        previousFBOID = GL11.glGetInteger(36006);
        fboID = EXTFramebufferObject.glGenFramebuffersEXT();
        fboTextureID = GL11.glGenTextures();
        int width = fboSize;
        int height = fboSize;
        EXTFramebufferObject.glBindFramebufferEXT(36160, fboID);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
        GLShim.glBindTexture(3553, fboTextureID);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5120, byteBuffer);
        EXTFramebufferObject.glFramebufferTexture2DEXT(36160, 36064, 3553, fboTextureID, 0);
        int depthRenderBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, depthRenderBufferID);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 33190, width, height);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, depthRenderBufferID);
        EXTFramebufferObject.glBindRenderbufferEXT(36161, 0);
        EXTFramebufferObject.glBindFramebufferEXT(36160, previousFBOID);
    }

    public static void bindFrameBuffer() {
        if (fboEnabledBase) {
            bindFrameBufferBASE();
        } else if (fboEnabledArb) {
            bindFrameBufferARB();
        } else if (fboEnabledExt) {
            bindFrameBufferEXT();
        }
    }

    private static void bindFrameBufferBASE() {
        previousFBOID = GL11.glGetInteger(36006);
        GL30.glBindFramebuffer(36160, fboID);
    }

    private static void bindFrameBufferARB() {
        previousFBOID = GL11.glGetInteger(36006);
        ARBFramebufferObject.glBindFramebuffer(36160, fboID);
    }

    private static void bindFrameBufferEXT() {
        previousFBOID = GL11.glGetInteger(36006);
        EXTFramebufferObject.glBindFramebufferEXT(36160, fboID);
    }

    public static void unbindFrameBuffer() {
        if (fboEnabledBase) {
            unbindFrameBufferBASE();
        } else if (fboEnabledArb) {
            unbindFrameBufferARB();
        } else if (fboEnabledExt) {
            unbindFrameBufferEXT();
        }
    }

    private static void unbindFrameBufferBASE() {
        GL30.glBindFramebuffer(36160, previousFBOID);
    }

    private static void unbindFrameBufferARB() {
        ARBFramebufferObject.glBindFramebuffer(36160, previousFBOID);
    }

    private static void unbindFrameBufferEXT() {
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
        int glid = GLShim.glGenTextures();
        int width = paramImg.getWidth();
        int height = paramImg.getHeight();
        int[] imageData = new int[width * height];
        paramImg.getRGB(0, 0, width, height, imageData, 0, width);
        GLShim.glBindTexture(3553, glid);
        ((Buffer) dataBuffer).clear();
        dataBuffer.put(imageData, 0, width * height);
        ((Buffer) dataBuffer).position(0).limit(width * height);
        GlStateManager.pixelStore(3314, 0);
        GlStateManager.pixelStore(3316, 0);
        GlStateManager.pixelStore(3315, 0);
        GLShim.glTexImage2D(3553, 0, 6408, width, height, 0, 32993, 33639, dataBuffer);
        return glid;
    }

    public static void img(String paramStr) {
        textureManager.bindTexture(new Identifier(paramStr));
    }

    public static void img(Identifier paramResourceLocation) {
        textureManager.bindTexture(paramResourceLocation);
    }

    public static void disp(int paramInt) {
        GlStateManager.bindTexture(paramInt);
    }

    public static void drawPre() {
        vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE);
    }

    public static void drawPre(VertexFormat vertexFormat) {
        vertexBuffer.begin(7, vertexFormat);
    }

    public static void drawPost() {
        tessellator.draw();
    }

    public static void glah(int g) {
        GLShim.glDeleteTextures(g);
    }

    public static void ldrawone(int x, int y, double z, double u, double v) {
        vertexBuffer.vertex(x, y, z).texture(u, v).next();
    }

    public static void ldrawtwo(double x, double y, double z) {
        vertexBuffer.vertex(x, y, z).next();
    }

    public static void ldrawthree(double x, double y, double z, double u, double v) {
        vertexBuffer.vertex(x, y, z).texture(u, v).next();
    }
}
