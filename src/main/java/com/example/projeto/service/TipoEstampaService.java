package com.example.projeto.service;

import com.example.projeto.entity.TipoEstampa;
import com.example.projeto.repository.TipoEstampaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoEstampaService {

    private final TipoEstampaRepository repository;

    public TipoEstampaService(TipoEstampaRepository repository) {
        this.repository = repository;
    }

    public List<TipoEstampa> listarTodos() {
        return repository.findAllByOrderByNomeAsc();
    }

    public TipoEstampa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de estampa não encontrado: " + id));
    }

    public TipoEstampa salvar(TipoEstampa tipoEstampa) {
        Long excludeId = tipoEstampa.getId() != null ? tipoEstampa.getId() : 0L;
        if (repository.existsByNomeIgnoreCaseAndIdNot(tipoEstampa.getNome(), excludeId)) {
            throw new IllegalArgumentException("Já existe um tipo de tecido com este nome.");
        }
        return repository.save(tipoEstampa);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
