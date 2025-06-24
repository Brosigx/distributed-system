package es.um.sisdist.backend.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.ConversationDTO;
import es.um.sisdist.models.ConversationDTOUtils;
import es.um.sisdist.models.DialogueDTO;
import es.um.sisdist.models.DialogueDTOUtils;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/u")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();
    private Logger logger = Logger.getLogger("UsersEndpoint");

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }

    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signUp(UserDTO udto){
        logger.info("Sign up petition received. Email: " + udto.getEmail() + "\nName: " + udto.getName() + "\nPassword: " + udto.getPassword() + "\n");
        Optional<User> u = impl.registerUser(udto.getEmail(), udto.getName(), udto.getPassword()); //Usamos el email 
        if(u.isEmpty()){
            throw new WebApplicationException("Email Registered. Conflict", Response.Status.CONFLICT); 
        }
        User user = u.get();
        return Response.status(Response.Status.CREATED).entity(UserDTOUtils.toDTO(user)).build();
    }

    @GET
    @Path("/{id}/dialogue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConversationIds(@PathParam("id") String user_id){
        Optional<List<String>> conversations = impl.getConversationIds(user_id);
        if(conversations.isEmpty()){
            throw new WebApplicationException("Conversations not found.", Response.Status.NOT_FOUND);  
        }
        return Response.status(Response.Status.OK).entity(conversations.get()).build();
    }

    @POST
    @Path("/{id}/dialogue/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConversation (@PathParam("id") String user_id, @HeaderParam("Auth-Token") String auth_token, @Context UriInfo url, ConversationDTO cdto){
        logger.info("dialogue_id: " + cdto.getDialogue_id());
        logger.info("user_id: " + user_id);
        logger.info("url: " + url.getAbsolutePath().toString());
        Optional<Conversation> dialogue = impl.createConversation(cdto.getDialogue_id(), user_id, url.getAbsolutePath().toString(), auth_token);
        if(dialogue.isEmpty()){
            throw new WebApplicationException("Error creating conversation", Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.CREATED).entity(ConversationDTOUtils.toDTO(dialogue.get())).build();
    }

    @POST
    @Path("{id}/dialogue/{dialogue_id}/delete")
    public Response deleteConversation(@PathParam("id") String user_id, @HeaderParam("Auth-Token") String auth_token, @PathParam("dialogue_id") String dialogue_id, @Context UriInfo url){
        logger.info("dialogue_id: " + dialogue_id);
        logger.info("user_id: " + user_id);
        logger.info("auth_token: " + auth_token);
        boolean is_deleted = impl.deleteConversation(dialogue_id, user_id, url.getAbsolutePath().toString(), auth_token);
        if(!is_deleted){
            throw new WebApplicationException("Error deleting conversation", Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("{id}/dialogue/{dialogue_id}/next/{next_token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response nextPrompt(@PathParam("id") String user_id,
                                 @PathParam("dialogue_id") String dialogue_id,
                                  @PathParam("next_token") String next_token,
                                   @HeaderParam("Auth-Token") String auth_token,
                                    DialogueDTO cdto,
                                     @Context UriInfo url){
        logger.info("dialogue_id: " + dialogue_id);
        logger.info("user_id: " + user_id);
        logger.info("token: " + next_token);
        Optional<String> location = impl.nextPrompt(user_id, dialogue_id, next_token, cdto.getPrompt(), cdto.getTimestamp(), url.getAbsolutePath().toString(), auth_token);
        if(location.isEmpty()){
            throw new WebApplicationException("Error getting next conversation", Response.Status.NO_CONTENT);
        }
        Response response = Response.status(Response.Status.ACCEPTED)
                .header("Location", location.get())
                .build();
        return response;
    }

    @POST
    @Path("{id}/dialogue/{dialogue_id}/end")
    public Response endConversation(@PathParam("id") String user_id, @PathParam("dialogue_id") String dialogue_id, @Context UriInfo url, @HeaderParam("Auth-Token") String auth_token){
        logger.info("dialogue_id: " + dialogue_id);
        logger.info("user_id: " + user_id);
        boolean is_deleted = impl.endConversation(user_id, dialogue_id, url.getAbsolutePath().toString(), auth_token);
        if(!is_deleted){
            throw new WebApplicationException("Error ending conversation", Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("{id}/dialogue/{dialogue_id}/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConversation(@PathParam("id") String user_id, @PathParam("dialogue_id") String dialogue_id, @HeaderParam("Auth-Token") String auth_token, @Context UriInfo url, @PathParam("token") String next_token){
        logger.info("dialogue_id: " + dialogue_id);
        logger.info("user_id: " + user_id);
        //logger.info("token: " + token);
        Optional<Conversation> dialogue = impl.getConversation(user_id, dialogue_id, next_token, url.getAbsolutePath().toString(), auth_token);
        if(dialogue.isEmpty()){
            throw new WebApplicationException("Error getting conversation", Response.Status.NO_CONTENT);
        }
        return Response.status(Response.Status.OK).entity(ConversationDTOUtils.toDTO(dialogue.get())).build();
    }

    @POST
    @Path("test")
    public void test(){
        logger.info("Test endpoint");
        impl.test();
    }

    @POST
    @Path("delete/{id}")
    public void deleteUser(@PathParam("id") String id){
        logger.info("Delete user endpoint");
        impl.delete(id);
    }

}
