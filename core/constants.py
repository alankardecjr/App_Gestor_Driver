"""Limiares de classificacao por valor obtido por quilometro.

Os valores funcionam como referencia de rentabilidade para apoiar a
decisao do motorista sobre aceitar ou recusar corridas.
"""

CLASSIFICACAO_LIMITES_R_POR_KM = {
	"EXCELENTE": 2.00,
	"BOA": 1.60,
	"REGULAR": 1.20,
	"BAIXA": 1.20,
}

CLASSIFICACAO_CORES = {
	"EXCELENTE": "#2E7D32",
	"BOA": "#F9A825",
	"REGULAR": "#EF6C00",
	"BAIXA": "#EF6C00",
	"RUIM": "#C62828",
}

CLASSIFICACAO_COR_BORDA_NEUTRA = "#607D8B"