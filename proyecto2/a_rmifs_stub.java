import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.*;

public class a_rmifs_stub extends UnicastRemoteObject implements s_a_services
{
	hash<usuario> users;

	/**
	 * Inicializacion del servidor de autenticacion
	 *
	 * @param Direccion del archivo con las tuplas de usuario:clave validos
	 * @return 
	 */
	public a_rmifs_stub(String f_users) throws RemoteException
	{
		super();
		users = new hash<usuario>(20);
		String[] ln;

		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(f_users));

			while ((line = br.readLine()) != null)
			{
				System.out.println(line);
				ln = line.split(":");
				users.add(new usuario(ln[0], ln[1]));
			}
		}
		catch (IOException e)
		{
			System.out.println("Error al acceder al archivo " + f_users);
			System.exit(0);
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException e)
			{
				System.out.println("Error al cerrar el archivo " + f_users);
				System.exit(0);
			}
		}
	}

	/**
	 * Verifica si la tupla nombre:clave
	 *
	 * @param Nombre a verificar
	 * @param Clave a verificar
	 * @return Devuelve true si son iguales, false en caso contrario
	 */
	public boolean validate(String nombre, String clave) throws RemoteException
	{
		return users.contains(new usuario(nombre, clave));
	}
}
