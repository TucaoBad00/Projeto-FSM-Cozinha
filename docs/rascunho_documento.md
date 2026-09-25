# Trabalho I — Máquina de Estados (RASCUNHO do PDF)

> Revise e reescreva com suas palavras antes de montar o PDF — a arguição é sobre o que VOCÊ entende.
> Campos entre [colchetes] são para você preencher.

**Aluno(a):** [nome] — **Disciplina:** IA e Ilusão de Inteligência em Jogos — **Prof.ª** Marina de Lara

## 1. Visão geral do cenário

O cenário simula a cozinha de um pequeno restaurante durante um turno de trabalho. Dois funcionários,
um cozinheiro e um garçom, trabalham em conjunto: o garçom anota pedidos nas mesas e os deixa no balcão
da cozinha; o cozinheiro pega esses pedidos, prepara os pratos e os devolve ao balcão para que o garçom
os sirva. O cozinheiro também se cansa ao cozinhar e precisa de pausas. A simulação avança em "ticks"
(voltas do loop principal), e em cada tick cada agente executa um passo do seu estado atual. Todas as
regras são determinísticas (contadores, timers e limiares), então a mesma execução sempre gera o mesmo log.

## 2. Visão geral dos agentes

**Agente A — Cozinheiro (Bob).** Responsável por transformar pedidos em pratos. Ele fica parado esperando
até que um pedido apareça no balcão; então cozinha por alguns ticks, acumulando progresso no prato e
cansaço. Quando o prato fica pronto, ele o coloca no balcão e decide se continua trabalhando ou se precisa
descansar, dependendo do seu nível de cansaço.

**Agente B — Garçom (Juca).** Responsável pelo atendimento. Ele vai até uma mesa, leva um tempo fixo
anotando o pedido, deixa o pedido no balcão e espera o prato ficar pronto. Quando o prato aparece, ele o
leva até a mesa e volta a atender outro cliente.

**Comunicação (bônus).** Os agentes não acessam um ao outro diretamente: eles se comunicam por um objeto
compartilhado, o `Balcao`. O estado `EsperandoPedido` do cozinheiro só termina quando o garçom finaliza
`AnotandoPedido` (e deixa um pedido), e o estado `AguardandoPrato` do garçom só termina quando o cozinheiro
finaliza `Cozinhando` (e deixa um prato).

## 3. Diagramas de estados

[Inserir `docs/diagrama_cozinheiro.svg` e `docs/diagrama_garcom.svg` — se o editor não aceitar SVG,
abra no navegador e tire um print, ou exporte para PNG.]

## 4. Tabela de regras

### Agente A — Cozinheiro

| Estado | Entrada (`entrar`) | Execução (`executar`) | Saída (`sair`) | Transições |
|---|---|---|---|---|
| EsperandoPedido | Avisa que vai esperar pedido | Verifica o balcão | Pega o pedido do balcão | `balcao.temPedido()` → Cozinhando |
| Cozinhando | Zera `progressoPrato`; acende o fogão | `progressoPrato += 25`, `cansaco += 10` | Desliga o fogão | `progressoPrato ≥ 100`: coloca prato no balcão; se `cansaco ≥ 60` → Descansando, senão → EsperandoPedido |
| Descansando | Avisa que vai pausar | `cansaco -= 20` (mínimo 0) | Avisa que descansou | `cansaco == 0` → EsperandoPedido |

### Agente B — Garçom

| Estado | Entrada (`entrar`) | Execução (`executar`) | Saída (`sair`) | Transições |
|---|---|---|---|---|
| AnotandoPedido | Zera `tempoNaMesa`; vai à mesa | `tempoNaMesa += 1` | Coloca o pedido no balcão | `tempoNaMesa ≥ 2` → AguardandoPrato |
| AguardandoPrato | Avisa que vai aguardar | Verifica o balcão | Avisa que pegou o prato | `balcao.temPrato()` → ServindoPrato |
| ServindoPrato | Leva o prato à mesa | `pratosEntregues += 1` | Avisa que a mesa foi servida | Após 1 tick → AnotandoPedido |

## 5. Variáveis e limiares

### Agente A — Cozinheiro

| Nome | Tipo | Intervalo | Inicial | Uso / limiar |
|---|---|---|---|---|
| `cansaco` | int | 0 a 80 | 0 | +10 por tick cozinhando, −20 descansando. ≥ `LIMITE_CANSACO` (60) ao terminar prato → Descansando; == 0 → sai de Descansando |
| `progressoPrato` | int | 0 a 100 (%) | 0 | +25 por tick; ≥ `PROGRESSO_PRATO_PRONTO` (100) → prato pronto |
| `LIMITE_CANSACO` | int (constante) | — | 60 | Limiar para descansar |
| `PROGRESSO_PRATO_PRONTO` | int (constante) | — | 100 | Limiar de prato pronto |

