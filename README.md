# PaddleOCR Java Wrapper

这个项目提供了一个Java包装器，用于调用PaddleOCR命令行工具进行OCR识别。

## 前提条件

1. 安装Python环境（3.6+）
2. 安装PaddleOCR:
   ```bash
   pip install paddlepaddle paddleocr
   ```
3. Java 8+
4. Maven 3.6+（用于构建项目）

## 使用方法

### 验证PaddleOCR安装

在使用Java包装器之前，请先验证PaddleOCR命令行工具能正常工作：

```bash
# 测试PaddleOCR命令行
python -m paddleocr ocr -i /path/to/image.jpg --lang ch
```

### 基本用法

```java
import com.example.ppocr.OCRException;
import com.example.ppocr.OCRResult;
import com.example.ppocr.PaddleOCR;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        try {
            // 创建PaddleOCR实例
            PaddleOCR paddleOCR = new PaddleOCR();
            
            // 执行OCR识别
            List<OCRResult> results = paddleOCR.recognize("/path/to/image.jpg");
            
            // 处理结果
            for (OCRResult result : results) {
                System.out.println("文本: " + result.getText());
                System.out.println("位置: " + result.getBoundingBox());
            }
            
        } catch (OCRException e) {
            e.printStackTrace();
        }
    }
}
```

### 高级配置

```java
PaddleOCR paddleOCR = new PaddleOCR()
    .withPythonPath("/usr/local/bin/python3")  // 指定Python解释器路径
    .withPaddleOcrScript("paddleocr")          // 指定PaddleOCR脚本
    .withLanguage("en")                        // 设置识别语言（默认为"ch"中文）
    .withTimeout(60)                           // 设置超时时间（秒）
    .withOutputDir("/path/to/output")          // 设置输出目录
    .withGpu(true);                            // 启用GPU加速
```

## 构建项目

```bash
# 使用Maven构建
mvn clean package

# 运行示例程序
mvn exec:java -Dexec.args="/path/to/image.jpg"

# 或者使用生成的jar包运行
java -jar target/ppocr-java-wrapper-1.0-SNAPSHOT.jar /path/to/image.jpg
```

## 故障排除

1. 确保Python环境变量正确设置
2. 验证PaddleOCR已成功安装：
   ```bash
   python -m pip list | grep paddleocr
   ```
3. 尝试直接在命令行运行PaddleOCR:
   ```bash
   python -m paddleocr ocr -i /path/to/image.jpg --lang ch
   ```
4. 如果遇到命令行参数问题，请查看帮助文档：
   ```bash
   python -m paddleocr ocr --help
   ```
5. 检查错误信息中的详细信息
6. 增加超时时间或考虑使用GPU模式提高性能

## 注意事项

1. 此包装器需要PaddleOCR CLI工具在系统上正确安装
2. 首次运行时，PaddleOCR可能会下载模型文件，这会延长处理时间
3. PaddleOCR的命令行参数可能随版本更新而变化，如遇问题请查看最新文档
