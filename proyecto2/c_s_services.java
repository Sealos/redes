import java.rmi.Remote;
import java.rmi.RemoteException;

public interface c_s_services extends Remote
{
	public String init(String nombre, String clave) throws RemoteException;

	public String close(String nombre, String clave) throws RemoteException;

	public String rls(String nombre, String clave) throws RemoteException;

	public String sub(String nombre, String clave, byte[] file, String f_name) throws RemoteException;

	public byte[] baj(String nombre, String clave, String f_name) throws RemoteException;

	public String bor(String nombre, String clave, String f_name) throws RemoteException;
}
