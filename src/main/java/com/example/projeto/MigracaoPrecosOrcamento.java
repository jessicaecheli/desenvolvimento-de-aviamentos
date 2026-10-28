package com.example.projeto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converte os preços antigos do orçamento (tamanho/tamanho2/tamanho3 + valor/valor2/valor3)
 * para a grade dinâmica (tabela orcamento_preco). Regra combinada com a usuária:
 * - se todos os "tamanhos" preenchidos forem só números (ex.: 500, 1000) → viram faixas de quantidade;
 * - caso contrário → viram colunas de tamanho.
 * Só converte orçamentos que ainda não têm nenhum preço na grade (idempotente).
 * As colunas antigas não são apagadas.
 */
@Component
@Order(1)
public class MigracaoPrecosOrcamento implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracaoPrecosOrcamento.class);

    private final JdbcTemplate jdbc;

    public MigracaoPrecosOrcamento(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer colunasAntigas = jdbc.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' " +
            "AND TABLE_NAME = 'ORCAMENTO' AND COLUMN_NAME IN ('TAMANHO','TAMANHO2','TAMANHO3','VALOR','VALOR2','VALOR3')",
            Integer.class);
        if (colunasAntigas == null || colunasAntigas < 6) return;   // banco novo, nada a converter

        List<Map<String, Object>> pendentes = jdbc.queryForList(
            "SELECT o.ID, o.TAMANHO, o.TAMANHO2, o.TAMANHO3, o.VALOR, o.VALOR2, o.VALOR3 FROM ORCAMENTO o " +
            "WHERE NOT EXISTS (SELECT 1 FROM ORCAMENTO_PRECO p WHERE p.ORCAMENTO_ID = o.ID) " +
            "AND (COALESCE(o.TAMANHO, o.TAMANHO2, o.TAMANHO3) IS NOT NULL " +
            "     OR COALESCE(o.VALOR, o.VALOR2, o.VALOR3) IS NOT NULL)");
        if (pendentes.isEmpty()) return;

        int convertidos = 0, comoFaixa = 0;
        for (Map<String, Object> o : pendentes) {
            Long id = ((Number) o.get("ID")).longValue();
            List<String> tams = new ArrayList<>();
            List<BigDecimal> vals = new ArrayList<>();
            for (String sufixo : new String[]{"", "2", "3"}) {
                String t = (String) o.get("TAMANHO" + sufixo);
                BigDecimal v = (BigDecimal) o.get("VALOR" + sufixo);
                t = (t == null || t.isBlank()) ? null : t.trim();
                if (t == null && v == null) continue;
                tams.add(t);
                vals.add(v);
            }
            if (tams.isEmpty()) continue;

            boolean soNumeros = tams.stream().filter(t -> t != null).count() > 0
                && tams.stream().allMatch(t -> t == null || numero(t) != null);

            for (int i = 0; i < tams.size(); i++) {
                if (soNumeros) {
                    // faixas de quantidade: uma única coluna sem tamanho
                    jdbc.update("INSERT INTO ORCAMENTO_PRECO (ORCAMENTO_ID, COLUNA_TAMANHO, TAMANHO, QUANTIDADE, VALOR) VALUES (?,?,?,?,?)",
                        id, 0, null, tams.get(i) == null ? null : numero(tams.get(i)), vals.get(i));
                } else {
                    // tamanhos: uma coluna por tamanho, sem faixa de quantidade
                    jdbc.update("INSERT INTO ORCAMENTO_PRECO (ORCAMENTO_ID, COLUNA_TAMANHO, TAMANHO, QUANTIDADE, VALOR) VALUES (?,?,?,?,?)",
                        id, i, tams.get(i), null, vals.get(i));
                }
            }
            convertidos++;
            if (soNumeros) comoFaixa++;
        }
        if (convertidos > 0) {
            log.info("Orçamentos convertidos para a grade de preços: {} ({} como faixas de quantidade)", convertidos, comoFaixa);
        }
    }

    /** "1.000", "500UN", "2000 un" → número; texto com letras → null. */
    private static Integer numero(String t) {
        String s = t.toUpperCase().replace(".", "").replace("UN", "").replace(" ", "");
        if (!s.matches("\\d{1,9}")) return null;
        return Integer.valueOf(s);
    }
}
