package com.tubugs.tools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by xuzhang on 2017/8/23.
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 读取文本文件
     *
     * @param filePath
     * @param encoding
     * @return
     */
    public static String readText(String filePath, String encoding) {
        try {
            StringBuilder sb = new StringBuilder();
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStream in = new FileInputStream(file);
                OutputStream out = new ByteArrayOutputStream();
                //已字节为单位进行读取
                byte[] bytes = new byte[1024];
                int num = 0;
                while ((num = in.read(bytes)) != -1) {
                    out.write(bytes, 0, num);
                }
                in.close();
                out.close();
                return out.toString();
            } else {
                logger.error("找不到指定的文件:" + filePath);
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static void save(String path, String content) throws IOException {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        //写文件
        OutputStream out = new FileOutputStream(file.getPath());
        byte[] bytes = content.getBytes();
        out.write(bytes, 0, bytes.length);
        out.close();
    }
}
