package com.lavaderosepulveda.app.dto;

public class ChatbotOption {

    private String label;
    private String value;

    public ChatbotOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}

