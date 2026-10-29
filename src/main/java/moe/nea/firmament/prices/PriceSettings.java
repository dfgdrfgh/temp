// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import io.github.notenoughupdates.moulconfig.annotations.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PriceSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @ConfigOption(name="Enable price tooltips",desc="Show price information when hovering SkyBlock items.") @ConfigEditorBoolean
    public boolean enabled = true;
    @ConfigOption(name="Instant Bazaar prices",desc="Show instant buy/sell prices instead of order prices.") @ConfigEditorBoolean
    public boolean instantPrices = false;
    public int averageDays = 3;
    @ConfigOption(name="Lowest BIN",desc="Show the current Lowest BIN line for auction items.") @ConfigEditorBoolean
    public boolean showLowestBin=true;
    @ConfigOption(name="Average Lowest BIN",desc="Show the average BIN line independently of the current Lowest BIN line.") @ConfigEditorBoolean
    public boolean showAverageBin=true;
    @ConfigOption(name="Average window",desc="Choose the period used for the average BIN line.") @ConfigEditorDropdown(values={"Off","1 day","3 days","7 days"})
    public transient int averageSelection=2;
    @ConfigOption(name="Bazaar buy price",desc="Show the Bazaar Buy Order or Instant Buy line.") @ConfigEditorBoolean
    public boolean showBazaarBuy=true;
    @ConfigOption(name="Bazaar sell price",desc="Show the Bazaar Sell Order or Instant Sell line.") @ConfigEditorBoolean
    public boolean showBazaarSell=true;
    @ConfigOption(name="Stack prices",desc="Allow the stack-price key to multiply prices by the item's logical quantity.") @ConfigEditorBoolean
    public boolean stackPrices=true;
    @ConfigOption(name="Stack key hint",desc="Show the key hint underneath items with a quantity greater than one.") @ConfigEditorBoolean
    public boolean stackHint=true;
    public ApiSources apis=new ApiSources();
    public PriceSettings copy() {return GSON.fromJson(GSON.toJson(this),PriceSettings.class);}
    public void prepareMenu() {averageSelection=switch(averageDays){case 0->0;case 1->1;case 7->3;default->2;};}
    public void readMenu() {averageDays=new int[]{0,1,3,7}[Math.clamp(averageSelection,0,3)];}
    public static PriceSettings load(Path path) {
        if (Files.exists(path)) try (var reader = Files.newBufferedReader(path)) {
            var settings = GSON.fromJson(reader, PriceSettings.class);
            if (settings != null) {
                if (settings.averageDays != 0 && settings.averageDays != 1 && settings.averageDays != 3 && settings.averageDays != 7) settings.averageDays = 3;
                if(settings.apis==null) settings.apis=new ApiSources();
                try {settings.apis.snapshot();} catch(IllegalArgumentException e) {settings.apis.resetDefaults();}
                return settings;
            }
        } catch (IOException | RuntimeException e) {
            System.getLogger("FirmamentPrices").log(System.Logger.Level.WARNING, "Could not read price settings", e);
        }
        return new PriceSettings();
    }
    public void save(Path path) {
        try { Files.createDirectories(path.getParent()); Files.writeString(path, GSON.toJson(this)); }
        catch (IOException e) { System.getLogger("FirmamentPrices").log(System.Logger.Level.WARNING, "Could not save price settings", e); }
    }
}
