package moe.nea.firmament.prices;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ItemIdsTest {
    private String id(String json) { return ItemIds.fromData(JsonParser.parseString(json).getAsJsonObject()); }
    @Test void mapsSpecialItemsToFirmamentIds() {
        assertEquals("STONE", id("{id:'STONE'}"));
        assertEquals("DYE-BLUE", id("{id:'DYE:BLUE'}"));
        assertEquals("SHEEP;4", id("{id:'PET',petInfo:'{\"type\":\"SHEEP\",\"tier\":\"LEGENDARY\"}'}"));
        assertEquals("SHARPNESS;7", id("{id:'ENCHANTED_BOOK',enchantments:{sharpness:7}}"));
        assertEquals("BLOOD_RUNE;3", id("{id:'RUNE',runes:{blood:3}}"));
        assertEquals("ATTRIBUTE_SHARD_LIFE_RECOVERY;1", id("{id:'ATTRIBUTE_SHARD',attributes:{life_recovery:1}}"));
        assertEquals("ABICASE_SUMSUNG", id("{id:'ABICASE',model:'sumsung'}"));
        assertEquals("POTION_SPEED;4", id("{id:'POTION',potion:'speed',potion_level:4}"));
        assertEquals("PARTY_HAT_CRAB_RED_ANIMATED", id("{id:'PARTY_HAT_CRAB',party_hat_year:2022,party_hat_color:'red'}"));
        assertEquals("BALLOON_HAT_2025_BLUE", id("{id:'BALLOON_HAT_2025',party_hat_year:2025,party_hat_color:'blue'}"));
    }
    @Test void handlesInvalidAndAmbiguousData() {
        assertNull(id("{}"));
        assertEquals("null_pet", id("{id:'PET',petInfo:'broken'}"));
        assertEquals("ENCHANTED_BOOK", id("{id:'ENCHANTED_BOOK',enchantments:{sharpness:1,smite:1}}"));
    }
}
