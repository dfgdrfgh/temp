// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PriceSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = true;
    public boolean instantPrices = false;
    public int averageDays = 3;
    public static PriceSettings load(Path path) {
        if (Files.exists(path)) try (var reader = Files.newBufferedReader(path)) {
            var settings = GSON.fromJson(reader, PriceSettings.class);
            if (settings != null) {
                if (settings.averageDays != 0 && settings.averageDays != 1 && settings.averageDays != 3 && settings.averageDays != 7) settings.averageDays = 3;
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
