// Derived from Firmament PriceData.kt, FirmFormatters.kt and SkyblockId.kt.
// SPDX-FileCopyrightText: Firmament contributors
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PriceLogic {
    public record Bazaar(double sellPrice, double buyPrice) {}
    public record Line(String label, double price) {}
    private static final String NUMBER = "[0-9]+(?:,[0-9]+)*(?:\\.[0-9]+)?[kKmMbB]?";
    private static final List<Pattern> COUNTS = List.of(
        Pattern.compile(" Amount: (" + NUMBER + ")"),
        Pattern.compile("Stored: (" + NUMBER + ")/.+"),
        Pattern.compile(".*(?:Offer amount|Amount|Order amount): (" + NUMBER + ")x"),
        Pattern.compile("Compost Available: (" + NUMBER + ")"));

    public static List<Line> lines(Bazaar bazaar, Double bin, Double average, boolean instant, long multiplier) {
        if (bazaar != null) {
            return instant
                ? List.of(new Line("Bazaar Instant Buy", bazaar.buyPrice * multiplier),
                          new Line("Bazaar Instant Sell", bazaar.sellPrice * multiplier))
                : List.of(new Line("Bazaar Buy Order", bazaar.sellPrice * multiplier),
                          new Line("Bazaar Sell Order", bazaar.buyPrice * multiplier));
        }
        if (bin == null) return List.of();
        var lines = new ArrayList<Line>();
        lines.add(new Line("Lowest BIN", bin * multiplier));
        if (average != null) lines.add(new Line("AVG Lowest BIN", average * multiplier));
        return lines;
    }

    public static List<Line> visibleLines(Bazaar bazaar, Double bin, Double average, PriceSettings settings, long multiplier) {
        if(!settings.enabled) return List.of();
        var candidates=new ArrayList<Line>();
        if(bazaar!=null) candidates.addAll(lines(bazaar,bin,average,settings.instantPrices,multiplier));
        else {
            if(bin!=null)candidates.add(new Line("Lowest BIN",bin*multiplier));
            if(average!=null)candidates.add(new Line("AVG Lowest BIN",average*multiplier));
        }
        return candidates.stream().filter(line -> switch(line.label()) {
            case "Lowest BIN" -> settings.showLowestBin;
            case "AVG Lowest BIN" -> settings.showAverageBin && settings.averageDays!=0;
            case "Bazaar Buy Order", "Bazaar Instant Buy" -> settings.showBazaarBuy;
            case "Bazaar Sell Order", "Bazaar Instant Sell" -> settings.showBazaarSell;
            default -> false;
        }).toList();
    }

    public static String format(double price) {
        long integer = (long) price;
        int decimal = (int) (10 * Math.abs(price - integer));
        return String.format(Locale.ROOT, "%,d", integer) + (decimal == 0 ? "" : "." + decimal);
    }

    public static long logicalCount(List<String> lore, int count) {
        for (String line : lore) for (Pattern pattern : COUNTS) {
            var match = pattern.matcher(clean(line));
            if (match.matches()) {
                String number = match.group(1).replace(",", "");
                char last = Character.toLowerCase(number.charAt(number.length() - 1));
                double multiplier = switch (last) { case 'k' -> 1e3; case 'm' -> 1e6; case 'b' -> 1e9; default -> 1; };
                if (multiplier != 1) number = number.substring(0, number.length() - 1);
                return (long) (Double.parseDouble(number) * multiplier);
            }
        }
        return count;
    }

    public static String clean(String text) { return text.replaceAll("§.", ""); }
    private PriceLogic() {}
}
