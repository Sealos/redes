//java a_rmifs -f usuarios -p puerto 
// usuarios: Nombre de archivo con usuarios
// puerto: Donde corre el rmiregistry

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

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
			a_rmifs_stub stub = new a_rmifs_stub(f_users);
			LocateRegistry.createRegistry(local_port);
			Naming.rebind("rmi://localhost:" + local_port + "/s_a_services",
					stub);
		}
		catch (RemoteException e)
		{
			System.out.println(e);
			System.exit(0);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
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

		// TODO verificar integridad de argumentos
		new a_rmifs(rmi_port, f_user);
	}
}
