package zx.code.utils.html;

import zx.code.Main;
import zx.code.utils.common.EnumBlockListStyle;
import zx.code.utils.common.PatternReplaceEntity;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author: loubaole
 * @date: 2023/11/15 20:24
 * @@Description:
 */

public class GroffToHtmlConverter {

    private EnumBlockListStyle listStyle;

    private static final String HEADER =             "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\" />\n" +
            "    <style>\n" +
            "      * {\n" +
            "        font-size: 10pt;\n" +
            "      }\n" +
            "      .left-block {\n" +
            "        width: 33%;\n" +
            "        float: left;\n" +
            "        text-align: left;\n" +
            "      }\n" +
            "      .center-block {\n" +
            "        width: 33%;\n" +
            "        float: left;\n" +
            "        text-align: center;\n" +
            "      }\n" +
            "      .right-block {\n" +
            "        width: 33%;\n" +
            "        float: left;\n" +
            "        text-align: right;\n" +
            "      }\n" +
            "      i {\n" +
            "        color: #f99;\n" +
            "      }\n" +
            "      ul {\n" +
            "        list-style-type: none;\n" +
            "      }\n" +

            "    \n</style>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <span>\n";

    private static final String FOOTER =
            "            </span>\n" +
                    "          </body>\n" +
                    "        </html>\n";


    private static final TreeMap<String,String> DELETE_START_WITH_TAGS = new TreeMap<>(Collections.reverseOrder());
    static {
        DELETE_START_WITH_TAGS.put(".ft","");
        DELETE_START_WITH_TAGS.put(".ne","");
        DELETE_START_WITH_TAGS.put("tr","");
        DELETE_START_WITH_TAGS.put("'br","");
        DELETE_START_WITH_TAGS.put(".rr","");
        DELETE_START_WITH_TAGS.put(".\\}","");
        DELETE_START_WITH_TAGS.put(".rm","");
        DELETE_START_WITH_TAGS.put(".Nm","%s");   // 不知道是个什么
        DELETE_START_WITH_TAGS.put(".Nd","-%s");  // col.1 中替换为 -
    }


    private static final TreeMap<String,String> DELETE_CONTAINS_TAGS = new TreeMap<>(Collections.reverseOrder());
    static {
        DELETE_CONTAINS_TAGS.put(".","");
    }


    private static final Pattern[] DELETE_TAGS_PATTERN = {
            Pattern.compile("(\\.\\s+rr)"),         // 识别 .    rr
            Pattern.compile("(\\.\\s+\\\\\")"),     // 识别 .    /"
            Pattern.compile(""),
    };








    private static final TreeMap<String, String> INLINE_TAGS = new TreeMap<>(Collections.reverseOrder());;

    static {
        INLINE_TAGS.put(".de", "");
        INLINE_TAGS.put(".ds", "");
        INLINE_TAGS.put(".    ds","");
        INLINE_TAGS.put(".nr", "");
        INLINE_TAGS.put(".}f", "");
        INLINE_TAGS.put(".ll", "");
        INLINE_TAGS.put(".in", "");
        INLINE_TAGS.put(".ti", "");
        INLINE_TAGS.put(".el", "");
        INLINE_TAGS.put(".ie", "");
        INLINE_TAGS.put("..", "");
        INLINE_TAGS.put(".if", "");
        INLINE_TAGS.put(".nh", "");
        INLINE_TAGS.put(".zY", "");
        INLINE_TAGS.put(".LP", "<br />");
        INLINE_TAGS.put(".IB", "<b><i>%s</i></b>");
        INLINE_TAGS.put(".FN", "<i>%s</i>");
        INLINE_TAGS.put(".SM", "<span style=\"font-size: 9pt;\">%s</span>");
        INLINE_TAGS.put(".RB", "<b>%s</b>");
        INLINE_TAGS.put(".PD", "<!--%s-->");
        INLINE_TAGS.put(".\"", "<!--%s-->");
        INLINE_TAGS.put(".B", "<b>%s</b>");
        INLINE_TAGS.put("\\.B", "<b>%s</b>");
//        INLINE_TAGS.put(".BR", "<b>%s</b>");
        INLINE_TAGS.put(".I", "<u>%s</u>");     // 下划线
//        INLINE_TAGS.put(".IR", "<u>%s</u>");    // 下划线
        INLINE_TAGS.put(".PP", "<p /><p>");
        INLINE_TAGS.put(".Pp", "<p /><p>");
        INLINE_TAGS.put(".P", "<p /><p>");
        INLINE_TAGS.put(".TP", "<br/>");
        INLINE_TAGS.put(".br", "<br />");
        INLINE_TAGS.put(".IX ", "</div><div style=\"padding-left: 4em;\">");
        INLINE_TAGS.put("\\&", "<span style=\"margin-right: 1em\">%s</span>");
        INLINE_TAGS.put("\\fR","");
        INLINE_TAGS.put(".ft","");      // 不知道是什么
        INLINE_TAGS.put(".ne","");      // 不知道是什么
        INLINE_TAGS.put(".St","%s");    // 不知道是什么
        INLINE_TAGS.put("tr","");
        INLINE_TAGS.put(".tr","");      // 替换文本字符 麻烦！！！
        INLINE_TAGS.put(".ds","");     // 定义字符串 麻烦！！！
        INLINE_TAGS.put(".if","");     // 条件判断，也麻烦!!!
        INLINE_TAGS.put("'br","");
        INLINE_TAGS.put(". ","");      // .加空格，不知道是个啥，直接删
        INLINE_TAGS.put(".rr","");
        INLINE_TAGS.put(".\\}","");
        INLINE_TAGS.put("\\{","");
        INLINE_TAGS.put("\\}","");
        INLINE_TAGS.put(".rm","");
        INLINE_TAGS.put(".Nd","- %s");  // col.1 中替换为 -
        INLINE_TAGS.put(".BI","<b><i>%s</i></b>");  // .BI 命令（用于生成同时具有斜体和粗体的文本）


    }

