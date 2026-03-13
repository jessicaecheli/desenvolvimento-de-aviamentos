package com.example.projeto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "prazo_dias_uteis", nullable = false)
    private Integer prazoDiasUteis;

    public Categoria() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getPrazoDiasUteis() { return prazoDiasUteis; }
    public void setPrazoDiasUteis(Integer prazoDiasUteis) { this.prazoDiasUteis = prazoDiasUteis; }
}
