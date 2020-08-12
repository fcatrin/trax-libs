CC = gcc
LD = gcc

CFLAGS = -g -O2 -Wall -I .

LDFLAGS = -shared 
LIBARCH =

LIBPREFIX = lib
LDFLAGS += -lstdc++ -lm 
LDFLAGS += $(shell pkg-config --libs fluidsynth)

LIBXMPDIR=libs/libxmp

include libs/midiloader/Makefile
include libs/timidity/src/Makefile
include $(LIBXMPDIR)/src/Makefile
include $(LIBXMPDIR)/src/loaders/Makefile

CFLAGS += -Imidi/
CFLAGS += -DHAVE_MKSTEMP -DHAVE_FNMATCH -DLIBXMP_CORE_PLAYER -DHAVE_ROUND \
		  -I$(LIBXMPDIR)/include/libxmp-lite -I$(LIBXMPDIR)/src -I$(LIBXMPDIR)/include

UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
ifeq ($(UNAME_S),Linux)
	JAVA_HOME = /usr/lib/jvm/java-8-oracle
	CFLAGS += -D LINUX -fPIC -I$(JAVA_HOME)/include/linux/ -I$(JAVA_HOME)/include/
	LIBEXT = so
	ifeq ($(UNAME_P),x86_64)
		LIBARCH = x86_64
	endif
	ifneq ($(filter %86,$(UNAME_P)),)
	endif
endif
 
OBJDIR = ../obj
LIBSDIR = ../libs

LINK_TARGET = $(LIBSDIR)/$(LIBARCH)/$(LIBPREFIX)simusplayer-corelib.$(LIBEXT)
 
CFLAGS_ONLY = -std=gnu99
  
OBJS += $(addprefix $(OBJDIR)/, \
   common.o \
	modplayer.o \
	fluidplayer.o \
	)
		
$(OBJDIR)/%.o: %.c 
	$(CC) -c $(CFLAGS) $(CFLAGS_ONLY) -o $@ $< 

$(OBJDIR)/%.o: %.cpp
	$(CC) -c $(CFLAGS) -o $@ $< 

all : prepare $(LINK_TARGET)

$(LINK_TARGET) : $(CORE) $(OBJS)
	$(LD) -o $@ $^ $(LDFLAGS) 

prepare :
	@mkdir -p $(LIBSDIR)/$(LIBARCH)
	@mkdir -p $(OBJDIR)

clean :
	@find $(OBJDIR) -name "*.o" -exec rm {} \; 2>/dev/null
	@rm $(LINK_TARGET) 2>/dev/null || true
