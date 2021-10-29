package net.cupofcode.instruments;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Utils {

	public static HashMap<Player, HashMap<Integer, ItemStack>> inventoryMap = new HashMap<>();

	private static String[] notes = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

	public static void storeInventory(Player player) {
		Inventory inventory = player.getInventory();
		HashMap<Integer, ItemStack> items = new HashMap<>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null) {
				items.put(i, inventory.getItem(i));
			}
		}

		inventoryMap.put(player, items);
	}

	public static void loadInventory(Player player) {
		if (!inventoryMap.containsKey(player))
			return;

		clearInventory(player);

		Inventory inventory = player.getInventory();
		HashMap<Integer, ItemStack> items = inventoryMap.get(player);
		for (Integer i : items.keySet()) {
			inventory.setItem(i, items.get(i));
		}

		inventoryMap.remove(player);
	}

	public static void clearInventory(Player player) {
		for (int i = 0; i < 36; i++) {
			if (player.getInventory().getItem(i) != null) {
				player.getInventory().setItem(i, new ItemStack(Material.AIR));
			}
		}
	}

	public static String[] getMajorTriad(String note) {
		int position = getIndexOfNote(note);

		return new String[] { notes[position], notes[(position + 4) % notes.length],
				notes[(position + 7) % notes.length] };
	}

	public static String[] getMinorTriad(String note) {
		int position = getIndexOfNote(note);

		return new String[] { notes[position], notes[(position + 3) % notes.length],
				notes[(position + 7) % notes.length] };
	}

	public static int getIndexOfNote(String note) {
		for (int i = 0; i < notes.length; i++) {
			if (notes[i].equals(note)) {
				return i;
			}
		}
		return -1;
	}

	public static String formatString(String key) {
		String newKey = key.toLowerCase().replaceAll("_", " ");
		newKey = StringUtils.capitalize(newKey);
		return ChatColor.RESET + newKey;
	}

}
