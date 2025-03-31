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
        String diffCode = "diff --git a/src/main/java/com/example/Service.java b/src/main/java/com/example/Service.java\n" +
                "index a1b2c3d..e4f5g6h 100644\n" +
                "--- a/src/main/java/com/example/Service.java\n" +
                "+++ b/src/main/java/com/example/Service.java\n" +
                "@@ -5,12 +5,12 @@\n" +
                " public class service {\n" +
                "-    private int UserCount;\n" +
                "+    private int USERCOUNT;\n" +
                " \n" +
                "-    public void getUserInfo() {\n" +
                "+    public void GetUser() {\n" +
                "-        String userName = \"admin\";\n" +
                "+        String NameOfUser = \"admin\";\n" +
                "-        log.info(\"User: {}\", userName);\n" +
                "+        log.info(\"User: \" + NameOfUser);\n" +
                " \n" +
                "-        final int MAX_RETRY = 3;\n" +
                "+        final int maxRetry = 3;\n" +
                "     }\n" +
                " }\n" +
                " \n" +
                "@@ -20,7 +20,7 @@\n" +
                "-        boolean isValid = true;\n" +
                "+        boolean check_valid = false;\n" +
                " \n" +
                "-        List<String> userList = new ArrayList<>();\n" +
                "+        List<String> users_data_list = new ArrayList<>();\n" +
                "     }\n" +
                " }，说出违法代码规范第几条";

        String baseUrl = "http://localhost:8091/api/v1/openai/generate_stream_rag";
        String message = URLEncoder.encode(diffCode, String.valueOf(StandardCharsets.UTF_8));
        String tag = URLEncoder.encode("代码规范手册", String.valueOf(StandardCharsets.UTF_8));
        String model = "gpt-4o";
        String urlStr = baseUrl + "?message=" + message + "&ragTag=" + tag + "&model=" + model;

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/event-stream"); // 处理流式数据
        connection.setDoInput(true);

        StringBuilder fullContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received: " + line);

                if (line.startsWith("data:")) {
                    String jsonStr = line.substring(5).trim();
                    try {
                        JSONObject json = JSON.parseObject(jsonStr);

                        // 使用链式安全访问
                        String content = "";
                        if (json != null
                                && json.containsKey("result")
                                && json.getJSONObject("result").containsKey("output")) {
                            content = json.getJSONObject("result")
                                    .getJSONObject("output")
                                    .getString("content");
                        }

                        // 更安全的空值判断
                        if (content != null && !content.isEmpty()) {
                            fullContent.append(content);
                        }
                    } catch (JSONException e) {
                        System.err.println("FastJSON解析错误: " + e.getMessage());
                    }
                }
            }
        } finally {
            connection.disconnect();
        }

        System.out.println("\n完整合并内容：");
        System.out.println(fullContent.toString());
    }
}
