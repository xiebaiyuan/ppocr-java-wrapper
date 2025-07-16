package com.xiebaiyuan.ppocr;

import java.util.List;

/**
 * Represents an OCR recognition result
 */
public class OCRResult {
    private final String text;
    private final List<Point> boundingBox;

    public OCRResult(String text, List<Point> boundingBox) {
        this.text = text;
        this.boundingBox = boundingBox;
    }

    /**
     * Get the recognized text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the bounding box coordinates
     */
    public List<Point> getBoundingBox() {
        return boundingBox;
    }

    @Override
    public String toString() {
        return "OCRResult{" +
               "text='" + text + '\'' +
               ", boundingBox=" + boundingBox +
               '}';
    }
}
