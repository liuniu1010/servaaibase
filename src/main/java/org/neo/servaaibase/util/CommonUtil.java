package org.neo.servaaibase.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Random;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.NeoAIException;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;

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
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    public static String randomChooseOpenAiApiKey(DBConnectionIFC dbConnection) {
        try {
            String sql = "select configvalue";
            sql += " from configs";
            sql += " where configname like ?";
            List<Object> params = new ArrayList<Object>();
            params.add("OpenAiApiKey%");
            SQLStruct sqlStruct = new SQLStruct(sql, params);

            List<Map<String, Object>> keyMaps = dbConnection.query(sqlStruct);
            Random random = new Random();
            int index = random.nextInt(keyMaps.size());
            Map<String, Object> keyMap = keyMaps.get(index);
            return (String)keyMap.get("configvalue");
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
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
            // rendered += RenderToShowAsOrigin(chatRecord.getContent());
            rendered += RenderToShowAsHtml(chatRecord.getContent());
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
                return stdResult + "\n" + errResult;
            }
            else {
                throw new NeoAIException(stdResult + "\n" + errResult);
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    public static double consineSimilarity(AIModel.Embedding embeddingA, AIModel.Embedding embeddingB) {
        if(embeddingA.size() != embeddingB.size()) {
            throw new NeoAIException("consineSimilartiy can only be applied with two embeddings with the same dimension");
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

    public static String base64ToFile(String base64, String onlineFileAbsolutePath) throws IOException {
        String mimeType = extractMimeTypeFromBase64(base64);
        String rawBase64 = extractRawBase64(base64); 
        String extension = mimeTypeToExtenstion(mimeType);
        String fileName = "upload_" + getRandomString(10) + extension;
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
        IOUtil.rawBase64ToFile(rawBase64, filePath);
        return fileName; 
    }

    private static final Map<String, String> mimeTypeToExtensionMap = new HashMap<>();
    static {
        mimeTypeToExtensionMap.put("text/plain", ".txt");
        mimeTypeToExtensionMap.put("text/html", ".html");
        mimeTypeToExtensionMap.put("text/css", ".css");
        mimeTypeToExtensionMap.put("text/javascript", ".js");
        mimeTypeToExtensionMap.put("text/csv", ".csv");
        mimeTypeToExtensionMap.put("text/xml", ".xml");
        mimeTypeToExtensionMap.put("image/jpeg", ".jpg");
        mimeTypeToExtensionMap.put("image/png", ".png");
        mimeTypeToExtensionMap.put("image/gif", ".gif");
        mimeTypeToExtensionMap.put("image/bmp", ".bmp");
        mimeTypeToExtensionMap.put("image/webp", ".webp");
        mimeTypeToExtensionMap.put("image/svg+xml", ".svg");
        mimeTypeToExtensionMap.put("image/tiff", ".tiff");
        mimeTypeToExtensionMap.put("image/x-icon", ".ico");
        mimeTypeToExtensionMap.put("audio/mpeg", ".mp3");
        mimeTypeToExtensionMap.put("audio/wav", ".wav");
        mimeTypeToExtensionMap.put("audio/ogg", ".ogg");
        mimeTypeToExtensionMap.put("audio/aac", ".aac");
        mimeTypeToExtensionMap.put("audio/webm", ".weba");
        mimeTypeToExtensionMap.put("audio/flac", ".flac");
        mimeTypeToExtensionMap.put("video/mp4", ".mp4");
        mimeTypeToExtensionMap.put("video/mpeg", ".mpeg");
        mimeTypeToExtensionMap.put("video/ogg", ".ogv");
        mimeTypeToExtensionMap.put("video/webm", ".webm");
        mimeTypeToExtensionMap.put("video/x-msvideo", ".avi");
        mimeTypeToExtensionMap.put("video/quicktime", ".mov");
        mimeTypeToExtensionMap.put("application/json", ".json");
        mimeTypeToExtensionMap.put("application/xml", ".xml");
        mimeTypeToExtensionMap.put("application/pdf", ".pdf");
        mimeTypeToExtensionMap.put("application/zip", ".zip");
        mimeTypeToExtensionMap.put("application/gzip", ".gz");
        mimeTypeToExtensionMap.put("application/x-www-form-urlencoded", "");
        mimeTypeToExtensionMap.put("application/octet-stream", ".bin");
        mimeTypeToExtensionMap.put("application/msword", ".doc");
        mimeTypeToExtensionMap.put("application/vnd.ms-excel", ".xls");
        mimeTypeToExtensionMap.put("application/vnd.ms-powerpoint", ".ppt");
        mimeTypeToExtensionMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        mimeTypeToExtensionMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        mimeTypeToExtensionMap.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
        mimeTypeToExtensionMap.put("font/otf", ".otf");
        mimeTypeToExtensionMap.put("font/ttf", ".ttf");
        mimeTypeToExtensionMap.put("font/woff", ".woff");
        mimeTypeToExtensionMap.put("font/woff2", ".woff2");
        mimeTypeToExtensionMap.put("multipart/form-data", "");
        mimeTypeToExtensionMap.put("multipart/byteranges", "");
        mimeTypeToExtensionMap.put("multipart/alternative", "");
    }

    public static String mimeTypeToExtenstion(String mimeType) {
        return mimeTypeToExtensionMap.getOrDefault(mimeType, "");
    }

    public static String getRandomString(int length) {
        String charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charSet.length());
            sb.append(charSet.charAt(index));
        }

        return sb.toString();
    }

    public static String normalizeFolderPath(String folderPath) {
        if (folderPath.endsWith("/") && !folderPath.equals("/")) {
            return folderPath.substring(0, folderPath.length() - 1);
        }
        return folderPath;
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    public static String alignJson(String compactJson) {
        Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new StringReader(compactJson));
        reader.setLenient(true);
        JsonElement jsonElement = JsonParser.parseReader(reader);
        return gson.toJson(jsonElement);
    }
}

