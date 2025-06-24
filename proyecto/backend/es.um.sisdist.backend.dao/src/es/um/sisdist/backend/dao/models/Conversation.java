package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    String dialogue_id;
    Status status;
    List<Dialogue> dialogue;
    String next;
    String end;
    
    public Conversation(String dialogue_id, Status status, List<Dialogue> dialogue, String next, String end) {
        this.dialogue_id = dialogue_id;
        this.status = status;
        this.dialogue = dialogue;
        this.next = next;
        this.end = end;
    }

    public Conversation(String dialogue_id, String next, String end){
        this.dialogue_id = dialogue_id;
        this.status = Status.READY;
        this.dialogue = new ArrayList<>();
        this.next = next;
        this.end = end;
    }

    public Conversation(){

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
    public void addDialogue(Dialogue dialogue) {
        this.dialogue.add(dialogue);
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
    public String toString(){
        String str = "";
        for(Dialogue d : this.dialogue){
            str += "{" + d.toString() + "}";
        }
        return str;
    }
}
