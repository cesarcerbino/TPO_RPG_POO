package rpg.modelo.unidad;

public enum TipoUnidad{
    BOMBERO("Bombero"),
    MEDICO("Medico"),
    INGENIERO("Ingeniero"),
    MILITAR("Militar"),
    ZOMBIE_COMUN("Zombie Común"),
    ZOMBIE_RAPIDO("Zombie Rápido"),
    ZOMBIE_TANQUE("Zombie Tanque");

    private final String nombreVisible;

    TipoUnidad (String nombreVisible){
        this.nombreVisible = nombreVisible;
    }
    public String getNombreVisible(){
        return nombreVisible;
    }
}

