# Trabalho I — Máquina de Estados: Cozinha do Restaurante

Simulação de uma cozinha com **dois agentes** controlados por máquinas de estados finitas usando o
**padrão State** (cada estado é uma classe com `entrar()`, `executar()` e `sair()`).
Os agentes se comunicam por um **balcão** compartilhado (bônus).

## Como compilar e rodar

Requisito: Java 17 ou superior (testado no JDK 21). Não usa bibliotecas externas.

Na pasta raiz do projeto:

```bash
javac -encoding UTF-8 -d out src/*.java
java -Dstdout.encoding=UTF-8 -cp out Simulacao
```

- O primeiro comando compila todos os arquivos de `src/` e coloca os `.class` em `out/`.
- O segundo roda a classe `Simulacao`, que tem o `main`.
- Se os acentos aparecerem estranhos no terminal do Windows, rode `chcp 65001` antes.

## Agentes e estados

**Agente A — Cozinheiro (3 estados)**

| Estado | O que faz | Sai quando |
|---|---|---|
| `EsperandoPedido` | espera um pedido no balcão | há pedido no balcão → `Cozinhando` |
| `Cozinhando` | progresso +25%, cansaço +10 por tick | prato em 100% → coloca no balcão → `Descansando` (cansaço ≥ 60) ou `EsperandoPedido` |
| `Descansando` | cansaço −20 por tick | cansaço = 0 → `EsperandoPedido` |

**Agente B — Garçom (3 estados)**

| Estado | O que faz | Sai quando |
|---|---|---|
| `AnotandoPedido` | timer na mesa +1 por tick | timer ≥ 2 → deixa pedido no balcão → `AguardandoPrato` |
| `AguardandoPrato` | espera no balcão | há prato no balcão → `ServindoPrato` |
| `ServindoPrato` | entrega o prato (contador +1) | após 1 tick → `AnotandoPedido` |

**Comunicação (bônus):** o cozinheiro só cozinha depois que o garçom deixa um pedido, e o garçom só serve
depois que o cozinheiro deixa um prato pronto. Tudo passa pela classe `Balcao`.

Diagramas de estados: [`docs/diagrama_cozinheiro.svg`](docs/diagrama_cozinheiro.svg) e
[`docs/diagrama_garcom.svg`](docs/diagrama_garcom.svg).

## Como observar as transições nos logs

Cada tick começa com um cabeçalho. Toda troca de estado aparece com `>>> TRANSIÇÃO`, seguida da mensagem
de **saída** do estado antigo e da mensagem de **entrada** do novo:

```
==================== TICK 15 ====================
[Cozinheiro Bob] Cozinhando... | Progresso: 100% | Cansaço: 80
    [Balcão] Prato pronto! Pratos no balcão: 1
    >>> TRANSIÇÃO: Cozinhando -> Descansando
[Cozinheiro Bob] Prato finalizado. Desligando o fogão.     <- sair() de Cozinhando
[Cozinheiro Bob] Estou exausto! Vou fazer uma pausa.       <- entrar() de Descansando
```

- `[Cozinheiro ...]` / `[Garçom ...]`: mensagens de cada agente.
- `[Balcão]`: mensagens de comunicação entre os agentes.

Para mudar a duração ou a velocidade, altere `TOTAL_TICKS` e `PAUSA_MS` em `src/Simulacao.java`.

## Estrutura do código

```
src/
  Simulacao.java         loop principal: cria os agentes e chama atualizar() a cada tick
  Estado.java            interface do padrão State (entrar / executar / sair)
  MaquinaDeEstados.java  guarda o estado atual e faz a troca (sair -> trocar -> entrar)
  Balcao.java            comunicação entre os agentes (pedidos e pratos)
  Cozinheiro.java        Agente A: atributos, ações e limiares
  EsperandoPedido.java   \
  Cozinhando.java         > estados do Cozinheiro
  Descansando.java       /
  Garcom.java            Agente B: atributos, ações e limiares
  AnotandoPedido.java    \
  AguardandoPrato.java    > estados do Garçom
  ServindoPrato.java     /
docs/
  diagrama_cozinheiro.svg
  diagrama_garcom.svg
  rascunho_documento.md  texto-base do PDF
```
