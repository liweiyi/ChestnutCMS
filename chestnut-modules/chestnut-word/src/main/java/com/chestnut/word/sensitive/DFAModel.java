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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chestnut.common.utils.StringUtils;

/**
 * 敏感词DFA算法模型
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class DFAModel<T> {

	private final Map<String, DFANode<T>> root = new HashMap<>();
    
//	public void load(Set<String> blackList, Set<String> whiteList) {
//		this.addWords(blackList, SensitiveWordType.BLACK);
//		this.addWords(whiteList, SensitiveWordType.WHITE);
//	}
	
	public Map<String, DFANode<T>> getRoot() {
		return this.root;
	}



	protected void addWord(String word, T data) {
		this.addWords(Set.of(word), data);
	}

	protected void addWords(Set<String> wordList, T data) {
		Map<String, DFANode<T>> current = null;
        for (String key : wordList) {
            current = root;
            String parentKey = StringUtils.EMPTY;
            for (int i = 0; i < key.length(); i++) {
                String word = String.valueOf(key.charAt(i));
                DFANode<T> node = current.get(word);
                if (node != null) {
                    if (i == key.length() - 1) {
                    	node.end = true;
                    	node.data = data;
                    } else {
	                	if (node.children == null) {
	                		node.children = new HashMap<>();
	                	}
	                    current = node.children;
	                    parentKey = node.key;
                    }
                } else {
	            	node = new DFANode<T>();
	            	node.key = word;
	            	node.parentKey = parentKey;
	                current.put(word, node);
	
	                if (i == key.length() - 1) {
	                    // last char
	                	node.end = true;
	                	node.data = data;
	                } else {
	                	node.children = new HashMap<>();
	                    current = node.children;
	                    parentKey = node.key;
	                }
                }
            }
        }
	}
	
	public void removeWord(String word) {
		Map<String, DFANode<T>> current = null;
        List<DFANode<T>> pathNodeList = new ArrayList<>();
        current = root;
        for (int i = 0; i < word.length(); i++) {
            String keyChar = String.valueOf(word.charAt(i));

            DFANode<T> node = current.get(keyChar);
            if (node != null) {
                current = node.children;
                pathNodeList.add(node);
            } else {
                return; // mismatch
            }
            if (i == word.length() - 1 && !node.end) {
            	return; // not end node
            }
        }
        
        for (int j = pathNodeList.size() - 1; j >= 0; j--) {
            DFANode<T> node = pathNodeList.get(j);
            if (j == pathNodeList.size() - 1) {
            	// 路径末尾节点删除时要判断节点是否含有子节点，例如：[“中国”, “中国人”]， 删除“中国”时存在子节点则不能删除，标记国字符节点end为false
	        	if (node.children == null) {
	                if (StringUtils.EMPTY.equals(node.parentKey)) {
	                	this.root.remove(node.key);
	                } else {
	                	pathNodeList.get(j - 1).children.remove(node.key);
	                	if (pathNodeList.get(j - 1).children.size() == 0) {
	                		pathNodeList.get(j - 1).children = null; // 释放空的子节点集合
	                	}
	                }
	        	} else {
	        		node.end = false; 
	        		return;
	        	}
            } else {
            	// 路径中节点删除时要判断节点非结束字符，例如：[“中国”, “中国人”]， 删除“中国人”时“中国”不能删除
	        	if (node.children == null && !node.end) {
	                if (StringUtils.EMPTY.equals(node.parentKey)) {
	                	this.root.remove(node.key);
	                } else {
	                	pathNodeList.get(j - 1).children.remove(node.key);
	                	if (pathNodeList.get(j - 1).children.size() == 0) {
	                		pathNodeList.get(j - 1).children = null; // 释放空的子节点集合
	                	}
	                }
	        	}
            }
        }
    }
}
