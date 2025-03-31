package cn.Levionyx.middleware.sdk.infrastructure.rag;

import cn.Levionyx.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cn.Levionyx.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IRAGService {
    String completionsWithRag(String message) throws Exception;
}
