package zx.code;

import zx.code.utils.directory.ListFilesInDirectory;
import zx.code.utils.directory.strategy.Impl.PrefixFilterStrategy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: loubaole
 * @date: 2023/11/16 13:45
 * @@Description:
 */
public class FileTest {
    public static void main(String[] args) throws Exception {
        String source ="D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man";   // 源文件地址
        String targetPrefix = "man";    // 筛选字符串
        String targetParentPath = "D:\\我的文件资料"; // 存储地址

        ListFilesInDirectory files = new ListFilesInDirectory(PrefixFilterStrategy.class,targetPrefix);

        files.listFiles(source,targetParentPath);
    }
}
