package com.genai.restapi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@RestController
@RequestMapping("/agent")
@SessionAttributes("memory")
public class AgentController {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    @ModelAttribute("memory")
    public List<Map<String, String>> memory() {
        return new ArrayList<>();
    }

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> payload,
                       @ModelAttribute("memory") List<Map<String, String>> memory) throws IOException {

        String userInput = payload.get("message");

        // Tool: Calculator
        if (userInput.matches(".*\\d+\\s*[+\\-*/]\\s*\\d+.*")) {
            return evaluateExpression(userInput);
        }

        memory.add(Map.of("role", "user", "content", userInput));

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", memory,
                "temperature", 0.7
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(okhttp3.RequestBody.create(mapper.writeValueAsString(body),MediaType.parse("application/json")))
                .build();



        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            Map<?, ?> res = mapper.readValue(json, Map.class);
            Map<String, Object> message = (Map<String, Object>) ((List<?>) res.get("choices")).get(0);
            String reply = (String) ((Map<?, ?>) message.get("message")).get("content");
            memory.add(Map.of("role", "assistant", "content", reply.trim()));
            return reply.trim();
        }
    }

    private String evaluateExpression(String inputVal) {
        try {
            String input = inputVal.replaceAll("[^\\d+\\-*/.]", "");
            double result = new Object() {
                int pos = -1, ch;

                void nextChar() { ch = (++pos < input.length()) ? input.charAt(pos) : -1; }
                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) { nextChar(); return true; }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < input.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(input.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char)ch);
                    }
                    return x;
                }
            }.parse();
            return String.format("Result from calculator tool: %.2f", result);
        } catch (Exception e) {
            return "Failed to evaluate expression.";
        }
    }
}