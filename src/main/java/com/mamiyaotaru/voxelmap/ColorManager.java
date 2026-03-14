package com.mamiyaotaru.voxelmap;

import com.google.common.collect.UnmodifiableIterator;
import com.mamiyaotaru.voxelmap.ornithe.Share;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.interfaces.IColorManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.block.*;
import net.minecraft.block.BlockDoublePlant.EnumPlantType;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.util.vector.Vector3f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ColorManager implements IColorManager {
	private final int BIOME_ARRAY_HEIGHT = 32;
	private final int WORLD_HEIGHT = 256;
	private final int BIOME_ARRAY_HEIGHT_MULTIPLIER = 8;
	private final int COLOR_NOT_LOADED = -16842497;
	private final int COLOR_FAILED_LOAD = 452984832;
	private final Object tpLoadLock = new Object();
	private final MutableBlockPos dummyBlockPos = new MutableBlockPos(
		BlockPos.ORIGIN.getX(), BlockPos.ORIGIN.getY(), BlockPos.ORIGIN.getZ()
	);
	private final ColorManager.ColorResolver pineColorResolver = new ColorManager.ColorResolver() {
		@Override
		public int getColorAtPos(Biome biome, BlockPos blockPos) {
			return ColorizerFoliage.getFoliageColorPine();
		}
	};
	private final ColorManager.ColorResolver birchColorResolver = new ColorManager.ColorResolver() {
		@Override
		public int getColorAtPos(Biome biome, BlockPos blockPos) {
			return ColorizerFoliage.getFoliageColorBirch();
		}
	};
	private final ColorManager.ColorResolver grassColorResolver = new ColorManager.ColorResolver() {
		@Override
		public int getColorAtPos(Biome biome, BlockPos blockPos) {
			return biome.getGrassColorAtPos(blockPos);
		}
	};
	private final ColorManager.ColorResolver leavesColorResolver = new ColorManager.ColorResolver() {
		@Override
		public int getColorAtPos(Biome biome, BlockPos blockPos) {
			return biome.getFoliageColorAtPos(blockPos);
		}
	};
	private final ColorManager.ColorResolver waterColorResolver = new ColorManager.ColorResolver() {
		@Override
		public int getColorAtPos(Biome biome, BlockPos blockPos) {
			return biome.getWaterColor();
		}
	};
	Minecraft game = null;
	private final IVoxelMap master;
	private boolean resourcePacksChanged = false;
	private BufferedImage terrainBuff = null;
	private BufferedImage colorPicker;
	private int sizeOfBiomeArray = 0;
	private int[] blockColors = new int[16384];
	private int[] blockColorsWithDefaultTint = new int[16384];
	private final HashSet<Integer> biomeTintsAvailable = new HashSet<>();
	private boolean optifineInstalled = false;
	private final HashMap<Integer, int[][]> blockTintTables = new HashMap<>();
	private final HashSet<Integer> biomeTextureAvailable = new HashSet<>();
	private final HashMap<String, Integer> blockBiomeSpecificColors = new HashMap<>();
	private float failedToLoadX = 0.0F;
	private float failedToLoadY = 0.0F;
	private String renderPassThreeBlendMode;
	private boolean loaded = false;

	public ColorManager(IVoxelMap master) {
		this.master = master;
		this.game = Minecraft.getMinecraft();
		this.optifineInstalled = false;
		Field ofProfiler = null;

		try {
			ofProfiler = GameSettings.class.getDeclaredField("ofProfiler");
		} catch (SecurityException var9) {
		} catch (NoSuchFieldException var10) {
		} finally {
			if (ofProfiler != null) {
				this.optifineInstalled = true;
			}
		}

		for (Biome biome : Biome.REGISTRY) {
			int biomeID = Biome.REGISTRY.getIDForObject(biome);
			if (biomeID > this.sizeOfBiomeArray) {
				this.sizeOfBiomeArray = biomeID;
			}
		}

		this.sizeOfBiomeArray++;
		BlockRepository.loadDefaultStorageStateIDs();
		BlockRepository.loadBlockIDs();
	}

	private static void findResourcesDirectory(
		File base, String namespace, String directory, String suffix, boolean recursive, boolean directories, Collection<ResourceLocation> resources
	) {
		File subdirectory = new File(base, directory);
		String[] list = subdirectory.list();
		if (list != null) {
			String pathComponent = directory.equals("") ? "" : directory + "/";

			for (String s : list) {
				File entry = new File(subdirectory, s);
				String resourceName = pathComponent + s;
				if (entry.isDirectory()) {
					if (directories && s.endsWith(suffix)) {
						resources.add(new ResourceLocation(namespace, resourceName));
					}

					if (recursive) {
						findResourcesDirectory(base, namespace, pathComponent + s, suffix, recursive, directories, resources);
					}
				} else if (s.endsWith(suffix) && !directories) {
					resources.add(new ResourceLocation(namespace, resourceName));
				}
			}
		}
	}

	@Override
	public int getAirColor() {
		return this.blockColors[BlockRepository.airID];
	}

	@Override
	public BufferedImage getColorPicker() {
		return this.colorPicker;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.resourcePacksChanged = true;
	}

	@Override
	public boolean checkForChanges() {
		boolean changed = this.resourcePacksChanged;
		this.resourcePacksChanged = false;
		if (changed) {
			this.loadColors();
		}

		return changed;
	}

	public void loadColors() {
		this.game.player.getLocationSkin();
		BlockRepository.getBlocks();
		this.loadColorPicker();
		this.loadTexturePackTerrainImage();
		TextureAtlasSprite missing = this.game.getTextureMapBlocks().getAtlasSprite("missingno");
		this.failedToLoadX = missing.getMinU();
		this.failedToLoadY = missing.getMinV();
		this.loaded = false;

		try {
			Arrays.fill(this.blockColors, -16842497);
			Arrays.fill(this.blockColorsWithDefaultTint, -16842497);
			this.loadSpecialColors();
			this.biomeTintsAvailable.clear();
			this.biomeTextureAvailable.clear();
			this.blockBiomeSpecificColors.clear();
			this.blockTintTables.clear();
			if (this.optifineInstalled) {
				try {
					this.processCTM();
				} catch (Exception ex) {
					System.err.println("error loading CTM " + ex.getLocalizedMessage());
					ex.printStackTrace();
				}
			}

			try {
				this.loadWaterColor();
			} catch (Exception ex) {
				System.err.println("error getting water color " + ex.getLocalizedMessage());
			}

			if (this.optifineInstalled) {
				try {
					this.processColorProperties();
				} catch (Exception ex) {
					System.err.println("error loading custom color properties " + ex.getLocalizedMessage());
					ex.printStackTrace();
				}
			}

			this.master.getMap().forceFullRender(true);
		} catch (Exception e) {
			System.err.println("error loading pack");
			e.printStackTrace();
		}

		this.loaded = true;
	}

	@Override
	public final BufferedImage getBlockImage(IBlockState blockState, ItemStack stack, World world) {
		try {
			BlockRendererDispatcher blockRendererDispatcher = this.game.getBlockRendererDispatcher();
			BufferedImage blockImage;
			if (GLUtils.fboEnabled) {
				IBakedModel model = this.game.getRenderItem().getItemModelWithOverrides(stack, world, null);
				this.drawModel(1.0F, 2, EnumFacing.EAST, blockState, model, stack);
				blockImage = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
				blockImage = ImageUtils.trimCentered(blockImage);
			} else {
				BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
				TextureAtlasSprite icon = blockModelShapes.getTexture(blockState);
				int left = (int) (icon.getMinU() * this.terrainBuff.getWidth());
				int right = (int) Math.ceil(icon.getMaxU() * this.terrainBuff.getWidth());
				int top = (int) (icon.getMinV() * this.terrainBuff.getHeight());
				int bottom = (int) Math.ceil(icon.getMaxV() * this.terrainBuff.getHeight());
				blockImage = this.terrainBuff.getSubimage(left, top, right - left, bottom - top);
				float scale = blockImage.getWidth() / 16.0F;
				blockImage = ImageUtils.scaleImage(blockImage, 1.0F / scale);
			}

			return blockImage;
		} catch (Exception e) {
			System.out
				.println(
					"error getting block armor image for "
						+ Block.getIdFromBlock(blockState.getBlock())
						+ ","
						+ blockState.getBlock().getMetaFromState(blockState)
						+ ": "
						+ e.getLocalizedMessage()
				);
			e.printStackTrace();
			return null;
		}
	}

	private void drawModel(float scale, int captureDepth, EnumFacing facing, IBlockState blockState, IBakedModel model, ItemStack stack) {
		float size = 16.0F * scale;
		ItemCameraTransforms transforms = model.getItemCameraTransforms();
		ItemTransformVec3f headTransforms = transforms.head;
		Vector3f translations = headTransforms.translation;
		float transX = translations.x * size + 0.5F * size;
		float transY = translations.y * size + 0.5F * size;
		float transZ = translations.z * size + 0.5F * size;
		Vector3f rotations = headTransforms.rotation;
		float rotX = rotations.x;
		float rotY = rotations.y;
		float rotZ = rotations.z;
		GLShim.glBindTexture(3553, GLUtils.fboTextureID);
		int width = GLShim.glGetTexLevelParameteri(3553, 0, 4096);
		int height = GLShim.glGetTexLevelParameteri(3553, 0, 4097);
		GLShim.glBindTexture(3553, 0);
		GLShim.glPushAttrib(4096);
		GLShim.glViewport(0, 0, width, height);
		GLShim.glMatrixMode(5889);
		GLShim.glPushMatrix();
		GLShim.glLoadIdentity();
		GLShim.glOrtho(0.0, width, height, 0.0, 1000.0, 3000.0);
		GLShim.glMatrixMode(5888);
		GLShim.glPushMatrix();
		GLShim.glLoadIdentity();
		GLShim.glTranslatef(0.0F, 0.0F, -3000.0F - size / 4.0F);
		GLUtils.bindFrameBuffer();
		GLShim.glDepthMask(true);
		GLShim.glEnable(2929);
		GLShim.glEnable(3553);
		GLShim.glEnable(3042);
		GLShim.glEnable(3008);
		GLShim.glEnable(2977);
		GLShim.glDisable(2884);
		GLShim.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		GLShim.glClear(16640);
		GLShim.glBlendFunc(770, 771);
		GLShim.glPushMatrix();
		GLShim.glTranslatef(width / 2 - size / 2.0F + transX, height / 2 - size / 2.0F + transY, 0.0F + transZ);
		GLShim.glScalef(size, size, size);
		GLUtils.img(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GLShim.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
		GLShim.glRotatef(rotY, 0.0F, 1.0F, 0.0F);
		GLShim.glRotatef(rotX, 1.0F, 0.0F, 0.0F);
		GLShim.glRotatef(rotZ, 0.0F, 0.0F, 1.0F);
		if (facing == EnumFacing.UP) {
			GLShim.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		}

		Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		GLShim.glPopMatrix();
		GLShim.glEnable(2884);
		GLShim.glDisable(2929);
		GLShim.glDepthMask(false);
		GLUtils.unbindFrameBuffer();
		GLShim.glMatrixMode(5889);
		GLShim.glPopMatrix();
		GLShim.glMatrixMode(5888);
		GLShim.glPopMatrix();
		GLShim.glPopAttrib();
		GLShim.glViewport(0, 0, this.game.displayWidth, this.game.displayHeight);
	}

	private void loadColorPicker() {
		try {
			InputStream is = this.game.getResourceManager().getResource(new ResourceLocation("voxelmap", "images/colorpicker.png")).getInputStream();
			Image picker = ImageIO.read(is);
			is.close();
			this.colorPicker = new BufferedImage(picker.getWidth(null), picker.getHeight(null), 2);
			Graphics gfx = this.colorPicker.createGraphics();
			gfx.drawImage(picker, 0, 0, null);
			gfx.dispose();
		} catch (Exception e) {
			System.err.println("Error loading color picker: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void setSkyColor(int skyColor) {
		this.blockColors[BlockRepository.airID] = skyColor;
	}

	private void loadTexturePackTerrainImage() {
		try {
			TextureManager textureManager = this.game.getTextureManager();
			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			BufferedImage terrainStitched = ImageUtils.createBufferedImageFromCurrentGLImage();
			this.terrainBuff = new BufferedImage(terrainStitched.getWidth(null), terrainStitched.getHeight(null), 6);
			Graphics gfx = this.terrainBuff.createGraphics();
			gfx.drawImage(terrainStitched, 0, 0, null);
			gfx.dispose();
		} catch (Exception e) {
			System.err.println("Error processing new resource pack: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private void loadSpecialColors() {
		Iterator<IBlockState> blockStateIterator = BlockRepository.pistonTechBlock.getBlockState().getValidStates().iterator();

		while (blockStateIterator.hasNext()) {
			IBlockState blockState = blockStateIterator.next();
			int blockStateID = BlockRepository.getStateId(blockState);
			this.blockColors[blockStateID] = 0;
		}

		Iterator<IBlockState> var12 = BlockRepository.barrier.getBlockState().getValidStates().iterator();

		while (var12.hasNext()) {
			IBlockState blockState = var12.next();
			int blockStateID = BlockRepository.getStateId(blockState);
			this.blockColors[blockStateID] = 0;
		}

		try {
			Field opacity1 = ReflectionUtils.getFieldByType(BlockRepository.lava, Block.class, int.class);
			opacity1.set(BlockRepository.lava, 1);
			Field opacity2 = ReflectionUtils.getFieldByType(BlockRepository.flowingLava, Block.class, int.class);
			opacity2.set(BlockRepository.flowingLava, 1);
			Field material1 = ReflectionUtils.getFieldByType(BlockRepository.chorusPlant, Block.class, Material.class);
			material1.set(BlockRepository.chorusPlant, Material.WOOD);
			Field material2 = ReflectionUtils.getFieldByType(BlockRepository.chorusFlower, Block.class, Material.class);
			material2.set(BlockRepository.chorusFlower, Material.WOOD);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}

	private void loadWaterColor() {
		int waterRGB = -1;
		IBlockState blockState = BlockRepository.water.getDefaultState();
		int blockStateID = BlockRepository.getStateId(blockState);
		waterRGB = this.getBlockColor(blockStateID);
		int waterMult = -1;
		if (this.optifineInstalled) {
			InputStream is = null;

			try {
				is = this.game.getResourceManager().getResource(new ResourceLocation("mcpatcher/colormap/water.png")).getInputStream();
			} catch (IOException e) {
				is = null;
			}

			if (is != null) {
				try {
					Image waterColor = ImageIO.read(is);
					is.close();
					BufferedImage waterColorBuff = new BufferedImage(waterColor.getWidth(null), waterColor.getHeight(null), 1);
					Graphics gfx = waterColorBuff.createGraphics();
					gfx.drawImage(waterColor, 0, 0, null);
					gfx.dispose();
					Biome biome = Biomes.FOREST;
					double var1 = MathHelper.clamp(biome.getTemperature(new BlockPos(0, 64, 0)), 0.0F, 1.0F);
					double var2 = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
					var2 *= var1;
					var1 = 1.0 - var1;
					var2 = 1.0 - var2;
					waterMult = waterColorBuff.getRGB((int) ((waterColorBuff.getWidth() - 1) * var1), (int) ((waterColorBuff.getHeight() - 1) * var2)) & 16777215;
				} catch (Exception var14) {
				}
			}
		}

		if (waterMult != -1 && waterMult != 0) {
			waterRGB = this.colorMultiplier(waterRGB, waterMult | 0xFF000000);
		} else {
			waterRGB = this.colorMultiplier(waterRGB, Biomes.FOREST.getWaterColor() | 0xFF000000);
		}

		for (int t = 0; t < 16; t++) {
			blockState = BlockRepository.water.getDefaultState().withProperty(BlockLiquid.LEVEL, t);
			blockStateID = BlockRepository.getStateId(blockState);
			this.blockColorsWithDefaultTint[blockStateID] = waterRGB;
			blockState = BlockRepository.water.getDefaultState().withProperty(BlockLiquid.LEVEL, t);
			blockStateID = BlockRepository.getStateId(blockState);
			this.blockColorsWithDefaultTint[blockStateID] = waterRGB;
		}
	}

	@Override
	public final int getBlockColorWithDefaultTint(MutableBlockPos blockPos, int blockStateID) {
		if (this.loaded) {
			int col = 452984832;

			try {
				col = this.blockColorsWithDefaultTint[blockStateID];
			} catch (ArrayIndexOutOfBoundsException var5) {
			}

			return col != -16842497 && col != 452984832 ? col : this.getBlockColor(blockPos, blockStateID);
		} else {
			return 0;
		}
	}

	@Override
	public final int getBlockColor(MutableBlockPos blockPos, int blockStateID, int biomeID) {
		if (this.loaded) {
			if (this.optifineInstalled && this.biomeTextureAvailable.contains(blockStateID)) {
				Integer col = this.blockBiomeSpecificColors.get(blockStateID + " " + biomeID);
				if (col != null) {
					return col;
				}
			}

			return this.getBlockColor(blockPos, blockStateID);
		} else {
			return 0;
		}
	}

	private int getBlockColor(int blockStateID) {
		return this.getBlockColor(this.dummyBlockPos, blockStateID);
	}

	private final int getBlockColor(MutableBlockPos blockPos, int blockStateID) {
		int col = 452984832;

		try {
			col = this.blockColors[blockStateID];
		} catch (ArrayIndexOutOfBoundsException e) {
			this.resizeColorArrays(blockStateID);
		}

		if (col == -16842497 || col == 452984832) {
			IBlockState blockState = BlockRepository.getStateById(blockStateID);
			col = this.blockColors[blockStateID] = this.getColor(blockPos, blockState);
		}

		return col;
	}

	private synchronized void resizeColorArrays(int queriedID) {
		if (queriedID >= this.blockColors.length) {
			int[] newBlockColors = new int[this.blockColors.length * 2];
			int[] newBlockColorsWithDefaultTint = new int[this.blockColors.length * 2];
			System.arraycopy(this.blockColors, 0, newBlockColors, 0, this.blockColors.length);
			System.arraycopy(this.blockColorsWithDefaultTint, 0, newBlockColorsWithDefaultTint, 0, this.blockColorsWithDefaultTint.length);
			Arrays.fill(newBlockColors, this.blockColors.length, newBlockColors.length, -16842497);
			Arrays.fill(newBlockColorsWithDefaultTint, this.blockColorsWithDefaultTint.length, newBlockColorsWithDefaultTint.length, -16842497);
			this.blockColors = newBlockColors;
			this.blockColorsWithDefaultTint = newBlockColorsWithDefaultTint;
		}
	}

	private int getColor(MutableBlockPos blockPos, IBlockState blockState) {
		try {
			int color = this.getColorForBlockPosBlockStateAndFacing(blockPos, blockState, EnumFacing.UP);
			if (color == 452984832) {
				BlockRendererDispatcher blockRendererDispatcher = this.game.getBlockRendererDispatcher();
				color = this.getColorForTerrainSprite(blockState, blockRendererDispatcher);
			}

			Block block = blockState.getBlock();
			if (block == BlockRepository.cobweb) {
				color |= -16777216;
			}

			if (block == BlockRepository.redstone) {
				this.blockColorsWithDefaultTint[BlockRepository.getStateId(blockState)] = this.colorMultiplier(
					color, this.game.getBlockColors().colorMultiplier(blockState, this.game.world, blockPos, 1) | 0xFF000000
				);
			}

			if (BlockRepository.biomeBlocks.contains(block)) {
				this.applyBuiltInShading(blockState, color);
			} else {
				this.checkForBiomeTinting(blockPos, blockState, color);
			}

			if (BlockRepository.shapedBlocks.contains(block)) {
				color = this.applyShape(block, color);
			}

			if ((color >> 24 & 0xFF) < 27) {
				color |= 452984832;
			}

			return color;
		} catch (Exception e) {
			System.err.println("failed getting color: " + blockState.getBlock().getLocalizedName());
			e.printStackTrace();
			return 452984832;
		}
	}

	private int getColorForBlockPosBlockStateAndFacing(BlockPos blockPos, IBlockState blockState, EnumFacing facing) {
		int color = 452984832;

		try {
			EnumBlockRenderType blockRenderType = blockState.getRenderType();
			BlockRendererDispatcher blockRendererDispatcher = this.game.getBlockRendererDispatcher();
			if (blockRenderType == EnumBlockRenderType.MODEL) {
				try {
					blockState = blockState.getActualState(this.game.world, blockPos);
				} catch (Exception var11) {
				}

				IBakedModel iBakedModel = blockRendererDispatcher.getModelForState(blockState);
				List<BakedQuad> quads = new ArrayList<>();
				quads.addAll(iBakedModel.getQuads(blockState, facing, 0L));
				quads.addAll(iBakedModel.getQuads(blockState, null, 0L));
				BlockModel model = new BlockModel(quads);
				model.setFailedToLoadCoords(this.failedToLoadX, this.failedToLoadY);
				if (model.numberOfFaces() > 0) {
					BufferedImage modelImage = model.getImage(this.terrainBuff);
					if (modelImage != null) {
						color = this.getColorForCoordinatesAndImage(new float[]{0.0F, 1.0F, 0.0F, 1.0F}, modelImage);
					}
				}
			} else if (blockRenderType == EnumBlockRenderType.LIQUID) {
				color = this.getColorForTerrainSprite(blockState, blockRendererDispatcher);
			}
		} catch (Exception e) {
			color = 452984832;
		}

		return color;
	}

	private int getColorForTerrainSprite(IBlockState blockState, BlockRendererDispatcher blockRendererDispatcher) {
		int color = 452984832;
		BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
		TextureAtlasSprite icon = blockModelShapes.getTexture(blockState);
		if (icon == blockModelShapes.getModelManager().getMissingModel().getParticleTexture()) {
			TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
			Block block = blockState.getBlock();
			Material material = blockState.getMaterial();
			if (block instanceof BlockDynamicLiquid) {
				if (material == Material.WATER) {
					icon = textureMap.getAtlasSprite("minecraft:blocks/water_flow");
				} else if (material == Material.LAVA) {
					icon = textureMap.getAtlasSprite("minecraft:blocks/lava_flow");
				}
			} else if (!(block instanceof BlockStaticLiquid) && !(block instanceof BlockLiquid)) {
				if (material == Material.WATER) {
					icon = textureMap.getAtlasSprite("minecraft:blocks/water_still");
				} else if (material == Material.LAVA) {
					icon = textureMap.getAtlasSprite("minecraft:blocks/lava_still");
				}
			} else if (material == Material.WATER) {
				icon = textureMap.getAtlasSprite("minecraft:blocks/water_still");
			} else if (material == Material.LAVA) {
				icon = textureMap.getAtlasSprite("minecraft:blocks/lava_still");
			}
		}

		return this.getColorForIcon(icon);
	}

	private int getColorForIcon(TextureAtlasSprite icon) {
		int color = 452984832;
		if (icon != null) {
			float left = icon.getMinU();
			float right = icon.getMaxU();
			float top = icon.getMinV();
			float bottom = icon.getMaxV();
			color = this.getColorForCoordinatesAndImage(new float[]{left, right, top, bottom}, this.terrainBuff);
		}

		return color;
	}

	private int getColorForCoordinatesAndImage(float[] uv, BufferedImage imageBuff) {
		int color = 452984832;
		if (uv[0] != this.failedToLoadX || uv[2] != this.failedToLoadY) {
			int left = (int) (uv[0] * imageBuff.getWidth());
			int right = (int) Math.ceil(uv[1] * imageBuff.getWidth());
			int top = (int) (uv[2] * imageBuff.getHeight());
			int bottom = (int) Math.ceil(uv[3] * imageBuff.getHeight());

			try {
				BufferedImage blockTexture = imageBuff.getSubimage(left, top, right - left, bottom - top);
				Image singlePixel = blockTexture.getScaledInstance(1, 1, 4);
				BufferedImage singlePixelBuff = new BufferedImage(1, 1, imageBuff.getType());
				Graphics gfx = singlePixelBuff.createGraphics();
				gfx.drawImage(singlePixel, 0, 0, null);
				gfx.dispose();
				color = singlePixelBuff.getRGB(0, 0);
			} catch (RasterFormatException e) {
				System.out.println("error getting color");
				System.out.println(left + " " + right + " " + top + " " + bottom);
				color = 452984832;
			}
		}

		return color;
	}

	private void applyBuiltInShading(IBlockState blockState, int color) {
		Block block = blockState.getBlock();
		int blockStateID = BlockRepository.getStateId(blockState);
		if (block == BlockRepository.tallFlower) {
			EnumPlantType type = blockState.getValue(BlockDoublePlant.VARIANT);
			switch (type) {
				case FERN:
					this.blockColorsWithDefaultTint[blockStateID] = this.colorMultiplier(color, ColorizerGrass.getGrassColor(0.7, 0.8) | 0xFF000000);
					break;
				case GRASS:
					this.blockColorsWithDefaultTint[blockStateID] = this.colorMultiplier(color, ColorizerGrass.getGrassColor(0.7, 0.8) | 0xFF000000);
			}
		} else if (block == BlockRepository.reeds) {
			this.blockColorsWithDefaultTint[blockStateID] = this.colorMultiplier(color, ColorizerGrass.getGrassColor(0.7, 0.8) | 0xFF000000);
		} else {
			this.blockColorsWithDefaultTint[blockStateID] = this.colorMultiplier(
				color, this.game.getBlockColors().colorMultiplier(blockState, null, null, 0) | 0xFF000000
			);
		}
	}

	private void checkForBiomeTinting(MutableBlockPos blockPos, IBlockState blockState, int color) {
		Block block = blockState.getBlock();
		String blockName = "" + Block.REGISTRY.getNameForObject(block);
		if (!BlockRepository.biomeBlocks.contains(block) && !blockName.startsWith("minecraft:")) {
			int tint = -1;
			MutableBlockPos tempBlockPos = new MutableBlockPos(0, 0, 0);
			if (blockPos == this.dummyBlockPos) {
				tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, (byte) 4);
			} else {
				Chunk chunk = this.game.world.getChunk(blockPos);
				if (chunk.isLoaded()) {
					tint = this.game.getBlockColors().colorMultiplier(blockState, this.game.world, blockPos, 1) | 0xFF000000;
				} else {
					tint = this.tintFromFakePlacedBlock(blockState, tempBlockPos, (byte) 4);
				}
			}

			if (tint != 16777215 && tint != -1) {
				int blockStateID = BlockRepository.getStateId(blockState);
				this.biomeTintsAvailable.add(blockStateID);
				this.blockColorsWithDefaultTint[blockStateID] = this.colorMultiplier(color, tint);
				this.createTintTable(blockState, tempBlockPos);
			} else {
				this.blockColorsWithDefaultTint[BlockRepository.getStateId(blockState)] = 452984832;
			}
		}
	}

	private int tintFromFakePlacedBlock(IBlockState blockState, MutableBlockPos loopBlockPos, byte biomeID) {
		World world = this.game.world;
		if (world == null) {
			return -1;
		}

		if (blockState.getBlock() == null) {
			return -1;
		}

		Share.updateCloudsLock.lock();
		int tint = -1;

		try {
			int fakeX = (int) this.game.player.posX - 32;
			int fakeZ = (int) this.game.player.posZ - 32;
			byte[] fakeBiome = new byte[256];
			Arrays.fill(fakeBiome, biomeID);
			Chunk chunk = world.getChunk(loopBlockPos.withXYZ(fakeX, 0, fakeZ));
			byte[] actualBiomes = new byte[256];
			System.arraycopy(chunk.getBiomeArray(), 0, actualBiomes, 0, 256);
			IBlockState actualBlockState = world.getBlockState(loopBlockPos);
			chunk.setBlockState(loopBlockPos, blockState);
			chunk.setBiomeArray(fakeBiome);
			tint = this.game.getBlockColors().colorMultiplier(blockState, world, loopBlockPos, 1) | 0xFF000000;
			chunk.setBiomeArray(actualBiomes);
			chunk.setBlockState(loopBlockPos, actualBlockState);
		} catch (Exception var15) {
		} finally {
			Share.updateCloudsLock.unlock();
		}

		return tint;
	}

	private void createTintTable(IBlockState blockState, MutableBlockPos loopBlockPos) {
		World world = this.game.world;
		if (world != null) {
			Block block = blockState.getBlock();
			if (block != null) {
				Share.updateCloudsLock.lock();

				try {
					int[][] tints = new int[this.sizeOfBiomeArray][32];

					for (int[] row : tints) {
						Arrays.fill(row, -1);
					}

					int fakeX = (int) this.game.player.posX - 32;
					int fakeZ = (int) this.game.player.posZ - 32;
					Chunk chunk = world.getChunk(loopBlockPos.withXYZ(fakeX, 0, fakeZ));
					byte[] originalBiomes = new byte[256];
					System.arraycopy(chunk.getBiomeArray(), 0, originalBiomes, 0, 256);
					IBlockState actualBlockState = world.getBlockState(loopBlockPos);
					chunk.setBlockState(loopBlockPos, blockState);
					byte[] fakeBiome = new byte[256];

					for (int biomeID = 0; biomeID < this.sizeOfBiomeArray; biomeID++) {
						if (Biome.getBiome(biomeID) != null) {
							int[] row = new int[32];
							Arrays.fill(fakeBiome, (byte) biomeID);
							chunk.setBiomeArray(fakeBiome);
							Arrays.fill(row, this.game.getBlockColors().colorMultiplier(blockState, world, loopBlockPos, 1) | 0xFF000000);
							tints[biomeID] = row;
						}
					}

					chunk.setBiomeArray(originalBiomes);
					chunk.setBlockState(loopBlockPos, actualBlockState);
					int blockStateID = BlockRepository.getStateId(blockState);
					this.blockTintTables.put(blockStateID, tints);
				} catch (Exception var17) {
				} finally {
					Share.updateCloudsLock.unlock();
				}
			}
		}
	}

	@Override
	public int getBiomeTint(
		AbstractMapData mapData,
		World world,
		IBlockState blockState,
		int blockStateID,
		MutableBlockPos blockPos,
		MutableBlockPos loopBlockPos,
		int startX,
		int startZ
	) {
		Chunk chunk = world.getChunk(blockPos);
		boolean live = chunk.isLoaded();
		int tint = -2;
		if (this.optifineInstalled || !live && this.biomeTintsAvailable.contains(blockStateID)) {
			try {
				int[][] tints = this.blockTintTables.get(blockStateID);
				if (tints != null) {
					int r = 0;
					int g = 0;
					int b = 0;

					for (int t = blockPos.getX() - 1; t <= blockPos.getX() + 1; t++) {
						for (int s = blockPos.getZ() - 1; s <= blockPos.getZ() + 1; s++) {
							int biomeID = 0;
							if (live) {
								biomeID = Biome.getIdForBiome(world.getBiome(loopBlockPos.withXYZ(t, blockPos.getY(), s)));
							} else {
								int dataX = t - startX;
								int dataZ = s - startZ;
								dataX = Math.max(dataX, 0);
								dataX = Math.min(dataX, mapData.getWidth() - 1);
								dataZ = Math.max(dataZ, 0);
								dataZ = Math.min(dataZ, mapData.getHeight() - 1);
								biomeID = mapData.getBiomeID(dataX, dataZ);
								if (biomeID == -1) {
									biomeID = 1;
								}
							}

							int biomeTint = tints[biomeID][loopBlockPos.y / 8];
							r += (biomeTint & 0xFF0000) >> 16;
							g += (biomeTint & 0xFF00) >> 8;
							b += biomeTint & 0xFF;
						}
					}

					tint = 0xFF000000 | (r / 9 & 0xFF) << 16 | (g / 9 & 0xFF) << 8 | b / 9 & 0xFF;
				}
			} catch (Exception e) {
				tint = -2;
			}
		}

		if (tint == -2) {
			tint = this.getBuiltInBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ, live);
		}

		return tint;
	}

	private int getBuiltInBiomeTint(
		AbstractMapData mapData,
		World world,
		IBlockState blockState,
		int blockStateID,
		MutableBlockPos blockPos,
		MutableBlockPos loopBlockPos,
		int startX,
		int startZ,
		boolean live
	) {
		int tint = -1;
		Block block = blockState.getBlock();
		if (block == BlockRepository.redstone || BlockRepository.biomeBlocks.contains(block) || this.biomeTintsAvailable.contains(blockStateID)) {
			if (!live) {
				tint = this.getBuiltInBiomeTintFromUnloadedChunk(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ) | 0xFF000000;
			} else {
				tint = this.game.getBlockColors().colorMultiplier(blockState, world, blockPos, 1) | 0xFF000000;
			}
		}

		return tint;
	}

	private int getBuiltInBiomeTintFromUnloadedChunk(
		AbstractMapData mapData,
		World world,
		IBlockState blockState,
		int blockStateID,
		MutableBlockPos blockPos,
		MutableBlockPos loopBlockPos,
		int startX,
		int startZ
	) {
		int tint = -1;
		Block block = blockState.getBlock();
		ColorManager.ColorResolver colorResolver = null;
		if (block == BlockRepository.water || block == BlockRepository.flowingWater) {
			colorResolver = this.waterColorResolver;
		} else if (block == BlockRepository.leaves || block == BlockRepository.leaves2) {
			PropertyEnum<EnumType> propertyEnum = block == BlockRepository.leaves ? BlockOldLeaf.VARIANT : BlockNewLeaf.VARIANT;
			EnumType type = blockState.getValue(propertyEnum);
			if (type == EnumType.SPRUCE) {
				colorResolver = this.pineColorResolver;
			} else if (type == EnumType.BIRCH) {
				colorResolver = this.birchColorResolver;
			} else {
				colorResolver = this.leavesColorResolver;
			}
		} else if (BlockRepository.biomeBlocks.contains(block)) {
			colorResolver = this.grassColorResolver;
		}

		if (colorResolver != null) {
			int var3x = 0;
			int var4x = 0;
			int var5x = 0;

			for (int t = blockPos.getX() - 1; t <= blockPos.getX() + 1; t++) {
				for (int s = blockPos.getZ() - 1; s <= blockPos.getZ() + 1; s++) {
					int dataX = t - startX;
					int dataZ = s - startZ;
					dataX = Math.max(dataX, 0);
					dataX = Math.min(dataX, 255);
					dataZ = Math.max(dataZ, 0);
					dataZ = Math.min(dataZ, 255);
					int biomeID = mapData.getBiomeID(dataX, dataZ);
					Biome biome = Biome.getBiome(biomeID);
					if (biome == null) {
						MessageUtils.printDebug("Null biome ID! " + biomeID + " at " + t + "," + s);
						MessageUtils.printDebug("block: " + mapData.getBlockstate(dataX, dataZ) + ", height: " + mapData.getHeight(dataX, dataZ));
						MessageUtils.printDebug("Mapdata: " + mapData);
						biome = Biomes.FOREST;
					}

					int var8x = colorResolver.getColorAtPos(biome, loopBlockPos.withXYZ(t, blockPos.getY(), s));
					var3x += (var8x & 0xFF0000) >> 16;
					var4x += (var8x & 0xFF00) >> 8;
					var5x += var8x & 0xFF;
				}
			}

			tint = (var3x / 9 & 0xFF) << 16 | (var4x / 9 & 0xFF) << 8 | var5x / 9 & 0xFF;
		} else if (this.biomeTintsAvailable.contains(blockStateID)) {
			tint = this.getCustomBlockBiomeTintFromUnloadedChunk(mapData, world, blockState, blockPos, loopBlockPos, startX, startZ);
		}

		return tint;
	}

	private int getCustomBlockBiomeTintFromUnloadedChunk(
		AbstractMapData mapData, World world, IBlockState blockState, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ
	) {
		int tint = -1;

		try {
			int dataX = blockPos.getX() - startX;
			int dataZ = blockPos.getZ() - startZ;
			dataX = Math.max(dataX, 0);
			dataX = Math.min(dataX, mapData.getWidth() - 1);
			dataZ = Math.max(dataZ, 0);
			dataZ = Math.min(dataZ, mapData.getHeight() - 1);
			byte biomeID = (byte) mapData.getBiomeID(dataX, dataZ);
			tint = this.tintFromFakePlacedBlock(blockState, loopBlockPos, biomeID);
		} catch (Exception e) {
			tint = -1;
		}

		return tint;
	}

	private int applyShape(Block block, int color) {
		int alpha = color >> 24 & 0xFF;
		int red = color >> 16 & 0xFF;
		int green = color >> 8 & 0xFF;
		int blue = color >> 0 & 0xFF;
		if (block != BlockRepository.sign && block != BlockRepository.wallSign) {
			if (Arrays.asList(BlockRepository.doorsArray).contains(block)) {
				alpha = 47;
			} else if (block == BlockRepository.ladder || block == BlockRepository.vine) {
				alpha = 15;
			}
		} else {
			alpha = 31;
		}

		return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
	}

	@Override
	public int colorMultiplier(int color1, int color2) {
		int alpha1 = color1 >> 24 & 0xFF;
		int red1 = color1 >> 16 & 0xFF;
		int green1 = color1 >> 8 & 0xFF;
		int blue1 = color1 >> 0 & 0xFF;
		int alpha2 = color2 >> 24 & 0xFF;
		int red2 = color2 >> 16 & 0xFF;
		int green2 = color2 >> 8 & 0xFF;
		int blue2 = color2 >> 0 & 0xFF;
		int alpha = alpha1 * alpha2 / 255;
		int red = red1 * red2 / 255;
		int green = green1 * green2 / 255;
		int blue = blue1 * blue2 / 255;
		return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
	}

	@Override
	public int colorAdder(int color1, int color2) {
		float topAlpha = (color1 >> 24 & 0xFF) / 255.0F;
		float red1 = (color1 >> 16 & 0xFF) * topAlpha;
		float green1 = (color1 >> 8 & 0xFF) * topAlpha;
		float blue1 = (color1 >> 0 & 0xFF) * topAlpha;
		float bottomAlpha = (color2 >> 24 & 0xFF) / 255.0F;
		float red2 = (color2 >> 16 & 0xFF) * bottomAlpha * (1.0F - topAlpha);
		float green2 = (color2 >> 8 & 0xFF) * bottomAlpha * (1.0F - topAlpha);
		float blue2 = (color2 >> 0 & 0xFF) * bottomAlpha * (1.0F - topAlpha);
		float alpha = topAlpha + bottomAlpha * (1.0F - topAlpha);
		float red = (red1 + red2) / alpha;
		float green = (green1 + green2) / alpha;
		float blue = (blue1 + blue2) / alpha;
		return ((int) (alpha * 255.0F) & 0xFF) << 24 | ((int) red & 0xFF) << 16 | ((int) green & 0xFF) << 8 | (int) blue & 0xFF;
	}

	private void processCTM() {
		this.renderPassThreeBlendMode = "alpha";
		Properties properties = new Properties();
		ResourceLocation propertiesFile = new ResourceLocation("minecraft", "mcpatcher/renderpass.properties");

		try {
			InputStream input = this.game.getResourceManager().getResource(propertiesFile).getInputStream();
			if (input != null) {
				properties.load(input);
				input.close();
				this.renderPassThreeBlendMode = properties.getProperty("blend.3", "alpha");
			}
		} catch (IOException e) {
			this.renderPassThreeBlendMode = "alpha";
		}

		String namespace = "minecraft";

		for (ResourceLocation s : this.findResources(namespace, "/mcpatcher/ctm", ".properties", true, false, true)) {
			try {
				this.loadCTM(s);
			} catch (NumberFormatException var7) {
			} catch (IllegalArgumentException var8) {
			}
		}

		for (int t = 0; t < this.blockColors.length; t++) {
			if (this.blockColors[t] != 452984832 && this.blockColors[t] != -16842497) {
				if ((this.blockColors[t] >> 24 & 0xFF) < 27) {
					this.blockColors[t] = this.blockColors[t] | 452984832;
				}

				this.checkForBiomeTinting(this.dummyBlockPos, BlockRepository.getStateById(t), this.blockColors[t]);
			}
		}
	}

	private void loadCTM(ResourceLocation propertiesFile) {
		if (propertiesFile != null) {
			BlockRendererDispatcher blockRendererDispatcher = this.game.getBlockRendererDispatcher();
			BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
			Properties properties = new Properties();

			try {
				InputStream input = this.game.getResourceManager().getResource(propertiesFile).getInputStream();
				if (input != null) {
					properties.load(input);
					input.close();
				}
			} catch (IOException e) {
				return;
			}

			String filePath = propertiesFile.getPath();
			String method = properties.getProperty("method", "").trim().toLowerCase();
			String faces = properties.getProperty("faces", "").trim().toLowerCase();
			String matchBlocks = properties.getProperty("matchBlocks", "").trim().toLowerCase();
			String matchTiles = properties.getProperty("matchTiles", "").trim().toLowerCase();
			String metadata = properties.getProperty("metadata", "").trim().toLowerCase();
			String tiles = properties.getProperty("tiles", "").trim();
			String biomes = properties.getProperty("biomes", "").trim().toLowerCase();
			String renderPass = properties.getProperty("renderPass", "").trim().toLowerCase();
			metadata = metadata.replaceAll("\\s+", ",");
			Set<IBlockState> blockStates = new HashSet<>();
			blockStates.addAll(this.parseBlocksList(matchBlocks, metadata));
			String directory = filePath.substring(0, filePath.lastIndexOf("/") + 1);
			String[] tilesParsed = this.parseStringList(tiles);
			String tilePath = directory + "0";
			if (tilesParsed.length > 0) {
				tilePath = tilesParsed[0].trim();
			}

			if (tilePath.startsWith("~")) {
				tilePath = tilePath.replace("~", "mcpatcher");
			} else if (!tilePath.contains("/")) {
				tilePath = directory + tilePath;
			}

			if (!tilePath.toLowerCase().endsWith(".png")) {
				tilePath = tilePath + ".png";
			}

			String[] biomesArray = biomes.split(" ");
			if (blockStates.size() == 0) {
				Block block = null;
				Pattern pattern = Pattern.compile(".*/block([\\d]+)[a-zA-Z_]*.properties");
				Matcher matcher = pattern.matcher(filePath);
				if (matcher.find()) {
					block = this.getBlockWithNameOrID(matcher.group(1));
					if (block != null) {
						Set<IBlockState> matching = this.parseBlockMetadata(block, metadata);
						if (matching.size() == 0) {
							matching.addAll(block.getBlockState().getValidStates());
						}

						blockStates.addAll(matching);
					}
				} else {
					if (matchTiles.equals("")) {
						matchTiles = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".properties"));
					}

					if (!matchTiles.contains(":")) {
						matchTiles = "minecraft:blocks/" + matchTiles;
					}

					TextureAtlasSprite compareIcon = this.game.getTextureMapBlocks().getAtlasSprite(matchTiles);
					if (compareIcon != this.game.getTextureMapBlocks().getMissingSprite()) {
						ArrayList<IBlockState> tmpList = new ArrayList<>();

						for (Block testBlock : Block.REGISTRY) {
							UnmodifiableIterator blockState = testBlock.getBlockState().getValidStates().iterator();

							while (blockState.hasNext()) {
								IBlockState blockStatex = (IBlockState) blockState.next();

								try {
									IBakedModel bakedModel = blockModelShapes.getModelForState(blockStatex);
									List<BakedQuad> quads = new ArrayList<>();
									quads.addAll(bakedModel.getQuads(blockStatex, EnumFacing.UP, 0L));
									quads.addAll(bakedModel.getQuads(blockStatex, null, 0L));
									BlockModel model = new BlockModel(quads);
									model.setFailedToLoadCoords(this.failedToLoadX, this.failedToLoadY);
									if (model.numberOfFaces() > 0) {
										ArrayList<BlockModel.BlockFace> blockFaces = model.getFaces();

										for (int i = 0; i < blockFaces.size(); i++) {
											BlockModel.BlockFace face = model.getFaces().get(i);
											float minU = face.getMinU();
											float maxU = face.getMaxU();
											float minV = face.getMinV();
											float maxV = face.getMaxV();
											if (this.similarEnough(
												minU,
												maxU,
												minV,
												maxV,
												compareIcon.getMinU(),
												compareIcon.getMaxU(),
												compareIcon.getMinV(),
												compareIcon.getMaxV()
											)) {
												tmpList.add(blockStatex);
											}
										}
									}
								} catch (Exception var40) {
								}
							}
						}

						blockStates.addAll(tmpList);
					}
				}
			}

			if (blockStates.size() != 0) {
				if (!method.equals("horizontal")
					&& !method.startsWith("overlay")
					&& (method.equals("sandstone") || method.equals("top") || faces.contains("top") || faces.contains("all") || faces.length() == 0)) {
					try {
						ResourceLocation pngResource = new ResourceLocation(propertiesFile.getNamespace(), tilePath);
						InputStream is = this.game.getResourceManager().getResource(pngResource).getInputStream();
						Image top = ImageIO.read(is);
						is.close();
						top = top.getScaledInstance(1, 1, 4);
						BufferedImage topBuff = new BufferedImage(top.getWidth(null), top.getHeight(null), 6);
						Graphics gfx = topBuff.createGraphics();
						gfx.drawImage(top, 0, 0, null);
						gfx.dispose();
						int topRGB = topBuff.getRGB(0, 0);
						if ((topRGB >> 24 & 0xFF) == 0) {
							return;
						}

						for (IBlockState blockState : blockStates) {
							topRGB = topBuff.getRGB(0, 0);
							if (blockState.getBlock() == BlockRepository.cobweb) {
								topRGB |= -16777216;
							}

							if (renderPass.equals("3")) {
								topRGB = this.processRenderPassThree(topRGB);
								int blockStateID = BlockRepository.getStateId(blockState);
								int baseRGB = this.blockColors[blockStateID];
								if (baseRGB != 452984832 && baseRGB != -16842497) {
									topRGB = this.colorMultiplier(baseRGB, topRGB);
								}
							}

							if (BlockRepository.shapedBlocks.contains(blockState.getBlock())) {
								topRGB = this.applyShape(blockState.getBlock(), topRGB);
							}

							int blockStateID = BlockRepository.getStateId(blockState);
							if (!biomes.equals("")) {
								this.biomeTextureAvailable.add(blockStateID);

								for (int r = 0; r < biomesArray.length; r++) {
									int biomeInt = this.parseBiomeName(biomesArray[r]);
									if (biomeInt != -1) {
										this.blockBiomeSpecificColors.put(blockStateID + " " + biomeInt, topRGB);
									}
								}
							} else {
								this.blockColors[blockStateID] = topRGB;
							}
						}
					} catch (IOException ex) {
						System.err
							.println(
								"error getting CTM block from "
									+ propertiesFile.getPath()
									+ ": "
									+ filePath
									+ " "
									+ Block.REGISTRY.getNameForObject(blockStates.iterator().next().getBlock())
									+ " "
									+ tilePath
							);
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private boolean similarEnough(float a, float b, float c, float d, float one, float two, float three, float four) {
		boolean similar = Math.abs(a - one) < 1.0E-4;
		similar = similar && Math.abs(b - two) < 1.0E-4;
		similar = similar && Math.abs(c - three) < 1.0E-4;
		return similar && Math.abs(d - four) < 1.0E-4;
	}

	private int processRenderPassThree(int rgb) {
		if (this.renderPassThreeBlendMode.equals("color") || this.renderPassThreeBlendMode.equals("overlay")) {
			int red = rgb >> 16 & 0xFF;
			int green = rgb >> 8 & 0xFF;
			int blue = rgb >> 0 & 0xFF;
			float colorAverage = (red + blue + green) / 3.0F;
			float lighteningFactor = (colorAverage - 127.5F) * 2.0F;
			red += (int) (red * (lighteningFactor / 255.0F));
			blue += (int) (red * (lighteningFactor / 255.0F));
			green += (int) (red * (lighteningFactor / 255.0F));
			int newAlpha = (int) Math.abs(lighteningFactor);
			rgb = newAlpha << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
		}

		return rgb;
	}

	private int[] parseIntegerList(String list, int minValue, int maxValue) {
		ArrayList<Integer> tmpList = new ArrayList<>();

		for (String token : list.replace(',', ' ').split("\\s+")) {
			token = token.trim();

			try {
				if (token.matches("^\\d+$")) {
					tmpList.add(Integer.parseInt(token));
				} else if (token.matches("^\\d+-\\d+$")) {
					String[] t = token.split("-");
					int min = Integer.parseInt(t[0]);
					int max = Integer.parseInt(t[1]);

					for (int i = min; i <= max; i++) {
						tmpList.add(i);
					}
				}
			} catch (NumberFormatException var13) {
			}
		}

		if (minValue <= maxValue) {
			int i = 0;

			while (i < tmpList.size()) {
				if (tmpList.get(i) >= minValue && tmpList.get(i) <= maxValue) {
					i++;
				} else {
					tmpList.remove(i);
				}
			}
		}

		int[] a = new int[tmpList.size()];

		for (int i = 0; i < a.length; i++) {
			a[i] = tmpList.get(i);
		}

		return a;
	}

	private String[] parseStringList(String list) {
		ArrayList<String> tmpList = new ArrayList<>();

		for (String token : list.split("\\s+")) {
			token = token.trim();

			try {
				if (token.matches("^\\d+$")) {
					tmpList.add("" + Integer.parseInt(token));
				} else if (token.matches("^\\d+-\\d+$")) {
					String[] t = token.split("-");
					int min = Integer.parseInt(t[0]);
					int max = Integer.parseInt(t[1]);

					for (int i = min; i <= max; i++) {
						tmpList.add("" + i);
					}
				} else if (token != null && token != "") {
					tmpList.add(token);
				}
			} catch (NumberFormatException var11) {
			}
		}

		String[] a = new String[tmpList.size()];

		for (int i = 0; i < a.length; i++) {
			a[i] = tmpList.get(i);
		}

		return a;
	}

	private Set<IBlockState> parseBlocksList(String blocks, String metadataLine) {
		Set<IBlockState> blockStates = new HashSet<>();

		for (String blockString : blocks.split("\\s+")) {
			String metadata = metadataLine;
			blockString = blockString.trim();
			String[] blockComponents = blockString.split(":");
			int tokensUsed = 0;
			Block block = null;
			block = this.getBlockWithNameOrID(blockComponents[0]);
			if (block != null) {
				tokensUsed = 1;
			} else if (blockComponents.length > 1) {
				block = this.getBlockWithNameOrID(blockComponents[0] + ":" + blockComponents[1]);
				if (block != null) {
					tokensUsed = 2;
				}
			}

			if (block == null) {
				if (blockString.matches("^\\d+-\\d+$")) {
					String[] range = blockString.split("-");
					int min = Integer.parseInt(range[0]);
					int max = Integer.parseInt(range[1]);

					for (int i = min; i <= max; i++) {
						block = this.getBlockWithNameOrID("" + i);
						blockStates.addAll(this.parseBlockMetadata(block, metadata));
					}
				}
			} else {
				if (blockComponents.length > tokensUsed) {
					metadata = blockComponents[tokensUsed];

					for (int t = tokensUsed + 1; t < blockComponents.length; t++) {
						metadata = metadata + ":" + blockComponents[t];
					}
				}

				blockStates.addAll(this.parseBlockMetadata(block, metadata));
			}
		}

		return blockStates;
	}

	private <T extends Comparable<T>, V extends T> Set<IBlockState> parseBlockMetadata(Block block, String metadataList) {
		Set<IBlockState> blockStates = new HashSet<>();
		if (metadataList.equals("")) {
			blockStates.addAll(block.getBlockState().getValidStates());
		} else {
			Set<String> valuePairs = new HashSet<>();

			for (String metadata : metadataList.split(":")) {
				metadata.trim();
				if (metadata.contains("=")) {
					valuePairs.add(metadata);
				} else {
					int[] metadatas = this.parseIntegerList(metadata, 0, 15);

					for (int t = 0; t < metadatas.length; t++) {
						IBlockState blockState = this.parseIntegerMetadata(block, metadatas[t]);
						if (blockState != null) {
							blockStates.add(blockState);
						}
					}
				}
			}

			if (valuePairs.size() > 0) {
				UnmodifiableIterator var22 = block.getBlockState().getValidStates().iterator();

				while (var22.hasNext()) {
					IBlockState blockState = (IBlockState) var22.next();
					boolean matches = true;

					for (String pair : valuePairs) {
						String[] propertyAndValues = pair.split("\\s*=\\s*", 5);
						if (propertyAndValues.length == 2) {
							IProperty<?> property = block.getBlockState().getProperty(propertyAndValues[0]);
							if (property != null) {
								boolean valueIncluded = false;
								String[] values = propertyAndValues[1].split(",");

								for (String value : values) {
									if (property.getValueClass() == Integer.class && value.matches("^\\d+-\\d+$")) {
										String[] range = value.split("-");
										int min = Integer.parseInt(range[0]);
										int max = Integer.parseInt(range[1]);
										int intValue = (Integer) blockState.getProperties().get(property);
										if (intValue >= min && intValue <= max) {
											valueIncluded = true;
										}
									} else if (!blockState.getProperties().get(property).equals(property.parseValue(value))) {
										valueIncluded = true;
									}
								}

								matches = matches && valueIncluded;
							}
						}
					}

					if (matches) {
						blockStates.add(blockState);
					}
				}
			}
		}

		return blockStates;
	}

	private IBlockState parseIntegerMetadata(Block block, int metadata) {
		int blockID = BlockRepository.blockIDs.get(block);
		int blockStateID = blockID + (metadata << 12);
		return BlockRepository.defaultStorageStateIDs.inverse().get(blockStateID);
	}

	private int parseBiomeName(String name) {
		if (name.matches("^\\d+$")) {
			return Integer.parseInt(name);
		}

		for (int t = 0; t < this.sizeOfBiomeArray; t++) {
			if (Biome.getBiome(t) != null && Biome.getBiome(t).getBiomeName().toLowerCase().replace(" ", "").equalsIgnoreCase(name)) {
				return t;
			}
		}

		return -1;
	}

	private List<IResourcePack> getResourcePacks(String namespace) {
		List<IResourcePack> list = new ArrayList<>();
		IResourceManager superResourceManager = this.game.getResourceManager();
		if (superResourceManager instanceof SimpleReloadableResourceManager) {
			Object nameSpaceToResourceManagerObj = ReflectionUtils.getPrivateFieldValueByType(
				superResourceManager, SimpleReloadableResourceManager.class, java.util.Map.class
			);
			if (nameSpaceToResourceManagerObj == null) {
				return list;
			}

			java.util.Map<String, FallbackResourceManager> nameSpaceToResourceManager = (java.util.Map<String, FallbackResourceManager>) nameSpaceToResourceManagerObj;

			for (Entry<String, FallbackResourceManager> entry : nameSpaceToResourceManager.entrySet()) {
				if (namespace == null || namespace.equals(entry.getKey())) {
					FallbackResourceManager resourceManager = entry.getValue();
					Object resourcePacksObj = ReflectionUtils.getPrivateFieldValueByType(resourceManager, FallbackResourceManager.class, List.class);
					if (resourcePacksObj == null) {
						return list;
					}

					List<IResourcePack> resourcePacks = (List<IResourcePack>) resourcePacksObj;
					list.addAll(resourcePacks);
				}
			}
		}

		Collections.reverse(list);
		return list;
	}

	private List<ResourceLocation> findResources(
		String namespace, String directory, String suffix, boolean recursive, boolean directories, boolean sortByFilename
	) {
		if (directory == null) {
			directory = "";
		}

		if (directory.startsWith("/")) {
			directory = directory.substring(1);
		}

		if (suffix == null) {
			suffix = "";
		}

		ArrayList<ResourceLocation> resources = new ArrayList<>();

		for (IResourcePack resourcePack : this.getResourcePacks(namespace)) {
			if (!(resourcePack instanceof DefaultResourcePack)) {
				if (resourcePack instanceof FileResourcePack) {
					Object zipFileObj = ReflectionUtils.getPrivateFieldValueByType(resourcePack, FileResourcePack.class, ZipFile.class);
					if (zipFileObj == null) {
						return resources;
					}

					ZipFile zipFile = (ZipFile) zipFileObj;
					if (zipFile != null) {
						this.findResourcesZip(zipFile, namespace, "assets/" + namespace, directory, suffix, recursive, directories, resources);
					}
				} else if (resourcePack instanceof AbstractResourcePack) {
					Object baseObj = ReflectionUtils.getPrivateFieldValueByType(resourcePack, AbstractResourcePack.class, File.class);
					if (baseObj != null) {
						File base = (File) baseObj;
						if (base != null && base.isDirectory()) {
							base = new File(base, "assets/" + namespace);
							if (base.isDirectory()) {
								findResourcesDirectory(base, namespace, directory, suffix, recursive, directories, resources);
							}
						}
					}
				}
			}
		}

		if (sortByFilename) {
			Collections.sort(resources, new Comparator<ResourceLocation>() {
				public int compare(ResourceLocation o1, ResourceLocation o2) {
					String f1 = o1.getPath().replaceAll(".*/", "").replaceFirst("\\.properties", "");
					String f2 = o2.getPath().replaceAll(".*/", "").replaceFirst("\\.properties", "");
					int result = f1.compareTo(f2);
					return result != 0 ? result : o1.getPath().compareTo(o2.getPath());
				}
			});
		} else {
			Collections.sort(resources, new Comparator<ResourceLocation>() {
				public int compare(ResourceLocation o1, ResourceLocation o2) {
					return o1.getPath().compareTo(o2.getPath());
				}
			});
		}

		return resources;
	}

	private void findResourcesZip(
		ZipFile zipFile,
		String namespace,
		String root,
		String directory,
		String suffix,
		boolean recursive,
		boolean directories,
		Collection<ResourceLocation> resources
	) {
		String base = root + "/" + directory;

		for (ZipEntry entry : Collections.list(zipFile.entries())) {
			if (entry.isDirectory() == directories) {
				String name = entry.getName().replaceFirst("^/", "");
				if (name.startsWith(base) && name.endsWith(suffix)) {
					if (directory.equals("")) {
						if (recursive || !name.contains("/")) {
							resources.add(new ResourceLocation(namespace, name));
						}
					} else {
						String subpath = name.substring(base.length());
						if ((subpath.equals("") || subpath.startsWith("/")) && (recursive || subpath.equals("") || !subpath.substring(1).contains("/"))) {
							resources.add(new ResourceLocation(namespace, name.substring(root.length() + 1)));
						}
					}
				}
			}
		}
	}

	private void processColorProperties() {
		Properties properties = new Properties();

		try {
			InputStream input = this.game.getResourceManager().getResource(new ResourceLocation("mcpatcher/color.properties")).getInputStream();
			if (input != null) {
				properties.load(input);
				input.close();
			}
		} catch (IOException var20) {
		}

		IBlockState blockState = BlockRepository.lilypad.getDefaultState();
		int blockStateID = BlockRepository.getStateId(blockState);
		int lilyRGB = this.getBlockColor(blockStateID);
		int lilypadMultiplier = 2129968;
		String lilypadMultiplierString = properties.getProperty("lilypad");
		if (lilypadMultiplierString != null) {
			lilypadMultiplier = Integer.parseInt(lilypadMultiplierString, 16);
		}

		UnmodifiableIterator defaultFormat = BlockRepository.lilypad.getBlockState().getValidStates().iterator();

		while (defaultFormat.hasNext()) {
			IBlockState padBlockState = (IBlockState) defaultFormat.next();
			blockStateID = BlockRepository.getStateId(padBlockState);
			this.blockColors[blockStateID] = this.colorMultiplier(lilyRGB, lilypadMultiplier | 0xFF000000);
			this.blockColorsWithDefaultTint[blockStateID] = this.blockColors[blockStateID];
		}

		String defaultFormatx = properties.getProperty("palette.format");
		boolean globalGrid = defaultFormatx != null && defaultFormatx.equalsIgnoreCase("grid");
		Enumeration<?> e = properties.propertyNames();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.startsWith("palette.block")) {
				String filename = key.substring("palette.block.".length());
				filename = filename.replace("~", "mcpatcher");
				this.processColorPropertyHelper(new ResourceLocation(filename), properties.getProperty(key), globalGrid);
			}
		}

		for (ResourceLocation resource : this.findResources("minecraft", "/mcpatcher/colormap/blocks", ".properties", true, false, true)) {
			Properties colorProperties = new Properties();

			try {
				InputStream input = this.game.getResourceManager().getResource(resource).getInputStream();
				if (input != null) {
					colorProperties.load(input);
					input.close();
				}
			} catch (IOException ex) {
				break;
			}

			String names = colorProperties.getProperty("blocks");
			if (names == null) {
				String name = resource.getPath();
				name = name.substring(name.lastIndexOf("/") + 1, name.lastIndexOf(".properties"));
				names = name;
			}

			String source = colorProperties.getProperty("source");
			ResourceLocation resourcePNG;
			if (source != null) {
				resourcePNG = new ResourceLocation(resource.getNamespace(), source);

				try {
					this.game.getResourceManager().getResource(resourcePNG);
				} catch (IOException exx) {
					Path path = Paths.get("optifine/colormap/blocks/", source);
					path = path.normalize();
					resourcePNG = new ResourceLocation(resource.getNamespace(), path.toString().replace(File.separatorChar, '/'));
					System.out.println("trying " + resourcePNG);
				}
			} else {
				resourcePNG = new ResourceLocation(resource.getNamespace(), resource.getPath().replace(".properties", ".png"));
			}

			String format = colorProperties.getProperty("format");
			boolean grid;
			if (format != null) {
				grid = format != null && format.equalsIgnoreCase("grid");
			} else {
				grid = globalGrid;
			}

			String yOffsetString = colorProperties.getProperty("yOffset");
			int yOffset = 0;
			if (yOffsetString != null) {
				yOffset = Integer.valueOf(yOffsetString);
			}

			this.processColorProperty(resourcePNG, names, grid, yOffset);
		}

		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/water.png"), "water", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/watercolorx.png"), "water", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/swampgrass.png"), "grass_block grass fern tall_grass large_fern", globalGrid);
		this.processColorPropertyHelper(
			new ResourceLocation("mcpatcher/colormap/swampgrasscolor.png"), "grass_block grass fern tall_grass large_fern", globalGrid
		);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/swampfoliage.png"), "oak_leaves vine", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/swampfoliagecolor.png"), "oak_leaves vine", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/pine.png"), "spruce_leaves", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/pinecolor.png"), "spruce_leaves", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/birch.png"), "birch_leaves", globalGrid);
		this.processColorPropertyHelper(new ResourceLocation("mcpatcher/colormap/birchcolor.png"), "birch_leaves", globalGrid);
	}

	private void processColorPropertyHelper(ResourceLocation resource, String list, boolean grid) {
		ResourceLocation resourceProperties = new ResourceLocation(resource.getNamespace(), resource.getPath().replace(".png", ".properties"));
		Properties colorProperties = new Properties();
		int yOffset = 0;

		try {
			InputStream input = this.game.getResourceManager().getResource(resourceProperties).getInputStream();
			if (input != null) {
				colorProperties.load(input);
				input.close();
			}

			String format = colorProperties.getProperty("format");
			if (format != null) {
				grid = format.equalsIgnoreCase("grid");
			}

			String yOffsetString = colorProperties.getProperty("yOffset");
			if (yOffsetString != null) {
				yOffset = Integer.valueOf(yOffsetString);
			}
		} catch (IOException var10) {
		}

		this.processColorProperty(resource, list, grid, yOffset);
	}

	private void processColorProperty(ResourceLocation resource, String list, boolean grid, int yOffset) {
		int[][] tints = new int[this.sizeOfBiomeArray][32];

		for (int[] row : tints) {
			Arrays.fill(row, -1);
		}

		boolean swamp = resource.getPath().contains("/swamp");
		Image tintColors = null;

		try {
			InputStream is = this.game.getResourceManager().getResource(resource).getInputStream();
			tintColors = ImageIO.read(is);
			is.close();
		} catch (IOException e) {
			return;
		}

		BufferedImage tintColorsBuff = new BufferedImage(tintColors.getWidth(null), tintColors.getHeight(null), 1);
		Graphics gfx = tintColorsBuff.createGraphics();
		gfx.drawImage(tintColors, 0, 0, null);
		gfx.dispose();
		int numBiomesToCheck = grid ? Math.min(tintColorsBuff.getWidth(), this.sizeOfBiomeArray) : this.sizeOfBiomeArray;

		for (int t = 0; t < numBiomesToCheck; t++) {
			Biome biome = Biome.getBiome(t);
			if (biome != null) {
				int tintMult = 0;
				int heightMultiplier = tintColorsBuff.getHeight() / 32;

				for (int s = 0; s < 32; s++) {
					if (grid) {
						tintMult = tintColorsBuff.getRGB(t, Math.min(Math.max(0, s * heightMultiplier - yOffset), tintColorsBuff.getHeight() - 1)) & 16777215;
					} else {
						double var1x = MathHelper.clamp(biome.getTemperature(new BlockPos(0, 64, 0)), 0.0F, 1.0F);
						double var2x = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
						var2x *= var1x;
						var1x = 1.0 - var1x;
						var2x = 1.0 - var2x;
						tintMult = tintColorsBuff.getRGB((int) ((tintColorsBuff.getWidth() - 1) * var1x), (int) ((tintColorsBuff.getHeight() - 1) * var2x)) & 16777215;
					}

					if (tintMult != 0 && (!swamp || t == 6)) {
						tints[t][s] = tintMult;
					}
				}
			}
		}

		Set<IBlockState> blockStates = new HashSet<>();
		blockStates.addAll(this.parseBlocksList(list, ""));

		for (IBlockState blockState : blockStates) {
			int blockStateID = BlockRepository.getStateId(blockState);
			int[][] previousTints = this.blockTintTables.get(blockStateID);
			if (swamp && previousTints == null) {
				ResourceLocation defaultResource;
				if (resource.getPath().contains("grass")) {
					defaultResource = new ResourceLocation("textures/colormap/grass.png");
				} else {
					defaultResource = new ResourceLocation("textures/colormap/foliage.png");
				}

				String stateString = blockState.toString().toLowerCase();
				stateString = stateString.replaceAll("^block", "");
				stateString = stateString.replace("{", "");
				stateString = stateString.replace("}", "");
				stateString = stateString.replace("[", ":");
				stateString = stateString.replace("]", "");
				stateString = stateString.replace(",", ":");
				this.processColorProperty(defaultResource, stateString, false, 0);
				previousTints = this.blockTintTables.get(blockStateID);
			}

			if (previousTints != null) {
				for (int tx = 0; tx < this.sizeOfBiomeArray; tx++) {
					for (int s = 0; s < 32; s++) {
						if (tints[tx][s] == -1) {
							tints[tx][s] = previousTints[tx][s];
						}
					}
				}
			}

			this.blockColorsWithDefaultTint[blockStateID] = this.colorMultiplier(this.getBlockColor(blockStateID), tints[4][8] | 0xFF000000);
			this.blockTintTables.put(blockStateID, tints);
			this.biomeTintsAvailable.add(blockStateID);
		}
	}

	private Block getBlockWithNameOrID(String name) {
		ResourceLocation resourceLocation = new ResourceLocation(name);
		if (Block.REGISTRY.containsKey(resourceLocation)) {
			return Block.REGISTRY.getObject(resourceLocation);
		}

		try {
			return BlockRepository.blockIDs.inverse().get(Integer.parseInt(name));
		} catch (NumberFormatException var3) {
			return null;
		}
	}

	interface ColorResolver {
		int getColorAtPos(Biome var1, BlockPos var2);
	}
}
