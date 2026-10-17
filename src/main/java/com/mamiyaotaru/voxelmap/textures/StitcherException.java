package com.mamiyaotaru.voxelmap.textures;

public class StitcherException extends RuntimeException {
	public StitcherException(Stitcher.Holder holder, String message) {
		super(message);
	}
}
