package zx.code;

import org.jruby.RubyProcess;
import zx.code.utils.directory.ListFilesInDirectory;
import zx.code.utils.directory.strategy.Impl.PrefixFilterStrategy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: loubaole
 * @date: 2023/11/18 11:22
 * @@Description:
 */
public class Main {

    public static void main(String[] args) throws Exception {
//        String source ="D:\\我的文件资料\\代码仓库\\Java\\DataFormatConversion\\src\\main\\resources\\man";   // 源文件地址
//        String targetPrefix = "man";    // 筛选字符串
//        String targetParentPath = "D:\\我的文件资料"; // 存储地址
//
//        ListFilesInDirectory files = new ListFilesInDirectory(PrefixFilterStrategy.class,targetPrefix);
//
//        files.listFiles(source,targetParentPath);
        if(args.length<3){
            System.out.println("参数缺失，请检查参数！");
        }
        String sourcePath = args[0];
        String targetPrefix = args[1];
        String targetPath = args[2];
        ListFilesInDirectory files = new ListFilesInDirectory(PrefixFilterStrategy.class,targetPrefix);

        files.listFiles(sourcePath,targetPath);
        System.out.println("生成成功！");


    }
}
