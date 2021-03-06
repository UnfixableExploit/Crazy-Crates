package me.BadBones69.CrazyCrates.CrateTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.BadBones69.CrazyCrates.Api;
import me.BadBones69.CrazyCrates.CC;
import me.BadBones69.CrazyCrates.GUI;
import me.BadBones69.CrazyCrates.Main;
import me.BadBones69.CrazyCrates.ParticleEffect;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import net.minecraft.server.v1_8_R3.TileEntityEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class QCC implements Listener{ // Quad Crate Control.
	public static Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CrazyCrates");
	@SuppressWarnings("static-access")
	public QCC(Plugin plugin){
		this.plugin = plugin;
	}
	public static HashMap<Player, HashMap<Location, BlockState>> crates = new HashMap<Player, HashMap<Location, BlockState>>();
	public static HashMap<Player, HashMap<Location, BlockState>> All = new HashMap<Player, HashMap<Location, BlockState>>();
	public static HashMap<Player, HashMap<Location, Boolean>> opened = new HashMap<Player, HashMap<Location, Boolean>>();
	public static HashMap<Player, ArrayList<Location>> chests = new HashMap<Player, ArrayList<Location>>();
	public static HashMap<Player, ArrayList<Location>> Rest = new HashMap<Player, ArrayList<Location>>();
	public static HashMap<Player, ArrayList<Entity>> Rewards = new HashMap<Player, ArrayList<Entity>>();
	public static HashMap<Player, Integer> P = new HashMap<Player, Integer>();
	/**
	 * Starts building the Crate Setup.
	 * @param player Player that is opening the chest.
	 * @param loc Location it opens at.
	 * @param Middle Middle Material.
	 * @param Inner Inner line Material.
	 * @param Outer Outer line Material.
	 * @param Frame Frame Material.
	 * @param sound Sound the Frame Makes.
	 * @param Chest Chest Type.
	 */
	public static void startBuild(final Player player, final Location loc, Material Chest){
		final ArrayList<Location> Ch = getChests(loc);
		String schem = Api.pickRandomSchem();
		List<Location> Check = Api.getLocations(schem, player.getLocation());
		ArrayList<Location> rest = new ArrayList<Location>();
		rest.add(loc.clone().add(0, -1, 0));
		HashMap<Location, Boolean> checks = new HashMap<Location, Boolean>();
		for(Location l : Ch){
			checks.put(l, false);
		}
		opened.put(player, checks);
		ArrayList<Material> BlackList = new ArrayList<Material>();
		BlackList.add(Material.SIGN_POST);
		BlackList.add(Material.WALL_SIGN);
		BlackList.add(Material.STONE_BUTTON);
		BlackList.add(Material.WOOD_BUTTON);
		for(Location l : Check){
			if(BlackList.contains(l.getBlock().getType())){
				player.sendMessage(Api.color(Api.getPrefix()+"&cThere is not enough space to open that here."));
				GUI.Crate.remove(player);
				return;
			}
			if(l.getBlockY()!=player.getLocation().getBlockY()-1&&l.getBlock().getType()!=Material.AIR){
				if(!l.equals(player.getLocation())){
					player.sendMessage(Api.color(Api.getPrefix()+"&cThere is not enough space to open that here."));
					GUI.Crate.remove(player);
					return;
				}
			}
		}
		if(player.getLocation().subtract(0, 1, 0).getBlock().getType()==Material.AIR){
			player.sendMessage(Api.color(Api.getPrefix()+"&cYou must be standing on a block."));
			GUI.Crate.remove(player);
			return;
		}
		for(Entity en : player.getNearbyEntities(6, 6, 6)){
			if(en instanceof Player){
				Player p = (Player) en;
				if(crates.containsKey(p)){
					player.sendMessage(Api.color(Api.getPrefix()+"&cYou are to close to a player that is opening their Crate."));
					GUI.Crate.remove(player);
					return;
				}
			}
		}
		for(Entity en : player.getNearbyEntities(2, 2, 2)){
			if(en instanceof Player){
				Player p = (Player) en;
				Vector v = p.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().setY(1);
				p.setVelocity(v);
			}
		}
		if(Main.settings.getFile(GUI.Crate.get(player)).getBoolean("Crate.OpeningBroadCast")){
			String msg = Api.color(Main.settings.getFile(GUI.Crate.get(player)).getString("Crate.BroadCast"));
			msg = msg.replaceAll("%Prefix%", Api.getPrefix());
			msg = msg.replaceAll("%prefix%", Api.getPrefix());
			msg = msg.replaceAll("%Player%", player.getName());
			msg = msg.replaceAll("%player%", player.getName());
			Bukkit.broadcastMessage(msg);
		}
		chests.put(player, Ch);
		if(Api.Key.get(player).equals("PhysicalKey")){
			Api.removeItem(CC.Key.get(player), player);
		}
		if(Api.Key.get(player).equals("VirtualKey")){
			Api.takeKeys(1, player, GUI.Crate.get(player));
		}
		rest.clear();
		HashMap<Location, BlockState> locs = new HashMap<Location, BlockState>();
		HashMap<Location, BlockState> A = new HashMap<Location, BlockState>();
		for(Location l : Api.getLocations(schem, player.getLocation())){
			boolean found = false;
			for(Location L : chests.get(player)){
				if(l.getBlockX()==L.getBlockX()&&l.getBlockY()==L.getBlockY()&&l.getBlockZ()==L.getBlockZ()){
					found=true;
					break;
				}
			}
			if(!found){
				locs.put(l, l.getBlock().getState());
				rest.add(l);
			}
			A.put(l, l.getBlock().getState());
		}
		Api.pasteSchem(schem, player.getLocation());
		All.put(player, A);
		crates.put(player, locs);
		Rest.put(player, rest);
		Location L = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		player.teleport(L.add(.5, 0, .5));
		spawnChest(Ch, player, Chest);
	}
	private static void spawnChest(final ArrayList<Location> locs, final Player player, final Material Chest){
		P.put(player, Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
			double r = 0;
			int i = 0;
			int e = 0;
			int f = 0;
			Location l = new Location(locs.get(0).getWorld(), locs.get(0).getBlockX(), locs.get(0).getBlockY(), locs.get(0).getBlockZ()).add(.5, 3, .5);
			Location loc = locs.get(0).clone();
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				ArrayList<Location> L = getCircle(l, r, 10);
				ArrayList<Location> L2 = getCircleReverse(l, r, 10);
				ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L.get(i), 100);
				ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L2.get(i), 100);
				i++;f++;e++;
				l.add(0, -.05, 0);
				if(i==10)i=0;
				if(e==6){
					e=0;
					r=r+.08;
				}
				if(f==60){
					loc.getWorld().spigot().playEffect(loc.clone().add(0, .3, 0), Effect.TILE_BREAK, Chest.getId(), 0, (float).5, (float).5, (float).5, 0, 10, 100);
					loc.getWorld().playSound(loc.clone(), Sound.STEP_STONE, 1, 1);
					loc.getBlock().setType(Chest);
					loc.getBlock().setData((byte)4);
					Bukkit.getScheduler().cancelTask(P.get(player));
					P.remove(player);
				}
			}
		}, 0, 1));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				P.put(player, Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
					double r = 0;
					int i = 0;
					int e = 0;
					int f = 0;
					Location l = new Location(locs.get(1).getWorld(), locs.get(1).getBlockX(), locs.get(1).getBlockY(), locs.get(1).getBlockZ()).add(.5, 3, .5);
					Location loc = locs.get(1).clone();
					@SuppressWarnings("deprecation")
					@Override
					public void run() {
						ArrayList<Location> L = getCircle(l, r, 10);
						ArrayList<Location> L2 = getCircleReverse(l, r, 10);
						ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L.get(i), 100);
						ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L2.get(i), 100);
						i++;f++;e++;
						l.add(0, -.05, 0);
						if(i==10)i=0;
						if(e==6){
							e=0;
							r=r+.08;
						}
						if(f==60){
							loc.getWorld().spigot().playEffect(loc.clone().add(0, .3, 0), Effect.TILE_BREAK, Chest.getId(), 0, (float).5, (float).5, (float).5, 0, 10, 100);
							loc.getWorld().playSound(loc.clone(), Sound.STEP_STONE, 1, 1);
							loc.getBlock().setType(Chest);
							loc.getBlock().setData((byte)2);
							Bukkit.getScheduler().cancelTask(P.get(player));
							P.remove(player);
						}
					}
				}, 0, 1));
			}
		}, 61);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				P.put(player, Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
					double r = 0;
					int i = 0;
					int e = 0;
					int f = 0;
					Location l = new Location(locs.get(2).getWorld(), locs.get(2).getBlockX(), locs.get(2).getBlockY(), locs.get(2).getBlockZ()).add(.5, 3, .5);
					Location loc = locs.get(2).clone();
					@SuppressWarnings("deprecation")
					@Override
					public void run() {
						ArrayList<Location> L = getCircle(l, r, 10);
						ArrayList<Location> L2 = getCircleReverse(l, r, 10);
						ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L.get(i), 100);
						ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L2.get(i), 100);
						i++;f++;e++;
						l.add(0, -.05, 0);
						if(i==10)i=0;
						if(e==6){
							e=0;
							r=r+.08;
						}
						if(f==60){
							loc.getWorld().spigot().playEffect(loc.clone().add(0, .3, 0), Effect.TILE_BREAK, Chest.getId(), 0, (float).5, (float).5, (float).5, 0, 10, 100);
							loc.getWorld().playSound(loc.clone(), Sound.STEP_STONE, 1, 1);
							loc.getBlock().setType(Chest);
							loc.getBlock().setData((byte)5);
							Bukkit.getScheduler().cancelTask(P.get(player));
							P.remove(player);
						}
					}
				}, 0, 1));
			}
		}, 121);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				P.put(player, Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
					double r = 0;
					int i = 0;
					int e = 0;
					int f = 0;
					Location l = new Location(locs.get(3).getWorld(), locs.get(3).getBlockX(), locs.get(3).getBlockY(), locs.get(3).getBlockZ()).add(.5, 3, .5);
					Location loc = locs.get(3).clone();
					@SuppressWarnings("deprecation")
					@Override
					public void run() {
						ArrayList<Location> L = getCircle(l, r, 10);
						ArrayList<Location> L2 = getCircleReverse(l, r, 10);
						ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L.get(i), 100);
						ParticleEffect.FLAME.display(0, 0, 0, 0, 1, L2.get(i), 100);
						i++;f++;e++;
						l.add(0, -.05, 0);
						if(i==10)i=0;
						if(e==6){
							e=0;
							r=r+.08;
						}
						if(f==60){
							loc.getWorld().spigot().playEffect(loc.clone().add(0, .3, 0), Effect.TILE_BREAK, Chest.getId(), 0, (float).5, (float).5, (float).5, 0, 10, 100);
							loc.getWorld().playSound(loc.clone(), Sound.STEP_STONE, 1, 1);
							loc.getBlock().setType(Chest);
							loc.getBlock().setData((byte)3);
							Bukkit.getScheduler().cancelTask(P.get(player));
							P.remove(player);
						}
					}
				}, 0, 1));
			}
		}, 181);
	}
	/**
	 * Undoes the Crate Build.
	 * @param player The player that opened the Crate.
	 */
	@SuppressWarnings("deprecation")
	public static void undoBuild(Player player){
		HashMap<Location, BlockState> locs = All.get(player);
		for(Location loc : locs.keySet()){
			if(locs.get(loc)!=null){
				loc.getBlock().setTypeIdAndData(locs.get(loc).getTypeId(), locs.get(loc).getRawData(), true);
			}
		}
		crates.remove(player);
		chests.remove(player);
		GUI.Crate.remove(player);
		if(P.containsKey(player)){
			Bukkit.getScheduler().cancelTask(P.get(player));
			P.remove(player);
		}
		if(Rewards.containsKey(player)){
			for(Entity h : Rewards.get(player)){
				h.remove();
			}
			Rewards.remove(player);
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		Location loc = e.getBlock().getLocation();
		for(Player player : crates.keySet()){
			for(Location l : crates.get(player).keySet()){
				if(l.getBlockX()==loc.getBlockX()&&l.getBlockY()==loc.getBlockY()&&l.getBlockZ()==loc.getBlockZ()){
					e.setCancelled(true);
					return;
				}
			}
		}
		if(crates.containsKey(e.getPlayer())){
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void onChestClick(PlayerInteractEvent e){
		if(e.getAction()==Action.RIGHT_CLICK_BLOCK||e.getAction()==Action.LEFT_CLICK_BLOCK){
			if(e.getClickedBlock().getType()==Material.CHEST){
				for(final Player player : chests.keySet()){
					for(final Location l : chests.get(player)){
						Location B = e.getClickedBlock().getLocation();
						if(l.getBlockX()==B.getBlockX()&&l.getBlockY()==B.getBlockY()&&l.getBlockZ()==B.getBlockZ()){
							e.setCancelled(true);
							if(player==e.getPlayer()){
								playChestAction(e.getClickedBlock(), true);
								if(!opened.get(player).get(l)){
									ArrayList<Entity> rewards = new ArrayList<Entity>();
									ItemStack it = Api.displayItem(player, B.clone().add(.5, 1.3, .5));
									String name = Api.color(Main.settings.getFile(GUI.Crate.get(player)).getString(Api.path.get(player)+".DisplayName"));
									final Entity reward = player.getWorld().dropItem(B.clone().add(.5, 1, .5), it);
									reward.setVelocity(new Vector(0,.2,0));
									reward.setCustomName(name);
									reward.setCustomNameVisible(true);
									if(Rewards.containsKey(player)){
										for(Entity h : Rewards.get(player)){
											rewards.add(h);
										}
									}
									rewards.add(reward);
									Rewards.put(player, rewards);
								}
								boolean trigger = true;
								for(Location loc : opened.get(player).keySet()){
									if(loc.getBlockX()==l.getBlockX()&&loc.getBlockY()==l.getBlockY()&&loc.getBlockZ()==l.getBlockZ()){
										opened.get(player).put(loc, true);
									}
									if(opened.get(player).get(loc)==false){
										trigger = false;
									}
								}
								if(trigger){
									Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
										@SuppressWarnings("deprecation")
										@Override
										public void run() {
											for(Location loc : Rest.get(player)){ // Rest is all except Chests.
												HashMap<Location, BlockState> locs = crates.get(player); // Crates holds the Data.
												if(locs!=null){
													for(Location loc2 : locs.keySet()){ // Looping though the locations.
														if(locs.get(loc)!=null){ // Checking to make sure a location isn't null.
															if(loc.equals(loc2)){
																//  BlockState block = loc2.getBlock().getState();
																//	loc2.getWorld().spigot().playEffect(loc2.clone().add(0, -1, 0).add(0, .3, 0), Effect.TILE_BREAK, block.getTypeId(), block.getData().getData(), (float).5, (float).5, (float).5, 0, 10, 100);
																loc2.getBlock().setTypeIdAndData(locs.get(loc2).getTypeId(), locs.get(loc2).getRawData(), true);
															}
														}
													}
												}
											}
											player.getLocation().getWorld().playSound(player.getLocation().clone(), Sound.STEP_STONE, 1, 1);
										}
									}, 3*20);
									Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
										@Override
										public void run() {
											for(Location loc : Rest.get(player)){
												HashMap<Location, BlockState> locs = crates.get(player);
												for(Location loc2 : chests.get(player)){
													if(locs.get(loc)!=null){
														loc2.getBlock().setType(Material.AIR);
													}
												}
											}
											for(Entity h : Rewards.get(player)){
												h.remove();
											}
											Rewards.remove(player);
											player.getWorld().playSound(player.getLocation().clone(), Sound.STEP_STONE, 1, 1);
											crates.remove(player);
											chests.remove(player);
											GUI.Crate.remove(player);
											if(Api.Key.get(player).equals("PhysicalKey")){
												player.teleport(CC.LastLoc.get(player));
											}
										}
									}, 6*20);
								}
							}
							return;
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent e){
		Entity item = e.getItem();
		for(Player p : Rewards.keySet()){
			if(Rewards.get(p).contains(item)){
				e.setCancelled(true);
				return;
			}
		}
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		Player player = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();
		if(crates.containsKey(player)){
			if(from.getBlockX()!=to.getBlockX()||from.getBlockZ()!=to.getBlockZ()){
				e.setCancelled(true);
				player.teleport(from);
				return;
			}
		}
		for(Entity en : player.getNearbyEntities(2, 2, 2)){
			if(en instanceof Player){
				Player p = (Player) en;
				if(crates.containsKey(p)){
					Vector v = player.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().setY(1);
					player.setVelocity(v);
				}
			}
		}
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if(crates.containsKey(e.getPlayer())){
			e.setCancelled(true);
		}
		for(Player p : Rest.keySet()){
			for(Location l : Rest.get(p)){
				Location P = e.getBlockPlaced().getLocation();
				if(P.getBlockX()==l.getBlockX()&&P.getBlockY()==l.getBlockY()&&P.getBlockZ()==l.getBlockZ()){
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		Player player = e.getPlayer();
		String uuid = player.getUniqueId().toString();
		if(!Main.settings.getData().contains("Players."+uuid)){
			Main.settings.getData().set("Players."+uuid+".Name", player.getName());
			for(String crate : Main.settings.getAllCratesNames()){
				int amount = Main.settings.getFile(crate).getInt("Crate.StartingKeys");
				Main.settings.getData().set("Players."+uuid+"."+crate, amount);
			}
			Main.settings.saveData();
		}
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent e){
		Player player = e.getPlayer();
		if(crates.containsKey(player)){
			undoBuild(player);
		}
	}
	private static ArrayList<Location> getChests(Location loc){
		ArrayList<Location> U = new ArrayList<Location>();
		U.add(loc.clone().add(2, 0, 0));
		U.add(loc.clone().add(0, 0, 2));
		U.add(loc.clone().add(-2, 0, 0));
		U.add(loc.clone().add(0, 0, -2));
		return U;
	}
	private void playChestAction(Block b, boolean open) {
        Location location = b.getLocation();
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        if (b.getType() == Material.ENDER_CHEST) {
            TileEntityEnderChest tileChest = (TileEntityEnderChest) world.getTileEntity(position);
            world.playBlockAction(position, tileChest.w(), 1, open ? 1 : 0);
        } else {
            TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
            world.playBlockAction(position, tileChest.w(), 1, open ? 1 : 0);
        }
    }
	private static ArrayList<Location> getCircle(Location center, double radius, int amount){
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for(int i = 0;i < amount; i++){
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
	public static ArrayList<Location> getCircleReverse(Location center, double radius, int amount){
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for(int i = 0;i < amount; i++){
            double angle = i * increment;
            double x = center.getX() - (radius * Math.cos(angle));
            double z = center.getZ() - (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}