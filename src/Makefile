CC = gcc
CFLAGS = -g -Wall -fPIC -I. 
AR = ar
ARFLAGS =
LIBS =

LIBPREFIX = lib

LIBXMPDIR=libs/libxmp

include formats/midi/Makefile
include libs/timidity/src/Makefile
include $(LIBXMPDIR)/src/Makefile
include $(LIBXMPDIR)/src/loaders/Makefile

CFLAGS += -DHAVE_MKSTEMP -DHAVE_FNMATCH -DLIBXMP_CORE_PLAYER -DHAVE_ROUND \
		  -I$(LIBXMPDIR)/include/libxmp-lite -I$(LIBXMPDIR)/src -I$(LIBXMPDIR)/include

OBJS += common.o 

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