# Relatório — UI, usabilidade e acessibilidade

**Data:** 02/09/2026  
**No ar:** Beta `1.1.10` (UI congelada em 02/09/2026)  
**Rascunho:** Vs 2.0  
**Inspiração:** `C:\Users\akard\Pictures\Gestor drive\Inspiração`

Cópia de trabalho também em: `Documentos\Bloco de notas\Relatorio_UI_Gestor_Driver_Beta_vs_2.0.txt`

---

## 1. Por que a UI atual teve feedback ruim

A compacta trata cinco números com o mesmo peso (R$/km, valor, dist., tempo, nota) e emojis coloridos. Na rua o olho não acha o número da decisão.

A expandida é um segundo cartão grande no topo; Histórico e Config colam embaixo. Cobre o mapa e o Recusar da 99.

O selo é pequeno. O histórico em 7 colunas, fonte 9, parece planilha. O 🆗 da acessibilidade não ensina o caminho no Android (configurações restritas → serviços instalados).

## 2. Critérios

Leitura em ~2 s; não tapar Aceitar/Recusar; um número herói; contraste; cor da classificação não é o único sinal; alvos grandes; o Gestor não aceita a corrida; histórico só no aceite; notificação do Android não é tela cheia.

## 3. O que fica

**Beta:** selo como casa; não aceitar; histórico só no aceite; borda = classificação; combustível atual; permissões + tutorial; mapa quando houver endereço.

**2.0:** notificação permanente (Abrir / Desligar); compacta 3 linhas só na oferta; selo maior + X na base; menu no lugar da expandida; Dashboard só botão Pro; tema escuro/claro/celular; distâncias/custos da oferta na notificação e na compacta, não no menu.

**Inspiração (ideia, não clone):** número herói; mini-card no mapa; histórico em cards; config em lista + Ajuda; interruptor por app; fundo escuro e acento verde-limão; linha do tempo do endereço.

## 4. O que é ruim / não copiar

Não copiar: Aceitar no Gestor; overlay lotado; semáforo % e custo operacional (Pro); dashboard completo agora.

Ruim na 2.0 crua: card inteiro na notificação; compacta cobrindo Recusar por 2–4 s; “Consumo” vs “$/Gastos”; esconder destino se houver parada; menu abrindo o formulário antigo; detalhe sumir após 4 s sem resumo na notificação.

## 5. Telas-alvo (quando autorizar código)

- **Notificação:** uma linha de resumo + Abrir / Desligar; Embarque/Destino só com endereço.
- **Compacta:** mini-card, borda 5 dp, valores grandes, fora da zona Recusar; 2 s / 4 s; toque fora não recolhe.
- **Selo:** maior; toque = menu; X = esconde overlay, monitoramento segue.
- **Menu:** Histórico · Calibrar · Custos · Usuário · Dashboard (Pro) · Configurações · Fechar.
- **Histórico:** cards por corrida, abas Uber / 99 / inDrive.
- **Config:** lista + Ajuda; tema na aba APP.

## 6. Recomendação

Seguir a 2.0 na hierarquia e no menu. Não clonar o app da Play Store. Não jogar a expandida atual na barra de notificação.

Ordem sugerida: compacta herói → notificação magra → menu + X → histórico cards → config lista + tema → botão Pro.

**Retorno:** Beta `1.1.10` em `main` (`9226e2f`). Docs de freeze ainda locais, sem commit.
