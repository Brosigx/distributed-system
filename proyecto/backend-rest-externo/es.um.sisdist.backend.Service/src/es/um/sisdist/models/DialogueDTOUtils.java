package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Dialogue;

public class DialogueDTOUtils {
    public static DialogueDTO toDTO(Dialogue dialogue) {
        DialogueDTO dto = new DialogueDTO();
        dto.setPrompt(dialogue.getPrompt());
        dto.setAnswer(dialogue.getAnswer());
        dto.setTimestamp(dialogue.getTimestamp());
        return dto;
    }
}
