package com.example.projeto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Padrão: valores em reais com 4 casas decimais.
 * O ddl-auto=update do Hibernate não altera a escala de colunas já existentes,
 * então este ajuste amplia para 4 casas as colunas em reais que ainda estiverem com menos.
 * Aumentar a escala não altera os valores gravados (12,50 passa a 12,5000). Idempotente.
 */
@Component
@Order(0)
public class AjusteCasasDecimaisReais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AjusteCasasDecimaisReais.class);
    private static final int ESCALA = 4;

    /** tabela -> colunas em reais */
    private static final Map<String, List<String>> COLUNAS_REAIS = Map.of(
        "DESENVOLVIMENTO", List.of("PRECO_TARGET", "PRECO_INICIAL", "PRECO_FINAL"),
        "ETAPA_DESENVOLVIMENTO", List.of("CUSTO_AMOSTRA", "VALOR_FRETE", "VALOR_AMOSTRA"),
        "ORCAMENTO", List.of("VALOR", "VALOR2", "VALOR3"),
        // preços em dólar e cotação: a tela já aceita 4 casas, mas a coluna guardava só 2 (arredondava)
        "DADOS_TECIDO", List.of("PRECO", "PRECO_NEGOCIADO",
                                "PRECO_EM_DOLAR", "PRECO_NEGOCIADO_EM_DOLAR", "VALOR_DOLAR")
    );

    private final JdbcTemplate jdbc;

    public AjusteCasasDecimaisReais(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        COLUNAS_REAIS.forEach((tabela, colunas) -> colunas.forEach(coluna -> {
            List<Map<String, Object>> info = jdbc.queryForList(
                "SELECT NUMERIC_PRECISION, NUMERIC_SCALE FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ? AND COLUMN_NAME = ?", tabela, coluna);
            if (info.isEmpty()) return;
            int precisao = ((Number) info.get(0).get("NUMERIC_PRECISION")).intValue();
            int escala = ((Number) info.get(0).get("NUMERIC_SCALE")).intValue();
            if (escala >= ESCALA) return;
            int novaPrecisao = Math.max(precisao + (ESCALA - escala), 19);
            jdbc.execute("ALTER TABLE " + tabela + " ALTER COLUMN " + coluna +
                " SET DATA TYPE NUMERIC(" + novaPrecisao + "," + ESCALA + ")");
            log.info("Coluna {}.{} ajustada para {} casas decimais", tabela, coluna, ESCALA);
        }));
    }
}
