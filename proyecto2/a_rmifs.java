// java a_rmifs -f usuarios -p puerto
// usuarios: Nombre de archivo con usuarios
// puerto: Donde corre el rmiregistry

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

public class a_rmifs
{
	private static final int DEFAULT_PORT = 20203;

	/**
	 * Verifica si la opcion es alguna de las validas
	 * 
	 * @param Opcion
	 *            del argumento de entrada
	 * @return Devuelve 0 si la opcion es -f, 1 si es -p, -1 en caso contrario
	 */
	private static int get_opt(String opt)
	{
		if (opt.equals("-f"))
			return 0;
		else if (opt.equals("-p"))
			return 1;
		else
			return -1;
	}

	/**
	 * Creacion del url del servidor remoto de autenticacion
	 * 
	 * @param Puerto
	 *            de ejecucion del servidor
	 * @param Direccion
	 *            del archivo con las tuplas de usuario:clave validos
	 * @return
	 */
	public a_rmifs(int local_port, String f_users)
	{
		try
		{
			a_rmifs_stub stub = new a_rmifs_stub(f_users);
			LocateRegistry.createRegistry(local_port);
			Naming.rebind("rmi://localhost:" + local_port + "/s_a_services", stub);
		}
		catch (RemoteException e)
		{
			System.out.println("Ocurrio un problema al establecer el servidor de las funciones remotas.");
			System.exit(0);
		}
		catch (MalformedURLException e)
		{
			System.out.println("El URL del servidor es incorrecto.");
			System.exit(0);
		}
	}

	public static void main(String args[])
	{
		String f_user = "";
		int rmi_port = DEFAULT_PORT;
		int requerimientos = 0;
		for (int i = 0; i < args.length; i = i + 2)
		{
			switch (get_opt(args[i]))
			{
			// Archivo de usuarios
				case 0:
					requerimientos = requerimientos | 1;
					f_user = args[i + 1];
					break;
				// Puerto local de rmi
				case 1:
					requerimientos = requerimientos | 2;
					rmi_port = Integer.parseInt(args[i + 1]);
					break;
				default:
					System.out.println("Opcion no reconocida " + args[i]);
					System.exit(0);
			}
		}

		if ((requerimientos & 3) != 3)
		{
			System.out.println("Error, faltan argumentos");
			System.out.println("java a_rmifs -f usuarios -p puerto");
			System.exit(0);
		}

		if (rmi_port <= 1024 | rmi_port > 65535)
		{
			System.out.println("Usted ingreso un numero de puerto invalido");
			System.out.println("El numero de puerto debe estar en el rango de (1024, 65535)");
			System.exit(0);
		}

		new a_rmifs(rmi_port, f_user);
	}
}
