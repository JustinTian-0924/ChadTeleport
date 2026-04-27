package basementhost.randomchad.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class HomeNameValidator {

	private static final int MIN_LENGTH = 1;
	private static final int MAX_LENGTH = 16;
	private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

	private HomeNameValidator() {
	}

	public static boolean isValid(String input) {
		if (input == null) {
			return false;
		}

		String trimmed = input.trim();

		if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
			return false;
		}

		return ALLOWED_PATTERN.matcher(trimmed).matches();
	}

	public static String normalize(String input) {
		if (input == null || input.isBlank()) {
			return "default";
		}
		return input.toLowerCase(Locale.ROOT);
	}

	public static int getMinLength() {
		return MIN_LENGTH;
	}

	public static int getMaxLength() {
		return MAX_LENGTH;
	}
}