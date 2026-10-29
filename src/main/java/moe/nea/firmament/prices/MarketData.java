// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;

public final class MarketData {
    public static Map<String, Double> prices(JsonElement json) {
        var result = new HashMap<String, Double>();
        for (var entry : json.getAsJsonObject().entrySet()) result.put(entry.getKey(), price(entry.getValue()));
        return Map.copyOf(result);
    }
    public static Map<String, PriceLogic.Bazaar> bazaar(JsonElement json) {
        var root = json.getAsJsonObject();
        if (!root.has("success") || !root.get("success").getAsBoolean() || !root.has("products"))
            throw new IllegalArgumentException("Unsuccessful Bazaar response");
        var result = new HashMap<String, PriceLogic.Bazaar>();
        for (var entry : root.getAsJsonObject("products").entrySet()) {
            var status = entry.getValue().getAsJsonObject().getAsJsonObject("quick_status");
            result.put(entry.getKey(), new PriceLogic.Bazaar(price(status.get("sellPrice")), price(status.get("buyPrice"))));
        }
        return Map.copyOf(result);
    }
    private static double price(JsonElement element) {
        double value = element.getAsDouble();
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Invalid market price");
        return value;
    }
    private MarketData() {}
}