    /**
     * 文本内需要替换的内容
     */

    private static final TreeMap<String, PatternReplaceEntity> TEXT_REPLACE_TAG = new TreeMap<>(Collections.reverseOrder());
    static
    {

        TEXT_REPLACE_TAG.put(".Fl ",new PatternReplaceEntity(Pattern.compile("[\\\\.][F][l]\\s+"),"-"));
        TEXT_REPLACE_TAG.put("Fl ",new PatternReplaceEntity(Pattern.compile("\\bFl\\s+"),"-"));
        TEXT_REPLACE_TAG.put("Ar ",new PatternReplaceEntity(Pattern.compile("\\bAr\\s+"),"-"));

    };
    private static final TreeMap<String, String> TEXT_PART_TAGS = new TreeMap<>(Collections.reverseOrder());

    static {
        TEXT_PART_TAGS.put("\\fB", "<b>");
        TEXT_PART_TAGS.put("\\f(BI", "<span class=BI\">");
        TEXT_PART_TAGS.put("\\f(IB", "<span class=IB\">");
        TEXT_PART_TAGS.put("\\f(CW", "<span class=\"CW\">");
        TEXT_PART_TAGS.put("\\f(CI", "<span class=\"CI\">");
        TEXT_PART_TAGS.put("\\f(CB", "<span class=\"CB\">");
        TEXT_PART_TAGS.put("\\fI", "<i>");

    }

    private static final TreeMap<String, String> CLOSING_TAGS = new TreeMap<>(Collections.reverseOrder());

    static {
        CLOSING_TAGS.put("\\fB", "</b>");
        CLOSING_TAGS.put("\\f(BI", "</span>");
        CLOSING_TAGS.put("\\f(IB", "</span>");
        CLOSING_TAGS.put("\\f(CW", "</span>");
        CLOSING_TAGS.put("\\f(CI", "</span>");
        CLOSING_TAGS.put("\\f(CB", "</span>");
        CLOSING_TAGS.put("\\fI", "</i>");
    }

    private static final String[] CLOSING_TAG_VARIANTS = {"\\fR"};
    private static final String[] CLOSING_ALL_TAG_VARIANTS = {"\\fP"};

    private static final TreeMap<String, String> NOT_CLOSING_PART_TAGS = new TreeMap<>(Collections.reverseOrder());

    static {
        NOT_CLOSING_PART_TAGS.put("\\(dq", "\"");
        NOT_CLOSING_PART_TAGS.put("\\(bv", "|");
        NOT_CLOSING_PART_TAGS.put(".zZ", "");
        NOT_CLOSING_PART_TAGS.put("\\\\$1", "");
        NOT_CLOSING_PART_TAGS.put("\\*(L\"", "\"");
        NOT_CLOSING_PART_TAGS.put("\\*(R\"", "\"");
        NOT_CLOSING_PART_TAGS.put(".nf", "<p>");
        NOT_CLOSING_PART_TAGS.put(".fi", "</p>");
        NOT_CLOSING_PART_TAGS.put("\\(co", "©");
        NOT_CLOSING_PART_TAGS.put(".Os", "");
        NOT_CLOSING_PART_TAGS.put("\\|", "");
        NOT_CLOSING_PART_TAGS.put("\\`", "`");
        NOT_CLOSING_PART_TAGS.put("(\"", "\"");
        NOT_CLOSING_PART_TAGS.put("\\-", "-");
        NOT_CLOSING_PART_TAGS.put(".Sp", "<br />");
        NOT_CLOSING_PART_TAGS.put("C`", "\"");
        NOT_CLOSING_PART_TAGS.put("C'", "\"");
        NOT_CLOSING_PART_TAGS.put("\\*\\(", "");
        NOT_CLOSING_PART_TAGS.put("\\|_", "_");
        // NOT_CLOSING_PART_TAGS.put("\\(bu", "<b>.</b>");
        NOT_CLOSING_PART_TAGS.put("\\fB\\f(BI", "<span class=\"BI\">");
        NOT_CLOSING_PART_TAGS.put("\\fB\\f(CB", "<span class=\"CB\">");
        NOT_CLOSING_PART_TAGS.put("\\fB\\fR", "</span>");
        NOT_CLOSING_PART_TAGS.put("\\e", "\\");
        NOT_CLOSING_PART_TAGS.put("\\(aq", "'");
        NOT_CLOSING_PART_TAGS.put("\\(bu", "\u2022");
    }

    private TreeMap<String,Function<String,String>> INLINE_FUNCTIONS = new TreeMap<>(Collections.reverseOrder());

