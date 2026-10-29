// Derived from Firmament util/SkyblockId.kt.
// SPDX-FileCopyrightText: Firmament contributors
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Locale;

public final class ItemIds {
    private static final List<String> RARITIES = List.of("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY_SPECIAL");

    public static String fromData(JsonObject data) {
        String id = text(data, "id");
        if (id == null || id.isBlank()) return null;
        return switch (id) {
            case "PET" -> pet(data);
            case "RUNE", "UNIQUE_RUNE" -> variant(data, "runes", "_RUNE", "RUNE");
            case "ENCHANTED_BOOK" -> variant(data, "enchantments", "", "ENCHANTED_BOOK");
            case "ATTRIBUTE_SHARD" -> variant(data, "attributes", "", "ATTRIBUTE_SHARD", "ATTRIBUTE_SHARD_");
            case "ABICASE" -> "ABICASE_" + upper(text(data, "model"));
            case "POTION" -> potion(data);
            case "PARTY_HAT_SLOTH", "PARTY_HAT_CRAB", "PARTY_HAT_CRAB_ANIMATED", "CAKE_HAT_2026" -> {
                String emoji = text(data, "party_hat_emoji");
                String color = upper(text(data, "party_hat_color"));
                if (emoji != null) yield "PARTY_HAT_SLOTH_" + upper(emoji);
                if ("2022".equals(text(data, "party_hat_year"))) yield "PARTY_HAT_CRAB_" + color + "_ANIMATED";
                yield (id.equals("CAKE_HAT_2026") ? "CAKE_HAT_2026_" : "PARTY_HAT_CRAB_") + color;
            }
            case "BALLOON_HAT_2024", "BALLOON_HAT_2025" -> "BALLOON_HAT_" + text(data, "party_hat_year") + "_" + upper(text(data, "party_hat_color"));
            default -> id.replace(':', '-');
        };
    }

    private static String pet(JsonObject data) {
        try {
            var pet = JsonParser.parseString(text(data, "petInfo")).getAsJsonObject();
            int rarity = RARITIES.indexOf(text(pet, "tier"));
            return rarity < 0 || text(pet, "type") == null ? "null_pet" : upper(text(pet, "type")) + ";" + rarity;
        } catch (RuntimeException e) { return "null_pet"; }
    }

    private static String variant(JsonObject data, String key, String suffix, String fallback) {
        return variant(data, key, suffix, fallback, "");
    }

    private static String variant(JsonObject data, String key, String suffix, String fallback, String prefix) {
        if (!data.has(key) || !data.get(key).isJsonObject()) return fallback;
        var entries = data.getAsJsonObject(key).entrySet();
        if (entries.size() != 1) return fallback;
        var entry = entries.iterator().next();
        return prefix + upper(entry.getKey()) + suffix + ";" + entry.getValue().getAsInt();
    }

    private static String potion(JsonObject data) {
        for (String key : List.of("potion_name", "potion")) {
            String name = text(data, key);
            if (name != null) return "POTION_" + upper(name).replace(' ', '_') + ";" + text(data, "potion_level");
        }
        String type = text(data, "potion_type");
        return type == null ? "WATER_BOTTLE" : "POTION_" + upper(type).replace(' ', '_');
    }

    public static String text(JsonObject data, String key) {
        return !data.has(key) || !data.get(key).isJsonPrimitive() ? null : data.get(key).getAsString();
    }
    private static String upper(String text) { return text == null ? "null" : text.toUpperCase(Locale.ROOT); }
    private ItemIds() {}
}
