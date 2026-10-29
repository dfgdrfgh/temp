package moe.nea.firmament.prices;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class MarketDataTest {
    @Test void parsesBazaarAndRejectsUnsuccessfulSnapshots() {
        assertEquals(Map.of("STONE",new PriceLogic.Bazaar(11,13)), MarketData.bazaar(JsonParser.parseString("{success:true,products:{STONE:{quick_status:{sellPrice:11,buyPrice:13}}}}")));
        assertThrows(IllegalArgumentException.class, () -> MarketData.bazaar(JsonParser.parseString("{success:false,products:{}}")));
        assertThrows(IllegalArgumentException.class, () -> MarketData.bazaar(JsonParser.parseString("{success:true}")));
    }
    @Test void parsesPriceMapsWithoutAcceptingInvalidNumbers() {
        assertEquals(Map.of("SHEEP;4",123.0), MarketData.prices(JsonParser.parseString("{'SHEEP;4':123}")));
        assertThrows(IllegalArgumentException.class, () -> MarketData.prices(JsonParser.parseString("{STONE:-1}")));
    }
    @Test void findsCurrentNeuEnchantmentsAfterLoreHeader() {
        var data = new Metadata();
        data.addItem("SHARPNESS;7",JsonParser.parseString("{displayname:'§5Enchanted Book',lore:['§8Combinable in Anvil','','§9Sharpness VII','§7Increases melee damage']}").getAsJsonObject());
        assertEquals("SHARPNESS;7",data.find("Sharpness VII",true));
    }
    @Test void loadsNeuStockAliasesAndEnchantmentNames() {
        var data = new Metadata();
        data.addStocks(JsonParser.parseString("[{id:'SHARPNESS;7',stock:'ENCHANTMENT_SHARPNESS_7'}]"));
        data.addItem("SHARPNESS;7",JsonParser.parseString("{displayname:'§fEnchanted Book',lore:['§9Sharpness VII']}").getAsJsonObject());
        assertEquals("ENCHANTMENT_SHARPNESS_7",data.stock("SHARPNESS;7"));
        assertEquals("STONE",data.stock("STONE"));
        assertEquals("SHARPNESS;7",data.find("Sharpness VII",true));
        assertNull(data.find("Unknown",true));
    }
}