    private String parentUrl = "";
    public GroffToHtmlConverter(String parentPath){
        this.parentUrl = parentPath;

        // 需要记录状态或者读取参数的，都要进行函数的处理

        INLINE_FUNCTIONS.put(".IP", this::_startParagraph);
        INLINE_FUNCTIONS.put(".SH", this::_startHeader);
        INLINE_FUNCTIONS.put(".Sh", this::_startHeader);
        INLINE_FUNCTIONS.put(".RS", this::_padRight);   // 开始一个相对缩进块，其后的文本会受到相对缩进的影响，整体右移。
        INLINE_FUNCTIONS.put(".RE", this::_padLeft);    // 结束缩进，和.RS一起使用
//        INLINE_FUNCTIONS.put(".TP",this::)
        INLINE_FUNCTIONS.put(".SS", this::_startSubheader);
        INLINE_FUNCTIONS.put(".Vb", this::_startPre);
        INLINE_FUNCTIONS.put(".Ve", this::_finishPre);
        INLINE_FUNCTIONS.put(".TH", this::_mainTitle);      // 处理主标题，应该不会和 .Dt 同时存在
        INLINE_FUNCTIONS.put(".Dt", this::_startTitle);     // 处理文档标题
        INLINE_FUNCTIONS.put(".Dd",this::_recorderDate);    // 添加处理 .Dd，记录时间信息
        INLINE_FUNCTIONS.put(".Xr",this::_generateLinkFormat); // 需要生成链接的格式，link(2)，后面会处理
        INLINE_FUNCTIONS.put(".Op",this::_optionConvert);      // 是用于标记可选参数（Optional Parameter）的命令
        INLINE_FUNCTIONS.put(".Nm",this::_getNm);   // 获取文本的元数据，名称 title
        INLINE_FUNCTIONS.put(".BI",this::_analyticCrossFormat);  // 解析 .BI .BR 这种交叉
        INLINE_FUNCTIONS.put(".BR",this::_analyticCrossFormat); //
        INLINE_FUNCTIONS.put(".IR",this::_analyticCrossFormat);

        INLINE_FUNCTIONS.put(".ad",this::_changlineDistance);   // 调整行距
        INLINE_FUNCTIONS.put(".EX",this::_exampleBlock);        // .EX 是一个命令，用于表示一个例子块（Example Block）
        INLINE_FUNCTIONS.put(".EE",this::_exampleBlock);        // .EE 是一个命令，用于结束一个例子块（Example Block）
        INLINE_FUNCTIONS.put(".Ex",this::_exampleBlock);
        INLINE_FUNCTIONS.put(".El",this::_exampleBlock);
        INLINE_FUNCTIONS.put(".TS",this::_table);               // 处理表格，开始标志
        INLINE_FUNCTIONS.put(".TE",this::_table);               // 处理表格，结束标志
        INLINE_FUNCTIONS.put(".Bl",this::_blockList);            // 块列表，每行用 .IT，.El 可以标识 .Bl 的结束
        INLINE_FUNCTIONS.put(".It",this::_blockListrow);         // 每行数据
        INLINE_FUNCTIONS.put(".El",this::_blockListEnd);         // 表结束
        INLINE_FUNCTIONS.put(".TP",this::_tagTable);              // 带标签的表
        INLINE_FUNCTIONS.put(".Ev",this::_setEnv);              // 设置环境变量
        INLINE_FUNCTIONS.put(".Eg",this::_getEnv);              // 获取环境变量


    }



    static int DEFAULT_FONT_SIZE = 10;
    static double DEFAULT_LINE_DISTANCE = 0;

    private Boolean pre_open = false;
    private Boolean level1_open = false;
    private Boolean level2_open = false;
    private Boolean level3_open = false;
    private Boolean list_open = false;
    private Info info = new Info();
    private Boolean recording = false;
    private List<String> closing_tags = new ArrayList<>();
    private Integer current_font_size = DEFAULT_FONT_SIZE;
    private Double current_line_distance = DEFAULT_LINE_DISTANCE;
    private Integer header_id = 0;
    private List<Heading> headers = new ArrayList<>();
    private String page_title = "";
    ////////////////////////// 正则表达式 //////////////////////////////////////

    private static final String FONT_CHANGING_OPEN = "</span><span style=\"font-size:%spt;\">";
    private static final String LINE_DISTANCE_CHANGE = "</span><span style=\"line-height:%s;\">";

    private static final Pattern LOCAL_REF_RE1 = Pattern.compile("(?<=<i>)([A-Za-z0-9-]*?)</i>\\((\\d+)\\)");
    private static final Pattern LOCAL_REF_RE2 = Pattern.compile("(?<=<b>)([A-Za-z0-9-]*?)</b>\\((\\d+)\\)");
    private static final Pattern LOCAL_REF_RE3 = Pattern.compile("([A-Za-z0-9-]*?)\\((\\d+)\\)");

