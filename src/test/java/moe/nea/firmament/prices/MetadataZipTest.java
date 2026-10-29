package moe.nea.firmament.prices;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;
class MetadataZipTest {
    @Test void readsIndependentZipEntriesAndRejectsIncompleteRepository() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("repo/constants/bazaarstocks.json"));
            zip.write("[{id:'SHARPNESS;7',stock:'ENCHANTMENT_SHARPNESS_7'}]".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            zip.putNextEntry(new ZipEntry("repo/items/SHARPNESS;7.json"));
            zip.write("{displayname:'Enchanted Book',lore:['Sharpness VII']}".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
        }
        var data = Metadata.readZip(new ByteArrayInputStream(bytes.toByteArray()));
        assertEquals("SHARPNESS;7",data.find("Sharpness VII",true));
        assertEquals("ENCHANTMENT_SHARPNESS_7",data.stock("SHARPNESS;7"));
        assertThrows(IOException.class, () -> Metadata.readZip(new ByteArrayInputStream(new byte[0])));
    }
}
