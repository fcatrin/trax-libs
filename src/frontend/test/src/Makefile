CC = gcc
LD = gcc

CFLAGS = -g -O2 -Wall -I../../.. -D LINUX

LDFLAGS = -lm -L../../../ -lsimusplayer-core

OBJDIR = ../obj

LINK_TARGET = simusplayer_test

CFLAGS += \
   
  
OBJS = $(addprefix $(OBJDIR)/, \
	main.o \
	)
		
$(OBJDIR)/%.o: %.c 
	$(CC) -c $(CFLAGS) $(CFLAGS_ONLY) -o $@ $< 

$(OBJDIR)/%.o: %.cpp
	$(CC) -c $(CFLAGS) -o $@ $< 

all : prepare core $(LINK_TARGET)

$(LINK_TARGET) : $(OBJS)
	$(CC) -o $@ $^ $(LDFLAGS) 

prepare :
	@mkdir -p $(OBJDIR)

core:
	cd ../../../ && $(MAKE)

clean :
	find $(OBJDIR) -name "*.o" -exec rm {} \;
	rm $(LINK_TARGET)
	cd ../../../ && $(MAKE) clean
