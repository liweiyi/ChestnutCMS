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
package comchestnut.word;

import com.chestnut.word.sensitive.ErrorProneWordProcessor;

import java.util.*;

public class FallibleWordTester {

    public static void main(String[] args) {
		Map<String, String> blackList = new HashMap<>();
		blackList.put("水笼头", "水龙头");
		blackList.put("甘败下风", "甘拜下风");
		blackList.put("渡假", "度假");
		blackList.put("凑和", "凑合");
		blackList.put("防碍", "妨碍");
		blackList.put("妨碍工务", "妨碍公务");


		ErrorProneWordProcessor p = new ErrorProneWordProcessor();
		p.addWords(blackList);
		System.out.println(p.getWordDFAModel().getRoot().toString());

		String text = "中国甘败下风人拉三等奖水笼头七风发斯蒂芬艹甘败下风妈的逼拉屎的发生看到了防碍公务上游行业拉萨到付凑和款啦草渡假";
		p.listWords(text).forEach((k, v) -> {
			System.out.println(k + " = " + v);
		});

		long s = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			p.replace(text, ErrorProneWordProcessor.MatchType.MAX);
		}
		System.out.println("replacement <||> cost: " + (System.currentTimeMillis() - s) + "ms");

		s = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			String t = text;
			for(Iterator<Map.Entry<String, String>> iterator = blackList.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, String> next = iterator.next();
				t = t.replaceAll(next.getKey(), next.getValue());
			}
		}
		System.out.println("replacement <||> cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
