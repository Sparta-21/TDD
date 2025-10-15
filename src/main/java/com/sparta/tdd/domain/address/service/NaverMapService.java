package com.sparta.tdd.domain.address.service;

import com.sparta.tdd.domain.address.dto.AddressResponseDto;
import com.sparta.tdd.domain.address.dto.NaverAddress;
import com.sparta.tdd.domain.address.dto.NaverAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class NaverMapService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String secretId;

    public NaverAddressResponse getAddress(String address) {
        String url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;

        HttpHeaders header = new HttpHeaders();
        header.set("X-NCP-APIGW-API-KEY-ID", clientId);
        header.set("X-NCP-APIGW-API-KEY", secretId);

        HttpEntity<Object> httpEntity = new HttpEntity<>(header);
        ResponseEntity<NaverAddressResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, NaverAddressResponse.class);

        return response.getBody();
    }
}
