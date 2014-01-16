public class cola<E> extends lista<E>
{
	public cola()
	{
		super();
	}

	public cola(lista<E> list)
	{
		head = list.head;
		tail = list.tail;
		size = list.size;
	}

	/**
	 * Obtiene el primer elemento de la cola
	 * 
	 * @return
	 */
	public E pop()
	{
		E ele = null;
		if (size >= 1)
		{
			ele = head.elem;
			head = head.next;
			if (size == 1)
				tail = null;
			size--;
		}
		return ele;
	}

	public E getNext()
	{
		return head.elem;
	}
}
// End Cola.
