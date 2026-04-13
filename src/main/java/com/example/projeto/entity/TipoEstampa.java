package com.example.projeto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_estampa")
public class TipoEstampa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    public TipoEstampa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
