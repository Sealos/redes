import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.*;

public class a_rmifs_stub
	extends UnicastRemoteObject
	implements s_a_services
{

	private static final long serialVersionUID = 4332409159215010949L;
	hash<usuario> users;

	public a_rmifs_stub(String f_users)
		throws RemoteException
	{
		super();
		users = new hash<usuario>(20);
		String[] ln;

		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(f_users));
 
			while ((line = br.readLine()) != null)
			{
				System.out.println(line);
				ln = line.split(":");
				users.add(new usuario(ln[0], ln[1]));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public boolean validate(String nombre, String clave)
		throws RemoteException
	{
		return users.contains(new usuario(nombre, clave));
	}
}
