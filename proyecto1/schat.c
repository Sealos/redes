#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <signal.h>
#include "lista.h"

#define BUFFER_SIZE	2048
#define MAX_NOMBRE	20

/**
* Tipo de datos para manejar las salas en el servidor, estas incluyen:
* * Nombre de la sala
* * Usuarios conectados
*/
typedef struct sala
{
	char *nombre;
	lista *usuarios;
} sala;

/**
* Tipo de datos para manejar los usuarios en el servidor, estos incluyen:
* * Socket por donde se realiza la comunicacion con el cliente del usuario
* * Nombre del usuario
* * Salas a las cuales esta subscrito el usuario
*/
typedef struct usuario
{
	char *nombre;
	int socket;
	lista *salas_subscritas;
} usuario;

// Las variables son globales para poder obtener las variables en momento
// de cierre del programa
lista *salas;
lista *usuarios_conectados;
pthread_mutex_t lock;
lista *lista_hilos;
int sockfd;
int interrumpir_ejecucion;
char *nombre_sala = NULL;

/**
 * Desubscribe a un usuario de la sala
*/
int eliminar_usuario_salas(nodo *n, void *obj)
{
	sala *s = n->data;
	usuario *u = (usuario *)obj;
	if (s != NULL)
	{
		nodo *k = lista_get_node(s->usuarios, u);
		if (k)
			lista_remove_node(s->usuarios, k);
		else
		printf("eliminar_usuario_salas\n 61");
	}
	return 0;
}

/**
* Funcion para cerrar los sockets y limpiar las salas subscritas de un
* usuarios particular, usar con lista_iterate_function
*/
int limpiar_usuarios(nodo *n, void *obj)
{
	usuario *u = n->data;
	if (u != NULL)
	{
		close(u->socket);
		free(u->nombre);
		if (u->salas_subscritas)
			lista_free(u->salas_subscritas);
	}
	return 0;
}

/**
* Funcion para esperar todos los hilos actualmente ejecutando antes de
* cerrar el programa, usar con lista_iterate_function
*/
int esperar_hilos(nodo *n, void *obj)
{
	pthread_t *h = n->data;
	if (h != NULL)
		pthread_join(*h, NULL);
	return 0;
}

/**
* Funcion para limpiar los usuarios subscritos de una sala particular,
* usar con lista_iterate_function
*/
int limpiar_salas(nodo *n, void *obj)
{
	sala *s = n->data;
	if (s != NULL)
	{
		free(s->nombre);
		if (s->usuarios)
			lista_free(s->usuarios);
	}
	return 0;
}

/**
* Funcion para buscar si una sala existe o no
* usar con lista_iterate_function
*/
int buscar_sala(nodo *n, void *obj)
{
	char *nombre_buscar = (char *)obj;
	sala *s = n->data;
	if (s != NULL)
	{
		if (strcmp(nombre_buscar, s->nombre) == 0)
			return 1;
		else
			return 0;
	}
	return 0;
}

/**
* Funcion para buscar si un usuario existe o no
* usar con lista_iterate_function
*/
int buscar_usuario(nodo *n, void *obj)
{
	char *nombre_buscar = (char *)obj;
	usuario *u = n->data;
	if (u != NULL)
	{
		if (strcmp(nombre_buscar, u->nombre) == 0)
			return 1;
		else
			return 0;
	}
	return 0;
}

/**
 * Funcion para listar los usuarios
*/
int imprimir_usuario(nodo *n, void *obj)
{
	char *buffer = (char *)obj;
	usuario *u = n->data;
	if (u != NULL)
	{
		strcat(buffer, ">>");
		strcat(buffer, u->nombre);
		strcat(buffer, "\n");
	}
	return 0;
}

/**
 * Funcion para listar las salas
*/
int imprimir_sala(nodo *n, void *obj)
{
	char *buffer = (char *)obj;
	sala *s = n->data;
	if (s != NULL)
	{
		strcat(buffer, ">>");
		strcat(buffer, s->nombre);
		strcat(buffer, "\n");
	}
	return 0;
}