    private static final Pattern GLOBAL_REF_RE = Pattern.compile("(https?://|ftp://|file:///)([A-Z0-9\\-~]+\\.?/?)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAILTO_REF_RE = Pattern.compile("([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})", Pattern.CASE_INSENSITIVE);

    private static final Pattern[] FONT_SIZE_CHANGING_TAGS = {Pattern.compile("\\\\s(\\+|\\-)?(\\d+)")};
    private static final Pattern[] FONT_CHANGING_TAGS = {

    };

    // 抽取 link(2) 的表达式
    private static final Pattern EXTRACT_INSTRUCT = Pattern.compile("([A-Za-z0-9\\-_]+)\\s*\\((\\d+\\w*)\\)");
//    private static final Pattern EXTRACT_INSTRUCT = Pattern.compile("[>]*(\\w+[^\\s]+\\w*)\\s*\\((\\d+\\w*)\\)");
//    private static final Pattern EXTRACT_INSTRUCT_ = Pattern.compile(">\\s*(\\w+[^\\s]+\\w*)\\s*[<]/[\\.]*[>]\\s*\\((\\d+\\w*)\\)");
    // 抽取 <..> link <..> (2)
    // 考虑有单个 X(7) 所以使用 (\w*[^\s]+\w*)
    private static final Pattern EXTRACT_INSTRUCT_ = Pattern.compile("\\.*<[biu]>\\s*([A-Za-z0-9\\-_]+)\\s*<[^>]+[biu]>\\s*\\((\\d+\\w*)\\)");


    // 抽取 <..> link (2)
    private static final Pattern EXTRACT_INSTRUCT_1 = Pattern.compile("\\.*<[^>]>\\s*([A-Za-z0-9\\-_]+)\\s*<[^>]+>\\s*\\((\\d+\\w*)\\)");


    // 抽取 > link (2)
    private static final Pattern EXTRACT_INSTRUCT__ = Pattern.compile(">\\s*([A-Za-z0-9\\-_]+)\\s*\\((\\d+\\w*)\\)");

    // 抽取 > link <..> (2)
    private static final Pattern EXTRACT_INSTRUCT___ = Pattern.compile(">\\s*([A-Za-z0-9\\-_]+)\\s*<[^>]+>\\s*\\((\\d+\\w*)\\)");


    // 抽取 link <..> (2)
    private static final Pattern EXTRACT_INSTRUCT____ = Pattern.compile("\\s*([A-Za-z0-9\\-_]+)\\s*<[^>]+>\\s*\\((\\d+\\w*)\\)");




    // 抽取操作的参数识别 .Op
    private static final Pattern[] OPTIONS_PARAMETER_PATTERNS = {
            Pattern.compile("(\\w+\\s+[A][r]\\s+\\w+\\s+[A][r].+)"),
            Pattern.compile("(\\w+\\s+[A][r]\\s+\\w+)"),
            Pattern.compile("(\\w+)")
    };

    /**
     * 双引号识别
     */
    private static final Pattern DOUBLE_QUATATION_MAKRKS = Pattern.compile("([\"][^\"]+[\"])");





    /////////////////////////// 映射功能方法 //////////////////////////////////////////
    private String _startParagraph(String data) {
        String closing = level3_open ? "</div><br />" : "";
        level3_open = true;
        Pattern pattern = Pattern.compile("\"(.*)\" (\\d+)");
        Matcher matcher = pattern.matcher(data);
        String inlineStyle = "display:block;";
        String part1 = "", part2 = "";
        if (matcher.find()) {
            part1 = matcher.group(1);
            part2 = matcher.group(2);
            if ((part1.endsWith(".") && part1.substring(0, part1.length() - 1).matches("\\d+")) ||
                    part1.length() == 1) {
                inlineStyle = "display:inline;";
                part2 = "0";
            }
        }
        if(part2.equals("")){
            return String.format("%s<h4 style=\"%s\">%s</h4><div " +
                            "style=\"%s\">",
                    closing, inlineStyle, part1, inlineStyle);
        }
        return String.format("%s<h4 style=\"%s\">%s</h4><div " +
                        "style=\"padding-left: %sem;%s\">",
                closing, inlineStyle, part1, part2, inlineStyle);
    }

    private String _startSubheader(String data) {
        // 修改：遇到了 .SS 后面没带数据的问题

        String closing = level3_open ? "</div><br />" : "";
        closing += level2_open ? "</div>" : "";
        level3_open = false;
        level2_open = true;
        Pattern pattern = Pattern.compile("\"(.*)\"");
        Matcher matcher = pattern.matcher(data);
        String result = "";
        if (matcher.find() ) {
            result = matcher.group(1);
        }
        else if(data.length()>1) {
//            result = data.split(" ", 2)[1];
            result = data;
        }
        return String.format("%s<h3 id=\"%s\">%s</h2><div style=\"padding-left: 3em;\">",
                closing, getHeaderName(result, 2), result);
    }

    private String _startHeader(String data) {
        String closing = level3_open ? "</div><br />" : "";
        closing += level2_open ? "</div>" : "";
        closing += level1_open ? "</div>" : "";
        level3_open = false;
        level2_open = false;
        level1_open = true;
        Pattern pattern = Pattern.compile("\"(.*)\"");
        Matcher matcher = pattern.matcher(data);
        String result = "";
        if (matcher.find()) {
            result = matcher.group(1);
        } else if(data.length()>1){
//            result = data.split(" ")[1];
            result = data;
        }

        return String.format("%s<h2 id=\"%s\">%s</h2><div style=\"padding-left: 3em;\">",
                closing, getHeaderName(result, 1), result);
    }

    private String _padRight(String length) {
        try {
            int padLength = Integer.parseInt(length.trim());
            return String.format("<div style=\"padding-left: %sem;\">", padLength);
        } catch (NumberFormatException e) {
            return "<div style=\"padding-left: 1em;\">";
        }
    }

    private String _padLeft(String data) {
        return "</div>";
    }

    private String _startPre(String data) {
        pre_open = true;
        return "<pre>";
    }

    private String _finishPre(String data) {
        pre_open = false;
        return "</pre>";
    }

    private String _mainTitle(String data) {
        recording = true;
        String[] dataItems = data.trim().split("\\s+", 5);

        try{
            info.setTitle(dataItems[0].replace("\"",""));
            info.setSection(dataItems[1].replace("\"",""));
            info.setDate(dataItems[2].replace("\"",""));
            info.setSource(dataItems[3].replace("\"",""));
            info.setManual(dataItems[4].replace("\"",""));
        }
        catch (Exception e){
            System.out.println("拿到的 Title 信息不足");
        }

        String header1 = String.format("%s (%s)", info.getTitle(), info.getSection());
        String header2 = "General Commands Manual";
        page_title = String.format("<div><h1 class=\"left-block\">%s</h1>" +
                "<h1 class=\"center-block\">%s</h1>" +
                "<h1 class=\"right-block\">%s</h1></div>", header1, header2, header1);
        return "";
    }

    /**
     * 记录 title 的信息
     * .Dt 是一个用于定义整个文档标题和其他信息的命令
     * .Dt title section [date] [source] [manual]
     * @param data
     * @return {@link String}
     */
    private String _startTitle(String data) {
        recording = true;
        String[] dataItems = data.trim().split("\\s+", 5);

        try{
            info.setTitle(dataItems[0].replace("\"",""));
            info.setSection(dataItems[1].replace("\"",""));
            info.setDate(dataItems[2].replace("\"",""));
            info.setSource(dataItems[3].replace("\"",""));
            info.setManual(dataItems[4].replace("\"",""));
        }
        catch (Exception e){
            System.out.println("拿到的 Title 信息不足");
        }
        String header1 = String.format("%s (%s)", info.getTitle(), info.getSection());
        String header2 = "General Commands Manual";
        page_title = String.format("<div><h1 class=\"left-block\">%s</h1>" +
                "<h1 class=\"center-block\">%s</h1>" +
                "<h1 class=\"right-block\">%s</h1></div>", header1, header2, header1);
        return "";
    }


    /**
     * 记录时间信息
     * @param data
     * @return {@link String}
     */
    private String _recorderDate(String data){
        info.setDate(data.trim());
        return "";
    }

    /**
     * .Xr x1 x2 用于引用一个名为 x1(x2) 的程序或命令
     * @param data
     * @return {@link String}
     */
    private String _generateLinkFormat(String data){

        String[] infos = data.trim().split(" ");
        if(infos.length>=2){
            return infos[0]+"("+infos[1]+")";
        }
        return data;
    }


    /**
     *
     * 三种格式：
     * command
     * .Op Fl a
     * .Op Fl b Ar arg
     * .Op Fl c Ar arg1 Ar arg2
     * 从后往前遍历
     *
     * @param data
     * @return {@link String}
     */
    private String _optionConvert(String data){

        String paramString = data.trim().replace("Fl","").trim();


        for(Pattern pattern : OPTIONS_PARAMETER_PATTERNS){
            Matcher matcher = pattern.matcher(paramString);
            if(matcher.find()){
                paramString = paramString.replace("Ar"," ");
                String[] params = paramString.split("\\s+");

                if(params.length==1){
                    return "["+"-"+params[0]+"]";
                }
                if(params.length==2){
                    return "["+"-"+params[0]+" "+params[1]+"]";
                }
                if (params.length>3){
                    StringBuilder result = new StringBuilder();
                    result.append("[-"+params[0]);
                    for(int i=1;i<params.length;i++){
                        result.append(" "+params[i]);
                    }
                    result.append("]");
                    return result.toString();
                }
            }

        }
        return data;
    }

    /**
     * @param data
     * @return {@link String}
     */
    private String _getNm(String data){
        return info.getTitle().toLowerCase();
    }


    /**
     * 分析交叉的格式，只有引号内的数据正常显示，不加引号的加下划线
     *
     * @param data
     * @return {@link String}
     */
    private String _analyticCrossFormat(String data){
        int length = data.trim().trim().split("\\s+").length;
        if(length==1 || length==0){
//            return "<i>"+data+"</i>";
            return data;
        }
        data = data.trim();
        Matcher matcher = DOUBLE_QUATATION_MAKRKS.matcher(data);
        StringBuilder result = new StringBuilder();
        int start = 0;
        int end = 0;
        String before = "";
        String after = data;
//        result.append("<i>");
        // 处理匹配到的内容和剩余部分
        while (matcher.find()) {
            start = matcher.start();
            before = data.substring(end,start);
            end = matcher.end();
            after = data.substring(end,data.length());
            if(!before.equals("")){
                // 找到前面部分的单词部分
                Pattern pattern = Pattern.compile("([^\\s]+[(]*\\.*[)]*)");
                Matcher matcher1 = pattern.matcher(before);
                while (matcher1.find()){
                    before = before.replace(matcher1.group(0),"<i><u>"+matcher1.group(0)+"</u></i>");
                }
                result.append(before);
            }
            result.append(matcher.group(0).split("\"")[1]);
        }
        if(!after.equals("")){
            // 找到最后部分的单词部分
            Pattern pattern = Pattern.compile("([^\\s]+[(]*\\.*[)]*)");
            Matcher matcher1 = pattern.matcher(after);
            while (matcher1.find()){
                after = after.replace(matcher1.group(0),"<i><u>"+matcher1.group(0)+"</u></i>");
            }
        }
        result.append(after);
//        result.append("</i>");
        return result.toString();
    }


    private String _analyticCrossFormat2(String data){
        int length = data.trim().trim().split("\\s+").length;
        if(length==1 || length==0){
            return "<b>"+data+"</b>";
        }
        data = data.trim();
        Matcher matcher = DOUBLE_QUATATION_MAKRKS.matcher(data);
        StringBuilder result = new StringBuilder();
        int start = 0;
        int end = 0;
        String before = "";
        String after = data;
        result.append("<b>");
        // 处理匹配到的内容和剩余部分
        while (matcher.find()) {
            start = matcher.start();
            before = data.substring(end,start);
            end = matcher.end();
            after = data.substring(end,data.length());
            if(!before.equals("")){
                // 找到前面部分的单词部分
                Pattern pattern = Pattern.compile("([^\\s]+[(]*\\.*[)]*)");
                Matcher matcher1 = pattern.matcher(before);
                while (matcher1.find()){
                    before = before.replace(matcher1.group(0),"<u>"+matcher1.group(0)+"</u>");
                }
                result.append(before);
            }
            result.append(matcher.group(0).split("\"")[1]);
        }
        if(!after.equals("")){
            // 找到最后部分的单词部分
            Pattern pattern = Pattern.compile("([^\\s]+[(]*\\.*[)]*)");
            Matcher matcher1 = pattern.matcher(after);
            while (matcher1.find()){
                after = after.replace(matcher1.group(0),"<u>"+matcher1.group(0)+"</u>");
            }
        }
        result.append(after);
        result.append("</b>");
        return result.toString();
    }

    private String _changlineDistance(String lineDistance){

        lineDistance  = lineDistance.trim();
        if(lineDistance.contains("\\d*")){
            current_line_distance = Double.parseDouble(lineDistance);
            return String.format(LINE_DISTANCE_CHANGE,current_line_distance);
        }
        else if(lineDistance.contains("\\w")){
            if(lineDistance.equals("l")){
                return "</span><span style=\"line-height:1.5;\">";
            }
            if(lineDistance.equals("r")){
                return "</span><span style=\"line-height:1;\">";
            }
            if(lineDistance.equals("i")){
                return "</span>";
            }else {
                return "</span>";
            }
        }
        else {
            return "</span>";
        }
    }


    /**
     * 标记一个文本块，通常用于显示源代码或其他文本的示例
     * .EX
     * This is an example block.
     * It can contain multiple lines of text.
     * .EE
     * @param data
     * @return {@link String}
     */
    private Boolean ExStart = true;
    private String _exampleBlock(String data){
        if(ExStart){
            ExStart = !ExStart;
            return "<pre>";
        }
        else {
            ExStart = !ExStart;
            return "</pre>";
        }
    }

    /**
     * 生成表
     * @param data
     * @return {@link String}
     */
    private String _table(String data){


        return "";
    }


    /**
     * 块列表（block list）块列表通常由 .It（item）命令组成，每个 .It 命令表示列表中的一个项目。
     * @param data
     * @return {@link String}
     */
    private String _blockList(String data){

        if(data.contains("bullet") || data.contains("itemize") || data.contains("ohang")){
            listStyle = EnumBlockListStyle.BULLE;
        }
        else if(data.contains("enum")){
            listStyle = EnumBlockListStyle.ENUM;
        }
        else if(data.contains("tag")){

            listStyle = EnumBlockListStyle.TAG;
        }
        else if(data.contains("column")){
            listStyle = EnumBlockListStyle.TABLE;
        }
        else {
            return "<br/>";
        }

        return listStyle.getListDom1().getStartTag();
    }

    private Boolean firstBlockListrow = true;
    private String _blockListrow(String data){

        if(listStyle == EnumBlockListStyle.TAG){
            if(firstBlockListrow){
                return listStyle.getListDom2().getStartTag()+ data + listStyle.getListDom3().getStartTag();
            }
            return listStyle.getListDom2().getEndTag()+
                    listStyle.getListDom2().getStartTag()+
                    data +
                    listStyle.getListDom3().getStartTag()
                    ;
        }
        else if(listStyle == EnumBlockListStyle.TABLE){
            return data;
        }
        else {
            if(firstBlockListrow){
                return listStyle.getListDom2().getStartTag()+ data ;
            }
            return listStyle.getListDom2().getEndTag()+
                    listStyle.getListDom2().getStartTag()+
                    data ;


        }
    }

    private String _blockListEnd(String data){

        if(listStyle == EnumBlockListStyle.TAG){
            // tag 类型需要单独处理
            return  listStyle.getListDom3().getEndTag() + listStyle.getListDom2().getEndTag()+listStyle.getListDom1().getEndTag();
        }
        else if (listStyle == EnumBlockListStyle.TABLE){
            return  listStyle.getListDom2().getEndTag()+listStyle.getListDom1().getEndTag();

        }
        else {
            return listStyle.getListDom2().getEndTag()+listStyle.getListDom1().getEndTag();

        }

    }

    /**
     * 实现 .TP 的带标签的列表
     * .TP 后面是有参数的，但是我直接舍弃参数了
     * @param data
     * @return {@link String}
     */
    private int tagTableResource  = 0;
    private String _tagTable(String data){
        listStyle = EnumBlockListStyle.TAG;
        tagTableResource = 1;
        return listStyle.getListDom1().getEndTag()+listStyle.getListDom1().getStartTag();
    }


    private String _setEnv(String data){
        info.getEnvironment().add(data.trim());
        return data;
    }

    /**
     * 暂时还没有用到
     * @param data
     * @return {@link String}
     */
    private String _getEnv(String data){
        StringBuilder result = new StringBuilder();
        return info.getEnvironment()+" "+data;
    }



    /////////////////////////////// 功能方法，用于在处理一行数据时使用 //////////////////////////////////////


    /**
     * 检查表的 tag 是否结束
     * 不同的表有不同的策略：
     *  .TP 是检测两行，第一行为 Tag，第二行为 data
     *  .Bl 是检测 .IT
     * @param data
     * @return {@link Boolean}
     */
    private String BuildtableText(String data){
        // 说明没有文本，跳过
        if (data.trim().length()<1 || tagTableResource==0){
            return data;
        }
        // 有文本，且是第一行
        if(tagTableResource==1){
            tagTableResource += 1;
            // 第二个标签
            data = listStyle.getListDom2().getStartTag()+data+listStyle.getListDom2().getEndTag();

        }else {
            // 第三个标签
            data = listStyle.getListDom3().getStartTag()+data+listStyle.getListDom3().getEndTag();
        }

        return data;
    }

    /**
     * 检查 TP是否结束
     * 第一行一定是 tag
     * data 截至有以下可能：
     *      1. 遇到了 .TP
     *      2.
     * @param line
     * @return {@link Boolean}
     */
    private Boolean CheckTPtagEnd(String line){

        return true;
    }


    private String replaceTags(String line){
        line = line.trim();
        for (Map.Entry<String,PatternReplaceEntity> entry : TEXT_REPLACE_TAG.entrySet()){

            Pattern pattern = entry.getValue().getPattern();
            String replaceString = entry.getValue().getReplaceString();
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()){
                line = line.replace(matcher.group(0),replaceString);
            }
        }
        return line;
    }



