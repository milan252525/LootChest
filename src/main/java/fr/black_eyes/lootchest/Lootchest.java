package fr.black_eyes.lootchest;

import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.material.MaterialData;

import fr.black_eyes.events.LootChestSpawnEvent;
import fr.black_eyes.lootchest.falleffect.FallingPackageEntity;
import fr.black_eyes.lootchest.particles.Particle;
import lombok.Getter;
import lombok.Setter;
public class Lootchest {
	/**
	 * @return the Lootchest name
	 */
	@Getter /**
	 * @param name the name to give to the lootchest
	 */
	@Setter private String name;
	/**
	 * @param globalLoc The static location of the lootchest, which is always used to choose the random location
	 */
	@Setter 		private Location globalLoc;
	/**
	 * @param randomLoc the random location that changes every time a lootchest respawns. Can be disabled to only use global location
	 */
	@Setter 		private Location randomLoc;
	/**
	 * @return The inventory of the lootchest, used to give it to someone, or fill the lootchest
	 */
	@Getter			private Inventory inv;
	/**
	 * @return the value of the fall boolean, which says if the LootChest should display a fall effect or not
	 */
	@Getter /**
	 * @param fall says if the LootChest should display a fall effect or not
	 */
	@Setter private Boolean fall;
	/**
	 * @return An array of integers, representing chances of each item in the chest
	 */
	@Getter			 Integer[] chances;
	/**
	 * @return a string representing the direction of the chest block (north, east, south, or east)
	 */
	@Getter /**
	 * @param direction a string representing the direction of the chest block (north, east, south, or east)
	 */
	@Setter private String direction;
	/**
	 * @return the text displayed by the hologram of the lootchest
	 */
	@Getter private String holo;
	/**
	 * @return the time between two spawns of the lootchest. 
	 */
	@Getter /**
	 * @param time the time between two spawns of the lootchest. -1 to disable auto respawn
	 */
	@Setter private long time;
	/**
	 * @return the last respawn date of the chest, in milliseconds.
	 */
	@Getter /**
	 * @param lastreset the last respawn date of the chest, in milliseconds.
	 */
	private long lastreset;
	/**
	 * @return the particle to spawn around the lootchest
	 */
	@Getter /**
	 * @param particle the particle to spawn around the lootchest
	 */
	@Setter private Particle particle;
	/**
	 * @return the value of respawn_cmd boolean, which says if we should send a broadcast if the chest is respawned manually
	 */
	@Getter /**
	 * @param respawn_cmd boolean, which says if we should send a broadcast if the chest is respawned manually
	 */
	@Setter private Boolean respawn_cmd;
	/**
	 * @return the value of the respawn_natural boolean, which says if we should send a broadcast if the chest is respawned "naturally"
	 */
	@Getter /**
	 * @param respawn_natural boolean, which says if we should send a broadcast if the chest is respawned "naturally"
	 */
	@Setter private Boolean respawn_natural;
	/**
	 * @return the value of the take_msg boolean, which says if we should send a broadcast if the chest taken/looted by a player
	 */
	@Getter /**
	 * @param take_msg boolean, which says if we should send a broadcast if the chest taken/looted by a playe
	 */
	@Setter private Boolean take_msg;
	/**
	 * @return the radius around the global location, to set the randm location
	 */
	@Getter /** 
	 * @param radius the radius around the global location, to set the randm location
	 */
	@Setter private int radius;
	/**
	 * @return the world to spawn the lootchest
	 */
	@Getter /**
	 * @param world the world to spawn the lootchest
	 */
	@Setter private String world;
	/**
	 * @return taken boolean, which says if the chest was looted already or not
	 */
	@Getter /**
	 * @param taken boolean, which says if the chest was looted already or not
	 */
	@Setter private Boolean taken;
	/** 
	 * @return the type of the lootchest, can be chest, trapped chest or barrel
	 */
	 /**
	 * @param type the type of the lootchest, can be chest, trapped chest or barrel
	 */
	@Getter @Setter private Material type;
	/**
	 * @return the hologram object attached to this lootchest
	 */
	@Getter private Hologram hologram;

