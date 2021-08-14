package net.cupofcode.instruments.listeners;

import net.cupofcode.instruments.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    private Instruments instance = Instruments.getInstance();

    // Use instrument item
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if(event.getItem() == null)
            return;

        if(instance.getInstrumentManager().containsKey(p)) {
            Instrument instrument = instance.getInstrumentManager().get(p);

            if(!instrument.isHotBarMode()) return;

            if(InstrumentType.getInstrumentTypeByItemStack(event.getItem()) == null) return;

            // Prevent players from using the wooden hoe item
            if(this.isUsingHoe(event)) event.setCancelled(true);

            // Exit player from hot play mode
            if(p.getInventory().getHeldItemSlot() == 0) {
                if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    Utils.loadInventory(p);

                    for (int i = 0; i < 9; i++) {
                        if (p.getInventory().getItem(i) == null) continue;
                        if (p.getInventory().getItem(i).equals(instrument.getInstrumentType().getItemStack())) {
                            p.getInventory().setHeldItemSlot(i);
                            break;
                        }
                    }

                    instrument.setHotBarMode(false);
                    instrument.play();
                }

                return;
            }

            // Don't interact if in quick play
            if(instrument.getScalesInventory().isQuickPlay()) return;

            int octave = 0;
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                octave = 1;
            }

            String note = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());

            boolean foundNote = false;
            for(String scaleNote : Scale.notes) {
                if(scaleNote.equals(note)) {
                    foundNote = true;
                    break;
                }
            }

            if(!foundNote) return;

            if(p.isSneaking()) {
                String[] notes = Utils.getMajorTriad(note);
                for (String pNote : notes) {
                    instrument.playNote(pNote, octave);
                }
                return;
            }

            instrument.playNote(note, octave);
            return;
        }

        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;

        InstrumentType instrumentType = InstrumentType.getInstrumentTypeByItemStack(event.getItem());

        if(instrumentType == null) return;

        if (instance.getConfig().getBoolean("settings.instruments.permissions")
                && !p.hasPermission("instruments.use"))
            return;

        if(this.isUsingHoe(event)) event.setCancelled(true);

        new Instrument(instrumentType, p).play();
    }

    private boolean isUsingHoe(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return false;

        Material clicked = event.getClickedBlock().getType();

        if (clicked != Material.DIRT &&
                clicked != Material.GRASS_BLOCK
                && clicked != Material.GRASS_PATH
                && clicked != Material.COARSE_DIRT)
            return false;

        if (event.getItem() == null)
            return false;

        return true;
    }

}
