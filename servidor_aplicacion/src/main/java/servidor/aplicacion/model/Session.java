package servidor.aplicacion.model;

public class Session {
	private int id;
	private long userId;
	private String token;
	private java.sql.Timestamp createdAt;
	private java.sql.Timestamp expiresAt;

	public Session() {}

	public Session(int id, long userId, String token, java.sql.Timestamp createdAt, java.sql.Timestamp expiresAt) {
		this.id = id;
		this.userId = userId;
		this.token = token;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}
	public int getId() {
		return id;
	}

	public long getUserId() {
		return userId;
	}

	public String getToken() {
		return token;
	}

	public java.sql.Timestamp getCreatedAt() {
		return createdAt;
	}

	public java.sql.Timestamp getExpiresAt() {
		return expiresAt;
	}
}
