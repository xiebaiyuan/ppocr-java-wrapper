package com.example.ppocr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 帮助调试PaddleOCR命令行工具的辅助类
 */
public class OCRDebugHelper {

    /**
     * 直接在命令行执行PaddleOCR命令并打印输出
     * @param imagePath 图片路径
     */
    public static void testDirectCommand(String imagePath) {
        try {
            List<String> command = new ArrayList<>();
            command.add("/Users/xiebaiyuan/miniforge3/envs/ppocr/bin/python");
            command.add("-m");
            command.add("paddleocr");
            command.add("ocr");
            command.add("-i");
            command.add(imagePath);
            command.add("--use_doc_orientation_classify");
            command.add("False");
            command.add("--use_doc_unwarping");
            command.add("False");
            command.add("--use_textline_orientation");
            command.add("False");
            command.add("--device");
            command.add("cpu");
            command.add("--save_path");
            command.add("/Users/xiebaiyuan/Downloads/output/ocr_output");

            ProcessBuilder pb = new ProcessBuilder(command);
            // 重定向错误流到标准输出
            pb.redirectErrorStream(true);

            System.out.println("执行测试命令: " + String.join(" ", pb.command()));
            Process process = pb.start();

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println("命令输出:");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\n命令执行完毕，退出码: " + exitCode);

        } catch (IOException | InterruptedException e) {
            System.err.println("测试命令执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取PaddleOCR版本信息
     */
    public static String getPaddleOCRVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "-m", "pip", "show", "paddleocr");
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Version:")) {
                    return line.substring("Version:".length()).trim();
                }
                output.append(line).append("\n");
            }

            process.waitFor();
            return "未找到版本信息\n" + output.toString();

        } catch (IOException | InterruptedException e) {
            return "获取版本失败: " + e.getMessage();
        }
    }
}
