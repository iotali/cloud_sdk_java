package com.iot.sdk.device;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.iot.sdk.client.IoTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonParser;

/**
 * 设备管理模块，提供设备相关操作
 */
public class DeviceManager {
    private final IoTClient client;
    private final Logger logger;

    /**
     * 初始化设备管理模块
     *
     * @param client IoT客户端实例
     */
    public DeviceManager(IoTClient client) {
        this.client = client;
        this.logger = LoggerFactory.getLogger(DeviceManager.class);
    }

    /**
     * 注册设备
     *
     * @param productKey 产品唯一标识码
     * @param deviceName 设备标识码，可选，若未提供则自动生成
     * @param nickName   设备显示名称，可选
     * @return 注册结果，包含设备ID和密钥等信息
     * @throws IOException 网络请求异常
     */
    public JsonObject registerDevice(String productKey, String deviceName, String nickName) throws IOException {
        if (productKey == null || productKey.isEmpty()) {
            throw new IllegalArgumentException("产品密钥不能为空");
        }

        String endpoint = "/api/v1/quickdevice/register";

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("productKey", productKey);

        // 添加可选参数
        if (deviceName != null && !deviceName.isEmpty()) {
            payload.put("deviceName", deviceName);
        }

        if (nickName != null && !nickName.isEmpty()) {
            payload.put("nickName", nickName);
        }

        // 发送请求
        JsonObject response = client.post(endpoint, payload);

        // 检查结果并格式化输出
        if (client.checkResponse(response)) {
            JsonObject deviceInfo = response.getAsJsonObject("data");
            logger.info("设备注册成功: {}", deviceInfo.get("deviceName").getAsString());

            // 输出详细信息
            logger.info("设备信息摘要:");
            logger.info("产品密钥: {}", deviceInfo.get("productKey").getAsString());
            logger.info("设备名称: {}", deviceInfo.get("deviceName").getAsString());
            logger.info("显示名称: {}", deviceInfo.get("nickName").getAsString());
            logger.info("设备ID: {}", deviceInfo.get("deviceId").getAsString());
            logger.info("设备密钥: {}", deviceInfo.get("deviceSecret").getAsString());
        }

        return response;
    }

    /**
     * 查询设备详情
     *
     * @param deviceName 设备编码，可选
     * @param deviceId   设备唯一标识，可选
     * @return 设备详情信息
     * @throws IOException 网络请求异常
     */
    public JsonObject getDeviceDetail(String deviceName, String deviceId) throws IOException {
        // 参数验证
        if ((deviceName == null || deviceName.isEmpty()) && (deviceId == null || deviceId.isEmpty())) {
            throw new IllegalArgumentException("设备编码(deviceName)和设备ID(deviceId)至少需要提供一个");
        }

        String endpoint = "/api/v1/quickdevice/detail";

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        if (deviceName != null && !deviceName.isEmpty()) {
            payload.put("deviceName", deviceName);
        }
        if (deviceId != null && !deviceId.isEmpty()) {
            payload.put("deviceId", deviceId);
        }

        // 发送请求
        JsonObject response = client.post(endpoint, payload);

        // 检查结果并格式化输出
        if (client.checkResponse(response)) {
            JsonObject deviceInfo = response.getAsJsonObject("data");
            String deviceStatus = deviceInfo.get("status").getAsString();

            // 格式化设备状态
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put("ONLINE", "在线");
            statusMap.put("OFFLINE", "离线");
            statusMap.put("UNACTIVE", "未激活");
            String statusText = statusMap.getOrDefault(deviceStatus, deviceStatus);

            // 输出设备基础信息
            logger.info("设备ID: {}", deviceInfo.has("deviceId") ? deviceInfo.get("deviceId").getAsString() : "未知");
            logger.info("设备名称: {}", deviceInfo.has("deviceName") ? deviceInfo.get("deviceName").getAsString() : "未知");
            logger.info("设备状态: {}", statusText);
        }

        return response;
    }