    public int getHeaderId() {
        return ++header_id;
    }

    public String getHeaderName(String title, int level) {
        String name = "header-" + getHeaderId();
        headers.add(new Heading(name, title, level));
        return name;
    }

    public String getContents() {
        StringBuilder result = new StringBuilder("<div class=\"contents\">");
        for (int i = 0; i < headers.size(); i++) {
            if (i == 0 || headers.get(i).getLevel() > headers.get(i - 1).getLevel()) {
                result.append("<ul>");
            }
            else if (headers.get(i).getLevel() < headers.get(i - 1).getLevel()) {
                result.append("</ul>");
            }
            result.append(String.format("<li><a href=\"#%s\">%s</a></li>",
                    headers.get(i).getName(), headers.get(i).getTitle()));
        }
        result.append("</ul></div>");
        return result.toString();
    }


    /**
     * 生成最后一行脚注
     * @return {@link String}
     */
    private String mainFooter() {
        return String.format("<div><h1 class=\"left-block\">%s</h1><h1 class=\"center-block\">%s</h1><h1 class=\"right-block\">%s</h1></div>",
                info.getSource(), info.getDate(), String.format("%s (%s)", info.getTitle(), info.getSection()));

    }

    /**
     * 替换全局文本中预设的超链接
     * @param line
     * @return {@link String}
     */
    public String localRefSelection(String line) {
        line = LOCAL_REF_RE1.matcher(line).replaceAll("<a href=\"$1#$2\">$1</a></i>($2)");
        line = LOCAL_REF_RE2.matcher(line).replaceAll("<a href=\"$1#$2\">$1</a></b>($2)");
//        return line;
        // 重新检测了 .BR实现识别 instruct (num)
        return LOCAL_REF_RE3.matcher(line).replaceAll("<a href=\"$1#$2\">$1</a>($2)");

    }


