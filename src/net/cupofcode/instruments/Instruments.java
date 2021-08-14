package net.cupofcode.instruments;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.cupofcode.instruments.bstats.Metrics;
import net.cupofcode.instruments.commands.InstrumentsCommand;
import net.cupofcode.instruments.commands.InstrumentsTabCompleter;
import net.cupofcode.instruments.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Instruments extends JavaPlugin {

	private static Instruments instance;
	private HashMap<Player, Instrument> instrumentManager = new HashMap<>();
	private File configFile;
	private FileConfiguration config;

	@Override
	public void onEnable() {
		instance = this;

		loadConfig();

		// Set instrument names & modelId
		for (InstrumentType instrumentType : InstrumentType.values()) {
			String name = config.getString("settings.instruments.name." + instrumentType.getKey());
			int modelId = config.getInt("settings.instruments.modelId." + instrumentType.getKey());
			instrumentType.setName(name);
			instrumentType.setModelId(modelId);
		}

		getCommand("instruments").setExecutor(new InstrumentsCommand());

		getCommand("instruments").setTabCompleter(new InstrumentsTabCompleter());

		this.registerListeners(new InventoryClick(), new InventoryClose(), new PlayerInteract(),
				new PlayerDrop(), new PlayerPickup(), new PlayerJoin(), new PlayerDeath(),
                new PlayerQuit(), new BlockBreak(), new PlayerAttack(), new PlayerItemHeld(), new PlayerRespawn(), new InventoryOpen(), new PlayerSwapItem(), new PlayerEntityInteract(), new ItemCraft());

		if (config.getBoolean("settings.instruments.recipe.enabled"))
			this.addBukkitRecipes();

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

		// Disables arm swing animation while player is in hotbar mode
		protocolManager
				.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ANIMATION) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (event.getPacketType() == PacketType.Play.Server.ANIMATION) {
							int senderId = event.getPacket().getIntegers().read(0);

							Player sender = null;
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								if (onlinePlayer.getEntityId() == senderId)
									sender = onlinePlayer;
							}

							if (sender == null || !instrumentManager.containsKey(sender))
								return;

							if (instrumentManager.get(sender).isHotBarMode())
								event.setCancelled(true);
						}
					}
				});

		// Add bStats
		Metrics metrics = new Metrics(this, 9792);
		Bukkit.getLogger()
				.info("[Instruments] bStats: " + metrics.isEnabled() + " plugin ver: " + getDescription().getVersion());

		metrics.addCustomChart(new Metrics.SimplePie("plugin_version", () -> getDescription().getVersion()));
	}

	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!instrumentManager.containsKey(player))
				continue;

			if (instrumentManager.get(player).isHotBarMode())
				Utils.loadInventory(player);

			instrumentManager.remove(player);
		}
	}

	private void registerListeners(Listener... listeners) {
		Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
	}

	public HashMap<Player, Instrument> getInstrumentManager() {
		return instrumentManager;
	}

	public static Instruments getInstance() {
		return instance;
	}

	private void loadConfig() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = YamlConfiguration.loadConfiguration(configFile);

		HashMap<String, Object> defaultConfig = new HashMap<>();

		HashMap<String, String> fluteRecipe = new HashMap<>();
		fluteRecipe.put("I", Material.IRON_INGOT.toString());
		fluteRecipe.put("G", Material.GOLD_INGOT.toString());

		ArrayList<String> fluteRecipeShape = new ArrayList<String>() {
			{
				add("IGG");
			}
		};

		HashMap<String, String> guitarRecipe = new HashMap<>();
		guitarRecipe.put("W", Material.OAK_WOOD.toString());
		guitarRecipe.put("I", Material.IRON_INGOT.toString());
		guitarRecipe.put("S", Material.STRING.toString());
		guitarRecipe.put("Z", Material.STICK.toString());
		guitarRecipe.put("A", Material.AIR.toString());

		ArrayList<String> guitarRecipeShape = new ArrayList<String>() {
			{
				add("WWA");
				add("ISZ");
				add("WWA");
			}
		};

		HashMap<String, String> bassGuitarRecipe = new HashMap<>();
		bassGuitarRecipe.put("W", Material.OAK_LOG.toString());
		bassGuitarRecipe.put("I", Material.IRON_INGOT.toString());
		bassGuitarRecipe.put("S", Material.STRING.toString());
		bassGuitarRecipe.put("Z", Material.STICK.toString());
		bassGuitarRecipe.put("A", Material.AIR.toString());

		ArrayList<String> bassGuitarRecipeShape = new ArrayList<String>() {
			{
				add("WWA");
				add("ISZ");
				add("WWA");
			}
		};

		HashMap<String, String> banjoRecipe = new HashMap<>();
		banjoRecipe.put("W", Material.OAK_WOOD.toString());
		banjoRecipe.put("I", Material.IRON_INGOT.toString());
		banjoRecipe.put("S", Material.WHITE_WOOL.toString());
		banjoRecipe.put("Z", Material.STICK.toString());
		banjoRecipe.put("A", Material.AIR.toString());

		ArrayList<String> banjoRecipeShape = new ArrayList<String>() {
			{
				add("WWA");
				add("ISZ");
				add("WWA");
			}
		};

		HashMap<String, String> pianoRecipe = new HashMap<>();
		pianoRecipe.put("W", Material.OAK_WOOD.toString());
		pianoRecipe.put("I", Material.IRON_INGOT.toString());
		pianoRecipe.put("F", Material.FLINT.toString());

		ArrayList<String> pianoRecipeShape = new ArrayList<String>() {
			{
				add("WWW");
				add("IFI");
			}
		};

		HashMap<String, String> plingRecipe = new HashMap<>();
		plingRecipe.put("R", Material.REDSTONE.toString());
		plingRecipe.put("I", Material.IRON_INGOT.toString());
		plingRecipe.put("F", Material.FLINT.toString());

		ArrayList<String> plingRecipeShape = new ArrayList<String>() {
			{
				add("FFF");
				add("III");
				add("IRI");
			}
		};

		HashMap<String, String> snareDrumRecipe = new HashMap<>();
		snareDrumRecipe.put("W", Material.OAK_WOOD.toString());
		snareDrumRecipe.put("I", Material.PAPER.toString());
		snareDrumRecipe.put("A", Material.AIR.toString());

		ArrayList<String> snareDrumRecipeShape = new ArrayList<String>() {
			{
				add("AWA");
				add("WIW");
				add("AWA");
			}
		};

		HashMap<String, String> bassDrumRecipe = new HashMap<>();
		bassDrumRecipe.put("W", Material.OAK_LOG.toString());
		bassDrumRecipe.put("I", Material.PAPER.toString());
		bassDrumRecipe.put("A", Material.AIR.toString());

		ArrayList<String> bassDrumRecipeShape = new ArrayList<String>() {
			{
				add("AWA");
				add("WIW");
				add("AWA");
			}
		};

		HashMap<String, String> sticksRecipe = new HashMap<>();
		sticksRecipe.put("S", Material.STICK.toString());

		ArrayList<String> sticksRecipeShape = new ArrayList<String>() {
			{
				add("SS");
			}
		};

		HashMap<String, String> ironXylophoneRecipe = new HashMap<>();
		ironXylophoneRecipe.put("W", Material.OAK_WOOD.toString());
		ironXylophoneRecipe.put("I", Material.IRON_INGOT.toString());
		ironXylophoneRecipe.put("A", Material.AIR.toString());

		ArrayList<String> ironXylophoneRecipeShape = new ArrayList<String>() {
			{
				add("AWI");
				add("WIA");
				add("IAA");
			}
		};

		HashMap<String, String> xylophoneRecipe = new HashMap<>();
		xylophoneRecipe.put("W", Material.OAK_WOOD.toString());
		xylophoneRecipe.put("G", Material.GOLD_INGOT.toString());
		xylophoneRecipe.put("A", Material.AIR.toString());

		ArrayList<String> xylophoneRecipeShape = new ArrayList<String>() {
			{
				add("AWG");
				add("WGA");
				add("GAA");
			}
		};

		HashMap<String, String> bellRecipe = new HashMap<>();
		bellRecipe.put("G", Material.GOLD_INGOT.toString());
		bellRecipe.put("S", Material.STICK.toString());
		bellRecipe.put("A", Material.AIR.toString());

		ArrayList<String> bellRecipeShape = new ArrayList<String>() {
			{
				add("AGA");
				add("GSG");
			}
		};

		HashMap<String, String> bitRecipe = new HashMap<>();
		bitRecipe.put("I", Material.IRON_INGOT.toString());
		bitRecipe.put("G", Material.GOLD_INGOT.toString());
		bitRecipe.put("R", Material.REDSTONE.toString());

		ArrayList<String> bitRecipeShape = new ArrayList<String>() {
			{
				add("III");
				add("RGR");
				add("RRR");
			}
		};

		HashMap<String, String> chimeRecipe = new HashMap<>();
		chimeRecipe.put("W", Material.OAK_WOOD.toString());
		chimeRecipe.put("G", Material.GOLD_INGOT.toString());

		ArrayList<String> chimeRecipeShape = new ArrayList<String>() {
			{
				add("WWW");
				add("GGG");
				add("GGG");
			}
		};

		HashMap<String, String> cowBellRecipe = new HashMap<>();
		cowBellRecipe.put("I", Material.IRON_INGOT.toString());
		cowBellRecipe.put("S", Material.STICK.toString());
		cowBellRecipe.put("A", Material.AIR.toString());

		ArrayList<String> cowBellRecipeShape = new ArrayList<String>() {
			{
				add("AIA");
				add("ISI");
			}
		};

		HashMap<String, String> didgeridooRecipe = new HashMap<>();
		didgeridooRecipe.put("I", Material.IRON_INGOT.toString());
		didgeridooRecipe.put("W", Material.OAK_PLANKS.toString());
		didgeridooRecipe.put("M", Material.OAK_LOG.toString());

		ArrayList<String> didgeridooRecipeShape = new ArrayList<String>() {
			{
				add("IWM");
			}
		};

		// itemName must be a key in InventoryType
		this.addConfigRecipe("piano", pianoRecipe, pianoRecipeShape);
		this.addConfigRecipe("bass_drum", bassDrumRecipe, bassDrumRecipeShape);
		this.addConfigRecipe("snare_drum", snareDrumRecipe, snareDrumRecipeShape);
		this.addConfigRecipe("sticks", sticksRecipe, sticksRecipeShape);
		this.addConfigRecipe("bass_guitar", bassGuitarRecipe, bassGuitarRecipeShape);
		this.addConfigRecipe("flute", fluteRecipe, fluteRecipeShape);
		this.addConfigRecipe("bell", bellRecipe, bellRecipeShape);
		this.addConfigRecipe("guitar", guitarRecipe, guitarRecipeShape);
		this.addConfigRecipe("chime", chimeRecipe, chimeRecipeShape);
		this.addConfigRecipe("xylophone", xylophoneRecipe, xylophoneRecipeShape);
		this.addConfigRecipe("iron_xylophone", ironXylophoneRecipe, ironXylophoneRecipeShape);
		this.addConfigRecipe("cow_bell", cowBellRecipe, cowBellRecipeShape);
		this.addConfigRecipe("didgeridoo", didgeridooRecipe, didgeridooRecipeShape);
		this.addConfigRecipe("bit", bitRecipe, bitRecipeShape);
		this.addConfigRecipe("banjo", banjoRecipe, banjoRecipeShape);
		this.addConfigRecipe("pling", plingRecipe, plingRecipeShape);

		// add Instrument Name to config
		for (InstrumentType instrumentType : InstrumentType.values()) {
			String configLoc = "settings.instruments.name." + instrumentType.getKey();
			if (!config.contains(configLoc)) {
				config.set(configLoc, instrumentType.getName());
			}
		}
		// add Instrument modelId to config
		for (InstrumentType instrumentType : InstrumentType.values()) {
			String configLoc = "settings.instruments.modelId." + instrumentType.getKey();
			if (!config.contains(configLoc)) {
				config.set(configLoc, instrumentType.getModelId());
			}
		}

		defaultConfig.put("settings.instruments.recipe.enabled", true);
		defaultConfig.put("settings.instruments.resourcepack.enabled", true);
		defaultConfig.put("settings.instruments.permissions", true);

		for (String key : defaultConfig.keySet()) {
			if (!config.contains(key)) {
				config.set(key, defaultConfig.get(key));
			}
		}

		this.saveConfig();
	}

	private void addConfigRecipe(String itemName, HashMap<String, String> recipe, ArrayList<String> shape) {
		HashMap<String, Object> defaultConfig = new HashMap<>();

		String configLoc = "settings.instruments.recipe." + itemName;

		defaultConfig.put(configLoc + ".shape", shape);

		if (!config.contains(configLoc + ".ingredients")) {
			for (String key : recipe.keySet()) {
				defaultConfig.put(configLoc + ".ingredients." + key, recipe.get(key));
			}
		}

		for (String key : defaultConfig.keySet()) {
			if (!config.contains(key)) {
				config.set(key, defaultConfig.get(key));
			}
		}
	}

	private void addBukkitRecipes() {
		for (String instrumentKey : config.getConfigurationSection("settings.instruments.recipe").getKeys(false)) {
			if (instrumentKey.equals("enabled"))
				continue;

			if (InstrumentType.getInstrumentTypeByKey(instrumentKey) == null) {
				Bukkit.getLogger().warning("[Instruments] Error when loading recipes, " + instrumentKey
						+ " is not a recognized instrument.");
				return;
			}

			ItemStack instrumentItemStack = InstrumentType.getInstrumentTypeByKey(instrumentKey).getItemStack();
			NamespacedKey key = new NamespacedKey(this, instrumentKey);
			ShapedRecipe recipe = new ShapedRecipe(key, instrumentItemStack);

			String configPath = "settings.instruments.recipe." + instrumentKey;

			ArrayList<String> shapeArr = (ArrayList<String>) config.get(configPath + ".shape");
			recipe.shape(shapeArr.toArray(new String[shapeArr.size()]));

			String ingredientsPath = configPath + ".ingredients";

			for (String ingredientKey : config.getConfigurationSection(ingredientsPath).getKeys(false)) {
				recipe.setIngredient(ingredientKey.charAt(0),
						Material.valueOf((String) config.get(ingredientsPath + "." + ingredientKey)));
			}

			Bukkit.addRecipe(recipe);
		}
	}

	@Override
	public void saveConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}
}
