package tw.moses.sellchest;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public final class SellChestCommand implements CommandExecutor {
    private final SellChest plugin;

    public SellChestCommand(SellChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("用法：/sellchest give <玩家>"));
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("找不到在線玩家：" + args[1]));
            return true;
        }

        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(createTaggedNameTag());
        leftovers.values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
        sender.sendMessage(Component.text("已將自動販賣牌交給 " + target.getName() + "。"));
        if (sender != target) {
            target.sendMessage(Component.text("你收到了一個自動販賣牌。"));
        }
        return true;
    }

    private ItemStack createTaggedNameTag() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("SellChest-未設定"));
        meta.lore(List.of(
        Component.text("將此命名牌命名為 SellChest-玩家名稱"),
        Component.text("可以自動售賣內容物"),
        Component.text("紅石觸發每 5 秒最多自動販售一次")
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "sell_tag"), PersistentDataType.BYTE, (byte) 1);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}