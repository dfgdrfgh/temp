// Price sources and cadence extracted from Firmament HypixelStaticData.kt.
// SPDX-FileCopyrightText: Firmament contributors
// SPDX-License-Identifier: GPL-3.0-or-later
package moe.nea.firmament.prices;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public final class PriceRepository implements AutoCloseable {
    private static final System.Logger LOG=System.getLogger("FirmamentPrices");
    private static final Gson GSON=new Gson();
    private final Path cache;
    @FunctionalInterface interface Transport {InputStream request(String url) throws IOException;}
    private final Transport transport;
    private final ScheduledExecutorService executor=Executors.newScheduledThreadPool(3,task->{
        var thread=new Thread(task,"FirmamentPrices-download");thread.setDaemon(true);return thread;
    });
    private record Sources(ApiSources.Snapshot endpoints,long revision) {}
    private record CachedMetadata(String source,Metadata data) {}
    private volatile Sources sources;
    private volatile Map<String,PriceLogic.Bazaar> bazaar=Map.of();
    private volatile Map<Integer,Map<String,Double>> auctions=Map.of();
    private volatile Metadata metadata=new Metadata();
    private volatile boolean metadataDownloaded;
    private boolean started;

    public PriceRepository(Path cache) {this(cache,new ApiSources().snapshot(),PriceRepository::request);}
    PriceRepository(Path cache,ApiSources.Snapshot endpoints,Transport transport) {
        this.cache=cache;this.sources=new Sources(endpoints,0);this.transport=transport;
    }
    public PriceLogic.Bazaar bazaar(String id) {return bazaar.get(metadata.stock(id));}
    public Double auction(String id,int days) {return auctions.getOrDefault(days,Map.of()).get(id);}
    public String find(String name,boolean book) {return metadata.find(name,book);}

    public synchronized void configure(ApiSources.Snapshot endpoints) {
        if(sources.endpoints.equals(endpoints)) return;
        boolean metadataChanged=!sources.endpoints.metadata().equals(endpoints.metadata());
        sources=new Sources(endpoints,sources.revision+1);
        bazaar=Map.of();auctions=Map.of();
        if(metadataChanged){metadata=new Metadata();metadataDownloaded=false;}
        if(started){executor.execute(this::refreshBazaar);executor.execute(this::refreshAuctions);executor.execute(this::loadMetadata);}
    }
    public synchronized void start() {
        if(started) return;started=true;
        executor.scheduleWithFixedDelay(()->{if(!metadataDownloaded)loadMetadata();},0,2,TimeUnit.MINUTES);
        executor.scheduleWithFixedDelay(this::refreshBazaar,0,2,TimeUnit.MINUTES);
        executor.scheduleWithFixedDelay(this::refreshAuctions,0,2,TimeUnit.MINUTES);
    }
    void refreshBazaar() {
        var source=sources;
        safely("Bazaar",()->{
            var downloaded=MarketData.bazaar(json(source.endpoints.bazaar()));
            synchronized(this){if(sources==source)bazaar=downloaded;}
        });
    }
    private void refreshAuctions() {
        var source=sources;
        for(int days:new int[]{0,1,3,7}) {
            if(sources!=source)return;
            safely("auctions ("+days+" days)",()->{
                var prices=MarketData.prices(json(source.endpoints.auction(days)));
                synchronized(this){if(sources!=source)return;var updated=new HashMap<>(auctions);updated.put(days,prices);auctions=Map.copyOf(updated);}
            });
        }
    }
    private void loadMetadata() {
        var source=sources;
        if(metadataDownloaded)return;
        safely("cached NEU metadata",()->{
            if(Files.exists(cache))try(var reader=Files.newBufferedReader(cache)){
                var json=JsonParser.parseReader(reader).getAsJsonObject();
                CachedMetadata saved=json.has("source")?GSON.fromJson(json,CachedMetadata.class)
                    :new CachedMetadata(ApiSources.DEFAULT_METADATA,GSON.fromJson(json,Metadata.class));
                if(!source.endpoints.metadata().equals(saved.source))return;
                if(saved.data==null||saved.data.names==null||saved.data.stocks==null||saved.data.books==null||saved.data.names.isEmpty())throw new IOException("Invalid metadata cache");
                synchronized(this){if(sources==source)metadata=saved.data;}
            }
        });
        safely("NEU metadata",()->{
            Metadata downloaded;
            try(var input=transport.request(source.endpoints.metadata())){downloaded=Metadata.readZip(input);}
            synchronized(this){
                if(sources!=source)return;
                metadata=downloaded;metadataDownloaded=true;
                Files.createDirectories(cache.toAbsolutePath().getParent());
                Path temporary=cache.resolveSibling(cache.getFileName()+".tmp");
                Files.writeString(temporary,GSON.toJson(new CachedMetadata(source.endpoints.metadata(),downloaded)));
                Files.move(temporary,cache,StandardCopyOption.REPLACE_EXISTING);
            }
            LOG.log(System.Logger.Level.INFO,"Price metadata ready: "+downloaded.names.size()+" names");
        });
    }
    private JsonElement json(String url) throws IOException {
        try(var input=transport.request(url);var reader=new InputStreamReader(input,StandardCharsets.UTF_8)){return JsonParser.parseReader(reader);}
    }
    private static InputStream request(String url) throws IOException {
        var connection=(HttpURLConnection)URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(15000);connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent","FirmamentPrices/1.1.0 (https://github.com/dfgdrfgh/temp)");
        int status=connection.getResponseCode();
        if(status!=200){connection.disconnect();throw new IOException("HTTP "+status);}
        return connection.getInputStream();
    }
    @FunctionalInterface private interface Download {void run() throws IOException;}
    private static void safely(String label,Download download) {
        try{download.run();}catch(IOException|RuntimeException e){LOG.log(System.Logger.Level.WARNING,"Could not refresh "+label+"; retaining previous data",e);}
    }
    @Override public void close(){executor.shutdownNow();}
}
