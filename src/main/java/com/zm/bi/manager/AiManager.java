package com.zm.bi.manager;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import com.zm.bi.common.ErrorCode;
import com.zm.bi.exception.BusinessException;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用AI调用能力  
 */  
@Component
public class AiManager {  
  
    @Resource
    private ClientV4 clientV4;
  
    //随机数：默认是0.95，高了就是不稳定，低了就是稳定  
    //稳定的随机数  
    private static final float STABLE_TEMPERATURE = 0.05f;  
  
    //不稳定的随机数  
    private static final float UNSTABLE_TEMPERATURE = 0.99f;  
  
  
    /**  
     * 通用请求  
     * @param messages  
     * @param stream  
     * @param temperature  
     * @return  
     */    public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature){
        //构建请求  
        //请求id可以自己创建，也可以自动生成，类比数据库主键  
        //String requestId = String.format(requestIdTemplate, System.currentTimeMillis());  
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                //false代表回答全部生成才返回，true代表生成一个字返回一个字  
                .stream(stream)  
                .temperature(temperature)  
                //异步不会立马返回回答，而是返回id，回头要根据id去查询  
                //        .invokeMethod(Constants.invokeMethodAsync)  
                .messages(messages)  
                .invokeMethod(Constants.invokeMethod)  
                //        .requestId(requestId)  
                .build();  
        try{  
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).toString();  
        }catch (Exception e){  
            e.printStackTrace();  
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }  
    }  
  
    /**  
     * 通用请求（简化消息传递）  
     * @param systemMessage  
     * @param userMessage  
     * @param stream  
     * @param temperature  
     * @return  
     */    public String doRequest(String systemMessage,String userMessage, Boolean stream,Float temperature){  
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);  
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(),userMessage);  
        chatMessageList.add(userChatMessage);  
        return doRequest(chatMessageList,stream,temperature);  
    }  
  
    /**  
     * 同步请求  
     * @param systemMessage  
     * @param userMessage  
     * @param temperature  
     * @return  
     */    public String doSyncRequest(String systemMessage,String userMessage,Float temperature){  
        return doRequest(systemMessage,userMessage,Boolean.FALSE,temperature);  
    }  
  
  
    /**  
     * 同步请求（答案较稳定）  
     * @param systemMessage  
     * @param userMessage  
     * @return  
     */    public String doSyncStableRequest(String systemMessage,String userMessage){  
        return doRequest(systemMessage,userMessage,Boolean.FALSE,STABLE_TEMPERATURE);  
    }  
  
  
    /**  
     * 同步请求（答案较不稳定）  
     * @param systemMessage  
     * @param userMessage  
     * @return  
     */    public String doSyncUnstableRequest(String systemMessage,String userMessage){  
        return doRequest(systemMessage,userMessage,Boolean.FALSE,UNSTABLE_TEMPERATURE);  
    }  
  
  
    /**  
     * 通用流式请求  
     * @param messages  
     * @param temperature  
     * @return  
     */    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature){
        //构建请求  
        //请求id可以自己创建，也可以自动生成，类比数据库主键  
        //String requestId = String.format(requestIdTemplate, System.currentTimeMillis());  
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()  
                .model(Constants.ModelChatGLM4)  
                //false代表回答全部生成才返回，true代表生成一个字返回一个字  
                .stream(Boolean.TRUE)  
                .temperature(temperature)  
                //异步不会立马返回回答，而是返回id，回头要根据id去查询  
                //        .invokeMethod(Constants.invokeMethodAsync)  
                .messages(messages)  
                .invokeMethod(Constants.invokeMethod)  
                //        .requestId(requestId)  
                .build();  
        try{  
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);  
            return invokeModelApiResp.getFlowable();  
        }catch (Exception e){  
            e.printStackTrace();  
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());  
        }  
    }  
  
  
    /**  
     * 通用流式请求（简化消息传递）  
     * @param systemMessage  
     * @param userMessage  
     * @param temperature  
     * @return  
     */    public Flowable<ModelData> doStreamRequest(String systemMessage,String userMessage,Float temperature){  
        List<ChatMessage> chatMessageList = new ArrayList<>();  
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);  
        chatMessageList.add(systemChatMessage);  
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(),userMessage);  
        chatMessageList.add(userChatMessage);  
        return doStreamRequest(chatMessageList,temperature);  
    }  
}