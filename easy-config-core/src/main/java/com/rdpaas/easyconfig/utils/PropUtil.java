package com.rdpaas.easyconfig.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 属性工具类
 * @author rongdi
 * @date 2019-09-21 16:30:07
 */
public class PropUtil {

    public static boolean isWebProp(String filePath) {
        return filePath.startsWith("http:") || filePath.startsWith("https:");
    }

    public static Map<String,Object> prop2Map(Properties prop) {
        Map<String,Object> props = new HashMap<>();
        prop.forEach((key,value) -> {
            props.put(String.valueOf(key),value);
        });
        return props;
    }

    public static boolean isSame(Properties p1,Properties p2) {
        if(p1.isEmpty() && p2.isEmpty()) {
            return true;
        }
        for(Iterator<Map.Entry<Object,Object>> iter = p1.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Object,Object> entry = iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if(!value.equals(p2.get(key))) {
                return false;
            }
        }
        return true;
    }
}
