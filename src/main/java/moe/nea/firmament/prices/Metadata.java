// Derived from Firmament ItemNameLookup and EnchantedBookCache.
// SPDX-FileCopyrightText: Firmament contributors
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

public final class Metadata {
    final Map<String, String> stocks = new HashMap<>();
    final Map<String, String> names = new HashMap<>();
    final Map<String, String> books = new HashMap<>();
    public String stock(String id) { return stocks.getOrDefault(id, id); }
    public String find(String name, boolean book) {
        name = PriceLogic.clean(name);
        return book && books.containsKey(name) ? books.get(name) : names.get(name);
    }
    public void addStocks(JsonElement json) {
        for (var element : json.getAsJsonArray()) {
            var entry = element.getAsJsonObject();
            stocks.put(entry.get("id").getAsString(), entry.get("stock").getAsString());
        }
    }
    public void addItem(String id, JsonObject item) {
        String display = ItemIds.text(item, "displayname");
        if (display == null) return;
        names.putIfAbsent(PriceLogic.clean(display), id);
        if (display.contains("Enchanted Book") && item.has("lore") && !item.getAsJsonArray("lore").isEmpty()) {
            String enchant = PriceLogic.clean(item.getAsJsonArray("lore").get(0).getAsString());
            names.putIfAbsent(enchant, id);
            if (display.endsWith("Enchanted Book")) books.put(enchant, id);
        }
    }
    public static Metadata readZip(InputStream input) throws IOException {
        var data = new Metadata();
        try (var zip = new ZipInputStream(input)) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                String name = entry.getName();
                if (name.endsWith("/constants/bazaarstocks.json")) {
                    data.addStocks(JsonParser.parseReader(new InputStreamReader(zip, StandardCharsets.UTF_8)));
                } else if (name.contains("/items/") && name.endsWith(".json") && !entry.isDirectory()) {
                    String id = name.substring(name.lastIndexOf('/') + 1, name.length() - 5);
                    data.addItem(id, JsonParser.parseReader(new InputStreamReader(zip, StandardCharsets.UTF_8)).getAsJsonObject());
                }
            }
        }
        if (data.names.isEmpty() || data.stocks.isEmpty()) throw new IOException("Incomplete NEU metadata");
        return data;
    }
}
