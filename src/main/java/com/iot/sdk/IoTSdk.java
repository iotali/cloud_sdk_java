package com.iot.sdk;

import com.iot.sdk.client.IoTClient;
import com.iot.sdk.device.DeviceManager;

/**
 * IoT云平台SDK
 * 提供与IoT云平台交互的简便方法
 */
public class IoTSdk {
    
    /**
     * SDK版本
     */
    public static final String VERSION = "1.0.0";
    
    /**
     * 使用token创建IoT客户端
     *
     * @param baseUrl API基础URL
     * @param token   认证令牌
     * @return IoTClient IoT客户端实例
     */
    public static IoTClient createClient(String baseUrl, String token) {
        return new IoTClient(baseUrl, token);
    }

    /**
     * 使用应用凭证创建IoT客户端（推荐方式）
     *
     * @param baseUrl   API基础URL
     * @param appId     应用ID
     * @param appSecret 应用密钥
     * @return IoTClient IoT客户端实例
     */
    public static IoTClient createClientFromCredentials(String baseUrl, String appId, String appSecret) {
        return new IoTClient(baseUrl, appId, appSecret);
    }
    
    /**
     * 创建设备管理器
     *
     * @param client IoT客户端实例
     * @return DeviceManager 设备管理器实例
     */
    public static DeviceManager createDeviceManager(IoTClient client) {
        return new DeviceManager(client);
    }
} 