    /**
     * 将文本中本身的链接设为超链接
     * @param line
     * @return {@link String}
     */
    public String globalRefSelection(String line) {
//        line = GLOBAL_REF_RE.matcher(line).replaceAll("<a href=\"$0\">$0</a>");
        return MAILTO_REF_RE.matcher(line).replaceAll("<a href=\"mailto:$0\">$0</a>");
    }

    public void updateFont(Matcher positions) {
        if(positions==null){
            return;
        }
        if ("0".equals(positions.group(2))) {
            current_font_size = DEFAULT_FONT_SIZE;
        } else {
            current_font_size += Integer.parseInt((positions.group(1)==null?"":positions.group(1)) +
                    positions.group(2)==null?"0":positions.group(2));
        }
    }

    public String changeFontSize(String line) {
        int length = line.length();
        int count = 0;
        while (true) {
            // 防止跳不出去
            if(count>length){
                return line;
            }
            count++;

            String replaceData = "";
            int replacePosition = line.length();
            Matcher bestPositions = null;
            for (Pattern tag : FONT_SIZE_CHANGING_TAGS) {
                Matcher positions = tag.matcher(line);
                if (!positions.find()) {
                    continue;
                }
                String replaceGroup = positions.group(0);
                if (line.indexOf(replaceGroup) < replacePosition) {
                    bestPositions = positions;
                    replacePosition = line.indexOf(replaceGroup);
                    replaceData = replaceGroup;
                }
            }
            if (replaceData.isEmpty()) {
                return line;
            }
            updateFont(bestPositions);


            line = line.replaceFirst(replaceData, String.format(FONT_CHANGING_OPEN, current_font_size));
        }
    }

