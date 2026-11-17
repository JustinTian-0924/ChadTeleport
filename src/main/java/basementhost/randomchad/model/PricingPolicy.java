package basementhost.randomchad.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public record PricingPolicy(Map<String, PricingRule> defaults, List<PricingGroup> groups) {

	public static final List<String> COMMANDS = List.of("tpa", "spawn", "home", "warp");

	public PricingPolicy {
		defaults = Map.copyOf(defaults);
		if (defaults.size() != COMMANDS.size() || !defaults.keySet().containsAll(COMMANDS)) {
			throw new IllegalArgumentException("Default pricing must define exactly tpa, spawn, home and warp.");
		}
		groups = groups.stream().sorted(Comparator.comparingInt(PricingGroup::priority).reversed()
				.thenComparing(PricingGroup::id)).toList();
		HashSet<String> ids = new HashSet<>();
		for (PricingGroup group : groups) {
			if (!ids.add(group.id())) {
				throw new IllegalArgumentException("Duplicate pricing group ID: " + group.id());
			}
		}
	}

	public PricingRule resolve(String command, Predicate<String> hasPermission) {
		PricingRule fallback = defaults.get(command);
		if (fallback == null) {
			throw new IllegalArgumentException("No pricing rule for command: " + command);
		}
		// Resolve each command separately, so a partial group never hides another group's command rule.
		for (PricingGroup group : groups) {
			PricingRule rule = group.commands().get(command);
			if (rule != null && hasPermission.test(group.permission())) {
				return rule;
			}
		}
		return fallback;
	}
}
