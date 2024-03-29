CC = gcc
LD = gcc

CFLAGS = -g -O2 -Wall -I .

LDFLAGS = -shared 
LIBARCH =

LIBPREFIX = lib
LDFLAGS += -lstdc++ -lm
LDFLAGS += $(shell pkg-config --libs fluidsynth)

LIBXMPDIR = libs/libxmp
LIBGMEDIR = libs/game-music-emu
LIBGMEDIR_BUILD = $(LIBGMEDIR)/build
LIBGME = $(LIBGMEDIR_BUILD)/gme/libgme.a

# include libs/midiloader/Makefile
include libs/timidity/src/Makefile
include $(LIBXMPDIR)/src/Makefile
include $(LIBXMPDIR)/src/loaders/Makefile

CFLAGS += -Imidi/
CFLAGS += -DHAVE_MKSTEMP -DHAVE_FNMATCH -DLIBXMP_CORE_PLAYER -DHAVE_ROUND \
		  -I$(LIBXMPDIR)/include/libxmp-lite -I$(LIBXMPDIR)/src -I$(LIBXMPDIR)/include -I$(LIBGMEDIR)

UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)
ifeq ($(UNAME_S),Linux)
        ifeq ($(UNAME_M),armv7l)
    	    JAVA_HOME = /usr/lib/jvm/java-11-openjdk-armhf
    	    LIBARCH = armv7l
        else
    	    JAVA_HOME = /usr/lib/jvm/java-8-oracle
	    ifeq ($(UNAME_M),x86_64)
		LIBARCH = x86_64
            endif
        endif
	CFLAGS += -D LINUX -fPIC -I$(JAVA_HOME)/include/linux/ -I$(JAVA_HOME)/include/
	LIBEXT = so
	ifneq ($(filter %86,$(UNAME_M)),)
	endif
endif

BUILD_DIR=../../../build/jni
OBJDIR = $(BUILD_DIR)/obj
LIBSDIR = $(BUILD_DIR)/libs

LINK_TARGET = $(LIBSDIR)/$(LIBARCH)/$(LIBPREFIX)trax-corelib.$(LIBEXT)
 
CFLAGS_ONLY = -std=gnu99
  
OBJS += $(addprefix $(OBJDIR)/, \
   common.o \
	modplayer.o \
	fluidplayer.o \
	gmeplayer.o \
	)
		
$(OBJDIR)/%.o: %.c 
	$(CC) -c $(CFLAGS) $(CFLAGS_ONLY) -o $@ $< 

$(OBJDIR)/%.o: %.cpp
	$(CC) -c $(CFLAGS) -o $@ $< 

all : prepare $(LINK_TARGET)

$(LINK_TARGET) : $(CORE) $(OBJS) $(LIBGME)
	$(LD) -o $@ $^ $(LDFLAGS) 

prepare :
	@mkdir -p $(LIBSDIR)/$(LIBARCH)
	@mkdir -p $(OBJDIR)

clean :
	@find $(OBJDIR) -name "*.o" -exec rm {} \; 2>/dev/null
	@find $(LIBXMPDIR) -name "*.o" -exec rm {} \; 2>/dev/null
	@rm -rf $(LIBGMEDIR_BUILD) 2>/dev/null || true
	@rm $(LINK_TARGET) 2>/dev/null || true

$(LIBGME) :
	@mkdir -p $(LIBGMEDIR)/build
	cd $(LIBGMEDIR)/build && cmake ../ -DBUILD_SHARED_LIBS=OFF && make
