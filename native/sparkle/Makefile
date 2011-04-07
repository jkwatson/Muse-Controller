# Makefile for the Sparkle JNI
# native/sparkle/Makefile
#
# Requires the modified Sparkle.framework installed in /Library/Frameworks

CC=gcc -arch x86_64 -arch i386 -arch ppc
TARGET=libsparkle_init.dylib
JNI_INCLUDE_PATH=/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/
CFLAGS=-I$(JNI_INCLUDE_PATH)
LIBS=-framework AppKit -framework Foundation -framework Sparkle
OBJS=com_sleazyweasel_sparkle_SparkleActivator.o

# Set the below field to the location of your other libraries
# for example lib/native
INSTALL_DIR=../../lib/native/

all:$(TARGET)

clean:
	rm -rf $(TARGET) $(OBJS) *~

install:$(TARGET)
	cp $(TARGET) $(INSTALL_DIR)

libsparkle_init.dylib:$(OBJS)
	$(CC) -dynamiclib -o $(TARGET) $(LIBS) $(CFLAGS) $<
# EOF