    /**
     * 查询设备在线状态
     *
     * @param deviceName 设备编码，可选
     * @param deviceId   设备唯一标识，可选
     * @return 设备状态信息
     * @throws IOException 网络请求异常
     */
    public JsonObject getDeviceStatus(String deviceName, String deviceId) throws IOException {
        // 参数验证
        if ((deviceName == null || deviceName.isEmpty()) && (deviceId == null || deviceId.isEmpty())) {
            throw new IllegalArgumentException("设备编码(deviceName)和设备ID(deviceId)至少需要提供一个");
        }

        String endpoint = "/api/v1/quickdevice/status";

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        if (deviceName != null && !deviceName.isEmpty()) {
            payload.put("deviceName", deviceName);
        }
        if (deviceId != null && !deviceId.isEmpty()) {
            payload.put("deviceId", deviceId);
        }

        // 发送请求
        JsonObject response = client.post(endpoint, payload);

        // 检查结果并格式化输出
        if (client.checkResponse(response)) {
            JsonObject statusData = response.getAsJsonObject("data");
            String deviceStatus = statusData.has("status") ? statusData.get("status").getAsString() : null;
            
            // 修改时间戳处理逻辑
            long timestampMs = 0;
            if (statusData.has("timestamp")) {
                JsonElement timestampElement = statusData.get("timestamp");
                if (!timestampElement.isJsonNull()) {
                    timestampMs = timestampElement.getAsLong();
                }
            }

            // 状态映射
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put("ONLINE", "在线");
            statusMap.put("OFFLINE", "离线");
            statusMap.put("UNACTIVE", "未激活");
            String statusText = statusMap.getOrDefault(deviceStatus, deviceStatus);

            // 时间戳格式化
            String timeStr = "未知";
            if (timestampMs > 0) {
                Date date = new Date(timestampMs);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeStr = sdf.format(date);
            }

            // 显示状态信息
            logger.info("设备状态: {}", statusText);
            logger.info("状态更新时间: {}", timeStr);

            // 如果设备离线，计算离线时长
            if ("OFFLINE".equals(deviceStatus) && timestampMs > 0) {
                long nowMs = System.currentTimeMillis();
                long offlineDurationMs = nowMs - timestampMs;
                long offlineMinutes = offlineDurationMs / (1000 * 60);

                String offlineText;
                if (offlineMinutes < 60) {
                    offlineText = String.format("约 %d 分钟", offlineMinutes);
                } else {
                    long offlineHours = offlineMinutes / 60;
                    if (offlineHours < 24) {
                        offlineText = String.format("约 %d 小时 %d 分钟", offlineHours, offlineMinutes % 60);
                    } else {
                        long offlineDays = offlineHours / 24;
                        long remainingHours = offlineHours % 24;
                        offlineText = String.format("约 %d 天 %d 小时", offlineDays, remainingHours);
                    }
                }

                logger.info("离线时长: {}", offlineText);
            }
        }

        return response;
    }

    /**
     * 批量查询设备在线状态
     *
     * @param deviceNameList 设备编码列表，可选
     * @param deviceIdList   设备唯一标识列表，可选
     * @return 设备状态信息
     * @throws IOException 网络请求异常
     */
    public JsonObject batchGetDeviceStatus(List<String> deviceNameList, List<String> deviceIdList) throws IOException {
        // 参数验证
        if ((deviceNameList == null || deviceNameList.isEmpty()) && (deviceIdList == null || deviceIdList.isEmpty())) {
            throw new IllegalArgumentException("设备编码列表和设备ID列表至少需要提供一个");
        }

        // 检查设备数量限制
        int deviceCount = (deviceNameList != null ? deviceNameList.size() : 0) + 
                         (deviceIdList != null ? deviceIdList.size() : 0);
        if (deviceCount > 100) {
            throw new IllegalArgumentException(
                String.format("单次请求最多支持查询100个设备，当前请求包含%d个设备", deviceCount));
        }

        String endpoint = "/api/v1/quickdevice/batchGetDeviceState";

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        if (deviceNameList != null && !deviceNameList.isEmpty()) {
            payload.put("deviceName", deviceNameList);
        }
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            payload.put("deviceId", deviceIdList);
        }

        // 发送请求
        JsonObject response = client.post(endpoint, payload);

        // 检查结果并格式化输出
        if (client.checkResponse(response)) {
            if (response.has("data") && !response.get("data").isJsonNull()) {
                JsonElement dataElement = response.get("data");
                JsonArray devices;
                
                // 处理 data 可能是数组或对象的情况
                if (dataElement.isJsonArray()) {
                    devices = dataElement.getAsJsonArray();
                } else if (dataElement.isJsonObject() && dataElement.getAsJsonObject().has("devices")) {
                    devices = dataElement.getAsJsonObject().getAsJsonArray("devices");
                } else {
                    logger.warn("返回数据格式不正确");
                    return response;
                }
                
                logger.info("批量查询设备状态结果, 设备数量: {}", devices.size());
                
                // 统计各状态设备数量
                Map<String, Integer> statusCounts = new HashMap<>();
                statusCounts.put("ONLINE", 0);
                statusCounts.put("OFFLINE", 0);
                statusCounts.put("UNACTIVE", 0);
                
                // 遍历设备列表并处理每个设备的状态
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
                        
                        // 获取时间戳
                        long timestampMs = 0;
                        if (device.has("timestamp") && !device.get("timestamp").isJsonNull()) {
                            timestampMs = device.get("timestamp").getAsLong();
                        }
                        
                        // 格式化状态
                        Map<String, String> statusMap = new HashMap<>();
                        statusMap.put("ONLINE", "在线");
                        statusMap.put("OFFLINE", "离线");
                        statusMap.put("UNACTIVE", "未激活");
                        String statusText = statusMap.getOrDefault(status, status);
                        
                        // 格式化时间
                        String timeStr = "未知";
                        if (timestampMs > 0) {
                            Date date = new Date(timestampMs);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            timeStr = sdf.format(date);
                        }
                        
                        // 输出设备信息
                        logger.info("设备 {} (ID: {}) - 状态: {}, 更新时间: {}", 
                            deviceName, deviceId, statusText, timeStr);
                    }
                }
                
