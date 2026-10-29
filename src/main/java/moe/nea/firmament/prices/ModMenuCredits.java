// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import java.util.List;
public final class ModMenuCredits {
    public static List<String> summaryAuthors(String modId,List<String> authors) {
        return "firmament_prices".equals(modId)?List.of("RaidBTW, forked from Firmament"):authors;
    }
    private ModMenuCredits() {}
}
