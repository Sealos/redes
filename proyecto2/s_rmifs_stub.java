import java.net.MalformedURLException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.*;

public class s_rmifs_stub extends UnicastRemoteObject implements c_s_services
{

	private static final long serialVersionUID = 2149954566701153431L;

	/**
	 * Clase para los 20 ultimos comandos del servidor
	 **/
	protected class s_log
	{
		private String[] log;
		private int counter;

		/**
		 * Constructor de la clase s_log
		 *
		 * @return
		 */
		public s_log()
		{
			log = new String[20];
			for (int i = 0; i < 20; ++i)
				log[i] = "";
			counter = 0;
		}

		/**
		 * Agrega un comando al log
		 *
		 * @return
		 */
		public void add_log(String cmd)
		{
			log[counter] = cmd;
			counter = (counter + 1) % 20;
		}

		/**
		 * Imprime el conjunto de comandos en el log
		 *
		 * @return
		 */
		public String print_log()
		{
			int j = 1;
			int i = counter;
			int stop = i == 0 ? 19 : counter -1;
			String out = "";
			while (i != stop)
			{
				if (!log[i].equals(""))
				{
					if (j < 10)
						out = out + j + "  " + log[i] + "\n";
					else
						out = out + j + " " + log[i] + "\n";
					++j;
				}
				i = (i + 1) % 20;
			}

			return out;
		}
		
	}

	protected class archivo
	{
		final private String f_name;
		final private String owner;

		/**
		 * Constructor de la clase archivo
		 *
		 * @param Nombre del archivo
		 * @param Nombre del propietario del archivo
		 * @param Bytes del contenido del archivo
		 * @return
		 */
		archivo(String f_name, String owner, byte[] file)
		{
			this.f_name = f_name;
			this.owner = owner;
			byte_to_file(file, f_name);
			write_ownership(owner, f_name);
		}

		/**
		 * Constructor de la clase archivo
		 *
		 * @param Nombre del archivo
		 * @return
		 */
		archivo(String f_name)
		{
			this.f_name = f_name;
			this.owner = get_ownership(f_name);
		}

		/**
		 * Indica si existe un archivo con el mismo nombre en el directorio
		 *
		 * @return Devuelve true si el archivo existe, false en caso contrario
		 */
		public boolean exist()
		{
			File file = new File(f_name);
			return file.exists();
		}

		/**
		 * Borra el archivo del directorio y el archivo que indica su propietario
		 *
		 * @return
		 */
		public void delete()
		{
			File file = new File("./" + f_name + ".own");
			file.delete();
			file = new File("./" + f_name);
			file.delete();
		}

		/**
		 * Toma una cadena de bytes y se usa para crear un archivo con esos datos
		 *
		 * @param Bytes que seran contenido del archivo a crear
		 * @param Nombre del archivo
		 * @return 
		 */
		private void byte_to_file(byte[] file, String f_name)
		{
			try
			{
				BufferedOutputStream fs = new BufferedOutputStream(new FileOutputStream((new File(f_name)).getName()));

				fs.write(file, 0, file.length);
				fs.flush();
				fs.close();
			}
			catch (FileNotFoundException e)
			{
				System.out.println("El archivo " + f_name + "no se encontro");
				System.exit(0);
			}
			catch (IOException e)
			{
				System.out.println("Error al acceder al archivo " + f_name);
				System.exit(0);
			}
		}

		/**
		 * Dado un nombre de archivo crea un archivo .own
		 * con el nombre del propietario del archivo
		 *
		 * @param Nombre del propietario
		 * @param Nombre del archivo
		 * @return 
		 */
		private void write_ownership(String owner, String f_name)
		{
			try
			{
				File file = new File("./" + f_name + ".own");

				if (!file.exists())
					file.createNewFile();

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(owner);
				bw.close();

			}
			catch (IOException e)
			{
				System.out.println("Error al acceder al archivo de verificacion de propiedad");
				System.exit(0);
			}
		}

