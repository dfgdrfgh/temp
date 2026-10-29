// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import io.github.notenoughupdates.moulconfig.annotations.*;
import java.net.URI;

public final class ApiSources {
    public static final String DEFAULT_BAZAAR="https://api.hypixel.net/skyblock/bazaar";
    public static final String DEFAULT_BIN="https://api.eliteskyblock.com/resources/auctions/neu";
    public static final String DEFAULT_METADATA="https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/master.zip";
    @ConfigOption(name="Bazaar API",desc="Full URL. Must return Hypixel Bazaar JSON with success and products/quick_status.") @ConfigEditorText
    public String bazaar=DEFAULT_BAZAAR;
    @ConfigOption(name="Lowest BIN API",desc="Full URL. Must return a JSON object mapping NEU item IDs to prices.") @ConfigEditorText
    public String lowestBin=DEFAULT_BIN;
    @ConfigOption(name="1-day average BIN API",desc="Full URL. Must return a JSON object mapping NEU item IDs to average prices.") @ConfigEditorText
    public String average1=DEFAULT_BIN+"/average-lbin/1day";
    @ConfigOption(name="3-day average BIN API",desc="Full URL. Must return a JSON object mapping NEU item IDs to average prices.") @ConfigEditorText
    public String average3=DEFAULT_BIN+"/average-lbin/3day";
    @ConfigOption(name="7-day average BIN API",desc="Full URL. Must return a JSON object mapping NEU item IDs to average prices.") @ConfigEditorText
    public String average7=DEFAULT_BIN+"/average-lbin/7day";
    @ConfigOption(name="Item metadata source",desc="Full URL of a NEU-format repository ZIP containing items/ and constants/bazaarstocks.json.") @ConfigEditorText
    public String metadata=DEFAULT_METADATA;
    @ConfigOption(name="Restore default APIs",desc="Restore all six built-in API/source addresses.") @ConfigEditorButton(buttonText="Reset APIs")
    public transient Runnable reset=()->{};

    public record Snapshot(String bazaar,String lowestBin,String average1,String average3,String average7,String metadata) {
        public String auction(int days) { return switch(days) {case 0->lowestBin;case 1->average1;case 3->average3;case 7->average7;default->throw new IllegalArgumentException("Unsupported average window");}; }
    }
    public Snapshot snapshot() {
        return new Snapshot(validate("Bazaar",bazaar),validate("Lowest BIN",lowestBin),validate("1-day average",average1),
            validate("3-day average",average3),validate("7-day average",average7),validate("Metadata",metadata));
    }
    public void resetDefaults() {
        bazaar=DEFAULT_BAZAAR;lowestBin=DEFAULT_BIN;average1=DEFAULT_BIN+"/average-lbin/1day";
        average3=DEFAULT_BIN+"/average-lbin/3day";average7=DEFAULT_BIN+"/average-lbin/7day";metadata=DEFAULT_METADATA;
    }
    private static String validate(String name,String value) {
        try {
            var uri=URI.create(value==null?"":value.trim());
            if ((!"https".equalsIgnoreCase(uri.getScheme())&&!"http".equalsIgnoreCase(uri.getScheme())) || uri.getHost()==null || uri.getFragment()!=null)
                throw new IllegalArgumentException();
            return uri.toString();
        } catch (IllegalArgumentException e) {throw new IllegalArgumentException(name+" needs a complete http:// or https:// URL.");}
    }
}
