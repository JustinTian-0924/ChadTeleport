package basementhost.randomchad.service;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class HomeLimitService {

	private static final int DEFAULT_MAX_HOMES = 1;

	public int getMaxHomes(Player player) {
		if (player.hasPermission("chadteleport.home.*")) {
			return Integer.MAX_VALUE;
		}

		int max = -1;

		for (PermissionAttachmentInfo permissionInfo : player.getEffectivePermissions()) {
			if (!permissionInfo.getValue()) {
				continue;
			}

			String permission = permissionInfo.getPermission();
			if (permission == null) {
				continue;
			}

			if (!permission.startsWith("chadteleport.home.")) {
				continue;
			}

			String suffix = permission.substring("chadteleport.home.".length());
			if (suffix.equals("*")) {
				return Integer.MAX_VALUE;
			}

			try {
				int value = Integer.parseInt(suffix);
				if (value > max) {
					max = value;
				}
			} catch (NumberFormatException ignored) {
				// skip if its not a number
			}
		}

		// 1 sethome allowance if not explicitelly setted；home.0 will make that player cannot set home。
		return max >= 0 ? max : DEFAULT_MAX_HOMES;
	}

	public boolean hasFreeSlot(Player player, int currentCount) {
		int maxHomes = getMaxHomes(player);
		return currentCount < maxHomes;
	}
}
