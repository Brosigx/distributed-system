package es.um.sisdist.models;

import java.util.List;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.Status;

public class ConversationDTO {
    String dialogue_id;
    Status status;
    List<Dialogue> dialogue;
    String next;
    String end;
    public ConversationDTO(String dialogue_id, Status status, List<Dialogue> dialogue, String next, String end) {
        this.dialogue_id = dialogue_id;
        this.status = status;
        this.dialogue = dialogue;
        this.next = next;
        this.end = end;
    }
    public ConversationDTO() {
    }
    public String getDialogue_id() {
        return dialogue_id;
    }
    public void setDialogue_id(String dialogue_id) {
        this.dialogue_id = dialogue_id;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public List<Dialogue> getDialogue() {
        return dialogue;
    }
    public void setDialogue(List<Dialogue> dialogue) {
        this.dialogue = dialogue;
    }
    public String getNext() {
        return next;
    }
    public void setNext(String next) {
        this.next = next;
    }
    public String getEnd() {
        return end;
    }
    public void setEnd(String end) {
        this.end = end;
    }
    
}
