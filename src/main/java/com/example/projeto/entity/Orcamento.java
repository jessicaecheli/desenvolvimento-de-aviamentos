package com.example.projeto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desenvolvimento_id", nullable = false)
    private Desenvolvimento desenvolvimento;

    private String fornecedor;

    private String tamanho;
    private String tamanho2;
    private String tamanho3;

    @Column(precision = 19, scale = 2)
    private BigDecimal valor;

    private Integer quantidade;

    private String observacao;

    public Orcamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Desenvolvimento getDesenvolvimento() { return desenvolvimento; }
    public void setDesenvolvimento(Desenvolvimento desenvolvimento) { this.desenvolvimento = desenvolvimento; }
    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }
    public String getTamanho() { return tamanho; }
    public void setTamanho(String tamanho) { this.tamanho = tamanho; }
    public String getTamanho2() { return tamanho2; }
    public void setTamanho2(String tamanho2) { this.tamanho2 = tamanho2; }
    public String getTamanho3() { return tamanho3; }
    public void setTamanho3(String tamanho3) { this.tamanho3 = tamanho3; }
    public String getTamanhosConcatenados() {
        StringBuilder sb = new StringBuilder();
        if (tamanho  != null && !tamanho.isBlank())  sb.append(tamanho);
        if (tamanho2 != null && !tamanho2.isBlank()) { if (!sb.isEmpty()) sb.append(", "); sb.append(tamanho2); }
        if (tamanho3 != null && !tamanho3.isBlank()) { if (!sb.isEmpty()) sb.append(", "); sb.append(tamanho3); }
        return sb.toString();
    }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
