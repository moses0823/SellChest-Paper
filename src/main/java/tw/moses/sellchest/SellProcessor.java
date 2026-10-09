package tw.moses.sellchest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SellProcessor {
    private static final String PREFIX = "[Sell Chest] ";
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    private final SellChest plugin;
    private final SellPluginConfig sellPluginConfig;

    public SellProcessor(SellChest plugin) {
        this.plugin = plugin;
        this.sellPluginConfig = new SellPluginConfig(plugin);
    }

    public void process(Player operator, Inventory inventory) {
        process(inventory, operator);
    }

    public void processAutomatically(Inventory inventory) {
        process(inventory, null);
    }

    private void process(Inventory inventory, Player operator) {
        List<Marker> markers = findMarkers(inventory);
        if (markers.isEmpty()) {
            return;
        }
        if (markers.size() != 1) {
            send(operator, "箱子內只能有一個 SellChest 命名牌。");
            return;
        }

        String receiverName = markers.getFirst().receiverName();
        OfflinePlayer receiver = resolveReceiver(receiverName);
        if (receiver == null) {
            send(operator, "找不到收款玩家。");
            return;
        }

        Economy economy = plugin.getEconomy();
        if (economy == null) {
            send(operator, "經濟系統目前無法使用，商品未移除。");
            return;
        }

        Map<Material, BigDecimal> prices = sellPluginConfig.loadPrices();
        if (prices.isEmpty()) {
            send(operator, "無法讀取 SellPlugin 價格設定，商品未移除。");
            return;
        }

        List<Sale> sales = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
                if (item == null || item.getType().isAir()
                    || SellChestParser.receiverName(plugin, item).isPresent()) {
                continue;
            }

            BigDecimal unitPrice = prices.get(item.getType());
            if (unitPrice == null) {
                continue;
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getAmount()));
            sales.add(new Sale(slot, item.clone(), lineTotal));
            total = total.add(lineTotal);
        }

        if (sales.isEmpty()) {
            return;
        }
        if (!Double.isFinite(total.doubleValue())) {
            send(operator, "總金額超出經濟系統可處理範圍，商品未移除。");
            return;
        }

        EconomyResponse response = economy.depositPlayer(receiver, total.doubleValue());
        if (response == null || !response.transactionSuccess()) {
            send(operator, "付款失敗，商品未移除。");
            plugin.getLogger().warning("Vault deposit failed for " + receiverName + ": "
                    + (response == null ? "no response" : response.errorMessage));
            return;
        }

        for (Sale sale : sales) {
            inventory.setItem(sale.slot(), null);
        }

        if (operator != null) {
            operator.sendMessage(message("販售完成！"));
            for (Sale sale : sales) {
                operator.sendMessage(message(displayName(sale.item()) + " x" + sale.item().getAmount()
                        + " → " + economy.format(sale.lineTotal().doubleValue())));
            }
            operator.sendMessage(message("總計：" + economy.format(total.doubleValue())));
            operator.sendMessage(message("收款人：" + receiverName));
        }
    }

    private void send(Player operator, String text) {
        if (operator != null) {
            operator.sendMessage(message(text));
        }
    }

    private List<Marker> findMarkers(Inventory inventory) {
        List<Marker> markers = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            Optional<String> receiver = SellChestParser.receiverName(plugin, item);
            if (receiver.isPresent()) {
                for (int count = 0; count < item.getAmount(); count++) {
                    markers.add(new Marker(receiver.get()));
                }
            }
        }
        return markers;
    }

    private OfflinePlayer resolveReceiver(String name) {
        if (!name.matches("[A-Za-z0-9_]{1,16}")) {
            return null;
        }
        OfflinePlayer receiver = Bukkit.getOfflinePlayer(name);
        if (!receiver.isOnline() && !receiver.hasPlayedBefore()) {
            return null;
        }
        return receiver;
    }

    private String displayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta.displayName() != null) {
            return PLAIN_TEXT.serialize(meta.displayName());
        }
        String[] words = item.getType().name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder display = new StringBuilder();
        for (String word : words) {
            if (!display.isEmpty()) {
                display.append(' ');
            }
            display.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return display.toString();
    }

    private Component message(String text) {
        return Component.text(PREFIX + text);
    }

    private record Marker(String receiverName) {
    }

    private record Sale(int slot, ItemStack item, BigDecimal lineTotal) {
    }
}