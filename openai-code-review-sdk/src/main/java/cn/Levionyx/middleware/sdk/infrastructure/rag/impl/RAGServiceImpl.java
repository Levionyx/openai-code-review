package cn.Levionyx.middleware.sdk.infrastructure.rag.impl;

import cn.Levionyx.middleware.sdk.OpenAiCodeReview;
import cn.Levionyx.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cn.Levionyx.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import cn.Levionyx.middleware.sdk.infrastructure.rag.IRAGService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RAGServiceImpl implements IRAGService {

    private static final Logger logger = LoggerFactory.getLogger(RAGServiceImpl.class);

    @Override
    public String completionsWithRag(String diffCode) throws Exception {
        logger.info("开始RAG评审代码，输入长度: {}", diffCode.length());

        HttpURLConnection connection = null;
        try {
            // 1. 构建请求URL
            String baseUrl = "http://localhost:8091/api/v1/openai/generate_stream_rag";
            String message = URLEncoder.encode(diffCode, StandardCharsets.UTF_8.toString());
            String tag = URLEncoder.encode("代码规范手册", StandardCharsets.UTF_8.toString());
            String model = "gpt-4o";
            String urlStr = baseUrl + "?message=" + message + "&ragTag=" + tag + "&model=" + model;
            logger.debug("编码后参数 - message: {}, tag: {}", message, tag);

            // 2. 创建连接
            URL url = new URL(urlStr);
            logger.info("请求目标URL: {}", url.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoInput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            logger.info("连接配置完成，准备发起请求...");

            // 3. 发起请求
            StringBuilder fullContent = new StringBuilder();
            try {
                InputStream inputStream = connection.getInputStream();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                    logger.info("连接成功，开始读取流数据...");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.trace("收到原始数据: {}", line);

                        if (line.startsWith("data:")) {
                            String jsonStr = line.substring(5).trim();
                            logger.debug("解析JSON数据: {}", jsonStr);

                            try {
                                JSONObject json = JSON.parseObject(jsonStr);
                                String content = Optional.ofNullable(json)
                                        .map(j -> j.getJSONObject("result"))
                                        .map(r -> r.getJSONObject("output"))
                                        .map(o -> o.getString("content"))
                                        .orElse("");

                                if (!content.isEmpty()) {
                                    fullContent.append(content);
                                    logger.debug("已累积内容长度: {}", fullContent.length());
                                }
                            } catch (JSONException e) {
                                logger.error("JSON解析失败！原始数据: {}", jsonStr, e);
                            }
                        }
                    }
                }
            } catch (ConnectException e) {
                logger.error("连接被拒绝！请检查服务状态和网络配置", e);
                throw e;
            } catch (SocketTimeoutException e) {
                logger.error("请求超时！当前设置: connectTimeout={}ms, readTimeout={}ms",
                        connection.getConnectTimeout(),
                        connection.getReadTimeout());
                throw e;
            } catch (IOException e) {
                // 原生方式读取错误流
                String errorResponse = readErrorStream(connection);
                logger.error("IO异常！响应码: {}, 错误响应: {}",
                        connection.getResponseCode(),
                        errorResponse);
                throw new IOException("HTTP " + connection.getResponseCode() + ": " + errorResponse, e);
            }

            logger.info("处理完成，最终内容长度: {}", fullContent.length());
            return fullContent.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
                logger.info("连接已释放");
            }
        }
    }

    // 替换IOUtils.toString的原生方法
    private String readErrorStream(HttpURLConnection connection) {
        InputStream errorStream = connection.getErrorStream();
        if (errorStream == null) {
            return "[无错误信息]";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (IOException e) {
            logger.warn("读取错误流失败", e);
            return "[无法读取错误详情: " + e.getMessage() + "]";
        }
    }
}
