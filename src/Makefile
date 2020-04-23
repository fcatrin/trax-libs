CC = gcc
CFLAGS = -g -Wall
AR = ar
ARFLAGS =
LIBS =

LIBPREFIX = lib

include formats/midi/Makefile

OBJDIR = ../obj

ALL_OBJS = $(addprefix $(OBJDIR)/, $(OBJS))

TARGET = $(LIBPREFIX)simusplayer-core.a

all: $(TARGET)

$(OBJDIR)/%.o: %.c
	@mkdir -p $(dir $@) 2> /dev/null 
	$(CC) -c -o $@ -I. $(CFLAGS) $<
	
	
$(TARGET): $(ALL_OBJS)
	$(AR) rcs -o $@ $(ARFLAGS) $(ALL_OBJS) $(LIBS)

clean:
	rm -f $(ALL_OBJS) 2>/dev/null
	rm -f $(TARGET) 2>/dev/null