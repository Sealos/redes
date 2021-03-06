/*
 * java c_rmifs [-f usuarios] -m servidor -p puerto [-c comandos] Invocacion: usuarios: Nombre de archivo? con usuario y
 * claves servidor: direccion de servidor de archivos puerto: rmiregistry del servidor de archivos comandos: Comandos a
 * ejecutar Comandos: rls lls sub archivo baj archivo bor archivo info sal
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

	/**
	 * Verifica si la opcion es alguna de las validas
	 * 
	 * @param Opcion del argumento de entrada
	 * @return Un indice que indica la opcion que se elegio o si fue un error
	 */
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

	/**
	 * Verifica si el comando ingresado es valido
	 * 
	 * @param Nombre del comando
	 * @return Un indice que indica la opcion que se elegio o si fue un error
	 */
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

	/**
	 * El contenido de un archivo se devuelven como una cadena de bytes
	 * 
	 * @param Nombre del archivo
	 * @return La cadena de bytes que representan el contenido del archivo
	 */
	private static byte[] file_to_byte(String f_name)
	{
		try
		{
			File archivo = new File(f_name);
			byte buffer[] = new byte[(int) archivo.length()];
			BufferedInputStream br = new BufferedInputStream(new FileInputStream(f_name));
			br.read(buffer, 0, buffer.length);
			br.close();
			return buffer;
		}
		catch (FileNotFoundException e)
		{
			System.out.println("El archivo " + f_name + " no se encontro");
		}
		catch (IOException e)
		{
			System.out.println("Error al acceder al archivo " + f_name);
		}
		return null;
	}

	/**
	 * Toma una cadena de bytes y se usa para crear un archivo con esos datos
	 * 
	 * @param Bytes que seran contenido del archivo a crear
	 * @param Nombre del archivo
	 * @return
	 */
	private static void byte_to_file(byte[] file, String f_name)
	{
		try
		{
			if (file != null)
			{
				BufferedOutputStream fs = new BufferedOutputStream(new FileOutputStream((new File(f_name)).getName()));

				fs.write(file, 0, file.length);
				fs.flush();
				fs.close();
				System.out.println("Se bajo el archivo " + f_name + " exitosamente.");
			}
			else
				System.out.print("El archivo no existe en el servidor.");
		}
		catch (FileNotFoundException e)
		{
			System.out.println("El archivo " + f_name + "no se encontro");
		}
		catch (IOException e)
		{
			System.out.println("Error al acceder al archivo " + f_name);
		}
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

			if ((args.length % 2) == 1)
			{
				System.out.println("Error en la invocacion.");
				System.out.println("java c_rmifs [-f usuarios] -m servidor -p puerto [-c comandos]");
				System.exit(0);
			}

			for (int i = 0; i < args.length; i = i + 2)
			{
				switch (get_opt(args[i]))
				{
				// Archivo de usuario
					case 0:
						try
						{
							f_user = new BufferedReader(new FileReader(new File(args[i + 1])));
							requerimientos = requerimientos | 1;
						}
						catch (FileNotFoundException e)
						{
							System.out.println("El archivo de usuarios no se encontro.");
						}
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
						try
						{
							f_cmd = new BufferedReader(new FileReader(new File(args[i + 1])));
							requerimientos = requerimientos | 8;
						}
						catch (FileNotFoundException e)
						{
							System.out.println("El archivo de comandos no se encontro.");
						}
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
				try
				{
					remote = (c_s_services) Naming.lookup("rmi://" + servidor + ":" + puerto + "/c_s_services");
				}
				catch (RemoteException e)
				{
					System.out.println("El servidor no se encuentra disponible");
					System.exit(0);
				}

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
					boolean valid = false;
					while (!valid)
					{
						System.out.println("Introduzca sus datos en el formato: <nombre> <clave>");
						ln = System.console().readLine().split(" ");
						if (ln.length != 2)
							System.out.println("Error leyendo los datos.");
						else
						{
							user = ln[0];
							pas = ln[1];
							valid = true;
						}
					}
				}

				if (remote.init(user, pas))
					System.out.println("Bienvenido " + user + ". Usted esta conectado al servidor.");
				else
				{
					System.out.println("Usuario o contrasena incorrecta.");
					System.exit(0);
				}
			}
			else
			{
				System.out.println("Error, faltan argumentos");
				System.out.println("java c_rmifs [-f usuarios] -m servidor -p puerto [-c comandos]");
				System.exit(0);
			}

			if (f_cmd != null)
			{
				while (((cmd = f_cmd.readLine()) != null))
				{
					ejecutar_comando(cmd);
				}
				f_cmd.close();
			}

			while (true)
			{
				cmd = System.console().readLine();
				ejecutar_comando(cmd);
			}
		}
		catch (IOException e)
		{
			System.out.println("Error al acceder al archivo.");
		}
		catch (NotBoundException e)
		{
			System.out.println("El URL del servidor no existe");
			System.exit(0);
		}
	}

	/**
	 * Busca el conjunto de archivos que se encuentran en el directorio actual
	 * 
	 * @return Los nombres de los archivos.
	 */
	public static void local_files()
	{
		File[] files = (new File(".")).listFiles();

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isFile())
			{
				System.out.println(files[i].getName());
			}
		}
	}

	/**
	 * Realiza el llamado a una funcion remota segun un comando dado
	 * 
	 * @param Nombre del comando
	 * @return
	 */
	public static void ejecutar_comando(String cmd)
	{
		String[] ln = cmd.split(" ");
		try
		{
			switch (get_cmd(ln[0]))
			{
			// rls
				case 0:
					System.out.println(remote.rls(user, pas));
					break;
				// lls
				case 1:
					System.out.println("Archivos locales:");
					local_files();
					break;
				// sub <archivo>
				case 2:
					byte[] a = file_to_byte(ln[1]);
					if (a != null)
						System.out.println(remote.sub(user, pas, a, ln[1]));
					break;
				// baj <archivo>
				case 3:
					byte_to_file(remote.baj(user, pas, ln[1]), ln[1]);
					break;
				// bor <archivo>
				case 4:
					System.out.println(remote.bor(user, pas, ln[1]));
					break;
				// info
				case 5:
					System.out.println("Comandos:");
					System.out.println("rls | lls | sub <archivo> | baj <archivo> | bor <archivo> | info | sal");
					break;
				// sal
				case 6:
					System.out.println(remote.close(user, pas));
					System.exit(0);
				default:
					System.out.println("No existe el comando " + cmd);
					break;
			}
		}
		catch (RemoteException e)
		{
			System.out.println("El servidor de archivos no se encuentra disponible.");
			System.out.println("Cerrando...");
			System.exit(0);
		}
	}
}
