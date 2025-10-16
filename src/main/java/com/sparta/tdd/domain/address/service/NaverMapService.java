package com.sparta.tdd.domain.address.service;

import com.sparta.tdd.domain.address.dto.AddressResponseDto;
import com.sparta.tdd.domain.address.dto.naver.NaverAddress;
import com.sparta.tdd.domain.address.dto.naver.NaverAddressResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class NaverMapService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String secretId;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", secretId)
                .build();
    }
    public Page<AddressResponseDto> getAddress(String address, Pageable pageable) {
        NaverAddressResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("maps.apigw.ntruss.com")
                        .path("/map-geocode/v2/geocode")
                        .queryParam("query", address)
                        .build())
                .retrieve()
                .body(NaverAddressResponse.class);
        List<NaverAddress> addresses = response.addresses();
        Page<NaverAddress> naverAddresses = new PageImpl<>(addresses, pageable, addresses.size());
        return naverAddresses.map(AddressResponseDto::from);
    }
}
