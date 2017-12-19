package md2html;

import javafx.util.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Created by nikitos on 18.12.17.
 */

public class Md2Html {

    private static final String ENCODING = "utf8";
    private static StringBuilder sb = new StringBuilder();
    private static final Pattern headingPattern = Pattern.compile("^(#+)\\s+(.*)", Pattern.DOTALL);
    private static List<Pair<Pair<String,String>,Pair<String, String>>> tagPair = Arrays.asList(
            new Pair<>(new Pair<>("(?<!\\\\)\\*\\*","(?<!\\\\)\\*\\*"), new Pair<>("<strong>", "</strong>")),
            new Pair<>(new Pair<>("(?<!\\\\)__", "(?<!\\\\)__"), new Pair<>("<strong>", "</strong>")),
            new Pair<>(new Pair<>("(?<!\\\\)\\*", "(?<!\\\\)\\*"), new Pair<>("<em>", "</em>")),
            new Pair<>(new Pair<>("(?<!\\\\)_", "(?<!\\\\)_"), new Pair<>("<em>", "</em>")),
            new Pair<>(new Pair<>("--", "--"), new Pair<>("<s>", "</s>")),
            new Pair<>(new Pair<>("\\+\\+", "\\+\\+"), new Pair<>("<u>", "</u>")),
            new Pair<>(new Pair<>("`", "`"), new Pair<>("<code>", "</code>")),
            new Pair<>(new Pair<>("~", "~"), new Pair<>("<mark>", "</mark>"))
    );

    public static void start(String inputFileName, String outputFileName) {

        String contents;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), ENCODING))) {
            contents = reader.lines().collect(Collectors.joining("\n"));
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found: " + inputFileName);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String[] inputStrings = contents.replaceAll("^\n*", "").replaceAll("\n*$", "").split("\n\\s*\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {

            int iterator = 0;
            for (String now: inputStrings) {

                if (isHeading(now)) {
                    parseHeader(now); 
                } else { 
                    parseParagraph(now);
                }

                writer.write(sb.toString());
                if (inputStrings.length - 1 != iterator++) writer.write("\n");
                sb.setLength(0);
            }

        } catch (IOException e) {
            System.err.println("Failed to write to the output file: " + outputFileName);
        }

    }

    private static void parseHeader(String s) {
        Matcher matcher = headingPattern.matcher(s);
        boolean matches = matcher.matches();
        int headingLevel = matcher.group(1).length();
        String rest = matcher.group(2);

        sb.append("<h").append(headingLevel).append(">");

        checkContent(rest);

        sb.append("</h").append(headingLevel).append(">");
    }

    private static void parseParagraph(String s) {

        sb.append("<p>");

        checkContent(s);

        sb.append("</p>");
    }

    private static void checkContent(String s) {
        sb.append(unescapeSpecialCharacters(replaceTags(escapeSpecialCharacters(s))));
    }

    private static String replaceTags(String s) {
        if (s == null) throw new NullPointerException();

        for (Pair<Pair<String,String>,Pair<String,String>> pair: tagPair) {
            Pair first = pair.getKey();
            Pair second = pair.getValue();
            s = s.replaceAll("(?s)" + first.getKey() + "(.*?)" + first.getValue(), second.getKey() + "$1" + second.getValue());
        }

        return s;
    }

    private static String escapeSpecialCharacters(String s) {
        if (s == null) throw new NullPointerException();

        return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    private static String unescapeSpecialCharacters(String s) {
        if (s == null) throw new NullPointerException();

        return s.replaceAll("\\\\\\*", "\\*").replaceAll("\\\\_", "_");
    }

    private static boolean isHeading(String s) {
        if (s == null) throw new NullPointerException();

        return headingPattern.matcher(s).matches();
    }

    public static void main(String[] args) {
        new Md2Html().start(args[0], args[1]);
    }

}
