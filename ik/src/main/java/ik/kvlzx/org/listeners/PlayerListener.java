package ik.kvlzx.org.listeners;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ik.kvlzx.items.*;
import ik.kvlzx.org.IntKnock;
import ik.kvlzx.org.arenas.ArenaBuilder;
import ik.kvlzx.org.utils.MessageUtils;

public class PlayerListener implements Listener {

    private static final int COOLDOWN_SECONDS = 10;
    private static final String COOLDOWN_BOW = "BOW";
    private static final String COOLDOWN_FEATHER = "FEATHER";
    private static final String COOLDOWN_PLATE = "PLATE";

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, ItemStack> savedArrows = new HashMap<>();
    private final Map<Location, BukkitRunnable> plateTimers = new HashMap<>();
    private final Map<UUID, List<BukkitRunnable>> cooldownTasks = new HashMap<>();
    private final IntKnock plugin;
    private final ArenaBuilder arenaBuilder;

    public PlayerListener(IntKnock plugin) {
        this.plugin = plugin;
        this.arenaBuilder = plugin.getArenaBuilder();
    }

    // Método para verificar si un jugador está dentro de la arena
    private boolean isInArena(Player player) {
        List<Location> arenaBlocks = arenaBuilder.getArenaBlocks();
        if (arenaBlocks.isEmpty()) return false; // Si no hay bloques de arena, no está dentro

        Location playerLoc = player.getLocation();
        int playerX = playerLoc.getBlockX();
        int playerY = playerLoc.getBlockY();
        int playerZ = playerLoc.getBlockZ();

        // Determinar los límites de la arena (considerando el anillo externo y mini islas)
        int minX = arenaBlocks.stream().mapToInt(loc -> loc.getBlockX()).min().orElse(0);
        int maxX = arenaBlocks.stream().mapToInt(loc -> loc.getBlockX()).max().orElse(0);
        int minY = arenaBlocks.stream().mapToInt(loc -> loc.getBlockY()).min().orElse(0);
        int maxY = arenaBlocks.stream().mapToInt(loc -> loc.getBlockY()).max().orElse(0);
        int minZ = arenaBlocks.stream().mapToInt(loc -> loc.getBlockZ()).min().orElse(0);
        int maxZ = arenaBlocks.stream().mapToInt(loc -> loc.getBlockZ()).max().orElse(0);

        // Verificar si el jugador está dentro de los límites
        return playerX >= minX && playerX <= maxX &&
               playerY >= minY && playerY <= maxY &&
               playerZ >= minZ && playerZ <= maxZ;
    }

