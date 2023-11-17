package zx.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: loubaole
 * @date: 2023/11/16 10:23
 * @@Description:
 */
public class myTest {
    public static void main(String[] args) {
        String inputString = "l Ar num Ar num";
        String[] result = extractNameAndNum(inputString);

        if (result != null) {
            String name = result[0];
            String num = result[1];
            System.out.println("Name: " + name);
            System.out.println("Num: " + num);
        } else {
            System.out.println("未匹配到指定格式");
        }
    }

    public static String[] extractNameAndNum(String inputString) {
        String regex = "(\\w+\\s+[A][r]\\s+\\w+\\s+[A][r].+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);

        if (matcher.find()) {
            String name = matcher.group(1);
            String num = matcher.group(2);
            return new String[]{name, num};
        } else {
            return null;
        }
    }
}


