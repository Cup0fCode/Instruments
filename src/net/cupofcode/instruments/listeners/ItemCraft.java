package net.cupofcode.instruments.listeners;

import net.cupofcode.instruments.InstrumentType;
import net.cupofcode.instruments.Instruments;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemCraft implements Listener {

    private Instruments instance = Instruments.getInstance();

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        if(event.getInventory().getResult() != null) {
            ItemStack result = event.getInventory().getResult();
            InstrumentType instrumentType = InstrumentType.getInstrumentTypeByItemStack(result);
            if(instrumentType != null) {
                if (instance.getConfig().getBoolean("settings.instruments.permissions")
                        && !event.getWhoClicked().hasPermission("instruments.use"))
                    event.setCancelled(true);
            }
        }
    }

}
