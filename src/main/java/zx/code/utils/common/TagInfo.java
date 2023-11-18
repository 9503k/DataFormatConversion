package zx.code.utils.common;

/**
 * @author: loubaole
 * @date: 2023/11/17 19:53
 * @@Description:
 */
public class TagInfo {
    String startTag;
    String endTag;

    TagInfo(String startTag, String endTag) {
        this.startTag = startTag;
        this.endTag = endTag;
    }

    public String getStartTag() {
        return startTag;
    }


    public String getEndTag() {
        return endTag;
    }

}