/**
 * Funcion para enviar mensaje a los usuarios
*/
int enviar_mensaje_usuarios(nodo *n, void *obj)
{
	char *mensaje = (char *)obj;
	usuario *u = n->data;
	if (u != NULL)
		send(u->socket, mensaje, BUFFER_SIZE, 0);
	return 0;
}

/**
 * Funcion para enviar mensaje a las salas
*/
int enviar_mensaje_salas(nodo *n, void *obj)
{
	char *mensaje = (char *)obj;
	sala *s = n->data;
	if (s != NULL)
		lista_iterate_function(s->usuarios, mensaje, enviar_mensaje_usuarios);
	return 0;
}

/**
 * Funcion para cerrar los clientes
*/
int cerrar_cliente(nodo *n, void *obj)
{
	usuario *u = n->data;
	if (u)
		send(u->socket, "close", 6, 0);
	return 0;
}

int eliminar_sala_usuario(nodo *n, void *obj)
{
	usuario *u = n->data;
	char *nombre_sala = (char *)obj;
	if (u)
	{
		nodo *n = lista_get_function(u->salas_subscritas, nombre_sala, buscar_sala);
		if (n)
			lista_remove_node(u->salas_subscritas, n);
	}
	return 0;
}

/**
* Funcion que substituye la senal de Crtl-C y procede a cerrar el
* servidor correctamente
*/
void terminar_ejecucion()
{
	// Cerramos el socket, no vamos a atender mas peticiones mientras
	// apagamos el servidor
	printf("Cerrando socket... ");
	close(sockfd);
	printf("OK!\n");
	printf("\nCerrando...\n");
	// Flag para los hilos para detener la ejecucion actual
	interrumpir_ejecucion = 1;
	// Cerramos los clientes
	lista_iterate_function(usuarios_conectados, NULL, cerrar_cliente);
	// Esperamos a que todos los hilos se detengan y luego cerramos
	printf("Esperando hilos... ");
	lista_iterate_function(lista_hilos, NULL, esperar_hilos);
	lista_destroy(lista_hilos);
	printf("OK!\n");
	// Destruimos el mutex
	pthread_mutex_destroy(&lock);
	// Primero eliminamos las listas de las salas subscritas por los
	// usuarios y cerramos los sockets de comunicación entre
	// cliente/servidor, esto causa que los hilos bloqueados por el read
	// se detengan y cierren
	printf("Limpiando usuarios... ");
	lista_iterate_function(usuarios_conectados, NULL, limpiar_usuarios);
	lista_destroy(usuarios_conectados);
	printf("OK!\n");
	// Procedemos a eliminar las listas de usuarios subscritos a una sala
	printf("Cerrando salas... ");
	lista_iterate_function(salas, NULL, limpiar_salas);
	lista_destroy(salas);
	printf("OK!\n");
	exit(0);
}

/**
* Fija los valores base para que el servidor inicie su ejecucion
*/
inline void iniciar_ejecucion(char *servidor)
{
	// Creamos la lista de salas
	salas = lista_new();
	sala *s = calloc(1, sizeof(sala));
	if (s)
		s->nombre = calloc(MAX_NOMBRE, sizeof(char));
	else
	{
		printf("Error calloc 255\n");
		exit(0);
	}
	// Agregamos la sala principal
	strcpy(s->nombre, servidor);
	// Y una lista de usuarios vacia
	s->usuarios = lista_new();
	lista_add(salas, s);
	// Creamos la lista de usuarios
	usuarios_conectados = lista_new();
	// Y lista de hilos
	lista_hilos = lista_new();
	// El servidor va a ejecutarse
	interrumpir_ejecucion = 0;
	pthread_mutex_init(&lock, NULL);
}

