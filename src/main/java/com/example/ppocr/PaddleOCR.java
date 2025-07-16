package com.example.ppocr;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Java wrapper for PaddleOCR CLI tool
 */
public class PaddleOCR {
    private String pythonPath = "/Users/xiebaiyuan/miniforge3/envs/ppocr/bin/python";
    private String paddleOcrScript = "paddleocr";
    private int timeoutSeconds = 30;
    private boolean useGpu = false;
    private String language = "ch";
    private boolean debug = false;
    private String outputDir = "/Users/xiebaiyuan/Downloads/output/ocr_output";

    /**
     * Constructor with default settings
     */
    public PaddleOCR() {
        // Default constructor
    }

    /**
     * Set custom Python executable path
     */
    public PaddleOCR withPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
        return this;
    }

    /**
     * Set custom PaddleOCR script path
     */
    public PaddleOCR withPaddleOcrScript(String paddleOcrScript) {
        this.paddleOcrScript = paddleOcrScript;
        return this;
    }

    /**
     * Set command timeout in seconds
     */
    public PaddleOCR withTimeout(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    /**
     * Enable or disable GPU usage
     */
    public PaddleOCR withGpu(boolean useGpu) {
        this.useGpu = useGpu;
        return this;
    }

    /**
     * Set recognition language
     */
    public PaddleOCR withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Enable or disable debug mode
     */
    public PaddleOCR withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Set output directory for OCR results
     */
    public PaddleOCR withOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * Perform OCR on the given image file
     *
     * @param imagePath Path to the image file
     * @return List of OCR results
     * @throws OCRException If OCR process fails
     */
    public List<OCRResult> recognize(String imagePath) throws OCRException {
        List<String> command = buildCommand(imagePath);

        try {
            // 确保输出目录存在
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            // 重定向错误流到标准输出，以便捕获所有输出
            processBuilder.redirectErrorStream(true);

            if (debug) {
                System.out.println("启动进程: " + String.join(" ", command));
            }

            Process process = processBuilder.start();

            // 收集输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> outputLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
                if (debug) {
                    System.out.println("进程输出: " + line);
                }
            }

            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new OCRException("OCR process timed out after " + timeoutSeconds + " seconds");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = String.join("\n", outputLines);
                throw new OCRException("OCR process failed with exit code " + exitCode + ": " + errorOutput);
            }

            // 从文件名中提取基本名称
            String baseFileName = new File(imagePath).getName();
            String baseName = baseFileName.substring(0, baseFileName.lastIndexOf('.'));
            String jsonFilePath = outputDir + "/" + baseName + "_res.json";

            if (debug) {
                System.out.println("尝试从文件读取OCR结果: " + jsonFilePath);
            }

            // 等待确保文件被写入
            File jsonFile = new File(jsonFilePath);
            int waitCount = 0;
            while (!jsonFile.exists() && waitCount < 10) {
                Thread.sleep(500); // 等待500毫秒
                waitCount++;
                if (debug) {
                    System.out.println("等待JSON文件生成，尝试次数: " + waitCount);
                }
            }

            if (!jsonFile.exists()) {
                throw new OCRException("OCR结果文件未找到: " + jsonFilePath);
            }

            // 使用org.json库解析JSON文件
            return parseJsonFile(jsonFilePath);
        } catch (IOException | InterruptedException e) {
            throw new OCRException("Failed to execute OCR command", e);
        }
    }

    private List<String> buildCommand(String imagePath) {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add("-m");
        command.add(paddleOcrScript);

        // 添加"ocr"子命令
        command.add("ocr");

        // 使用-i或--input参数代替--image_dir
        command.add("-i");
        command.add(imagePath);

        // 添加文档方向识别参数，每个参数都需要单独添加
        command.add("--use_doc_orientation_classify");
        command.add("False");

        command.add("--use_doc_unwarping");
        command.add("False");

        command.add("--use_textline_orientation");
        command.add("False");

        // 保存路径
        command.add("--save_path");
        command.add(outputDir);

        // 添加GPU参数（如果启用）
        if (useGpu) {
            command.add("--device");
            command.add("gpu");
        } else {
            command.add("--device");
            command.add("cpu");
        }

        return command;
    }

    /**
     * 输出命令行，用于调试
     */
    public String getCommandString(String imagePath) {
        List<String> command = buildCommand(imagePath);
        return String.join(" ", command);
    }

    /**
     * 使用org.json库解析JSON文件
     *
     * @param jsonFilePath JSON文件路径
     * @return OCR结果列表
     * @throws OCRException 如果解析失败
     */
    private List<OCRResult> parseJsonFile(String jsonFilePath) throws OCRException {
        List<OCRResult> results = new ArrayList<>();

        try {
            // 读取JSON文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            if (debug) {
                System.out.println("读取的JSON内容长度: " + jsonContent.length());
            }

            // 解析JSON
            JSONObject jsonObject = new JSONObject(jsonContent);

            // 获取识别的文本列表
            JSONArray recTexts = jsonObject.getJSONArray("rec_texts");
            JSONArray recPolys = jsonObject.getJSONArray("rec_polys");

            if (debug) {
                System.out.println("识别到的文本数量: " + recTexts.length());
                System.out.println("识别到的多边形数量: " + recPolys.length());
            }

            // 处理所有文本和坐标
            for (int i = 0; i < recTexts.length(); i++) {
                String text = recTexts.getString(i);

                // 获取多边形坐标
                JSONArray polyCoords = recPolys.getJSONArray(i);
                List<Point> boundingBox = new ArrayList<>();

                for (int j = 0; j < polyCoords.length(); j++) {
                    JSONArray point = polyCoords.getJSONArray(j);
                    double x = point.getDouble(0);
                    double y = point.getDouble(1);
                    boundingBox.add(new Point(x, y));
                }

                // 创建OCR结果对象
                results.add(new OCRResult(text, boundingBox));
            }

            return results;
        } catch (IOException e) {
            throw new OCRException("读取JSON文件失败: " + jsonFilePath, e);
        } catch (Exception e) {
            throw new OCRException("解析JSON文件失败: " + e.getMessage(), e);
        }
    }
}