	@Getter @Setter private long protectionTime;
	
	
	/**
	 * Function used in Main / reload for chest loading
	 * @param naming
	 */
	public Lootchest(String naming) {
		Main main = Main.getInstance();
		Utils utils = main.getUtils();
		Files configFiles = main.getConfigFiles();
		taken = false;
		if(!configFiles.getData().isSet("chests."+naming+".type")){
			type = Mat.CHEST;
		}else {
			String types = configFiles.getData().getString("chests."+naming+".type");
			switch(types) {
				case "TRAPPED_CHEST": type = Mat.TRAPPED_CHEST; break;
				case "BARREL": type = Mat.BARREL; break;
				default: type = Mat.CHEST; break;
			}
		}
		name = naming;
		Integer[] chancesInit = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		chances = chancesInit;
		chancesInit = null;
		inv = Bukkit.createInventory(null, 27);
		globalLoc = utils.getPosition(naming);
		if(configFiles.getData().isSet("chests."+naming+".randomradius")) {
			radius = configFiles.getData().getInt("chests."+naming+".randomradius");
			if(radius > 0) {
				randomLoc = utils.getRandomPosition(naming);
			}else {
				randomLoc = null;
			}
		}else {
			radius = 0;
			randomLoc = null;
		}

		if(configFiles.getData().isSet("chests."+naming+".protectionTime")) {
			protectionTime = configFiles.getData().getLong("chests."+naming+".protectionTime");
		}else {
			protectionTime = Main.configs.defaultRespawnProtection;
		}


		holo = configFiles.getData().getString("chests." + naming + ".holo");
		String part = configFiles.getData().getString("chests." + naming + ".particle");
		if(part.equals("Disabled")) {
			particle = null;
		}else {
			try {
				particle = Particle.valueOf(part);
			}catch(IllegalArgumentException e) {
				particle = Particle.FLAME;
			}
			if(!particle.isSupported()) {
				particle = Particle.FLAME;
			}
		}
		time = configFiles.getData().getInt("chests." + naming + ".time");
		fall =  configFiles.getData().getBoolean("chests." + naming + ".fall");
		try {
		for(String keys : configFiles.getData().getConfigurationSection("chests." + naming + ".inventory").getKeys(false)) {
			inv.setItem(Integer.parseInt(keys), configFiles.getData().getItemStack("chests." + naming + ".inventory." + keys));
			chances[Integer.parseInt(keys)] = configFiles.getData().getInt("chests." + naming + ".chance." + keys);
		}
		}catch(NullPointerException e) {
			main.logInfo("&cMaybe you changed to an older server version recently: chest inventory of "+name+" was lost :/");
		}
		respawn_cmd =  configFiles.getData().getBoolean("chests." + naming + ".respawn_cmd");
		respawn_natural =  configFiles.getData().getBoolean("chests." + naming + ".respawn_natural");
		take_msg =  configFiles.getData().getBoolean("chests." + naming + ".take_message");
		world = configFiles.getData().getString("chests." + naming + ".position.world");
		direction = configFiles.getData().getString("chests." + naming + ".direction");
		lastreset = configFiles.getData().getLong("chests." + name + ".lastreset");
		
		hologram = new Hologram(this);
	}
	
	
	
