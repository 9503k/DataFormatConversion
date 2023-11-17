package zx.code;

import zx.code.utils.GroffToHtmlConverter;

/**
 * @author: loubaole
 * @date: ${DATE} ${TIME}
 * @@Description:
 */
public class Main {

    public static void main(String[] args) {
        String filePath = "D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man\\man1\\col.1.gz";
        try {
            GroffToHtmlConverter converter = new GroffToHtmlConverter("D:\\我的文件资料\\man");
            String htmlContent = converter.readCompressedGroffFile(filePath);
            System.out.println(htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}