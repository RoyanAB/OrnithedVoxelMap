package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IDimensionManager;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

import java.util.ArrayList;
import java.util.Comparator;

public class DimensionManager implements IDimensionManager {
	private final ArrayList<Dimension> dimensions;
	private final IVoxelMap master;

	public DimensionManager(IVoxelMap master) {
		this.master = master;
		this.dimensions = new ArrayList<>();
	}

	public static int getDimensionIDfromProvider(WorldProvider provider) {
		return provider.getDimensionType().getId();
	}

	@Override
	public ArrayList<Dimension> getDimensions() {
		return this.dimensions;
	}

	@Override
	public void populateDimensions() {
		this.dimensions.clear();

		for (int t = -1; t <= 1; t++) {
			String name;
			WorldProvider provider = null;

			try {
				provider = DimensionType.getById(t).createDimension();
			} catch (Exception ignored) {
			}

			if (provider != null) {
				try {
					name = provider.getDimensionType().getName();
				} catch (Exception e) {
					name = "failedToLoad";
				}

				Dimension dim = new Dimension(name, t);
				this.dimensions.add(dim);
			}
		}

		for (Waypoint pt : this.master.getWaypointManager().getWaypoints()) {
			for (Integer t : pt.dimensions) {
				if (this.getDimensionByID(t) == null) {
					String name;
					WorldProvider provider = null;

					try {
						provider = DimensionType.getById(t).createDimension();
					} catch (Exception ignored) {
					}

					if (provider != null) {
						try {
							name = provider.getDimensionType().getName();
						} catch (Exception e) {
							name = "failedToLoad";
						}

						Dimension dim = new Dimension(name, t);
						this.dimensions.add(dim);
					}
				}
			}
		}

		this.dimensions.sort(Comparator.comparingInt(Dimension::getID));
	}

	@Override
	public void enteredDimension(int ID) {
		Dimension dim = this.getDimensionByID(ID);
		if (dim == null) {
			dim = new Dimension("notLoaded", ID);
			this.dimensions.add(dim);
			this.dimensions.sort(Comparator.comparingInt(Dimension::getID));
		}

		if (dim.getName().equals("notLoaded") || dim.getName().equals("failedToLoad")) {
			try {
				dim.setName(Minecraft.getMinecraft().world.provider.getDimensionType().getName() + " " + ID);
			} catch (Exception e) {
				dim.setName("dimension " + ID + "(" + Minecraft.getMinecraft().world.provider.getClass().getSimpleName() + ")");
			}
		}
	}

	@Override
	public Dimension getDimensionByID(int ID) {
		for (Dimension dim : this.dimensions) {
			if (dim.getID() == ID) {
				return dim;
			}
		}

		return null;
	}
}
