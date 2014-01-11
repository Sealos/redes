// Comandos log sal
// java s_rmifs -l puertolocal -h host -r puerto 
// puertolocal: Donde esta el rmiregistry info de los objetos del servidor de archivo
// host: Donde corre el servidor de autenticacion
// puerto: Donde esta el rmiregistry info de los objetos del servidor de autenticacion

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

class s_rmifs
{
	public s_rmifs(Integer rmi_port, String aut, Integer puerto)
	{
		try
		{
			s_rmifs_stub stub = new s_rmifs_stub();
			LocateRegistry.createRegistry(rmi_port);
			Naming.rebind("rmi://localhost:"+ rmi_port +"/c_s_services", stub);
		}
		catch (Exception e)
		{
			System.out.println (e);
		}
	}

	public static void main(String[] args)
	{
		new s_rmifs(20203, "localhost", 20203);
	}
}
