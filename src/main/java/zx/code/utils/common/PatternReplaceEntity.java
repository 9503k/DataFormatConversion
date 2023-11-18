package zx.code.utils.common;

import java.util.regex.Pattern;

/**
 * 记录表达式与替换之间的关系
 *
 * @author LBL
 * @date 2023/11/17
 */
public class PatternReplaceEntity {

    /**
     * 表达式
     */
    private Pattern pattern;
    /**
     * 替换表达式的字符串
     */
    private String replaceString;

    public PatternReplaceEntity(Pattern pattern, String replaceString) {
        this.pattern = pattern;
        this.replaceString = replaceString;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplaceString() {
        return replaceString;
    }
}
