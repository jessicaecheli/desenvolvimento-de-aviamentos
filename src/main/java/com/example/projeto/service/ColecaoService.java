package com.example.projeto.service;

import com.example.projeto.entity.Colecao;
import com.example.projeto.repository.ColecaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColecaoService {

    private final ColecaoRepository repository;

    public ColecaoService(ColecaoRepository repository) {
        this.repository = repository;
    }

    public List<Colecao> listarTodas() {
        return repository.findAll();
    }

    public Colecao buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Coleção não encontrada: " + id));
    }

    public Colecao salvar(Colecao colecao) {
        Long excludeId = colecao.getId() != null ? colecao.getId() : 0L;
        if (repository.existsByNomeIgnoreCaseAndAnoAndIdNot(colecao.getNome(), colecao.getAno(), excludeId)) {
            throw new IllegalArgumentException("Já existe uma coleção com este nome e ano.");
        }
        return repository.save(colecao);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
