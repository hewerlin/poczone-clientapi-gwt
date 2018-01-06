package net.poczone.backend.client.impl;

import java.util.Random;

public class IDGenerator {
	private static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
	private static int LENGTH = 8;
	private static String PATTERN = "[" + CHARS + "]{" + LENGTH + "}";

	private static Random random = new Random();

	public static String generateID() {
		char[] chars = new char[LENGTH];
		for (int i = 0; i < LENGTH; i++) {
			chars[i] = CHARS.charAt(random.nextInt(CHARS.length()));
		}
		return new String(chars);
	}

	public static boolean isID(String value) {
		return value != null && value.matches(PATTERN);
	}

	public static float getFraction(String str, int max) {
		int sum = 0;
		int total = 1;
		for (int i = 0; i < max && i < str.length(); i++) {
			int index = CHARS.indexOf(str.substring(i, i + 1));
			sum = sum * CHARS.length() + Math.max(0, index);
			total *= CHARS.length();
		}
		return 1.0f * sum / total;
	}
}