/**
 * Implementacion del comando cre <sala>
 * @param Nombre de la sala
 * @result Devuelve el string de resultado
*/
char *crear_sala(char *nombre_sala)
{
	char *buffer = calloc(1, BUFFER_SIZE);
	if (lista_iterate_function(salas, nombre_sala, buscar_sala) == 0)
	{
		sala *s = calloc(1, sizeof(sala));
		s->nombre = calloc(MAX_NOMBRE, sizeof(char));
		strcpy(s->nombre, nombre_sala);
		s->usuarios = lista_new();
		lista_add(salas, s);
		strcpy(buffer, "Sala creada\n\0");
	}
	else
		strcpy(buffer, "Ya existe una sala con ese nombre\n\0");

	return buffer;
}

/**
 * Implementacion del comando des
 * @param Usuario que se quiere desubscribir de las salas
 * @result Devuelve el string de resultado
*/
char *desubscribir_usuario_salas(usuario *u)
{
	// Removemos las salas subscritas del usuario
	char *buffer = calloc(1, BUFFER_SIZE);
	lista_iterate_function(u->salas_subscritas, u, eliminar_usuario_salas);
	free(u->salas_subscritas);
	u->salas_subscritas = lista_new();
	// Removemos el usuario de las salas
	strcpy(buffer, "Se desubscribio al usuario de todas las salas\n\0");
	return buffer;
}

/**
 * Implementacion del comando eli <sala>
 * @param Nombre de la sala
 * @result Devuelve el string de resultado
*/
char *eliminar_sala(char *nombre_sala)
{
	char *buffer = calloc(1, BUFFER_SIZE);
	nodo *s = lista_get_function(salas, nombre_sala, buscar_sala);
	if (s != NULL)
	{
		sala *sal = (sala *)s->data;
		lista_iterate_function(sal->usuarios, nombre_sala, eliminar_sala_usuario);
		lista_free(sal->usuarios);
		free(sal->nombre);
		free(sal);
		lista_remove_node(salas, s);
		strcpy(buffer, "Sala eliminada\n\0");
	}
	else
		strcpy(buffer, "No se encontro la sala\n\0");

	return buffer;
}

/**
 * Implementacion del comando men <mensaje>
 * @param Mensaje a enviar
 * @result Devuelve el string de resultado
*/
char *enviar_mensaje(char *mensaje, usuario *u)
{
	char *buffer = calloc(1, BUFFER_SIZE);
	char *mensaje_concat = calloc(strlen(mensaje) + 30, sizeof(char));
	if (mensaje_concat != NULL)
	{
		if (lista_size(u->salas_subscritas))
		{
			strcpy(mensaje_concat, ">>");
			strcat(mensaje_concat, u->nombre);
			strcat(mensaje_concat, ": ");
			strcat(mensaje_concat, mensaje);
			strcat(mensaje_concat, "\n\0");
			lista_iterate_function(u->salas_subscritas, mensaje_concat, enviar_mensaje_salas);
		}
		else
			strcat(buffer, "No estas subscrito a ninguna sala\n\0");
		free(mensaje_concat);
	}
	return buffer;
}

/**
 * Implementacion del comando sal
 * @result Devuelve las salas
*/
char *listar_salas()
{
	char *buffer = calloc(1, BUFFER_SIZE);
	lista_iterate_function(salas, buffer, imprimir_sala);
	return buffer;
}

/**
 * Implementacion del comando sus <sala>
 * @param Nombre de la sala
 * @param Usuario a subscribir
 * @result Devuelve el string de resultado
*/
char *subscribir_sala(char *nombre_sala, usuario *u)
{
	char *buffer = calloc(1, BUFFER_SIZE);
	nodo *s = lista_get_function(salas, nombre_sala, buscar_sala);
	if (s != NULL)
	{
		sala *sal = (sala *)s->data;
		if (lista_iterate_function(sal->usuarios, u->nombre, buscar_usuario) == 0)
		{
			lista_add(sal->usuarios, u);
			lista_add(u->salas_subscritas, sal);

			strcpy(buffer, "Subscrito exitosamente\n\0");
		}
		else
			strcpy(buffer, "Ya estas subscrito a esta sala\n\0");
	}
	else
		strcpy(buffer, "No se encontro la sala\n\0");

	return buffer;
}

