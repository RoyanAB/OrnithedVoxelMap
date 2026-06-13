package com.mamiyaotaru.voxelmap.persistent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mamiyaotaru.voxelmap.VoxelConstants;
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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unused")
public class CachedRegion implements IThreadCompleteListener, ISettingsAndLightingChangeListener {
	public final static EmptyCachedRegion emptyRegion = new EmptyCachedRegion();
	private final int width = 256;
	private final boolean[] chunkChanged = new boolean[256];
	private final ReentrantLock threadLock = new ReentrantLock();
	private final MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
	private final MutableBlockPos loopBlockPos = new MutableBlockPos(0, 0, 0);

	private boolean remoteWorld;
	private boolean empty = true;
	private boolean liveChunksUpdated = false;
	private boolean queuedToCompress = false;
	private boolean displayOptionsChanged = false;
	private boolean imageChanged = false;
	private boolean queued = false;
	private boolean refreshingImage = false;
	private boolean dataUpdated = false;
	private boolean updateQueued = false;
	private boolean loaded = false;
	private boolean underground = false;

	private Future<?> future;
	private IPersistentMap persistentMap;
	private String key;
	private World world;
	private IChunkLoader chunkLoader;

	private String subworldName;
	private String worldNamePathPart;
	private String subworldNamePathPart = "";
	private String dimensionNamePathPart;

	private int x;
	private int z;

	private long mostRecentView = 0L;
	private long mostRecentChange = 0L;

	private CompressibleGLBufferedImage image;
	private CompressibleMapData data;

	public CachedRegion() {
	}

