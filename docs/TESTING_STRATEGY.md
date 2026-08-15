# Estratégia de testes

## Objetivo

Garantir confiabilidade na lógica de cálculo e na experiência do usuário.

## Prioridades

### Testes unitários
- validar cálculo de distância;
- validar cálculo de valor por quilômetro;
- validar classificação de rentabilidade;
- validar regras de negócio básicas.

### Testes de interface
- validar telas principais;
- validar navegação inicial;
- validar comportamento de componentes.

## Recomendações

- começar com testes de regras centrais;
- expandir gradualmente para interface e fluxo do usuário;
- manter os testes simples e objetivos.

## Cobertura atual (Sprint 2)

- testes unitarios do pipeline de notificacoes:
	- extracao de valor, distancia e tempo;
	- parse por plataforma suportada;
	- falha explicita para plataforma nao suportada.

## Execucao dos testes Python

```bash
python -m unittest discover -s tests -p "test_*.py"
```