    ////////////////////////////////////////////////////////////////////////

    public String applyPartTagsOnce(String line) {
        int minPos = line.length();
        String currentTag = "";

        // 找文本中的 tag
        for (Map.Entry<String, String> entry : TEXT_PART_TAGS.entrySet()) {
            String tag = entry.getKey();
            if (line.contains(tag) && line.indexOf(tag) < minPos) {
                minPos = line.indexOf(tag);
                currentTag = tag;
            }
        }

        // 找文本中关闭的 tag
        for (String tag : CLOSING_ALL_TAG_VARIANTS) {
            if (line.contains(tag) && line.indexOf(tag) < minPos) {
                StringBuilder result = new StringBuilder();
                while (!closing_tags.isEmpty()) {
                    result.append(closing_tags.remove(closing_tags.size() - 1));
                }
                return line.replace(tag, result.toString());
            }
        }

        for (String closingTag : CLOSING_TAG_VARIANTS) {
            if (line.contains(closingTag) && line.indexOf(closingTag) < minPos) {
//                String result = closing_tags.isEmpty() ? "</.....>" : closing_tags.remove(closing_tags.size() - 1);
                String result = closing_tags.isEmpty() ? " " : closing_tags.remove(closing_tags.size() - 1);

                return line.replace(closingTag, result);
            }
        }

        if (currentTag.isEmpty() || currentTag.equals("")) {
            return line;
        }
        if (!closing_tags.isEmpty() && closing_tags.get(closing_tags.size() - 1).equals(CLOSING_TAGS.get(currentTag))
                && closing_tags.get(closing_tags.size() - 1).equals("</i>")) {
            return line.replace(currentTag, "");
        }


        closing_tags.add(CLOSING_TAGS.get(currentTag));
        return line.replace(currentTag, TEXT_PART_TAGS.get(currentTag));
    }

    private String applyInlineTags(String input) {
        for (Map.Entry<String, String> entry : INLINE_TAGS.entrySet()) {
            String patternString = Pattern.quote(entry.getKey());
            String replacement = entry.getValue();
            input = input.replaceAll(patternString, Matcher.quoteReplacement(replacement));
        }
        return input;
    }


    private String apply_not_closing_tags(String line){
        for (Map.Entry<String,String> entry : NOT_CLOSING_PART_TAGS.entrySet()){
            line = line.replace(entry.getKey(),entry.getValue());
        }
        return line;
    }

    private String apply_part_tags(String line){
        String newLine = null;
        while (true){
            newLine = applyPartTagsOnce(line);
            if(line.equals(newLine)){
                break;
            }
            line = newLine;
        }
        return line;
    }

    public String modifyLine(String line) {
        String source = new String(line);
        line = line.trim();

        line = reBuildInstruction(line); // 先把格式重构了，防止后面出问题
        line = apply_not_closing_tags(line);
        line = apply_part_tags(line);
        line = changeFontSize(line);

        for (Map.Entry<String, Function<String, String>> entry : INLINE_FUNCTIONS.entrySet()) {
            String tag = entry.getKey();
            Function<String, String> func = entry.getValue();
            if (line.startsWith(tag)) {
                line = func.apply(line.substring(tag.length()));
            }
        }

        for (Map.Entry<String, String> entry : INLINE_TAGS.entrySet()) {
            String tag = entry.getKey();
            String result = entry.getValue();

            if (line.startsWith(tag)) {
                line = String.format(result, line.substring(tag.length()).trim());
            }
        }
        // 编译超链接符串
        line = compileInstruction(line);
        // 替换所有要替换的数据
        line = replaceTags(line);
//        // 构建表格数据
//        line = BuildtableText(line);

        // 这种获取方式我不喜欢，不能自定义url，所以我使用了 compileInstruction生成
        if (!pre_open) {
//            line = localRefSelection(line);
            line = globalRefSelection(line);
        }
        // 判断是不是只有文本
        if(source.trim().equals(line)){
            line = source;
        }

        return line;
    }