	public CachedRegion(IPersistentMap persistentMap, String key, World world, String worldName, String subworldName, int x, int z) {
		this.persistentMap = persistentMap;
		this.key = key;
		this.world = world;
		this.subworldName = subworldName;
		this.worldNamePathPart = TextUtils.scrubNameFile(worldName);
		if (!Objects.equals(subworldName, "")) {
			this.subworldNamePathPart = TextUtils.scrubNameFile(subworldName) + "/";
		}

		String dimensionName = world.provider.getDimensionType().getName();
		int dimensionID = DimensionManager.getDimensionIDfromProvider(world.provider);
		TextUtils.scrubNameFile(dimensionName);
		this.dimensionNamePathPart = TextUtils.scrubNameFile(dimensionName + " (dimension " + dimensionID + ")");
		this.underground = !world.provider.isSurfaceWorld() && !world.provider.hasSkyLight() && dimensionID != 1;
		this.remoteWorld = !Minecraft.getMinecraft().isIntegratedServerRunning();
		persistentMap.getSettingsAndLightingChangeNotifier().addObserver(this);
		this.x = x;
		this.z = z;
		if (!this.remoteWorld) {
			WorldServer worldServer = Objects.requireNonNull(Minecraft.getMinecraft().getIntegratedServer()).getWorld(dimensionID);
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
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
	}

	public void renameSubworld(String oldName, String newName) {
		if (oldName.equals(this.subworldName)) {
			this.threadLock.lock();

			try {
				this.subworldName = newName;
				if (!this.subworldName.isEmpty()) {
					this.subworldNamePathPart = TextUtils.scrubNameFile(this.subworldName) + "/";
				}
			} catch (Exception ignored) {
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
	public void notifyOfThreadComplete(AbstractNotifyingRunnable notifyingRunnable) {
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
				Chunk chunk = world.getChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ);
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
					Chunk chunk = this.world.getChunk(this.x * 16 + chunkX, this.z * 16 + chunkZ);
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
						Chunk chunk = world.getChunk(this.x * 16 + t, this.z * 16 + s);
						if (chunk == null || chunk.isEmpty()) {
							try {
								Chunk loadedChunk = this.chunkLoader.loadChunk(world, this.x * 16 + t, this.z * 16 + s);
								if (loadedChunk != null) {
									this.loadChunkData(loadedChunk, t, s);
									this.empty = false;
									this.dataUpdated = true;
									this.liveChunksUpdated = true;
								}
							} catch (Exception ignored) {
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
				Minecraft.getMinecraft().gameDir, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart
			);
			cachedRegionFileDir.mkdirs();
			File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
			if (cachedRegionFile.exists()) {
				ZipFile zFile = new ZipFile(cachedRegionFile);
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
				BiMap<IBlockState, Integer> hashBiMap = HashBiMap.create();
				Scanner sc = new Scanner(is);

				while (sc.hasNextLine()) {
					BlockStateParser.parseLine(sc.nextLine(), hashBiMap);
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
						} catch (NumberFormatException ignored) {
						}

						is.close();
					}
				}

				zFile.close();
				if (total == this.data.getWidth() * this.data.getHeight() * 18) {
					byteData = new byte[this.data.getWidth() * this.data.getHeight() * 18];
					System.arraycopy(decompressedByteData, 0, byteData, 0, byteData.length);
					this.data.setData(byteData, hashBiMap, version);
					this.empty = false;
					this.dataUpdated = true;
				} else {
					VoxelConstants.getLogger().error("failed to load data from {}", cachedRegionFile.getPath());
				}

				if (version < 2) {
					this.liveChunksUpdated = true;
				}
			}
		} catch (Exception e) {
			VoxelConstants.getLogger().error("Failed to load region file for {},{} in {}/{}{}", this.x, this.z, this.worldNamePathPart, this.subworldNamePathPart, this.dimensionNamePathPart, e);
		}
	}

	private void saveData() {
		if (this.liveChunksUpdated && !this.worldNamePathPart.isEmpty()) {
			ThreadManager.executorService
				.execute(
					() -> {
						CachedRegion.this.threadLock.lock();

						try {
							BiMap<IBlockState, Integer> stateToInt = CachedRegion.this.data.getStateToInt();
							byte[] byteArray = CachedRegion.this.data.getData();
							int length = byteArray.length;
							int square = CachedRegion.this.data.getWidth() * CachedRegion.this.data.getHeight();

							if (length == square * 18) {
								File cachedRegionFileDir = new File(
									Minecraft.getMinecraft().gameDir,
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
									StringBuilder stringBuffer = new StringBuilder();

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
								VoxelConstants.getLogger().error("Data array wrong size: {}for {},{} in {}/{}{}", byteArray.length, CachedRegion.this.x, CachedRegion.this.z, CachedRegion.this.worldNamePathPart, CachedRegion.this.subworldNamePathPart, CachedRegion.this.dimensionNamePathPart);
							}
						} catch (IOException e) {
							VoxelConstants.getLogger().error("Failed to save region file for {},{} in {}/{}{}", CachedRegion.this.x, CachedRegion.this.z, CachedRegion.this.worldNamePathPart, CachedRegion.this.subworldNamePathPart, CachedRegion.this.dimensionNamePathPart, e);
						} finally {
							CachedRegion.this.threadLock.unlock();
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
		int color;

		for (int t = 0; t < 256; t++) {
			for (int s = 0; s < 256; s++) {
				color = this.persistentMap
					.getPixelColor(this.data, this.world, this.blockPos, this.loopBlockPos, this.underground, 8, this.x * 256, this.z * 256, t, s);
				this.image.setRGB(t, s, color);
			}
		}
	}

	private void saveImage() {
		if (!this.empty) {
			File imageFileDir = new File(
				Minecraft.getMinecraft().gameDir,
				"/voxelMap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart + "/images/z1"
			);
			imageFileDir.mkdirs();
			final File imageFile = new File(imageFileDir, this.key + ".png");
			if (this.liveChunksUpdated || !imageFile.exists()) {
				ThreadManager.executorService.execute(() -> {
					CachedRegion.this.threadLock.lock();

					try {
						BufferedImage realBufferedImage = new BufferedImage(CachedRegion.this.width, CachedRegion.this.width, 6);
						byte[] dstArray = ((DataBufferByte) realBufferedImage.getRaster().getDataBuffer()).getData();
						System.arraycopy(CachedRegion.this.image.getData(), 0, dstArray, 0, CachedRegion.this.image.getData().length);
						ImageIO.write(realBufferedImage, "png", imageFile);
					} catch (IOException exception) {
						VoxelConstants.getLogger().error(exception);
					} finally {
						CachedRegion.this.threadLock.unlock();
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
			ThreadManager.executorService.execute(() -> {
				if (CachedRegion.this.threadLock.tryLock()) {
					try {
						CachedRegion.this.compressData();
					} catch (Exception ignored) {
					} finally {
						CachedRegion.this.threadLock.unlock();
					}
				}

				CachedRegion.this.queuedToCompress = false;
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
			} catch (Exception ignored) {
			} finally {
				CachedRegion.this.threadLock.unlock();
			}
		}
	}

	private class RefreshRunnable extends AbstractNotifyingRunnable {
		private final boolean forceCompress;

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
				VoxelConstants.getLogger().error("Exception loading chunk: {}", e.getLocalizedMessage(), e);
			} finally {
				CachedRegion.this.threadLock.unlock();
			}
		}
	}
}
