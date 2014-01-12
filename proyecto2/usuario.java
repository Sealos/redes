public class usuario
{
	private String name;
	private String pas;

	usuario(String name, String pas)
	{
		this.name = name;
		this.pas = pas;
	}

	/**
	 * @return the name
	 */
	public String get_name()
	{
		return name;
	}

	/**
	 * @return the pas
	 */
	public String get_pas()
	{
		return pas;
	}

	public int hashCode()
	{
		return (name + pas).hashCode();
	}

	public boolean equals(Object o)
	{
		if (o instanceof usuario)
		{
			usuario u = (usuario) o;
			return u.pas.equals(pas) && u.name.equals(name);
		}
		else
			return false;
	}
}