	/**
	 * Function used for /lc create
	 * @param chest
	 * @param naming
	 */
	public Lootchest(Block chest, String naming){
		Main main = Main.getInstance();
		Utils utils = main.getUtils();

		type = chest.getType();
		taken = false;
		name = naming;
		inv = Bukkit.createInventory(null, 27);
		Integer[] chancesInit = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		chances = chancesInit;
		Inventory inve =  ((InventoryHolder) chest.getState()).getInventory();
		for(int i = 0 ; i < 27 ; i++) {
			if(inve.getItem(i) != null) {
				inv.setItem( i, inve.getItem(i));
				chances[i] =  Main.configs.default_item_chance;
			}
		}
		if(inve.getSize() >27) {
			main.logInfo("&cDo not use double chests to create chests! Only half of the inventory of the chest was registered.");
		}
		fall =  Main.configs.FALL_Enabled;
		respawn_cmd =  Main.configs.NOTE_command_e;
		respawn_natural =  Main.configs.NOTE_natural_e;
		take_msg =  Main.configs.NOTE_message_on_chest_take;
		if( !(Mat.CHEST != Mat.BARREL && chest.getType() == Mat.BARREL) ) {
			direction = utils.getDirection(chest);
		}
		holo = name;
		time =  Main.configs.default_reset_time;
		globalLoc =  chest.getLocation();
		lastreset =  new Timestamp(System.currentTimeMillis()).getTime();
	   	particle =  Particle.valueOf(Main.configs.PART_default_particle);
	   	radius = 0;
	   	world = chest.getWorld().getName();
		((InventoryHolder) chest.getLocation().getBlock().getState()).getInventory().clear();
		chest.getLocation().getBlock().setType(Material.AIR);
		protectionTime = Main.configs.defaultRespawnProtection;
		hologram = new Hologram(this);
	}
	
	
	
	/**
	 * Function used at defined time in config and at plugin stop for saving chests
	 */
	void saveInConfig(){
		Main main = Main.getInstance();
		Utils utils = main.getUtils();
		Files configFiles = main.getConfigFiles();
		configFiles.getData().set("chests." + name + ".inventory", null);
		for(int i = 0 ; i < inv.getSize() ; i++) {
			if(inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR) {
				configFiles.getData().set("chests." + name + ".inventory." + i, inv.getItem(i));
				configFiles.getData().set("chests." + name + ".chance." + i, chances[i]);
			}
		}
		configFiles.getData().set("chests." + name + ".fall", fall);
		configFiles.getData().set("chests." + name + ".type", type.name());
		configFiles.getData().set("chests." + name + ".respawn_cmd", respawn_cmd);
		configFiles.getData().set("chests." + name + ".respawn_natural", respawn_natural);
		configFiles.getData().set("chests." + name + ".take_message", take_msg);
		
		configFiles.getData().set("chests." + name + ".direction", direction);
		configFiles.getData().set("chests." + name + ".holo", holo);
		configFiles.getData().set("chests." + name + ".time", time);
		configFiles.getData().set("chests." + name + ".protectionTime", protectionTime);
		utils.setPosition(name, globalLoc);
		configFiles.getData().set("chests." + name + ".lastreset", lastreset);
		if(particle!=null)
			configFiles.getData().set("chests." +name+ ".particle", particle.name());
		else
			configFiles.getData().set("chests." +name+ ".particle", "Disabled");
		configFiles.getData().set("chests."+name+".randomradius", radius);
		if(randomLoc != null) {
			utils.setRandomPosition(name, randomLoc);
		}

	}

	//fonction pour changer la position d'un coffre
	//function to change a chest location
	public void setLocation(Location loc3) {
		despawn();
		setWorld(loc3.getWorld().getName());
		setGlobalLoc(loc3);
		spawn(true);
	}

	/**
	 * Remove the chest block, the hologram, and the particle
	 * If the chunk isn't loaded before doing this, it will be unloaded after.
	 * @return true if the chunk was loaded before, false if it wasn't
	 */
	public boolean despawn(){
		Location startloc = getActualLocation();
		Boolean loaded = startloc.getWorld().isChunkLoaded((int)startloc.getX()/16, (int)startloc.getZ()/16) ;
		if(Utils.isWorldLoaded(getWorld()) && isGoodType(startloc.getBlock())) {
			Block chest = startloc.getBlock();
			((InventoryHolder) chest.getLocation().getBlock().getState()).getInventory().clear();
			chest.setType(Material.AIR);
			Main.getInstance().getPart().remove(getParticleLocation());
			hologram.remove();
		}
		Boolean loaded2 = startloc.getWorld().isChunkLoaded((int)startloc.getX()/16, (int)startloc.getZ()/16) ;
		if(loaded != loaded2) {
			startloc.getWorld().unloadChunk((int)startloc.getX()/16, (int)startloc.getZ()/16);
			return false;
		}
		return true;
	}

