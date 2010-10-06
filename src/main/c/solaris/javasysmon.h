  /* DO NOT EDIT THIS FILE - it is machine generated */
  #include <jni.h>
  /* Header for class com_jezhumble_javasysmon_SolarisMonitor */

  #ifndef _Included_com_jezhumble_javasysmon_SolarisMonitor
  #define _Included_com_jezhumble_javasysmon_SolarisMonitor
  #ifdef __cplusplus
  extern "C" {
  #endif

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    numCpus
   * Signature: ()I
   */
  JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_numCpus
    (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    cpuFrequencyInHz
   * Signature: ()I
   */
  JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_cpuFrequencyInHz
    (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    uptimeInSeconds
   * Signature: ()J
   */
   JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_uptimeInSeconds
     (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    numCpus
   * Signature: ()I
   */
   JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_currentPid
     (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    cpuTimes
   * Signature: ()Lcom/jezhumble/javasysmon/CpuTimes;
   */
   JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_cpuTimes
     (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    physical
   * Signature: ()Lcom/jezhumble/javasysmon/MemoryStats;
   */
   JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_physical
     (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    swap
   * Signature: ()Lcom/jezhumble/javasysmon/MemoryStats;
   */
   JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_swap
     (JNIEnv *, jobject);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    killProcess
   * Signature: (I)V
   */
   JNIEXPORT void JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_killProcess
     (JNIEnv *, jobject, jint);

  /*
   * Class:     com_jezhumble_javasysmon_SolarisMonitor
   * Method:    psinfoToProcess
   * Signature: ([B[B)Lcom/jezhumble/javasysmon/ProcessInfo;
   */
   JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_psinfoToProcess
     (JNIEnv *, jobject, jbyteArray, jbyteArray);

  #ifdef __cplusplus
  }
  #endif
  #endif
