package com.chestnut.common.utils.image;

import com.chestnut.common.utils.StringUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ImageTypeUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ImageTypeUtils {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("image/jpeg", "jpg");
        MIME_TYPES.put("image/png", "png");
        MIME_TYPES.put("image/gif", "gif");
        MIME_TYPES.put("image/bmp", "bmp");
        MIME_TYPES.put("image/webp", "webp");
        MIME_TYPES.put("image/svg+xml", "svg+xml");
        MIME_TYPES.put("image/tiff", "tif");
        MIME_TYPES.put("image/vnd.microsoft.icon", "ico");
        MIME_TYPES.put("image/x-icon", "ico");
        MIME_TYPES.put("image/avif", "avif");
        MIME_TYPES.put("image/apng", "apng");
        MIME_TYPES.put("image/emf", "emf");
        MIME_TYPES.put("image/x-emf", "emf");
    }

    // 定义文件头和对应文件类型的映射
    private static final Map<byte[], String> HEADER_TO_TYPE = new HashMap<>();

    static {
        HEADER_TO_TYPE.put(new byte[]{(byte) 0xFF, (byte) 0xD8}, "jpg"); // JPEG
        HEADER_TO_TYPE.put(new byte[]{'B', 'M'}, "bmp"); // BMP
        HEADER_TO_TYPE.put(new byte[]{'G', 'I', 'F', '8', '7', 'a'}, "gif"); // GIF 87a
        HEADER_TO_TYPE.put(new byte[]{'G', 'I', 'F', '8', '9', 'a'}, "gif"); // GIF 89a
        HEADER_TO_TYPE.put(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}, "png"); // PNG
        HEADER_TO_TYPE.put(new byte[]{'W', 'E', 'B', 'P', 'V', 'P', '8', ' '}, "webp"); // WEBP (Lossy)
        HEADER_TO_TYPE.put(new byte[]{'W', 'E', 'B', 'P', 'V', 'P', '8', 'L'}, "webp"); // WEBP (Lossless)
        HEADER_TO_TYPE.put(new byte[]{'W', 'E', 'B', 'P', 'V', 'P', '8', 'X'}, "webp"); // WEBP (Extended)
        // 可以添加更多的文件头和对应的类型
    }

    public static String get(InputStream is) throws IOException {
        return getImageTypeByHeader(is);
    }

    public static String get(String mimeType) {
        return MIME_TYPES.get(mimeType);
    }

    public static String get(byte[] bytes) throws IOException {
        if (bytes.length < 8) {
            throw new IOException("Not enough bytes to read.");
        }
        for (Map.Entry<byte[], String> entry : HEADER_TO_TYPE.entrySet()) {
            if (compareByteArrays(entry.getKey(), bytes)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static String get(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            // 先读header
            String format = getImageTypeByHeader(fis);
            // 判断不了再读后缀名
            if (StringUtils.isEmpty(format)) {
                return FilenameUtils.getExtension(file.getName());
            }
            return format;
        }
    }

    private static String getImageTypeByHeader(InputStream is) throws IOException {
        byte[] headerBytes = new byte[8];
        int read = is.read(headerBytes, 0, headerBytes.length);
        if (read != headerBytes.length) {
            throw new IOException("Not enough bytes to read.");
        }
        for (Map.Entry<byte[], String> entry : HEADER_TO_TYPE.entrySet()) {
            if (compareByteArrays(entry.getKey(), headerBytes)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static boolean compareByteArrays(byte[] expected, byte[] actual) {
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return false;
            }
        }
        return true;
    }
}