/**
 * Implementacion del comando usu
 * @result Devuelve el string con todos los usuarios
*/
char *listar_usuarios()
{
	char *buffer = calloc(1, BUFFER_SIZE);
	lista_iterate_function(usuarios_conectados, buffer, imprimir_usuario);
	return buffer;
}

/**
 * Mini modulo para seleccion de funcion que se va a ejecutar
 * @param Comando
 * @param Usuario que invoca el comando
 * @result Devuelve el string de haber ejecutado un comando
*/
char *ejecutar_comando(char *comando, usuario *u)
{
	char *argumento = NULL;
	switch(*comando)
	{
		// Crear sala 'cre <sala>'
		case 'c':
			argumento = comando + 4;
			return crear_sala(argumento);
			break;
		// Desubscribir todas sala 'des'
		case 'd':
			return desubscribir_usuario_salas(u);
			break;
		// Eliminar sala 'eli <sala>'
		case 'e':
			argumento = comando + 4;
			return eliminar_sala(argumento);
			break;
		// Envia un mensaje 'men <mensaje>'
		case 'm':
			argumento = comando + 4;
			return enviar_mensaje(argumento, u);
			break;
		// Lista las salas 'sal'
		// Usuario se subscribe a una sala 'sus <sala>'
		case 's':
			comando++;
			switch(*comando)
			{
				// Lista las salas 'sal'
				case 'a':
					return listar_salas();
					break;
				// Usuario se subscribe a una sala 'sus <sala>'
				case 'u':
					argumento = comando + 3;
					return subscribir_sala(argumento, u);
					break;
			}
			break;
		// Lista todos los usuarios
		case 'u':
			return listar_usuarios();
			break;
		default:
			return NULL;
	}
	return NULL;
}

/**
* Funcion que elimina el usuario cuando este se desconecta
* @param El usuario a eliminar
*/
void eliminar_usuario(usuario *u)
{
	char *temp;
	temp = desubscribir_usuario_salas(u);
	free(temp);
	free(u->nombre);
	lista_free(u->salas_subscritas);
	nodo *k = lista_get_node(usuarios_conectados, u);
	if (k)
		lista_remove_node(usuarios_conectados, k);
	else
		printf("eliminar_usuario\n 496");
	close(u->socket);
	free(u);
}

/**
* Funcion que ejecutan los hilos
* @param El socket por donde el hilo se comunica con el usuario
*/
void manejar_usuario(int *socket)
{
	char *buffer = calloc(BUFFER_SIZE, sizeof(char));
	// Crear usuario
	usuario *u = calloc(1, sizeof(usuario));
	// Asignar socket a usuario
	u->socket = *socket;
	pthread_mutex_unlock(&lock);
	u->salas_subscritas = lista_new();
	u->nombre = calloc(MAX_NOMBRE, sizeof(char));
	// Agregar el usuario a la lista de conectados
	lista_add(usuarios_conectados, u);
	char *buffer2 = NULL;

	if (read(u->socket, buffer, BUFFER_SIZE) > 0)
	{
		char *nombre = buffer + 3;
		// Si el nombre tiene mas de 20 caracteres...
		if (strlen(nombre) < 20)
		{
			strcpy(u->nombre, nombre);
			bzero(buffer, BUFFER_SIZE*sizeof(char));
			strcpy(buffer, "Bienvenido ");
			strcat(buffer, u->nombre);
			strcat(buffer, "!\n\0");
			send(u->socket, buffer, strlen(buffer), 0);
			buffer2 = subscribir_sala(nombre_sala, u);
			free(buffer2);
			bzero(buffer, BUFFER_SIZE*sizeof(char));
		}
		else
		{
			send(u->socket, "close", 6, 0);
			eliminar_usuario(u);
			free(buffer);
			pthread_exit(NULL);
		}
	}

	while (read(u->socket, buffer, BUFFER_SIZE) > 0)
	{
		if (interrumpir_ejecucion)
		{
			free(buffer);
			send(u->socket, "close", 6, 0);
			eliminar_usuario(u);
			pthread_exit(NULL);
		}

		if (*buffer == 'f')
		{
			send(u->socket, "close", 6, 0);
			eliminar_usuario(u);
			free(buffer);
			pthread_exit(NULL);
		}

		// Si el comando es diferente de fue
		buffer2 = ejecutar_comando(buffer, u);
		// Hacer depende de lo que lea, hacer cosas
		send(u->socket, buffer2, strlen(buffer2), 0);
		free(buffer2);
		bzero(buffer, BUFFER_SIZE*sizeof(char));
	}

	eliminar_usuario(u);
	free(buffer);
	pthread_exit(NULL);
}

