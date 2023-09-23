package com.chestnut.advertisement;

import org.springframework.stereotype.Component;

@Component(IAdvertisementType.BEAN_NAME_PREFIX + ImageAdvertisementType.ID)
public class ImageAdvertisementType implements IAdvertisementType {
	
	public static final String ID = "image";
	
	public static final  String NAME = "{ADVERTISEMENT.TYPE." + ID + "}";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
