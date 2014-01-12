// Comandos log sal
// java s_rmifs -l puertolocal -h host -r puerto 
// puertolocal: Donde esta el rmiregistry info de los objetos del servidor de archivo
// host: Donde corre el servidor de autenticacion
// puerto: Donde esta el rmiregistry info de los objetos del servidor de autenticacion

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class s_rmifs
{
	private static final int DEFAULT_PORT = 20203;

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

	public s_rmifs(String rmi_host, int rmi_port, int local_port)
	{
		try
		{
			s_rmifs_stub stub = new s_rmifs_stub(rmi_host, rmi_port);
			LocateRegistry.createRegistry(local_port);
			Naming.rebind("rmi://localhost:"+ local_port +"/c_s_services", stub);
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static void main(String[] args)
	{
		int requerimientos = 0;
		int local_port = DEFAULT_PORT;
		String rmi_host = "localhost";
		int rmi_port = DEFAULT_PORT;
		for(int i = 0; i < args.length; i = i + 2)
		{
			switch(get_opt(args[i]))
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
			System.out.println("Advertencia, faltan argumentos corriendo con:");
			System.out.println("java s_rmifs -l " + local_port + " -h " + rmi_host + " -r " + rmi_port);
		}

		new s_rmifs(rmi_host, rmi_port, local_port);
	}
}
