package zx.code.utils;

import jnr.ffi.annotations.In;
import org.jruby.RubyProcess;

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
        INLINE_TAGS.put(".SM", "<span style=\"font-size: 9pt;\"></span>");
        INLINE_TAGS.put(".RB", "<b>%s</b>");
        INLINE_TAGS.put(".PD", "<!--%s-->");
        INLINE_TAGS.put(".\"", "<!--%s-->");
        INLINE_TAGS.put(".B", "<b>%s</b>");
        INLINE_TAGS.put("\\.B", "<b>%s</b>");
        INLINE_TAGS.put(".BR", "<b>%s</b>");
        INLINE_TAGS.put(".I", "<i>%s</i>");
        INLINE_TAGS.put(".IR", "<i>%s</i>");
        INLINE_TAGS.put(".PP", "<br />");
        INLINE_TAGS.put(".P", "<br />");
        INLINE_TAGS.put(".TP", "<br />");
        INLINE_TAGS.put(".br", "<br />");
        INLINE_TAGS.put(".IX ", "</div><div style=\"padding-left: 4em;\">");
        INLINE_TAGS.put("\\&", "<span style=\"margin-right: 1em\">%s</span>");
        INLINE_TAGS.put("\\fR","");
        INLINE_TAGS.put(".ft","");      // 不知道是什么
        INLINE_TAGS.put(".ne","");      // 不知道是什么
        INLINE_TAGS.put("tr","");
        INLINE_TAGS.put("'br","");
        INLINE_TAGS.put(".rr","");
        INLINE_TAGS.put(".\\}","");
        INLINE_TAGS.put(".rm","");
        INLINE_TAGS.put(".Nd","- %s");  // col.1 中替换为 -
    }

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
        INLINE_FUNCTIONS.put(".IP", this::_startParagraph);
        INLINE_FUNCTIONS.put(".SH", this::_startHeader);
        INLINE_FUNCTIONS.put(".Sh", this::_startHeader);
        INLINE_FUNCTIONS.put(".RS", this::_padRight);
        INLINE_FUNCTIONS.put(".RE", this::_padLeft);
        INLINE_FUNCTIONS.put(".SS", this::_startSubheader);
        INLINE_FUNCTIONS.put(".Vb", this::_startPre);
        INLINE_FUNCTIONS.put(".Ve", this::_finishPre);
        INLINE_FUNCTIONS.put(".TH", this::_mainTitle);      // 处理主标题，应该不会和 .Dt 同时存在
        INLINE_FUNCTIONS.put(".Dt", this::_startTitle);     // 处理文档标题
        INLINE_FUNCTIONS.put(".Dd",this::_recorderDate);    // 添加处理 .Dd，记录时间信息
        INLINE_FUNCTIONS.put(".Xr",this::_generateLinkFormat); // 需要生成链接的格式，link(2)，后面会处理
        INLINE_FUNCTIONS.put(".Op",this::_optionConvert);      // 是用于标记可选参数（Optional Parameter）的命令
        INLINE_FUNCTIONS.put(".Nm",this::_getNm);   // 获取文本的元数据，名称

    }



    static int DEFAULT_FONT_SIZE = 10;

    private Boolean pre_open = false;
    private Boolean level1_open = false;
    private Boolean level2_open = false;
    private Boolean level3_open = false;
    private Boolean list_open = false;
    private Info info = new Info();
    private Boolean recording = false;
    private List<String> closing_tags = new ArrayList<>();
    private Integer current_font_size = DEFAULT_FONT_SIZE;
    private Integer header_id = 0;
    private List<Heading> headers = new ArrayList<>();
    private String page_title = "";
    ////////////////////////////////////////////////////////////////

    private static final String FONT_CHANGING_OPEN = "</span><span style=\"font-size:%spt;\">";

    private static final Pattern LOCAL_REF_RE1 = Pattern.compile("(?<=<i>)([A-Za-z0-9-]*?)</i>\\((\\d+)\\)");
    private static final Pattern LOCAL_REF_RE2 = Pattern.compile("(?<=<b>)([A-Za-z0-9-]*?)</b>\\((\\d+)\\)");
    private static final Pattern LOCAL_REF_RE3 = Pattern.compile("([A-Za-z0-9-]*?)\\((\\d+)\\)");

    private static final Pattern GLOBAL_REF_RE = Pattern.compile("(https?://|ftp://|file:///)([A-Z0-9\\-~]+\\.?/?)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAILTO_REF_RE = Pattern.compile("([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})", Pattern.CASE_INSENSITIVE);

    private static final Pattern[] FONT_SIZE_CHANGING_TAGS = {Pattern.compile("\\\\s(\\+|\\-)?(\\d+)")};
    private static final Pattern[] FONT_CHANGING_TAGS = {

    };

    // 抽取 link(2) 的表达式
    private static final Pattern EXTRACT_INSTRUCT = Pattern.compile("(\\w*)\\s*\\((\\d+\\w*)\\)");

    // 抽取操作的参数识别
    private static final Pattern[] OPTIONS_PARAMETER_PATTERNS = {
            Pattern.compile("(\\w+\\s+[A][r]\\s+\\w+\\s+[A][r].+)"),
            Pattern.compile("(\\w+\\s+[A][r]\\s+\\w+)"),
            Pattern.compile("(\\w+)")
    };



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
            result = data.split(" ", 2)[1];
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
            result = data.split(" ")[1];
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
        data = data.replace("\"","");
        String[] dataItems = data.trim().split("\\s+", 4);

        try{
            info.setName(dataItems[0]);
            info.setNum(dataItems[1]);
            info.setDate(dataItems[2]);
            info.setVer(dataItems[4]);
        }
        catch (Exception e){
            System.out.println("拿到的 Title 信息不足");
        }

        String header1 = String.format("%s (%s)", info.getName(), info.getNum());
        String header2 = "General Commands Manual";
        page_title = String.format("<div><h1 class=\"left-block\">%s</h1>" +
                "<h1 class=\"center-block\">%s</h1>" +
                "<h1 class=\"right-block\">%s</h1></div>", header1, header2, header1);
        return page_title;
    }

    /**
     * 记录 title 的信息
     * @param data
     * @return {@link String}
     */
    private String _startTitle(String data) {
        recording = true;
        data = data.trim();
        String[] titleInfos = data.split(" ");
        if (titleInfos.length==0){
            return data;
        }
        if(titleInfos.length==1){
            info.setName(titleInfos[0]);
        }
        else if(titleInfos.length==2){
            info.setName(titleInfos[0]);
            info.setNum(titleInfos[1]);
        }
        else if(titleInfos.length==3 || titleInfos.length==4){
            info.setName(titleInfos[0]);
            info.setNum(titleInfos[1]);
            info.setDate(titleInfos[2]);
        }
        String header1 = String.format("%s (%s)", info.getName(), info.getNum());
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
        return info.getName().toLowerCase();
    }


    /////////////////////////////////////////////////////////////////////
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
                info.getVer(), info.getDate(), String.format("%s (%s)", info.getName(), info.getNum()));

    }

    /**
     * 替换全局文本中预设的超链接
     * @param line
     * @return {@link String}
     */
    public String localRefSelection(String line) {
        line = LOCAL_REF_RE1.matcher(line).replaceAll("<a href=\"$1#$2\">$1</a></i>($2)");
        line = LOCAL_REF_RE2.matcher(line).replaceAll("<a href=\"$1#$2\">$1</a></b>($2)");
        return line;
        // 重新检测了 .BR实现识别 instruct (num)
//        return LOCAL_REF_RE3.matcher(line).replaceAll("<a href=\"$1#$2\">$1</a>($2)");

    }


    /**
     * 将文本中本身的链接设为超链接
     * @param line
     * @return {@link String}
     */
    public String globalRefSelection(String line) {
        line = GLOBAL_REF_RE.matcher(line).replaceAll("<a href=\"$0\">$0</a>");
        return MAILTO_REF_RE.matcher(line).replaceAll("<a href=\"mailto:$0\">$0</a>");
    }

    public void updateFont(Matcher positions) {
        if ("0".equals(positions.group(2))) {
            current_font_size = DEFAULT_FONT_SIZE;
        } else {
            current_font_size += Integer.parseInt(positions.group(1) + positions.group(2));
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
    private String apply_part_tags_once(String line){

        int min_pos = line.length();
        String current_tag = "";
        String result = "";
        // 找到最靠前的 tag
        for(Map.Entry<String,String> entry : TEXT_PART_TAGS.entrySet()){
            if(line.contains(entry.getKey())){
                if(line.indexOf(entry.getKey())<min_pos){
                    min_pos = line.indexOf(entry.getKey());
                    current_tag = entry.getKey();
                }
            }
        }

        //
        for(String tag : CLOSING_ALL_TAG_VARIANTS){
            if(line.contains(tag) && line.indexOf(tag)<min_pos){
                while (closing_tags.size()>0){
                    result += closing_tags.get(-1);
                    closing_tags.remove(-1);
                }
                return line.replaceFirst(tag,result);
            }
        }

        for(String CLOSING_TAG : CLOSING_TAG_VARIANTS){

            if(line.contains(CLOSING_TAG) && line.indexOf(CLOSING_TAG)<min_pos){
                if(closing_tags.size()>0){
                    result = closing_tags.get(-1);
                    closing_tags.remove(-1);
                }
                else {
                    result = "</.....>";
                }
                return line.replaceFirst(CLOSING_TAG,result);
            }
        }
        if(!current_tag.equals("")){
            return line;
        }

        if(closing_tags.size()>0
                && closing_tags.get(-1).equals(CLOSING_TAGS.get(current_tag))
                && closing_tags.get(-1).equals("</i>")
        ){
            return line.replaceFirst(current_tag,"");
        }
        closing_tags.add(CLOSING_TAGS.get(current_tag));
        return line.replaceFirst(current_tag,TEXT_PART_TAGS.get(current_tag));
    }
    public String applyPartTagsOnce(String line) {
        int minPos = line.length();
        String currentTag = "";

        for (Map.Entry<String, String> entry : TEXT_PART_TAGS.entrySet()) {
            String tag = entry.getKey();
            if (line.contains(tag) && line.indexOf(tag) < minPos) {
                minPos = line.indexOf(tag);
                currentTag = tag;
            }
        }

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
                String result = closing_tags.isEmpty() ? "</.....>" : closing_tags.remove(closing_tags.size() - 1);
                return line.replace(closingTag, result);
            }
        }

        if (currentTag.isEmpty()) {
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
        line = line.trim();
        line = apply_not_closing_tags(line);
        line = apply_part_tags(line);
        line = changeFontSize(line);

        for (Map.Entry<String, Function<String, String>> entry : INLINE_FUNCTIONS.entrySet()) {
            String tag = entry.getKey();
            Function<String, String> func = entry.getValue();
            try {
                if (line.startsWith(tag)) {
                    line = func.apply(line.substring(tag.length()));
                }
            }
            catch(Exception e) {
                System.out.println(e);
            }


        }
        for (Map.Entry<String, String> entry : INLINE_TAGS.entrySet()) {
            String tag = entry.getKey();
            String result = entry.getValue();


            if (line.startsWith(tag)) {
                line = String.format(result, line.substring(tag.length()).trim());
            }
        }
        line = compileInstruction(line);

        if (!pre_open) {
            line = localRefSelection(line);
//            line = globalRefSelection(line);
        }
        return line;
    }
    private String convertGroffToHtml(String line) {

        StringBuilder htmlBuilder = new StringBuilder();
        // 检查是否有标识
        for (Map.Entry<String, String> entry : INLINE_TAGS.entrySet()) {
            String groffTag = entry.getKey();
            if (line.startsWith(groffTag)) {
                // 提取标识后的文本
                String textAfterTag = line.substring(groffTag.length()).trim();

                // 应用内联标记
                textAfterTag = applyInlineTags(textAfterTag);

                // 构建相应的 HTML 标签
                String htmlTag = entry.getValue();
                String htmlLine = String.format("<%s>%s</%s>", htmlTag, textAfterTag, htmlTag);

                // 添加到 HTML 结果中
                htmlBuilder.append(htmlLine);
                break; // 处理完当前行就跳出内循环
            }
        }

        return htmlBuilder.toString();
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
        name = instruction.getName();
        num = instruction.getNum();

        Path filePath = FileSystems.getDefault().getPath(parentUrl,"man"+num);
        filePath = FileSystems.getDefault().getPath(filePath.toString(),name+"."+num+"."+"html");

        return "file:///"+filePath.toString().replace("\\","/"); // 读文件得是 /

    }

    private String compileInstruction(String line){
//        line = line.substring(".BR".length()).trim();
        Matcher matcher = EXTRACT_INSTRUCT.matcher(line);
        StringBuffer  result = new StringBuffer();

        if (matcher.find()){
            String link = "<a href = \"%s\">%s</a>";
            Info instruct = new Info();
            instruct.setName(matcher.group(1));
            instruct.setNum(matcher.group(2));
            link = String.format(link,generateUrl(instruct),instruct.getName()+"("+instruct.getNum()+")");
            matcher.appendReplacement(result,link);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}

class Info {
    private String name;
    private String num;
    private String date;
    private String ver;

    public Info(String name, String num, String date, String ver) {
        this.name = name;
        this.num = num;
        this.date = date;
        this.ver = ver;
    }

    public Info(){

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getName() {
        return name;
    }

    public String getNum() {
        return num;
    }

    public String getDate() {
        return date;
    }

    public String getVer() {
        return ver;
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

class Instruct{

    String name;
    String level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
