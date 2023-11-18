package zx.code.utils.directory;

import org.jruby.RubyProcess;
import zx.code.utils.common.Directory;
import zx.code.utils.common.EnumOS;
import zx.code.utils.html.GroffToHtmlConverter;
import zx.code.utils.directory.strategy.FilterStrategy;
import zx.code.utils.directory.strategy.Impl.PrefixFilterStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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

    public void listFiles(String resourcePath,String targetParentPath) throws Exception {
//        targetParentPath = Paths.get(targetParentPath).toAbsolutePath().toString();
        // linux 和 windows切分目录的方式不同
        String[] directoryParent = new String[0];
        String osName = System.getProperty("os.name");
        System.out.println("操作系统："+osName);
        for (EnumOS enumOS : EnumOS.values()){
            if(osName.toUpperCase().contains(enumOS.getOsName())){
                directoryParent = resourcePath.split(enumOS.getSplitPathChar());
            }
        }

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
        level1.setIsDirectory(true);  // 确认一级目录是目录
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
                    level2.setIsDirectory(true);                          // 设置为文件夹
                    children.add(level2);                               // 把level2挂载到level1
                    createFolder(level2.getDirectory());                // 创建 level2 的文件夹
                    ReadDirectoryAndFile(level2);                       // 扫描 level2 文件夹下的文件
                    creatIndexHtml(level2);
                }
            }
        }

        creatIndexHtml(level1);

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
                        System.out.println("目录: " + file.getAbsolutePath());

                        // 1. 创建文件夹，以及Directory描述，挂载到父文件夹中
                        Directory newLevel = new Directory();
                        newLevel.setSourceFileName(file.getName());
                        newLevel.setSourceDirectory(file.getAbsolutePath());
                        newLevel.setFileName(file.getName());
                        newLevel.setParentDirectory(parent.getDirectory());
                        newLevel.setIsDirectory(true);
                        chidren.add(newLevel);
                        // 2. 检查在目标文件夹下是否已经存在文件夹
                        createFolder(newLevel.getDirectory());

                        // 3. 循环调用 ReadDirectoryAndFile 方法
                        ReadDirectoryAndFile(newLevel);

                        // 4. 在该文件夹下创建 index.html
                        creatIndexHtml(newLevel);

                    } else {
                        System.out.println("文件: " + file.getAbsolutePath());

                        // 文件的话，直接在目标文件夹下生成该文件，并把该文件Directory描述挂载到父目录
                        Directory newLevel = new Directory();
                        newLevel.setSourceFileName(file.getName());
                        newLevel.setSourceDirectory(file.getAbsolutePath());
                        // 文件名后缀改为html
                        String SourceFileName = newLevel.getSourceFileName();
                        newLevel.setFileName(replaceFileExtension(SourceFileName,"html"));
                        newLevel.setParentDirectory(parent.getDirectory());
                        newLevel.setIsDirectory(false);
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
        System.out.println("写入父地址："+file.getParentDirectory());
        String filePath = file.getDirectory();
        createHtmlFile(filePath,htmlContext);
    }
    private static void createHtmlFile(String filePath,String htmlContext) {

        File htmlFile = new File(filePath);
        System.out.println("写入地址："+filePath);
        try (FileWriter writer = new FileWriter(htmlFile)) {
            // 写入HTML内容
            writer.write(htmlContext);

            if (htmlFile.exists()) {
                System.out.println("HTML文件覆盖成功");
            } else {
                System.out.println("HTML文件创建成功");
            }
        } catch (IOException e) {
            System.err.println("HTML文件操作失败：" + e.getMessage());
        }
    }

    static String header = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Table of Contents</title>\n" +
            "    <style>\n" +
            "        /* Add your CSS styles here */\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            margin: 20px;\n" +
            "        }\n" +
            "\n" +
            "        h1 {\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "\n" +
            "        ul {\n" +
            "            list-style-type: none;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "\n" +
            "        li {\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "%s"+
            "</body>\n" +
            "</html>";
    static String urlDom = "<a href = \"%s\">%s</a>";
    /**
     * 在文件夹中创建一个目录 index.html
     * 该文件中包含指向该目录下的子文件的超链接：
     *      1. 文件的话，指向该子文件下的index.html
     *      2.
     * @param file
     */
    private static void creatIndexHtml(Directory file,String indexFileName){
        StringBuilder result = new StringBuilder();
        result.append("<h1>"+file.getFileName()+"</h1>");   // 以文件名命名标题
        result.append("<ul>");
        List<Directory> chidren = file.getChildren();
        for(Directory child : chidren){
            result.append("<li>");
            if(child.getIsDirectory()){
                Path path = FileSystems.getDefault().getPath(child.getDirectory(),indexFileName);
                String url = "file://" + path.toString().replace("\\","/");
                result.append(String.format(urlDom,url,child.getFileName()));
            }
            else {
                String url = "file://" + child.getDirectory().replace("\\","/");
                result.append(String.format(urlDom,url,child.getFileName()));
            }
            result.append("</li>");
        }

        result.append("</ul>");

        String html = String.format(header,result.toString());
        String filePath = FileSystems.getDefault().getPath(file.getDirectory(),indexFileName).toString();
        createHtmlFile(filePath,html);
    }

    /**
     * 希望只有 level1 需要命名，其他的直接调用无命名的方法
     * @param file
     */
    private static void creatIndexHtml(Directory file){
        creatIndexHtml(file,"index.html");
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



