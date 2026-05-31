package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public abstract class AbstractMapData implements IMapData {
	protected final Object dataLock = new Object();
	private final Object labelLock = new Object();
	private final ArrayList<AbstractMapData.BiomeLabel> labels = new ArrayList<>();
	public AbstractMapData.Point[][] points;
	public ArrayList<AbstractMapData.Segment> segments;
	protected int width;
	protected int height;

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

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
							System.out.println("no biome segment!");
						}

						AbstractMapData.Segment segment = new AbstractMapData.Segment(this.points[x][z]);
						this.segments.add(segment);
						segment.flood();
						if (this.points[x][z].biomeID == -1) {
							System.out.println("created in " + (System.nanoTime() - startTime));
						}
					}
				}
			}
		}
	}

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

	public ArrayList<AbstractMapData.BiomeLabel> getBiomeLabels() {
		synchronized (this.labelLock) {
			return new ArrayList<>(this.labels);
		}
	}

	public static class BiomeLabel {
		public int biomeID = -1;
		public String name = "";
		public int segmentSize = 0;
		public int x = 0;
		public int z = 0;
	}

	private static class Point {
		public int x;
		public int z;
		public boolean inSegment = false;
		public boolean isCandidate = false;
		public int layer = -1;
		public int biomeID;

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

	public class Segment {
		public ArrayList<AbstractMapData.Point> memberPoints;
		public int biomeID;
		public String name = null;
		public int centerX = 0;
		public int centerZ = 0;
		ArrayList<AbstractMapData.Point> currentShell;

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

		public void calculateCenter(boolean horizontalBias) {
			this.calculateCenterOfMass();
			this.morphologicallyErode(horizontalBias);
		}

		public void calculateCenterOfMass() {
			this.calculateCenterOfMass(this.memberPoints);
		}

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

		public void morphologicallyErode(boolean horizontalBias) {
			float labelWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.name) + 8;
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
