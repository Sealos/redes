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

		// Para un nuevo archivo
		archivo(String f_name, String owner, byte[] file)
		{
			this.f_name = f_name;
			this.owner = owner;
			byte_to_file(file, f_name);
			write_ownership(owner, f_name);
		}

		archivo(String f_name)
		{
			this.f_name = f_name;
			this.owner = get_ownership(f_name);
		}

		public void delete()
		{
			File file = new File("./" + f_name + ".own");
			file.delete();
			file = new File("./" + f_name);
			file.delete();
		}

		// TODO verificar si ya existe el archivo
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
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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

		final private byte[] file_to_byte(String f_name)
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public byte[] file_to_byte()
		{
			return file_to_byte(f_name);
		}

		final private String get_ownership(String f_name)
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(f_name));

				return br.readLine();
			}
			catch (FileNotFoundException e)
			{
				System.out.println("No se encontro el archivo de ownership para " + f_name);
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

		public String get_ownership()
		{
			return get_ownership(f_name);
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
			System.out.println(rmi_host + rmi_port);
			remote = (s_a_services) Naming.lookup("rmi://" + rmi_host + ":" + rmi_port + "/s_a_services");
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (NotBoundException e)
		{
			e.printStackTrace();
		}
	}

	public static String local_files()
	{
		File[] files = (new File(".")).listFiles();
		String output = "";

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isFile())
				output += files[i].getName();
		}

		return output;
	}

	public String init(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			return "Bienvenido " + nombre + ". Usted esta conectado al servidor.\n";
		}
		else
			return "Usuario o contrasena incorrecta.\n";
	}

	public String close(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			return "Cerrando sesion.\n";
		}
		else
			return "No tienes permisos para usar esta funcion.\n";
	}

	public String rls(String nombre, String clave) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			return local_files();
		}
		else
			return "No tienes permisos para usar esta funcion.\n";
	}

	public String sub(String nombre, String clave, byte[] file, String f_name) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			// TODO revsiar si existe antes de subirlo
			archivo a = new archivo(f_name, nombre, file);
			return "Subiendo archivo al servidor.\n";
		}

		return "No tienes permisos para usar esta funcion.\n";
	}

	public byte[] baj(String nombre, String clave, String f_name) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			archivo a = new archivo(f_name);
			return a.file_to_byte();
		}
		else
			return null;
	}

	// TODO check
	public String bor(String nombre, String clave, String f_name) throws RemoteException
	{
		if (remote.validate(nombre, clave))
		{
			archivo a = new archivo(f_name);
			if (nombre.equals(a.get_ownership()))
			{
				a.delete();
				return "Archivo borrado correctamente.\n";
			}
			else
				return "No tienes permisos para borrar este archivo\n";
		}
		else
			return "No tienes permisos para usar esta funcion.\n";
	}
}