	/**
	 * @return whever config option Minimum_Number_Of_Players_For_Natural_Spawning is respected
	 */
	private static boolean checkIfEnoughPlayers(){
		Integer num = Main.configs.Minimum_Number_Of_Players_For_Natural_Spawning;
		int players = Utils.getPlayersOnServer();
		return (players >= num); 
	}

	private boolean checkIfTimeToRespawn(){
		long tempsactuel = (new Timestamp(System.currentTimeMillis())).getTime();
		long minutes = getTime()*60*1000;
		long tempsenregistre = getLastreset();
		return (tempsactuel - tempsenregistre > minutes);
	}

	/**
	 * used by spawn, spawns the chest
	 * @param name - A Lootchest to spawn
	 * @param block - The block concerned, where the spawn will append
	 * @param theloc - Location of the block
	 * @param force - True if respawned with a command
	 */
	private void createchest( Block block, Location theloc) {
		block.setType(getType());
		Inventory inve = ((InventoryHolder) block.getState()).getInventory();
		Utils.fillInventory(this, inve, true, null);
		MaterialData data = null;
		//if the chest isn't a barrel, we can change its direction
		if( !(Mat.CHEST != Mat.BARREL && getType() == Mat.BARREL) && Main.getVersion()>7) {
				data = block.getState().getData();
			((DirectionalContainer)data).setFacingDirection(BlockFace.valueOf(direction));
			BlockState state = block.getState();
			state.setData(data);
			state.update();
		}
		
		// spawn particles and hologram if needed
		final Location loc2 = getParticleLocation();
		if(getParticle() != null && Main.configs.PART_enable){
			for(Particle part : Main.getInstance().getParticules()) {
				if((""+part).contains(getParticle().name())) {
					Main.getInstance().getPart().put(loc2, part);
				}
			}
		}
		getHologram().setLoc(theloc);
		
		setLastreset();
		if(Main.configs.save_Chest_Locations_At_Every_Spawn) {
			saveInConfig();
			Main.getInstance().getConfigFiles().saveData();
		}
		setTaken(false);
		if(protectionTime >0){
			Long now = (new Timestamp(System.currentTimeMillis())).getTime();
			Main.getInstance().getProtection().put(block.getLocation(), now+protectionTime*1000);
		}
		Bukkit.getPluginManager().callEvent(new LootChestSpawnEvent(this));
		Utils.sheduleRespawn(this);
	}

