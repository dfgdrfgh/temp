package moe.nea.firmament.prices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class PriceSettingsTest {
    @TempDir Path directory;
    @Test void loadsOldSettingsWithoutLosingDefaults() throws Exception {
        Path file=directory.resolve("config.json");Files.writeString(file,"{\"enabled\":false,\"instantPrices\":true,\"averageDays\":7}");
        var settings=PriceSettings.load(file);
        assertFalse(settings.enabled);assertTrue(settings.instantPrices);assertEquals(7,settings.averageDays);
        assertTrue(settings.showLowestBin);assertTrue(settings.showAverageBin);assertTrue(settings.showBazaarBuy);
        assertEquals(ApiSources.DEFAULT_BAZAAR,settings.apis.bazaar);
    }
    @Test void savesIndependentTogglesAndCustomSources() {
        var settings=new PriceSettings();settings.showLowestBin=false;settings.showBazaarSell=false;
        settings.apis.bazaar="https://example.com/bazaar";
        Path file=directory.resolve("config.json");settings.save(file);var loaded=PriceSettings.load(file);
        assertFalse(loaded.showLowestBin);assertFalse(loaded.showBazaarSell);assertTrue(loaded.showAverageBin);
        assertEquals("https://example.com/bazaar",loaded.apis.bazaar);
    }
    @Test void rejectsInvalidSourcesAndCanResetDefaults() {
        var api=new ApiSources();api.bazaar="file:///tmp/prices.json";
        assertThrows(IllegalArgumentException.class,api::snapshot);
        api.bazaar="not a url";assertThrows(IllegalArgumentException.class,api::snapshot);
        api.bazaar="http://localhost:8080/bazaar";assertDoesNotThrow(api::snapshot);
        api.resetDefaults();assertEquals(ApiSources.DEFAULT_BAZAAR,api.bazaar);
    }
    @Test void averageLineCanWorkWithoutCurrentBinData() {
        var settings=new PriceSettings();settings.showLowestBin=false;
        assertEquals(List.of(new PriceLogic.Line("AVG Lowest BIN",1000)),PriceLogic.visibleLines(null,null,1000.0,settings,1));
    }
    @Test void independentPriceLinesDoNotInventAuctionFallbackForBazaarItems() {
        var settings=new PriceSettings();settings.showBazaarBuy=false;
        var lines=PriceLogic.visibleLines(new PriceLogic.Bazaar(11,13),999.0,1000.0,settings,1);
        assertEquals(List.of(new PriceLogic.Line("Bazaar Sell Order",13)),lines);
        settings.showBazaarSell=false;assertTrue(PriceLogic.visibleLines(new PriceLogic.Bazaar(11,13),999.0,1000.0,settings,1).isEmpty());
        settings.showLowestBin=false;
        assertEquals(List.of(new PriceLogic.Line("AVG Lowest BIN",1000)),PriceLogic.visibleLines(null,999.0,1000.0,settings,1));
    }
}
