package rpg.modelo.accion;

public class AccionInvalidaException extends RuntimeException{
    public AccionInvalidaException ( String mensaje){
        super(mensaje);
    }
}
