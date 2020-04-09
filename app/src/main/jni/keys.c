#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_idan_plusplus_Utils_getFirstKeyPart(JNIEnv *env, jclass type) {
    return (*env)->NewStringUTF(env, "oQy3naubq0uII3SCnHMeqHMLEGE4Mm09Bt==");
}

JNIEXPORT jstring JNICALL
Java_com_example_idan_plusplus_Utils_getSecondKeyPart(JNIEnv *env, jclass type) {
    return (*env)->NewStringUTF(env, "qQEKqTEIJRWADmMnZH0lI3yOL1E2ATcyoUyyJRITMztkrzy1oQt2ExEkFG0=");
}




