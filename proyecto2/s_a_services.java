import java.rmi.Remote;
import java.rmi.RemoteException;

public interface s_a_services extends Remote
{
	public boolean validate(String nombre, String clave) throws RemoteException;
}
