package xyz.breakit.game.gateway.webclient;

import brave.http.HttpClientAdapter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

public class WebClientAdapter extends HttpClientAdapter<ClientRequest.Builder, ClientResponse> {

    @Override
    public String method(ClientRequest.Builder request) {
        return request.build().method().name();
    }

    @Override
    public String url(ClientRequest.Builder request) {
        return request.build().url().toString();
    }

    @Override
    public String requestHeader(ClientRequest.Builder request, String name) {
        return String.valueOf(request.build().headers().get(name));
    }

    @Override
    public Integer statusCode(ClientResponse response) {
        return response.statusCode().value();
    }
}