    public String readCompressedGroffFile(String filePath) throws IOException {

        StringBuilder content = new StringBuilder();

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath));
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // 处理每一行文本
                line = line.replaceAll("<", "&lt;");
                // 将 > 替换为 &gt;
                line = line.replaceAll(">", "&gt;");
                // 注释不翻译
                if(line.startsWith(".\\\"")){
                    continue;
                }
                content.append(modifyLine(line)).append("\n");
            }
        }

        // content已经能实现想要的结果，但是没加 style
//        return content.toString();

        String beginning = HEADER + page_title + getContents() + content.toString();

        return beginning + "</div></span>" + mainFooter() + FOOTER;
    }


    private String generateUrl(Info instruction){
        String name = "";
        String num = "";
        name = instruction.getTitle();
        num = instruction.getSection();

        Path filePath = FileSystems.getDefault().getPath(parentUrl,"man"+num);
        filePath = FileSystems.getDefault().getPath(filePath.toString(),name+"."+num+"."+"html");

        return "file:///"+filePath.toString().replace("\\","/"); // 读文件得是 /

    }


    /**
     * 重构 lin () 格式为 link()
     * @param line
     * @return {@link String}
     */
    private String reBuildInstruction(String line){
        Pattern reBuilder = Pattern.compile("(\\w+\\s*\\([^()]*\\))");
        Matcher matcher = reBuilder.matcher(line);
        while (matcher.find()){
            line = line.replace(matcher.group(0),
                    matcher.group(0).replaceAll("\\s+","")
            );
        }
        return line;
    }
    private String compileInstruction(String line){


        Matcher matcher = EXTRACT_INSTRUCT.matcher(line);
        StringBuffer  result = new StringBuffer();
        String link = "<a href = \"%s\">%s</a>";
        if (matcher.find()){
            if(line.contains("4*a")){
                System.out.println();
            }
            Matcher matcher1 = EXTRACT_INSTRUCT_.matcher(line);
            Matcher matcher1_1 = EXTRACT_INSTRUCT_1.matcher(line);
            Matcher matcher2 = EXTRACT_INSTRUCT__.matcher(line);
            Matcher matcher3 = EXTRACT_INSTRUCT___.matcher(line);
            Matcher matcher4 = EXTRACT_INSTRUCT____.matcher(line);
            if(matcher1.find()){
                matcher1 = EXTRACT_INSTRUCT_.matcher(line);
                while (matcher1.find()){
                    // 使用 Info 作为页面标识
                    Info instruct = new Info();
                    instruct.setTitle(matcher1.group(1));
                    instruct.setSection(matcher1.group(2));
                    String newLink = String.format(link,generateUrl(instruct),matcher1.group(0));
                    line = line.replace(matcher1.group(0),newLink);
                }

                return line;
            }
            else if(matcher1_1.find()){
                Info instruct = new Info();
                instruct.setTitle(matcher1_1.group(1));
                instruct.setSection(matcher1_1.group(2));
                String newLink = String.format(link,generateUrl(instruct),matcher1_1.group(0));
                line = line.replace(matcher1_1.group(0),newLink);
                return line;
            }
            else if (matcher2.find()){
                Info instruct = new Info();
                instruct.setTitle(matcher2.group(1));
                instruct.setSection(matcher2.group(2));
                String newLink = String.format(link,generateUrl(instruct),matcher2.group(0));
                line = line.replace(matcher2.group(0),newLink);
                return line;
            }
            else if(matcher3.find()){
                Info instruct = new Info();
                instruct.setTitle(matcher3.group(1));
                instruct.setSection(matcher3.group(2));
                String newLink = String.format(link,generateUrl(instruct),matcher3.group(0));
                line = line.replace(matcher3.group(1),newLink);

                return line;
            }
            else if (matcher4.find()){
                Info instruct = new Info();
                instruct.setTitle(matcher4.group(1));
                instruct.setSection(matcher4.group(2));
                String newLink = String.format(link,generateUrl(instruct),matcher4.group(0));
                line = line.replace(matcher4.group(0),newLink);
                return line;
            }
            else {

                // 使用 Info 作为页面标识
                Info instruct = new Info();
//            instruct.setTitle(matcher.group(1).replaceAll("[<]\\w*[>]","").replaceAll("\\.*[>]",""));
                instruct.setTitle(matcher.group(1));
                instruct.setSection(matcher.group(2));
                String newLink = String.format(link,generateUrl(instruct),instruct.getTitle()+"("+instruct.getSection()+")");
                line = line.replace(matcher.group(0),newLink);
                return line;
            }
        }
        return line;
    }
}

class Info {
    /**
     * 页面标题
     */
    private String title = "";

    /**
     * 章节
     */
    private String section = "";
    /**
     * 创建或修改日期
     */
    private String date = "";
    /**
     * man 页面来源（程序或模块名称）
     */
    private String source = "";


    private List<String> environment = new ArrayList<>();

    /**
     * man页面所属的手册（manual）或文档集
     */
    private String manual = "";

    public Info(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getManual() {
        return manual;
    }

    public void setManual(String manual) {
        this.manual = manual;
    }

    public List<String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(List<String> environment) {
        this.environment = environment;
    }
}
class Heading {
    private String name;
    private String title;
    private int level;

    public Heading(String name, String title, int level) {
        this.name = name;
        this.title = title;
        this.level = level;
    }

    // Getter methods (you may generate them using your IDE)

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getLevel() {
        return level;
    }
}

