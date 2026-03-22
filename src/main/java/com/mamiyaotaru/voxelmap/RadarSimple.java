package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.interfaces.IRadar;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class RadarSimple implements IRadar {
	public MapSettingsManager minimapOptions;
	public RadarSettingsManager options;
	UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
	private Minecraft game;
	private LayoutVariables layoutVariables;
	private final TextureAtlas textureAtlas;
	private final boolean enabled = true;
	private boolean completedLoading = false;
	private int timer = 500;
	private float direction = 0.0F;
	private final ArrayList<Contact> contacts = new ArrayList<>(40);

	public RadarSimple(IVoxelMap master) {
		this.minimapOptions = master.getMapOptions();
		this.options = master.getRadarOptions();
		this.game = Minecraft.getMinecraft();
		this.textureAtlas = new TextureAtlas("pings");
		this.textureAtlas.setBlurMipmapDirect(false, false);
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.loadTexturePackIcons();
	}

	private void loadTexturePackIcons() {
		this.completedLoading = false;

		try {
			this.textureAtlas.reset();
			BufferedImage contact = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/contact.png"), 0, 0, 32, 32, 32, 32);
			contact = ImageUtils.fillOutline(contact, false, true, 32, -10);
			this.textureAtlas.registerIconForBufferedImage("contact", contact);
			BufferedImage facing = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/contact_facing.png"), 0, 0, 32, 32, 32, 32);
			facing = ImageUtils.fillOutline(facing, false, true, 32, -10);
			this.textureAtlas.registerIconForBufferedImage("facing", facing);
			BufferedImage glow = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/glow.png"), 0, 0, 16, 16, 16, 16);
			glow = ImageUtils.fillOutline(glow, false, true, 16, -10);
			this.textureAtlas.registerIconForBufferedImage("glow", glow);
			this.textureAtlas.stitch();
			this.completedLoading = true;
		} catch (Exception e) {
			VoxelMapMod.LOGGER.error("Failed getting mobs {}", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void OnTickInGame(Minecraft mc, LayoutVariables layoutVariables) {
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
		List<Entity> entities = this.game.world.getLoadedEntityList();

		for (Entity value : entities) {
			try {
				Entity entity = value;
				if (entity != null
					&& !entity.isInvisibleToPlayer(this.game.player)
					&& (
					this.options.showHostiles && (this.options.radarAllowed || this.options.radarMobsAllowed) && this.isHostile(entity)
						|| this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed) && this.isPlayer(entity)
						|| this.options.showNeutrals && this.options.radarMobsAllowed && this.isNeutral(entity)
				)) {
					int wayX = GameVariableAccessShim.xCoord() - (int) entity.posX;
					int wayZ = GameVariableAccessShim.zCoord() - (int) entity.posZ;
					int wayY = GameVariableAccessShim.yCoord() - (int) entity.posY;
					double hypot = wayX * wayX + wayZ * wayZ + wayY * wayY;
					hypot /= this.layoutVariables.zoomScaleAdjusted * this.layoutVariables.zoomScaleAdjusted;
					if (hypot < 961.0) {
						Contact contact = new Contact(entity, this.getUnknownMobNeutrality(entity));
						String unscrubbedName = contact.entity.getDisplayName().getFormattedText();
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
				VoxelMapMod.LOGGER.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		this.contacts.sort(Comparator.comparingInt(contact -> contact.y));
	}

	private EnumMobs getUnknownMobNeutrality(Entity entity) {
		if (this.isHostile(entity)) {
			return EnumMobs.GENERICHOSTILE;
		} else {
			return !(entity instanceof EntityTameable)
				|| !((EntityTameable) entity).isTamed()
				|| !this.game.isSingleplayer() && !((EntityTameable) entity).getOwner().equals(this.game.player)
				? EnumMobs.GENERICNEUTRAL
				: EnumMobs.GENERICTAME;
		}
	}

	private boolean isHostile(Entity entity) {
		if (entity instanceof EntityPigZombie) {
			return ((EntityPigZombie) entity).isAngry();
		}

		if (entity instanceof IMob) {
			return true;
		}

		if (entity instanceof EntityPolarBear) {
			for (Object object : ((EntityPolarBear) entity)
				.world
				.getEntitiesWithinAABB(EntityPolarBear.class, entity.getEntityBoundingBox().expand(8.0, 4.0, 8.0))) {
				if (((EntityPolarBear) object).isChild()) {
					return true;
				}
			}
		}

		if (entity instanceof EntityRabbit) {
			return ((EntityRabbit) entity).getRabbitType() == 99;
		} else {
			return entity instanceof EntityWolf && ((EntityWolf) entity).isAngry();
		}
	}

	private boolean isPlayer(Entity entity) {
		return entity instanceof EntityOtherPlayerMP;
	}

	private boolean isNeutral(Entity entity) {
		return entity instanceof EntityLiving && !(entity instanceof EntityPlayer) && !this.isHostile(entity);
	}

	public void renderMapMobs(int x, int y) {
		double max = this.layoutVariables.zoomScaleAdjusted * 32.0;
		GLUtils.disp(this.textureAtlas.getGlTextureId());

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

			boolean inRange;
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
					float contactFacing = contact.entity.getRotationYawHead();
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
					VoxelMapMod.LOGGER.error("Error rendering mob icon! {} contact type {}", localException.getLocalizedMessage(), contact.type);
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
