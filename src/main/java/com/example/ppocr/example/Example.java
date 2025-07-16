package com.example.ppocr.example;

import com.example.ppocr.OCRDebugHelper;
import com.example.ppocr.OCRException;
import com.example.ppocr.OCRResult;
import com.example.ppocr.PaddleOCR;

import java.io.File;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        // 检查命令行参数
        if (args.length < 1) {
            System.out.println("Usage: java Example <image_path> [--debug]");
            System.exit(1);
        }

        String imagePath = args[0];
        boolean debug = args.length > 1 && args[1].equals("--debug");
        String outputDir = "/Users/xiebaiyuan/Downloads/output/ocr_output";

        // 确保输出目录存在
        new File(outputDir).mkdirs();

        System.out.println("开始处理图片: " + imagePath);
        System.out.println("结果将保存在: " + outputDir);

        try {
            // 创建PaddleOCR实例并配置
            PaddleOCR paddleOCR = new PaddleOCR()
                    .withLanguage("ch")      // 设置为中文识别
                    .withTimeout(120)        // 设置超时时间为120秒，考虑到首次运行时需要下载模型
                    .withDebug(debug)        // 设置调试模式
                    .withGpu(false)          // 使用CPU模式
                    .withOutputDir(outputDir); // 设置输出目录

            // 输出将要执行的命令
            System.out.println("执行命令: " + paddleOCR.getCommandString(imagePath));
            System.out.println("正在执行OCR识别...");

            // 执行OCR识别
            List<OCRResult> results = paddleOCR.recognize(imagePath);

            // 输出结果
            System.out.println("\n识别结果:");
            if (results.isEmpty()) {
                System.out.println("未识别到任何文本");
            } else {
                for (OCRResult result : results) {
                    System.out.println("文本: " + result.getText());
                    System.out.println("位置: " + result.getBoundingBox());
                    System.out.println("-------------------");
                }
                System.out.println("总共识别到 " + results.size() + " 个文本区域");
            }

        } catch (OCRException e) {
            System.err.println("OCR识别失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
