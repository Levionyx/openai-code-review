package cn.Levionyx.middleware.sdk.infrastructure.rag.impl;

import cn.Levionyx.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cn.Levionyx.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import cn.Levionyx.middleware.sdk.infrastructure.rag.IRAGService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RAGServiceImpl implements IRAGService {
    @Override
    public String completionsWithRag(String diffCode) throws Exception {

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
                // System.out.println("Received: " + line);

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

        return String.valueOf(fullContent);
    }

}
