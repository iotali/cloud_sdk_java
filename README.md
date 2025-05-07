# IoT 云平台 Java SDK

这是一个用于与IoT云平台交互的Java SDK，提供了设备管理相关的API调用功能。

## 功能特性

- 设备注册与管理
- 设备状态查询
- 设备详情获取
- 批量设备状态查询
- 发送RRPC消息到设备
- 发送自定义指令到设备

## 系统要求

- Java 8+
- Maven 3.6+

## 安装

1. 克隆代码库:

```bash
git clone https://github.com/yourusername/cloud_sdk_java.git
cd cloud_sdk_java
```

2. 使用Maven进行安装:

```bash
mvn clean install
```

3. 在您的项目中添加依赖:

```xml
<dependency>
    <groupId>com.iot</groupId>
    <artifactId>cloud-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

以下是SDK的基本使用方法:

```java
import com.iot.sdk.IoTSdk;
import com.iot.sdk.client.IoTClient;
import com.iot.sdk.device.DeviceManager;
import com.google.gson.JsonObject;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        // 初始化SDK
        String baseUrl = "https://api.iot-platform.com"; // 替换为实际的API地址
        String token = "your-api-token";                 // 替换为实际的API令牌
        
        IoTClient client = IoTSdk.createClient(baseUrl, token);
        DeviceManager deviceManager = IoTSdk.createDeviceManager(client);
        
        // 注册设备
        JsonObject registerResult = deviceManager.registerDevice(
            "your-product-key",  // 产品密钥
            "device123",         // 设备名称（可选）
            "测试设备"            // 设备显示名称（可选）
        );
        
        // 查询设备状态
        JsonObject statusResult = deviceManager.getDeviceStatus(
            "device123",  // 设备名称
            null          // 设备ID（与设备名称二选一即可）
        );
        
        // 发送自定义指令
        JsonObject commandResult = deviceManager.sendCustomCommand(
            "device123",              // 设备名称
            "{\"command\": \"open\"}" // 指令内容（JSON格式）
        );
    }
}
```

## 主要API

### 创建客户端

```java
IoTClient client = IoTSdk.createClient(baseUrl, token);
DeviceManager deviceManager = IoTSdk.createDeviceManager(client);
```

### 设备管理

#### 注册设备

```java
JsonObject result = deviceManager.registerDevice(productKey, deviceName, nickName);
```

#### 查询设备详情

```java
JsonObject result = deviceManager.getDeviceDetail(deviceName, deviceId);
```

#### 查询设备状态

```java
JsonObject result = deviceManager.getDeviceStatus(deviceName, deviceId);
```

#### 批量查询设备状态

```java
List<String> deviceNameList = Arrays.asList("device1", "device2", "device3");
JsonObject result = deviceManager.batchGetDeviceStatus(deviceNameList, null);
```

#### 发送RRPC消息

```java
JsonObject result = deviceManager.sendRrpcMessage(deviceName, productKey, messageContent, timeout);
```

#### 发送自定义指令

```java
JsonObject result = deviceManager.sendCustomCommand(deviceName, messageContent);
```

## 错误处理

SDK中的方法可能会抛出以下异常:

- `IllegalArgumentException`: 参数错误
- `IOException`: 网络请求错误
- 其他异常: JSON解析错误等

建议使用try-catch处理这些异常:

```java
try {
    JsonObject result = deviceManager.getDeviceStatus(deviceName, null);
} catch (IllegalArgumentException e) {
    System.err.println("参数错误: " + e.getMessage());
} catch (IOException e) {
    System.err.println("网络请求错误: " + e.getMessage());
} catch (Exception e) {
    System.err.println("未知错误: " + e.getMessage());
}
```

## 示例

SDK包含了一个完整的示例应用程序，展示了各API的使用方法。可以通过以下方式运行:

```bash
mvn exec:java -Dexec.mainClass="com.iot.sdk.examples.DeviceExample"
```

## 日志记录

SDK使用SLF4J和Logback进行日志记录。默认配置会将日志输出到控制台和logs目录下的文件中。

您可以通过覆盖classpath中的logback.xml配置文件来自定义日志行为。

## 许可证

这个项目使用MIT许可证 - 详情请参见[LICENSE](LICENSE)文件。

## 联系方式

如有问题或建议，请联系我们的支持团队: support@iot-platform.com 