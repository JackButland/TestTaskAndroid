#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL Java_com_noname_testtaskapplication_MainActivity_fingerprintFromJNI(
        JNIEnv* env,
        jobject /* this */,
        jstring android_secure_id,
        jstring android_gsf_id,
        jstring build_fingerprint) {
    jclass cls = env->FindClass("javax/crypto/Mac");
    jstring hashType = env->NewStringUTF("HMACSHA1");
    jmethodID methodInstance = env->GetStaticMethodID(cls, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Mac;");
    if (NULL == methodInstance) return NULL;
    jobject newMacObject = env->CallStaticObjectMethod(cls, methodInstance, hashType);
    jstring macInitKey = env->NewStringUTF("random init key");
    jclass secretKeyCls = env->FindClass("javax/crypto/spec/SecretKeySpec");
    jmethodID methodKeyConstructor = env->GetMethodID(secretKeyCls, "<init>", "([BLjava/lang/String;)V");
    if (NULL == methodKeyConstructor) return NULL;
    jclass clsString = env->FindClass("java/lang/String");
    jmethodID methodGetBytes = env->GetMethodID(clsString, "getBytes", "()[B");
    if (NULL == methodGetBytes) return NULL;
    jobject keyBytes = env->CallObjectMethod(macInitKey,methodGetBytes);
    jobject newKeyObject = env->NewObject(secretKeyCls, methodKeyConstructor, keyBytes,hashType);
    jmethodID methodInit = env->GetMethodID(cls, "init", "(Ljava/security/Key;)V");
    if (NULL == methodInit) return NULL;
    env->CallVoidMethod(newMacObject, methodInit, newKeyObject);
    jobject bAndroidSecureID = env->CallObjectMethod(android_secure_id,methodGetBytes);
    jobject bAndroidGsfID = env->CallObjectMethod(android_gsf_id,methodGetBytes);
    jobject bBuildFingerprint = env->CallObjectMethod(build_fingerprint,methodGetBytes);
    jmethodID methodUpdate = env->GetMethodID(cls, "update", "([B)V");
    if (NULL == methodUpdate) return NULL;
    env->CallVoidMethod(newMacObject, methodUpdate, bAndroidSecureID);
    env->CallVoidMethod(newMacObject, methodUpdate, bAndroidGsfID);
    env->CallVoidMethod(newMacObject, methodUpdate, bBuildFingerprint);
    jmethodID methodDoFinal = env->GetMethodID(cls, "doFinal", "()[B");
    if (NULL == methodDoFinal) return NULL;
    jobject hashResult = env->CallObjectMethod(newMacObject, methodDoFinal);

    jclass clsBase64 = env->FindClass("android/util/Base64");
    jmethodID methodEncode = env->GetStaticMethodID(clsBase64, "encodeToString", "([BI)Ljava/lang/String;");
    if (NULL == methodEncode) return NULL;
    jstring result = (jstring)env->CallStaticObjectMethod(clsBase64,methodEncode,hashResult,2);
    return result;
}
