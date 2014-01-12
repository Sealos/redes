/*
java c_rmifs [-f usuarios] -m servidor -p puerto [-c comandos]
Invocacion:
	usuarios: Nombre de archivo? con usuario y claves
	servidor: direccion de servidor de archivos
	puerto: rmiregistry del servidor de archivos
	comandos: Comandos a ejecutar
Comandos:
	rls
	lls
	sub archivo
	baj archivo
	bor archivo
	info
	sal
 */

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.*;

public class c_rmifs
{
	private static c_s_services remote;
	public static String user;
	public static String pas;

	private static int get_opt(String opt)
	{
		if (opt.equals("-f"))
			return 0;
		else if (opt.equals("-p"))
			return 1;
		else if (opt.equals("-m"))
			return 2;
		else if (opt.equals("-c"))
			return 3;
		else
			return -1;
	}

	private static int get_cmd(String cmd)
	{
		if (cmd.equals("rls"))
			return 0;
		else if (cmd.equals("lls"))
			return 1;
		else if (cmd.equals("sub"))
			return 2;
		else if (cmd.equals("baj"))
			return 3;
		else if (cmd.equals("bor"))
			return 4;
		else if (cmd.equals("info"))
			return 5;
		else if (cmd.equals("sal"))
			return 6;
		else
			return -1;
	}

	public static void main(String[] args)
	{
		try
		{
			String servidor = "";
			int puerto = 0;

			String cmd;
			BufferedReader f_cmd = null;
			BufferedReader f_user = null;
			int requerimientos = 0;
			String[] ln;

			for (int i = 0; i < args.length; i = i + 2)
			{
				switch (get_opt(args[i]))
				{
				// Archivo de usuario
					case 0:
						requerimientos = requerimientos | 1;
						f_user = new BufferedReader(new FileReader(new File(
								args[i + 1])));
						break;
					// Direccion de rmi
					case 1:
						requerimientos = requerimientos | 2;
						puerto = Integer.parseInt(args[i + 1]);
						break;
					// Puerto de rmi
					case 2:
						requerimientos = requerimientos | 4;
						servidor = args[i + 1];

						break;
					// Archivos de commandos
					case 3:
						requerimientos = requerimientos | 8;
						f_cmd = new BufferedReader(new FileReader(new File(
								args[i + 1])));
						break;
					default:
						System.out.println("Opcion no reconocida " + args[i]);
						System.exit(0);
				}
			}
			// Estan los requerimientos obligatorios
			if ((requerimientos & 6) == 6)
			{
				// Obtemenos el RPC
				remote = (c_s_services) Naming.lookup("rmi://" + servidor + ":"
						+ puerto + "/c_s_services");

				// Obtenemos el usuario con el cual vamos a trabajar
				if (f_user != null)
				{
					ln = (f_user.readLine()).split(":");
					user = ln[0];
					pas = ln[1];
					f_user.close();
				}
				else
				{
					System.out
							.print("Introduzca sus datos en el formato: <nombre> <clave>");
					ln = System.console().readLine().split(" ");
					user = ln[0];
					pas = ln[1];
				}
				System.out.println(remote.init(user, pas));
			}
			else
			{
				System.out.println("Error, faltan argumentos");
				System.out
						.println("java c_rmifs [-f usuarios] -m servidor -p puerto [-c comandos]");
				System.exit(0);
			}

			/*
			 * if (f_cmd != null) { while(((cmd = f_cmd.readLine()) != null)) {
			 * ln = cmd.split(" "); ejecutarComando(ln[0], ln[1]); }
			 * f_cmd.close(); }
			 */

			while (true)
			{
				cmd = System.console().readLine();
				ln = cmd.split(" ");
				ejecutarComando(ln[0], ln[1]);
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (NotBoundException e)
		{
			e.printStackTrace();
		}
	}

	public static void local_files()
	{
		File[] files = (new File(".")).listFiles();

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isFile())
			{
				System.out.println(files[i].getName());
				/*
				 * if (!(files.matches("(*\\.(class|java))"))) { }
				 */
			}
		}
	}

	public static void ejecutarComando(String cmd, String argumento)
	{
		try
		{
			switch (get_cmd(cmd))
			{
				case 0:
					System.out.println(remote.rls(user, pas));
					break;
				case 1:
					System.out.println("Archivos locales:");
					local_files();
					break;
				case 2:
					// System.out.println(remote.subirArchivo(user, pas));
					break;
				case 3:
					// System.out.println(remote.bajarArchivo(user, pas));
					break;
				case 4:
					// System.out.println(remote.bor(user, pas));
					break;
				case 5:
					System.out.println("Comandos:");
					System.out
							.println("rls | lls | sub <archivo> | baj <archivo> | bor <archivo> | info | sal");
					break;
				case 6:
					// System.out.println(remote.close(user, pas));
					System.out.println("Cerrando...");
					System.exit(0);
				default:
					System.out.println("No existe el comando " + cmd);
					break;
			}
		}
		catch (RemoteException e)
		{
			System.out.println(e);
		}
	}

	/*
	 * InputStream input = new FileInputStream(file); ByteArrayOutputStream
	 * output = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; for
	 * (int length = 0; (length = input.read(buffer)) > 0;) {
	 * output.write(buffer, 0, length); } byte[] bytes = output.toByteArray();
	 * // Pass that instead to RMI response.
	 */
}
