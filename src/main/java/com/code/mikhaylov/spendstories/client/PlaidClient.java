package com.code.mikhaylov.spendstories.client;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PlaidClient {

    private ApiClient client;

    public PlaidClient(@Value("${plaid.client-id}") String clientId,
                       @Value("${plaid.secret}") String secret) {
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", secret);
        this.client = new ApiClient(apiKeys);
        this.client.setPlaidAdapter(ApiClient.Sandbox);
    }

    public PlaidApi getPlaidClient() {
        return client.createService(PlaidApi.class);
    }

}
