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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.chestnut.word.sensitive.SensitiveWordProcessor;
import com.chestnut.word.sensitive.SensitiveWordProcessor.MatchType;
import com.chestnut.word.sensitive.SensitiveWordProcessor.ReplaceType;

public class SensitiveWordTester {

    public static void main(String[] args) {
		Set<String> blackList = new HashSet<>();
		blackList.add("中国");
		blackList.add("中国人");
		blackList.add("七风");
		blackList.add("妈的");
		blackList.add("妈的逼");
		blackList.add("妈的逼蛋");
		blackList.add("游行");
		blackList.add("草");
		blackList.add("艹");

		Set<String> whiteList = new HashSet<>();
		whiteList.add("上游行业");
		
		SensitiveWordProcessor p = new SensitiveWordProcessor();
		p.init(blackList, whiteList);
		System.out.println(p.getWordDFAModel().getRoot().toString());
//		p.removeWord("中国人");
//
//		Set<String> set = new HashSet<>();
//		set.add("中国人");
//		p.getWordDFAModel().addWords(set, SensitiveWordType.WHITE);

		System.out.println(p.getWordDFAModel().getRoot().toString());
		
		String text = "妈的逼加大拉家带口中国拉市解放路卡士大夫七风妈的拉克丝七副风经理中国人拉三等奖七风发斯蒂芬艹妈的逼拉屎的发生看到了上游行业拉萨到付款啦草";
		Set<String> wordList = p.listWords(text);
		for (String string : wordList) {
			System.out.println(string);
		}
		System.out.println(p.replace(text, MatchType.MAX, ReplaceType.CHAR, "<|>"));
		
//		long s = System.currentTimeMillis();
//		for (int i = 0; i < 1000000; i++) {
//			p.replace(text, MatchType.MAX, ReplaceType.WORD, "<||>");
//		}
//		System.out.println("replacement <||> cost: " + (System.currentTimeMillis() - s) + "ms");

//		s = System.currentTimeMillis();
//		for (int i = 0; i < 1000000; i++) {
//			p.replace(text, MatchType.MAX, ReplaceType.WORD, "*");
//		}
//		System.out.println("replacement * cost: " + (System.currentTimeMillis() - s) + "ms");
//
//		s = System.currentTimeMillis();
//		for (int i = 0; i < 1000000; i++) {
//			String text2 = "加大拉家带口中国拉市解放路卡士大夫七风妈的拉克丝七副风经理中国人拉三等奖七风发斯蒂芬艹妈的逼拉屎的发生看到了上游行业拉萨到付款啦";
//			for (String string : wordList) {
//				text2 = text2.replaceAll(string, "*");
//			}
//		}
//		System.out.println("replacement * cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
