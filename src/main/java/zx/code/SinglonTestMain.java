package zx.code;

import zx.code.utils.html.GroffToHtmlConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author: loubaole
 * @date: ${DATE} ${TIME}
 * @@Description:
 */
public class SinglonTestMain {

    public static void main(String[] args) {
//        String filePath = "D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man1\\col.1.gz";
//        String filePath = "D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man2\\link.2.gz";
//        String filePath = "D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man8\\netstat.8.gz";
//        String filePath = "D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man8\\sfdisk.8.gz";
        String filePath = "D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man1\\tclsh.1.gz";


        try {
            GroffToHtmlConverter converter = new GroffToHtmlConverter("D:\\我的文件资料\\man");
            String htmlContent = converter.readCompressedGroffFile(filePath);
            createHtmlFile("C:/Users/LBL/Desktop/test.html",htmlContent);
//            System.out.println(htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createHtmlFile(String filePath, String htmlContext) {
        File htmlFile = new File(filePath);

        // 如果文件不存在，创建文件并写入HTML内容
        if (!htmlFile.exists()) {
            try (FileWriter writer = new FileWriter(htmlFile)) {
                // 写入HTML内容
                writer.write(htmlContext);
                System.out.println("HTML文件创建成功");
            } catch (IOException e) {
                System.err.println("HTML文件创建失败：" + e.getMessage());
            }
        } else {
            try (FileWriter writer = new FileWriter(htmlFile)) {
                // 写入HTML内容
                writer.write(htmlContext);
                System.out.println("HTML文件创建成功");
            } catch (IOException e) {
                System.err.println("HTML文件创建失败：" + e.getMessage());
            }        }
    }
}