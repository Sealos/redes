import java.rmi.Remote;

public interface c_s_services extends Remote
{
	public String iniciarSesion(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String cerrarSesion(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String listarArchivosEnServidor(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String subirArchivo(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String bajarArchivo(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String borrarArchivo(String nombre, String clave)
		throws java.rmi.RemoteException;
}
