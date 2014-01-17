import java.rmi.Remote;
import java.rmi.RemoteException;

public interface s_a_services extends Remote
{
	/**
	 * Verifica si la tupla nombre:clave
	 * 
	 * @param Nombre a verificar
	 * @param Clave a verificar
	 * @return Devuelve true si son iguales, false en caso contrario
	 */
	public boolean validate(String nombre, String clave) throws RemoteException;
}
