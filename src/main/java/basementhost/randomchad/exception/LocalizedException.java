package basementhost.randomchad.exception;

import java.util.Collections;
import java.util.Map;

public class LocalizedException extends RuntimeException {

	private final String messageKey;
	private final Map<String, String> placeholders;

	public LocalizedException(String messageKey) {
		this(messageKey, Collections.emptyMap());
	}

	public LocalizedException(String messageKey, Map<String, String> placeholders) {
		super(messageKey);
		this.messageKey = messageKey;
		this.placeholders = placeholders;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Map<String, String> getPlaceholders() {
		return placeholders;
	}
}