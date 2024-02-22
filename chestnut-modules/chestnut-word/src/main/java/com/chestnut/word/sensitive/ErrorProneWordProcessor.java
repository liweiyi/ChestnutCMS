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
package com.chestnut.word.sensitive;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.ErrorProneWord;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 易错词处理器
 */
@Component
public class ErrorProneWordProcessor {

	/**
	 * 易错词表
	 */
	private DFAModel<String> wordDFAModel = new DFAModel<>();

	public void addWord(String word, String replaceWord) {
		this.wordDFAModel.addWord(word, replaceWord);
	}

	public void addWords(Map<String, String> words) {
		words.forEach((k, v) -> {
			this.wordDFAModel.addWord(k, v);
		});
	}

	public void removeWord(String word) {
		wordDFAModel.removeWord(word);
	}
	
	public void removeWords(Set<String> words) {
		words.forEach(word -> removeWord(word));
	}

	/**
	 * 获取易错词列表
	 *
	 * @param text
	 * @param matchType
	 */
	public Map<String, String> listWords(final String text, MatchType matchType) {
		List<MatchFlag> matchList = new ArrayList<>();
		char[] charText = text.toCharArray();
		for (int i = 0; i < charText.length; i++) {
			MatchFlag flag = this.match(charText, i, matchType);
			if (flag == null) {
				break;
			}
			matchList.add(flag);
			i = flag.index + flag.length - 1;
		}
		Map<String, String> map = new HashMap<>();
		matchList.forEach(flag -> map.put(text.substring(flag.index, flag.index + flag.length), flag.replacement));
		return map;
	}

	/**
	 * 获取易错词列表，默认最大匹配模式
	 *
	 * @param text
	 */
	public Map<String, String> listWords(final String text) {
		return this.listWords(text, MatchType.MAX);
	}
	
	/**
	 * 指定字符串是否包含易错词
	 *
	 * @param text
	 */
	public boolean check(final String text) {
		char[] charText = text.toCharArray();
		for (int i = 0; i < charText.length; i++) {
			MatchFlag flag = this.match(charText, i, MatchType.MIN);
			if (flag == null) {
				break;
			}
			return true;
		}
		return false;
	}

	/**
	 * 获取易错词数量
	 *
	 * @param text
	 * @param matchType
	 */
	public int count(final String text, MatchType matchType) {
		int count = 0;
		char[] charText = text.toCharArray();
		for (int i = 0; i < charText.length; i++) {
			MatchFlag flag = this.match(charText, i, matchType);
			if (flag == null) {
				break;
			}
			count++;
			i = flag.index + flag.length - 1;
		}
		return count;
	}

	/**
	 * 获取易错词数量，默认最大匹配模式
	 *
	 * @param text
	 */
	public int count(final String text) {
		return this.count(text, MatchType.MAX);
	}

	/**
	 * 替换易错词
	 *
	 * @param text
	 * @param matchType
	 */
	public String replace(final String text, MatchType matchType) {
		StringBuilder sb = new StringBuilder();
		int index = 0;
		char[] charText = text.toCharArray();
		for (int i = 0; i < charText.length; i++) {
			MatchFlag flag = this.match(charText, i, matchType);
			if (flag == null) {
				for (int j = index; j < charText.length; j++) {
					sb.append(charText[j]);
				}
				break;
			}
			for (int j = index; j < flag.index; j++) {
				sb.append(charText[j]);
			}
			sb.append(flag.replacement);
			index = flag.index + flag.length;
			i = flag.index + flag.length - 1;
		}
		return sb.toString();
	}
	
	public MatchFlag match(final char[] charText, int start, MatchType matchType) {
		boolean hit = false;
		MatchFlag hitFlag = null;
		int index = 0; // match start index
		int length = 0; // match length
		Map<String, DFANode<String>> current = this.getWordDFAModel().getRoot();
		for (int i = start; i < charText.length; i++) {
			String c = String.valueOf(charText[i]);
			DFANode<String> node = current.get(c);
			if (node != null) {
				if (!hit) {
					hit = true;
					index = i;
					length = 1;
				} else {
					length++;
				}
				if (node.end) {
					// matched
					MatchFlag matchFlag = new MatchFlag();
					matchFlag.index = index;
					matchFlag.length = length;
					matchFlag.replacement = node.data;
					if (matchType == MatchType.MIN || node.children == null) {
						return matchFlag; // 最小匹配碰到结束字符完成本次匹配 || 无子节点完成本次匹配
					} else {
						hitFlag = matchFlag;
					}
				}
				current = node.children;
			} else {
				if (hitFlag != null) {
					return hitFlag;
				}
				if (hit) {
					// mismatch, reset
					hit = false;
					i = index; // 索引回退到上次未匹配成功的匹配起始点继续
					current = this.getWordDFAModel().getRoot();
				}
			}
		}
		return null;
	}

	/**
	 * 匹配标识
	 */
	class MatchFlag {
		int index; // match start index
		int length; // match length

		String replacement;
	}
	
	/**
	 * 匹配方式<br/>
	 * MIN: 最小匹配，匹配到任意词后重新开始新的匹配
	 * MAX: 最大匹配，匹配到任意词后继续匹配直到匹配失败
	 */
	public enum MatchType {
		MIN, MAX
	}
	
	/**
	 * 获取敏感词索引标记
	 * 
	 * @return
	 */
	public List<MatchFlag> match(final char[] charText, MatchType matchType) {
		List<MatchFlag> list = new ArrayList<>();
		boolean hit = false;
		MatchFlag hitFlag = null;
		int index = 0;
		int length = 0;
		Map<String, DFANode<String>> current = this.getWordDFAModel().getRoot();
		for (int i = 0; i < charText.length; i++) {
			String c = String.valueOf(charText[i]);
			DFANode<String> node = current.get(c);
			if (node != null) {
				// hit node
				if (!hit) {
					// first hit
					hit = true;
					index = i;
					length = 1;
				} else {
					length++;
				}
				if (node.end) {
					// matched
					MatchFlag matchFlag = new MatchFlag();
					matchFlag.index = index;
					matchFlag.length = length;
					matchFlag.replacement = node.data;
					if (matchType == MatchType.MIN) {
						list.add(matchFlag);
						// 最小匹配碰到结束字符重新开始匹配
						current = this.getWordDFAModel().getRoot();
						hit = false;
					} else {
						hitFlag = matchFlag;
					}
				} else {
					current = node.children;
				}
			} else {
				if (hitFlag != null) {
					list.add(hitFlag);
				}
				hit = false;
			}
		}
		return list;
	}

	public void setWordDFAModel(DFAModel<String> wordDFAModel) {
		this.wordDFAModel = wordDFAModel;
	}

	public DFAModel<String> getWordDFAModel() {
		return this.wordDFAModel;
	}
}

