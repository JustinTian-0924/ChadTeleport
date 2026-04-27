package basementhost.randomchad.model;

import org.bukkit.Location;

public class HomeEntry {

	private final String name;
	private final Location location;

	public HomeEntry(String name, Location location) {
		this.name = name;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return location;
	}
}