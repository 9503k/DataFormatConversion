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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author: loubaole
 * @date: 2023/11/15 14:53
 * @@Description:
 */
public class GroffToAsciiDocConverter {
    public static void main(String[] args) {
        String filePath = "/usr/share/man/man2/link.2.gz"; // 替换为实际的文件路径
        filePath ="D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man2\\link.2.gz";
        try {
            String groffContent = readCompressedGroffFile(filePath);
            String asciidocContent = convertGroffToAsciiDoc(groffContent);
            String htmlContent = convertAsciiDocToHtml(asciidocContent);
            String html = convertGroffToHtml(groffContent);
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

    private static String convertGroffToAsciiDoc(String groffContent) {
        // 删除注释
        String contentWithoutComments = groffContent.replaceAll("\\s*\\'[^\\n]*", "");


        // 将 Groff 标记转换为对应的 AsciiDoc 语法
        String convertedContent = contentWithoutComments;
        // 删除 .\" 标识的注释
        convertedContent = convertedContent.replaceAll("\\.\\\\\".*?\\n", "");
        // 将 .SH 转换为 AsciiDoc 的 == 标题
        convertedContent = convertedContent.replaceAll("\\.SH", "==");

        // 将 .SS 转换为 AsciiDoc 的 === 标题
        convertedContent = convertedContent.replaceAll("\\.SS", "===");

        // 将 .B 转换为 AsciiDoc 的 * 加粗
        convertedContent = convertedContent.replaceAll("\\.B", "*");

        // 将 .I 转换为 AsciiDoc 的 _ 斜体
        convertedContent = convertedContent.replaceAll("\\.I", "_");

        // 将 .TP 转换为 AsciiDoc 的 . 表格
        convertedContent = convertedContent.replaceAll("\\.TP", ".");

        // 将 .PP 转换为 AsciiDoc 的 换行段落
        convertedContent = convertedContent.replaceAll("\\.PP", "\n");

        // 将 .TP 转换为 AsciiDoc 的 . 表格
        convertedContent = convertedContent.replaceAll("\\.TP", ".");

        // 处理 .BI 标记
        convertedContent = convertedContent.replaceAll("\\.BI (.*?)\\n", "<strong><em>$1</em></strong>");
        // 处理 .IR 标记
        convertedContent = convertedContent.replaceAll("\\.IR (.*?) (.*?)\\n", "<i>$1</i> <code>$2</code>");
        // 处理 .TH 标记
        convertedContent = convertedContent.replaceAll("\\.TH (.*?) (.*?) (.*?) (.*?) (.*?)\\n", "<html><head><title>$3</title></head><body><h1>$1 $2</h1></body></html>");
        // 移除注释
        convertedContent = convertedContent.replaceAll("\\.\\\".*?\\n", "");

        // 处理 .PD 标记
        convertedContent = convertedContent.replaceAll("\\.PD", "</p>");

        // 处理 .RS 标记
        convertedContent = convertedContent.replaceAll("\\.RS", "<div>");
        // 处理 .R 标记
        convertedContent = convertedContent.replaceAll("\\.R", "<span style='margin-right: 1em;'></span>");

        // 将其他可能的标记进行类似的映射...

        return convertedContent;
    }

    private static String convertAsciiDocToHtml(String asciidocContent) {

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        Options options = Options.builder()
                .backend("html")
                .safe(SafeMode.UNSAFE)
                .build();

        return asciidoctor.convert(asciidocContent, options);
    }

    private static String convertGroffToHtml(String groffText) {

        // 移除注释
        groffText = groffText.replaceAll("\\.\\\\\".*?\\n", "");

        // 替换 <>
        // 将 < 替换为 &lt;
        groffText = groffText.replaceAll("<", "&lt;");
        // 将 > 替换为 &gt;
        groffText = groffText.replaceAll(">", "&gt;");

        // 替换 .nf  .fi
        // 将 .nf 替换为 <pre>
        groffText = groffText.replaceAll("\\.nf", "<pre>");
        // 将 .fi 替换为 </pre>
        groffText = groffText.replaceAll("\\.fi", "</pre>");

        // 处理 .BI 标记
        groffText = groffText.replaceAll("\\.BI (.*?)\\n", "<strong><em>$1</em></strong>");

        // 处理 .BR 标记
        groffText = groffText.replaceAll("\\.BR(.*?)\\n", "<br>$1");
        // 处理 .B 标记
        groffText = groffText.replaceAll("\\.B(.*?)\\n", "<strong>$1</strong>");

        // 处理 .EX 和 .EE 标记
        groffText = groffText.replaceAll("\\.EX(.*?)\\.EE", "<pre>$1</pre>");

        // 处理 .I 标记
        groffText = groffText.replaceAll("\\.I(.*?)\\n", "<em>$1</em>");
        // 处理 .in 标记
        groffText = groffText.replaceAll("\\.in (.*?)\\n", "<div style=\"margin-left: $1;\">");
        // 处理 .IR 标记
        groffText = groffText.replaceAll("\\.IR (.*?) (.*?)\\n", "<i>$1</i> <code>$2</code>");

        // 处理 .LI 标记
        groffText = groffText.replaceAll("\\.LI(.*?)\\n", "<li>$1</li>");

        // 处理 .PD 标记
        groffText = groffText.replaceAll("\\.PD", "</p>");
        // 处理 .PP 标记
        groffText = groffText.replaceAll("\\.PP\\n", "<p>");


        // 处理 .R 标记
        groffText = groffText.replaceAll("\\.R", "<span style='margin-right: 1em;'></span>");
        // 处理 .RE 标记
        groffText = groffText.replaceAll("\\.RE\\n", "</blockquote>");

        // 处理 .RS 标记
        groffText = groffText.replaceAll("\\.RS\\n", "<blockquote>");
        // 处理 .RS 标记
        groffText = groffText.replaceAll("\\.RS", "<div>");

        // 处理 .SH 标记
        groffText = groffText.replaceAll("\\.SH(.*?)\\n", "<h1>$1</h1>");
        // 处理 .TH 标记
        groffText = groffText.replaceAll("\\.TH (\\S+) (\\S+) \"(.*?)\"\\n", "<html><head><title>$2</title></head><body>");
        // 处理 .TP 标记
        groffText = groffText.replaceAll("\\.TP(.*?)\\n", "<p>$1</p>");





        // 其他处理规则可以根据需要添加
        String style = "<style>\n" +
                "    .indent {\n" +
                "        margin-left: 20px; /* 调整缩进的像素值 */\n" +
                "    }\n" +
                "    pre {\n" +
                "        white-space: pre-wrap; /* 保留空白符，自动换行 */\n" +
                "    }"+
                "</style>";

        return "<html><body>" + groffText + "</body>"+style+"</html>";
    }

}
