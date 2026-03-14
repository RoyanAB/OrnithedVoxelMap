package com.mamiyaotaru.voxelmap.textures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import com.mamiyaotaru.voxelmap.util.MathHelperExtra;
import net.minecraft.util.math.MathHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Stitcher {
	private final Set<Stitcher.Holder> setStitchHolders = Sets.newHashSetWithExpectedSize(256);
	private final List<Stitcher.Slot> stitchSlots = Lists.newArrayListWithCapacity(256);
	private final int maxWidth;
	private final int maxHeight;
	private final int maxTileDimension;
	private int currentWidth = 0;
	private int currentHeight = 0;
	private int currentWidthToPowerOfTwo = 0;
	private int currentHeightToPowerOfTwo = 0;

	public Stitcher(int maxWidth, int maxHeight, int maxTileDimension) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.maxTileDimension = maxTileDimension;
	}

	public int getCurrentImageWidth() {
		return this.currentWidthToPowerOfTwo;
	}

	public int getCurrentImageHeight() {
		return this.currentHeightToPowerOfTwo;
	}

	public int getCurrentWidth() {
		return this.currentWidth;
	}

	public int getCurrentHeight() {
		return this.currentHeight;
	}

	public void addSprite(Sprite icon) {
		Stitcher.Holder holder = new Stitcher.Holder(icon);
		if (this.maxTileDimension > 0) {
			holder.setNewDimension(this.maxTileDimension);
		}

		this.setStitchHolders.add(holder);
	}

	public void doStitch() {
		Stitcher.Holder[] stitchHoldersArray = this.setStitchHolders.toArray(new Stitcher.Holder[this.setStitchHolders.size()]);
		Arrays.sort(stitchHoldersArray);
		Stitcher.Holder[] tempStitchHoldersArray = stitchHoldersArray;
		int stitcherHoldersArrayLength = stitchHoldersArray.length;
		if (stitcherHoldersArrayLength > 0) {
			Stitcher.Holder holder = tempStitchHoldersArray[0];
			int iconWidth = holder.width;
			int iconHeight = holder.height;
			boolean allSameSize = true;

			for (int stitcherHolderIndex = 1; stitcherHolderIndex < stitcherHoldersArrayLength && allSameSize; stitcherHolderIndex++) {
				holder = tempStitchHoldersArray[stitcherHolderIndex];
				allSameSize = allSameSize && holder.width == iconWidth && holder.height == iconHeight;
			}

			if (allSameSize) {
				int nextPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(stitcherHoldersArrayLength);
				int power = Integer.numberOfTrailingZeros(nextPowerOfTwo);
				int width = (int) Math.pow(2.0, Math.ceil(power / 2.0)) * iconWidth;
				int height = (int) Math.pow(2.0, Math.floor(power / 2.0)) * iconHeight;
				this.currentWidth = width;
				this.currentHeight = height;
				this.currentWidthToPowerOfTwo = width;
				this.currentHeightToPowerOfTwo = height;
				Stitcher.Slot slot = new Stitcher.Slot(0, 0, this.currentWidth, this.currentHeight);
				this.stitchSlots.add(slot);
			}
		}

		for (int stitcherHolderIndex = 0; stitcherHolderIndex < stitcherHoldersArrayLength; stitcherHolderIndex++) {
			Stitcher.Holder holder = tempStitchHoldersArray[stitcherHolderIndex];
			if (!this.allocateSlot(holder)) {
				String errorString = String.format(
					"Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?",
					holder.getAtlasSprite().getIconName(),
					holder.getAtlasSprite().getIconWidth(),
					holder.getAtlasSprite().getIconHeight()
				);
				throw new StitcherException(holder, errorString);
			}
		}

		this.currentWidthToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentWidth);
		this.currentHeightToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentHeight);
		this.setStitchHolders.clear();
	}

	public void doStitchNew() {
		Stitcher.Holder[] stitchHoldersArray = this.setStitchHolders.toArray(new Stitcher.Holder[this.setStitchHolders.size()]);
		Arrays.sort(stitchHoldersArray);
		Stitcher.Holder[] tempStitchHoldersArray = stitchHoldersArray;
		int stitcherHoldersArrayLength = stitchHoldersArray.length;

		for (int stitcherHolderIndex = 0; stitcherHolderIndex < stitcherHoldersArrayLength; stitcherHolderIndex++) {
			Stitcher.Holder holder = tempStitchHoldersArray[stitcherHolderIndex];
			if (!this.allocateSlot(holder)) {
				String errorString = String.format(
					"Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?",
					holder.getAtlasSprite().getIconName(),
					holder.getAtlasSprite().getIconWidth(),
					holder.getAtlasSprite().getIconHeight()
				);
				throw new StitcherException(holder, errorString);
			}
		}

		this.currentWidthToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentWidth);
		this.currentHeightToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentHeight);
		this.setStitchHolders.clear();
	}

	public List<Sprite> getStitchSlots() {
		ArrayList<Stitcher.Slot> listOfStitchSlots = Lists.newArrayList();

		for (Stitcher.Slot slot : this.stitchSlots) {
			slot.getAllStitchSlots(listOfStitchSlots);
		}

		ArrayList<Sprite> spritesList = Lists.newArrayList();

		for (Stitcher.Slot stitcherSlot : listOfStitchSlots) {
			Stitcher.Holder stitcherHolder = stitcherSlot.getStitchHolder();
			Sprite icon = stitcherHolder.getAtlasSprite();
			icon.initSprite(this.currentWidthToPowerOfTwo, this.currentHeightToPowerOfTwo, stitcherSlot.getOriginX(), stitcherSlot.getOriginY());
			spritesList.add(icon);
		}

		return spritesList;
	}

	private boolean allocateSlot(Stitcher.Holder holder) {
		for (int stitcherSlotsIndex = 0; stitcherSlotsIndex < this.stitchSlots.size(); stitcherSlotsIndex++) {
			if (this.stitchSlots.get(stitcherSlotsIndex).addSlot(holder)) {
				return true;
			}
		}

		return this.expandAndAllocateSlot(holder);
	}

	private boolean expandAndAllocateSlot(Stitcher.Holder holder) {
		int expandBy = holder.getWidth();
		int currentWidthToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentWidth);
		int currentHeightToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentHeight);
		int possibleNewWidthToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentWidth + expandBy);
		int possibleNewHeightToPowerOfTwo = MathHelperExtra.smallestEncompassingPowerOfTwo(this.currentHeight + expandBy);
		boolean isRoomToExpandRight = possibleNewWidthToPowerOfTwo <= this.maxWidth;
		boolean isRoomToExpandDown = possibleNewHeightToPowerOfTwo <= this.maxHeight;
		if (!isRoomToExpandRight && !isRoomToExpandDown) {
			return false;
		}

		boolean widthWouldChange = currentWidthToPowerOfTwo != possibleNewWidthToPowerOfTwo;
		boolean heightWouldChange = currentHeightToPowerOfTwo != possibleNewHeightToPowerOfTwo;
		boolean shouldExpandRight;
		if (widthWouldChange ^ heightWouldChange) {
			shouldExpandRight = !widthWouldChange;
		} else {
			shouldExpandRight = isRoomToExpandRight && currentWidthToPowerOfTwo <= currentHeightToPowerOfTwo;
		}

		if (MathHelperExtra.smallestEncompassingPowerOfTwo((shouldExpandRight ? this.currentWidth : this.currentHeight) + expandBy)
			> (shouldExpandRight ? this.maxWidth : this.maxHeight)) {
			return false;
		}

		Stitcher.Slot slot;
		if (shouldExpandRight) {
			if (this.currentHeight == 0) {
				this.currentHeight = holder.getHeight();
			}

			slot = new Stitcher.Slot(this.currentWidth, 0, holder.getWidth(), this.currentHeight);
			this.currentWidth = this.currentWidth + holder.getWidth();
		} else {
			slot = new Stitcher.Slot(0, this.currentHeight, this.currentWidth, holder.getHeight());
			this.currentHeight = this.currentHeight + holder.getHeight();
		}

		if (!slot.addSlot(holder)) {
			String errorString = String.format(
				"Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?",
				holder.getAtlasSprite().getIconName(),
				holder.getAtlasSprite().getIconWidth(),
				holder.getAtlasSprite().getIconHeight()
			);
			System.err.println(errorString);
		}

		this.stitchSlots.add(slot);
		return true;
	}

	public class Holder implements Comparable<Stitcher.Holder> {
		private final Sprite icon;
		private final int width;
		private final int height;
		private float scaleFactor = 1.0F;

		public Holder(Sprite icon) {
			this.icon = icon;
			this.width = icon.getIconWidth();
			this.height = icon.getIconHeight();
		}

		public Sprite getAtlasSprite() {
			return this.icon;
		}

		public int getWidth() {
			return (int) (this.width * this.scaleFactor);
		}

		public int getHeight() {
			return (int) (this.height * this.scaleFactor);
		}

		public void setNewDimension(int newDimension) {
			if (this.width > newDimension && this.height > newDimension) {
				this.scaleFactor = (float) newDimension / Math.min(this.width, this.height);
			}
		}

		public int compareTo(Stitcher.Holder compareTo) {
			int var2;
			if (this.getHeight() == compareTo.getHeight()) {
				if (this.getWidth() == compareTo.getWidth()) {
					if (this.icon.getIconName() == null) {
						return compareTo.icon.getIconName() == null ? 0 : -1;
					}

					Collator collator = I18nUtils.getLocaleAwareCollator();
					return collator.compare(this.icon.getIconName(), compareTo.icon.getIconName());
				}

				var2 = this.getWidth() < compareTo.getWidth() ? 1 : -1;
			} else {
				var2 = this.getHeight() < compareTo.getHeight() ? 1 : -1;
			}

			return var2;
		}
	}

	public class Slot {
		private final int originX;
		private final int originY;
		private final int width;
		private final int height;
		private int failsAt = Stitcher.this.maxWidth;
		private List<Stitcher.Slot> subSlots;
		private Stitcher.Holder holder;

		public Slot(int originX, int originY, int width, int height) {
			this.originX = originX;
			this.originY = originY;
			this.width = width;
			this.height = height;
		}

		public Stitcher.Holder getStitchHolder() {
			return this.holder;
		}

		public int getOriginX() {
			return this.originX;
		}

		public int getOriginY() {
			return this.originY;
		}

		public boolean addSlot(Stitcher.Holder holder) {
			if (holder.width >= this.failsAt) {
				return false;
			}

			if (this.holder != null) {
				this.failsAt = 0;
				return false;
			}

			int holderWidth = holder.getWidth();
			int holderHeight = holder.getHeight();
			if (holderWidth > this.width || holderHeight > this.height) {
				this.failsAt = holder.width;
				return false;
			}

			if (holderWidth == this.width && holderHeight == this.height) {
				this.holder = holder;
				return true;
			}

			if (this.subSlots == null) {
				this.subSlots = Lists.newArrayListWithCapacity(1);
				this.subSlots.add(Stitcher.this.new Slot(this.originX, this.originY, holderWidth, holderHeight));
				int excessWidth = this.width - holderWidth;
				int excessHeight = this.height - holderHeight;
				if (excessHeight > 0 && excessWidth > 0) {
					int var6 = Math.max(this.height, excessWidth);
					int var7 = Math.max(this.width, excessHeight);
					if (var6 > var7) {
						this.subSlots.add(Stitcher.this.new Slot(this.originX, this.originY + holderHeight, holderWidth, excessHeight));
						this.subSlots.add(Stitcher.this.new Slot(this.originX + holderWidth, this.originY, excessWidth, this.height));
					} else {
						this.subSlots.add(Stitcher.this.new Slot(this.originX + holderWidth, this.originY, excessWidth, holderHeight));
						this.subSlots.add(Stitcher.this.new Slot(this.originX, this.originY + holderHeight, this.width, excessHeight));
					}
				} else if (excessWidth == 0) {
					this.subSlots.add(Stitcher.this.new Slot(this.originX, this.originY + holderHeight, holderWidth, excessHeight));
				} else if (excessHeight == 0) {
					this.subSlots.add(Stitcher.this.new Slot(this.originX + holderWidth, this.originY, excessWidth, holderHeight));
				}
			}

			for (Stitcher.Slot slot : this.subSlots) {
				if (slot.addSlot(holder)) {
					return true;
				}
			}

			this.failsAt = holder.width;
			return false;
		}

		public void getAllStitchSlots(List<Stitcher.Slot> listOfStitchSlots) {
			if (this.holder != null) {
				listOfStitchSlots.add(this);
			} else if (this.subSlots != null) {
				for (Stitcher.Slot slot : this.subSlots) {
					slot.getAllStitchSlots(listOfStitchSlots);
				}
			}
		}
	}
}
