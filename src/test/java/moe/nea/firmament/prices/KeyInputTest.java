package moe.nea.firmament.prices;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class KeyInputTest {
    @Test void resolvesScancodesWithoutGameplayKeyState() {
        assertEquals(65, KeyInput.keyForScancode(38, key -> key == 65 ? 38 : -1));
        assertEquals(-1, KeyInput.keyForScancode(999, key -> -1));
    }
}
