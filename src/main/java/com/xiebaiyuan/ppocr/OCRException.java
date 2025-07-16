package com.xiebaiyuan.ppocr;

/**
 * Exception thrown when OCR operations fail
 */
public class OCRException extends Exception {
    public OCRException(String message) {
        super(message);
    }

    public OCRException(String message, Throwable cause) {
        super(message, cause);
    }
}
