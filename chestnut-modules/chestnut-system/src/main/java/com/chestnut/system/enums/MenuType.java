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
package com.chestnut.system.enums;

public enum MenuType {

	Directory("M"), Menu("C"), Button("F");
	
	private String value;

	MenuType(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value;
	}
	
	public static boolean isDirectory(String v) {
		return Directory.value.equals(v);
	}
	
	public static boolean isMenu(String v) {
		return Menu.value.equals(v);
	}
	
	public static boolean isButton(String v) {
		return Button.value.equals(v);
	}
}
