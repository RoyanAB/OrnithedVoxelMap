package com.mamiyaotaru.voxelmap.persistent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeListener;
import com.mamiyaotaru.voxelmap.interfaces.ISettingsAndLightingChangeNotifier;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class CachedRegion implements IThreadCompleteListener, ISettingsAndLightingChangeListener {
	public static EmptyCachedRegion emptyRegion = new EmptyCachedRegion();
	private final int width = 256;
	private final boolean[] chunkChanged = new boolean[256];
	private final ReentrantLock threadLock = new ReentrantLock();
	boolean remoteWorld;
	MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
	MutableBlockPos loopBlockPos = new MutableBlockPos(0, 0, 0);
	Future<?> future = null;
	boolean displayOptionsChanged = false;
	boolean imageChanged = false;
	boolean queued = false;
	boolean refreshingImage = false;
	boolean dataUpdated = false;
	boolean updateQueued = false;
	boolean loaded = false;
	private long mostRecentView = 0L;
	private long mostRecentChange = 0L;
	private IPersistentMap persistentMap;
	private String key;
	private World world;
	private IChunkLoader chunkLoader;
	private String worldName;
	private String subworldName;
	private String worldNamePathPart;
	private String subworldNamePathPart = "";
	private String dimensionNamePathPart;
	private String dimensionNamePathPartOld;
	private boolean underground = false;
	private int x;
	private int z;
	private boolean empty = true;
	private boolean liveChunksUpdated = false;
	private CompressibleGLBufferedImage image;
	private CompressibleMapData data;
	private boolean queuedToCompress = false;

	public CachedRegion() {
	}

	public CachedRegion(IPersistentMap persistentMap, String key, World world, String worldName, String subworldName, int x, int z) {
		this.persistentMap = persistentMap;
		this.key = key;
		this.world = world;
		this.worldName = worldName;
		this.subworldName = subworldName;
		this.worldNamePathPart = TextUtils.scrubNameFile(worldName);
		if (subworldName != "") {
			this.subworldNamePathPart = TextUtils.scrubNameFile(subworldName) + "/";
		}

		String dimensionName = world.provider.getDimensionType().getName();
		int dimensionID = DimensionManager.getDimensionIDfromProvider(world.provider);
		this.dimensionNamePathPartOld = TextUtils.scrubNameFile(dimensionName);
		this.dimensionNamePathPart = TextUtils.scrubNameFile(dimensionName + " (dimension " + dimensionID + ")");
		boolean knownUnderground = false;
		knownUnderground = knownUnderground || dimensionName.equalsIgnoreCase("erebus");
		this.underground = !world.provider.isSurfaceWorld() && !world.provider.hasSkyLight() && dimensionID != 1 || knownUnderground;
		this.remoteWorld = !Minecraft.getMinecraft().isIntegratedServerRunning();
		persistentMap.getSettingsAndLightingChangeNotifier().addObserver(this);
		this.x = x;
		this.z = z;
		if (!this.remoteWorld) {
			WorldServer worldServer = Minecraft.getMinecraft().getIntegratedServer().getWorld(dimensionID);
			this.chunkLoader = worldServer.getSaveHandler().getChunkLoader(worldServer.provider);
		}

		Arrays.fill(this.chunkChanged, false);
	}

	public void registerChangeAt(int worldBlockX, int worldBlockZ) {
		int blockX = worldBlockX - this.x * 256;
		int blockZ = worldBlockZ - this.z * 256;
		int chunkX = (int) Math.floor(blockX / 16.0F);
		int chunkZ = (int) Math.floor(blockZ / 16.0F);

		try {
			this.chunkChanged[chunkZ * 16 + chunkX] = true;
			this.updateQueued = true;
		} catch (ArrayIndexOutOfBoundsException var8) {
		}
	}

	public void renameSubworld(String oldName, String newName) {
		if (oldName.equals(this.subworldName)) {
			this.threadLock.lock();

			try {
				this.subworldName = newName;
				if (this.subworldName != "") {
					this.subworldNamePathPart = TextUtils.scrubNameFile(this.subworldName) + "/";
				}
			} catch (Exception var7) {
			} finally {
				this.threadLock.unlock();
			}
		}
	}

	@Override
	public void notifyOfActionableChange(ISettingsAndLightingChangeNotifier notifier) {
		this.displayOptionsChanged = true;
	}

	public void refresh(boolean forceCompress) {
		this.mostRecentView = System.currentTimeMillis();
		if (this.future != null && (this.future.isDone() || this.future.isCancelled())) {
			this.queued = false;
		}

		if (!this.queued) {
			this.queued = true;
			if (this.loaded && !this.dataUpdated && !this.updateQueued && !this.displayOptionsChanged) {
				this.queued = false;
			} else {
				CachedRegion.RefreshRunnable regionProcessingRunnable = new CachedRegion.RefreshRunnable(forceCompress);
				this.future = ThreadManager.executorService.submit(regionProcessingRunnable);
			}
		}
	}

	public void loadChunk(Chunk chunk) {
		this.mostRecentView = System.currentTimeMillis();
		this.mostRecentChange = this.mostRecentView;
		CachedRegion.FillChunkRunnable fillChunkRunnable = new CachedRegion.FillChunkRunnable(chunk);
		ThreadManager.executorService.execute(fillChunkRunnable);
	}

	@Override
	public void notifyOfThreadComplete(AbstractNotifyingRunnable runnable) {
	}

	private void load() {
		this.data = new CompressibleMapData(256, 256);
		this.image = new CompressibleGLBufferedImage(256, 256, 6);
		this.loadCachedData();
		this.loadCurrentData(this.world);
		if (!this.remoteWorld) {
			this.loadAnvilData(this.world);
		}

		this.loaded = true;
	}

	private void loadCurrentData(World world) {
		for (int chunkX = 0; chunkX < 16; chunkX++) {
			for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
				Chunk chunk = world.getChunkFromChunkCoords(this.x * 16 + chunkX, this.z * 16 + chunkZ);
				if (chunk != null && chunk.isLoaded()) {
					this.loadChunkData(chunk, chunkX, chunkZ);
					this.empty = false;
					this.liveChunksUpdated = true;
					this.dataUpdated = true;
				}
			}
		}
	}

	private void loadModifiedData() {
		for (int chunkX = 0; chunkX < 16; chunkX++) {
			for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
				if (this.chunkChanged[chunkZ * 16 + chunkX]) {
					Chunk chunk = this.world.getChunkFromChunkCoords(this.x * 16 + chunkX, this.z * 16 + chunkZ);
					if (chunk != null && chunk.isLoaded()) {
						this.loadChunkData(chunk, chunkX, chunkZ);
						this.empty = false;
						this.liveChunksUpdated = true;
						this.dataUpdated = true;
					}
				}
			}
		}
	}

	private void loadAnvilData(World world) {
		if (!this.remoteWorld) {
			for (int t = 0; t < 16; t++) {
				for (int s = 0; s < 16; s++) {
					if (this.data.getHeight(t * 16, s * 16) <= 0 && this.data.getLight(t * 16, s * 16) <= 0) {
						Chunk chunk = world.getChunkFromChunkCoords(this.x * 16 + t, this.z * 16 + s);
						if (chunk == null || chunk.isEmpty()) {
							try {
								Chunk loadedChunk = this.chunkLoader.loadChunk(world, this.x * 16 + t, this.z * 16 + s);
								if (loadedChunk != null) {
									this.loadChunkData(loadedChunk, t, s);
									this.empty = false;
									this.dataUpdated = true;
									this.liveChunksUpdated = true;
								}
							} catch (Exception var6) {
							}
						}
					}
				}
			}
		}
	}

	private void loadCachedData() {
		try {
			File cachedRegionFileDir = new File(
				Minecraft.getMinecraft().mcDataDir, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart
			);
			cachedRegionFileDir.mkdirs();
			File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
			if (cachedRegionFile.exists()) {
				ZipFile zFile = new ZipFile(cachedRegionFile);
				BiMap<IBlockState, Integer> stateToInt = null;
				int total = 0;
				byte[] decompressedByteData = new byte[this.data.getWidth() * this.data.getHeight() * 17 * 4];
				ZipEntry ze = zFile.getEntry("data");
				InputStream is = zFile.getInputStream(ze);
				byte[] byteData = new byte[2048];

				int count;
				while ((count = is.read(byteData, 0, 2048)) != -1 && count + total <= this.data.getWidth() * this.data.getHeight() * 17 * 4) {
					System.arraycopy(byteData, 0, decompressedByteData, total, count);
					total += count;
				}

				is.close();
				ze = zFile.getEntry("key");
				is = zFile.getInputStream(ze);
				BiMap<IBlockState, Integer> var18 = HashBiMap.create();
				Scanner sc = new Scanner(is);

				while (sc.hasNextLine()) {
					BlockStateParser.parseLine(sc.nextLine(), var18);
				}

				sc.close();
				is.close();
				int version = 1;
				ze = zFile.getEntry("control");
				if (ze != null) {
					is = zFile.getInputStream(ze);
					if (is != null) {
						Properties properties = new Properties();
						properties.load(is);
						String versionString = properties.getProperty("version", "1");

						try {
							version = Integer.parseInt(versionString);
						} catch (NumberFormatException ex) {
							version = 1;
						}

						is.close();
					}
				}

				zFile.close();
				if (total == this.data.getWidth() * this.data.getHeight() * 18 && var18 != null) {
					byteData = new byte[this.data.getWidth() * this.data.getHeight() * 18];
					System.arraycopy(decompressedByteData, 0, byteData, 0, byteData.length);
					this.data.setData(byteData, var18, version);
					this.empty = false;
					this.dataUpdated = true;
				} else {
					System.out.println("failed to load data from " + cachedRegionFile.getPath());
				}

				if (var18 == null || version < 2) {
					this.liveChunksUpdated = true;
				}
			}
		} catch (Exception e) {
			System.err
				.println(
					"Failed to load region file for "
						+ this.x
						+ ","
						+ this.z
						+ " in "
						+ this.worldNamePathPart
						+ "/"
						+ this.subworldNamePathPart
						+ this.dimensionNamePathPart
				);
			e.printStackTrace();
		}
	}

	private void saveData() {
		if (this.liveChunksUpdated && !this.worldNamePathPart.equals("")) {
			ThreadManager.executorService
				.execute(
					new Runnable() {
						@Override
						public void run() {
							CachedRegion.this.threadLock.lock();

							try {
								BiMap<IBlockState, Integer> stateToInt = CachedRegion.this.data.getStateToInt();
								byte[] byteArray = CachedRegion.this.data.getData();
								int var10000 = byteArray.length;
								int var10001 = CachedRegion.this.data.getWidth() * CachedRegion.this.data.getHeight();
								if (var10000 == var10001 * 18) {
									File cachedRegionFileDir = new File(
										Minecraft.getMinecraft().mcDataDir,
										"/voxelMap/cache/"
											+ CachedRegion.this.worldNamePathPart
											+ "/"
											+ CachedRegion.this.subworldNamePathPart
											+ CachedRegion.this.dimensionNamePathPart
									);
									cachedRegionFileDir.mkdirs();
									File cachedRegionFile = new File(cachedRegionFileDir, "/" + CachedRegion.this.key + ".zip");
									FileOutputStream fos = new FileOutputStream(cachedRegionFile);
									ZipOutputStream zos = new ZipOutputStream(fos);
									ZipEntry ze = new ZipEntry("data");
									ze.setSize(byteArray.length);
									zos.putNextEntry(ze);
									zos.write(byteArray);
									zos.closeEntry();
									if (stateToInt != null) {
										Iterator<Entry<IBlockState, Integer>> iterator = stateToInt.entrySet().iterator();
										StringBuffer stringBuffer = new StringBuffer();

										while (iterator.hasNext()) {
											Entry<IBlockState, Integer> entry = iterator.next();
											String nextLine = entry.getValue() + " " + entry.getKey().toString() + "\r\n";
											stringBuffer.append(nextLine);
										}

										byte[] keyByteArray = String.valueOf(stringBuffer).getBytes();
										ze = new ZipEntry("key");
										ze.setSize(keyByteArray.length);
										zos.putNextEntry(ze);
										zos.write(keyByteArray);
										zos.closeEntry();
									}

									String nextLine = "version:2\r\n";
									byte[] keyByteArray = nextLine.getBytes();
									ze = new ZipEntry("control");
									ze.setSize(keyByteArray.length);
									zos.putNextEntry(ze);
									zos.write(keyByteArray);
									zos.closeEntry();
									zos.close();
									fos.close();
								} else {
									System.err
										.println(
											"Data array wrong size: "
												+ byteArray.length
												+ "for "
												+ CachedRegion.this.x
												+ ","
												+ CachedRegion.this.z
												+ " in "
												+ CachedRegion.this.worldNamePathPart
												+ "/"
												+ CachedRegion.this.subworldNamePathPart
												+ CachedRegion.this.dimensionNamePathPart
										);
								}
							} catch (IOException e) {
								System.err
									.println(
										"Failed to save region file for "
											+ CachedRegion.this.x
											+ ","
											+ CachedRegion.this.z
											+ " in "
											+ CachedRegion.this.worldNamePathPart
											+ "/"
											+ CachedRegion.this.subworldNamePathPart
											+ CachedRegion.this.dimensionNamePathPart
									);
								e.printStackTrace();
							} finally {
								CachedRegion.this.threadLock.unlock();
							}
						}
					}
				);
			this.liveChunksUpdated = false;
		}
	}

	private void loadChunkData(Chunk chunk, int chunkX, int chunkZ) {
		for (int t = 0; t < 16; t++) {
			for (int s = 0; s < 16; s++) {
				this.persistentMap
					.getAndStoreData(this.data, this.world, chunk, this.blockPos, this.underground, this.x * 256, this.z * 256, chunkX * 16 + t, chunkZ * 16 + s);
			}
		}

		this.chunkChanged[chunkZ * 16 + chunkX] = false;
	}

	private void fillImage() {
		int color24 = 0;

		for (int t = 0; t < 256; t++) {
			for (int s = 0; s < 256; s++) {
				color24 = this.persistentMap
					.getPixelColor(this.data, this.world, this.blockPos, this.loopBlockPos, this.underground, 8, this.x * 256, this.z * 256, t, s);
				this.image.setRGB(t, s, color24);
			}
		}
	}

	private void saveImage() {
		if (!this.empty) {
			File imageFileDir = new File(
				Minecraft.getMinecraft().mcDataDir,
				"/voxelMap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart + "/images/z1"
			);
			imageFileDir.mkdirs();
			final File imageFile = new File(imageFileDir, this.key + ".png");
			if (this.liveChunksUpdated || !imageFile.exists()) {
				ThreadManager.executorService.execute(new Runnable() {
					@Override
					public void run() {
						CachedRegion.this.threadLock.lock();

						try {
							BufferedImage realBufferedImage = new BufferedImage(CachedRegion.this.width, CachedRegion.this.width, 6);
							byte[] dstArray = ((DataBufferByte) realBufferedImage.getRaster().getDataBuffer()).getData();
							System.arraycopy(CachedRegion.this.image.getData(), 0, dstArray, 0, CachedRegion.this.image.getData().length);
							ImageIO.write(realBufferedImage, "png", imageFile);
						} catch (IOException var9) {
							var9.printStackTrace();
						} finally {
							CachedRegion.this.threadLock.unlock();
						}
					}
				});
			}
		}
	}

	public long getMostRecentView() {
		return this.mostRecentView;
	}

	public long getMostRecentChange() {
		return this.mostRecentChange;
	}

	public String getKey() {
		return this.key;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public int getWidth() {
		return this.width;
	}

	public int getGLID() {
		if (this.image != null) {
			if (!this.refreshingImage) {
				synchronized (this.image) {
					if (this.imageChanged) {
						this.imageChanged = false;
						this.image.write();
					}
				}
			}

			return this.image.getIndex();
		} else {
			return 0;
		}
	}

	public CompressibleMapData getMapData() {
		return this.data;
	}

	public boolean isLoaded() {
		return this.loaded;
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public boolean isGroundAt(int blockX, int blockZ) {
		return this.isLoaded() && this.getHeightAt(blockX, blockZ) > 0;
	}

	public int getHeightAt(int blockX, int blockZ) {
		int x = blockX - this.x * 256;
		int z = blockZ - this.z * 256;
		int y = this.data == null ? 0 : this.data.getHeight(x, z);
		if (this.underground && y == 255) {
			y = CommandUtils.getSafeHeight(blockX, 64, blockZ, this.world);
		}

		return y;
	}

	public void compress() {
		if (this.data != null && !this.isCompressed() && !this.queuedToCompress) {
			this.queuedToCompress = true;
			ThreadManager.executorService.execute(new Runnable() {
				@Override
				public void run() {
					if (CachedRegion.this.threadLock.tryLock()) {
						try {
							CachedRegion.this.compressData();
						} catch (Exception var5) {
						} finally {
							CachedRegion.this.threadLock.unlock();
						}
					}

					CachedRegion.this.queuedToCompress = false;
				}
			});
		}
	}

	private void compressData() {
		this.data.compress();
	}

	private boolean isCompressed() {
		return this.data.isCompressed();
	}

	public synchronized void cleanup() {
		this.queuedToCompress = true;
		if (this.future != null) {
			this.future.cancel(false);
		}

		this.persistentMap.getSettingsAndLightingChangeNotifier().removeObserver(this);
		if (this.image != null) {
			this.image.baleet();
		}

		this.saveData();
		if (this.persistentMap.getOptions().outputImages) {
			this.saveImage();
		}
	}

	public synchronized void save() {
		this.saveData();
	}

	private class FillChunkRunnable implements Runnable {
		private final Chunk chunk;

		public FillChunkRunnable(Chunk chunk) {
			this.chunk = chunk;
		}

		@Override
		public void run() {
			CachedRegion.this.threadLock.lock();

			try {
				if (!CachedRegion.this.loaded) {
					CachedRegion.this.load();
				}

				int chunkX = this.chunk.x - CachedRegion.this.x * 16;
				int chunkZ = this.chunk.z - CachedRegion.this.z * 16;
				CachedRegion.this.loadChunkData(this.chunk, chunkX, chunkZ);
				CachedRegion.this.empty = false;
				CachedRegion.this.liveChunksUpdated = true;
				CachedRegion.this.dataUpdated = true;
			} catch (Exception var6) {
			} finally {
				CachedRegion.this.threadLock.unlock();
			}
		}
	}

	private class RefreshRunnable extends AbstractNotifyingRunnable {
		private boolean forceCompress = false;

		public RefreshRunnable(boolean forceCompress) {
			this.forceCompress = forceCompress;
		}

		@Override
		public void doRun() {
			CachedRegion.this.queued = false;
			CachedRegion.this.threadLock.lock();
			CachedRegion.this.mostRecentChange = System.currentTimeMillis();

			try {
				if (!CachedRegion.this.loaded) {
					CachedRegion.this.load();
				}

				if (CachedRegion.this.updateQueued) {
					CachedRegion.this.loadModifiedData();
					CachedRegion.this.updateQueued = false;
				}

				for (; CachedRegion.this.dataUpdated || CachedRegion.this.displayOptionsChanged; CachedRegion.this.refreshingImage = false) {
					CachedRegion.this.dataUpdated = false;
					CachedRegion.this.displayOptionsChanged = false;
					CachedRegion.this.refreshingImage = true;
					synchronized (CachedRegion.this.image) {
						CachedRegion.this.fillImage();
						CachedRegion.this.imageChanged = true;
					}
				}

				if (this.forceCompress) {
					CachedRegion.this.compressData();
				}
			} catch (Exception e) {
				System.out.println("Exception loading chunk: " + e.getLocalizedMessage());
				e.printStackTrace();
			} finally {
				CachedRegion.this.threadLock.unlock();
			}
		}
	}
}
