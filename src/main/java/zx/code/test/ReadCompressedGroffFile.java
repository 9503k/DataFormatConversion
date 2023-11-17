package zx.code.test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jruby.RubyProcess;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * @author: loubaole
 * @date: 2023/11/15 9:33
 * @@Description:
 */
public class ReadCompressedGroffFile {
    public static void main(String[] args) {
    String resourcePath = "/usr/share/man/man2/link.2.gz"; // 注意：路径以"/"开头，表示在resources目录下

        try {
            String groffContent = readCompressedGroffResource(resourcePath);
            String htlm = convertGroffToHtml(groffContent);
            System.out.println(htlm);
        } catch (IOException e) {
            e.printStackTrace();
        }
}

    private static String readCompressedGroffResource(String resourcePath) throws IOException {
        StringBuilder content = new StringBuilder();

        // 使用ClassLoader获取资源
        ClassLoader classLoader = ReadCompressedGroffFile.class.getClassLoader();
        try (FileInputStream fileInputStream = new FileInputStream(resourcePath);
             GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }
//    private static String convertGroffToHtml(String groffContent) throws IOException, InterruptedException {
//        ProcessBuilder processBuilder = new ProcessBuilder("groff", "-Thtml");
//        processBuilder.redirectErrorStream(true);
//
//        Process process = processBuilder.start();
//
//        try (OutputStream outputStream = process.getOutputStream();
//             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
//
//            // 向groff命令写入groff内容
//            writer.write(groffContent);
//            writer.flush();
//        }
//
//        // 读取并返回HTML输出
//        try (InputStream inputStream = process.getInputStream();
//             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//
//            StringBuilder htmlContent = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                htmlContent.append(line).append("\n");
//            }
//
//            int exitCode = process.waitFor();
//            if (exitCode != 0) {
//                throw new RuntimeException("Groff command failed with exit code: " + exitCode);
//            }
//
//            return htmlContent.toString();
//        }
//    }


    private static void displayGroffManPage(String groffContent) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("man", "-l", "-");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            process.getOutputStream().write(groffContent.getBytes());
            process.getOutputStream().close();

            // 读取并打印命令输出
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Process exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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
