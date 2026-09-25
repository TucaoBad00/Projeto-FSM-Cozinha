// Estado do COZINHEIRO: descansando porque ficou muito cansado.
public class Descansando implements Estado<Cozinheiro> {

    @Override
    public void entrar(Cozinheiro cozinheiro) {
        cozinheiro.falar("Estou exausto! Vou fazer uma pausa.");
    }

    @Override
    public void executar(Cozinheiro cozinheiro) {
        // Ação: descansar (cansaço -20, nunca abaixo de 0)
        cozinheiro.descansar();
        cozinheiro.falar("Descansando... | Cansaço: " + cozinheiro.getCansaco());

        // Condição de saída: descansou totalmente?
        if (cozinheiro.getCansaco() == 0) {
            cozinheiro.mudarEstado(new EsperandoPedido());
        }
    }

    @Override
    public void sair(Cozinheiro cozinheiro) {
        cozinheiro.falar("Pronto, estou descansado!");
    }
}
