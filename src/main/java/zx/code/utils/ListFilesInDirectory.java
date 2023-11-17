package zx.code.utils;

import org.jruby.util.Dir;
import zx.code.test.ReadCompressedGroffFile;
import zx.code.utils.strategy.FilterStrategy;
import zx.code.utils.strategy.Impl.PrefixFilterStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author: loubaole
 * @date: 2023/11/15 9:44
 * @@Description:
 */
public class ListFilesInDirectory {


    private FilterStrategy filterStrategy;

    private String parentPath;
    public ListFilesInDirectory(){
        filterStrategy = new PrefixFilterStrategy("");
    }

    public ListFilesInDirectory(String filterString){
        filterStrategy = new PrefixFilterStrategy(filterString);
    }

    public ListFilesInDirectory(Class filterStrategyClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<FilterStrategy> filterStrategyConstructor = filterStrategyClass.getDeclaredConstructor(String.class);
        filterStrategy = filterStrategyConstructor.newInstance("");
    }

    public ListFilesInDirectory(Class filterStrategyClass,String filterString) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<FilterStrategy> filterStrategyConstructor = filterStrategyClass.getDeclaredConstructor(String.class);
        filterStrategy = filterStrategyConstructor.newInstance(filterString);
    }


    private void listFiles(String directoryPath) {
        File directory = new File(directoryPath);

        // 检查目录是否存在
        if (!directory.exists()) {
            System.out.println("目录不存在：" + directoryPath);
            return;
        }

        // 检查是否为目录
        if (!directory.isDirectory()) {
            System.out.println("指定路径不是一个目录：" + directoryPath);
            return;
        }

        // 获取目录下的所有文件和子目录
        File[] files = directory.listFiles();

        // 输出文件和子目录
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("目录: " + file.getName());
                } else {
                    System.out.println("文件: " + file.getName());
                }
            }
        }
    }

    public void listFiles(String resourcePath,String targetParentPath) throws IOException {

        // linux 和 windows切分目录的方式不同
        String[] directoryParent = resourcePath.split("/");
        directoryParent = resourcePath.split("\\\\");
        String level1Name = directoryParent[directoryParent.length-1];

        // 一级目录
        Directory level1 = new Directory();
        level1.setSourceDirectory(resourcePath);    // 设置源文件地址
        level1.setSourceFileName(level1Name);       // 设置源文件名称
        level1.setFileName(level1Name);             // 设置存储时文件名
        level1.setParentDirectory(targetParentPath);// 设置存储时文件地址

        // 先检查一级目录是否是目录
        File directory = new File(resourcePath);

        // 检查目录是否存在
        if (!directory.exists()) {
            System.out.println("目录不存在：" + resourcePath);
            return;
        }

        // 检查是否为目录
        if (!directory.isDirectory()) {
            System.out.println("指定路径不是一个目录：" + resourcePath);
            return;
        }
        this.parentPath = level1.getDirectory();
        level1.setDirectory(true);  // 确认一级目录是目录
        level1.setSourceDirectory(directory.getAbsolutePath()); // 更新源地址为绝对地址
        // 判断出一级目录存在，在目标地址存储
        createFolder(level1.getParentDirectory());
        List<Directory> children = level1.getChildren();




        // 此时，获取二级文件夹并过滤文件夹

        // 获取目录下的所有文件和子目录
        File[] files = directory.listFiles();

        if(files!=null){
            for (File file : files) {
                if (file.isDirectory() && filterStrategy.Filter(file.getName())) {

                    System.out.println("目录: " + file.getName());

                    Directory level2 = new Directory();

                    level2.setSourceFileName(file.getName());           // 源文件名称
                    level2.setSourceDirectory(file.getAbsolutePath());  // 源文件地址
                    level2.setFileName(file.getName());                 // 存储文件名称
                    level2.setParentDirectory(level1.getDirectory());   // 存储时父文件地址
                    level2.setDirectory(true);                          // 设置为文件夹
                    children.add(level2);                               // 把level2挂载到level1

                    createFolder(level2.getDirectory());                // 创建 level2 的文件夹
                    ReadDirectoryAndFile(level2);                       // 扫描 level2 文件夹下的文件
                }
            }
        }

    }


    /**
     * 读取文件夹下的文件夹以及文件
     * @param parent
     */
    private void ReadDirectoryAndFile(Directory parent) throws IOException {

        String resourcePath = parent.getSourceDirectory();
        List<Directory> chidren = parent.getChildren();

        File directory = new File(resourcePath);

        // 检查目录是否存在
        if (!directory.exists()) {
            System.out.println("目录不存在：" + resourcePath);
            return;
        }
        // 检查是否为目录
        if (directory.isDirectory()) {
            // 获取目录下的所有文件和子目录
            File[] files = directory.listFiles();

            if(files!=null){
                for (File file : files) {
                    if (file.isDirectory()) {
                        System.out.println("目录: " + file.getName());

                        // 1. 创建文件夹，以及Directory描述，挂载到父文件夹中
                        Directory newLevel = new Directory();
                        newLevel.setSourceFileName(file.getName());
                        newLevel.setSourceDirectory(file.getAbsolutePath());
                        newLevel.setFileName(file.getName());
                        newLevel.setParentDirectory(parent.getDirectory());
                        newLevel.setDirectory(true);
                        chidren.add(newLevel);
                        // 2. 检查在目标文件夹下是否已经存在文件夹
                        createFolder(newLevel.getDirectory());

                        // 3. 循环调用 ReadDirectoryAndFile 方法
                        ReadDirectoryAndFile(newLevel);
                    } else {
                        System.out.println("文件: " + file.getName());

                        // 文件的话，直接在目标文件夹下生成该文件，并把该文件Directory描述挂载到父目录
                        Directory newLevel = new Directory();
                        newLevel.setSourceFileName(file.getName());
                        newLevel.setSourceDirectory(file.getAbsolutePath());
                        // 文件名后缀改为html
                        String SourceFileName = newLevel.getSourceFileName();
                        newLevel.setFileName(replaceFileExtension(SourceFileName,"html"));
                        newLevel.setParentDirectory(parent.getDirectory());
                        newLevel.setDirectory(false);
                        // 应该添加转换的格式及类型判断

                        // 读取groff并转换为html
                        GroffToHtmlConverter converter = new GroffToHtmlConverter(parentPath);
                        String htmlContent = converter.readCompressedGroffFile(file.getAbsolutePath());

                        createHtmlFile(newLevel,htmlContent);
                        chidren.add(newLevel);


                    }
                }
            }
            // 输出文件和子目录
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        System.out.println("目录: " + file.getName());
                    } else {
                        System.out.println("文件: " + file.getName());
                    }
                }
            }
        }





    }


    private static void createFolder(String folderPath) {
        File folder = new File(folderPath);

        // 如果文件夹不存在，创建文件夹
        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (success) {
                System.out.println("文件夹创建成功");
            } else {
                System.err.println("文件夹创建失败");
            }
        } else {
            System.out.println("文件夹已经存在");
        }
    }

    private static void createHtmlFile(Directory file,String htmlContext) {
        String filePath = file.getDirectory();
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
            System.out.println("HTML文件已经存在");
        }
    }
    private static String replaceFileExtension(String fileName, String newExtension) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            // 获取文件名中最后一个点的索引
            String baseName = fileName.substring(0, lastDotIndex);
            // 使用新的扩展名替换原有的扩展名
            return baseName + "." + newExtension;
        } else {
            // 如果文件名中没有点，直接追加新的扩展名
            return fileName + "." + newExtension;
        }
    }

}



