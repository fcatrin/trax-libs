CC = gcc
LD = gcc

CFLAGS = -g -Wall -I../../..

CORELIBDIR=../../../corelib/jni

LDFLAGS = -lm -shared -L../../../ -lsimusplayer-core -lSDL2 -lGL
LIBARCH =

LIBPREFIX = lib
LDFLAGS += -lstdc++
LDFLAGS += $(shell pkg-config --libs freetype2)

CORE = ../../../libsimusplayer-core.a

UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
UNAME_M := $(shell uname -m)
ifeq ($(UNAME_S),Linux)
        ifeq ($(UNAME_M),armv7l)
    	    JAVA_HOME = /usr/lib/jvm/java-11-openjdk-armhf
            LIBARCH = armv7l
        else
	    JAVA_HOME = /usr/lib/jvm/java-8-oracle
    	    ifeq ($(UNAME_P),x86_64)
		LIBARCH = x86_64
    	    endif
        endif
	CFLAGS += -D LINUX -fPIC -I$(JAVA_HOME)/include/linux/
	LIBEXT = so
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
	)
		
$(OBJDIR)/%.o: %.c 
	$(CC) -c $(CFLAGS) $(CFLAGS_ONLY) -o $@ $< 

$(OBJDIR)/%.o: %.cpp
	$(CC) -c $(CFLAGS) -o $@ $< 

all : prepare corebuild corelib $(LINK_TARGET)

$(LINK_TARGET) : $(CORE) $(OBJS)
	$(LD) -o $@ $^ $(LDFLAGS) 

prepare :
	@mkdir -p $(LIBSDIR)/$(LIBARCH)
	@mkdir -p $(OBJDIR)

corebuild:
	cd ../../../ && $(MAKE)
	
corelib:
	cd $(CORELIBDIR) && $(MAKE)
	cp $(CORELIBDIR)/$(LIBSDIR)/$(LIBARCH)/lib*.so $(LIBSDIR)/$(LIBARCH)

clean :
	find $(OBJDIR) -name "*.o" -exec rm {} \;
	find $(LIBSDIR) -name "*.$(LIBEXT)" -exec rm {} \;
	cd ../../../ && $(MAKE) clean
	cd $(CORELIBDIR) && $(MAKE) clean
