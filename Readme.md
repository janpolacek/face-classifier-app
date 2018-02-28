#Navod na vyvoj:
##Instalacia Python, Tensorflow, Dlib, Android Studia
##Klonovanie repozitara so submodulmi --recurse-submodules

#Navod pre pouzitie novsich verzii kniznic OpenCV, Dlib, Tensorflow
##OpenCV
Priecinok skopirovat do priecinka thirdparty
Nastavit cestu na novy priecinok v CMakeList cez premenu OPENCV_DIR
Skopirovat priecinok obsah z OPENCV_DIR/sdk/native/libs do src/main/jniLibs

##Dlib
Staci pullnut najnovsie zmeny, pripadne vlozit novsiu kniznicu do thirdparty a nastavit premennu v CmakeLists

##Tensorflow
V thirdparty/tensorflow nastavit podla navodu workspace
V mojom pripade to bolo
 android_sdk_repository(
                               name = "androidsdk",
                               api_level = 21,
                               # Ensure that you have the build_tools_version below installed in the
                               # SDK manager as it updates periodically.
                               build_tools_version = "26.0.3",
                               # Replace with path to Android SDK on your system
                               path = "/home/jan/Android/Sdk/",
                           )

 android_ndk_repository(
     name="androidndk",
     path="/home/jan/Android/Sdk/ndk-bundle/",
     # This needs to be 14 or higher to compile TensorFlow.
     # Please specify API level to >= 21 to build for 64-bit
     # archtectures or the Android NDK will automatically select biggest
     # API level that it supports without notice.
     # Note that the NDK version is not the API level.
     api_level=14)

Pre buildovanie Tensorflow bolo tiez nainstalovat NDK vo verzii 14, s novsou to neslo
###Zbuildovanie kniznic tensorflow pre Android
Otvorit tensorflow examples/android v android studiu a zbuildovat
Nasledne skopirovat libtensorflow.so z tensorflow/contrib/android/jni do src/main/jniLibs

#Kopirovanie modelov
Momentalne nie je spojazdnene automaticke stahovanie modelov, a je potrebne ich rucne na zariadenie skopirovat
Modely sa kopiruju do priecinka na sdstorage/jp.faceclassifier/
Defaultne ide o model dlibu - shape_predictor_5_face_landmarks.dat,
 facenet na extrakciu embeddingov - model/20170512-110547.pb,
 svc klasifikatora - TODO
Pripadne obmeny modelu je potrebne upravit aj v ceste aplikacie (TODO rovnaky nazov a verzionovanie napr. shape_predictor.v1.dat)


#Priprava na trenovanie facenet
## Trenovanie klasifikatora
Je potrebne stiahnut dataset - napr LFW a extrahovat jeho obsah
Nasledne pokracuje podla prilozenej dokumentacie facenetu s pripadnymi obmenami cesty k datam

###Predspracovanie obrazkov
for N in {1..4}; do python src/align/align_dataset_mtcnn.py ~/datasets/lfw/raw ~/datasets/lfw/lfw_mtcnnpy_160 --image_size 160 --margin 32 --random_order --gpu_memory_fraction 0.1 & done

###Natrenovanie classifikatora:
python src/classifier.py TRAIN ~/datasets/lfw/lfw_mtcnnpy_160 ~/models/facenet/20170512-110547.pb  ~/models/lfw_classifier.pkl --batch_size 100 --min_nrof_images_per_class 5 --nrof_train_images_per_class 4 --use_split_dataset

###Testovanie classifikatora
python src/classifier.py CLASSIFY ~/datasets/lfw/lfw_mtcnnpy_160 ~/models/facenet/20170512-110547.pb  ~/models/lfw_classifier.pkl --batch_size 100 --min_nrof_images_per_class 5 --nrof_train_images_per_class 4 --use_split_dataset

Dalsie kroky:
1, Natrenovanie dlib svc a ulozenie modelu cez python
2, Nacitanie modelu cez dlib v androide
3, Oprava povoleni na citanie/zapis z externeho storage
4, Optimalizacia facenet modelu (podla navodu na tensorflow stranke)
5, Automaticke stiahnutie modelov
6, ...

