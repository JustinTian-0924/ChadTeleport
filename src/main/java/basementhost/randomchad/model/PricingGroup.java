package basementhost.randomchad.model;

import java.util.Map;
import java.util.Objects;

public record PricingGroup(String id, String permission, int priority, Map<String, PricingRule> commands) {

	public PricingGroup {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(permission, "permission");
		if (!id.matches("[a-z0-9_-]+")) {
			throw new IllegalArgumentException("Pricing group IDs must use lowercase letters, digits, underscores or hyphens: " + id);
		}
		if (!permission.matches("[a-z0-9_-]+(?:\\.[a-z0-9_-]+)+")) {
			throw new IllegalArgumentException("Use a specific lowercase permission node, such as chadteleport.pricing.vip: " + permission);
		}
		commands = Map.copyOf(commands);
		if (commands.isEmpty()) {
			throw new IllegalArgumentException("Pricing group '" + id + "' must define at least one command.");
		}
		for (String command : commands.keySet()) {
			if (!PricingPolicy.COMMANDS.contains(command)) {
				throw new IllegalArgumentException("Unsupported pricing command in group '" + id + "': " + command);
			}
		}
	}
}
