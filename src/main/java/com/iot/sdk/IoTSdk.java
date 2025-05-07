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
     * 创建IoT客户端
     *
     * @param baseUrl API基础URL
     * @param token   认证令牌
     * @return IoTClient IoT客户端实例
     */
    public static IoTClient createClient(String baseUrl, String token) {
        return new IoTClient(baseUrl, token);
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