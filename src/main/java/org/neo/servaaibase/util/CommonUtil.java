package org.neo.servaaibase.util;

import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaaibase.model.AIModel;

public class CommonUtil {
    public static String getConfigValue(DBConnectionIFC dbConnection, String configName) {
        try {
            String sql = "select configvalue";
            sql += " from configs";
            sql += " where configname = ?";
            List<Object> params = new ArrayList<Object>();
            params.add(configName);
            SQLStruct sqlStruct = new SQLStruct(sql, params);
            return (String)dbConnection.queryScalar(sqlStruct);
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String dateToString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String renderChatRecords(List<AIModel.ChatRecord> chatRecords, String datetimeFormat) {
        String rendered = "<!DOCTYPE html>";
        rendered += "\n<html>";
        rendered += "\n<head>";
        rendered += "\n<meta charset=\"UTF-8\">";
        rendered += "\n<title></title>";
        rendered += "\n<style>";
        rendered += "\n.chat-container {";
        rendered += "\ndisplay: flex;";
        rendered += "\nflex-direction: column;";
        rendered += "\nalign-items: flex-start;";
        rendered += "\ntext-align: left;";
        rendered += "\n}";

        rendered += "\n.chat-bubble {";
        rendered += "\nbackground-color: #e0e0e0;";
        rendered += "\nborder-radius: 5px;";
        rendered += "\npadding-top: 10px;";
        rendered += "\npadding-bottom: 10px;";
        rendered += "\npadding-left: 10px;";
        rendered += "\npadding-right: 10px;";
        rendered += "\nmargin-top: 5px;";
        rendered += "\nmargin-bottom: 5px;";
        rendered += "\nmax-width: 80%;";
        rendered += "\nfont-family: sans-serif;";
        rendered += "\nfont-size: 16px;";
        rendered += "\nfont-weight: 300;";
        rendered += "\n}";

        rendered += "\n.chat-bubble.request-title {";
        rendered += "\nbackground-color: #ffffff;";
        rendered += "\ncolor: #000000;";
        rendered += "\nalign-self: flex-end;";
        rendered += "\n}";

        rendered += "\n.chat-bubble.request {";
        rendered += "\nbackground-color: #3388FF;";
        rendered += "\ncolor: #ffffff;";
        rendered += "\nalign-self: flex-end;";
        rendered += "\n}";

        rendered += "\n.chat-bubble.response-title {";
        rendered += "\nbackground-color: #ffffff;";
        rendered += "\ncolor: #000000;";
        rendered += "\nalign-self: flex-start;";
        rendered += "\n}";

        rendered += "\n.chat-bubble.response {";
        rendered += "\nbackground-color: #dfdfe9;";
        rendered += "\ncolor: #000000;";
        rendered += "\nalign-self: flex-start;";
        rendered += "\n}";

        rendered += "\n.avatar-img {";
        rendered += "\nborder-radius: 50%;";
        rendered += "\nwidth: 40px;";
        rendered += "\nheight: 40px;";
        rendered += "\nobject-fit: cover;";
        rendered += "\n}";

        rendered += "\n</style>";
        rendered += "\n</head>";
        rendered += "\n<body>";
        rendered += "\n<div class=\"chat-container\">";

        for(AIModel.ChatRecord chatRecord: chatRecords) {
            rendered += renderChatRecord(chatRecord, datetimeFormat);
        }

        rendered += "\n</div>";
        rendered += "\n</body>";
        rendered += "\n</html>";

        return rendered;
    }

    private static String renderChatRecord(AIModel.ChatRecord chatRecord, String datetimeFormat) {
        String rendered = "";

        if(chatRecord.getIsRequest()) {
            rendered += "\n<div class=\"chat-bubble request-title\">";
            rendered += "\n<p>";
            rendered += "[" + dateToString(chatRecord.getChatTime(), datetimeFormat) + "] ";
            rendered += "<img src=\"images/client1.png\" class=\"avatar-img\" alt=\"Avatar\">";
            rendered += "</p>";
            rendered += "\n</div>";
            rendered += "\n<div class=\"chat-bubble request\">";
        }
        else {
            rendered += "\n<div class=\"chat-bubble response-title\">";
            rendered += "\n<p>";
            rendered += "<img src=\"images/robot1.png\" class=\"avatar-img\" alt=\"Avatar\">";
            rendered += "[" + dateToString(chatRecord.getChatTime(), datetimeFormat) + "]";
            rendered += "</p>";
            rendered += "\n</div>";
            rendered += "\n<div class=\"chat-bubble response\">";
        }

        rendered += "\n<p>";

        if(chatRecord.getIsRequest()) {
            rendered += RenderToShowAsOrigin(chatRecord.getContent());
        }
        else {
            rendered += RenderToShowAsHtml(chatRecord.getContent());
        }
        
        rendered += "\n</p>";
        rendered += "\n</div>";

        return rendered;
    }

    private static String RenderToShowAsOrigin(String content) {
        String renderOrigin = content.replace("<", "&lt");
        renderOrigin = renderOrigin.replace(">", "&gt");
        renderOrigin = renderOrigin.replace("\n", "<br>");
        return renderOrigin;
    }

    private static String RenderToShowAsHtml(String content) {
        String renderFormat = content.replace("\n", "<br>");
        return renderFormat;
    }

    private static boolean isUnix() {
        return File.separator.equals("/"); 
    }

    private static String[] wrapCommand(String input) {
        if(isUnix()) {
            return new String[] {"/bin/sh", "-c", input};
        }
        else {
            return new String[] {"cmd", "/c", input};
        }
    }

    private static String[] splitCommand(String input) {
        List<String> commandParts = new ArrayList<>();
        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (matcher.find()) {
            commandParts.add(matcher.group(1).replace("\"", ""));
        }
        return commandParts.toArray(new String[0]);
    }

    public static String executeCommand(String command) {
        try {
            String[] wrappedCommand = wrapCommand(command);
            ProcessBuilder processBuilder = new ProcessBuilder(wrappedCommand);
            Process process = processBuilder.start();

            String stdResult = IOUtil.inputStreamToString(process.getInputStream());
            String errResult = IOUtil.inputStreamToString(process.getErrorStream());
            int exitCode = process.waitFor();
            if(exitCode == 0) {
                return stdResult;
            }
            else {
                throw new RuntimeException(errResult);
            }
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static double consineSimilarity(AIModel.Embedding embeddingA, AIModel.Embedding embeddingB) {
        if(embeddingA.size() != embeddingB.size()) {
            throw new RuntimeException("consineSimilartiy can only be applied with two embeddings with the same dimension");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < embeddingA.size(); i++) {
            dotProduct += embeddingA.get(i) * embeddingB.get(i);
            normA += Math.pow(embeddingA.get(i), 2);
            normB += Math.pow(embeddingB.get(i), 2);
        }   
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static String extractMimeTypeFromBase64(String base64) {
        // Check if the string starts with "data:" and contains ";base64,"
        if (base64.startsWith("data:") && base64.contains(";base64,")) {
            // Define the regular expression pattern for extracting the MIME type
            Pattern pattern = Pattern.compile("data:([^;]+);base64,");
            Matcher matcher = pattern.matcher(base64);
            
            // If the pattern matches, return the MIME type
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        // Return null if no MIME type is present or the string does not follow the data URI format
        return null;
    }

    public static String extractRawBase64(String base64) {
        if (base64.startsWith("data:") && base64.contains(";base64,")) {
            return base64.substring(base64.indexOf(";base64,") + 8);
        }
        return base64; // If no prefix is present, return the original string
    }
}
