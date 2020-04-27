CC = gcc
LD = gcc

CFLAGS = -g -Wall -I../../..

LDFLAGS = -lm -shared -L../../../ -lsimusplayer-core -lSDL2 -lGL
LIBARCH =

LIBPREFIX = lib
LDFLAGS += -lstdc++
LDFLAGS += $(shell pkg-config --libs freetype2)

CORE = ../../../libsimusplayer-core.a

UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
ifeq ($(UNAME_S),Linux)
	JAVA_HOME = /usr/lib/jvm/java-8-oracle/
	CFLAGS += -D LINUX -fPIC -I$(JAVA_HOME)/include/linux/
	LIBEXT = so
	ifeq ($(UNAME_P),x86_64)
		LIBARCH = x86_64
	endif
	ifneq ($(filter %86,$(UNAME_P)),)
	endif
endif
 
OBJDIR = ../obj
LIBSDIR = ../libs

LINK_TARGET = $(LIBSDIR)/$(LIBARCH)/$(LIBPREFIX)simusplayer.$(LIBEXT)

CFLAGS += \
 -O2 -Wall -I . -g -I$(JAVA_HOME)/include/ 

CFLAGS += $(shell pkg-config --cflags alsa)
LDFLAGS += $(shell pkg-config --libs alsa)
 
CFLAGS_ONLY = -std=gnu99
  
OBJS = $(addprefix $(OBJDIR)/, \
	main.o \
	alsalib.o \
	midiplayer.o \
	window.o \
	)
		
$(OBJDIR)/%.o: %.c 
	$(CC) -c $(CFLAGS) $(CFLAGS_ONLY) -o $@ $< 

$(OBJDIR)/%.o: %.cpp
	$(CC) -c $(CFLAGS) -o $@ $< 

all : prepare corebuild $(LINK_TARGET)

$(LINK_TARGET) : $(CORE) $(OBJS)
	$(LD) -o $@ $^ $(LDFLAGS) 

prepare :
	@mkdir -p $(LIBSDIR)/$(LIBARCH)
	@mkdir -p $(OBJDIR)

corebuild:
	cd ../../../ && $(MAKE)

clean :
	find $(OBJDIR) -name "*.o" -exec rm {} \;
	find $(LIBSDIR) -name "*.$(LIBEXT)" -exec rm {} \;
	cd ../../../ && $(MAKE) clean
