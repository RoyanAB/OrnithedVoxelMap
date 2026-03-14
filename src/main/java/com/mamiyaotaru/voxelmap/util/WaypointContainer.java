package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.options.Option;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class WaypointContainer {
    private final String TARGETFLAG = "*&^TARget%$^";
    public MapSettingsManager options = null;
    private final ArrayList<Waypoint> wayPts = new ArrayList<>();
    private Waypoint highlightedWaypoint = null;
    private final MinecraftClient mc;

    public WaypointContainer(MapSettingsManager options) {
        this.mc = MinecraftClient.getInstance();
        this.options = options;
    }

    public void addWaypoint(Waypoint newWaypoint) {
        this.wayPts.add(newWaypoint);
    }

    public void removeWaypoint(Waypoint waypoint) {
        this.wayPts.remove(waypoint);
    }

    public void setHighlightedWaypoint(Waypoint highlightedWaypoint) {
        this.highlightedWaypoint = highlightedWaypoint;
    }

    private void sortWaypoints() {
        Collections.sort(this.wayPts, Collections.reverseOrder());
    }

    public void renderWaypoints(float partialTicks) {
        this.sortWaypoints();
        Entity cameraEntity = this.options.game.getCameraEntity();
        double renderPosX = cameraEntity.lastRenderX + (cameraEntity.getPos().getX() - cameraEntity.lastRenderX) * partialTicks;
        double renderPosY = cameraEntity.lastRenderY + (cameraEntity.getPos().getY() - cameraEntity.lastRenderY) * partialTicks;
        double renderPosZ = cameraEntity.lastRenderZ + (cameraEntity.getPos().getZ() - cameraEntity.lastRenderZ) * partialTicks;
        GLShim.glEnable(2884);

        for (Waypoint pt : this.wayPts) {
            if (pt.isActive() || pt == this.highlightedWaypoint) {
                int x = pt.getX();
                int z = pt.getZ();
                int y = pt.getY();
                WorldChunk chunk = this.mc.world.getChunk(x >> 4, z >> 4);
                if (this.options.showBeacons && chunk != null && !chunk.isEmpty() && this.mc.world.isChunkLoaded(x >> 4, z >> 4)) {
                    double bottomOfWorld = 0.0 - renderPosY;
                    this.renderBeam(pt, x - renderPosX, bottomOfWorld, z - renderPosZ, 64.0F);
                }

                double distance = Math.sqrt(pt.getDistanceSqToEntity(cameraEntity));
                if ((distance < this.options.maxWaypointDisplayDistance || this.options.maxWaypointDisplayDistance < 0 || pt == this.highlightedWaypoint)
                        && this.options.showWaypoints
                        && !this.options.game.options.hudHidden) {
                    boolean isPointedAt = this.isPointedAt(pt, distance, cameraEntity, partialTicks);
                    String label = pt.name;
                    this.renderLabel(pt, distance, isPointedAt, label, x - renderPosX, y - renderPosY - 0.5, z - renderPosZ, 64);
                }
            }
        }

        if (this.highlightedWaypoint != null && this.options.showWaypoints && !this.options.game.options.hudHidden) {
            int xx = this.highlightedWaypoint.getX();
            int zx = this.highlightedWaypoint.getZ();
            int yx = this.highlightedWaypoint.getY();
            double distance = Math.sqrt(this.highlightedWaypoint.getDistanceSqToEntity(cameraEntity));
            boolean isPointedAt = this.isPointedAt(this.highlightedWaypoint, distance, cameraEntity, partialTicks);
            this.renderLabel(this.highlightedWaypoint, distance, isPointedAt, "*&^TARget%$^", xx - renderPosX, yx - renderPosY - 0.5, zx - renderPosZ, 64);
        }
    }

    private boolean isPointedAt(Waypoint waypoint, double distance, Entity cameraEntity, Float partialTicks) {
        Vec3d cameraPos = cameraEntity.getCameraPosVec(partialTicks);
        double degrees = 5.0 + Math.min(5.0 / distance, 5.0);
        double angle = degrees * 0.0174533;
        double size = Math.sin(angle) * distance;
        Vec3d cameraPosPlusDirection = cameraEntity.getRotationVec(partialTicks);
        Vec3d cameraPosPlusDirectionTimesDistance = cameraPos.add(
                cameraPosPlusDirection.x * distance, cameraPosPlusDirection.y * distance, cameraPosPlusDirection.z * distance
        );
        Box axisalignedbb = new Box(
                waypoint.getX() + 0.5F - size,
                waypoint.getY() + 1.5F - size,
                waypoint.getZ() + 0.5F - size,
                waypoint.getX() + 0.5F + size,
                waypoint.getY() + 1.5F + size,
                waypoint.getZ() + 0.5F + size
        );
        Optional<Vec3d> raytraceresult = axisalignedbb.rayTrace(cameraPos, cameraPosPlusDirectionTimesDistance);
        if (axisalignedbb.contains(cameraPos)) {
            return distance >= 1.0;
        } else return raytraceresult.isPresent();
    }

    private void renderBeam(Waypoint par1EntityWaypoint, double baseX, double baseY, double baseZ, float par8) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        GLShim.glDisable(3553);
        GLShim.glDisable(2896);
        GLShim.glDisable(2912);
        GLShim.glDepthMask(false);
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 1);
        int height = 256;
        float brightness = 0.06F;
        double topWidthFactor = 1.05;
        double bottomWidthFactor = 1.05;
        float r = par1EntityWaypoint.red;
        float b = par1EntityWaypoint.blue;
        float g = par1EntityWaypoint.green;

        for (int width = 0; width < 4; width++) {
            vertexBuffer.begin(5, VertexFormats.POSITION_COLOR);
            double d6 = 0.1 + width * 0.2;
            d6 *= topWidthFactor;
            double d7 = 0.1 + width * 0.2;
            d7 *= bottomWidthFactor;

            for (int side = 0; side < 5; side++) {
                double vertX2 = baseX + 0.5 - d6;
                double vertZ2 = baseZ + 0.5 - d6;
                if (side == 1 || side == 2) {
                    vertX2 += d6 * 2.0;
                }

                if (side == 2 || side == 3) {
                    vertZ2 += d6 * 2.0;
                }

                double vertX1 = baseX + 0.5 - d7;
                double vertZ1 = baseZ + 0.5 - d7;
                if (side == 1 || side == 2) {
                    vertX1 += d7 * 2.0;
                }

                if (side == 2 || side == 3) {
                    vertZ1 += d7 * 2.0;
                }

                vertexBuffer.vertex(vertX1, baseY + 0.0, vertZ1).color(r * brightness, g * brightness, b * brightness, 0.8F).next();
                vertexBuffer.vertex(vertX2, baseY + height, vertZ2).color(r * brightness, g * brightness, b * brightness, 0.8F).next();
            }

            tessellator.draw();
        }

        GLShim.glDisable(3042);
        GLShim.glEnable(2912);
        GLShim.glEnable(2896);
        GLShim.glEnable(3553);
        GLShim.glDepthMask(true);
    }

    private void renderLabel(Waypoint pt, double distance, boolean isPointedAt, String name, double par3, double par5, double par7, int par9) {
        GLShim.glAlphaFunc(516, 0.1F);
        boolean target = name == "*&^TARget%$^";
        if (target) {
            if (pt.red == 2.0F && pt.green == 0.0F && pt.blue == 0.0F) {
                name = "X:" + pt.getX() + ", Y:" + pt.getY() + ", Z:" + pt.getZ();
            } else {
                isPointedAt = false;
            }
        }

        name = name + " (" + (int) distance + "m)";
        double maxDistance = Option.RENDER_DISTANCE.get(this.options.game.options) * 16.0 * 0.99;
        double adjustedDistance = distance;
        if (distance > maxDistance) {
            par3 = par3 / distance * maxDistance;
            par5 = par5 / distance * maxDistance;
            par7 = par7 / distance * maxDistance;
            adjustedDistance = maxDistance;
        }

        float var14 = ((float) adjustedDistance * 0.1F + 1.0F) * 0.0266F;
        GLShim.glPushMatrix();
        GLShim.glTranslatef((float) par3 + 0.5F, (float) par5 + 0.5F, (float) par7 + 0.5F);
        GLShim.glNormal3f(0.0F, 1.0F, 0.0F);
        GLShim.glRotatef(-this.mc.getEntityRenderManager().cameraYaw, 0.0F, 1.0F, 0.0F);
        GLShim.glRotatef(this.mc.getEntityRenderManager().cameraPitch, 1.0F, 0.0F, 0.0F);
        GLShim.glScalef(-var14, -var14, var14);
        GLShim.glDisable(2896);
        GLShim.glDisable(2912);
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        float fade = distance > 5.0 ? 1.0F : (float) distance / 5.0F;
        fade = Math.min(fade, !pt.enabled && !target ? 0.3F : 1.0F);
        if (distance < maxDistance) {
            GLShim.glDepthMask(true);
        }

        GLShim.glEnable(3553);
        double width = 10.0;
        float r = target ? 1.0F : pt.red;
        float g = target ? 0.0F : pt.green;
        float b = target ? 0.0F : pt.blue;
        TextureAtlas textureAtlas = AbstractVoxelMap.getInstance().getWaypointManager().getTextureAtlas();
        Sprite icon = target
                ? textureAtlas.getAtlasSprite("voxelmap:images/waypoints/target.png")
                : textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint" + pt.imageSuffix + ".png");
        if (icon == textureAtlas.getMissingImage()) {
            icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint.png");
        }

        GLUtils.disp(textureAtlas.getGlId());
        GLShim.glEnable(2929);
        vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        vertexBuffer.vertex(-width, -width, 0.0).texture(icon.getMinU(), icon.getMinV()).color(r, g, b, fade).next();
        vertexBuffer.vertex(-width, width, 0.0).texture(icon.getMinU(), icon.getMaxV()).color(r, g, b, fade).next();
        vertexBuffer.vertex(width, width, 0.0).texture(icon.getMaxU(), icon.getMaxV()).color(r, g, b, fade).next();
        vertexBuffer.vertex(width, -width, 0.0).texture(icon.getMaxU(), icon.getMinV()).color(r, g, b, fade).next();
        tessellator.draw();
        GLShim.glDisable(2929);
        GLShim.glDepthMask(false);
        vertexBuffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        vertexBuffer.vertex(-width, -width, 0.0).texture(icon.getMinU(), icon.getMinV()).color(r, g, b, 0.3F * fade).next();
        vertexBuffer.vertex(-width, width, 0.0).texture(icon.getMinU(), icon.getMaxV()).color(r, g, b, 0.3F * fade).next();
        vertexBuffer.vertex(width, width, 0.0).texture(icon.getMaxU(), icon.getMaxV()).color(r, g, b, 0.3F * fade).next();
        vertexBuffer.vertex(width, -width, 0.0).texture(icon.getMaxU(), icon.getMinV()).color(r, g, b, 0.3F * fade).next();
        tessellator.draw();
        GLShim.glDepthMask(false);
        GLShim.glDisable(2929);
        byte elevateBy = -18;
        TextRenderer fontRenderer = this.mc.getEntityRenderManager().getTextRenderer();
        if (isPointedAt && fontRenderer != null) {
            GLShim.glDisable(3553);
            int halfStringWidth = fontRenderer.getStringWidth(name) / 2;
            GLShim.glEnable(2929);
            if (distance < maxDistance) {
                GLShim.glDepthMask(true);
            }

            GLShim.glEnable(32823);
            GLShim.glPolygonOffset(1.0F, 3.0F);
            vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
            vertexBuffer.vertex(-halfStringWidth - 2, -2 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.6F * fade).next();
            vertexBuffer.vertex(-halfStringWidth - 2, 9 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.6F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 2, 9 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.6F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 2, -2 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.6F * fade).next();
            tessellator.draw();
            GLShim.glPolygonOffset(1.0F, 1.0F);
            vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
            vertexBuffer.vertex(-halfStringWidth - 1, -1 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            vertexBuffer.vertex(-halfStringWidth - 1, 8 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 1, 8 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 1, -1 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            tessellator.draw();
            GLShim.glDisable(2929);
            GLShim.glDepthMask(false);
            GLShim.glPolygonOffset(1.0F, 7.0F);
            vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
            vertexBuffer.vertex(-halfStringWidth - 2, -2 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.15F * fade).next();
            vertexBuffer.vertex(-halfStringWidth - 2, 9 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.15F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 2, 9 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.15F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 2, -2 + elevateBy, 0.0).color(pt.red, pt.green, pt.blue, 0.15F * fade).next();
            tessellator.draw();
            GLShim.glPolygonOffset(1.0F, 5.0F);
            vertexBuffer.begin(7, VertexFormats.POSITION_COLOR);
            vertexBuffer.vertex(-halfStringWidth - 1, -1 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            vertexBuffer.vertex(-halfStringWidth - 1, 8 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 1, 8 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            vertexBuffer.vertex(halfStringWidth + 1, -1 + elevateBy, 0.0).color(0.0F, 0.0F, 0.0F, 0.15F * fade).next();
            tessellator.draw();
            GLShim.glDisable(32823);
            GLShim.glEnable(3553);
            int textColor = (int) (255.0F * fade) << 24 | 13421772;
            fontRenderer.draw(name, -fontRenderer.getStringWidth(name) / 2, elevateBy, textColor);
            GLShim.glEnable(2929);
            int var34 = (int) (255.0F * fade) << 24 | 16777215;
            fontRenderer.draw(name, -fontRenderer.getStringWidth(name) / 2, elevateBy, var34);
        }

        GLShim.glEnable(2929);
        GLShim.glDepthMask(true);
        GLShim.glEnable(2912);
        GLShim.glEnable(2896);
        GLShim.glDisable(3042);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLShim.glPopMatrix();
    }
}
