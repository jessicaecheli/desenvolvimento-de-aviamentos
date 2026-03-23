package com.example.projeto.service;

import com.example.projeto.entity.Fornecedor;
import com.example.projeto.repository.FornecedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    public List<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Fornecedor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado: " + id));
    }

    public Fornecedor salvar(Fornecedor fornecedor) {
        Long excludeId = fornecedor.getId() != null ? fornecedor.getId() : 0L;
        if (repository.existsByNomeIgnoreCaseAndIdNot(fornecedor.getNome(), excludeId)) {
            throw new IllegalArgumentException("Já existe um fornecedor com este nome.");
        }
        return repository.save(fornecedor);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
