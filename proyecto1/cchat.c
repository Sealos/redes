#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <signal.h>
#include "lista.h"

#define BUFFER_SIZE 2048

int n_lei;
void terminar_ejecucion();

FILE *fp;
char* buffer;
char *buf;

/**
 * Funcion ejecutada por un hilo para escuchar del socket
 * @param Socket a escuchar
*/
void manejar_cliente(int *socket)
{
	buf = calloc(BUFFER_SIZE, sizeof(char));

	while (read(*socket, buf, BUFFER_SIZE))
	{
		n_lei = 1;
		if (!strcmp(buf, "close"))
		{
			printf("Finalizó su sesión en cchat \n");
			fclose(fp);
			free(buffer);
			free(buf);
			exit(0);
		}
		else
		{
			strcat(buf, "\0");
			printf("%s\n", buf);
			bzero(buf, BUFFER_SIZE*sizeof(char));
		}
	}
	fclose(fp);
	free(buffer);
	free(buf);
	exit(0);
}

/**
* Funcion que substituye la senal de Crtl-C y procede a cerrar el
* cliente
*/
void terminar_ejecucion()
{
	fclose(fp);
	free(buffer);
	free(buf);
	exit(0);
}

/**
 * Verica si un comando es valido o no
 * @param Comando a verificar
*/
int comando_valido(char *buffer)
{
	char *comando = calloc(4, sizeof(char));
	strncpy(comando, buffer, 4);

	if (!strcmp(comando, "cre ") || !strcmp(comando, "des\0") || !strcmp(comando, "eli ") ||
		!strcmp(comando, "fue\0") || !strcmp(comando2, "men ") || !strcmp(comando, "sal\0") ||
		!strcmp(comando, "sus ") || !strcmp(comando, "usu\0"))
		{
			free(comando);
			return 1;
		}

	free(comando);
	return 0;
}

/**
 * Programa principal
*/
int main (int argc, char **argv)
{
	char *host = NULL;
	char *puerto = NULL;
	char *nombre = NULL;
	char *archivo = NULL;
	int c;
	int requerimientos = 0;

	int fd_socket;
	struct sockaddr_in server;
	struct hostent *he;

	while ((c = getopt(argc, argv, "h:p:n:a:")) != -1)
	{
		switch (c)
		{
			case 'h':
				host = optarg;
				requerimientos = requerimientos | 1;
 				break;
			case 'p':
				puerto = optarg;
				requerimientos = requerimientos | 2;
				break;
			case 'n':
				nombre = optarg;
				requerimientos = requerimientos | 4;
				break;
			case 'a':
				archivo = optarg;
				requerimientos = requerimientos | 8;
				break;
			case '?':
				if (optopt == 'h' || optopt == 'p' || optopt == 'n' || optopt == 'a')
					fprintf (stderr, "Opcion -%c requiere un argumento.\n", optopt);
				else if (isprint (optopt))
					fprintf (stderr, "Opcion no soportada `-%c'.\n", optopt);
				return 1;
			default:
				abort ();
		}
	}

	// Atrapamos CRTL-C
	signal(SIGINT, terminar_ejecucion);
	// Creamos un arreglo
	buffer = calloc(BUFFER_SIZE, sizeof(char));

	if (!(requerimientos & 2))
	{
		puerto = "20203";
		requerimientos = requerimientos | 2;
	}

	if (!(requerimientos & 1))
	{
		host = "127.0.0.1";
		requerimientos = requerimientos | 1;
	}

	if (requerimientos != 15)
		printf("Faltan argumentos\n");
	else
	{
		// Creamos el socket
		if ((fd_socket = socket(AF_INET, SOCK_STREAM, 0)) == -1)
		{
			printf("Error al crear el socket\n");
			exit(-1);
		}

		if ((he = gethostbyname(host)) == NULL) 
		{
			printf("error de gethostbyname()\n");
			exit(-1);
		}

		server.sin_family = AF_INET;
		server.sin_port = htons(atoi(puerto)); 
		server.sin_addr = *((struct in_addr *)he->h_addr);

		if(connect(fd_socket, (struct sockaddr *) &server, sizeof(server)) == -1)
		{
			printf("Error al conectar\n");
			exit(-1);
		}

		pthread_t hilo;
		// Creamos el hilo
		// Hacemos el hilo para escuchar el servidor
		pthread_create(&hilo, NULL, (void *)manejar_cliente, &fd_socket);

		strcpy(buffer, "hi ");
		strcat(buffer, nombre);
		strcat(buffer, "\0");
		printf("<<%s\n", buffer);
		send(fd_socket,buffer,strlen(buffer),0);
		sleep(1);

		//Lectura del archivo de entrada
		if ((fp = fopen(archivo, "r" )) == NULL)
		{
			printf("error abriendo el archivo\n");
			exit(-1);
		}

		while(fgets(buffer, BUFFER_SIZE, fp) > 0)
		{
			n_lei = 0;
			//Linea del archivo, sin el caracter
			buffer[strlen(buffer) - 1] = '\0';

			if (comando_valido(buffer))
			{
				printf("<<%s\n", buffer);
				send(fd_socket, buffer, strlen(buffer), 0);
			}
			else
				printf("Verificar buffer fallido\n");
			while (!(n_lei))
			{
				printf('\0');
			}
		}

		bzero(buffer, BUFFER_SIZE*sizeof(char));
		while (fgets(buffer, BUFFER_SIZE, stdin))
		{
			buffer[strlen(buffer) - 1] = '\0';
			if (comando_valido(buffer))
			{
				printf("<<%s\n", buffer);
				send(fd_socket,buffer,strlen(buffer), 0);
			}
			else
			{
				printf("Comando no soportado\n");
				printf("Comandos disponibles:\ncre <sala>\ndes\neli <sala>\nfue\nmen <mensaje>\nsal\nsus <sala>\nusu\n\n");
			}
			bzero(buffer, BUFFER_SIZE*sizeof(char));
		}

		fclose (fp);
		free(buffer);

		close(fd_socket);
	}

	return 0;
}
