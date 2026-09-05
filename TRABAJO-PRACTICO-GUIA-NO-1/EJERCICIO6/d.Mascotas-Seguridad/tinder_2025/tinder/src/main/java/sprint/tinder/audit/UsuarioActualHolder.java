package sprint.tinder.audit;

// Clase de utilidad, nunca se instancia con "new" (por eso el
// constructor privado). Se usa siempre por sus metodos estaticos,
// igual que Math.max(...).
public class UsuarioActualHolder {

    private static final ThreadLocal<String> USUARIO_ID = new ThreadLocal<>();

    private UsuarioActualHolder() {}

    public static void set(String usuarioId) {
        USUARIO_ID.set(usuarioId);
    }

    public static String get() {
        return USUARIO_ID.get();
    }

    public static void limpiar() {
        USUARIO_ID.remove();
    }
}
