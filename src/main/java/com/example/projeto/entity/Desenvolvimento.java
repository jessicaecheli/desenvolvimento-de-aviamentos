package com.example.projeto.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "desenvolvimento")
public class Desenvolvimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private String descricao;

    private String codigoSystextil1;
    private String codigoSystextil2;
    private String codigoSystextil3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colecao_id")
    private Colecao colecao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private StatusDesenvolvimento status = StatusDesenvolvimento.ORCAMENTO;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao = LocalDate.now();

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal precoInicial;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal precoFinal;

    private Integer qtdCompraMostruario;

    @OneToMany(mappedBy = "desenvolvimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataOcorrencia ASC, id ASC")
    private List<EtapaDesenvolvimento> etapas = new ArrayList<>();

    @OneToMany(mappedBy = "desenvolvimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Orcamento> orcamentos = new ArrayList<>();

    public Desenvolvimento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCodigoSystextil1() { return codigoSystextil1; }
    public void setCodigoSystextil1(String codigoSystextil1) { this.codigoSystextil1 = codigoSystextil1; }
    public String getCodigoSystextil2() { return codigoSystextil2; }
    public void setCodigoSystextil2(String codigoSystextil2) { this.codigoSystextil2 = codigoSystextil2; }
    public String getCodigoSystextil3() { return codigoSystextil3; }
    public void setCodigoSystextil3(String codigoSystextil3) { this.codigoSystextil3 = codigoSystextil3; }
    public Marca getMarca() { return marca; }
    public void setMarca(Marca marca) { this.marca = marca; }
    public Colecao getColecao() { return colecao; }
    public void setColecao(Colecao colecao) { this.colecao = colecao; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public StatusDesenvolvimento getStatus() { return status; }
    public void setStatus(StatusDesenvolvimento status) { this.status = status; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }
    public List<EtapaDesenvolvimento> getEtapas() { return etapas; }
    public void setEtapas(List<EtapaDesenvolvimento> etapas) { this.etapas = etapas; }
    public List<Orcamento> getOrcamentos() { return orcamentos; }
    public void setOrcamentos(List<Orcamento> orcamentos) { this.orcamentos = orcamentos; }
    public java.math.BigDecimal getPrecoInicial() { return precoInicial; }
    public void setPrecoInicial(java.math.BigDecimal precoInicial) { this.precoInicial = precoInicial; }
    public java.math.BigDecimal getPrecoFinal() { return precoFinal; }
    public void setPrecoFinal(java.math.BigDecimal precoFinal) { this.precoFinal = precoFinal; }
    public Integer getQtdCompraMostruario() { return qtdCompraMostruario; }
    public void setQtdCompraMostruario(Integer qtdCompraMostruario) { this.qtdCompraMostruario = qtdCompraMostruario; }
}
