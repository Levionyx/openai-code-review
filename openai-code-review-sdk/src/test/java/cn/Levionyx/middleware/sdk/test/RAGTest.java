package cn.Levionyx.middleware.sdk.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RAGTest {

    @Test
    public void rag_http() throws Exception {
//        String diffCode = "diff --git a/src/main/java/com/example/Service.java b/src/main/java/com/example/Service.java\n" +
//                "index a1b2c3d..e4f5g6h 100644\n" +
//                "--- a/src/main/java/com/example/Service.java\n" +
//                "+++ b/src/main/java/com/example/Service.java\n" +
//                "@@ -5,12 +5,12 @@\n" +
//                " public class service {\n" +
//                "-    private int UserCount;\n" +
//                "+    private int USERCOUNT;\n" +
//                " \n" +
//                "-    public void getUserInfo() {\n" +
//                "+    public void GetUser() {\n" +
//                "-        String userName = \"admin\";\n" +
//                "+        String NameOfUser = \"admin\";\n" +
//                "-        log.info(\"User: {}\", userName);\n" +
//                "+        log.info(\"User: \" + NameOfUser);\n" +
//                " \n" +
//                "-        final int MAX_RETRY = 3;\n" +
//                "+        final int maxRetry = 3;\n" +
//                "     }\n" +
//                " }\n" +
//                " \n" +
//                "@@ -20,7 +20,7 @@\n" +
//                "-        boolean isValid = true;\n" +
//                "+        boolean check_valid = false;\n" +
//                " \n" +
//                "-        List<String> userList = new ArrayList<>();\n" +
//                "+        List<String> users_data_list = new ArrayList<>();\n" +
//                "     }\n" +
//                " }，说出违法代码规范第几条";
//
//        String baseUrl = "http://localhost:8091/api/v1/openai/generate_stream_rag";
//        String message = URLEncoder.encode(diffCode, String.valueOf(StandardCharsets.UTF_8));
//        String tag = URLEncoder.encode("代码规范手册", String.valueOf(StandardCharsets.UTF_8));
//        String model = "gpt-4o";
//        String urlStr = baseUrl + "?message=" + message + "&ragTag=" + tag + "&model=" + model;
//        String urlStr = "http://localhost:8091/api/v1/openai/generate_stream_rag?message=diff+--git+a%2Fopenai-code-review-sdk%2Fsrc%2Fmain%2Fjava%2Fcn%2FLevionyx%2Fmiddleware%2Fsdk%2Finfrastructure%2Frag%2Fimpl%2FRAGServiceImpl.java+b%2Fopenai-code-review-sdk%2Fsrc%2Fmain%2Fjava%2Fcn%2FLevionyx%2Fmiddleware%2Fsdk%2Finfrastructure%2Frag%2Fimpl%2FRAGServiceImpl.java%0Aindex+f0385b2..3b974fd+100644%0A---+a%2Fopenai-code-review-sdk%2Fsrc%2Fmain%2Fjava%2Fcn%2FLevionyx%2Fmiddleware%2Fsdk%2Finfrastructure%2Frag%2Fimpl%2FRAGServiceImpl.java%0A%2B%2B%2B+b%2Fopenai-code-review-sdk%2Fsrc%2Fmain%2Fjava%2Fcn%2FLevionyx%2Fmiddleware%2Fsdk%2Finfrastructure%2Frag%2Fimpl%2FRAGServiceImpl.java%0A%40%40+-32%2C6+%2B32%2C7+%40%40+public+class+RAGServiceImpl+implements+IRAGService+%7B%0A+++++++++String+urlStr+%3D+baseUrl+%2B+%22%3Fmessage%3D%22+%2B+message+%2B+%22%26ragTag%3D%22+%2B+tag+%2B+%22%26model%3D%22+%2B+model%3B%0A+%0A+++++++++URL+url+%3D+new+URL%28urlStr%29%3B%0A%2B++++++++logger.info%28%22%E8%AF%B7%E6%B1%82%E7%BD%91%E5%9D%80%E4%B8%BA%EF%BC%9A%7B%7D%22%2C+url%29%3B%0A+++++++++HttpURLConnection+connection+%3D+%28HttpURLConnection%29+url.openConnection%28%29%3B%0A+++++++++connection.setRequestMethod%28%22GET%22%29%3B%0A+++++++++connection.setRequestProperty%28%22Accept%22%2C+%22text%2Fevent-stream%22%29%3B+%2F%2F+%E5%A4%84%E7%90%86%E6%B5%81%E5%BC%8F%E6%95%B0%E6%8D%AE%0A&ragTag=%E4%BB%A3%E7%A0%81%E8%A7%84%E8%8C%83%E6%89%8B%E5%86%8C&model=gpt-4o";
//        URL url = new URL(urlStr);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Accept", "text/event-stream"); // 处理流式数据
//        connection.setDoInput(true);
//
//        StringBuilder fullContent = new StringBuilder();
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println("Received: " + line);
//
//                if (line.startsWith("data:")) {
//                    String jsonStr = line.substring(5).trim();
//                    try {
//                        JSONObject json = JSON.parseObject(jsonStr);
//
//                        // 使用链式安全访问
//                        String content = "";
//                        if (json != null
//                                && json.containsKey("result")
//                                && json.getJSONObject("result").containsKey("output")) {
//                            content = json.getJSONObject("result")
//                                    .getJSONObject("output")
//                                    .getString("content");
//                        }
//
//                        // 更安全的空值判断
//                        if (content != null && !content.isEmpty()) {
//                            fullContent.append(content);
//                        }
//                    } catch (JSONException e) {
//                        System.err.println("FastJSON解析错误: " + e.getMessage());
//                    }
//                }
//            }
//        } finally {
//            connection.disconnect();
//        }
//
//        System.out.println("\n完整合并内容：");
//        System.out.println(fullContent.toString());
    }
}