                // 打印设备状态统计
                logger.info("设备状态统计: 在线设备: {} 台, 离线设备: {} 台, 未激活设备: {} 台",
                    statusCounts.get("ONLINE"),
                    statusCounts.get("OFFLINE"),
                    statusCounts.get("UNACTIVE"));
            } else {
                logger.warn("返回数据中没有设备状态信息");
            }
        }

        return response;
    }

    /**
     * 发送RRPC消息到设备
     *
     * @param deviceName      设备编码
     * @param productKey      产品密钥
     * @param messageContent  消息内容
     * @param timeout         超时时间(毫秒)，默认5000
     * @return 响应结果
     * @throws IOException 网络请求异常
     */
    public JsonObject sendRrpcMessage(String deviceName, String productKey, String messageContent, int timeout) throws IOException {
        // 参数验证
        if (deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("设备编码不能为空");
        }
        if (productKey == null || productKey.isEmpty()) {
            throw new IllegalArgumentException("产品密钥不能为空");
        }
        if (messageContent == null || messageContent.isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        String endpoint = "/api/v1/device/rrpc";

        // 消息内容Base64编码
        String base64Message = Base64.getEncoder().encodeToString(messageContent.getBytes(StandardCharsets.UTF_8));

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceName", deviceName);
        payload.put("productKey", productKey);
        payload.put("requestBase64Byte", base64Message);
        
        if (timeout > 0) {
            payload.put("timeout", timeout);
        }

        // 发送请求
        JsonObject response = client.post(endpoint, payload);

        // 检查结果
        if (client.checkResponse(response)) {
            logger.info("RRPC消息发送成功");
            String base64Response = null;
            
            // 检查两种可能的字段名
            if (response.has("payloadBase64Byte")) {
                base64Response = response.get("payloadBase64Byte").getAsString();
            } else if (response.has("playloadBase64Byte")) {
                base64Response = response.get("playloadBase64Byte").getAsString();
            }
            
            if (base64Response != null) {
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Response);
                    String decodedResponse = new String(decodedBytes, StandardCharsets.UTF_8);
                    logger.info("设备响应内容: {}", decodedResponse);
                    
                    // 尝试解析为JSON
                    try {
                        JsonParser parser = new JsonParser();
                        JsonElement jsonElement = parser.parse(decodedResponse);
                        if (jsonElement.isJsonObject()) {
                            logger.info("响应内容为JSON格式");
                        }
                    } catch (Exception e) {
                        logger.debug("响应内容不是有效的JSON格式");
                    }
                } catch (Exception e) {
                    logger.error("解析响应内容失败: {}", e.getMessage());
                }
            } else {
                logger.warn("响应中没有包含payloadBase64Byte或playloadBase64Byte字段");
            }
        }

        return response;
    }

    /**
     * 发送自定义指令到设备
     *
     * @param deviceName      设备编码
     * @param messageContent  消息内容
     * @return 响应结果
     * @throws IOException 网络请求异常
     */
    public JsonObject sendCustomCommand(String deviceName, String messageContent) throws IOException {
        // 参数验证
        if (deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("设备编码不能为空");
        }
        if (messageContent == null || messageContent.isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        String endpoint = "/api/v1/device/down/record/add/custom";

        // 消息内容Base64编码
        String base64Message = Base64.getEncoder().encodeToString(messageContent.getBytes(StandardCharsets.UTF_8));

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceName", deviceName);
        payload.put("messageContent", base64Message);

        // 发送请求
        JsonObject response = client.post(endpoint, payload);

        // 检查结果
        if (client.checkResponse(response)) {
            logger.info("自定义指令下发成功");
            if (response.has("data")) {
                logger.info("响应数据: {}", response.get("data"));
            }
        } else {
            logger.error("自定义指令下发失败: {}", 
                response.has("errorMessage") ? response.get("errorMessage").getAsString() : "未知错误");
        }

        return response;
    }
} 