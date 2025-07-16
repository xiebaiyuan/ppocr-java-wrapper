package com.xiebaiyuan.ppocr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的JSON解析器，用于解析PaddleOCR输出的JSON文件
 * 这是一个替代方案，当org.json库不可用时使用
 */
public class JsonParser {

    /**
     * 从JSON文件中提取OCR结果
     *
     * @param jsonFilePath JSON文件路径
     * @param debug 是否启用调试输出
     * @return OCR结果列表
     * @throws IOException 如果读取文件失败
     */
    public static List<OCRResult> parseJsonFile(String jsonFilePath, boolean debug) throws IOException {
        List<OCRResult> results = new ArrayList<>();

        // 读取JSON文件内容
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        String jsonContent = content.toString();

        if (debug) {
            System.out.println("读取的JSON内容: " + jsonContent.substring(0, Math.min(200, jsonContent.length())) + "...");
        }

        // 提取rec_texts数组
        List<String> texts = extractStringArray(jsonContent, "\"rec_texts\"\\s*:\\s*\\[([^\\]]+)\\]");
        if (debug) {
            System.out.println("提取到 " + texts.size() + " 个文本项");
        }

        // 提取rec_polys数组
        List<List<Point>> allPolygons = extractPolygons(jsonContent, "\"rec_polys\"\\s*:\\s*\\[([^\\]]+)\\]\\s*,");
        if (debug) {
            System.out.println("提取到 " + allPolygons.size() + " 个多边形");
        }

        // 匹配文本和多边形
        int minSize = Math.min(texts.size(), allPolygons.size());
        for (int i = 0; i < minSize; i++) {
            String text = texts.get(i);
            List<Point> polygon = allPolygons.get(i);

            results.add(new OCRResult(text, polygon));
        }

        return results;
    }

    /**
     * 提取字符串数组
     */
    private static List<String> extractStringArray(String jsonContent, String pattern) {
        List<String> result = new ArrayList<>();

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(jsonContent);

        if (m.find()) {
            String arrayContent = m.group(1);
            String[] items = arrayContent.split(",");

            for (String item : items) {
                item = item.trim();
                // 移除引号
                if (item.startsWith("\"") && item.endsWith("\"")) {
                    item = item.substring(1, item.length() - 1);
                }
                result.add(item);
            }
        }

        return result;
    }

    /**
     * 提取多边形坐标
     */
    private static List<List<Point>> extractPolygons(String jsonContent, String pattern) {
        List<List<Point>> allPolygons = new ArrayList<>();

        // 查找rec_polys部分
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher m = p.matcher(jsonContent);

        if (m.find()) {
            String polysContent = m.group(1);

            // 提取每个多边形
            Pattern polyPattern = Pattern.compile("\\[\\s*\\[([^\\]]+)\\]\\s*\\]");
            Matcher polyMatcher = polyPattern.matcher(polysContent);

            while (polyMatcher.find()) {
                String polygonContent = polyMatcher.group(1);
                List<Point> polygon = new ArrayList<>();

                // 提取每个点
                Pattern pointPattern = Pattern.compile("\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
                Matcher pointMatcher = pointPattern.matcher(polygonContent);

                while (pointMatcher.find()) {
                    double x = Double.parseDouble(pointMatcher.group(1));
                    double y = Double.parseDouble(pointMatcher.group(2));
                    polygon.add(new Point(x, y));
                }

                allPolygons.add(polygon);
            }
        }

        return allPolygons;
    }
}
