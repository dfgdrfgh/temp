package moe.nea.firmament.prices;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ModMenuCreditsTest {
 @Test void usesExactRequestedHeaderWithoutChangingCredits() {
  var credits=List.of("Firmament","RaidBTW");
  assertEquals(List.of("RaidBTW, forked from Firmament"),ModMenuCredits.summaryAuthors("firmament_prices",credits));
  assertEquals(List.of("Firmament","RaidBTW"),credits);
 }
 @Test void leavesOtherModsAlone() {
  var authors=List.of("Other author");assertSame(authors,ModMenuCredits.summaryAuthors("another_mod",authors));
 }
}
