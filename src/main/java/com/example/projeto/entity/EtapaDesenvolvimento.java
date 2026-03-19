package com.example.projeto.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "etapa_desenvolvimento")
public class EtapaDesenvolvimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desenvolvimento_id", nullable = false)
    private Desenvolvimento desenvolvimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private TipoEtapa tipo;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia = LocalDate.now();

    private String observacao;

    @Column(name = "numero_rodada")
    private Integer numeroRodada = 1;

    public EtapaDesenvolvimento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Desenvolvimento getDesenvolvimento() { return desenvolvimento; }
    public void setDesenvolvimento(Desenvolvimento desenvolvimento) { this.desenvolvimento = desenvolvimento; }
    public TipoEtapa getTipo() { return tipo; }
    public void setTipo(TipoEtapa tipo) { this.tipo = tipo; }
    public LocalDate getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDate dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Integer getNumeroRodada() { return numeroRodada; }
    public void setNumeroRodada(Integer numeroRodada) { this.numeroRodada = numeroRodada; }
}