    // Método para verificar si un jugador está en cooldown para un tipo de ítem
    private boolean isOnCooldown(Player player, String type) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        return playerCooldowns != null && playerCooldowns.getOrDefault(type, 0L) > System.currentTimeMillis();
    }

    // Método para establecer un cooldown para un tipo de ítem
    private void setCooldown(Player player, String type, int seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                 .put(type, System.currentTimeMillis() + seconds * 1000);
    }

    // Método para encontrar un ítem por tipo en el inventario
    private ItemStack findItemByType(Player player, Material type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == type) {
                return item;
            }
        }
        return null;
    }

    // Método para encontrar el slot de un ítem por tipo
    private int findSlotByType(Player player, Material type) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() == type) {
                return i;
            }
        }
        return -1;
    }

    // Método para iniciar el cooldown visual
    private void startCooldownVisual(Player player, ItemStack original, int slot, int seconds) {
        if (original == null) return;
        ItemStack cooldownItem = original.clone();
        cooldownItem.setAmount(seconds);
        player.getInventory().setItem(slot, cooldownItem);
        player.updateInventory();

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = seconds;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (timeLeft <= 0) {
                    ItemStack restoredItem = original.clone();
                    restoredItem.setAmount(1);
                    player.getInventory().setItem(slot, restoredItem);
                    player.updateInventory();
                    cancel();
                    return;
                }
                cooldownItem.setAmount(timeLeft);
                player.getInventory().setItem(slot, cooldownItem);
                player.updateInventory();
                timeLeft--;
            }
        };

        task.runTaskTimer(plugin, 0, 20);
        cooldownTasks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(task);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(MessageUtils.getColoredMessage("&9&l¡Bienvenido! &7Domina la arena y deja tu marca."));

        // Teletransportar al centro de la arena
        World world = plugin.getServer().getWorlds().get(0);
        if (world != null) {
            Location arenaCenter = new Location(world, 0, 65, 0); // Ajusta la altura para que sea justo encima de la arena
            player.teleport(arenaCenter);
        } else {
            player.sendMessage(MessageUtils.getColoredMessage("&cNo se pudo teletransportar. Mundo no disponible."));
        }

        // Knockers
        Knockers knockers = new Knockers("&5 Knocker", Arrays.asList(MessageUtils.getColoredMessage("&8 No es la fuerza, es la técnica.")), Material.STICK);

        // Blocks
        Blocks blocks = new Blocks("&3 Arenisca", Arrays.asList(MessageUtils.getColoredMessage("&8 Un clásico del desierto")), Material.SANDSTONE);

        // Bow
        Bow bow = new Bow("&4 Mazakarko", Arrays.asList(MessageUtils.getColoredMessage("&8 Un disparo, un impacto, un salto al vacío.")), Material.BOW);

        // Arrow
        Arrow arrow = new Arrow("&5 Flecha", Material.ARROW);

        // Plate
        Plate plate = new Plate("&6 Placa", Arrays.asList(MessageUtils.getColoredMessage("&8 ¿Listo para volar? Pisa y verás.")), Material.GOLD_PLATE);

        // Feather
        Feather feather = new Feather("&e Pluma", Arrays.asList(MessageUtils.getColoredMessage("&8 No es magia, es pura aerodinámica.")), Material.FEATHER);

        // Pearl
        Pearl pearl = new Pearl("&5 Perla", Arrays.asList(MessageUtils.getColoredMessage("&8 Cada lanzamiento reescribe tu destino.")), Material.ENDER_PEARL);

        player.getInventory().clear();
        player.getInventory().setItem(0, knockers.getItem());
        player.getInventory().setItem(1, blocks.getItem());
        player.getInventory().setItem(2, bow.getItem());
        player.getInventory().setItem(9, arrow.getItem());
        player.getInventory().setItem(6, plate.getItem());
        player.getInventory().setItem(7, feather.getItem());
        player.getInventory().setItem(8, pearl.getItem());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Solo cancelamos el daño por caída
        if (cause == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Permitir muerte solo por vacío o por comandos
        if (cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.CUSTOM) {
            player.setHealth(0.0); // Muerte instantánea
            return;
        }

        // Mostrar el daño pero sin reducir corazones
        event.setDamage(0.0D); // Aplica animación sin quitar vida
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true); // Evita la pérdida de hambre
            ((Player) event.getEntity()).setFoodLevel(20); // Mantiene la comida llena
            ((Player) event.getEntity()).setSaturation(20.0f); // Mantiene la saturación al máximo
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        // Verificar si está dentro de la arena
        if (!isInArena(player)) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.getColoredMessage("&cDebes estar dentro de la arena para usar el arco."));
            return;
        }

        if (isOnCooldown(player, COOLDOWN_BOW)) {
            event.setCancelled(true);
            return;
        }

        ItemStack arrow = findItemByType(player, Material.ARROW);
        int arrowSlot = findSlotByType(player, Material.ARROW);

        if (arrow != null) {
            savedArrows.put(uuid, arrow.clone());
            player.getInventory().setItem(arrowSlot, null);
            player.updateInventory();
        }

        ItemStack bow = findItemByType(player, Material.BOW);
        int bowSlot = findSlotByType(player, Material.BOW);

        if (bow != null) {
            bow.setAmount(1);
            player.getInventory().setItem(bowSlot, bow);
            player.updateInventory();
            setCooldown(player, COOLDOWN_BOW, COOLDOWN_SECONDS);
            startCooldownVisual(player, bow, bowSlot, COOLDOWN_SECONDS);
            restoreArrowAndBowLater(player, arrowSlot, bowSlot);
        }
    }

    private void restoreArrowAndBowLater(Player player, int arrowSlot, int bowSlot) {
        UUID uuid = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                if (savedArrows.containsKey(uuid)) {
                    ItemStack arrow = savedArrows.remove(uuid);
                    if (player.getInventory().getItem(arrowSlot) == null) {
                        player.getInventory().setItem(arrowSlot, arrow);
                    }
                    player.updateInventory();
                }

                ItemStack restoredBow = new Bow(
                    "&4 Mazakarko",
                    Arrays.asList(MessageUtils.getColoredMessage("&5 Un disparo, un impacto, un salto al vacío.")),
                    Material.BOW
                ).getItem();
                restoredBow.setDurability((short) 0);
                restoredBow.setAmount(1);
                player.getInventory().setItem(bowSlot, restoredBow);
                player.updateInventory();
            }
        }.runTaskLater(plugin, COOLDOWN_SECONDS * 20L);
    }

    @EventHandler
    public void onFeatherUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack feather = findItemByType(player, Material.FEATHER);
        int featherSlot = findSlotByType(player, Material.FEATHER);

        if (event.getItem() != null && event.getItem().getType() == Material.FEATHER &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            // Verificar si está dentro de la arena
            if (!isInArena(player)) {
                player.sendMessage(MessageUtils.getColoredMessage("&cDebes estar dentro de la arena para usar la pluma."));
                return;
            }

            if (isOnCooldown(player, COOLDOWN_FEATHER)) return;

            if (feather != null) {
                player.setWalkSpeed(0.4f);
                feather.setAmount(1);
                player.getInventory().setItem(featherSlot, feather);
                player.updateInventory();
                setCooldown(player, COOLDOWN_FEATHER, COOLDOWN_SECONDS);
                startCooldownVisual(player, feather, featherSlot, COOLDOWN_SECONDS);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.setWalkSpeed(0.2f);
                    }
                }, COOLDOWN_SECONDS * 20L);
            }
        }
    }

    @EventHandler
    public void onStepPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block == null) return;

        if (block.getType() == Material.GOLD_PLATE && plateTimers.containsKey(block.getLocation())) {
            // Verificar si está dentro de la arena
            if (!isInArena(player)) {
                player.sendMessage(MessageUtils.getColoredMessage("&cDebes estar dentro de la arena para usar la placa."));
                return;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(0.6);
                Vector velocity = new Vector(direction.getX(), 1.2, direction.getZ());
                player.setVelocity(velocity);
            }, 1L);

            player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE2, 1.0f, 1.0f);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(Material.AIR);
            }, 1L);

            plateTimers.get(block.getLocation()).cancel();
            plateTimers.remove(block.getLocation());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        // Verificar si está dentro de la arena
        if (!isInArena(player)) {
            player.sendMessage(MessageUtils.getColoredMessage("&cDebes estar dentro de la arena para colocar bloques."));
            event.setCancelled(true);
            return;
        }

        if (isinfiniteBlock(item.getType()) || item.getType() == Material.GOLD_PLATE) {
            int itemSlot = findSlotByType(player, item.getType());
            ItemStack stack = player.getInventory().getItem(itemSlot);

            if (stack != null) {
                if (item.getType() == Material.GOLD_PLATE) {
                    if (isOnCooldown(player, COOLDOWN_PLATE)) {
                        player.sendMessage(MessageUtils.getColoredMessage(
                            "&cDebes esperar " + ((cooldowns.get(player.getUniqueId()).get(COOLDOWN_PLATE) - System.currentTimeMillis()) / 1000.0) + " segundos para colocar otra placa."));
                        event.setCancelled(true);
                        return;
                    }
                    stack.setAmount(1);
                    setCooldown(player, COOLDOWN_PLATE, COOLDOWN_SECONDS);
                    startCooldownVisual(player, stack, itemSlot, COOLDOWN_SECONDS);
                    startPlateTimer(event.getBlock().getLocation());
                } else {
                    if (stack.getAmount() <= 63) {
                        stack.setAmount(64);
                    }
                }
                player.getInventory().setItem(itemSlot, stack);
                player.updateInventory();
            }

            if (isinfiniteBlock(item.getType())) {
                Block block = event.getBlock();
                Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), 20L * 10);
            }
        }
    }

    private boolean isinfiniteBlock(Material material) {
        return Arrays.asList(
            Material.SANDSTONE, 
            Material.SNOW_BLOCK, 
            Material.STONE, 
            Material.BEDROCK,
            Material.IRON_BLOCK,
            Material.GOLD_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.EMERALD_BLOCK,
            Material.OBSIDIAN,
            Material.ENDER_STONE,
            Material.PRISMARINE,
            Material.SPONGE).contains(material);
    }

    private void startPlateTimer(Location location) {
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (location.getBlock().getType() == Material.GOLD_PLATE) {
                    location.getBlock().setType(Material.AIR);
                }
                plateTimers.remove(location);
            }
        };
        timer.runTaskLater(plugin, 20L * 10);
        plateTimers.put(location, timer);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer == victim) {
            return;
        }

        // -------------------------PROVISIONAL!!!-------------------------
        killer.playSound(killer.getLocation(), Sound.SWIM, 1.0f, 1.0f);

        int pearlSlot = 8;
        ItemStack currentItem = killer.getInventory().getItem(pearlSlot);

        Pearl pearl = new Pearl(
            "&5 Perla",
            Arrays.asList(MessageUtils.getColoredMessage("&5 Cada lanzamiento reescribe tu destino.")),
            Material.ENDER_PEARL
        );
        ItemStack pearlItem = pearl.getItem();
        pearlItem.setAmount(1);

        if (currentItem == null || currentItem.getType() == Material.AIR) {
            killer.getInventory().setItem(pearlSlot, pearlItem);
        } else if (currentItem.getType() == Material.ENDER_PEARL) {
            int currentAmount = currentItem.getAmount();
            if (currentAmount < 128) {
                currentItem.setAmount(currentAmount + 1);
                killer.getInventory().setItem(pearlSlot, currentItem);
            }
        }

        killer.updateInventory();
    }
}