		/**
		 * El contenido de un archivo se devuelven como una cadena de bytes
		 *
		 * @param Nombre del archivo
		 * @return La cadena de bytes que representan el contenido del archivo
		 */
		private byte[] file_to_byte(String f_name)
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
				System.out.println("El archivo " + f_name + "no se encontro");
				System.exit(0);
			}
			catch (IOException e)
			{
				System.out.println("Error al acceder al archivo " + f_name);
				System.exit(0);
			}
			return null;
		}

		/**
		 * El contenido de un archivo se devuelven como una cadena de bytes
		 *
		 * @return La cadena de bytes que representan el contenido del archivo
		 */
		public byte[] file_to_byte()
		{
			return file_to_byte(f_name);
		}

		/**
		 * Devuelve el nombre del propietario del archivo
		 *
		 * @param Nombre del archivo
		 * @return El nombre del propietario
		 */
		private String get_ownership(String f_name)
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(f_name + ".own"));
				String line = br.readLine();
				br.close();
				return line;
			}
			catch (FileNotFoundException e)
			{
				System.out.println("No se encontro el archivo .own para " + f_name);
				return "";
			}
			catch (IOException e)
			{
				return "";
			}
		}

		/**
		 * Devuelve el nombre del propietario del archivo
		 *
		 * @return El nombre del propietario
		 */
		private String get_ownership()
		{
			return get_ownership(f_name);
		}

		/**
		 * Devuelve el valor de la variable owner
		 *
		 * @return El nombre del propietario
		 */
		public String get_owner()
		{
			return owner;
		}
	}

	private static s_a_services remote;
	private s_log l;

	/**
	 * Inicializacion del servidor de archivos
	 *
	 * @param Host del servidor de autenticacion
	 * @param Puerto del servidor de autenticacion
	 * @return 
	 */
	public s_rmifs_stub(String rmi_host, int rmi_port) throws RemoteException
	{
		super();
		l = new s_log();

		try
		{
			remote = (s_a_services) Naming.lookup("rmi://" + rmi_host + ":" + rmi_port + "/s_a_services");
		}
		catch (RemoteException e)
		{
			System.out.println("Ocurrio un problema al intentar acceder al servidor de las funciones remotas");
			System.exit(0);
		}
		catch (MalformedURLException e)
		{
			System.out.println("El URL del servidor es incorrecto");
			System.exit(0);
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
	public static String local_files()
	{
		File[] files = (new File(".")).listFiles();
		String output = "";

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isFile())
				output = output + files[i].getName() + "\n";
		}

		return output;
	}

	/**
	 * Realiza el inicio de sesion del cliente
	 *
	 * @param Nombre del cliente
	 * @param Clave del cliente
	 * @return Devuelve true si la sesion se inicio con exito, false en caso contrario
	 */
	public boolean init(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			l.add_log("El usuario " + nombre + " inicio.");
			return true;
		}
		else
			return false;
	}

	/**
	 * Realiza el cierre de sesion del cliente
	 *
	 * @param Nombre del cliente
	 * @param Clave del cliente
	 * @return Devuelve un string que indica si realizo el cierre de sesion 
	 * en caso contrario se le indica el motivo del porque no se concreto el cierre
	 */
	public String close(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			l.add_log("El usuario " + nombre + " uso sal.");
			return "Cerrando...\n";
		}
		else
			return "No tienes permisos para usar esta funcion.\n";
	}

	/**
	 * Muestra los archivos en el directorio del servidor
	 *
	 * @param Nombre del cliente
	 * @param Clave del cliente
	 * @return Devuelve los nombres de los archivos en caso de exito
	 * en caso contrario se indica el motivo del porque no se concreto la accion
	 */
	public String rls(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			l.add_log("El usuario " + nombre + " uso rls.");
			return local_files();
		}
		else
			return "No tienes permisos para usar esta funcion.\n";
	}

	/**
	 * Sube un archivo del cliente al servidor
	 *
	 * @param Nombre del cliente
	 * @param Clave del cliente
	 * @param Cadena de bytes con la informacion del archivo
	 * @param Nombre del archivo a subir
	 * @return Devuelve un mensaje indicando lo ocurrido con esta accion
	 */
	public String sub(String nombre, String clave, byte[] file, String f_name) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			archivo a = new archivo(f_name);
			if (a.exist())
			{
				l.add_log("El usuario " + nombre + " uso sub en un archivo existente.");
				return "No se subio el archivo dado que ya existe un\narchivo con el mismo nombre.\n";
			}
			else
			{
				l.add_log("El usuario " + nombre + " uso sub.");
				a = new archivo(f_name, nombre, file);
				return "Subiendo archivo al servidor.\n";
			}

		}

		return "No tienes permisos para usar esta funcion.\n";
	}

	/**
	 * Baja un archivo del servidor
	 *
	 * @param Nombre del cliente
	 * @param Clave del cliente
	 * @param Nombre del archivo a bajar
	 * @return En caso de exito devuelve los bytes con la informacion del archivo,
	 * caso contrario devuelve null
	 */
	public byte[] baj(String nombre, String clave, String f_name) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			l.add_log("El usuario " + nombre + " uso baj.");
			archivo a = new archivo(f_name);
			return a.file_to_byte();
		}
		else
			return null;
	}

	/**
	 * Borra un archivo del servidor
	 *
	 * @param Nombre del cliente
	 * @param Clave del cliente
	 * @param Nombre del archivo
	 * @return Devuelve un mensaje indicando lo ocurrido con esta accion
	 */
	public String bor(String nombre, String clave, String f_name) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			l.add_log("El usuario " + nombre + " uso bor.");
			archivo a = new archivo(f_name);
			System.out.println(nombre + " " + a.get_ownership());
			if (nombre.equals(a.get_owner()))
			{
				a.delete();
				return "Archivo borrado correctamente.\n";
			}
			else
			{
				if (a.exist())
					return "No tienes permisos para borrar este archivo\n";
				else
					return "El archivo no existe.\n";

			}
		}
		else
			return "No tienes permisos para usar esta funcion.\n";
	}

	public String print_log()
	{
		return l.print_log();
	}
}
