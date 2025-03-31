package cn.Levionyx.middleware.sdk.domain.service.impl;

import cn.Levionyx.middleware.sdk.domain.service.AbstractOpenAiCodeReviewService;
import cn.Levionyx.middleware.sdk.infrastructure.git.GitCommand;
import cn.Levionyx.middleware.sdk.infrastructure.rag.IRAGService;
import cn.Levionyx.middleware.sdk.infrastructure.weixin.WeiXin;
import cn.Levionyx.middleware.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {
    public OpenAiCodeReviewService(GitCommand gitCommand, WeiXin weiXin, IRAGService ragService) {
        super(gitCommand, weiXin, ragService);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {

        // 1. 调用RAG知识库接口
        return ragService.completionsWithRag(diffCode);
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);
    }
}
