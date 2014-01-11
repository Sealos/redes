
public class hash<E>
{
	private lista<E>[] table;
	private int size;
	private int cota;
	private int cont;

	/**
	 * Crea una tabla de hash
	 */
	@SuppressWarnings("unchecked")
	public hash(int tam)
	{
		this.size = 0;
		this.table = new lista[tam];
		this.cota = (tam * 3)/4;
		this.cont = 0;
	}

	/**
	 * Agrega <i>element</i> a la tabla usando la clase
	 * str.
	 *
	 * @param Elemento de tipo E, con el que se declaro el objeto
	 * @return true si el elemento fue insertado, false en caso contrario
	 */
	public boolean add(E e)
	{
		int modif = e.hashCode() % table.length;
		if (modif < 0)
			modif = modif + table.length;
		if (this.table[modif] == null)
		{
			this.table[modif] = new lista<E>();
			this.cont++;
		}
		if (this.table[modif].add(e))
		{
			size++;
			if (this.cota <= this.cont)
				this.rehash();
			return true;
		}
		else
			return false;
	}

	/**
	 * Reinicializa la tabla de hash
	 * queda como recien creada
	 */
	public void clear()
	{
		this.size = 0;
		for (int i = 0 ; i < this.table.length ; i++)
			this.table[i] = null;
	}

	/**
	 * Determina si el objeto <i>o</i> esta contenido en esta tabla.
	 * usando str como clave
	 * {@code Object equals}
	 *
	 * @see Object#equals
	 *
	 *
	 */
	public boolean contains(Object o)
	{
		@SuppressWarnings("unchecked")
		E aux = (E)o;
		int modif = aux.hashCode() % table.length;
		if (modif < 0)
			modif = modif + table.length;
		if (this.table[modif] != null)
			return table[modif].contains(o);
		else
			return false;
	}

	/**
	 * Determina si la tabla tiene elementos.
	 *
	 * @return true si size() &eq; 0. falso en caso contrario
	 */

	public boolean isEmpty()
	{
		return this.size == 0;
	}

	/**
	 * Retorna el numero de elementos en la tabla
	 *
	 * @return el numero de elementos en la tabla
	 */
	public int getSize()
	{
		return this.size;
	}

	/**
	 * Obtiene el elemento de la tabla de hash
	 * @return null si el elemento no esta en la tabla, en caso
	 * contrario devuelve el elemento
	 */
	public E getElem(E e)
	{
		int modif = e.hashCode() % this.table.length;
		if (modif < 0)
			modif = modif + table.length;
		if (this.table[modif] != null)
			return (E)table[modif].getElem(e);
		else
			return null;
	}

	/**
	 * Elimina un elemento de la tabla de hash
	 * @return Retorna true si el elemento se elimino, false en caso contrario
	 */
	public boolean remove(E e)
	{
		int modif = e.hashCode() % this.table.length;
		if (modif < 0)
			modif = modif + table.length;
		if (this.table[modif] != null && this.table[modif].remove(e))
		{
			size--;
			return true;
		}
		else
			return false;
	}

	/**
	 * Duplica la tabla de hash cuando llega la cantidad de elementos
	 * es 75% a el tamano de la tabla de hash para evitar colisiones
	 */
	@SuppressWarnings("unchecked")
	private void rehash()
	{
		lista<E> clone[] = new lista[this.table.length];
		System.arraycopy(this.table, 0, clone, 0, this.table.length);
		int aux = this.size;
		this.table = new lista[clone.length * 2];
		this.size = 0;
		this.cota = (this.table.length * 3)/4;
		this.cont = 0;
		for(int i = 0; i < clone.length && aux > 0; i++)
		{
			if (clone[i] != null)
			{
				cola<E> cl = new cola<E>(clone[i]);
				E temp;
				while((temp = cl.pop()) != null)
				{
					this.add(temp); 
					aux--;
				}
				clone[i] = null;
			}
		}
	}

	public lista<E> getList()
	{
		lista<E> l = new lista<E>();
		for (int i = 0; i < table.length; i++)
		{
			if (table[i] != null)
				l.concatenar(table[i]);
		}

		return l;
	}
}
