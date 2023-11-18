package zx.code.utils.common;

import jnr.posix.Linux;

/**
 * @author: loubaole
 * @date: 2023/11/18 22:38
 * @@Description:
 */
public enum EnumOS {

    WINDOWS("WINDOWS","\\\\"),
    LINUX("LINUX","/");

    EnumOS(String osName,String splitPathChar){
        this.osName = osName;
        this.splitPathChar = splitPathChar;
    }

    private final String osName;

    private final String splitPathChar;

    public String getOsName() {
        return osName;
    }

    public String getSplitPathChar() {
        return splitPathChar;
    }
}
