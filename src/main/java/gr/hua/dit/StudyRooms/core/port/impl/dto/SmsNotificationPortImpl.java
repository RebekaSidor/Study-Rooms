package gr.hua.dit.StudyRooms.core.port.impl.dto;

import gr.hua.dit.StudyRooms.config.RestApiClientConfig;
import gr.hua.dit.StudyRooms.core.port.SmsNotificationPort;

import gr.hua.dit.StudyRooms.core.port.impl.dto.SendSmsRequest;
import gr.hua.dit.StudyRooms.core.port.impl.dto.SendSmsResult;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;


/**
 * Default implementation of {@SmsNotificationPort}. It uses the NOC external service.
 */
@Service
public class SmsNotificationPortImpl implements SmsNotificationPort {

    private final RestTemplate restTemplate;
    public  SmsNotificationPortImpl(final RestTemplate restTemplate) {
        if (restTemplate == null) throw new IllegalArgumentException();
        this.restTemplate = restTemplate;
    }
    @Override
    public boolean sendSms(final String e164, final String content) {
        if ( e164 == null ) throw new NullPointerException();
        if (e164.isBlank() ) throw new IllegalArgumentException();
        if (content == null ) throw new NullPointerException();
        if (content.isBlank() ) throw new IllegalArgumentException();


        //HTTP HEADERS
        //----------------------------------

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        final SendSmsRequest body = new SendSmsRequest(e164, content);

        final String baseUrl = RestApiClientConfig.BASE_URL;
        final String url = baseUrl + "/api/v1/sms";
        final HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, httpHeaders);//request
        final ResponseEntity<SendSmsResult> response = this.restTemplate.postForEntity(url, entity, SendSmsResult.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            final SendSmsResult sendSmsResult = response.getBody();
            if (sendSmsResult == null ) throw new NullPointerException();
            return sendSmsResult.sent();
        }
        throw new RuntimeException("External Service responded with" + response.getStatusCode());
    }
}
