/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.common.captcha.math;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码文本生成器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class KaptchaTextCreator extends DefaultTextCreator {
	
	private static final String[] CNUMBERS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };

	@Override
	public String getText() {
		int result;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int x = random.nextInt(10);
		int y = random.nextInt(10);
		StringBuilder suChinese = new StringBuilder();
		int randomOperands = random.nextInt(3);
		if (randomOperands == 0) {
			result = x * y;
			suChinese.append(CNUMBERS[x]);
			suChinese.append("*");
			suChinese.append(CNUMBERS[y]);
		} else if (randomOperands == 1) {
			if ((x != 0) && y % x == 0) {
				result = y / x;
				suChinese.append(CNUMBERS[y]);
				suChinese.append("/");
				suChinese.append(CNUMBERS[x]);
			} else {
				result = x + y;
				suChinese.append(CNUMBERS[x]);
				suChinese.append("+");
				suChinese.append(CNUMBERS[y]);
			}
		} else {
			if (x >= y) {
				result = x - y;
				suChinese.append(CNUMBERS[x]);
				suChinese.append("-");
				suChinese.append(CNUMBERS[y]);
			} else {
				result = y - x;
				suChinese.append(CNUMBERS[y]);
				suChinese.append("-");
				suChinese.append(CNUMBERS[x]);
			}
		}
		suChinese.append("=?@").append(result);
		return suChinese.toString();
	}
}