package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.render.model.BakedQuad;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockModel {
    ArrayList<BlockModel.BlockFace> faces = new ArrayList<>();
    BlockModel.BlockVertex[] longestSide;
    float failedToLoadX;
    float failedToLoadY;

    public BlockModel(List<BakedQuad> quads) {
        BakedQuad quad = null;

        for (BakedQuad var9 : quads) {
            BlockModel.BlockFace face = new BlockModel.BlockFace(var9.getVertexData());
            if (face.isClockwise && !face.isVertical) {
                this.faces.add(face);
            }
        }

        Collections.sort(this.faces);
        this.longestSide = new BlockModel.BlockVertex[2];
        float greatestLength = 0.0F;
        BlockModel.BlockFace face = null;

        for (BlockModel.BlockFace var12 : this.faces) {
            float uDiff = var12.longestSide[0].u - var12.longestSide[1].u;
            float vDiff = var12.longestSide[0].v - var12.longestSide[1].v;
            float segmentLength = (float) Math.sqrt(uDiff * uDiff + vDiff * vDiff);
            if (segmentLength > greatestLength) {
                greatestLength = segmentLength;
                this.longestSide = var12.longestSide;
            }
        }
    }

    public void setFailedToLoadCoords(float failedToLoadX, float failedToLoadY) {
        this.failedToLoadX = failedToLoadX;
        this.failedToLoadY = failedToLoadY;
    }

    public int numberOfFaces() {
        return this.faces.size();
    }

    public ArrayList<BlockModel.BlockFace> getFaces() {
        return this.faces;
    }

    public BufferedImage getImage(BufferedImage terrainImage) {
        float terrainImageAspectRatio = (float) terrainImage.getWidth() / terrainImage.getHeight();
        float longestSideUV = Math.max(
                Math.abs(this.longestSide[0].u - this.longestSide[1].u), Math.abs(this.longestSide[0].v - this.longestSide[1].v) / terrainImageAspectRatio
        );
        float modelImageWidthUV = longestSideUV
                / Math.max(Math.abs(this.longestSide[0].x - this.longestSide[1].x), Math.abs(this.longestSide[0].z - this.longestSide[1].z));
        int modelImageWidth = Math.round(modelImageWidthUV * terrainImage.getWidth());
        BufferedImage modelImage = new BufferedImage(modelImageWidth, modelImageWidth, 6);
        Graphics2D g2 = modelImage.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());
        g2.dispose();
        BlockModel.BlockFace face = null;

        for (BlockModel.BlockFace var32 : this.faces) {
            float minU = var32.getMinU();
            float maxU = var32.getMaxU();
            float minV = var32.getMinV();
            float maxV = var32.getMaxV();
            float minX = var32.getMinX();
            float maxX = var32.getMaxX();
            float minZ = var32.getMinZ();
            float maxZ = var32.getMaxZ();
            if (this.similarEnough(minU, minV, this.failedToLoadX, this.failedToLoadY)) {
                return null;
            }

            int faceImageX = Math.round(minX * modelImage.getWidth());
            int faceImageY = Math.round(minZ * modelImage.getHeight());
            int faceImageWidth = Math.round(maxX * modelImage.getWidth()) - faceImageX;
            int faceImageHeight = Math.round(maxZ * modelImage.getHeight()) - faceImageY;
            if (faceImageWidth == 0) {
                if (faceImageX > modelImageWidth - 1) {
                    faceImageX = modelImageWidth - 1;
                }

                faceImageWidth = 1;
            }

            if (faceImageHeight == 0) {
                if (faceImageY > modelImageWidth - 1) {
                    faceImageY = modelImageWidth - 1;
                }

                faceImageHeight = 1;
            }

            int faceImageU = Math.round(minU * terrainImage.getWidth());
            int faceImageV = Math.round(minV * terrainImage.getHeight());
            int faceImageUVWidth = Math.round(maxU * terrainImage.getWidth()) - faceImageU;
            int faceImageUVHeight = Math.round(maxV * terrainImage.getHeight()) - faceImageV;
            if (faceImageUVWidth == 0) {
                faceImageUVWidth = 1;
            }

            if (faceImageUVHeight == 0) {
                faceImageUVHeight = 1;
            }

            BufferedImage faceImage = terrainImage.getSubimage(faceImageU, faceImageV, faceImageUVWidth, faceImageUVHeight);
            if (faceImageWidth != faceImageUVWidth || faceImageHeight != faceImageUVHeight) {
                if (faceImageWidth == faceImageUVHeight && faceImageHeight == faceImageUVWidth) {
                    BufferedImage tmp = new BufferedImage(faceImageWidth, faceImageHeight, 6);
                    AffineTransform transform = new AffineTransform();
                    transform.translate(faceImage.getHeight() / 2, faceImage.getWidth() / 2);
                    transform.rotate(Math.PI / 2);
                    transform.translate(-faceImage.getWidth() / 2, -faceImage.getHeight() / 2);
                    AffineTransformOp op = new AffineTransformOp(transform, 1);
                    faceImage = op.filter(faceImage, tmp);
                } else {
                    BufferedImage tmp = new BufferedImage(faceImageWidth, faceImageHeight, 6);
                    g2 = tmp.createGraphics();
                    g2.drawImage(faceImage, 0, 0, faceImageWidth, faceImageHeight, null);
                    g2.dispose();
                    faceImage = tmp;
                }
            }

            g2 = modelImage.createGraphics();
            g2.drawImage(faceImage, faceImageX, faceImageY, null);
            g2.dispose();
        }

        return modelImage;
    }

    private boolean similarEnough(float a, float b, float one, float two) {
        boolean similar = Math.abs(a - one) < 1.0E-4;
        return similar && Math.abs(b - two) < 1.0E-4;
    }

    public class BlockFace implements Comparable<BlockModel.BlockFace> {
        BlockModel.BlockVertex[] vertices;
        boolean isHorizontal;
        boolean isVertical;
        boolean isClockwise;
        float yLevel;
        BlockModel.BlockVertex[] longestSide;

        BlockFace(int[] values) {
            int arraySize = values.length;
            int intsPerVertex = arraySize / 4;
            this.vertices = new BlockModel.BlockVertex[4];

            for (int t = 0; t < 4; t++) {
                float x = Float.intBitsToFloat(values[t * intsPerVertex]);
                float y = Float.intBitsToFloat(values[t * intsPerVertex + 1]);
                float z = Float.intBitsToFloat(values[t * intsPerVertex + 2]);
                float u = Float.intBitsToFloat(values[t * intsPerVertex + 4]);
                float v = Float.intBitsToFloat(values[t * intsPerVertex + 5]);
                this.vertices[t] = BlockModel.this.new BlockVertex(x, y, z, u, v);
            }

            this.isHorizontal = this.checkIfHorizontal();
            this.isVertical = this.checkIfVertical();
            this.isClockwise = this.checkIfClockwise();
            this.yLevel = this.calculateY();
            this.longestSide = this.getLongestSide();
        }

        private boolean checkIfHorizontal() {
            boolean isHorizontal = true;
            float initialY = this.vertices[0].y;

            for (int t = 1; t < this.vertices.length; t++) {
                if (this.vertices[t].y != initialY) {
                    isHorizontal = false;
                    break;
                }
            }

            return isHorizontal;
        }

        private boolean checkIfVertical() {
            boolean allSameX = true;
            boolean allSameZ = true;
            float initialX = this.vertices[0].x;
            float initialZ = this.vertices[0].z;

            for (int t = 1; t < this.vertices.length; t++) {
                if (this.vertices[t].x != initialX) {
                    allSameX = false;
                }

                if (this.vertices[t].z != initialZ) {
                    allSameZ = false;
                }
            }

            return allSameX || allSameZ;
        }

        private boolean checkIfClockwise() {
            float sum = 0.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                sum += (this.vertices[t == this.vertices.length - 1 ? 0 : t + 1].x - this.vertices[t].x)
                        * (this.vertices[t == this.vertices.length - 1 ? 0 : t + 1].z + this.vertices[t].z);
            }

            return sum > 0.0F;
        }

        private float calculateY() {
            float sum = 0.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                sum += this.vertices[t].y;
            }

            return sum / this.vertices.length;
        }

        private BlockModel.BlockVertex[] getLongestSide() {
            float greatestLength = -1.0F;
            BlockModel.BlockVertex[] longestSide = new BlockModel.BlockVertex[0];

            for (int t = 0; t < this.vertices.length; t++) {
                float uDiff = this.vertices[t].u - this.vertices[t == this.vertices.length - 1 ? 0 : t + 1].u;
                float vDiff = this.vertices[t].v - this.vertices[t == this.vertices.length - 1 ? 0 : t + 1].v;
                float segmentLength = (float) Math.sqrt(uDiff * uDiff + vDiff * vDiff);
                if (segmentLength > greatestLength) {
                    greatestLength = segmentLength;
                    longestSide = new BlockModel.BlockVertex[]{this.vertices[t], this.vertices[t == this.vertices.length - 1 ? 0 : t + 1]};
                }
            }

            return longestSide;
        }

        public float getMinX() {
            float minX = 1.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].x < minX) {
                    minX = this.vertices[t].x;
                }
            }

            return minX;
        }

        public float getMaxX() {
            float maxX = 0.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].x > maxX) {
                    maxX = this.vertices[t].x;
                }
            }

            return maxX;
        }

        public float getMinZ() {
            float minZ = 1.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].z < minZ) {
                    minZ = this.vertices[t].z;
                }
            }

            return minZ;
        }

        public float getMaxZ() {
            float maxZ = 0.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].z > maxZ) {
                    maxZ = this.vertices[t].z;
                }
            }

            return maxZ;
        }

        public float getMinU() {
            float minU = 1.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].u < minU) {
                    minU = this.vertices[t].u;
                }
            }

            return minU;
        }

        public float getMaxU() {
            float maxU = 0.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].u > maxU) {
                    maxU = this.vertices[t].u;
                }
            }

            return maxU;
        }

        public float getMinV() {
            float minV = 1.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].v < minV) {
                    minV = this.vertices[t].v;
                }
            }

            return minV;
        }

        public float getMaxV() {
            float maxV = 0.0F;

            for (int t = 0; t < this.vertices.length; t++) {
                if (this.vertices[t].v > maxV) {
                    maxV = this.vertices[t].v;
                }
            }

            return maxV;
        }

        public int compareTo(BlockModel.BlockFace compareTo) {
            if (this.yLevel > compareTo.yLevel) {
                return 1;
            } else {
                return this.yLevel < compareTo.yLevel ? -1 : 0;
            }
        }
    }

    private class BlockVertex {
        float x;
        float y;
        float z;
        float u;
        float v;

        BlockVertex(float x, float y, float z, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }
}
