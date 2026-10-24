package sprint.tinder.dtos;

public class AuthRequest {
    private String mail;
    private String clave;

    public AuthRequest() {}

    public AuthRequest(String mail, String clave) {
        this.mail = mail;
        this.clave = clave;
    }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
