package moe.nea.firmament.prices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class PriceRepositoryTest {
 @TempDir Path directory;
 static InputStream response(double price) {return new ByteArrayInputStream(("{success:true,products:{STONE:{quick_status:{sellPrice:"+price+",buyPrice:"+(price+1)+"}}}}").getBytes(StandardCharsets.UTF_8));}
 @Test void failedRefreshRetainsGoodPrices() {
  var attempts=new AtomicInteger();
  try(var repo=new PriceRepository(directory.resolve("cache"),new ApiSources().snapshot(),url->{if(attempts.getAndIncrement()>0)throw new IOException("offline");return response(11);})) {
   repo.refreshBazaar();assertEquals(11,repo.bazaar("STONE").sellPrice());repo.refreshBazaar();assertEquals(11,repo.bazaar("STONE").sellPrice());
  }
 }
 @Test void providerSwitchClearsOldPricesAndRejectsLateOldResponses() throws Exception {
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var api=new ApiSources();api.bazaar="https://old.example/bazaar";
  try(var repo=new PriceRepository(directory.resolve("cache"),api.snapshot(),url->{
   if(url.contains("old.example")){entered.countDown();try{if(!release.await(5,TimeUnit.SECONDS))throw new IOException("timeout");}catch(InterruptedException e){throw new IOException(e);}return response(11);}return response(21);
  })) {
   var old=new Thread(repo::refreshBazaar);old.start();assertTrue(entered.await(5,TimeUnit.SECONDS));
   api.bazaar="https://new.example/bazaar";repo.configure(api.snapshot());assertNull(repo.bazaar("STONE"));
   repo.refreshBazaar();assertEquals(21,repo.bazaar("STONE").sellPrice());release.countDown();old.join(5000);
   assertFalse(old.isAlive());assertEquals(21,repo.bazaar("STONE").sellPrice());
  } finally {release.countDown();}
 }
}
