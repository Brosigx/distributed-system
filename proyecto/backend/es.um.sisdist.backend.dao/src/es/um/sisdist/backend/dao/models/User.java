/**
 *
 */
package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import es.um.sisdist.backend.dao.models.utils.UserUtils;

public class User
{
    private String id;
    private String email;
    private String password_hash;
    private String name;
    private List<Conversation> dialogues;
    private String token;
    private static final Logger logger = Logger.getLogger(User.class.getName());
    private int visits;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String uid)
    {
        this.id = uid;
    }

    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(final String email)
    {
        this.email = email;
    }

    /**
     * @return the password_hash
     */
    public String getPassword_hash()
    {
        return password_hash;
    }

    /**
     * @param password_hash the password_hash to set
     */
    public void setPassword_hash(final String password_hash)
    {
        this.password_hash = password_hash;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * @return the TOKEN
     */
    public String getToken()
    {
        return token;
    }

    /**
     * @param tOKEN the tOKEN to set
     */
    public void setToken(final String TOKEN)
    {
        this.token = TOKEN;
    }

    public List<Conversation> getDialogues() {
        return dialogues;
    }

    public void setDialogues(List<Conversation> dialogues) {
        this.dialogues = dialogues;
    }

    public void addConversation(Conversation c){
        this.dialogues.add(c);
    }

    public boolean deleteConversation(String id){
        return this.dialogues.removeIf(d -> d.dialogue_id.equals(id) );
    }

    public List<String> getDialoguesIds(){
        return dialogues.stream()
                .map(Conversation::getDialogue_id)
                .collect(Collectors.toList());
    }

    /**
     * @return the visits
     */
    public int getVisits()
    {
        return visits;
    }

    /**
     * @param visits the visits to set
     */
    public void setVisits(final int visits)
    {
        this.visits = visits;
    }

    public User(String email, String password_hash, String name, String tOKEN, int visits)
    {
        this(email, email, password_hash, name, tOKEN, visits);
        this.id = UserUtils.md5pass(email);
    }

    public User(String id, String email, String password_hash, String name, String tOKEN, int visits)
    {
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.name = name;
        this.dialogues = new ArrayList<>();
        token = tOKEN;
        this.visits = visits;
    }

    public User(String id, String email, String password_hash, String name, List<Conversation> dialogues, String tOKEN, int visits)
    {
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.name = name;
        this.dialogues = dialogues;
        token = tOKEN;
        this.visits = visits;
    }

    @Override
    public String toString()
    {
        return "User [id=" + id + ", email=" + email + ", password_hash=" + password_hash + ", name=" + name
                + ", TOKEN=" + token + ", visits=" + visits + "]";
    }

    public User()
    {
    }

    public String dialoguesToString(){
        String str = "";
        if (this.dialogues == null || this.dialogues.isEmpty()){
            return "No dialogues";
        }
        for(Conversation c: this.dialogues){
            str += "[" + c.getDialogue_id() + ",";
            str += c.getStatus()+ ",";
            str += c.getNext()+ ",";
            str += c.getEnd()+ ",";
            str += "#####" + c.toString() + "#####" + "]";
        }
        return str;
    }

    public Status dialogueStatus(String id){
        for(Conversation c: this.dialogues){
            if(c.getDialogue_id().equals(id)){
                return c.getStatus();
            }
        }
        return Status.FINISHED;
    }

    public void setDialogueStatus(String id, Status status){
        for(Conversation c: this.dialogues){
            if(c.getDialogue_id().equals(id)){
                c.setStatus(status);
            }
        }
    }

    public void setDialogueNext(String dialogue_id, String next){
        for(Conversation c: this.dialogues){
            if(c.getDialogue_id().equals(dialogue_id)){
                c.setNext(next);
            }
        }
    }

    public List<Dialogue> getDialoguesConversation(String dialogue_id){
        for(Conversation c: this.dialogues){
            if(c.getDialogue_id().equals(dialogue_id)){
                return c.getDialogue();
            }
        }
        return new ArrayList<>();
    }

    public void addDialogueConversation(String dialogue_id, Dialogue dialogue){
        logger.info("Dialogue_id: " + dialogue_id);
        for(Conversation c: this.dialogues){
            if(c.getDialogue_id().equals(dialogue_id)){
                logger.info("He entrado");
                c.addDialogue(dialogue);
            }
        }
    }
}