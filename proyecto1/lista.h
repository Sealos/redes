#ifndef LISTA_H
#define LISTA_H

/*
 * Thread-Safe Linked List
 */
#include <pthread.h>

// Nodo de la lista
typedef struct nodo
{
	void *data;
	struct nodo *next;
} nodo;

// Sentinela
typedef struct lista
{
	nodo *primero;
	nodo *ultimo;
	int size;
	pthread_mutex_t lock;
} lista;

// Devuelve una lista nueva
lista *lista_new();

// Agrega un elemento a la lista
int lista_add(lista *l, void *elemento);

// Obtiene el nodo de una lista, se asume que no hay valores repetidos
nodo *lista_get_node(lista *l, void *data);

// Remueve un nodo de la lista
int lista_remove_node(lista *l, nodo *n);

// Remueve un nodo de la lista que contenga al elemento
int lista_remove(lista *l, void *elemento);

// Libera la lista, no los valores
void lista_free(lista *l);

// Libera lista y valores
void lista_destroy(lista *l);

// Aplica una funcion con 2 argumentos al nodo
int lista_iterate_function(lista *l, void *obj, int (*call_back)(nodo *, void *));

// Devuelve el nodo que cumple una condicion
nodo *lista_get_function(lista *l, void *obj, int (*call_back)(nodo *, void *));

// Devuelve el tamano de la lista
int lista_size(lista *l);

#endif /* LISTA_H */
