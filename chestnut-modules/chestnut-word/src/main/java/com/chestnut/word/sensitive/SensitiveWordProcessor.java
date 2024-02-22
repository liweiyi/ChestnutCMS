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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Component;

import com.chestnut.word.domain.SensitiveWord;

/**
 * 敏感词处理器
 */
@Component
public class SensitiveWordProcessor {

	/**
	 * 敏感词表
	 */
	private DFAModel<SensitiveWordType> wordDFAModel = new DFAModel<SensitiveWordType>();

	/**
	 * 初始化敏感词模型数据
	 * 
	 * @param blackList
	 * @param whiteList
	 */
	public void init(List<SensitiveWord> blackList, List<SensitiveWord> whiteList) {
		this.init(blackList.stream().map(SensitiveWord::getWord).collect(Collectors.toSet()), 
				whiteList.stream().map(SensitiveWord::getWord).collect(Collectors.toSet()));
	}
	
	public void init(Set<String> blackList, Set<String> whiteList) {
		this.addWords(blackList, SensitiveWordType.BLACK);
		this.addWords(whiteList, SensitiveWordType.WHITE);
	}
	
	/**
	 * 添加敏感词
	 * 
	 * @param word
	 */
	public void addWord(String word, SensitiveWordType wordType) {
		addWords(SetUtils.unmodifiableSet(word), wordType);
	}
	
	public void addWords(Set<String> words, SensitiveWordType wordType) {
		this.wordDFAModel.addWords(words, wordType);
	}
	
	/**
	 * 移除敏感词
	 * 
	 * @param word
	 */
	public void removeWord(String word) {
		wordDFAModel.removeWord(word);
	}
	
	public void removeWords(Set<String> words) {
		words.forEach(word -> removeWord(word));
	}

	/**
	 * 替换敏感词，字符替换，对匹配的每个字符都替换成replacement
	 *
	 * @param text
	 * @param matchType
	 * @param replacement
	 */
	public String replace(final String text, MatchType matchType, final char replacement) {
		char[] charText = text.toCharArray();
		for (int i = 0; i < charText.length; i++) {
			MatchFlag flag = this.match(charText, i, matchType);
			if (flag == null) {
				break;
			}
			if (flag.type == SensitiveWordType.BLACK) {
				for (int j = flag.index; j < flag.index + flag.length; j++) {
					charText[j] = replacement;
				}
			}
			i = flag.index + flag.length - 1;
		}
		return new String(charText);
	}

	/**
	 * 替换敏感词，字符串替换，对匹配的每个字符/词替换成replacement
	 *
	 * @param text
	 * @param matchType
	 * @param replacement
	 */
	public String replace(final String text, MatchType matchType, ReplaceType replaceType, final String replacement) {
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
			if (flag.type == SensitiveWordType.BLACK) {
				for (int j = index; j < flag.index; j++) {
					sb.append(charText[j]);
				}
				if (replaceType == ReplaceType.CHAR) {
					for (int j = 0; j < flag.length; j++) {
						sb.append(replacement);
					}
				} else {
					sb.append(replacement);
				}
				index = flag.index + flag.length;
			}
			i = flag.index + flag.length - 1;
		}
		return sb.toString();
	}

	/**
	 * 获取敏感词列表
	 *
	 * @param text
	 * @param matchType
	 */
	public Set<String> listWords(final String text, MatchType matchType) {
		List<MatchFlag> matchList = new ArrayList<>();
		char[] charText = text.toCharArray();
		for (int i = 0; i < charText.length; i++) {
			MatchFlag flag = this.match(charText, i, matchType);
			if (flag == null) {
				break;
			}
			if (flag.type == SensitiveWordType.BLACK) {
				matchList.add(flag);
			}
			i = flag.index + flag.length - 1;
		}
		return matchList.stream().map(flag -> text.substring(flag.index, flag.index + flag.length)).collect(Collectors.toSet());
	}

	/**
	 * 获取敏感词列表，默认最大匹配模式
	 *
	 * @param text
	 */
	public Set<String> listWords(final String text) {
		return this.listWords(text, MatchType.MAX);
	}
	
	/**
	 * 指定字符串是否包含敏感词
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
			if (flag.type == SensitiveWordType.BLACK) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取敏感词数量
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
			if (flag.type == SensitiveWordType.BLACK) {
				count++;
			}
			i = flag.index + flag.length - 1;
		}
		return count;
	}

	/**
	 * 获取敏感词数量，默认最大匹配模式
	 *
	 * @param text
	 */
	public int count(final String text) {
		return this.count(text, MatchType.MAX);
	}
	
	public MatchFlag match(final char[] charText, int start, MatchType matchType) {
		boolean hit = false;
		MatchFlag hitFlag = null;
		int index = 0; // match start index
		int length = 0; // match length
		Map<String, DFANode<SensitiveWordType>> current = this.getWordDFAModel().getRoot();
		for (int i = start; i < charText.length; i++) {
			String c = String.valueOf(charText[i]);
			DFANode<SensitiveWordType> node = current.get(c);
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
					matchFlag.type = node.data;
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
	static class MatchFlag {
		int index; // match start index
		int length; // match length
		SensitiveWordType type; // 敏感词类型
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
	 * 替换方式
	 * CHAR: 单个字符替换成
	 * WORD: 整个词汇替换成
	 */
	public enum ReplaceType {
		CHAR, WORD
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
		Map<String, DFANode<SensitiveWordType>> current = this.getWordDFAModel().getRoot();
		for (int i = 0; i < charText.length; i++) {
			String c = String.valueOf(charText[i]);
			DFANode<SensitiveWordType> node = current.get(c);
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
					matchFlag.type = node.data;
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

	public void setWordDFAModel(DFAModel<SensitiveWordType> wordDFAModel) {
		this.wordDFAModel = wordDFAModel;
	}

	public DFAModel<SensitiveWordType> getWordDFAModel() {
		return this.wordDFAModel;
	}
}

