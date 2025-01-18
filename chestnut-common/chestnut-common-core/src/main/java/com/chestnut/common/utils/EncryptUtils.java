/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.common.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 加解密工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class EncryptUtils {

    public static String sha1(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(input.getBytes());
            return Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found");
        }
    }


    /**
     * AES加密(可逆)
     *
     * @param plainText  明文
     * @param privateKey 密钥
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String encryptAES(String plainText, String privateKey) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(privateKey.getBytes());
            keyGen.init(128, random);

            SecretKey secretKey = keyGen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = plainText.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] byteResult = cipher.doFinal(byteContent);
            StringBuilder sb = new StringBuilder();

            for (byte b : byteResult) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES解密
     *
     * @param cipherText 密文
     * @param privateKey 密钥
     * @return
     * @throws Exception
     */
    public static String decryptAES(String cipherText, String privateKey) {
        try {
            if (StringUtils.isEmpty(cipherText)) {
                return null;
            }
            byte[] byteResult = new byte[cipherText.length() / 2];
            for (int i = 0; i < cipherText.length() / 2; i++) {
                int high = Integer.parseInt(cipherText.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(cipherText.substring(i * 2 + 1, i * 2 + 2), 16);
                byteResult[i] = (byte) (high * 16 + low);
            }
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(privateKey.getBytes());
            keyGen.init(128, random);
            SecretKey secretKey = keyGen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] result = cipher.doFinal(byteResult);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
