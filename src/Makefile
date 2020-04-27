CC = gcc
CFLAGS = -g -Wall -fPIC -I. -DNANOVG_GLES2_IMPLEMENTATION -DGL_GLEXT_PROTOTYPES -Wno-misleading-indentation
AR = ar
ARFLAGS =
LIBS =

LIBPREFIX = lib

include formats/midi/Makefile

OBJS += common.o \
	ui.o \
	libs/nanovg/nanovg.o

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