package basementhost.randomchad.service;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class HomeLimitService {

	public int getMaxHomes(Player player) {
		if (player.hasPermission("chadteleport.home.*")) {
			return Integer.MAX_VALUE;
		}

		int max = 0;

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
				// 不是数字就跳过
			}
		}

		return max;
	}

	public boolean hasFreeSlot(Player player, int currentCount) {
		int maxHomes = getMaxHomes(player);
		return currentCount < maxHomes;
	}
}