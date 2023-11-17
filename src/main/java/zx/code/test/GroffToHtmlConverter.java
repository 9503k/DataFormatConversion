package zx.code.test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 * @author: loubaole
 * @date: 2023/11/15 14:24
 * @@Description:
 */
public class GroffToHtmlConverter {
    public static void main(String[] args) {
        String filePath = "/usr/share/man/man2/link.2.gz"; // 注意：这是绝对路径

        try {
            String groffContent = readCompressedGroffFile(filePath);
            String htmlContent = convertGroffToHtml(groffContent);
            System.out.println(htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readCompressedGroffFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath));
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    private static String convertGroffToHtml(String groffContent) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        Options options = Options.builder()
                .backend("html")
                .safe(SafeMode.UNSAFE)
                .build();

        return asciidoctor.convert(groffContent, options);
    }

}
