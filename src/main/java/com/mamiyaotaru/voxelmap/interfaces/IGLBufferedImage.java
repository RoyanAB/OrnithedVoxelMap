package com.mamiyaotaru.voxelmap.interfaces;

/**
 * Abstraction over an image that is backed by an OpenGL texture.
 *
 * <p>Pixel data is kept in a byte buffer and uploaded to the graphics card on
 * demand. Implementations extend {@code java.awt.image.BufferedImage} so the
 * image can also be manipulated with standard AWT calls.
 */
public interface IGLBufferedImage {
	/**
	 * Returns the OpenGL texture ID for this image, or 0 if it has not yet
	 * been uploaded.
	 *
	 * @return the GL texture ID
	 */
	int getIndex();

	/**
	 * Returns the width of the image in pixels.
	 *
	 * @return the image width
	 */
	int getWidth();

	/**
	 * Returns the height of the image in pixels.
	 *
	 * @return the image height
	 */
	int getHeight();

	/**
	 * Deletes the underlying OpenGL texture and resets its ID to 0, freeing
	 * the graphics memory associated with this image.
	 */
	void baleet();

	/**
	 * Uploads the current pixel data to the OpenGL texture, generating a new
	 * texture ID on first use.
	 */
	void write();

	/**
	 * Fills the image with fully transparent pixels and uploads the result.
	 */
	void blank();

	/**
	 * Sets the color of a single pixel (without uploading).
	 *
	 * @param x       the pixel's x coordinate
	 * @param y       the pixel's y coordinate
	 * @param color24 the ARGB color to set
	 */
	void setRGB(int x, int y, int color24);

	/**
	 * Shifts the image contents horizontally by the given number of pixels.
	 * A positive offset shifts content toward the left edge.
	 *
	 * @param offset the number of pixels to shift
	 */
	void moveX(int offset);

	/**
	 * Shifts the image contents vertically by the given number of pixels.
	 * A positive offset shifts content toward the top edge.
	 *
	 * @param offset the number of pixels to shift
	 */
	void moveY(int offset);
}
