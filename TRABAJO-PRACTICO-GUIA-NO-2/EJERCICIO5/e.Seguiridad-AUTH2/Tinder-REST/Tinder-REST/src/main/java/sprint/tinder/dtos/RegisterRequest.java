package sprint.tinder.dtos;

public class RegisterRequest {
    private String nombre;
    private String mail;
    private String clave;

    public RegisterRequest() {}

    public RegisterRequest(String nombre, String mail, String clave) {
        this.nombre = nombre;
        this.mail = mail;
        this.clave = clave;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}