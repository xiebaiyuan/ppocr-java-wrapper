package com.xiebaiyuan.ppocr.example;

import com.xiebaiyuan.ppocr.OCRException;
import com.xiebaiyuan.ppocr.OCRResult;
import com.xiebaiyuan.ppocr.PaddleOCR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Example {
    public static void main(String[] args) {
        // 检查命令行参数
        if (args.length < 1) {
            System.out.println("Usage: java Example <image_path> [config_file]");
            System.out.println("Example: java Example /path/to/image.jpg /path/to/config.properties");
            System.exit(1);
        }

        String imagePath = args[0];
        String configFile = args.length > 1 ? args[1] : null;

        // 默认配置
        String pythonPath = "python";
        String outputDir = System.getProperty("user.home") + "/paddleocr_output";
        boolean debug = false;
        boolean useGpu = false;
        String language = "ch";
        int timeout = 60;
        String textDetLimitType = "min";
        int textDetLimitSideLen = 736;

        // 如果提供了配置文件，则从配置文件中加载设置
        if (configFile != null) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);

                pythonPath = props.getProperty("pythonPath", pythonPath);
                outputDir = props.getProperty("outputDir", outputDir);
                debug = Boolean.parseBoolean(props.getProperty("debug", "false"));
                useGpu = Boolean.parseBoolean(props.getProperty("useGpu", "false"));
                language = props.getProperty("language", language);
                timeout = Integer.parseInt(props.getProperty("timeout", "60"));
                textDetLimitType = props.getProperty("textDetLimitType", textDetLimitType);
                textDetLimitSideLen = Integer.parseInt(props.getProperty("textDetLimitSideLen", String.valueOf(textDetLimitSideLen)));
            } catch (IOException e) {
                System.err.println("无法读取配置文件: " + e.getMessage());
                System.err.println("将使用默认配置");
            }
        }

        // 确保输出目录存在
        new File(outputDir).mkdirs();

        System.out.println("开始处理图片: " + imagePath);
        System.out.println("结果将保存在: " + outputDir);
        System.out.println("使用Python路径: " + pythonPath);

        try {
            // 创建PaddleOCR实例并配置
            PaddleOCR paddleOCR = new PaddleOCR()
                    .withPythonPath(pythonPath)       // 设置Python路径
                    .withLanguage(language)           // 设置语言
                    .withTimeout(timeout)             // 设置超时时间
                    .withDebug(debug)                 // 设置调试模式
                    .withGpu(useGpu)                  // 设置GPU使用
                    .withOutputDir(outputDir)         // 设置输出目录
                    .withTextDetLimitType(textDetLimitType) // 新增
                    .withTextDetLimitSideLen(textDetLimitSideLen); // 新增

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
