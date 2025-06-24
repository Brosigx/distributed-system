/**
 *
 */
package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static java.util.Arrays.toString;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.Collections;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.Status;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class MongoUserDAO implements IUserDAO
{
    private Logger logger = Logger.getLogger("MongoDB");
    private Supplier<MongoCollection<User>> collection;

    public MongoUserDAO()
    {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
            .conventions(asList(Conventions.ANNOTATION_CONVENTION))
            .register("es.um.sisdist.backend.dao.models") // Asegúrate de que este paquete sea correcto
            .automatic(true)
            .build();

        CodecRegistry pojoCodecRegistry = fromRegistries(
            getDefaultCodecRegistry(),
            fromProviders(pojoCodecProvider)
        );
        /* 
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().conventions(asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider)); , PojoCodecProvider.builder()
        .register("es.um.sisdist.backend.dao.models")
        .automatic(true)
        .build()));*/

        /* 
	    CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
            .conventions(Collections.singletonList(Conventions.ANNOTATION_CONVENTION))
	        .register("es.um.sisdist.backend.dao.models")
            .automatic(true)
            .build();

        CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
            getDefaultCodecRegistry(),
            org.bson.codecs.configuration.CodecRegistries.fromProviders(pojoCodecProvider)
        );
        */
        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://root:root@"
        		+ Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
                + ":27017/ssdd?authSource=admin";

        collection = Lazy.lazily(() ->
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
        		.withCodecRegistry(pojoCodecRegistry);
        	return database.getCollection("users", User.class);
        });
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        allUsers("He entrado en getUserById: id: " + id);
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String email)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("email", email)).first());
        return user;
    }

    @Override
    public Optional<User> registerUser(String id, String name, String email, String hash_password) {
        if(getUserById(id).isPresent()){ //Si el usuario ya está registrado lo devuelve vacío
            return Optional.empty();
        }
        User u = new User(id,email,hash_password,name, UUID.randomUUID().toString(),0);
        try{ // Añadimos el usuario a la base de datos
            collection.get().insertOne(u);
            //update(u);
            logger.info("New user registered: " + email + "\n\n Dialogues: \n\n" + getUserById(id).get().dialoguesToString());
            return Optional.of(u);
        }catch(MongoException e){
            e.printStackTrace();
            return Optional.empty();
        }

    }
   
    public Optional<User> updateUser(User user) {
        logger.info("Voy a actualizar el usuario siguiente: id:" + user.getId() + ", nombre: " + user.getName());
        collection.get().updateOne(eq("id", user.getId()), Updates.combine(
            Updates.set("email", user.getEmail()),
            Updates.set("password_hash", user.getPassword_hash()),
            Updates.set("name", user.getName()),
            Updates.set("token", user.getToken()),
            Updates.set("visits", user.getVisits()),
            Updates.set("dialogues", user.getDialogues())
        ));
        logger.info("He terminado de actualizar el usuario siguiente: id:" + user.getId() + ", nombre: " + user.getName());
        logger.info("Entro en getUserById después de actualizar el usuario");
        return getUserById(user.getId());
    }

    @Override
    public void deleteUser(String id){
        collection.get().deleteOne(eq("id", id));
        allUsers("Después de eliminar el usuario con id: " + id);
    }

    private void allUsers(String comment){
        FindIterable<User> iterable = collection.get().find();
        logger.info(comment + "(comentario inicial)");
        logger.info("Usuarios encontrados:");
        for(User u : iterable){
            logger.info("Usuario encontrado: id: " + u.getId() + ", nombre: " + u.getName());
            logger.info("Diálogos: " + u.dialoguesToString());
        }
    }


    @Override
    public Optional<List<String>> getConversationsIds(String user_id) {
        if(getUserById(user_id).isEmpty()){ // Si el usuario no existe se devuelve vacío
            return Optional.empty();
        }
        Optional<List<String>> conversationIds = Optional.ofNullable(collection.get().find(eq("id", user_id)).first().getDialoguesIds());
        return conversationIds;
    }
}


     /* 
    private List<Conversation> test_dialogues(){
        List<Conversation> dialogues = new ArrayList<>();
        List<Dialogue> prompts1 = new ArrayList<>();
        List<Dialogue> prompts2 = new ArrayList<>();
        Dialogue d1 = new Dialogue("hola1", "hola1", 1);
        Dialogue d2 = new Dialogue("hola2", "hola2", 2);
        Dialogue d3 = new Dialogue("hola3", "hola3", 3);
        Dialogue d4 = new Dialogue("hola4", "hola4", 4);
        Dialogue d5 = new Dialogue("hola5", "hola5", 5);
        Dialogue d6 = new Dialogue("hola6", "hola6", 6);
        Dialogue d7 = new Dialogue("hola7", "hola7", 7);
        prompts1.add(d1);
        prompts1.add(d2);
        prompts1.add(d3);
        prompts2.add(d4);
        prompts2.add(d5);
        prompts2.add(d6);
        prompts2.add(d7);
        Conversation c1 = new Conversation("chat1", Status.READY, prompts1, "/next/1", "/end/1");
        Conversation c2 = new Conversation("chat2", Status.READY, prompts2, "/next/2", "/end/2");
        dialogues.add(c1);
        dialogues.add(c2);
        return dialogues;
    }
    
    public Optional<User> updateUser(User user){
        Document update = new Document("$set", new Document("id", user.getId())
            .append("email", user.getEmail())
            .append("password_hash", user.getPassword_hash())
            .append("name", user.getName())
            .append("token", user.getToken())
            .append("visits", user.getVisits())
            .append("dialogues", user.getDialogues())
        );
        try{
            MongoCollection<User> collection = this.collection.get();
            collection.updateOne(eq("id", user.getId()), update);
            return getUserById(user.getId());
        } catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public Optional<User> updateUser(User user) {
    if (user == null || user.getId() == null) {
        logger.info("El usuario o su ID son nulos. No se puede actualizar.");
        return Optional.empty();
    }

    Bson updates = Updates.combine(
        Updates.set("id", user.getId()),
        Updates.set("email", user.getEmail()),
        Updates.set("password_hash", user.getPassword_hash()),
        Updates.set("name", user.getName()),
        Updates.set("token", user.getToken()),
        Updates.set("visits", user.getVisits()),
        Updates.set("dialogues", user.getDialogues())
    );

    try {
        MongoCollection<User> collection = this.collection.get();
        UpdateResult result = collection.updateOne(eq("id", user.getId()), updates);

        if (result.getMatchedCount() == 0) {
            logger.info("No se encontró ningún usuario con ID: " + user.getId());
            return Optional.empty();
        }

        logger.info("JFIQEPFMQOWEMFIQOWEMPFIQOWEPFQIWEOFMQIEOPUsuario actualizado correctamente: " + user.getId());
        User user_check = getUserById(user.getId()).get();
        logger.info("Usuario actualizado: " + user_check.getId() + ", nombre: " + user_check.getName());
        logger.info("Diálogos del usuario: " + user_check.dialoguesToString());
        return getUserById(user.getId());
    } catch (MongoException e) {
        logger.severe("Error al actualizar el usuario con ID: " + user.getId() + ". Detalles: " + e.getMessage());
        return Optional.empty();
    }
}*/
