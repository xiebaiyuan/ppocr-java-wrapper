# PaddleOCR Java Wrapper

这个项目提供了一个Java包装器，用于调用PaddleOCR命令行工具进行OCR识别。

## 前提条件

1. 安装Python环境（3.6+）
2. 安装PaddleOCR:
   ```bash
   pip install paddlepaddle paddleocr
   ```
3. Java 8+
4. Maven 3.6+（用于构建Maven项目）或Gradle 6.0+（用于构建Gradle项目）

## 使用方法

### 验证PaddleOCR安装

在使用Java包装器之前，请先验证PaddleOCR命令行工具能正常工作：

```bash
# 测试PaddleOCR命令行
paddleocr ocr -i /Users/xiebaiyuan/Downloads/11.jpg  --use_doc_orientation_classify False --use_doc_unwarping False --use_textline_orientation False --device cpu```

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
    .withPythonPath("/path/to/python")      // 指定Python解释器路径
    .withPaddleOcrScript("paddleocr")       // 指定PaddleOCR脚本
    .withTimeout(120)                       // 设置超时时间（秒）
    .withOutputDir("/path/to/output")       // 设置输出目录
    .withGpu(true)                          // 启用GPU加速
    .withDocOrientationClassify(true)       // 启用文档方向分类
    .withDocUnwarping(true)                 // 启用文档校正
    .withTextlineOrientation(true)          // 启用文本行方向检测
    .withDebug(true);                       // 启用调试模式
```

### 使用配置文件

您可以通过properties文件配置PaddleOCR：

```bash
# 使用配置文件运行示例程序
java -jar ppocr-java-wrapper.jar /path/to/image.jpg /path/to/config.properties
```

配置文件示例 (config.properties):

```properties
pythonPath=/usr/local/bin/python3
outputDir=/home/user/paddleocr_output
debug=true
useGpu=false
language=ch
timeout=120
```

## 构建项目

### 使用Maven构建

```bash
# 使用Maven构建
mvn clean package

# 运行示例程序
mvn exec:java -Dexec.args="/path/to/image.jpg"

# 或者使用生成的jar包运行
java -jar target/ppocr-java-wrapper-1.0-SNAPSHOT.jar /path/to/image.jpg
```

### 使用Gradle构建

```bash
# 使用Gradle构建
./gradlew build

# 运行示例程序
./gradlew run --args="/path/to/image.jpg"

# 创建可分发的包
./gradlew distZip
```

## 故障排除

1. **Python路径问题**：确保指定了正确的Python解释器路径，特别是在使用虚拟环境时
2. **PaddleOCR安装**：验证PaddleOCR已成功安装：
   ```bash
   python -m pip list | grep paddleocr
   ```
3. **命令行测试**：尝试直接在命令行运行PaddleOCR:
   ```bash
   python -m paddleocr ocr -i /path/to/image.jpg --lang ch
   ```
4. **参数问题**：如果遇到命令行参数问题，请查看帮助文档：
   ```bash
   python -m paddleocr ocr --help
   ```
5. **超时问题**：增加超时时间（特别是首次运行时需要下载模型）
6. **权限问题**：确保输出目录具有写入权限

## 注意事项

1. 此包装器需要PaddleOCR CLI工具在系统上正确安装
2. 首次运行时，PaddleOCR可能会下载模型文件，这会延长处理时间
3. PaddleOCR的命令行参数可能随版本更新而变化，如遇问题请查看最新文档
4. 在不同操作系统中，Python路径可能需要不同的配置
