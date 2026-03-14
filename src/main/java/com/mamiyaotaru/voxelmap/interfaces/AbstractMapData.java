package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractMapData implements IMapData {
    public Point[][] points;
    public ArrayList<Segment> segments;
    protected int width;
    protected int height;
    protected Object dataLock = new Object();
    private final Object labelLock = new Object();
    private final ArrayList<BiomeLabel> labels = new ArrayList<>();

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public void segmentBiomes() {
        this.points = new Point[this.width][this.height];
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

                        Segment segment = new Segment(this.points[x][z]);
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
            for (Segment segment : this.segments) {
                if (segment.biomeID != -1) {
                    segment.calculateCenter(horizontalBias);
                }
            }
        }

        synchronized (this.labelLock) {
            this.labels.clear();
            if (this.segments != null) {
                for (Segment segmentx : this.segments) {
                    if (segmentx.biomeID != -1) {
                        BiomeLabel label = new BiomeLabel();
                        label.biomeID = segmentx.biomeID;
                        label.name = segmentx.name;
                        label.segmentSize = segmentx.memberPoints.size();
                        label.x = segmentx.centerX;
                        label.z = segmentx.centerZ;
                        this.labels.add(label);
                    }
                }
            }
        }
    }

    public ArrayList<BiomeLabel> getBiomeLabels() {
        ArrayList<BiomeLabel> labelsToReturn = new ArrayList<>();
        synchronized (this.labelLock) {
            labelsToReturn.addAll(this.labels);
            return labelsToReturn;
        }
    }

    public class BiomeLabel {
        public int biomeID = -1;
        public String name = "";
        public int segmentSize = 0;
        public int x = 0;
        public int z = 0;
    }

    private class Point {
        public int x;
        public int z;
        public boolean inSegment = false;
        public boolean isCandidate = false;
        public int layer = -1;
        public int biomeID = -1;

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
        public ArrayList<Point> memberPoints;
        public int biomeID;
        public String name = null;
        public int centerX = 0;
        public int centerZ = 0;
        ArrayList<Point> currentShell;

        public Segment(Point point) {
            this.biomeID = point.biomeID;
            Biome biome = null;
            if (this.biomeID != -1) {
                biome = Biome.getBiome(this.biomeID, null);
            }

            if (biome != null) {
                this.name = biome.getDisplayName().getFormattedText();
            }

            if (this.name == null) {
                this.name = "Unknown";
            }

            this.memberPoints = new ArrayList<>();
            this.memberPoints.add(point);
            this.currentShell = new ArrayList<>();
        }

        public void flood() {
            ArrayList<Point> candidatePoints = new ArrayList<>();
            candidatePoints.add(this.memberPoints.remove(0));

            while (candidatePoints.size() > 0) {
                Point point = candidatePoints.remove(0);
                point.isCandidate = false;
                if (point.biomeID == this.biomeID) {
                    this.memberPoints.add(point);
                    point.inSegment = true;
                    boolean edge = false;
                    if (point.x < AbstractMapData.this.width - 1) {
                        Point neighbor = AbstractMapData.this.points[point.x + 1][point.z];
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
                        Point neighborx = AbstractMapData.this.points[point.x - 1][point.z];
                        if (!neighborx.inSegment && !neighborx.isCandidate) {
                            candidatePoints.add(neighborx);
                            neighborx.isCandidate = true;
                        }

                        if (neighborx.biomeID != point.biomeID) {
                            edge = true;
                        }
                    } else {
                        edge = true;
                    }

                    if (point.z < AbstractMapData.this.height - 1) {
                        Point neighborxx = AbstractMapData.this.points[point.x][point.z + 1];
                        if (!neighborxx.inSegment && !neighborxx.isCandidate) {
                            candidatePoints.add(neighborxx);
                            neighborxx.isCandidate = true;
                        }

                        if (neighborxx.biomeID != point.biomeID) {
                            edge = true;
                        }
                    } else {
                        edge = true;
                    }

                    if (point.z > 0) {
                        Point neighborxxx = AbstractMapData.this.points[point.x][point.z - 1];
                        if (!neighborxxx.inSegment && !neighborxxx.isCandidate) {
                            candidatePoints.add(neighborxxx);
                            neighborxxx.isCandidate = true;
                        }

                        if (neighborxxx.biomeID != point.biomeID) {
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

        public void calculateCenterOfMass(Collection<Point> points) {
            this.centerX = 0;
            this.centerZ = 0;

            for (Point point : points) {
                this.centerX = this.centerX + point.x;
                this.centerZ = this.centerZ + point.z;
            }

            this.centerX = this.centerX / points.size();
            this.centerZ = this.centerZ / points.size();
        }

        public void calculateClosestPointToCenter(Collection<Point> points) {
            int distanceSquared = AbstractMapData.this.width * AbstractMapData.this.width + AbstractMapData.this.height * AbstractMapData.this.height;
            Point centerPoint = null;

            for (Point point : points) {
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
            float labelWidth = Minecraft.getInstance().fontRenderer.getStringWidth(this.name) + 8;
            float multi = AbstractMapData.this.width / 32;
            float shellWidth = 2.0F;
            float labelPadding = labelWidth / 16.0F * multi / shellWidth;
            int layer = 0;

            while (this.currentShell.size() > 0 && layer < labelPadding) {
                this.currentShell = this.getNextShell(this.currentShell, ++layer, horizontalBias);
            }

            if (this.currentShell.size() > 0) {
                ArrayList<Point> remainingPoints = new ArrayList<>();

                for (Point point : this.memberPoints) {
                    if (point.layer < 0 || point.layer == layer) {
                        remainingPoints.add(point);
                    }
                }

                this.calculateClosestPointToCenter(remainingPoints);
            }
        }

        public ArrayList<Point> getNextShell(Collection<Point> pointsToCheck, int layer, boolean horizontalBias) {
            int layerWidth = horizontalBias ? 2 : 1;
            int layerHeight = horizontalBias ? 1 : 2;
            ArrayList<Point> nextShell = new ArrayList<>();

            for (Point point : pointsToCheck) {
                if (point.x < AbstractMapData.this.width - layerWidth) {
                    boolean foundEdge = false;

                    for (int t = layerWidth; t > 0; t--) {
                        Point neighbor = AbstractMapData.this.points[point.x + t][point.z];
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

                    for (int tx = layerWidth; tx > 0; tx--) {
                        Point neighbor = AbstractMapData.this.points[point.x - tx][point.z];
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

                    for (int txx = layerHeight; txx > 0; txx--) {
                        Point neighbor = AbstractMapData.this.points[point.x][point.z + txx];
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

                    for (int txxx = layerHeight; txxx > 0; txxx--) {
                        Point neighbor = AbstractMapData.this.points[point.x][point.z - txxx];
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

            if (nextShell.size() > 0) {
                return nextShell;
            }

            this.calculateCenterOfMass(pointsToCheck);
            this.calculateClosestPointToCenter(pointsToCheck);
            return nextShell;
        }
    }
}
