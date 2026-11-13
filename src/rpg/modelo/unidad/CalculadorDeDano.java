package rpg.modelo.unidad;

public class CalculadorDeDano{

    private static final CalculadorDeDano INSTANCIA = new CalculadorDeDano();

    private CalculadorDeDano(){
    }

    public static CalculadorDeDano getInstancia(){
        return INSTANCIA;
    }
    public int calcular(Unidad atac, Unidad obj) {
        int dano = Math.max(1, atac.getAtaque() - obj.getDefensa() / 2);
        if (obj.isDefendiendo()) {
            dano = Math.max(1, dano / 2);
        }
        return dano;
    }


}
