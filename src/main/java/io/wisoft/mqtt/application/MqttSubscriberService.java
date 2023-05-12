package io.wisoft.mqtt.application;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscriberService implements MqttCallback {

    private MqttClient mqttClient;
    private MqttConnectOptions mqttOptions;

    public MqttSubscriberService init(String server, String clientId) throws MqttException {

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
    public void connectionLost(Throwable cause) {
        System.out.println("연결이 중단되었습니다.");
    }

    // 메시지가 도착하면 호출
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("메시지도착");
        System.out.println(message);
        System.out.println("topic = " + topic + ", id = " + message.getId() + ", payload = " + new String(message.getPayload()));
    }

    // 구독 신청
    public boolean subscribe(String topic) throws MqttException {

        if (topic != null) {
            mqttClient.subscribe(topic, 0);
        }

        return true;
    }

    // 메시지의 배달이 완료되면 호출
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}