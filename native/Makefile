# Biblioteca nativa libnumerico.so
# Uso: make        -> compila la biblioteca
#      make test   -> compila y ejecuta las pruebas en C
CC      ?= gcc
CFLAGS  := -Wall -O2 -fPIC -Iinclude
LDLIBS  := -lm

LIB     := libnumerico.so
SRC     := src/numerico.c
TEST    := test/test_numerico

all: $(LIB)

$(LIB): $(SRC) include/numerico.h
	$(CC) $(CFLAGS) -shared -o $@ $(SRC) $(LDLIBS)

# Las pruebas se enlazan contra el fuente para no depender de la ruta de la .so
$(TEST): test/test_numerico.c $(SRC) include/numerico.h
	$(CC) -Wall -O2 -Iinclude -o $@ test/test_numerico.c $(SRC) $(LDLIBS)

test: $(TEST)
	./$(TEST)

clean:
	rm -f $(LIB) $(TEST)

.PHONY: all test clean
