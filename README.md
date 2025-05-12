# IoT云平台SDK (Java)

这是一个用于连接和管理IoT设备的Java SDK，提供了与IoT云平台交互的简便方法。

## SDK结构

SDK采用模块化设计，主要包含以下组件：

- **IoTClient**: 核心客户端类，处理API请求、认证和基础通信
- **DeviceManager**: 设备管理模块，提供设备相关的所有操作
- **Utils**: 工具函数集，提供格式化、数据处理等辅助功能

## 功能特性

- 认证管理
  - 通过token直接认证
  - **新增：** 通过应用凭证(appId/appSecret)自动获取token
- 设备管理
  - 设备注册
  - 设备详情查询
  - 设备状态查询
  - 批量设备状态查询
- 远程控制
  - RRPC消息发送
  - 自定义指令下发（异步）

## 安装要求

1. Java 8 或更高版本
2. Maven 3.6 或更高版本
3. 添加依赖：

```xml
<dependency>
    <groupId>com.iot</groupId>
    <artifactId>iot-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

### 1. 创建客户端和设备管理器

#### 方式一：使用token创建客户端（传统方式）

```java
import com.iot.sdk.IoTSdk;
import com.iot.sdk.client.IoTClient;
import com.iot.sdk.device.DeviceManager;

// 创建IoT客户端
IoTClient client = IoTSdk.createClient(
    "https://your-iot-platform-url",
    "your-auth-token"
);

// 创建设备管理器
DeviceManager deviceManager = IoTSdk.createDeviceManager(client);
```

#### 方式二：使用应用凭证创建客户端（推荐方式）

```java
import com.iot.sdk.IoTSdk;
import com.iot.sdk.client.IoTClient;
import com.iot.sdk.device.DeviceManager;

// 使用应用凭证自动获取token并创建客户端
IoTClient client = IoTSdk.createClientFromCredentials(
    "https://your-iot-platform-url",
    "your-app-id",
    "your-app-secret"
);

// 创建设备管理器
DeviceManager deviceManager = IoTSdk.createDeviceManager(client);
```

### 2. 设备注册

```java
// 注册设备
JsonObject response = deviceManager.registerDevice(
    "your-product-key",
    "your-device-name",  // 可选
    "设备显示名称"  // 可选
);

// 检查结果
if (client.checkResponse(response)) {
    JsonObject deviceInfo = response.getAsJsonObject("data");
    System.out.println("设备ID: " + deviceInfo.get("deviceId").getAsString());
    System.out.println("设备密钥: " + deviceInfo.get("deviceSecret").getAsString());
}
```

### 3. 查询设备详情

```java
// 通过设备名称查询
JsonObject response = deviceManager.getDeviceDetail("your-device-name", null);

// 或通过设备ID查询
JsonObject response = deviceManager.getDeviceDetail(null, "your-device-id");

// 处理结果
if (client.checkResponse(response)) {
    JsonObject deviceInfo = response.getAsJsonObject("data");
    System.out.println("设备状态: " + deviceInfo.get("status").getAsString());
}
```

### 4. 查询设备状态

```java
// 查询设备在线状态
JsonObject response = deviceManager.getDeviceStatus("your-device-name", null);

// 处理结果
if (client.checkResponse(response)) {
    JsonObject statusData = response.getAsJsonObject("data");
    System.out.println("设备状态: " + statusData.get("status").getAsString());
    System.out.println("状态时间戳: " + statusData.get("timestamp").getAsLong());
}
```

### 5. 批量查询设备状态

```java
// 批量查询多个设备状态
List<String> deviceNames = Arrays.asList("device1", "device2", "device3");
JsonObject response = deviceManager.batchGetDeviceStatus(deviceNames, null);

