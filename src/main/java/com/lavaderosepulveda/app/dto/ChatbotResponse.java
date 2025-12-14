package com.lavaderosepulveda.app.dto;

import java.util.List;

public class ChatbotResponse {

    private String message;
    private String formUrl;
    private String redirectUrl;
    private List<ChatbotOption> options;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ChatbotResponse response = new ChatbotResponse();

        public Builder message(String message) {
            response.message = message;
            return this;
        }

        public Builder formUrl(String formUrl) {
            response.formUrl = formUrl;
            return this;
        }

        public Builder redirectUrl(String redirectUrl) {
            response.redirectUrl = redirectUrl;
            return this;
        }

        public Builder options(List<ChatbotOption> options) {
            response.options = options;
            return this;
        }

        public ChatbotResponse build() {
            return response;
        }
    }

    public String getMessage() {
        return message;
    }

    public String getFormUrl() {
        return formUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public List<ChatbotOption> getOptions() {
        return options;
    }
}
