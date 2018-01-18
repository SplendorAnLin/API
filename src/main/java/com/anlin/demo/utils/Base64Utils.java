package com.anlin.demo.utils;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * BASE64加密工具类
 *
 * @author AnLin
 * @version V1.0.0
 * @since 2017年4月24日
 */
public class Base64Utils {

    private static Logger logger = LoggerFactory.getLogger(Base64Utils.class);

    /**
     * Description: BASE64加密
     *
     * @param str
     * @return
     */
    public static String encode(byte[] str) {
        if (str == null || "".equals(str)) {
            return null;
        }
        return Base64.encodeBase64String(str);
    }

    /**
     * Description: BASE64解密
     *
     * @param str
     * @return
     */
    public static byte[] decode(String str) {
        if (str == null || "".equals(str)) {
            return null;
        }
        return Base64.decodeBase64(str);
    }

    public static boolean base64ToFile(String base64, String filePath) {
        byte[] buffer = decode(base64);
        File file = new File(filePath);
        FileOutputStream out = null;
        try {
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream(file);
            out.write(buffer);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.info("文件流异常{}", e);
                }
            }
        }
    }
}
