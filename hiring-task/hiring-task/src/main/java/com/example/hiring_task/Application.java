package com.example.hiring_task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            // Step 1: Generate webhook
            String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            JSONObject body = new JSONObject();
            body.put("name", "John Doe");
            body.put("regNo", "REG12347");
            body.put("email", "john@example.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

            System.out.println("Generating webhook...");
            ResponseEntity<String> response = restTemplate.postForEntity(generateWebhookUrl, request, String.class);

            System.out.println("Webhook generation full response: " + response.getBody());

            JSONObject jsonResponse = new JSONObject(response.getBody());
            String webhookUrl = jsonResponse.getString("webhook");
            String accessToken = jsonResponse.getString("accessToken");

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            // Step 2: SQL Query (Final Answer)
            String finalQuery = """
                SELECT 
                    e1.EMP_ID,
                    e1.FIRST_NAME,
                    e1.LAST_NAME,
                    d.DEPARTMENT_NAME,
                    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
                FROM EMPLOYEE e1
                JOIN DEPARTMENT d 
                    ON e1.DEPARTMENT = d.DEPARTMENT_ID
                LEFT JOIN EMPLOYEE e2 
                    ON e1.DEPARTMENT = e2.DEPARTMENT
                    AND e2.DOB > e1.DOB
                GROUP BY 
                    e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
                ORDER BY 
                    e1.EMP_ID DESC;
                """;

            JSONObject payload = new JSONObject();
            payload.put("finalQuery", finalQuery);

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Authorization", accessToken); // ✅ per instructions (no "Bearer ")

            HttpEntity<String> authRequest = new HttpEntity<>(payload.toString(), authHeaders);

            // Step 3: Submit SQL Query
            // Print full HTTP request for debugging
            System.out.println("\n========= REQUEST DETAILS =========");
            System.out.println("URL: " + webhookUrl);
            System.out.println("Method: POST");
            System.out.println("Headers:");
            authHeaders.forEach((key, value) -> System.out.println("  " + key + ": " + value));
            System.out.println("Body:");
            System.out.println(payload.toString(2)); // Pretty-print JSON body
            System.out.println("===================================\n");

            ResponseEntity<String> webhookResponse = restTemplate.postForEntity(webhookUrl, authRequest, String.class);

            System.out.println("Webhook submission response: " + webhookResponse.getStatusCode());
            System.out.println("Response Body: " + webhookResponse.getBody());
          
 } catch (HttpClientErrorException e) {
        System.err.println("❌ Webhook submission failed: " + e.getStatusCode());
        System.err.println("Response body: " + e.getResponseBodyAsString());
    } catch (Exception e) {
        System.err.println("❌ An unexpected error occurred: " + e.getMessage());
        e.printStackTrace();
    } 
         

            
}
}
