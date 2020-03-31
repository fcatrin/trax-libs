CC = gcc
LD = gcc

CFLAGS = -g -Wall

LDFLAGS = -lm -shared 
LIBPREFIX =
LIBARCH =

LIBPREFIX = lib
LDFLAGS += -lstdc++

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
 
CFLAGS_ONLY = -std=gnu99
  
OBJS = $(addprefix $(OBJDIR)/, \
	main.o \
	)
		
$(OBJDIR)/%.o: %.c 
	$(CC) -c $(CFLAGS) $(CFLAGS_ONLY) -o $@ $< 

$(OBJDIR)/%.o: %.cpp
	$(CC) -c $(CFLAGS) -o $@ $< 

all : prepare $(LINK_TARGET)

$(LINK_TARGET) : $(OBJS)
	$(LD) -o $@ $^ $(LDFLAGS)

prepare :
	mkdir -p $(LIBSDIR)/$(LIBARCH)
	mkdir -p $(OBJDIR)

clean :
	find $(OBJDIR) -name "*.o" -exec rm {} \;
	find $(LIBSDIR) -name "*.$(LIBEXT)" -exec rm {} \;
