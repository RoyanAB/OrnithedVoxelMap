package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base implementation of {@link IMapData} that adds the biome-segmentation
 * logic used to label biomes on the full-screen map.
 *
 * <p>Concrete subclasses provide the per-column storage for height, block
 * state, tint, and light. This class groups adjacent columns sharing the same
 * biome ID into {@link Segment segments} and, via
 * {@link #segmentBiomes()} and {@link #findCenterOfSegments(boolean)}, computes
 * a position for each biome's name label.
 */
public abstract class AbstractMapData implements IMapData {
	protected int width;
	protected int height;
	protected final Object dataLock = new Object();
	private final Object labelLock = new Object();
	public AbstractMapData.Point[][] points;
	public ArrayList<AbstractMapData.Segment> segments;
	private final ArrayList<AbstractMapData.BiomeLabel> labels = new ArrayList<>();

	/**
	 * Returns the width of the map data grid.
	 *
	 * @return the width in columns
	 */
	@Override
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height of the map data grid.
	 *
	 * @return the height in columns
	 */
	@Override
	public int getHeight() {
		return this.height;
	}

	/**
	 * Divides the grid into contiguous {@link Segment segments} of equal
	 * biome.
	 *
	 * <p>Each column becomes a {@link Point}; a flood fill then groups every
	 * adjacent column sharing the same biome ID into a single segment. Columns
	 * whose biome could not be determined (ID 255 or -1) are left out of the
	 * segmentation.
	 */
	public void segmentBiomes() {
		this.points = new AbstractMapData.Point[this.width][this.height];
		this.segments = new ArrayList<>();

		for (int x = 0; x < this.width; x++) {
			for (int z = 0; z < this.height; z++) {
				this.points[x][z] = new Point(x, z, this.getBiomeID(x, z));
			}
		}

		synchronized (this.dataLock) {
			for (int x = 0; x < this.width; x++) {
				for (int z = 0; z < this.height; z++) {
					if (!this.points[x][z].inSegment) {
						long startTime = System.nanoTime();
						if (this.points[x][z].biomeID == -1) {
							VoxelConstants.getLogger().debug("No biome segment!");
						}

						AbstractMapData.Segment segment = new AbstractMapData.Segment(this.points[x][z]);
						this.segments.add(segment);
						segment.flood();
						if (this.points[x][z].biomeID == -1) {
							VoxelConstants.getLogger().debug("Created in {} ns", System.nanoTime() - startTime);
						}
					}
				}
			}
		}
	}

	/**
	 * Computes a label position for every valid segment and collects the
	 * results into {@link #getBiomeLabels()}.
	 *
	 * <p>Each segment's label position is derived from its center of mass and
	 * then refined by morphologically eroding the segment inward so the label
	 * fits within its area.
	 *
	 * @param horizontalBias {@code true} to favor a label area that is wider
	 *                       than tall
	 */
	public void findCenterOfSegments(boolean horizontalBias) {
		if (this.segments != null) {
			for (AbstractMapData.Segment segment : this.segments) {
				if (segment.biomeID != -1) {
					segment.calculateCenter(horizontalBias);
				}
			}
		}

		synchronized (this.labelLock) {
			this.labels.clear();
			if (this.segments != null) {
				for (AbstractMapData.Segment segment : this.segments) {
					if (segment.biomeID != -1) {
						AbstractMapData.BiomeLabel label = new BiomeLabel();
						label.biomeID = segment.biomeID;
						label.name = segment.name;
						label.segmentSize = segment.memberPoints.size();
						label.x = segment.centerX;
						label.z = segment.centerZ;
						this.labels.add(label);
					}
				}
			}
		}
	}

	/**
	 * Returns a snapshot of the biome labels computed by
	 * {@link #findCenterOfSegments(boolean)}.
	 *
	 * @return a copy of the current biome labels
	 */
	public ArrayList<AbstractMapData.BiomeLabel> getBiomeLabels() {
		synchronized (this.labelLock) {
			return new ArrayList<>(this.labels);
		}
	}

	/**
	 * Holds the label information for one biome segment: the biome, its name,
	 * its area (number of columns), and the position where its label is drawn.
	 */
	public static class BiomeLabel {
		public int biomeID = -1;
		public String name = "";
		public int segmentSize = 0;
		public int x = 0;
		public int z = 0;
	}

	/**
	 * Represents a single (x, z) grid column during biome segmentation.
	 *
	 * <p>Tracks the column's biome ID plus temporary state used by the
	 * flood-fill and erosion passes.
	 */
	public static class Point {
		public int x;
		public int z;
		public boolean inSegment = false;
		public boolean isCandidate = false;
		public int layer = -1;
		public int biomeID;

		/**
		 * Creates a point for the given column and biome.
		 *
		 * @param x       the column x coordinate
		 * @param z       the column z coordinate
		 * @param biomeID the biome ID at this column (255 or -1 means unknown)
		 */
		public Point(int x, int z, int biomeID) {
			this.x = x;
			this.z = z;
			if (biomeID == 255 || biomeID == -1) {
				biomeID = -1;
				this.inSegment = true;
			}

			this.biomeID = biomeID;
		}
	}

	/**
	 * A contiguous region of columns sharing the same biome.
	 *
	 * <p>A segment is used to place a single biome name label; the label
	 * position is found by eroding the segment inward from its edges until only
	 * the area large enough to hold the label remains.
	 */
	public class Segment {
		public ArrayList<AbstractMapData.Point> memberPoints;
		ArrayList<AbstractMapData.Point> currentShell;
		public int biomeID;
		public String name = null;
		public int centerX = 0;
		public int centerZ = 0;

		/**
		 * Creates a segment seeded with the given column and resolves the
		 * biome's display name.
		 *
		 * @param point the initial column of the segment
		 */
		public Segment(AbstractMapData.Point point) {
			this.biomeID = point.biomeID;
			Biome biome = null;
			if (this.biomeID != -1) {
				biome = Biome.getBiome(this.biomeID);
			}

			if (biome != null) {
				this.name = biome.getBiomeName();
			}

			if (this.name == null) {
				this.name = "Unknown";
			}

			this.memberPoints = new ArrayList<>();
			this.memberPoints.add(point);
			this.currentShell = new ArrayList<>();
		}

		/**
		 * Flood-fills the segment from its seed column, absorbing every
		 * contiguous same-biome column into {@link #memberPoints} and
		 * collecting the segment's boundary columns into {@code currentShell}.
		 */
		public void flood() {
			ArrayList<AbstractMapData.Point> candidatePoints = new ArrayList<>();
			candidatePoints.add(this.memberPoints.remove(0));

			while (!candidatePoints.isEmpty()) {
				AbstractMapData.Point point = candidatePoints.remove(0);
				point.isCandidate = false;
				if (point.biomeID == this.biomeID) {
					this.memberPoints.add(point);
					point.inSegment = true;
					boolean edge = false;
					if (point.x < AbstractMapData.this.width - 1) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x + 1][point.z];
						if (!neighbor.inSegment && !neighbor.isCandidate) {
							candidatePoints.add(neighbor);
							neighbor.isCandidate = true;
						}

						if (neighbor.biomeID != point.biomeID) {
							edge = true;
						}
					} else {
						edge = true;
					}

					if (point.x > 0) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x - 1][point.z];
						if (!neighbor.inSegment && !neighbor.isCandidate) {
							candidatePoints.add(neighbor);
							neighbor.isCandidate = true;
						}

						if (neighbor.biomeID != point.biomeID) {
							edge = true;
						}
					} else {
						edge = true;
					}

					if (point.z < AbstractMapData.this.height - 1) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x][point.z + 1];
						if (!neighbor.inSegment && !neighbor.isCandidate) {
							candidatePoints.add(neighbor);
							neighbor.isCandidate = true;
						}

						if (neighbor.biomeID != point.biomeID) {
							edge = true;
						}
					} else {
						edge = true;
					}

					if (point.z > 0) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x][point.z - 1];
						if (!neighbor.inSegment && !neighbor.isCandidate) {
							candidatePoints.add(neighbor);
							neighbor.isCandidate = true;
						}

						if (neighbor.biomeID != point.biomeID) {
							edge = true;
						}
					} else {
						edge = true;
					}

					if (edge) {
						point.layer = 0;
						this.currentShell.add(point);
					}
				}
			}
		}

		/**
		 * Computes the segment's label position: the center of mass is
		 * calculated first, then the segment is morphologically eroded to fit
		 * the label.
		 *
		 * @param horizontalBias {@code true} to favor a label area that is
		 *                       wider than tall
		 */
		public void calculateCenter(boolean horizontalBias) {
			this.calculateCenterOfMass();
			this.morphologicallyErode(horizontalBias);
		}

		/**
		 * Sets the center to the average position of all member columns.
		 */
		public void calculateCenterOfMass() {
			this.calculateCenterOfMass(this.memberPoints);
		}

		/**
		 * Sets the center to the average position of the given columns.
		 *
		 * @param points the columns to average
		 */
		public void calculateCenterOfMass(Collection<AbstractMapData.Point> points) {
			this.centerX = 0;
			this.centerZ = 0;

			for (AbstractMapData.Point point : points) {
				this.centerX = this.centerX + point.x;
				this.centerZ = this.centerZ + point.z;
			}

			this.centerX = this.centerX / points.size();
			this.centerZ = this.centerZ / points.size();
		}

		/**
		 * Moves the center to the column in {@code points} that is nearest to
		 * the current center.
		 *
		 * @param points the candidate columns
		 */
		public void calculateClosestPointToCenter(Collection<AbstractMapData.Point> points) {
			int distanceSquared = AbstractMapData.this.width * AbstractMapData.this.width + AbstractMapData.this.height * AbstractMapData.this.height;
			AbstractMapData.Point centerPoint = null;

			for (AbstractMapData.Point point : points) {
				int pointDistanceSquared = (point.x - this.centerX) * (point.x - this.centerX) + (point.z - this.centerZ) * (point.z - this.centerZ);
				if (pointDistanceSquared < distanceSquared) {
					distanceSquared = pointDistanceSquared;
					centerPoint = point;
				}
			}

			this.centerX = centerPoint.x;
			this.centerZ = centerPoint.z;
		}

		/**
		 * Erodes the segment inward until the surviving area is just large
		 * enough to hold the biome name label, then snaps the center to the
		 * surviving column nearest the center of mass.
		 *
		 * @param horizontalBias {@code true} to favor a label area that is
		 *                       wider than tall
		 */
		public void morphologicallyErode(boolean horizontalBias) {
			float labelWidth = VoxelConstants.getMinecraft().fontRenderer.getStringWidth(this.name) + 8;
			float multi = AbstractMapData.this.width / 32;
			float shellWidth = 2.0F;
			float labelPadding = labelWidth / 16.0F * multi / shellWidth;
			int layer = 0;

			while (!this.currentShell.isEmpty() && layer < labelPadding) {
				this.currentShell = this.getNextShell(this.currentShell, ++layer, horizontalBias);
			}

			if (!this.currentShell.isEmpty()) {
				ArrayList<AbstractMapData.Point> remainingPoints = new ArrayList<>();

				for (AbstractMapData.Point point : this.memberPoints) {
					if (point.layer < 0 || point.layer == layer) {
						remainingPoints.add(point);
					}
				}

				this.calculateClosestPointToCenter(remainingPoints);
			}
		}

		/**
		 * Computes the next inner shell of columns (one step inward from the
		 * segment boundary), assigning each column the given layer number.
		 *
		 * <p>If no new columns are found, the center is set to the shell's
		 * center of mass and then snapped to the nearest shell column.
		 *
		 * @param pointsToCheck  the columns forming the current shell
		 * @param layer          the layer number to assign
		 * @param horizontalBias {@code true} to advance wider horizontally than
		 *                       vertically
		 * @return the columns of the next shell (empty when the segment is
		 *         exhausted)
		 */
		public ArrayList<AbstractMapData.Point> getNextShell(Collection<AbstractMapData.Point> pointsToCheck, int layer, boolean horizontalBias) {
			int layerWidth = horizontalBias ? 2 : 1;
			int layerHeight = horizontalBias ? 1 : 2;
			ArrayList<AbstractMapData.Point> nextShell = new ArrayList<>();

			for (AbstractMapData.Point point : pointsToCheck) {
				if (point.x < AbstractMapData.this.width - layerWidth) {
					boolean foundEdge = false;

					for (int t = layerWidth; t > 0; t--) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x + t][point.z];
						if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
							neighbor.layer = layer;
							if (!foundEdge) {
								foundEdge = true;
								nextShell.add(neighbor);
							}
						}
					}
				}

				if (point.x >= layerWidth) {
					boolean foundEdge = false;

					for (int t = layerWidth; t > 0; t--) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x - t][point.z];
						if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
							neighbor.layer = layer;
							if (!foundEdge) {
								foundEdge = true;
								nextShell.add(neighbor);
							}
						}
					}
				}

				if (point.z < AbstractMapData.this.height - layerHeight) {
					boolean foundEdge = false;

					for (int t = layerHeight; t > 0; t--) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x][point.z + t];
						if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
							neighbor.layer = layer;
							if (!foundEdge) {
								foundEdge = true;
								nextShell.add(neighbor);
							}
						}
					}
				}

				if (point.z >= layerHeight) {
					boolean foundEdge = false;

					for (int t = layerHeight; t > 0; t--) {
						AbstractMapData.Point neighbor = AbstractMapData.this.points[point.x][point.z - t];
						if (neighbor.biomeID == point.biomeID && neighbor.layer < 0) {
							neighbor.layer = layer;
							if (!foundEdge) {
								foundEdge = true;
								nextShell.add(neighbor);
							}
						}
					}
				}
			}

			if (!nextShell.isEmpty()) {
				return nextShell;
			}

			this.calculateCenterOfMass(pointsToCheck);
			this.calculateClosestPointToCenter(pointsToCheck);
			return nextShell;
		}
	}
}
