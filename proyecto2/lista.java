/**
 * Clase que implementa la interfaz List Esta es una clase parametrizada con tipo (clase) E; i.e., la lista contiene
 * elementos de tipo E.
 */
public class lista<E>
{
	protected Caja head;
	protected Caja tail;
	protected int size;

	protected class Caja
	{
		protected E elem;
		protected Caja next;

		public Caja(E elemento)
		{
			elem = elemento;
			next = null;
		}

		public void setNext(Caja a)
		{
			next = a;
		}

		public Caja getNext()
		{
			return next;
		}

		public E getElem()
		{
			return elem;
		}
	}

	/**
	 * Crea una lista de tipos E.
	 */
	public lista()
	{
		size = 0;
		head = null;
		tail = null;
	}

	/**
	 * Agrega un elemento al final de la lista.
	 */
	public boolean add(E element)
	{
		if (!this.contains(element))
		{
			Caja a = new Caja(element);

			if (head == null)
				head = a;
			else
				tail.setNext(a);

			tail = a;
			size++;
			return true;
		}
		else
			return false;
	}

	/**
	 * Elimina todos los elementos de la lista. La lista queda como recien creada.
	 */
	public void clear()
	{
		head = null;
		tail = null;
		size = 0;
	}

	/**
	 * Determina si el elemento dado esta en la lista.
	 */
	@SuppressWarnings("unchecked")
	public boolean contains(Object element)
	{

		Iterador iter = this.iterador();
		boolean c = false;
		E aux;
		while (iter.hasNext() && !c)
		{
			aux = (E) iter.next();
			c = aux.equals(element);
		}
		return c;
	}

	/**
	 * Determina si la lista dada es igual a la lista.
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object list)
	{
		lista<E> lista = (list instanceof lista<?>) ? (lista<E>) list : null;
		if (lista == null)
			return false;

		if (this.size != lista.getSize())
			return false;
		else
		{
			E aux;
			boolean b = true;
			Iterador iter = lista.iterador();
			while (iter.hasNext() && b)
			{
				aux = (E) iter.next();
				b = this.contains(aux);
			}
			return b;
		}
	}

	/**
	 * Determina si la lista es vacia.
	 */
	public boolean isEmpty()
	{
		return head == null;
	}

	/**
	 * Retorna el numero de elementos en la lista
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Retorna un arreglo que contiene todos los elementos en esta lista {@code Milista}.
	 * 
	 * @return an array of the elements from this {@code Milista}.
	 */

	@SuppressWarnings("unchecked")
	public Object[] toArray()
	{
		Object[] a = new Object[size];
		if (this.head == null)
			return null;
		else
		{
			Iterador iter = this.iterador();
			for (int i = 0; iter.hasNext(); i++)
				a[i] = (E) iter.next();
			return a;
		}
	}

	/**
	 * Devuelve el elemento en la lista copia de e si no existe devuelve null.
	 */

	@SuppressWarnings("unchecked")
	public E getElem(E e)
	{
		Iterador iter = this.iterador();
		E aux;
		while (iter.hasNext())
		{
			aux = (E) iter.next();
			if (aux.equals(e))
				return aux;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public lista<E> clone()
	{
		lista<E> f = new lista<E>();
		Iterador iter = iterador();
		while (iter.hasNext())
			f.add((E) iter.next());

		return f;
	}

	/**
	 * Elimina el elemento dado de la lista. Si la lista cambia, retorna true, sino retorna false.
	 */
	public boolean remove(E element)
	{
		if (this.size == 0)
			return false;
		else
		{
			Caja actual = this.head;
			Caja anterior = null;
			while (actual != null && !actual.getElem().equals(element))
			{
				anterior = actual;
				actual = actual.getNext();
			}

			if (actual != null)
			{
				if (anterior != null)
				{
					anterior.setNext(actual.getNext());
				}
				else
				{
					this.head = actual.getNext();
				}
				this.size--;
				return true;
			}
			else
				return false;
		}
	}

	@SuppressWarnings("unchecked")
	public void concatenar(lista<E> l)
	{
		Iterador iter = l.iterador();
		E aux;
		while (iter.hasNext())
		{
			aux = (E) iter.next();
			add(aux);
		}
	}

	/**
	 * Devuelve un Iterador sobre this.
	 */

	public Iterador iterador()
	{
		return new Iterador(this);
	}

	private class Iterador
	{
		private Caja pos, aux;

		public Iterador(lista<E> list)
		{
			this.pos = list.head;
			this.aux = null;
		}

		public boolean hasNext()
		{
			return this.pos != null;
		}

		public Object next()
		{
			this.aux = this.pos;
			this.pos = this.pos.getNext();
			return aux.getElem();
		}
	}

}

// End List.
