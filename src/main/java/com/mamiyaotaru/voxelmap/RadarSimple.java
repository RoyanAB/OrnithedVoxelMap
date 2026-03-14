package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.interfaces.IRadar;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombiePigmanEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class RadarSimple implements IRadar {
    public MapSettingsManager minimapOptions = null;
    public RadarSettingsManager options = null;
    UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
    private MinecraftClient game;
    private LayoutVariables layoutVariables = null;
    private final TextureAtlas textureAtlas;
    private final boolean enabled = true;
    private boolean completedLoading = false;
    private int timer = 500;
    private float direction = 0.0F;
    private final ArrayList<Contact> contacts = new ArrayList<>(40);

    public RadarSimple(IVoxelMap master) {
        this.minimapOptions = master.getMapOptions();
        this.options = master.getRadarOptions();
        this.game = MinecraftClient.getInstance();
        this.textureAtlas = new TextureAtlas("pings");
        this.textureAtlas.setFilter(false, false);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.loadTexturePackIcons();
    }

    private void loadTexturePackIcons() {
        this.completedLoading = false;

        try {
            this.textureAtlas.reset();
            BufferedImage contact = ImageUtils.loadImage(new Identifier("voxelmap", "images/radar/contact.png"), 0, 0, 32, 32, 32, 32);
            contact = ImageUtils.fillOutline(contact, false, true, 32, -10);
            this.textureAtlas.registerIconForBufferedImage("contact", contact);
            BufferedImage facing = ImageUtils.loadImage(new Identifier("voxelmap", "images/radar/contact_facing.png"), 0, 0, 32, 32, 32, 32);
            facing = ImageUtils.fillOutline(facing, false, true, 32, -10);
            this.textureAtlas.registerIconForBufferedImage("facing", facing);
            BufferedImage glow = ImageUtils.loadImage(new Identifier("voxelmap", "images/radar/glow.png"), 0, 0, 16, 16, 16, 16);
            glow = ImageUtils.fillOutline(glow, false, true, 16, -10);
            this.textureAtlas.registerIconForBufferedImage("glow", glow);
            this.textureAtlas.stitch();
            this.completedLoading = true;
        } catch (Exception e) {
            System.err.println("Failed getting mobs " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void OnTickInGame(MinecraftClient mc, LayoutVariables layoutVariables) {
        if (this.options.radarAllowed || this.options.radarMobsAllowed || this.options.radarPlayersAllowed) {
            if (this.game == null) {
                this.game = mc;
            }

            this.layoutVariables = layoutVariables;
            if (this.options.isChanged()) {
                this.timer = 500;
            }

            this.direction = GameVariableAccessShim.rotationYaw() + 180.0F;

            while (this.direction >= 360.0F) {
                this.direction -= 360.0F;
            }

            while (this.direction < 0.0F) {
                this.direction += 360.0F;
            }

            if (this.enabled) {
                if (this.completedLoading && this.timer > 95) {
                    this.calculateMobs();
                    this.timer = 0;
                }

                this.timer++;
                if (this.completedLoading) {
                    this.renderMapMobs(this.layoutVariables.mapX, this.layoutVariables.mapY);
                }

                GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    public void calculateMobs() {
        this.contacts.clear();

        for (Entity entity : this.game.world.getEntities()) {
            try {
                if (entity != null
                        && !entity.isInvisibleTo(this.game.player)
                        && (
                        this.options.showHostiles && (this.options.radarAllowed || this.options.radarMobsAllowed) && this.isHostile(entity)
                                || this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed) && this.isPlayer(entity)
                                || this.options.showNeutrals && this.options.radarMobsAllowed && this.isNeutral(entity)
                )) {
                    int wayX = GameVariableAccessShim.xCoord() - (int) entity.getPos().getX();
                    int wayZ = GameVariableAccessShim.zCoord() - (int) entity.getPos().getZ();
                    int wayY = GameVariableAccessShim.yCoord() - (int) entity.getPos().getY();
                    double hypot = wayX * wayX + wayZ * wayZ + wayY * wayY;
                    hypot /= this.layoutVariables.zoomScaleAdjusted * this.layoutVariables.zoomScaleAdjusted;
                    if (hypot < 961.0) {
                        Contact contact = new Contact(entity, this.getUnknownMobNeutrality(entity));
                        String unscrubbedName = contact.entity.getDisplayName().asFormattedString();
                        contact.setName(unscrubbedName);
                        contact.updateLocation();
                        if (contact.type != EnumMobs.UNKNOWN) {
                            contact.icons = new Sprite[]{
                                    this.textureAtlas
                                            .getAtlasSprite(
                                            contact.type.id
                                                    + contact.type.resourceLocation.toString()
                                                    + (contact.type.secondaryResourceLocation != null ? contact.type.secondaryResourceLocation.toString() : "")
                                                    + "0"
                                    ),
                                    this.textureAtlas
                                            .getAtlasSprite(
                                            contact.type.id
                                                    + contact.type.resourceLocation.toString()
                                                    + (contact.type.secondaryResourceLocation != null ? contact.type.secondaryResourceLocation.toString() : "")
                                                    + "1"
                                    )
                            };
                        }

                        this.contacts.add(contact);
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        Collections.sort(this.contacts, new Comparator<Contact>() {
            public int compare(Contact contact1, Contact contact2) {
                return contact1.y - contact2.y;
            }
        });
    }

    private EnumMobs getUnknownMobNeutrality(Entity entity) {
        if (this.isHostile(entity)) {
            return EnumMobs.GENERICHOSTILE;
        } else {
            return !(entity instanceof TameableEntity)
                    || !((TameableEntity) entity).isTamed()
                    || !this.game.isIntegratedServerRunning() && !((TameableEntity) entity).getOwner().equals(this.game.player)
                    ? EnumMobs.GENERICNEUTRAL
                    : EnumMobs.GENERICTAME;
        }
    }

    private boolean isHostile(Entity entity) {
        if (entity instanceof ZombiePigmanEntity) {
            return ((ZombiePigmanEntity) entity).isAngryAt(this.game.player);
        }

        if (entity instanceof Monster) {
            return true;
        }

        if (entity instanceof PolarBearEntity) {
            for (Object object : ((PolarBearEntity) entity)
                    .world
                    .getNonSpectatingEntities(PolarBearEntity.class, entity.getBoundingBox().expand(8.0, 4.0, 8.0))) {
                if (((PolarBearEntity) object).isBaby()) {
                    return true;
                }
            }
        }

        if (entity instanceof RabbitEntity) {
            return ((RabbitEntity) entity).getRabbitType() == 99;
        } else {
            return entity instanceof WolfEntity && ((WolfEntity) entity).isAngry();
        }
    }

    private boolean isPlayer(Entity entity) {
        return entity instanceof OtherClientPlayerEntity;
    }

    private boolean isNeutral(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof PlayerEntity) && !this.isHostile(entity);
    }

    public void renderMapMobs(int x, int y) {
        double max = this.layoutVariables.zoomScaleAdjusted * 32.0;
        GLUtils.disp(this.textureAtlas.getGlId());

        for (Contact contact : this.contacts) {
            contact.updateLocation();
            double contactX = contact.x;
            double contactZ = contact.z;
            int contactY = contact.y;
            double wayX = GameVariableAccessShim.xCoordDouble() - contactX;
            double wayZ = GameVariableAccessShim.zCoordDouble() - contactZ;
            int wayY = GameVariableAccessShim.yCoord() - contactY;
            double adjustedDiff = max - Math.max(Math.abs(wayY), 0);
            contact.brightness = (float) Math.max(adjustedDiff / max, 0.0);
            contact.brightness = contact.brightness * contact.brightness;
            contact.angle = (float) Math.toDegrees(Math.atan2(wayX, wayZ));
            contact.distance = Math.sqrt(wayX * wayX + wayZ * wayZ) / this.layoutVariables.zoomScaleAdjusted;
            GLShim.glBlendFunc(770, 771);
            if (wayY < 0) {
                GLShim.glColor4f(1.0F, 1.0F, 1.0F, contact.brightness);
            } else {
                GLShim.glColor3f(contact.brightness, contact.brightness, contact.brightness);
            }

            if (this.minimapOptions.rotates) {
                contact.angle = contact.angle + this.direction;
            } else if (this.minimapOptions.oldNorth) {
                contact.angle -= 90.0F;
            }

            boolean inRange = false;
            if (this.minimapOptions.squareMap) {
                double radLocate = Math.toRadians(contact.angle);
                double dispX = contact.distance * Math.cos(radLocate);
                double dispY = contact.distance * Math.sin(radLocate);
                inRange = Math.abs(dispX) <= 28.5 && Math.abs(dispY) <= 28.5;
            } else {
                inRange = contact.distance < 31.0;
            }

            if (inRange) {
                try {
                    GLShim.glPushMatrix();
                    float contactFacing = contact.entity.getHeadYaw();
                    if (this.minimapOptions.rotates) {
                        contactFacing -= this.direction;
                    } else if (this.minimapOptions.oldNorth) {
                        contactFacing += 90.0F;
                    }

                    GLShim.glTranslatef(x, y, 0.0F);
                    GLShim.glRotatef(-contact.angle, 0.0F, 0.0F, 1.0F);
                    GLShim.glTranslated(0.0, -contact.distance, 0.0);
                    GLShim.glRotatef(contact.angle + contactFacing, 0.0F, 0.0F, 1.0F);
                    GLShim.glTranslatef(-x, -y, 0.0F);
                    if (contact.uuid != null && contact.uuid.equals(this.devUUID)) {
                        Sprite icon = this.textureAtlas.getAtlasSprite("glow");
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, x, y, (int) (icon.getIconWidth() / 2.0F));
                        GLUtils.drawPost();
                    }

                    this.applyFilteringParameters();
                    GLUtils.drawPre();
                    GLUtils.setMap(this.textureAtlas.getAtlasSprite("contact"), x, y, 16.0F);
                    GLUtils.drawPost();
                    if (this.options.showFacing) {
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(this.textureAtlas.getAtlasSprite("facing"), x, y, 16.0F);
                        GLUtils.drawPost();
                    }
                } catch (Exception localException) {
                    System.err.println("Error rendering mob icon! " + localException.getLocalizedMessage() + " contact type " + contact.type);
                } finally {
                    GLShim.glPopMatrix();
                }
            }
        }
    }

    private void applyFilteringParameters() {
        if (this.options.filtering) {
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTexParameteri(3553, 10242, 10496);
            GLShim.glTexParameteri(3553, 10243, 10496);
        } else {
            GLShim.glTexParameteri(3553, 10241, 9728);
            GLShim.glTexParameteri(3553, 10240, 9728);
        }
    }
}
