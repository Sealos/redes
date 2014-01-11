//java a_rmifs -f usuarios -p puerto 
// usuarios: Nombre de archivo con usuarios
// puerto: Donde corre el rmiregistry

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.io.*;

public class a_rmifs
{

	private static final int DEFAULT_PORT = 20203;

	private static int get_opt(String opt)
	{
		if (opt.equals("-f"))
			return 0;
		else if (opt.equals("-p"))
			return 1;
		else
			return -1;
	}

	public a_rmifs(int local_port, String f_users)
	{
		try
		{
			a_rmifs_stub stub = new a_rmifs_stub();
			LocateRegistry.createRegistry(local_port);
			Naming.rebind("rmi://localhost:"+ local_port +"/s_a_services", stub);
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static void main(String args[])
	{
		BufferedReader f_user = null;
		int rmi_port = DEFAULT_PORT;
		int requerimientos = 0;
		for(int i = 0; i < args.length; i = i + 2)
		{
			try
			{
				switch(get_opt(args[i]))
				{
					// Archivo de usuarios
					case 0:
						requerimientos = requerimientos | 1;
						f_user = new BufferedReader(new FileReader(new File(args[i + 1])));
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
			catch (FileNotFoundException e)
			{
				System.out.println("No se encontro el archivo de usuarios");
				System.exit(0);
			}
		}

		if ((requerimientos & 3) != 3)
		{
			System.out.println("Error, faltan argumentos");
			System.exit(0);
		}
	}
}
