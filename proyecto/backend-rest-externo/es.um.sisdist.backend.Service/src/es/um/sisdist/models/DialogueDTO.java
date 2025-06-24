package es.um.sisdist.models;

public class DialogueDTO {
    String prompt;
    String answer;
    long timestamp;
    
    public DialogueDTO(String prompt, String answer, long timestamp) {
        this.prompt = prompt;
        this.answer = answer;
        this.timestamp = timestamp;
    }
    public DialogueDTO(){

    }
    public String getPrompt() {
        return prompt;
    }
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
}
