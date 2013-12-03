#include "lista.h"
#include <stdlib.h>

/*
 * Thread-Safe Linked List
 */

/**
* Inicializa una lista nueva
*
* @return Si se pudo alocar la memoria y inicializar el hilo, devuelve la lista
*/
lista *lista_new()
{
	lista *l = (lista *)calloc(1, sizeof(lista));
	if (l)
	{
		l->primero = NULL;
		l->ultimo = NULL;
		l->size = 0;
	}
	else
		return NULL;

	if (pthread_mutex_init(&(l->lock), NULL) != 0)
	{
		free(l);
		return NULL;
	}

	return l;
}

/**
* Agrega un apuntador al final de la lista
* @param Lista en donde se va a agregar el elemento
* @param Elemento a agregar en la lista
* @return Devuelve 1 se se agrega el elemento, 0 en caso contrario
*/
int lista_add(lista *l, void *elemento)
{
	nodo *caja = (nodo *)calloc(1, sizeof(nodo));

	// Pidiendo memoria...
	if (caja)
	{
		// Agregamos los valores a la lista
		caja->data = elemento;
		caja->next = NULL;

		// Entrando en seccion critica
		pthread_mutex_lock(&(l->lock));
		// Primer elemento agregado
		if (l->size == 0)
			l->primero = caja;
		else
		{
			// Si no es el primero, vamos al ultimo elemento
			// y lo agregamos ahi, actualizamos el ultimo
			l->ultimo->next = caja;
		}

		// Actualizamos la lista
		l->ultimo = caja;
		l->size = l->size + 1;

		// Saliendo seccion critica
		pthread_mutex_unlock(&(l->lock));
		return 1;
	}
	else
		return 0;
}

/**
* Obtine el nodo que contiene al elemento
* @param Lista en donde se buscara el elemento
* @param Elemento a buscar en la lista
* @return Devuelve el nodo si lo consigue, NULL en otro caso
*/
nodo *lista_get_node(lista *l, void *elemento)
{
	if (l == NULL)
		return NULL;

	// Buscamos el nodo
	nodo *auxiliar = l->primero;

	// Iteramos
	while (auxiliar && auxiliar->data != elemento)
		auxiliar = auxiliar->next;

	// Si lo conseguimos...
	if (auxiliar)
		return auxiliar;
	else
		return NULL;
}

/**
* Elimina al nodo que contiene al elemento
* @param Lista en donde se buscara el elemento
* @param Elemento a buscar en la lista
* @return Devuelve 1 si se elimina el nodo, 0 en caso contrario
*/
int lista_remove_node(lista *l, nodo *n)
{
	if (l == NULL || l->size == 0)
		return 0;

	// Entrando seccion critica
	pthread_mutex_lock(&(l->lock));

	// Buscamos el nodo
	nodo *auxiliar = l->primero;

	// Es el primero?
	if (auxiliar == n)
	{
		l->primero = auxiliar->next;
		free(n);

		// Lista de elemento unico
		if (l->size == 1)
			l->ultimo = NULL;

		l->size = l->size - 1;
		pthread_mutex_unlock(&(l->lock));
		return 1;
	}

	while (auxiliar && auxiliar->next != n)
		auxiliar = auxiliar->next;

	// Si lo conseguimos...
	if (auxiliar->next)
	{
		// Es el ultimo
		if (n->next == NULL)
			l->ultimo = auxiliar;

		// El anterior apunta al que apuntaba el nodo
		auxiliar->next = n->next;
		free(n);
		l->size = l->size - 1;
		pthread_mutex_unlock(&(l->lock));
		return 1;
	}
	else
	{
		pthread_mutex_unlock(&(l->lock));
		return 0;
	}
}

