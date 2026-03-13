package com.example.projeto.dto;

import com.example.projeto.entity.StatusDesenvolvimento;

public class DesenvolvimentoFiltroDTO {

    private Long colecaoId;
    private Long marcaId;
    private Long categoriaId;
    private StatusDesenvolvimento status;

    public Long getColecaoId() { return colecaoId; }
    public void setColecaoId(Long colecaoId) { this.colecaoId = colecaoId; }
    public Long getMarcaId() { return marcaId; }
    public void setMarcaId(Long marcaId) { this.marcaId = marcaId; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public StatusDesenvolvimento getStatus() { return status; }
    public void setStatus(StatusDesenvolvimento status) { this.status = status; }
}
