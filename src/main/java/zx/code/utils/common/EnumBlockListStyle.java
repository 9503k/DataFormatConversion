package zx.code.utils.common;

/**
 * @author: loubaole
 * @date: 2023/11/17 18:04
 * @@Description: groff的 block-list转html的表
 */
public enum EnumBlockListStyle {
    /**
     * 无序列表
     */
    BULLE(new TagInfo("<ul>","</ul>"),new TagInfo("<li>","</li>")),

    /**
     * 有序列表
     */
    ENUM(new TagInfo("<ol>","</ol>"),new TagInfo("<li>","</li>")),

    /**
     * 带标签的列表
     */
    TAG(new TagInfo("<dl>","</dl>"),new TagInfo("<dt>","</dt>"),new TagInfo("<dd>","</dd>")),

    TABLE(new TagInfo("<table>","</table>"),new TagInfo("<tr>","</tr>"),new TagInfo("<td>","</td>"))


    ;


    EnumBlockListStyle(TagInfo listDom1,TagInfo listDom2){
        this.listDom1 = listDom1;
        this.listDom2 = listDom2;
        this.listDom3 = null;
    }
    EnumBlockListStyle(TagInfo listDom1,TagInfo listDom2,TagInfo listDom3){
        this.listDom1 = listDom1;
        this.listDom2 = listDom2;
        this.listDom3 = listDom3;
    }
    private final TagInfo listDom1;
    private final TagInfo listDom2;
    private final TagInfo listDom3;

    public TagInfo getListDom1() {
        return listDom1;
    }

    public TagInfo getListDom2() {
        return listDom2;
    }

    public TagInfo getListDom3() {
        return listDom3;
    }
}