/**
 * Programa principal
*/
int main (int argc, char **argv)
{
	char *puerto = NULL;
	int c;
	int requerimientos = 0;

	int newsockfd;
	struct sockaddr_in clientaddr, serveraddr;
	socklen_t clientaddrlength;

	// Obtenemos los argumentos del programa
	while ((c = getopt (argc, argv, "p:s:")) != -1)
	{
		switch (c)
		{
			case 'p':
				puerto = optarg;
				requerimientos = requerimientos | 1;
				break;
			case 's':
				nombre_sala = optarg;
				requerimientos = requerimientos | 2;
				break;
			case '?':
				if (optopt == 'p' || optopt == 's')
					fprintf (stderr, "Opcion -%c requiere un argumento.\n", optopt);
				else if (isprint (optopt))
					fprintf (stderr, "Opcion no soportada `-%c'.\n", optopt);
				return 1;
			default:
				exit(0);
		}
	 }

	// Si no se especifico la sala, colocamos la default
	if (!(requerimientos & 2))
		nombre_sala = "actual\0";

	// Si no se especifico el puerto, colocamos el actual
	if (!(requerimientos & 1))
		puerto = "20203";

	// Atrapamos CRTL-C
	signal(SIGINT, terminar_ejecucion);
	// Iniciamos variables
	iniciar_ejecucion(nombre_sala);

	// Imprimimos los detalles de donde corre el servidor
	printf ("puerto = %s, servidor inicial = %s\n", puerto, nombre_sala);

	// Creamos el sockets
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if ((c = sockfd) < 0)
	{
		printf("Socket error %d\n", c);
	}

	// Bind
	bzero(&serveraddr, sizeof(serveraddr));
	serveraddr.sin_family = AF_INET;
	serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);
	serveraddr.sin_port = htons(atoi(puerto));
	if ((c = bind(sockfd, (struct sockaddr *) &serveraddr, sizeof(serveraddr))) != 0)
	{
		printf("Bind errord %c\n", c);
		close(sockfd);
	}

	if ((c = listen(sockfd, 20)) < 0)
	{
		printf("Listen error %d\n", c);
		exit(0);
	}

	pthread_t *hilo;
	while(1)
	{
		clientaddrlength = sizeof(clientaddr);

		pthread_mutex_lock(&lock);
		newsockfd = accept(sockfd, (struct sockaddr *) &clientaddr, &clientaddrlength);
		if (newsockfd < 0)
			printf("Accept error");
		// Creamos el hilo
		hilo = calloc(1, sizeof(pthread_t));
		// Guardamos el hilo en la lista
		lista_add(lista_hilos, hilo);
		// Hacemos que el hilo
		pthread_create(hilo, NULL, (void *)manejar_usuario, &newsockfd);
	}

	return 0;
}
