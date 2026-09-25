# Projeto FSM

Exercício didático de Máquina de Estados Finita (FSM) em Java puro, sem dependências.

## Build e execução (JDK 21)

```bash
javac -encoding UTF-8 -d out src/*.java
java -Dstdout.encoding=UTF-8 -cp out <ClassePrincipal>
```

## Arquitetura esperada (seguir os projetos de referência Juca / Juca_Bob / Mago)

- **Agente** (`Juca.java`, `Mago.java`...): atributos `private int`, métodos de ação que só alteram números,
  getters/setters. O agente não conhece estados nem decide transições.
- **Máquina de estados** (`FSM.java` / `StateMachine.java`): `enum Estado`, variável `estado`, `while` (game loop)
  com `switch (estado)`. Cada `case`: executa ação -> verifica condições de saída -> imprime mensagem de
  entrada do próximo estado -> troca `estado` -> `break`.
- Valores negativos são corrigidos (clamp) pela FSM via setters.
- Comentários e mensagens em português; nomes de enum em MAIÚSCULAS.

## Convenções

- Arquivos-fonte em `src/`, saída de compilação em `out/` (ignorada pelo git).
- Nome do arquivo = nome da classe pública (sem espaços).
- Manter o estilo didático e comentado.

## Trabalho atual (enunciado PUCPR - Trabalho I)

O enunciado EXIGE padrão State (entrar/executar/sair), então o projeto usa: interface `Estado<T>`,
`MaquinaDeEstados<T>` (única que troca estados: sair -> trocar -> entrar), uma classe por estado, agentes
`Cozinheiro` (A) e `Garcom` (B) sem lógica de decisão, e `Balcao` para comunicação (bônus).
Loop principal em `Simulacao`. Docs em `docs/` (diagramas SVG + rascunho do PDF). Entrega: 02/10/2026 18:00.
