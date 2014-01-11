import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.File;

public class s_rmifs_stub
	extends UnicastRemoteObject
	implements c_s_services
{

	private class archivo
	{
		final private String f_name;
		final private String owner;
		File file;

		archivo(String f_name, String owner, File file)
		{
			this.f_name = f_name;
			this.owner = owner;
			this.file = file;
		}

		archivo(String f_name)
		{
			this.f_name = f_name;
			this.owner = "asd";
			// Cargar archivos
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

	public s_rmifs_stub()
		throws RemoteException
	{
		super();
	}

	public String iniciarSesion(String nombre, String clave)
		throws RemoteException
	{
		return "Bienvenido " + nombre + ". Usted esta conectado al servidor.\n";
	}
	public String cerrarSesion(String nombre, String clave)
		throws RemoteException
	{
		return "Cerrando sesion.\n";
	}

	public String listarArchivosEnServidor(String nombre, String clave)
		throws RemoteException
	{
		return "Listando archivos en el servidor.\n";
	}

	public String subirArchivo(String nombre, String clave)
	throws RemoteException
	{
		return "Subiendo archivo al servidor.\n";
	}

	public String bajarArchivo(String nombre, String clave)
	throws RemoteException
	{
		return "Bajando archivo desde el servidor.\n";
	}

	public String borrarArchivo(String nombre, String clave)
	throws RemoteException
	{
		return "Borrando archivo en el servidor.\n";
	}
}
