package com.tubugs.tools.utils;

/**
 * Created by xuzhang on 2017/8/23.
 */
public class ResourceUtil {

    public static String getAbsolutePath(String resouce) {
        return ResourceUtil.class.getClassLoader().getResource(resouce).getPath();
    }
}
