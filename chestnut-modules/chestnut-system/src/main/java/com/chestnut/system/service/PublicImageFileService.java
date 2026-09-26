package com.chestnut.system.service;

import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.config.SysUploadImageTypes;
import com.chestnut.system.fixed.config.SysUploadSizeLimit;
import com.chestnut.system.fixed.config.SysUploadTypeLimit;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Stores images intended for public rendering after the business endpoint authorizes the upload. */
@Service
public class PublicImageFileService {

    public String upload(MultipartFile file, String namespace) throws IOException {
        if (file == null || file.isEmpty()) {
            throw SysErrorCode.UPLOAD_FILE_TYPE_LIMIT.exception();
        }
        SysUploadSizeLimit.check(file.getSize());
        byte[] bytes = file.getBytes();
        String extension = imageExtension(bytes);
        if (extension == null) {
            throw SysErrorCode.UPLOAD_FILE_TYPE_LIMIT.exception();
        }
        SysUploadImageTypes.check(extension);
        SysUploadTypeLimit.check(extension);

        Path root = Path.of(SystemConfig.getPublicFileUploadDir());
        Path relative = Path.of(namespace, DateUtils.datePath(), IdUtils.simpleUUID() + "." + extension);
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid public image namespace");
        }
        Files.createDirectories(target.getParent());
        Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
        return SystemConfig.getResourcePrefix() + relative.toString().replace('\\', '/');
    }

    static String imageExtension(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return "jpg";
        }
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G' && (bytes[4] & 0xff) == 0x0d
                && (bytes[5] & 0xff) == 0x0a && (bytes[6] & 0xff) == 0x1a
                && (bytes[7] & 0xff) == 0x0a) {
            return "png";
        }
        if (bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a') {
            return "gif";
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E'
                && bytes[10] == 'B' && bytes[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