### Agente B — Garçom

| Nome | Tipo | Intervalo | Inicial | Uso / limiar |
|---|---|---|---|---|
| `tempoNaMesa` | int (timer) | 0 a 2 | 0 | +1 por tick anotando; ≥ `TEMPO_ANOTAR_PEDIDO` (2) → AguardandoPrato |
| `pratosEntregues` | int (contador) | 0 a ∞ | 0 | +1 a cada prato servido (estatística do log) |
| `TEMPO_ANOTAR_PEDIDO` | int (constante) | — | 2 | Limiar do timer |

### Compartilhadas — Balcão

| Nome | Tipo | Intervalo | Inicial | Uso |
|---|---|---|---|---|
| `pedidos` | int | 0 a 1 | 0 | Garçom incrementa; cozinheiro consome. > 0 libera EsperandoPedido |
| `pratos` | int | 0 a 1 | 0 | Cozinheiro incrementa; garçom consome. > 0 libera AguardandoPrato |

## 6. Estrutura do código

| Arquivo | Papel |
|---|---|
| `Simulacao.java` | Classe que gerencia a execução: cria balcão e agentes e roda o loop principal (30 ticks), chamando `atualizar()` de cada agente |
| `Estado.java` | Interface do padrão State com `entrar`, `executar` e `sair` |
| `MaquinaDeEstados.java` | Guarda o estado atual de um agente; `atualizar()` chama `executar` e `mudarEstado()` faz sempre `sair → trocar → entrar`, imprimindo a transição |
| `Balcao.java` | Canal de comunicação entre os agentes (pedidos e pratos) |
| `Cozinheiro.java` / `Garcom.java` | Agentes: atributos, constantes (limiares), ações simples e getters/setters. Não decidem transições |
| `EsperandoPedido`, `Cozinhando`, `Descansando` | Estados do cozinheiro; contêm as regras de transição |
| `AnotandoPedido`, `AguardandoPrato`, `ServindoPrato` | Estados do garçom; contêm as regras de transição |

## 7. Resultados esperados (trecho real do log)

```
==================== TICK 2 ====================
[Cozinheiro Bob] Nenhum pedido ainda... | Cansaço: 0
[Garçom Juca] Anotando pedido... | Tempo na mesa: 2/2
    >>> TRANSIÇÃO: AnotandoPedido -> AguardandoPrato
[Garçom Juca] Pedido anotado! Deixando no balcão.
    [Balcão] Novo pedido! Pedidos na fila: 1
[Garçom Juca] Aguardando o prato ficar pronto.

==================== TICK 3 ====================
[Cozinheiro Bob] Chegou um pedido!
    >>> TRANSIÇÃO: EsperandoPedido -> Cozinhando
[Cozinheiro Bob] Pegando o pedido do balcão.
[Cozinheiro Bob] Acendendo o fogão. Hora de cozinhar!
[Garçom Juca] Esperando no balcão...

==================== TICK 15 ====================
[Cozinheiro Bob] Cozinhando... | Progresso: 100% | Cansaço: 80
    [Balcão] Prato pronto! Pratos no balcão: 1
    >>> TRANSIÇÃO: Cozinhando -> Descansando
[Cozinheiro Bob] Prato finalizado. Desligando o fogão.
[Cozinheiro Bob] Estou exausto! Vou fazer uma pausa.
    >>> TRANSIÇÃO: AguardandoPrato -> ServindoPrato
[Garçom Juca] Peguei o prato no balcão!
[Garçom Juca] Levando o prato até a mesa.

==================== TICK 19 ====================
[Cozinheiro Bob] Descansando... | Cansaço: 0
    >>> TRANSIÇÃO: Descansando -> EsperandoPedido
```

Em 30 ticks o garçom entrega 3 pratos e todos os 6 estados aparecem no log. Note que entre os ticks 18
e 20 o garçom fica esperando porque o cozinheiro está descansando — é a dependência entre agentes.

## 8. Limitações

- Existe apenas uma mesa/um pedido por vez; o balcão nunca acumula mais de 1 pedido ou prato.
- Os valores (ticks, limiares) são fixos no código; não há configuração externa nem aleatoriedade.
- [Acrescente aqui o que você gostaria de ter feito, se houver.]

## 9. Referências

- BUCKLAND, Mat. *Programming Game AI by Example*. Wordware, 2005 — capítulo 2 (máquinas de estados e padrão State).
- Material de aula da disciplina (exemplos Juca, Juca e Bob, Mago).
- [Se usou IA para auxiliar, verifique a política da disciplina e declare aqui.]
