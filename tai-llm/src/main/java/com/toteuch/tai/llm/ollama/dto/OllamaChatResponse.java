package com.toteuch.tai.llm.ollama.dto;

public class OllamaChatResponse {
    private String model;
    private OllamaMessage message;
    private Boolean done;
    private Integer prompt_eval_count;
    private Integer eval_count;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public OllamaMessage getMessage() {
        return message;
    }

    public void setMessage(OllamaMessage message) {
        this.message = message;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Integer getPrompt_eval_count() {
        return prompt_eval_count;
    }

    public void setPrompt_eval_count(Integer v) {
        this.prompt_eval_count = v;
    }

    public Integer getEval_count() {
        return eval_count;
    }

    public void setEval_count(Integer v) {
        this.eval_count = v;
    }
}