/**
* Elimina al nodo que contiene al elemento
* @param Lista en donde se buscara el elemento
* @param Elemento a buscar en la lista
* @return Devuelve 1 si se elimina el nodo, 0 en caso contrario
*/
int lista_remove(lista *l, void *elemento)
{
	if (l == NULL || l->size == 0)
		return 0;

	// Entrando seccion critica
	pthread_mutex_lock(&(l->lock));

	// Buscamos el nodo
	nodo *auxiliar = l->primero;

	// Es el primero?
	if (auxiliar->data == elemento)
	{
		l->primero = auxiliar->next;
		free(auxiliar);

		// Lista de elemento unico
		if (l->size == 1)
			l->ultimo = NULL;

		l->size = l->size - 1;
		pthread_mutex_unlock(&(l->lock));
		return 1;
	}

	while (auxiliar && auxiliar->next->data != elemento)
		auxiliar = auxiliar->next;

	// Si lo conseguimos...
	if (auxiliar->next)
	{
		// Es el ultimo
		if (auxiliar->next->next == NULL)
			l->ultimo = auxiliar;

		// El anterior apunta al que apuntaba el nodo
		auxiliar->next = auxiliar->next->next;
		free(auxiliar->next);
		l->size = l->size - 1;
		pthread_mutex_unlock(&(l->lock));
		return 1;
	}
	else
	{
		pthread_mutex_unlock(&(l->lock));
		return 0;
	}
}

/**
* Destruye los nodos, los valores guardados y finalmente la lista
* @param Lista a eliminar
*/
void lista_destroy(lista *l)
{
	pthread_mutex_lock(&(l->lock));
	nodo *li, *tmp;

	if (l != NULL)
	{
		// Obtenemos el primero elemento y iteramos sobre el
		li = l->primero;
		while (li != NULL)
		{
			tmp = li->next;
			// Liberamos el valor y luego el nodo
			free(li->data);
			free(li);
			li = tmp;
		}
	}

	pthread_mutex_unlock(&(l->lock));
	pthread_mutex_destroy(&(l->lock));
	free(l);
}

/**
* Destruye los nodos y finalmente la lista
* No elimina los apuntadores a los valores ni libera la memoria
* @param Lista a eliminar
*/
void lista_free(lista *l)
{
	nodo *li, *tmp;

	pthread_mutex_lock(&(l->lock));

	if (l != NULL)
	{
		// Obtenemos el primero elemento y iteramos sobre el
		li = l->primero;
		while (li != NULL)
		{
			// Liberamos el nodo
			tmp = li->next;
			free(li);
			li = tmp;
		}
	}

	pthread_mutex_unlock(&(l->lock));
	pthread_mutex_destroy(&(l->lock));
	free(l);
}

/**
* Aplica una funcion a todos los nodos de la lista
* @param Lista a iterar
* @param Segundo parametro
* @param Funcion a aplicar
* @return Devuelve 1 si f(param1, param2) es igual a 1, 0 en otro caso
*/
int lista_iterate_function(lista *l, void *obj, int (*call_back)(nodo *, void *))
{
	nodo *caja;
	if (l == NULL)
		return 0;
	pthread_mutex_lock(&(l->lock));

	caja = l->primero;
	while (caja != NULL)
	{
		if (call_back(caja, obj) == 1)
		{
			pthread_mutex_unlock(&(l->lock));
			return 1;
		}
		caja = caja->next;
	}
	pthread_mutex_unlock(&(l->lock));
	return 0;
}

/**
* Aplica una funcion a todos los nodos de la lista y devuelve el nodo
* que primero devuelve 1
* @param Lista a iterar
* @param Segundo parametro
* @param Funcion a aplicar
* @return Devuelve null si ningun nodo cumple la funcion, o el primer
* nodo que lo cumpla
*/
nodo *lista_get_function(lista *l, void *obj, int (*call_back)(nodo *, void *))
{
	nodo *caja;

	if (l == NULL)
		return 0;
	pthread_mutex_lock(&(l->lock));

	caja = l->primero;
	while (caja != NULL)
	{
		if (call_back(caja, obj) == 1)
		{
			pthread_mutex_unlock(&(l->lock));
			return caja;
		}
		caja = caja->next;
	}
	pthread_mutex_unlock(&(l->lock));
	return NULL;
}

/**
* Retorna el tamano de la lista
* @param Lista
* @return Tamano de la lista
*/
int lista_size(lista *l)
{
	if (l != NULL)
		return l->size;
	else
		return 0;
}
