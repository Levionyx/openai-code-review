package cn.Levionyx.middleware.sdk;

import cn.Levionyx.middleware.sdk.domain.model.ChatCompletionRequest;
import cn.Levionyx.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import cn.Levionyx.middleware.sdk.domain.model.Model;
import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {

        System.out.println("openai 代码评审，测试执行");

        String token = System.getenv("GITHUB_TOKEN");
        if (null == token || token.isEmpty()) {
            throw new RuntimeException("token is null");
        }

        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);
        System.out.println("diff code：" + diffCode.toString());

        // 2. DeepSeek代码评审
        String log = codeReview(diffCode.toString());
        System.out.println("code review：" + log);

        // 3. 写入评审日志
        String logUrl = writeLog(token, log);
        System.out.println("writeLog：" + logUrl);
    }

    public static String codeReview(String diffCode) throws IOException {
        String apikey = "sk-471cff88505d46b68356958a9cebe873";

        URL url = new URL("https://api.deepseek.com/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apikey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.DEEPSEEK_V3.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
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
        return (response.getChoices().get(0).getMessage().getContent());
    }

    public static String writeLog(String token, String log) throws Exception {

        // 使用 JGit 库克隆远程仓库到本地
        Git git = Git.cloneRepository()
                // 设置 Git 仓库的远程 URL
                .setURI("https://github.com/Levionyx/openai-code-review-log.git")
                // 指定本地存储目录（名为 "repo" 的文件夹）
                .setDirectory(new File("repo"))
                // 提供认证凭据（GitHub Token 作为用户名，密码留空）
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                // 执行克隆操作
                .call();

        // 生成日期格式的文件夹名（例如：2023-08-20）
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 创建日期文件夹对象（路径：repo/2023-08-20）
        File dateFolder = new File("repo/" + dateFolderName);
        // 检查文件夹是否存在，若不存在则创建
        if (!dateFolder.exists()) {
            dateFolder.mkdir(); // 创建单层目录
        }

        // 生成一个 12 位随机字符串作为文件名（例如：aBcDeF123456.md）
        String fileName = generateRandomString(12) + ".md";
        // 在日期文件夹下创建新文件对象
        File newFile = new File(dateFolder, fileName);
        // 使用 try-with-resources 自动关闭文件写入流
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(log); // 将日志内容写入文件
        }

        // 将新文件添加到 Git 暂存区
        // 文件模式需包含相对路径（例如：2023-08-20/aBcDeF123456.md）
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        // 提交变更，附带提交信息
        git.commit().setMessage("Add new file via GitHub Actions").call();
        // 推送到远程
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        System.out.println("Changes have been pushed to the repository.");
        // 返回新文件的 GitHub 访问 URL
        return "https://github.com/Levionyx/openai-code-review-log.git/blob/master/" + dateFolderName + "/" + fileName;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
