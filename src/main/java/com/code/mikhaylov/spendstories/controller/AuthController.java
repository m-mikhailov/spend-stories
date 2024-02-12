package com.code.mikhaylov.spendstories.controller;

import com.code.mikhaylov.spendstories.client.PlaidClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

// Read quick start from Plaid docs
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PlaidApi client;

    public AuthController(PlaidClient client) {
        this.client = client.getPlaidClient();
    }

    @GetMapping("/link-token")
    public LinkTokenCreateResponse getLinkToken(@AuthenticationPrincipal UserDetails userDetails) throws IOException {
        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(userDetails.getUsername());

        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(user)
                .clientName("Test Spring App")
                .products(List.of(Products.AUTH))
                .countryCodes(List.of(CountryCode.US))
                .language("en");

        return client
                .linkTokenCreate(request)
                .execute()
                .body();
    }

    @PostMapping("/exchange-token")
    public ResponseEntity<?> exchangeToken(@RequestParam("public_token") String publicToken) throws IOException {
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                .publicToken(publicToken);

        Response<ItemPublicTokenExchangeResponse> response = client
                .itemPublicTokenExchange(request)
                .execute();

        String accessToken = response.body().getAccessToken();
        String itemId = response.body().getItemId();

        System.out.println(accessToken);
        System.out.println(itemId);

        return ResponseEntity.ok().build();
    }

}
