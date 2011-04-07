/**
 * JNI to init the Sparkle subsystem.
 * native/sparkle/com_sleazyweasel_sparkle_SparkleActivator.m
 */

#include <Cocoa/Cocoa.h>
#include <Sparkle/Sparkle.h>
#include "com_sleazyweasel_sparkle_SparkleActivator.h"

/*
 * Class:     com_sleazyweasel_sparkle_SparkleActivator
 * Method:    initSparkle
 * Signature: (Ljava/lang/String;ZI)V
 */

JNIEXPORT void JNICALL
Java_com_sleazyweasel_sparkle_SparkleActivator_initSparkle
  (JNIEnv *env, jclass obj,
   jstring pathToSparkleFramework,
   jboolean updateAtStartup,
   jint checkInterval)
{
    BOOL hasLaunchedBefore = [[NSUserDefaults standardUserDefaults]
                              boolForKey:@"SCHasLaunchedBefore"];

    if(!hasLaunchedBefore)
    {
        [[NSUserDefaults standardUserDefaults] setBool:YES
                                               forKey:@"SCHasLaunchedBefore"];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }

    SUUpdater *suUpdater = [SUUpdater updaterForBundle:[NSBundle mainBundle]];

    NSMenu* menu = [[NSApplication sharedApplication] mainMenu];
    NSMenu* applicationMenu = [[menu itemAtIndex:0] submenu];
    NSMenuItem* checkForUpdatesMenuItem = [[NSMenuItem alloc]
                                            initWithTitle:@"Check for Updates..."
                                            action:@selector(checkForUpdates:)
                                            keyEquivalent:@""];

    [checkForUpdatesMenuItem setEnabled:YES];
    [checkForUpdatesMenuItem setTarget:suUpdater];

    // 0 => top, 1 => after "About..."
    [applicationMenu insertItem:checkForUpdatesMenuItem atIndex:1];

    // Update is launched only at the second startup
    if (hasLaunchedBefore && updateAtStartup == JNI_TRUE)
    {
        [suUpdater
         performSelectorOnMainThread:@selector(checkForUpdatesInBackground)
                                withObject:nil
                                waitUntilDone:NO];
    }
}
// EOF