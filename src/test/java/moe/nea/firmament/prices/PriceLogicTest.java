package moe.nea.firmament.prices;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PriceLogicTest {
    @Test void preservesOrderPriceDirectionsAndBazaarPriority() {
        var result = PriceLogic.lines(new PriceLogic.Bazaar(11, 13), 999.0, 1000.0, false, 1);
        assertEquals(List.of(new PriceLogic.Line("Bazaar Buy Order", 11), new PriceLogic.Line("Bazaar Sell Order", 13)), result);
    }
    @Test void preservesInstantPriceDirectionsAndStackMultiplier() {
        assertEquals(List.of(new PriceLogic.Line("Bazaar Instant Buy", 832), new PriceLogic.Line("Bazaar Instant Sell", 704)),
            PriceLogic.lines(new PriceLogic.Bazaar(11, 13), null, null, true, 64));
    }
    @Test void preservesBinAverageAndMissingDataBehavior() {
        assertEquals(List.of(new PriceLogic.Line("Lowest BIN", 25), new PriceLogic.Line("AVG Lowest BIN", 30)),
            PriceLogic.lines(null, 12.5, 15.0, false, 2));
        assertEquals(List.of(new PriceLogic.Line("Lowest BIN", 12.5)), PriceLogic.lines(null, 12.5, null, false, 1));
        assertTrue(PriceLogic.lines(null, null, 15.0, false, 1).isEmpty());
    }
    @Test void preservesFirmamentTruncationRatherThanRounding() {
        assertEquals("1,234.9", PriceLogic.format(1234.99));
        assertEquals("1", PriceLogic.format(1));
        assertEquals("0.1", PriceLogic.format(0.19));
    }
    @Test void readsLogicalSackAndOrderQuantities() {
        assertEquals(2500, PriceLogic.logicalCount(List.of("Stored: 2.5k/1m"), 1));
        assertEquals(1200000, PriceLogic.logicalCount(List.of(" Amount: 1.2M"), 1));
        assertEquals(123456, PriceLogic.logicalCount(List.of("Offer amount: 123,456x"), 1));
        assertEquals(120, PriceLogic.logicalCount(List.of("Compost Available: 120"), 1));
        assertEquals(64, PriceLogic.logicalCount(List.of("unrelated"), 64));
    }
}
