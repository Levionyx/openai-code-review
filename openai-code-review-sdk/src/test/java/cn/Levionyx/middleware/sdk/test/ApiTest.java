package cn.Levionyx.middleware.sdk.test;

import cn.Levionyx.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import cn.Levionyx.middleware.sdk.types.utils.WXAccessTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ApiTest {
    @Test
    public void test_http() throws Exception {
        String apikey = "sk-471cff88505d46b68356958a9cebe873";

        URL url = new URL("https://api.deepseek.com/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apikey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String code = "1+1";

        String jsonInputString = "{\n" +
                "        \"model\": \"deepseek-chat\",\n" +
                "        \"messages\": [\n" +
                "          {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n" +
                "          {\"role\": \"user\", \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言，请您根据git diff记录，对代码做出评审。代码如下:" + code + "\"}\n" +
                "        ],\n" +
                "        \"stream\": false\n" +
                "      }'";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());
    }

}
