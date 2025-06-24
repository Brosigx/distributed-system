package es.um.sisdist.backend.Service;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;
import java.util.ArrayList;

import org.glassfish.jersey.client.ClientConfig;

import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.models.Conversation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//import com.fasterxml.jackson.core.util.JacksonFeature;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TestClient {

        private static final String URL = "http://localhost:8080/Service";
        private static final HttpClient client = HttpClient.newHttpClient();
        private static Logger logger = Logger.getLogger("TestClient");

        public static void main(String[] args) throws Exception {
                String email = "user@um.es";
                String password = "password";
                String name = "User";
                String chatName = "chat1";
                String message = "Hola que tal";
                User user = testUserRegistration(email, password, name);
                if (user != null) {
                        Conversation c = testCreateConversation(user, chatName);
                        if (c != null) {
                                user.addConversation(c);
                                testsendMessage(user, c, message);
                        }
                }
                resetAll(user);
        }

        private static User testUserRegistration(String email, String password, String name) throws Exception {
                String url = URL + "/u/register";
                String json = "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password
                                + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI(url))
                                .header("Content-Type", "application/json")
                                .POST(BodyPublishers.ofString(json))
                                .build();

                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                logger.info("Código de respuesta de registro de usuario: " + response.statusCode());
                logger.info("Cuerpo de respuesta de registro de usuario: " + response.body());
                if (response.statusCode() == 201) {
                        Gson gson = new Gson();
                        return gson.fromJson(response.body(), User.class);
                } else {
                        logger.info("Error al registrar el usuario.");
                        return null;
                }
        }

        private static Conversation testCreateConversation(User user, String chatName) throws Exception {
                String url = URL + "/u/" + user.getId() + "/dialogue";
                // Json
                String json = "{\"dialogue_id\":\"" + chatName + "\"}";
                // Fecha
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
                String date = now.format(formatter);
                // Token
                String authToken = UserUtils.md5pass(url + date + user.getToken());
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI(url))
                                .header("Content-Type", "application/json")
                                .header("User", user.getId())
                                .header("Date", date)
                                .header("Auth-Token", authToken)
                                .POST(BodyPublishers.ofString(json))
                                .build();

                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                logger.info("Código de respuesta de crear conversacion: " + response.statusCode());
                logger.info("Cuerpo de respuesta de crear conversacion: " + response.body());
                if (response.statusCode() == 201) {
                        logger.info(chatName + "creada correctamente.");
                        Gson gson = new Gson();
                        Conversation conversation = gson.fromJson(response.body(), Conversation.class);
                        return conversation;
                } else {
                        logger.info("Error al crear la conversación.");
                        return null;
                }
        }

        private static void testsendMessage(User user, Conversation c, String prompt) throws Exception {
                String url = URL + c.getNext();
                // Json
                long timestamp = System.currentTimeMillis();
                String json = "{\"prompt\":\"" + prompt + "\", \"timestamp\":\"" + timestamp + "\"}";
                // Fecha
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
                String date = now.format(formatter);
                // Token
                String authToken = UserUtils.md5pass(url + date + user.getToken());
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI(url))
                                .header("Content-Type", "application/json")
                                .header("User", user.getId())
                                .header("Date", date)
                                .header("Auth-Token", authToken)
                                .POST(BodyPublishers.ofString(json))
                                .build();

                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                logger.info("Código de respuesta al enviar un prompt: " + response.statusCode());
                logger.info("Cuerpo de respuesta al enviar un prompt: " + response.body());
                if (response.statusCode() == 202) {
                        String location = response.headers().firstValue("Location").orElse("");
                        logger.info("Location: " + location);
                        logger.info("Bucle de espera de respuesta del prompt");

                        url = location;
                        // Fecha
                        now = ZonedDateTime.now(ZoneId.of("UTC"));
                        date = now.format(formatter);
                        // Token
                        authToken = UserUtils.md5pass(url + date + user.getToken());
                        request = HttpRequest.newBuilder()
                                        .uri(new URI(url))
                                        .header("Content-Type", "application/json")
                                        .header("User", user.getId())
                                        .header("Date", date)
                                        .header("Auth-Token", authToken)
                                        .GET()
                                        .build();
                        while (true) {
                                response = client.send(request, BodyHandlers.ofString());
                                logger.info("Código de respuesta de consulta del prompt: " + response.statusCode());
                                logger.info("Cuerpo de respuesta de consulta del prompt: " + response.body());
                                if (response.statusCode() == 200) {
                                        logger.info("Respuesta del prompt recibida");
                                        JsonObject jsonObject = JsonParser.parseString(response.body())
                                                        .getAsJsonObject();
                                        String answer = jsonObject.get("answer").getAsString();

                                        logger.info("conversacion:");
                                        logger.info(user.getEmail() + ":" + prompt + "timestamp:" + timestamp);
                                        logger.info("llama:" + answer);
                                        break;
                                }
                                Thread.sleep(5000);
                        }

                } else {
                        logger.info("Error al enviar el prompt.");
                }

        }

        private static void resetAll(User user) {
                String url = URL + "/u/delete/" + user.getId();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .POST(BodyPublishers.noBody())
                                .build();
                try {
                        client.send(request, BodyHandlers.ofString());
                        logger.info("Usuario" + user.getEmail() + " borrado correctamente.");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
