package rpg.modelo.habilidad;

import rpg.modelo.unidad.Unidad;

public enum Objetivo {
    ENEMIGO,
    ALIADO,
    TODOS_ENEMIGOS,
    NINGUNO;

    public boolean requiereSeleccion() {
        return this == ENEMIGO || this == ALIADO;
    }

    public boolean esDeAliados() {
        return this == ALIADO;
    }

    public boolean admite(Unidad lanzador, Unidad objetivo){
        if(!requiereSeleccion()){
            return true;
        }
        if(objetivo == null || !objetivo.estaVivo()){
            return false;
        }
        return this == ALIADO ? lanzador.esAliadoDe(objetivo) : !lanzador.esAliadoDe(objetivo);
    }


}
