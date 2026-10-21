package com.example.projeto.entity;

/**
 * Motivo de uma etapa ALTERACAO — define o efeito no prazo da amostra.
 * ESTILO: o estilo mudou o aviamento → o prazo reinicia a partir da data da alteração.
 * FORNECEDOR: o fornecedor enviou com divergência → o prazo original é mantido.
 * Alterações antigas (sem motivo gravado) são tratadas como FORNECEDOR.
 */
public enum MotivoAlteracao {
    ESTILO("Alteração de estilo", "Estilo"),
    FORNECEDOR("Divergência do fornecedor", "Fornecedor");

    private final String label;
    private final String labelCurto;

    MotivoAlteracao(String label, String labelCurto) {
        this.label = label;
        this.labelCurto = labelCurto;
    }

    public String getLabel() { return label; }
    public String getLabelCurto() { return labelCurto; }
}
