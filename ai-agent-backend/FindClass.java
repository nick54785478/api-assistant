import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

public class FindClass {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("C:\\Users\\nick5\\.m2\\repository\\org\\springframework\\ai\\spring-ai-mcp\\2.0.0-M3\\spring-ai-mcp-2.0.0-M3.jar");
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().contains("ToolCallbackProvider")) {
                System.out.println(entry.getName());
            }
        }
        zip.close();
    }
}
