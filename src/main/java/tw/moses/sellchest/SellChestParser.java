package tw.moses.sellchest;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public final class SellChestParser {
    private static final String PREFIX = "SellChest-";
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    private SellChestParser() {
    }

    public static Optional<String> receiverName(Plugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.NAME_TAG || !item.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "sell_tag"), PersistentDataType.BYTE)
                || meta.displayName() == null) {
            return Optional.empty();
        }

        String name = PLAIN_TEXT.serialize(meta.displayName());
        return name.startsWith(PREFIX) ? Optional.of(name.substring(PREFIX.length())) : Optional.empty();
    }
}