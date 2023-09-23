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
