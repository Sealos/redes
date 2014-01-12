import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
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

		public s_log()
		{
			log = new String[20];
			for (int i = 0; i < 20; ++i)
				log[i] = "";
			counter = 0;
		}

		public void add_log(String cmd)
		{
			log[counter] = cmd;
			counter = (counter + 1) % 20;
		}

		public void print_log()
		{
			int tmp = counter + 1;
			int j = 1;
			for (int i = 0; i != counter; i = (i + 1) % 20)
			{
				if (!log[counter].equals(""))
				{
					if (j < 10)
						System.out.println(j + "   " + log[counter]);
					else
						System.out.println(j + "  " + log[counter]);
				}
				++j;
			}
		}
	}

	protected class archivo
	{
		final private String f_name;
		final private String owner;
		File file;

		// Para un nuevo archivo
		archivo(String f_name, String owner, File file)
		{
			this.f_name = f_name;
			this.owner = owner;
			this.file = file;
			// Crear el archivo de ownership
			write_ownership(owner, f_name);
		}

		archivo(String f_name)
		{
			this.f_name = f_name;
			this.owner = get_ownership(f_name);
		}

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
				System.out.println(e);
			}
		}

		private String get_ownership(String f_name)
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(f_name));

				return br.readLine();
			}
			catch (FileNotFoundException e)
			{
				System.out
						.println("No se encontro el archivo de ownership para "
								+ f_name);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			return null;
		}

		public File get_file()
		{
			return file;
		}

		public String get_owner()
		{
			return owner;
		}
	}

	private static s_a_services remote;

	public s_rmifs_stub(String rmi_host, int rmi_port) throws RemoteException
	{
		super();
		try
		{
			remote = (s_a_services) Naming.lookup("rmi://" + rmi_host + ":"
					+ rmi_port + "/s_a_services");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public String init(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			archivo a = new archivo(nombre, nombre, null);
			return "Bienvenido " + nombre
					+ ". Usted esta conectado al servidor.\n";
		}
		else
			return "Usuario o contrasena incorrecta.\n";
	}

	public String close(String nombre, String clave) throws RemoteException
	{
		return "Cerrando sesion.\n";
	}

	public String rls(String nombre, String clave) throws RemoteException
	{
		return "Listando archivos en el servidor.\n";
	}

	public String sub(String archivo, String nombre, String clave)
			throws RemoteException
	{
		return "Subiendo archivo al servidor.\n";
	}

	public String baj(String archivo, String nombre, String clave)
			throws RemoteException
	{
		return "Bajando archivo desde el servidor.\n";
	}

	public String bor(String archivo, String nombre, String clave)
			throws RemoteException
	{
		return "Borrando archivo en el servidor.\n";
	}
}
