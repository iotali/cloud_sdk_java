package com.iot.sdk.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * IoT SDK工具类
 * 提供通用的辅助功能
 */
public class Utils {
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    
    /**
     * 检查字符串是否为有效的UUID格式
     *
     * @param uuid 待检查的UUID字符串
     * @return 是否为有效UUID
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid.toLowerCase()).matches();
    }
    
    /**
     * 将异常转换为字符串
     *
     * @param e 异常对象
     * @return 包含堆栈信息的字符串
     */
    public static String exceptionToString(Throwable e) {
        if (e == null) {
            return "";
        }
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * 格式化时间戳为易读的日期时间格式
     *
     * @param timestamp 时间戳(毫秒)
     * @param format    日期时间格式，默认为 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化的日期时间字符串
     */
    public static String formatTimestamp(long timestamp, String format) {
        if (timestamp <= 0) {
            return "未知时间";
        }
        
        String dateFormat = (format != null && !format.isEmpty()) ? format : "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * 使用默认格式格式化时间戳
     *
     * @param timestamp 时间戳(毫秒)
     * @return 格式化的日期时间字符串
     */
    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, null);
    }
    
    /**
     * 计算并格式化时间差
     *
     * @param startTimeMs 开始时间戳(毫秒)
     * @param endTimeMs   结束时间戳(毫秒)，如果为0则使用当前时间
     * @return 格式化的时间差
     */
    public static String formatDuration(long startTimeMs, long endTimeMs) {
        if (startTimeMs <= 0) {
            return "未知时长";
        }
        
        long end = (endTimeMs > 0) ? endTimeMs : System.currentTimeMillis();
        long durationMs = end - startTimeMs;
        
        if (durationMs < 0) {
            return "时间计算错误";
        }
        
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("天 ");
            hours = hours % 24;
        }
        
        if (hours > 0 || days > 0) {
            result.append(hours).append("小时 ");
            minutes = minutes % 60;
        }
        
        if (minutes > 0 || hours > 0 || days > 0) {
            result.append(minutes).append("分钟");
        } else {
            result.append(seconds).append("秒");
        }
        
        return result.toString();
    }
    
    /**
     * 验证设备名称是否合法
     * 设备名称规则：长度为4-32个字符，可包含英文字母、数字和下划线，不能以下划线开头
     *
     * @param deviceName 设备名称
     * @return 是否合法
     */
    public static boolean isValidDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            return false;
        }
        
        return deviceName.length() >= 4 && deviceName.length() <= 32 
                && !deviceName.startsWith("_") 
                && deviceName.matches("^[a-zA-Z0-9_]+$");
    }
} 