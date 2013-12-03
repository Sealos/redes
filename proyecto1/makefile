TARGET = schat cchat
LIBS = -pthread
CC = gcc
CFLAGS = -Wall
DFLAGS = -g -Wall

all: lista.o
	$(CC) cchat.c lista.o $(CFLAGS) $(LIBS) -o cchat
	$(CC) schat.c lista.o $(CFLAGS) $(LIBS) -o schat

%.o: %.c
	$(CC) $(DFLAGS) $(LIBS) -c $< -o $@

clean:
	rm cchat schat lista.o

rebuild: clean all

debug: lista.o
	$(CC) cchat.c lista.o $(DFLAGS) $(LIBS) -o cchat
	$(CC) schat.c lista.o $(DFLAGS) $(LIBS) -o schat

