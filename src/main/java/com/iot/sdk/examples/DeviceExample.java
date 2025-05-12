package com.iot.sdk.examples;

import com.google.gson.JsonObject;
import com.iot.sdk.IoTSdk;
import com.iot.sdk.client.IoTClient;
import com.iot.sdk.device.DeviceManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * IoT SDK 使用示例
 */
public class DeviceExample {
    
    public static void main(String[] args) {
        // 配置SDK
        String baseUrl = "https://your-api-base-url.com";  // 替换为实际的API地址
        
        // 选择认证方式
        System.out.println("请选择认证方式：");
        System.out.println("1. 使用Token认证");
        System.out.println("2. 使用应用凭证认证（推荐）");
        System.out.print("请输入选择（1或2）: ");
        
        Scanner scanner = new Scanner(System.in);
        String authChoice = scanner.nextLine();
        
        IoTClient client;
        try {
            if ("1".equals(authChoice)) {
                // 使用Token认证
                System.out.print("请输入Token: ");
                String token = scanner.nextLine();
                if (token == null || token.trim().isEmpty()) {
                    throw new IllegalArgumentException("Token不能为空");
                }
                client = IoTSdk.createClient(baseUrl, token);
                System.out.println("使用Token认证方式初始化客户端成功");
            } else if ("2".equals(authChoice)) {
                // 使用应用凭证认证
                System.out.print("请输入应用ID (appId): ");
                String appId = scanner.nextLine();
                if (appId == null || appId.trim().isEmpty()) {
                    throw new IllegalArgumentException("应用ID不能为空");
                }
                
                System.out.print("请输入应用密钥 (appSecret): ");
                String appSecret = scanner.nextLine();
                if (appSecret == null || appSecret.trim().isEmpty()) {
                    throw new IllegalArgumentException("应用密钥不能为空");
                }
                
                System.out.println("正在使用应用凭证初始化客户端...");
                client = IoTSdk.createClientFromCredentials(baseUrl, appId, appSecret);
                System.out.println("使用应用凭证认证方式初始化客户端成功");
                System.out.println("获取到的Token: " + client.getToken());
            } else {
                System.out.println("无效的选择，程序退出");
                return;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("参数错误: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("初始化客户端失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // 创建设备管理器
        DeviceManager deviceManager = IoTSdk.createDeviceManager(client);
        System.out.println("设备管理器创建成功");
        
        // 菜单循环
        boolean running = true;
        
        while (running) {
            System.out.println("\n========== IoT SDK 示例程序 ==========");
            System.out.println("1. 注册设备");
            System.out.println("2. 查询设备详情");
            System.out.println("3. 查询设备状态");
            System.out.println("4. 批量查询设备状态");
            System.out.println("5. 发送RRPC消息");
            System.out.println("6. 发送自定义指令");
            System.out.println("0. 退出");
            System.out.print("请选择操作: ");
            
            String choice = scanner.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        registerDevice(scanner, deviceManager);
                        break;
                    case "2":
                        getDeviceDetail(scanner, deviceManager);
                        break;
                    case "3":
                        getDeviceStatus(scanner, deviceManager);
                        break;
                    case "4":
                        batchGetDeviceStatus(scanner, deviceManager);
                        break;
                    case "5":
                        sendRrpcMessage(scanner, deviceManager);
                        break;
                    case "6":
                        sendCustomCommand(scanner, deviceManager);
                        break;
                    case "0":
                        running = false;
                        System.out.println("程序已退出");
                        break;
                    default:
                        System.out.println("无效的选择，请重试");
                }
            } catch (Exception e) {
                System.err.println("操作出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        scanner.close();
    }
    
    /**
     * 注册设备示例
     */
    private static void registerDevice(Scanner scanner, DeviceManager deviceManager) throws IOException {
        System.out.println("\n--- 注册设备 ---");
        System.out.print("请输入产品编码: ");
        String productKey = scanner.nextLine();
        
        System.out.print("请输入设备名称 (可选): ");
        String deviceName = scanner.nextLine();
        if (deviceName.isEmpty()) {
            deviceName = null;
        }
        
        System.out.print("请输入设备显示名称 (可选): ");
        String nickName = scanner.nextLine();
        if (nickName.isEmpty()) {
            nickName = null;
        }
        
        System.out.println("正在注册设备...");
        JsonObject result = deviceManager.registerDevice(productKey, deviceName, nickName);
        
        if (result != null && result.has("data")) {
            JsonObject data = result.getAsJsonObject("data");
            System.out.println("\n注册成功! 设备信息:");
            System.out.println("设备ID: " + data.get("deviceId").getAsString());
            System.out.println("设备名称: " + data.get("deviceName").getAsString());
            System.out.println("设备密钥: " + data.get("deviceSecret").getAsString());
        } else {
            System.out.println("设备注册失败或返回数据异常");
        }
    }
    
    /**
     * 查询设备详情示例
     */
    private static void getDeviceDetail(Scanner scanner, DeviceManager deviceManager) throws IOException {
        System.out.println("\n--- 查询设备详情 ---");
        System.out.print("请输入设备名称 (与设备ID至少提供一个): ");
        String deviceName = scanner.nextLine();
        if (deviceName.isEmpty()) {
            deviceName = null;
        }
        
        System.out.print("请输入设备ID (与设备名称至少提供一个): ");
        String deviceId = scanner.nextLine();
        if (deviceId.isEmpty()) {
            deviceId = null;
        }
        
        if (deviceName == null && deviceId == null) {
            System.out.println("设备名称和设备ID至少需要提供一个");
            return;
        }
        
        System.out.println("正在查询设备详情...");
        JsonObject result = deviceManager.getDeviceDetail(deviceName, deviceId);
        
        if (result != null && result.has("data")) {
            JsonObject data = result.getAsJsonObject("data");
            System.out.println("\n设备详情:");
            System.out.println("设备ID: " + data.get("deviceId").getAsString());
            System.out.println("设备名称: " + data.get("deviceName").getAsString());
            System.out.println("产品编码: " + data.get("productKey").getAsString());
            System.out.println("状态: " + data.get("status").getAsString());
        } else {
            System.out.println("设备查询失败或返回数据异常");
        }
    }
    
    /**
     * 查询设备状态示例
     */
    private static void getDeviceStatus(Scanner scanner, DeviceManager deviceManager) throws IOException {
        System.out.println("\n--- 查询设备状态 ---");
        System.out.print("请输入设备名称 (与设备ID至少提供一个): ");
        String deviceName = scanner.nextLine();
        if (deviceName.isEmpty()) {
            deviceName = null;
        }
        
        System.out.print("请输入设备ID (与设备名称至少提供一个): ");
        String deviceId = scanner.nextLine();
        if (deviceId.isEmpty()) {
            deviceId = null;
        }
        
        if (deviceName == null && deviceId == null) {
            System.out.println("设备名称和设备ID至少需要提供一个");
            return;
        }
        
        System.out.println("正在查询设备状态...");
        JsonObject result = deviceManager.getDeviceStatus(deviceName, deviceId);
        
        // 打印完整返回结果
        System.out.println("\n完整返回结果:");
        System.out.println(result.toString());
        
        if (result != null && result.has("data")) {
            JsonObject data = result.getAsJsonObject("data");
            System.out.println("\n设备状态:");
            System.out.println("状态: " + data.get("status").getAsString());
            
            // 格式化时间戳
            if (data.has("timestamp") && !data.get("timestamp").isJsonNull()) {
                long timestamp = data.get("timestamp").getAsLong();
                java.util.Date date = new java.util.Date(timestamp);
                System.out.println("时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            }
        } else {
            System.out.println("设备状态查询失败或返回数据异常");
        }
    }
    
    /**
     * 批量查询设备状态示例
     */
    private static void batchGetDeviceStatus(Scanner scanner, DeviceManager deviceManager) throws IOException {
        System.out.println("\n--- 批量查询设备状态 ---");
        System.out.println("请输入设备名称列表 (多个设备用逗号分隔，与设备ID列表至少提供一个): ");
        String deviceNamesInput = scanner.nextLine();
        List<String> deviceNameList = null;
        if (!deviceNamesInput.isEmpty()) {
            deviceNameList = Arrays.asList(deviceNamesInput.split(","));
        }
        
        System.out.println("请输入设备ID列表 (多个设备用逗号分隔，与设备名称列表至少提供一个): ");
        String deviceIdsInput = scanner.nextLine();
        List<String> deviceIdList = null;
        if (!deviceIdsInput.isEmpty()) {
            deviceIdList = Arrays.asList(deviceIdsInput.split(","));
        }
        
        if ((deviceNameList == null || deviceNameList.isEmpty()) && (deviceIdList == null || deviceIdList.isEmpty())) {
            System.out.println("设备名称列表和设备ID列表至少需要提供一个");
            return;
        }
        
        System.out.println("正在批量查询设备状态...");
        JsonObject result = deviceManager.batchGetDeviceStatus(deviceNameList, deviceIdList);
        
        // 打印完整返回结果
        System.out.println("\n完整返回结果:");
        System.out.println(result.toString());
        
        if (result != null && result.has("data") && !result.get("data").isJsonNull()) {
            JsonElement dataElement = result.get("data");
            JsonArray devices;
            
            // 处理 data 可能是数组或对象的情况
            if (dataElement.isJsonArray()) {
                devices = dataElement.getAsJsonArray();
            } else if (dataElement.isJsonObject() && dataElement.getAsJsonObject().has("devices")) {
                devices = dataElement.getAsJsonObject().getAsJsonArray("devices");
            } else {
                System.out.println("返回数据格式不正确");
                return;
            }
            
            System.out.println("\n设备状态列表:");
            System.out.println("----------------------------------------");
            
            // 统计各状态设备数量
            Map<String, Integer> statusCounts = new HashMap<>();
            statusCounts.put("ONLINE", 0);
            statusCounts.put("OFFLINE", 0);
            statusCounts.put("UNACTIVE", 0);
            
            for (int i = 0; i < devices.size(); i++) {
                JsonElement deviceElement = devices.get(i);
                if (!deviceElement.isJsonNull()) {
                    JsonObject device = deviceElement.getAsJsonObject();
                    
                    // 获取设备基本信息
                    String deviceName = device.has("deviceName") && !device.get("deviceName").isJsonNull() 
                        ? device.get("deviceName").getAsString() : "未知";
                    String deviceId = device.has("deviceId") && !device.get("deviceId").isJsonNull()
                        ? device.get("deviceId").getAsString() : "未知";
                    String status = device.has("status") && !device.get("status").isJsonNull()
                        ? device.get("status").getAsString() : "未知";
                    
                    // 更新状态计数
                    if (statusCounts.containsKey(status)) {
                        statusCounts.put(status, statusCounts.get(status) + 1);
                    }
                    
                    // 格式化状态
                    Map<String, String> statusMap = new HashMap<>();
                    statusMap.put("ONLINE", "在线");
                    statusMap.put("OFFLINE", "离线");
                    statusMap.put("UNACTIVE", "未激活");
                    String statusText = statusMap.getOrDefault(status, status);
                    
                    // 获取时间戳
                    String timeStr = "未知";
                    if (device.has("timestamp") && !device.get("timestamp").isJsonNull()) {
                        try {
                            long timestamp = device.get("timestamp").getAsLong();
                            java.util.Date date = new java.util.Date(timestamp);
                            timeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                        } catch (Exception e) {
                            System.out.println("时间: 未知");
                        }
                    }
                    
                    // 输出设备信息
                    System.out.println("设备名称: " + deviceName);
                    System.out.println("设备ID: " + deviceId);
                    System.out.println("设备状态: " + statusText);
                    System.out.println("更新时间: " + timeStr);
                    System.out.println("----------------------------------------");
                }
            }
            
            // 打印设备状态统计
            System.out.println("\n设备状态统计:");
            System.out.println("在线设备: " + statusCounts.get("ONLINE") + " 台");
            System.out.println("离线设备: " + statusCounts.get("OFFLINE") + " 台");
            System.out.println("未激活设备: " + statusCounts.get("UNACTIVE") + " 台");
        } else {
            System.out.println("批量查询设备状态失败或返回数据异常");
        }
    }
    
    /**
     * 发送RRPC消息示例
     */
    private static void sendRrpcMessage(Scanner scanner, DeviceManager deviceManager) throws IOException {
        System.out.println("\n--- 发送RRPC消息 ---");
        System.out.print("请输入设备名称: ");
        String deviceName = scanner.nextLine();
        
        System.out.print("请输入产品编码: ");
        String productKey = scanner.nextLine();
        
        System.out.print("请输入消息内容: ");
        String messageContent = scanner.nextLine();
        
        System.out.print("请输入超时时间(毫秒，默认5000): ");
        String timeoutStr = scanner.nextLine();
        int timeout = 5000;
        if (!timeoutStr.isEmpty()) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                System.out.println("使用默认超时时间: 5000毫秒");
            }
        }
        
        System.out.println("正在发送RRPC消息...");
        JsonObject result = deviceManager.sendRrpcMessage(deviceName, productKey, messageContent, timeout);
        
        if (result != null && result.has("success") && result.get("success").getAsBoolean()) {
            System.out.println("\nRRPC消息发送成功!");
        } else {
            System.out.println("RRPC消息发送失败或返回数据异常");
        }
    }
    
    /**
     * 发送自定义指令示例
     */
    private static void sendCustomCommand(Scanner scanner, DeviceManager deviceManager) throws IOException {
        System.out.println("\n--- 发送自定义指令 ---");
        System.out.print("请输入设备名称: ");
        String deviceName = scanner.nextLine();
        
        System.out.println("请输入要发送的命令内容(JSON格式):");
        System.out.println("示例: {\"washingMode\": 2, \"washingTime\": 30}");
        String messageContent = scanner.nextLine();
        
        System.out.println("正在发送自定义指令...");
        JsonObject result = deviceManager.sendCustomCommand(deviceName, messageContent);
        
        if (result != null && result.has("success") && result.get("success").getAsBoolean()) {
            System.out.println("\n自定义指令下发成功!");
            if (result.has("data")) {
                System.out.println("响应数据: " + result.get("data").toString());
            }
        } else {
            String errorMsg = result.has("errorMessage") ? 
                result.get("errorMessage").getAsString() : "未知错误";
            System.out.println("\n自定义指令下发失败: " + errorMsg);
        }
    }
} 