package tw.moses.sellchest;

import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SellChestListener implements Listener {
    private final SellChest plugin;
    private final SellProcessor processor;
    private final Map<String, Long> automaticSaleCooldowns = new HashMap<>();

    public SellChestListener(SellChest plugin, SellProcessor processor) {
        this.plugin = plugin;
        this.processor = processor;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder(false);
        if (!(holder instanceof Chest) && !(holder instanceof DoubleChest)) {
            return;
        }
        processor.process((Player) event.getPlayer(), inventory);
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (event.getNewCurrent() <= event.getOldCurrent()) {
            return;
        }

        Block changedBlock = event.getBlock();
        schedulePoweredChest(changedBlock);
        for (BlockFace face : BlockFace.values()) {
            if (face.getModX() == 0 && face.getModY() == 0 && face.getModZ() == 0) {
                continue;
            }
            schedulePoweredChest(changedBlock.getRelative(face));
        }
    }

    private void schedulePoweredChest(Block block) {
        if (!(block.getState() instanceof Chest)) {
            return;
        }

        org.bukkit.Location location = block.getLocation();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Block currentBlock = location.getBlock();
            if (!(currentBlock.getState() instanceof Chest chest)
                    || (!currentBlock.isBlockPowered() && !currentBlock.isBlockIndirectlyPowered())) {
                return;
            }

            Inventory inventory = chest.getInventory();
            String key = cooldownKey(inventory, chest);
            long now = System.currentTimeMillis();
            automaticSaleCooldowns.entrySet().removeIf(entry -> now - entry.getValue() >= 5_000);
            Long lastSale = automaticSaleCooldowns.get(key);
            if (lastSale != null && now - lastSale < 5_000) {
                return;
            }

            automaticSaleCooldowns.put(key, now);
            processor.processAutomatically(inventory);
        });
    }

    private String cooldownKey(Inventory inventory, Chest chest) {
        org.bukkit.Location location = inventory.getLocation();
        if (location == null && inventory.getHolder(false) instanceof DoubleChest doubleChest) {
            location = doubleChest.getLocation();
        }
        if (location == null) {
            location = chest.getLocation();
        }

        UUID worldId = location.getWorld().getUID();
        return worldId + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }
}