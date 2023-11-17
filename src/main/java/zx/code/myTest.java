package zx.code;

import org.jruby.RubyProcess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: loubaole
 * @date: 2023/11/16 10:23
 * @@Description:
 */
public class myTest {
    public static void main(String[] args) {
        String inputString = " .Fl s";
        String tt = "[^.][F][l]\\s+";
//        System.out.println(inputString.contains(tt));
//        String[] result = extractNameAndNum(inputString);
        extract(inputString);



    }


    public static void extract(String inputString){
        inputString = inputString.trim();
        // 匹配双引号中的内容
        String pattern = "[^\\w+][\\\\.][F][l]\\s+";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(inputString);
        while (matcher.find()){
            System.out.println(
                    inputString.replace(matcher.group(0),"-")
            );
        }
    }



    public static String[] extractNameAndNum(String inputString) {
        inputString = inputString.trim();
        // 匹配双引号中的内容
        String pattern = "([\"][^\"]+[\"])";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(inputString);
        int start = 0;
        int end = 0;
        String before = "";
        String after = "";
        System.out.println(inputString);
        // 处理匹配到的内容和剩余部分
        while (matcher.find()) {
            start = matcher.start();
            before = inputString.substring(end,start);
            end = matcher.end();
            after = inputString.substring(end,inputString.length());
            System.out.println("================================");
            System.out.println(matcher.group(0));
            System.out.println(before);

            System.out.println(after);

        }

        if(!after.equals("")){
            // 找到最后部分的单词部分
            Pattern pattern1 = Pattern.compile("(\\w+)");
            Matcher matcher1 = pattern1.matcher(after);
            while (matcher1.find()){
                after = after.replace(matcher1.group(0),"<u>"+matcher1.group(0)+"</u>");

            }
        }
        System.out.println(after);

        return new String[]{""};

    }
}


