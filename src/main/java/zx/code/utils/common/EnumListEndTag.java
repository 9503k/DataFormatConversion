package zx.code.utils.common;

/**
 * @author: loubaole
 * @date: 2023/11/17 19:59
 * @@Description:
 */
public enum EnumListEndTag {

    IT(".IT"),
    ;


    EnumListEndTag(String tag){
        this.tag = tag;
    };

    public String getTag() {
        return tag;
    }

    private final String tag;


}
