package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Status;

public class ConversationDTOUtils {
    public static Status stringToStatus(String status) throws Exception{
        if(status.equals("ready")){
            return Status.READY;
        }else if (status.equals("busy")){
            return Status.BUSY;
        }else if (status.equals("finished")){
            return Status.FINISHED;
        }else{
            throw new Exception("Status not found");
        }
    }

    public static String statusToString (Status status) throws Exception{
        if(status == Status.READY){
            return "ready";
        }else if (status == Status.BUSY){
            return "busy";
        }else if (status == Status.FINISHED){
            return "finished";
        }else{
            throw new Exception("Status not found");
        }
    }

    public static ConversationDTO toDTO (Conversation conversation){
        return new ConversationDTO(conversation.getDialogue_id(), conversation.getStatus(), conversation.getDialogue(), conversation.getNext(), conversation.getEnd());
    }
}
