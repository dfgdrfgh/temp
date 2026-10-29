// Price sources and cadence extracted from Firmament HypixelStaticData.kt.
// SPDX-FileCopyrightText: Firmament contributors
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PriceRepository implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger("FirmamentPrices");
    private static final String AUCTIONS = "https://api.eliteskyblock.com/resources/auctions/neu";
    private static final Gson GSON = new Gson();
    private final Path cache;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3, task -> {
        var thread = new Thread(task, "FirmamentPrices-download"); thread.setDaemon(true); return thread;
    });
    private volatile Map<String, PriceLogic.Bazaar> bazaar = Map.of();
    private volatile Map<Integer, Map<String, Double>> auctions = Map.of();
    private volatile Metadata metadata = new Metadata();
    private volatile boolean metadataDownloaded;

    public PriceRepository(Path cache) { this.cache = cache; }
    public PriceLogic.Bazaar bazaar(String id) { return bazaar.get(metadata.stock(id)); }
    public Double auction(String id, int days) { return auctions.getOrDefault(days, Map.of()).get(id); }
    public String find(String name, boolean book) { return metadata.find(name, book); }

    public void start() {
        executor.scheduleWithFixedDelay(() -> { if (!metadataDownloaded) loadMetadata(); }, 0, 2, TimeUnit.MINUTES);
        executor.scheduleWithFixedDelay(() -> safely("Bazaar", () -> bazaar = MarketData.bazaar(json("https://api.hypixel.net/skyblock/bazaar"))), 0, 2, TimeUnit.MINUTES);
        executor.scheduleWithFixedDelay(this::refreshAuctions, 0, 2, TimeUnit.MINUTES);
    }

    private void refreshAuctions() {
        for (int days : new int[] {0, 1, 3, 7}) safely("auctions (" + days + " days)", () -> {
            var prices = MarketData.prices(json(AUCTIONS + (days == 0 ? "" : "/average-lbin/" + days + "day")));
            var updated = new HashMap<>(auctions);
            updated.put(days, prices);
            auctions = Map.copyOf(updated);
        });
    }

    private void loadMetadata() {
        safely("cached NEU metadata", () -> {
            if (Files.exists(cache)) {
                try (var reader = Files.newBufferedReader(cache)) {
                    Metadata saved = GSON.fromJson(reader, Metadata.class);
                    if (saved == null || saved.names == null || saved.stocks == null || saved.books == null || saved.names.isEmpty())
                        throw new IOException("Invalid metadata cache");
                    metadata = saved;
                }
            }
        });
        safely("NEU metadata", () -> {
            Metadata downloaded;
            try (var input = request("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/master.zip")) {
                downloaded = Metadata.readZip(input);
            }
            metadata = downloaded;
            metadataDownloaded = true;
            Files.createDirectories(cache.getParent());
            Path temporary = cache.resolveSibling(cache.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(downloaded));
            Files.move(temporary, cache, StandardCopyOption.REPLACE_EXISTING);
            LOG.log(System.Logger.Level.INFO, "Price metadata ready: " + downloaded.names.size() + " names");
        });
    }

    private static JsonElement json(String url) throws IOException {
        try (var input = request(url); var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }
    private static InputStream request(String url) throws IOException {
        var connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "FirmamentPrices/1.0.0 (https://github.com/dfgdrfgh/temp)");
        int status = connection.getResponseCode();
        if (status != 200) { connection.disconnect(); throw new IOException("HTTP " + status + " from " + url); }
        return connection.getInputStream();
    }
    @FunctionalInterface private interface Download { void run() throws IOException; }
    private static void safely(String label, Download download) {
        try { download.run(); }
        catch (IOException | RuntimeException e) { LOG.log(System.Logger.Level.WARNING, "Could not refresh " + label + "; retaining previous data", e); }
    }
    @Override public void close() { executor.shutdownNow(); }
}
