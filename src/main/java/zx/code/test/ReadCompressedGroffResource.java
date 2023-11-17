package zx.code.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * @author: loubaole
 * @date: 2023/11/15 9:42
 * @@Description:
 */
public class ReadCompressedGroffResource {

    public static void main(String[] args) {
        String resourcePath = "/man/man2/link.2.gz"; // 注意：路径以"/"开头，表示在resources目录下

        try {
            String content = readCompressedGroffResource(resourcePath);
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readCompressedGroffResource(String resourcePath) throws IOException {
        StringBuilder content = new StringBuilder();

        ClassLoader classLoader = ReadCompressedGroffResource.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
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
}
