import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.File;

public class a_rmifs_stub
	extends UnicastRemoteObject
	implements s_a_services
{

	public a_rmifs_stub()
		throws RemoteException
	{
		// Cargar archivos o obtener usuarios
		super();
	}

	public Boolean validate(String nombre, String clave)
		throws RemoteException
	{
		// Validar
		return true;
	}
}