	//set le coffre et remplir son inventaire, cr§er l'holo en fonction du nom 
	//Restores the chest if it is time to do it, or if we force respawn. 
	public boolean spawn(Boolean force) {
		// if world is not loaded || lootchest was deleted || not enough players
		if(!Utils.isWorldLoaded(getWorld()) || !Main.getInstance().getLootChest().containsValue(this) ) {
			return false;
		}
		// if (there's not enough player || it's not time to respawn) && we didn't force respawn
		if( !checkIfTimeToRespawn() && !force) {
			return false;
		}
		if(!checkIfEnoughPlayers() && !force) {
			Utils.sheduleRespawn(this);
			return false;
		}
		Location actualloc = getActualLocation();
		Boolean chunk_was_loaded = actualloc.getWorld().isChunkLoaded((int)actualloc.getX()/16, (int)actualloc.getZ()/16) ;
		
		Location globalLocation = getPosition();
		Location spawnLoc = globalLocation.clone();
		//if randomspawn is enabled, we get a random location in the radius
		if(getRadius() !=0){
			//if this option is true, we take the location of one of online players randomly.
			if(Main.configs.use_players_locations_for_randomspawn ) {
				globalLocation = Utils.chooseRandomPlayer(getWorld());
				globalLocation = globalLocation!=null?globalLocation:spawnLoc.clone();
			}
			spawnLoc = Utils.getRandomLocation(globalLocation, radius);
			if(spawnLoc == null){
				Main.getInstance().logInfo(Utils.color("&cThe chest " + getName() + " didn't found a good location, so that it couldn't respawn! " ));
				Utils.sheduleRespawn(this);
				return false;
			}
			// whatever happens after, the chest will spawn, so we can set this
			despawn();
			setRandomLoc(spawnLoc.clone());
		}
		// if the chest is already spawned, we despawn it
		despawn();

		// handle natural spawning messages - command respawn messages are handled in command class
		if(!force && getRespawn_natural() ) {
			String natural_msg = Utils.color((((Main.configs.NOTE_natural_msg.replace("[Chest]", holo)).replace("[x]", spawnLoc.getX()+"")).replace("[y]", spawnLoc.getY()+"")).replace("[z]", spawnLoc.getZ()+"").replace("[World]", world));
			if(Main.configs.NOTE_bungee_broadcast) {
				BungeeChannel.bungeeBroadcast(natural_msg);
			}
			else if(!Main.configs.NOTE_per_world_message) {
				Utils.broadcast(natural_msg);	
			}else {
				for(Player p : spawnLoc.getWorld().getPlayers()){
					Utils.sendMultilineMessage(natural_msg, p);
				}
			}
		}

		// make the fall effect
		final Block newblock = spawnLoc.getBlock();
		if(getFall()) {
			int height = Main.configs.FALL_Height;
			Location startloc = new Location(spawnLoc.getWorld(), spawnLoc.getX()+0.5, spawnLoc.getY()+height, spawnLoc.getZ()+0.5);
			new FallingPackageEntity(startloc, chunk_was_loaded, spawnLoc);
			getHologram().remove();
		}
		createchest(newblock, spawnLoc);
		
		return true;
	}

	public Location getParticleLocation() {
		final Location loc2 = getActualLocation().clone();
		loc2.add(0.5,0.5,0.5);
		return loc2;
	}
	

	/**
	 * @return a clone of the global location
	 */
	public Location getPosition() {			return globalLoc.clone();	}
	/**
	 * @return a clone of the random location
	 */
	public Location getRandomPosition() {	return (randomLoc!=null)?randomLoc.clone():getPosition();	}

	/**
	 * @return a clone of the actual location, eg the random location if the radius is not 0, else the global location
	 */
	public Location getActualLocation() {
		return (radius!=0)?getRandomPosition():getPosition();
	}
	
	/**
	 * @param index the index to set the chance in inventory
	 * @param v the value of the chance to set for "index" item
	 */
	public void setChance(int index, int v) {			chances[index] = v;		}


	/**
	 * @param inve
	 */
	public void setInventory(Inventory inve) {
		// if we don't clear the existing inv, removed items will still be in the chest
		inv.clear();
		for(int i = 0 ; i < inve.getSize() ; i++) {
			if(inve.getItem(i) != null) {
				inv.setItem( i, inve.getItem(i));
				if(chances[i] ==0) {
					chances[i] =  Main.configs.default_item_chance;
				}
			}
		}
	}

	/**
	 * @param block
	 * @return true if the block is the good one for the chest
	 */
	public boolean isGoodType(Block block) {
		return type.equals(block.getType());
	}

	/**
	 * gives the main informations about the chest
	 */
	public String toString() {
		return (name +" "+fall +" " +direction+" "+ radius+" "+particle);             
	}
	
	/**
	 * sets the text of the lootchest's holo
	 * @param text the text to set
	 */
	public void setHolo(String text) {
		holo = text;
		hologram.setText(holo);
	}

	public void setLastreset() {
		this.lastreset = (new Timestamp(System.currentTimeMillis())).getTime();
	}
    
}