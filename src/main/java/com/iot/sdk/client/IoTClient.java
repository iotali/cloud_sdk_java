package com.iot.sdk.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

/**
 * IoT云平台SDK客户端
 * 提供与IoT云平台交互的基础功能
 */
public class IoTClient {
    private final String baseUrl;
    private String token;
    private final String appId;
    private final String appSecret;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Logger logger;
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    /**
     * 使用token初始化IoT客户端
     *
     * @param baseUrl API基础URL
     * @param token   认证令牌
     */
    public IoTClient(String baseUrl, String token) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.token = token;
        this.appId = null;
        this.appSecret = null;
        this.gson = new Gson();
        this.logger = LoggerFactory.getLogger(IoTClient.class);
        
        // 检查参数有效性
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalArgumentException("无效的baseUrl");
        }
        if (this.token == null || this.token.isEmpty()) {
            throw new IllegalArgumentException("无效的token");
        }
        
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        logger.info("IoT客户端已初始化: {}", this.baseUrl);
    }

    /**
     * 使用应用凭证初始化IoT客户端
     *
     * @param baseUrl   API基础URL
     * @param appId     应用ID
     * @param appSecret 应用密钥
     */
    public IoTClient(String baseUrl, String appId, String appSecret) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.appId = appId;
        this.appSecret = appSecret;
        this.gson = new Gson();
        this.logger = LoggerFactory.getLogger(IoTClient.class);
        
        // 检查参数有效性
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalArgumentException("无效的baseUrl");
        }
        if (this.appId == null || this.appId.isEmpty()) {
            throw new IllegalArgumentException("无效的appId");
        }
        if (this.appSecret == null || this.appSecret.isEmpty()) {
            throw new IllegalArgumentException("无效的appSecret");
        }
        
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // 获取token
        try {
            refreshToken();
        } catch (IOException e) {
            throw new RuntimeException("初始化客户端时获取token失败", e);
        }
        
        logger.info("IoT客户端已初始化: {}", this.baseUrl);
    }

    /**
     * 刷新认证token
     *
     * @throws IOException 网络请求异常
     */
    public void refreshToken() throws IOException {
        if (appId == null || appSecret == null) {
            throw new IllegalStateException("未配置应用凭证，无法刷新token");
        }

        String endpoint = "/api/v1/oauth/auth";
        Map<String, Object> payload = new HashMap<>();
        payload.put("appId", appId);
        payload.put("appSecret", appSecret);

        JsonObject response = makeRequest(endpoint, payload, "POST", null);
        if (checkResponse(response) && response.has("data")) {
            // 直接获取data字段的值作为token
            this.token = response.get("data").getAsString();
            logger.info("Token刷新成功");
        } else {
            throw new IOException("获取token失败：" + 
                (response.has("errorMessage") ? response.get("errorMessage").getAsString() : "未知错误"));
        }
    }
    
    /**
     * 发送API请求的通用方法
     *
     * @param endpoint          API端点路径
     * @param payload           请求体数据
     * @param method            HTTP方法(默认POST)
     * @param additionalHeaders 附加的请求头
     * @return API响应结果
     */
    public JsonObject makeRequest(String endpoint, Map<String, Object> payload, 
                                String method, Map<String, String> additionalHeaders) throws IOException {
        // 构建完整URL
        String url = baseUrl + endpoint;
        
        // 设置请求头
        Headers.Builder headersBuilder = new Headers.Builder()
                .add("Content-Type", "application/json");
        
        // 只有在token不为null时才添加token头
        if (token != null && !token.isEmpty()) {
            headersBuilder.add("token", token);
        }
        
        // 添加附加的请求头
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        
        Request.Builder requestBuilder = new Request.Builder()
                .headers(headersBuilder.build());
        
        // 根据HTTP方法构建请求
        if ("POST".equalsIgnoreCase(method)) {
            String jsonPayload = payload != null ? gson.toJson(payload) : "{}";
            RequestBody requestBody = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);
            requestBuilder.url(url).post(requestBody);
            
            logger.debug("发送POST请求: {}", url);
            logger.debug("请求体: {}", jsonPayload);
        } else if ("GET".equalsIgnoreCase(method)) {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            
            // 添加查询参数
            if (payload != null) {
                for (Map.Entry<String, Object> entry : payload.entrySet()) {
                    if (entry.getValue() != null) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
            
            requestBuilder.url(urlBuilder.build()).get();
            logger.debug("发送GET请求: {}", urlBuilder.build());
        } else {
            throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        }
        
        // 发送请求
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            logger.debug("收到响应: {}", responseBody);
            
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
    
    /**
     * 发送POST请求
     *
     * @param endpoint API端点路径
     * @param payload  请求体数据
     * @return API响应结果
     */
    public JsonObject post(String endpoint, Map<String, Object> payload) throws IOException {
        return makeRequest(endpoint, payload, "POST", null);
    }
    
    /**
     * 发送GET请求
     *
     * @param endpoint API端点路径
     * @param params   查询参数
     * @return API响应结果
     */
    public JsonObject get(String endpoint, Map<String, Object> params) throws IOException {
        return makeRequest(endpoint, params, "GET", null);
    }
    
    /**
     * 获取当前token
     *
     * @return 当前token
     */
    public String getToken() {
        return token;
    }

    /**
     * 检查API响应是否成功
     *
     * @param response API响应
     * @return 是否成功
     */
    public boolean checkResponse(JsonObject response) {
        if (response == null) {
            return false;
        }
        
        boolean success = response.has("success") && response.get("success").getAsBoolean();
        
        if (!success && response.has("errorMessage")) {
            String errorMsg = response.get("errorMessage").getAsString();
            logger.warn("API调用失败: {}", errorMsg);
        }
        
        return success;
    }
} 