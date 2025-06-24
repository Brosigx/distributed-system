package es.um.sisdist.backend.dao.models;

public class Dialogue {
    String prompt;
    String answer;
    long timestamp;

    public Dialogue(String prompt, String answer, long timestamp) {
        this.prompt = prompt;
        this.answer = answer;
        this.timestamp = timestamp;
    }
    public Dialogue(){

    }

    /**
     * @return La respuesta del diálogo
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * @param answer Establece la respuesta del diálogo
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * @return El prompt del diálogo
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * @param prompt Establece el prompt del diálogo
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * @return El timestamp del diálogo
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp Establece el timestamp del diálogo
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "prompt: " + this.prompt + ", answer: " + this.answer + ", timestamp: " + this.timestamp;
    }



}
