import java.rmi.Remote;

public interface s_a_services extends Remote
{
	public Boolean validate(String nombre, String clave)
		throws java.rmi.RemoteException;
}
