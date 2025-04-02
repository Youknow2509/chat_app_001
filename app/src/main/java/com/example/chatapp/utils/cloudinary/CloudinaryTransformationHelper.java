package com.example.chatapp.utils.cloudinary;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for creating Cloudinary transformation parameters
 * Makes it easy to build complex transformations for images and videos
 */
public class CloudinaryTransformationHelper {
    private final Map<String, String> transformations;

    /**
     * Create a new transformation helper with empty transformations
     */
    public CloudinaryTransformationHelper() {
        this.transformations = new HashMap<>();
    }

    /**
     * Get the map of transformations
     * @return Map of transformation key-value pairs
     */
    @NonNull
    public Map<String, String> getTransformations() {
        return transformations;
    }

    /**
     * Add a custom transformation parameter
     * @param key Transformation key
     * @param value Transformation value
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper addTransformation(String key, String value) {
        transformations.put(key, value);
        return this;
    }

    /**
     * Add width and height resize transformation
     * @param width Width in pixels
     * @param height Height in pixels
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper resize(int width, int height) {
        transformations.put("w", String.valueOf(width));
        transformations.put("h", String.valueOf(height));
        return this;
    }

    /**
     * Set the width while maintaining aspect ratio
     * @param width Width in pixels
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper width(int width) {
        transformations.put("w", String.valueOf(width));
        return this;
    }

    /**
     * Set the height while maintaining aspect ratio
     * @param height Height in pixels
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper height(int height) {
        transformations.put("h", String.valueOf(height));
        return this;
    }

    /**
     * Add a crop transformation
     * @param cropMode Crop mode (fill, scale, fit, etc.)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper crop(String cropMode) {
        transformations.put("c", cropMode);
        return this;
    }

    /**
     * Set the image quality
     * @param quality Quality percentage (1-100)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper quality(int quality) {
        transformations.put("q", String.valueOf(quality));
        return this;
    }

    /**
     * Set the format of the output file
     * @param format Format (jpg, png, webp, etc.)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper format(String format) {
        transformations.put("f", format);
        return this;
    }

    /**
     * Add a radius for rounded corners or circle crop
     * @param radius Radius value (use "max" for circle)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper radius(String radius) {
        transformations.put("r", radius);
        return this;
    }

    /**
     * Add effect transformation
     * @param effect Effect name
     * @param value Effect value
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper effect(String effect, String value) {
        transformations.put("e", effect + ":" + value);
        return this;
    }

    /**
     * Add border to image
     * @param width Border width
     * @param color Border color (RGB hex)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper border(int width, String color) {
        transformations.put("bo", width + "px_solid_" + color);
        return this;
    }

    /**
     * Add background color for transparent images
     * @param color Background color (RGB hex)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper background(String color) {
        transformations.put("b", color);
        return this;
    }

    /**
     * Add a named transformation
     * @param name Name of predefined transformation
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper named(String name) {
        transformations.put("t", name);
        return this;
    }

    /**
     * Add gravity parameter for cropping
     * @param gravity Gravity value (center, north, face, etc.)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper gravity(String gravity) {
        transformations.put("g", gravity);
        return this;
    }

    /**
     * Add angle for rotation
     * @param angle Rotation angle in degrees
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper rotate(int angle) {
        transformations.put("a", String.valueOf(angle));
        return this;
    }

    /**
     * Add overlay image
     * @param publicId Public ID of overlay image
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper overlay(String publicId) {
        transformations.put("l", publicId);
        return this;
    }

    /**
     * Add text overlay
     * @param text Text to overlay
     * @param font Font family
     * @param size Font size
     * @param color Text color
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper textOverlay(String text, String font, int size, String color) {
        transformations.put("l", "text:" + font + "_" + size + ":" + text);
        transformations.put("co", color);
        return this;
    }

    /**
     * Add automatic format detection (deliver the best format for browser)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper autoFormat() {
        transformations.put("f", "auto");
        return this;
    }

    /**
     * Add automatic quality optimization
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper autoQuality() {
        transformations.put("q", "auto");
        return this;
    }

    /**
     * Helper method to create thumbnail transformation
     * @param width Thumbnail width
     * @param height Thumbnail height
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper thumbnail(int width, int height) {
        return crop("thumb").resize(width, height).gravity("center");
    }

    /**
     * Helper method to create a profile picture transformation (circle crop)
     * @param size Size of the profile picture
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper profilePicture(int size) {
        return crop("fill").resize(size, size).gravity("face").radius("max");
    }

    /**
     * Helper method to apply responsive image sizing
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper responsive() {
        transformations.put("c", "scale");
        transformations.put("w", "auto");
        transformations.put("dpr", "auto");
        return this;
    }

    /**
     * Helper method to optimize for web delivery
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper optimize() {
        return autoFormat().autoQuality();
    }

    /**
     * Helper method to create a blurred version of an image
     * @param strength Blur strength (1-2000)
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper blur(int strength) {
        return effect("blur", String.valueOf(strength));
    }

    /**
     * Helper method to apply a grayscale effect
     * @return This helper for chaining
     */
    public CloudinaryTransformationHelper grayscale() {
        return effect("grayscale", "true");
    }
}