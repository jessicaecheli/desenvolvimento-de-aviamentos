package com.example.projeto.service;

import com.example.projeto.entity.Categoria;
import com.example.projeto.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> listarTodas() {
        return repository.findAll();
    }

    public List<Categoria> listarMasters() {
        return repository.findByCategoriaPaiIsNull();
    }

    public List<Categoria> listarSubcategorias() {
        return repository.findByCategoriaPaiIsNotNullOrderByCategoriaPaiNomeAscNomeAsc();
    }

    public List<Categoria> listarSubcategoriasPorMaster(Long masterId) {
        return repository.findByCategoriaPaiIdOrderByNomeAsc(masterId);
    }

    public List<Categoria> listarPorMaster(Long masterId) {
        return repository.findByCategoriaPaiId(masterId);
    }

    public Categoria buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + id));
    }

    public Categoria salvar(Categoria categoria) {
        Long excludeId = categoria.getId() != null ? categoria.getId() : 0L;
        if (repository.existsByNomeIgnoreCaseAndIdNot(categoria.getNome(), excludeId)) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome.");
        }
        return repository.save(categoria);
    }

    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        if (categoria.isMaster()) {
            throw new IllegalArgumentException("Categorias master (AVIAMENTOS, TECIDOS) não podem ser excluídas.");
        }
        repository.deleteById(id);
    }
}
