package io.wisoft.mqtt.application;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqttSubscriberService implements MqttCallback {

    private MqttClient mqttClient;
    private MqttConnectOptions mqttOptions;

    @Autowired
    private WebClientService webClientService;

    public MqttSubscriberService init(final String server, final String clientId) throws MqttException {

        mqttOptions = new MqttConnectOptions();
        mqttOptions.setCleanSession(true);
        mqttOptions.setKeepAliveInterval(30);
        mqttClient = new MqttClient(server, clientId);
        mqttClient.setCallback(this);
        mqttClient.connect(mqttOptions);

        return this;
    }

    // 커넥션이 종료되면 호출 - 통신 오류로 연결이 끊어지는 경우 호출
    @Override
    public void connectionLost(final Throwable cause) {
        System.out.println("연결이 중단되었습니다.");
        System.out.println(cause);
    }

    // 메시지가 도착하면 호출
    @Override
    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
        System.out.println("메시지도착");
        System.out.println(message);
        System.out.println("topic = " + topic + ", id = " + message.getId() + ", payload = " + new String(message.getPayload()));
        webClientService.post(message);
    }

    // 구독 신청
    public boolean subscribe(final String topic) throws MqttException {

        if (topic != null) {
            mqttClient.subscribe(topic, 0);
        }

        return true;
    }

    // 메시지의 배달이 완료되면 호출
    @Override
    public void deliveryComplete(final IMqttDeliveryToken token) {
    }
}