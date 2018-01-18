package com.anlin.demo.utils;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * AES 加解密工具类
 *
 * @author AnLin
 * @version v1.0.0
 * @since 2018/1/17
 */
public class AESUtils {

    public static Logger logger = LoggerFactory.getLogger(AESUtils.class);

    private static final String cipherAlgorithm = "AES/CBC/PKCS5Padding";

    private static final String keyAlgorithm = "AES";

    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 解密报文
     * @param map 返回参数
     * @param key 密钥
     * @return
     */
    public static JSONObject decode(Map<String, String> map, String key){
        String signKey = key.substring(16);
        String dataKey = key.substring(0, 16);
        String result = decode(decode(map.get("encryptData")), dataKey);
        logger.info("API - 请求报文明文:{}", result);
        String signature = map.get("signData");
        String reSign = "";
        reSign = DigestUtils.shaHex(result + signKey);
        if (signature != null && signature.equals(reSign)) {
            return JSONObject.fromObject(result);
        }
        return null;
    }

    /**
     * 解密报文
     * @param map 返回参数
     * @param key 密钥
     * @param isSynchronize 是否同步通知
     * @return
     */
    public static JSONObject decode(Map<String, String> map, String key, boolean isSynchronize){
        String signKey = key.substring(16);
        String dataKey = key.substring(0, 16);
        String result = decode(decode(map.get("encryptData")), dataKey);
        logger.info("返回报文明文:{}", result);
        String signature = map.get("signature");
        String reSign = "";
        if (isSynchronize){
            reSign = DigestUtils.shaHex(result + signKey);
        } else {
            reSign = DigestUtils.shaHex(map.get("encryptData") + signKey);
        }
        if (signature != null && signature.equals(reSign)) {
            return JSONObject.fromObject(result);
        }
        return null;
    }

    /**
     * 加密报文
     * @param key
     * @param dataJson
     * @param partnerNo
     * @param orderId
     * @return
     */
    public static Map<String, String> encryption(String key, String dataJson, String partnerNo, String orderId, String action) {
        logger.info("订单编号：{},请求报文：{}", orderId, dataJson);
        Map<String, String> params = new HashMap<>(5);
        try {
            String signKey = key.substring(16);
            String dataKey = key.substring(0, 16);
            String sign = DigestUtils.shaHex(dataJson + signKey);
            params.put("encryptData", encode(encode(dataJson, dataKey)));
            params.put("signData", sign);
            params.put("orderId", orderId);
            params.put("partnerNo", partnerNo);
            params.put("action", action);
            logger.info("加密后报文：{}", JSONUtils.valueToString(params));
        } catch (Exception e) {
            logger.error("加密处理异常！异常信息：{}", e);
        }
        return params;
    }


    /**
     * 使用Base64解密算法解密字符串
     * @param encodeStr
     * @return
     */
    public static byte [] decode(String encodeStr) {
        byte[] b = encodeStr.getBytes(StandardCharsets.UTF_8);
        Base64 base64 = new Base64();
        return base64.decode(b);
    }

    /**
     * 使用Base64加密算法加密字符串
     * @param plainBytes
     * @return
     */
    public static String encode(byte[] plainBytes) {
        Base64 base64 = new Base64();
        plainBytes = base64.encode(plainBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    /**
     * 解密
     * @param encodeStr
     * @param keyText
     * @return
     */
    public static String decode(byte [] encodeStr, String keyText) {
        return AESDecrypt(encodeStr,keyText);
    }

    @Deprecated
    private static String  AESDecrypt(byte [] encodeStr, String keyText) {
        byte[] bytes = AESDecrypt(encodeStr, getUTF8Bytes(keyText), keyAlgorithm, cipherAlgorithm,
                keyText);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * AES解密
     *
     * @param encryptedBytes
     *            密文字节数组，不经base64编码
     * @param keyBytes
     *            密钥字节数组
     * @param keyAlgorithm
     *            密钥算法
     * @param cipherAlgorithm
     *            加解密算法
     * @param IV
     *            随机向量
     * @return 解密后字节数组
     * @throws RuntimeException
     */
    @Deprecated
    private static byte[] AESDecrypt(byte[] encryptedBytes, byte[] keyBytes, String keyAlgorithm,
                                     String cipherAlgorithm, String IV) {
        try {
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new RuntimeException("AES密钥长度不合法");
            }
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
            if (IV != null) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
            return cipher.doFinal(encryptedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("没有[%s]此类加密算法", cipherAlgorithm),e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(String.format("没有[%s]此类填充模式", cipherAlgorithm),e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("无效密钥",e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("无效密钥参数",e);
        } catch (BadPaddingException e) {
            throw new RuntimeException("错误填充模式",e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("解密块大小不合法",e);
        }
    }

    /**
     * AES 加密
     *
     * @param plainText
     * @param keyText
     * @return
     */
    public static byte[] encode(String plainText, String keyText){
        try {
            return AESEncrypt(plainText, keyText);
        } catch (Exception e){

        }
        return null;
    }

    @Deprecated
    private static byte[] AESEncrypt(String plainText, String keyText) throws Exception {
        return AESEncrypt(getUTF8Bytes(plainText), getUTF8Bytes(keyText), keyAlgorithm, cipherAlgorithm,
                keyText);
    }

    /**
     * AES解密
     *
     * @param plainBytes      密文字节数组，不经base64编码
     * @param keyBytes        密钥字节数组
     * @param keyAlgorithm    密钥算法
     * @param cipherAlgorithm 加解密算法
     * @param IV              随机向量
     * @return 解密后字节数组
     * @throws RuntimeException
     */
    @Deprecated
    private static byte[] AESEncrypt(byte[] plainBytes, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm,
                                     String IV) {
        try {
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new RuntimeException("AES密钥长度不合法");
            }
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
            if (null != IV) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }
            return cipher.doFinal(plainBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("没有[%s]此类加密算法", cipherAlgorithm),e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(String.format("没有[%s]此类填充模式", cipherAlgorithm),e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("无效密钥",e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("无效密钥参数",e);
        } catch (BadPaddingException e) {
            throw new RuntimeException("错误填充模式",e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("加密块大小不合法",e);
        }
    }
}