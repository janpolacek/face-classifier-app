#Postup pre App:
Klonovat aj so submodulmi

##Skopirovat priecinok libs z OPENCV_DIR/sdk/native/libs do src/main/jniLibs

##Nastavit workspace v tensorflow takto, novsie NDK mi neslo zbuidlovat
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
    
    
##Skopirovat modely do telefonu
###dlib model, staci 5 landmark
jp.faceclassifier/shape_predictor_5_face_landmarks.dat
###facenet model
jp.faceclassifier/model/20170512-110547.pb
### classifier model: TODO
    
    
##Zbuildovat kniznicu pre tensorflow 
ja som otvoril examples/android a zbuildoval cez android studio, a skopirovat libtensorflow z tensorflow/contrib/android/jni do main/jniLibs


#Priprava modelu tensorflow
##Alignment obrazkov
for N in {1..4}; do python src/align/align_dataset_mtcnn.py ~/datasets/lfw/raw ~/datasets/lfw/lfw_mtcnnpy_160 --image_size 160 --margin 32 --random_order --gpu_memory_fraction 0.1 & done
##Natrenovanie classifikatora:  - TODO- parametre
python src/classifier.py TRAIN ~/datasets/lfw/lfw_mtcnnpy_160 ~/models/facenet/20170512-110547.pb  ~/models/lfw_classifier.pkl --batch_size 100 --min_nrof_images_per_class 5 --nrof_train_images_per_class 4 --use_split_dataset

##Testovanie classifikatora
python src/classifier.py CLASSIFY ~/datasets/lfw/lfw_mtcnnpy_160 ~/models/facenet/20170512-110547.pb  ~/models/lfw_classifier.pkl --batch_size 100 --min_nrof_images_per_class 5 --nrof_train_images_per_class 4 --use_split_dataset

#Zmensenie modelu : TODO


#TODO OSTATNE:
Povolenia na zapis do externeho
Stiahnutie do telefonu automaticky
