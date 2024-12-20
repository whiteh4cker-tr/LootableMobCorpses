package tr.alperendemir.lootableMobCorpses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class LootableMobCorpses extends JavaPlugin implements Listener {

    private Map<UUID, CorpseData> corpseDataMap = new HashMap<>();
    private FileConfiguration config;
    private Map<UUID, Inventory> openInventories = new HashMap<>();
    private String corpseName;
    private boolean disableVanillaLoot;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        corpseName = ChatColor.translateAlternateColorCodes('&', config.getString("corpse-name", "&cLootable Corpse"));
        disableVanillaLoot = config.getBoolean("disable-vanilla-loot", true);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Remove all corpses on disable
        for (CorpseData corpseData : corpseDataMap.values()) {
            corpseData.getArmorStand().remove();
            corpseData.getLegsArmorStand().remove();
        }
        corpseDataMap.clear();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        // Disable default drops if configured
        if (disableVanillaLoot) {
            event.getDrops().clear();
        }

        if (entity.getKiller() != null) {
            EntityType type = entity.getType();
            if (config.contains(type.name())) {
                Location location = entity.getLocation();

                // Calculate the direction for the legs (perpendicular to the facing direction)
                Vector direction = location.getDirection().setY(0).normalize();
                Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX());

                // Create the main armor stand for head and chestplate, 1 block below
                Location armorStandLocation = location.clone().subtract(0, 1, 0);
                ArmorStand armorStand = (ArmorStand) armorStandLocation.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
                armorStand.setGravity(false);
                armorStand.setInvulnerable(true);
                armorStand.setVisible(false);
                armorStand.setCustomName(corpseName);
                armorStand.setCustomNameVisible(true);

                // Rotate main armor stand to lie flat
                armorStand.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));

                // Create the legs armor stand, positioned to the side and rotated to lie flat
                Location legsLocation = location.clone().add(perpendicular.multiply(0.8)); // Moved further to the side
                ArmorStand legsArmorStand = (ArmorStand) legsLocation.getWorld().spawnEntity(legsLocation, EntityType.ARMOR_STAND);
                legsArmorStand.setGravity(false);
                legsArmorStand.setInvulnerable(true);
                legsArmorStand.setVisible(false);
                legsArmorStand.setSmall(false);

                // Prevent interaction with the legs armor stand
                legsArmorStand.setMarker(true);

                // Rotate legs armor stand to lie flat
                legsArmorStand.setHeadPose(new EulerAngle(Math.toRadians(0), 0, 0));

                // Set body parts to resemble the mob
                equipCorpse(armorStand, legsArmorStand, type);

                // Create inventory for the corpse and generate loot
                Inventory corpseInventory = Bukkit.createInventory(null, 9, corpseName);
                generateLoot(corpseInventory, type);

                // Store corpse data along with the inventory
                CorpseData corpseData = new CorpseData(armorStand, legsArmorStand, config.getInt(type.name() + ".corpse-duration", 900), corpseInventory);
                corpseDataMap.put(armorStand.getUniqueId(), corpseData);

                // Schedule the removal of the corpse based on duration
                BukkitRunnable removalTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (corpseDataMap.containsKey(armorStand.getUniqueId())) {
                            // Remove the corpse only if the inventory is empty
                            if (isCorpseInventoryEmpty(corpseData)) {
                                corpseDataMap.remove(armorStand.getUniqueId());
                                armorStand.remove();
                                legsArmorStand.remove();
                            } else {
                                // If not empty, reschedule
                                scheduleCorpseRemoval(corpseData);
                            }
                        }
                    }
                };
                corpseData.setRemovalTask(removalTask.runTaskLater(this, corpseData.getDuration() * 20L));
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (corpseDataMap.containsKey(entity.getUniqueId())) {
            CorpseData corpseData = corpseDataMap.get(entity.getUniqueId());
            if (corpseData != null) {
                event.setCancelled(true);
                Inventory inv = openCorpseInventory(player, corpseData);
                openInventories.put(player.getUniqueId(), inv);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (openInventories.containsKey(playerUUID)) {
            Inventory closedInventory = openInventories.get(playerUUID);

            for (Map.Entry<UUID, CorpseData> entry : corpseDataMap.entrySet()) {
                CorpseData corpseData = entry.getValue();
                ArmorStand armorStand = corpseData.getArmorStand();

                if (armorStand != null && event.getInventory().equals(closedInventory)) {
                    // Remove the inventory from the openInventories map
                    openInventories.remove(playerUUID);
                    // Check if the inventory is empty or time has passed
                    if (isCorpseInventoryEmpty(corpseData)) {
                        corpseDataMap.remove(armorStand.getUniqueId());
                        armorStand.remove();
                        corpseData.getLegsArmorStand().remove();
                    } else {
                        // Reschedule corpse removal task if not empty
                        scheduleCorpseRemoval(corpseData);
                    }

                    break;
                }
            }
        }
    }

    private void scheduleCorpseRemoval(CorpseData corpseData) {
        BukkitTask existingTask = corpseData.getRemovalTask();
        if (existingTask != null && (!existingTask.isCancelled())) {
            existingTask.cancel();
        }

        // Schedule a new task
        BukkitRunnable newRemovalTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (corpseDataMap.containsKey(corpseData.getArmorStand().getUniqueId())) {
                    if (isCorpseInventoryEmpty(corpseData)) {
                        corpseDataMap.remove(corpseData.getArmorStand().getUniqueId());
                        corpseData.getArmorStand().remove();
                        corpseData.getLegsArmorStand().remove();
                    } else {
                        // Reschedule if not empty
                        scheduleCorpseRemoval(corpseData);
                    }
                }
            }
        };
        corpseData.setRemovalTask(newRemovalTask.runTaskLater(this, corpseData.getDuration() * 20L));
    }

    private boolean isCorpseInventoryEmpty(CorpseData corpseData) {
        Inventory inv = corpseData.getCorpseInventory();
        if (inv != null) {
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    return false;
                }
            }
        }
        return true;
    }

    private Inventory openCorpseInventory(Player player, CorpseData corpseData) {
        Inventory inventory = corpseData.getCorpseInventory();
        player.openInventory(inventory);
        return inventory;
    }

    private void generateLoot(Inventory inventory, EntityType type) {
        if (config.contains(type.name() + ".loot")) {
            List<String> lootList = config.getStringList(type.name() + ".loot");
            for (String lootEntry : lootList) {
                String[] parts = lootEntry.split(":");
                if (parts.length == 3) {
                    Material material = Material.getMaterial(parts[0].toUpperCase());
                    int amount = Integer.parseInt(parts[1]);
                    double chance = Double.parseDouble(parts[2]);

                    if (material != null && Math.random() < chance) {
                        inventory.addItem(new ItemStack(material, amount));
                    }
                }
            }
        }
    }

    private void equipCorpse(ArmorStand armorStand, ArmorStand legsArmorStand, EntityType type) {
        armorStand.setSmall(false);
        legsArmorStand.setSmall(false);

        switch (type) {
            case ZOMBIE:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.ZOMBIE_HEAD));
                armorStand.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                legsArmorStand.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                legsArmorStand.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                break;
            case SKELETON:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SKELETON_SKULL));
                armorStand.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                legsArmorStand.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                legsArmorStand.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                break;
            case CREEPER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.CREEPER_HEAD));
                break;
            case SPIDER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SPIDER_EYE));
                break;
            case ENDERMAN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.ENDER_PEARL));
                break;
            case PIG:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.PORKCHOP));
                break;
            case COW:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.LEATHER));
                break;
            case CHICKEN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.CHICKEN));
                break;
            case SHEEP:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.WHITE_WOOL));
                break;
            case HORSE:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SADDLE));
                break;
            case WOLF:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.BONE));
                break;
            case OCELOT:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.TROPICAL_FISH));
                break;
            case CAT:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.STRING));
                break;
            case VILLAGER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.EMERALD));
                break;
            case IRON_GOLEM:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.IRON_BLOCK));
                break;
            case WITHER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.NETHER_STAR));
                break;
            case ENDER_DRAGON:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.DRAGON_HEAD));
                break;
            case BAT:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.COAL));
                break;
            case GHAST:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.GHAST_TEAR));
                break;
            case MAGMA_CUBE:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.MAGMA_CREAM));
                break;
            case SLIME:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SLIME_BALL));
                break;
            case BLAZE:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.BLAZE_ROD));
                break;
            case ZOMBIFIED_PIGLIN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.GOLD_NUGGET));
                break;
            case HOGLIN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.CRIMSON_FUNGUS));
                break;
            case ZOGLIN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.WARPED_FUNGUS));
                break;
            case PIGLIN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.GOLD_INGOT));
                break;
            case STRIDER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.STRING));
                break;
            case VEX:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.IRON_SWORD));
                break;
            case GUARDIAN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.PRISMARINE_SHARD));
                break;
            case ELDER_GUARDIAN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.PRISMARINE_CRYSTALS));
                break;
            case SHULKER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SHULKER_SHELL));
                break;
            case SILVERFISH:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.STONE));
                break;
            case ENDERMITE:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.END_STONE));
                break;
            case WITCH:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.POTION));
                break;
            case PILLAGER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.CROSSBOW));
                break;
            case RAVAGER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
                break;
            case VINDICATOR:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.IRON_AXE));
                break;
            case EVOKER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.TOTEM_OF_UNDYING));
                break;
            case PHANTOM:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.PHANTOM_MEMBRANE));
                break;
            case BEE:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.HONEYCOMB));
                break;
            case CAVE_SPIDER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.COBWEB));
                break;
            case DOLPHIN:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.COD));
                break;
            case DROWNED:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.TRIDENT));
                break;
            case FOX:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SWEET_BERRIES));
                break;
            case LLAMA:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.LEAD));
                break;
            case PANDA:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.BAMBOO));
                break;
            case PARROT:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.FEATHER));
                break;
            case POLAR_BEAR:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.SNOWBALL));
                break;
            case RABBIT:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.RABBIT_FOOT));
                break;
            case SQUID:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.INK_SAC));
                break;
            case WANDERING_TRADER:
                armorStand.getEquipment().setHelmet(new ItemStack(Material.MAP));
                break;
            default:
                return;
        }
    }

    private static class CorpseData {
        private final ArmorStand armorStand;
        private final ArmorStand legsArmorStand;
        private final int duration;
        private final EntityType originalEntityType;
        private final Inventory corpseInventory;
        private BukkitTask removalTask;

        public CorpseData(ArmorStand armorStand, ArmorStand legsArmorStand, int duration, Inventory corpseInventory) {
            this.armorStand = armorStand;
            this.legsArmorStand = legsArmorStand;
            this.duration = duration;
            this.originalEntityType = getOriginalEntityTypeFromCorpse(armorStand);
            this.corpseInventory = corpseInventory;
        }

        public ArmorStand getArmorStand() {
            return armorStand;
        }

        public ArmorStand getLegsArmorStand() {
            return legsArmorStand;
        }

        public int getDuration() {
            return duration;
        }

        public EntityType getOriginalEntityType() {
            return originalEntityType;
        }

        public Inventory getCorpseInventory() {
            return corpseInventory;
        }

        public BukkitTask getRemovalTask() {
            return removalTask;
        }

        public void setRemovalTask(BukkitTask removalTask) {
            this.removalTask = removalTask;
        }

        private EntityType getOriginalEntityTypeFromCorpse(ArmorStand corpse) {
            if (corpse.getEquipment().getHelmet() != null) {
                Material helmetType = corpse.getEquipment().getHelmet().getType();
                // Check for each supported entity type
                if (helmetType == Material.ZOMBIE_HEAD) {
                    return EntityType.ZOMBIE;
                } else if (helmetType == Material.SKELETON_SKULL) {
                    return EntityType.SKELETON;
                } else if (helmetType == Material.CREEPER_HEAD) {
                    return EntityType.CREEPER;
                } else if (helmetType == Material.SPIDER_EYE) {
                    return EntityType.SPIDER;
                } else if (helmetType == Material.ENDER_PEARL) {
                    return EntityType.ENDERMAN;
                } else if (helmetType == Material.PORKCHOP) {
                    return EntityType.PIG;
                } else if (helmetType == Material.LEATHER) {
                    return EntityType.COW;
                } else if (helmetType == Material.CHICKEN) {
                    return EntityType.CHICKEN;
                } else if (helmetType == Material.WHITE_WOOL) {
                    return EntityType.SHEEP;
                } else if (helmetType == Material.SADDLE) {
                    return EntityType.HORSE;
                } else if (helmetType == Material.BONE) {
                    return EntityType.WOLF;
                } else if (helmetType == Material.TROPICAL_FISH) {
                    return EntityType.OCELOT;
                } else if (helmetType == Material.STRING) {
                    return EntityType.CAT;
                } else if (helmetType == Material.EMERALD) {
                    return EntityType.VILLAGER;
                } else if (helmetType == Material.IRON_BLOCK) {
                    return EntityType.IRON_GOLEM;
                } else if (helmetType == Material.NETHER_STAR) {
                    return EntityType.WITHER;
                } else if (helmetType == Material.DRAGON_HEAD) {
                    return EntityType.ENDER_DRAGON;
                } else if (helmetType == Material.COAL) {
                    return EntityType.BAT;
                } else if (helmetType == Material.GHAST_TEAR) {
                    return EntityType.GHAST;
                } else if (helmetType == Material.MAGMA_CREAM) {
                    return EntityType.MAGMA_CUBE;
                } else if (helmetType == Material.SLIME_BALL) {
                    return EntityType.SLIME;
                } else if (helmetType == Material.BLAZE_ROD) {
                    return EntityType.BLAZE;
                } else if (helmetType == Material.GOLD_NUGGET) {
                    return EntityType.ZOMBIFIED_PIGLIN;
                } else if (helmetType == Material.CRIMSON_FUNGUS) {
                    return EntityType.HOGLIN;
                } else if (helmetType == Material.WARPED_FUNGUS) {
                    return EntityType.ZOGLIN;
                } else if (helmetType == Material.GOLD_INGOT) {
                    return EntityType.PIGLIN;
                } else if (helmetType == Material.IRON_SWORD) {
                    return EntityType.VEX;
                } else if (helmetType == Material.PRISMARINE_SHARD) {
                    return EntityType.GUARDIAN;
                } else if (helmetType == Material.PRISMARINE_CRYSTALS) {
                    return EntityType.ELDER_GUARDIAN;
                } else if (helmetType == Material.SHULKER_SHELL) {
                    return EntityType.SHULKER;
                } else if (helmetType == Material.STONE) {
                    return EntityType.SILVERFISH;
                } else if (helmetType == Material.END_STONE) {
                    return EntityType.ENDERMITE;
                } else if (helmetType == Material.POTION) {
                    return EntityType.WITCH;
                } else if (helmetType == Material.CROSSBOW) {
                    return EntityType.PILLAGER;
                } else if (helmetType == Material.DIAMOND_BLOCK) {
                    return EntityType.RAVAGER;
                } else if (helmetType == Material.IRON_AXE) {
                    return EntityType.VINDICATOR;
                } else if (helmetType == Material.TOTEM_OF_UNDYING) {
                    return EntityType.EVOKER;
                } else if (helmetType == Material.PHANTOM_MEMBRANE) {
                    return EntityType.PHANTOM;
                } else if (helmetType == Material.HONEYCOMB) {
                    return EntityType.BEE;
                } else if (helmetType == Material.COBWEB) {
                    return EntityType.CAVE_SPIDER;
                } else if (helmetType == Material.COD) {
                    return EntityType.DOLPHIN;
                } else if (helmetType == Material.TRIDENT) {
                    return EntityType.DROWNED;
                } else if (helmetType == Material.SWEET_BERRIES) {
                    return EntityType.FOX;
                } else if (helmetType == Material.LEAD) {
                    return EntityType.LLAMA;
                } else if (helmetType == Material.BAMBOO) {
                    return EntityType.PANDA;
                } else if (helmetType == Material.FEATHER) {
                    return EntityType.PARROT;
                } else if (helmetType == Material.SNOWBALL) {
                    return EntityType.POLAR_BEAR;
                } else if (helmetType == Material.RABBIT_FOOT) {
                    return EntityType.RABBIT;
                } else if (helmetType == Material.INK_SAC) {
                    return EntityType.SQUID;
                } else if (helmetType == Material.MAP) {
                    return EntityType.WANDERING_TRADER;
                } else if (helmetType == Material.STRING) {
                    return EntityType.STRIDER;
                }
            }
            return EntityType.UNKNOWN;
        }
    }
}