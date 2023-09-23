package com.chestnut.contentcore.core.impl;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.domain.CmsResource;

import lombok.RequiredArgsConstructor;

/**
 * 资源类型：音频
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceType.BEAN_NAME_PREFIX + ResourceType_Audio.ID)
public class ResourceType_Audio implements IResourceType {

	public final static String ID = "audio";
	
	public static final  String NAME = "{CMS.CONTENTCORE.RESOURCE_TYPE." + ID + "}";

	public final static String[] SuffixArray = { "mp3", "wav", "wma", "ogg", "aiff", "aac", "flac", "mid" };

	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String[] getUsableSuffix() {
		return SuffixArray;
	}
	
	public static boolean isAudio(String path) {
		String ext = FileNameUtils.getExtension(path);
		return Objects.nonNull(path) && ArrayUtils.contains(SuffixArray, ext);
	}
	
	@Override
	public byte[] process(CmsResource resource, byte[] bytes) throws IOException {
		return IResourceType.super.process(resource, bytes);
	}
}
