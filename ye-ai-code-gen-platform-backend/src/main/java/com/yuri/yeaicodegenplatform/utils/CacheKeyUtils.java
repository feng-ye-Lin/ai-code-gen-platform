package com.yuri.yeaicodegenplatform.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存 key 生成工具类
 * @author yuri
 * @create 2026-06-16 23:51
 */
public class CacheKeyUtils {

    public static String generateKey(Object obj) {
        if (obj == null) {
            return DigestUtil.md5Hex("null");
        }
        // 先转 JSON，再转 MD5
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
