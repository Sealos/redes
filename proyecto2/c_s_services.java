import java.rmi.Remote;

public interface c_s_services extends Remote
{
	public String init(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String close(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String rls(String nombre, String clave)
		throws java.rmi.RemoteException;

	public String sub(String archivo, String nombre, String clave)
		throws java.rmi.RemoteException;

	public String baj(String archivo, String nombre, String clave)
		throws java.rmi.RemoteException;

	public String bor(String archivo, String nombre, String clave)
		throws java.rmi.RemoteException;
}
