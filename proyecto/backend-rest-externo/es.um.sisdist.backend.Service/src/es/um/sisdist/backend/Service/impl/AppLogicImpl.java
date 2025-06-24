/**
 *
 */
package es.um.sisdist.backend.Service.impl;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.Status;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl
{
    IDAOFactory daoFactory;
    IUserDAO dao;

    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl()
    {
        daoFactory = new DAOFactoryImpl();
        Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));
        
        if (backend.isPresent() && backend.get().equals("mongo"))
            dao = daoFactory.createMongoUserDAO();
        else
            dao = daoFactory.createSQLUserDAO();

        var grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
        var grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

        channel = ManagedChannelBuilder
                .forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid needing certificates.
                .usePlaintext().build();
        blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
        asyncStub = GrpcServiceGrpc.newStub(channel);
    }

    public static AppLogicImpl getInstance()
    {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId)
    {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId)
    {
        return dao.getUserById(userId);
    }

    public boolean ping(int v)
    {
    	logger.info("Issuing ping, value: " + v);
    	
        // Test de grpc, puede hacerse con la BD
    	var msg = PingRequest.newBuilder().setV(v).build();
        var response = blockingStub.ping(msg);
        
        return response.getV() == v;
    }

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String pass)
    {
        Optional<User> u = dao.getUserByEmail(email);
        if (u.isPresent())
        {
            logger.info("Usuario: id: " + u.get().getId() + ", nombre: " + u.get().getName());
            logger.info("Dialogue: " + u.get().dialoguesToString());
            String hashed_pass = UserUtils.md5pass(pass);
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash()))
                return u;
        }

        return Optional.empty();
    }

    // Registro de un usuario
    public Optional<User> registerUser(String email, String name, String password){
        return dao.registerUser(UserUtils.md5pass(email), name, email, UserUtils.md5pass(password));
    }

    public Optional <List<String>> getConversationIds(String user_id){
        return dao.getConversationsIds(user_id);
    }

    private boolean checkToken(String user_id, String private_token, String url, String auth_token){
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC)
                                         .withSecond(0)
                                         .withNano(0);  
        // Formato ISO 8601
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
        String date = now.format(formatter);
        String token = UserUtils.md5pass(url + date + private_token);
        logger.info("Token del cliente: " + auth_token);
        logger.info("Token del servidor: " + token);
        if (token.equals(auth_token)){
            return true;
        }
        return false;
    }

    public Optional<Conversation> createConversation(String dialogue_id, String user_id, String url, String auth_token){
        // Comprobamos si está el usuario
        logger.info("Estoy en createConversation y voy a entrar en getUserbyID");
        Optional<User> u = dao.getUserById(user_id);
        if (u.isEmpty()){
            return Optional.empty();
        }
        User user = u.get();
        //Verificamos el token
        if(!checkToken(user.getId(), user.getToken(), url, auth_token)){
            return Optional.empty();
        }
        // Creamos la conversación y se la añadimos al usuario
        String next = "/u/" + user_id + "/dialogue/" + dialogue_id + "/next/" + UUID.randomUUID().toString();
        String end = "/u/" + user_id + "/dialogue/" + dialogue_id + "/end";
        Conversation conversation = new Conversation(dialogue_id, next, end);
        user.addConversation(conversation);
        logger.info("Estoy en createConveration y voy a entrar en updateUser");
        Optional<User> user_response =  dao.updateUser(user);
        // Si se ha añadido correctamente, se devuelve la conversación
        if (user_response.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(conversation);
    }

    public boolean deleteConversation(String dialogue_id, String user_id, String url, String auth_token){
        // Comprobamos si está el usuario
        logger.info("Estoy en deleteConversation y voy a entrar en getUserbyID");
        Optional<User> u = dao.getUserById(user_id);
        if (u.isEmpty()){
            return false;
        }
        User user = u.get();
        //Verificamos el token
        if(!checkToken(user.getId(), user.getToken(), url, auth_token)){
            return false;
        }
        if(!user.deleteConversation(dialogue_id)){
            return false;
        }
        logger.info("Estoy en deleteConversation y voy a entrar en updateUser");
        Optional<User> user_response =  dao.updateUser(user);
        // Si se ha añadido correctamente, se devuelve la conversación
        if (user_response.isEmpty()){
            return false;
        }
        return true;
    }

    public Optional<String> nextPrompt(String user_id, String dialogue_id, String token, String prompt, long timestamp, String url, String auth_token){
        // Comprobamos si está el usuario
        logger.info("Estoy en nextPrompt y voy a entrar en getUserbyID");
        Optional<User> u = dao.getUserById(user_id);
        if (u.isEmpty()){
            return Optional.empty();
        }
        User user = u.get();
        //Verificamos el token
        if(!checkToken(user.getId(), user.getToken(), url, auth_token)){
            return Optional.empty();
        }
        if (user.dialogueStatus(dialogue_id) == Status.READY){
            //TODO Enviar petición gRPC con el token
            user = dao.getUserById(user_id).get(); //recuperamos el usuario actualizado por si la peticion_grpc lo ha modificado
            user.setDialogueStatus(dialogue_id, Status.BUSY);
            logger.info("Estoy en nextPrompt y voy a entrar en updateUser");
            dao.updateUser(user);

            new Thread(() -> {
                peticion_grpc(user_id, dialogue_id, prompt, timestamp, token);
            }).start();

            String location = "/Service/u/" + user_id + "/dialogue/" + dialogue_id + "/" + token; // Usamos el token antiguo para localizar la conversación (no se usa realmente)
            return Optional.of(location);
        }else{
            return Optional.empty();
        }
    }

    public Optional<Conversation> getConversation(String user_id, String dialogue_id, String token, String url, String auth_token){
        // Comprobamos si está el usuario
        logger.info("Estoy en getConversation y voy a entrar en getUserbyID");
        Optional<User> u = dao.getUserById(user_id);
        if (u.isEmpty()){
            return Optional.empty();
        }
        User user = u.get();
        //Verificamos el token
        if(!checkToken(user.getId(), user.getToken(), url, auth_token)){
            return Optional.empty();
        }
        if (user.dialogueStatus(dialogue_id) == Status.READY){
            // Devolvemos la conversación
            Conversation conv = new Conversation ();
            conv.setDialogue_id(dialogue_id);
            conv.setStatus(Status.READY);
            String newNext = "/u/" + user_id + "/dialogue/" + dialogue_id + "/next/" + UUID.randomUUID().toString();
            conv.setNext(newNext);
            conv.setDialogue(user.getDialoguesConversation(dialogue_id)); //cogemos los dialogos de la conversacion actualizados
            return Optional.of(conv);
        }else{
            return Optional.empty();
        }
    }

    private void peticion_grpc(String user_id, String dialogue_id, String prompt, long timestamp, String token){
        //TODO Hacer la petición gRPC, ahora responde siempre HOlaaaaa
        //String answer = "Holaaaaa";
        PromptRequest request = PromptRequest.newBuilder()
                .setPrompt(prompt)
                .setToken(token)
                .build();
        PromptResponse response;
        
        response = blockingStub.prompt(request);
        
        Dialogue dialogue = new Dialogue(prompt, response.getAnswer(), timestamp);
        logger.info("Estoy en peticion_grpc y voy a entrar en getUserbyID");
        User user = dao.getUserById(user_id).get();
        user.addDialogueConversation(dialogue_id, dialogue);
        logger.info("Estoy en peticion_grpc y voy a entrar en updateUser");
        user.setDialogueStatus(dialogue_id, Status.READY);
        logger.info("Estoy en peticion_grpc y voy a entrar en updateUser");
        dao.updateUser(user);
    }

    public Boolean endConversation(String user_id, String dialogue_id, String url, String auth_token){
        // Comprobamos si está el usuario
        logger.info("Estoy en endConversation y voy a entrar en getUserbyID");
        Optional<User> u = dao.getUserById(user_id);
        if (u.isEmpty()){
            return false;
        }
        User user = u.get();
        //Verificamos el token
        if(!checkToken(user.getId(), user.getToken(), url, auth_token)){
            return false;
        }
        user.setDialogueStatus(dialogue_id, Status.FINISHED);
        dao.updateUser(user);
        return true;
    }

    public void test(){
        logger.info("Estoy en test");
        PromptRequest request = PromptRequest.newBuilder()
                .setPrompt("Hola")
                .setToken("pepe")
                .build();
        PromptResponse response;

        try{
            response = blockingStub.prompt(request);
        }catch (StatusRuntimeException e){
            logger.log(java.util.logging.Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Answer: " + response.getAnswer());
    }

    public void delete(String id){
        logger.info("Estoy en delete");
        dao.deleteUser(id);
        logger.info("Usuario eliminado");
    }
}
