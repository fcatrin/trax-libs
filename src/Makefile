CC = gcc
CFLAGS = -g -Wall -fPIC
LDFLAGS = -shared
LIBS =

include formats/midi/Makefile

OBJDIR = ../obj

ALL_OBJS = $(addprefix $(OBJDIR)/, $(OBJS))

TARGET = simusplayer-core.so

all: $(TARGET)

$(OBJDIR)/%.o: %.c
	@mkdir -p $(dir $@) 2> /dev/null 
	$(CC) -c -o $@ -I. $(CFLAGS) $<
	
	
$(TARGET): $(ALL_OBJS)
	$(CC) -o $@ $(LDFLAGS) $(ALL_OBJS) $(LIBS)

clean:
	rm -f $(ALL_OBJS)