package com.example.projeto.service;

import com.example.projeto.entity.Marca;
import com.example.projeto.repository.MarcaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarcaService {

    private final MarcaRepository repository;

    public MarcaService(MarcaRepository repository) {
        this.repository = repository;
    }

    public List<Marca> listarTodas() {
        return repository.findAll();
    }

    public Marca buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Marca não encontrada: " + id));
    }

    public Marca salvar(Marca marca) {
        Long excludeId = marca.getId() != null ? marca.getId() : 0L;
        if (repository.existsByNomeIgnoreCaseAndIdNot(marca.getNome(), excludeId)) {
            throw new IllegalArgumentException("Já existe uma marca com este nome.");
        }
        return repository.save(marca);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
