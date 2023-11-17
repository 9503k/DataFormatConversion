package zx.code.utils.directory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: loubaole
 * @date: 2023/11/16 11:55
 * @@Description:
 */
public class Directory {

    /**
     * 源地址
     */
    private String sourceDirectory;


    /**
     * 源文件名
     */
    private String sourceFileName;

    /**
     * 是否是目录
     */
    private Boolean IsDirectory;

    public void setDirectory(Boolean directory) {
        IsDirectory = directory;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    /**
     * 存储时父文件夹地址
     */
    private String parentDirectory;

    /**
     * 存储时文件名称
     */
    private String FileName;

    /**
     * 存储文件完整地址，parentDirectory/FileName
     */
    private String directory;

    /**
     * 存储文件数据，用于存储文件数据，html的字符串
     * 只有是文件时才会有值
     * 其实也可以省略，防止爆栈
     */
    private String FileData;

    /**
     * 文件夹下的子文件
     */
    private List<Directory> Children;


    public String getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public String getFileName() {
        return FileName;
    }

    public String getFileData(){
        return FileData;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getDirectory() {
        Path filePath = FileSystems.getDefault().getPath(parentDirectory,FileName);
        directory = filePath.toString();
        return directory;
    }


    public List<Directory> getChildren() {
        return Children;
    }

    public void setChildren(List<Directory> children) {
        Children = children;
    }

    public void setFileData(String fileData){
        FileData = fileData;
    }


    public Directory(){
        Children = new ArrayList<>();
    }

}
