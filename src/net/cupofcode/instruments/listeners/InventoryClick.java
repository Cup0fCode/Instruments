package net.cupofcode.instruments.listeners;

import net.cupofcode.instruments.Instrument;
import net.cupofcode.instruments.Instruments;
import net.cupofcode.instruments.Scale;
import net.cupofcode.instruments.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryClick implements Listener {

	private Instruments instance = Instruments.getInstance();

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (e.getClickedInventory() == null)
			return;

		if (!instance.getInstrumentManager().containsKey(p))
			return;

		Instrument instrument = instance.getInstrumentManager().get(p);

		if (instrument.isHotBarMode()) {
			if(instrument.getScalesInventory().isQuickPlay()) {
				if(e.getSlot() == 0 || e.getSlot() > 35) e.setCancelled(true);
			}

			return;
		}

		if (e.getView().getTitle().contains("Instruments:")
				&& !e.getClickedInventory().getType().equals(InventoryType.PLAYER)
				&& e.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {
			e.setCancelled(true);

			// prevent double clicking causing an extra note to be played
			if (e.getClick() == ClickType.DOUBLE_CLICK)
				return;
			
			if (e.getCurrentItem().getType().equals(Material.PAPER)) {
				instrument.playScales();
				return;
			}

			boolean useChords = true;

			int octave = 1;
			if (e.getRawSlot() > 26)
				octave = 0;

			// Player clicked a natural/sharp note
			if (e.getCurrentItem().getType().equals(Material.WHITE_STAINED_GLASS_PANE)
					|| e.getCurrentItem().getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
				String note = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

				if (useChords) {
					if (e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)) {
						boolean minor = e.getClick().equals(ClickType.SHIFT_LEFT) ? false : true;
						String[] notes = minor ? Utils.getMinorTriad(note) : Utils.getMajorTriad(note);
						for (String pNote : notes) {
							instrument.playNote(pNote, octave);
						}
						return;
					}
				}

				instrument.playNote(note, octave);
				return;
			}

			return;
		}

		if (e.getView().getTitle().contains("Scales") && !e.getClickedInventory().getType().equals(InventoryType.PLAYER)
				&& e.getView().getTopInventory().getType().equals(InventoryType.CHEST)) {
			e.setCancelled(true);

			String itemName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

			if (itemName.contains(instrument.getInstrumentType().toString())) {
				instrument.play();
				return;
			}

			if (e.getCurrentItem().getType().equals(Material.PAPER)) {
				Scale selectedScale = Scale.getScaleByName(itemName);

				if (selectedScale == null)
					return;

				instrument.playHotbar(selectedScale);
				return;
			}

			if (e.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE) ||
					e.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE)) {
				boolean quickPlay = instrument.getScalesInventory().isQuickPlay();
				instrument.getScalesInventory().setQuickPlay(!quickPlay);

				instrument.playScales();
				return;
			}

			if (e.getCurrentItem().getType().equals(Material.ARROW)) {
				int currPage = instrument.getScalesInventory().getPage();
				if (itemName.contains("Next")) {
					instrument.getScalesInventory().setPage(currPage + 1);
				} else {
					if (currPage != 0) {
						instrument.getScalesInventory().setPage(currPage - 1);
					}
				}
				instrument.playScales();
				return;
			}
		}
	}
}
