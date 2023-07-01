package io.wisoft.mqtt.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wisoft.mqtt.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.*;

@Service
public class WebClientService {

    MqttConfig mqttConfig = new MqttConfig();

    public static void post(final MqttMessage message) throws JsonProcessingException {
        final WebClientService webClientService = new WebClientService();
        webClientService.splitJsonData(message);
    }

    public void splitJsonData(final MqttMessage message) throws JsonProcessingException {

        String json = new String(message.getPayload());
        ObjectMapper objectMapper = new ObjectMapper();

        List<Map<String, Object>> dataList = objectMapper.readValue(json, new TypeReference<>() {});
        List<Map<String, Object>> requestDataList = new ArrayList<>();

        for (Map<String, Object> jsonData : dataList) {
            Map<String, Object> bodyMap = extractData(jsonData);
            requestDataList.add(bodyMap);
        }

        String accessToken = extractToken(dataList);
        requestToApiServer(requestDataList, accessToken);
    }

    private Map<String, Object> extractData(Map<String, Object> jsonData) {

        String value = jsonData.get("value").toString();
        String measurement = jsonData.get("measurement").toString();

        final Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("value", value);
        bodyMap.put("measurement", measurement);

        return bodyMap;
    }

    private String extractToken(List<Map<String, Object>> dataList) {

        if (!dataList.isEmpty()) {
            Map<String, Object> firstData = dataList.get(0);
            if (firstData.containsKey("accessToken")) {
                return firstData.get("accessToken").toString();
            }
        }
        return null;
    }

    public void requestToApiServer(final List<Map<String, Object>> bodyMap, final String accessToken) {

        final WebClient webClient = WebClient
                .builder()
                .baseUrl(mqttConfig.baseUrl)
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("dataList", bodyMap);

        final Map<String, Object> response = (Map<String, Object>) webClient
                .post()
                .uri(mqttConfig.apiUrl)
                .header("accessToken", accessToken)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