// 处理结果
if (client.checkResponse(response)) {
    JsonArray devicesData = response.getAsJsonObject("data").getAsJsonArray();
    for (JsonElement deviceElement : devicesData) {
        JsonObject device = deviceElement.getAsJsonObject();
        System.out.println("设备名称: " + device.get("deviceName").getAsString());
        System.out.println("设备状态: " + device.get("status").getAsString());
        System.out.println("最后在线时间: " + device.get("lastOnlineTime").getAsLong());
        System.out.println("-------------------");
    }
}
```

### 6. 发送RRPC消息

```java
// 向设备发送RRPC消息
JsonObject response = deviceManager.sendRrpcMessage(
    "your-device-name",
    "your-product-key",
    "Hello Device",
    5000  // 超时时间(毫秒)
);

// 处理响应
if (client.checkResponse(response)) {
    if (response.has("payloadBase64Byte")) {
        String base64Response = response.get("payloadBase64Byte").getAsString();
        String decodedResponse = new String(
            Base64.getDecoder().decode(base64Response),
            StandardCharsets.UTF_8
        );
        System.out.println("设备响应: " + decodedResponse);
    }
}
```

### 7. 发送自定义指令（异步）

```java
// 向设备发送自定义指令
String messageContent = new Gson().toJson(Map.of(
    "command", "set_mode",
    "params", Map.of(
        "mode", 2,
        "duration", 30
    )
));

JsonObject response = deviceManager.sendCustomCommand(
    "your-device-name",
    messageContent
);

if (client.checkResponse(response)) {
    System.out.println("自定义指令下发成功!");
}
```

## 完整示例

### 使用应用凭证并重用客户端

```java
import com.iot.sdk.IoTSdk;
import com.iot.sdk.client.IoTClient;
import com.iot.sdk.device.DeviceManager;
import com.google.gson.JsonObject;

// 配置参数
String baseUrl = "https://your-iot-platform-url";
String appId = "your-app-id";
String appSecret = "your-app-secret";
String productKey = "your-product-key";

try {
    // 初始化客户端（仅一次）
    IoTClient client = IoTSdk.createClientFromCredentials(baseUrl, appId, appSecret);
    System.out.println("客户端初始化成功，Token: " + client.getToken().substring(0, 10) + "...");
    
    // 创建设备管理器
    DeviceManager deviceManager = IoTSdk.createDeviceManager(client);
    
    // 执行多个操作，复用同一个客户端
    String deviceName = "test-device-1";
    
    // 查询设备状态
    JsonObject statusResponse = deviceManager.getDeviceStatus(deviceName, null);
    if (client.checkResponse(statusResponse)) {
        String status = statusResponse.getAsJsonObject("data").get("status").getAsString();
        System.out.println("设备状态: " + status);
    }
    
    // 发送指令
    String commandJson = new Gson().toJson(Map.of("command", "refresh"));
    JsonObject messageResponse = deviceManager.sendRrpcMessage(
        deviceName,
        productKey,
        commandJson
    );
    
    // 其他操作...
    
} catch (Exception e) {
    System.err.println("错误: " + e.getMessage());
}
```

## 自定义日志

SDK支持自定义日志记录器：

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 创建自定义日志记录器
Logger logger = LoggerFactory.getLogger("my-iot-app");

// 创建带自定义日志的客户端（使用token）
IoTClient client = IoTSdk.createClient(
    "https://your-iot-platform-url",
    "your-auth-token"
);

// 或使用应用凭证创建
IoTClient client = IoTSdk.createClientFromCredentials(
    "https://your-iot-platform-url",
    "your-app-id",
    "your-app-secret"
);
```

## 注意事项

- **认证方式**：推荐使用应用凭证方式自动获取token
- **客户端复用**：创建一次客户端实例后在应用程序中复用，避免重复获取token
- 使用前请确保已获取正确的认证令牌/应用凭证和产品密钥
- 所有API调用都会返回完整的响应内容，便于进一步处理和分析
- 自定义指令下发需要设备已订阅相应的主题

## 贡献

欢迎提交问题和改进建议，也欢迎通过Pull Request来提交代码贡献。

## 许可证

MIT License