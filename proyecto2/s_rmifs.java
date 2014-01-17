// Comandos log sal
// java s_rmifs -l puertolocal -h host -r puerto
// puertolocal: Donde esta el rmiregistry info de los objetos del servidor de
// archivo
// host: Donde corre el servidor de autenticacion
// puerto: Donde esta el rmiregistry info de los objetos del servidor de
// autenticacion

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class s_rmifs
{
	private static final int DEFAULT_PORT = 20203;
	private s_rmifs_stub stub;

	/**
	 * Verifica si la opcion es alguna de las validas
	 * 
	 * @param Opcion
	 *            del argumento de entrada
	 * @return Un indice que indica la opcion que se elegio o si fue un error
	 */
	private static int get_opt(String opt)
	{
		if (opt.equals("-l"))
			return 0;
		else if (opt.equals("-h"))
			return 1;
		else if (opt.equals("-r"))
			return 2;
		else
			return -1;
	}

	/**
	 * Verifica si el comando ingresado es valido
	 * 
	 * @param Nombre
	 *            del comando
	 * @return Un indice que indica la opcion que se elegio o si fue un error
	 */
	private static int get_cmd(String cmd)
	{
		if (cmd.equals("log"))
			return 0;
		else if (cmd.equals("sal"))
			return 1;
		else
			return -1;
	}

	/**
	 * Realiza el llamado de una funcion
	 * 
	 * @param Nombre
	 *            del comando
	 */
	public void ejecutar_comando(String cmd)
	{
		switch (get_cmd(cmd))
		{
		// log
			case 0:
				if (stub != null)
				{
					System.out.println("Log - Mas reciente primero");
					System.out.println(stub.print_log());
				}
				break;
			// sal
			case 1:
				System.out.println("Cerrando...");
				System.exit(0);
			default:
				System.out.println("No existe el comando " + cmd);
				break;
		}
	}

	/**
	 * Creacion del url del servidor remoto de archivos
	 * 
	 * @param Host
	 *            donde se ejecuta el servidor de autenticacion
	 * @param Puerto
	 *            donde se ejecuta el servidor de autenticacion
	 * @param Puerto
	 *            de ejecucion del servidor de archivo
	 * @return
	 */
	public s_rmifs(String rmi_host, int rmi_port, int local_port)
	{
		try
		{
			stub = new s_rmifs_stub(rmi_host, rmi_port);
			LocateRegistry.createRegistry(local_port);
			Naming.rebind("rmi://localhost:" + local_port + "/c_s_services", stub);
		}
		catch (RemoteException e)
		{
			System.out.println("Ocurrio un problema al establecer el servidor de las funciones remotas");
			System.exit(0);
		}
		catch (MalformedURLException e)
		{
			System.out.println("El URL del servidor es incorrecto");
			System.exit(0);
		}

		String cmd;
		while (true)
		{
			cmd = System.console().readLine();
			ejecutar_comando(cmd);
		}
	}

	public static void main(String[] args)
	{
		int requerimientos = 0;
		int local_port = DEFAULT_PORT;
		String rmi_host = "localhost";
		int rmi_port = DEFAULT_PORT;
		for (int i = 0; i < args.length; i = i + 2)
		{
			switch (get_opt(args[i]))
			{
			// Puerto local
				case 0:
					requerimientos = requerimientos | 1;
					local_port = Integer.parseInt(args[i + 1]);
					break;
				// Direccion de rmi
				case 1:
					requerimientos = requerimientos | 2;
					rmi_host = args[i + 1];
					break;
				// Puerto de rmi
				case 2:
					requerimientos = requerimientos | 4;
					rmi_port = Integer.parseInt(args[i + 1]);
					break;
				default:
					System.out.println("Opcion no reconocida " + args[i]);
					System.exit(0);
			}
		}

		if ((requerimientos & 7) != 7)
		{
			System.out.println("Faltan argumentos corriendo con:");
			System.out.println("java s_rmifs -l puertolocal -h host -r puerto");
			System.exit(0);
		}

		if (local_port <= 1024 | local_port > 65535 | rmi_port <= 1024 | rmi_port > 65535)
		{
			System.out.println("Usted ingreso un numero de puerto invalido");
			System.out.println("El numero de puerto debe estar en el rango de (1024,65535)");
			System.exit(0);
		}

		new s_rmifs(rmi_host, rmi_port, local_port);
	}
}
