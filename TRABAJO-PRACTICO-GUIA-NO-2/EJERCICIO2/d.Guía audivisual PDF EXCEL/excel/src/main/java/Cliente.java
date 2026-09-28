public class Cliente {
    private Long id;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String email;
    private Ciudad ciudad;

    public Cliente(Long id, String nombres, String apellidos, String telefono, String email, Ciudad ciudad) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.ciudad = ciudad;
    }

    public Long getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public Ciudad getCiudad() { return ciudad; }
}