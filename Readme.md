## Git:
Klonovanie repozitara so submodulmi 
git clone https://github.com/janpolacek/face-classifier-app --recurse-submodules

## Nastavenie kniznic
### OpenCV
Ak chceme novsiu verziu Opencv, treba stiahnut sdk a skopirovat do thirdparty
Nastavit cestu na novy priecinok v CMakeList cez premenu OPENCV_DIR
Skopirovat priecinok obsah z OPENCV_DIR/sdk/native/libs do src/main/jniLibs

File -> New -> Import Module a vybrat OPENCV_DIR/sdk/java  -> Finish
Pridat do dependencies v gradle, pricom 'openCVLibrary340' je nazov modulu
compile project(':openCVLibrary340')

Opencv moze vypisovat ze nepozna napr. android camera2 import. -> treba nastavit verziu android sdk v tomto module
na vyssiu (idealne rovnake nastavenie ako v app)

###Dlib
Staci pullnut najnovsie zmeny, pripadne vlozit novsiu kniznicu do thirdparty a nastavit premennu v CmakeLists
Kompilacia pre python: python setup.py install --yes USE_AVX_INSTRUCTIONS

##Tensorflow
Tiez by malo stacit pullnut nove zmeny pripadne nahradit v thirdparty
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

Pre buildovanie Tensorflow bolo tiez potrebne nainstalovat NDK vo verzii 14, s novsou to neslo

#### Zbuildovanie kniznic tensorflow pre Android
Otvorit tensorflow examples/android v android studiu a zbuildovat
Nasledne skopirovat libtensorflow.so z tensorflow/contrib/android/jni do src/main/jniLibs

Zda sa, ze namiesto celeho procesu buildovania, je mozne pouzit ich skompilovane kniznice ktore maju na jenkinse
https://ci.tensorflow.org/view/Nightly/job/nightly-android/lastSuccessfulBuild/artifact/out/native/

#Kopirovanie modelov na zariadenie
Momentalne nie je spojazdnene automaticke stahovanie modelov, a je potrebne ich rucne na zariadenie skopirovat
Modely sa kopiruju do priecinka na sdstorage/classifier, takze nakoniec by to malo vyzerat na zariadeni takto:

-classifier
---dlib
-----shape_predictor_5_face_landmarks.dat
---facenet
-----20170512-110547.pb
-----20170512-110547-optimized.pb (skusam zlepsit model)
-----...ostatne ktore sa stiahnu s facenet modelu (netreba ich)
---opencv
-----classifier_pairs.txt
-----lfw_classifier_opencv.yml

tie zakladne su pribalene v projekte v priecinku models (opat, je to len manualna aktualizacia)

## Dataset
Je potrebne stiahnut dataset - napr LFW a extrahovat jeho obsah
Dlib mal problem rozoznat velke tvare na velkych fotkach 

###Predspracovanie obrazkov
~~####MTCNN~~
~~for N in {1..4}; do python3 src/align/align_dataset_mtcnn.py ~/datasets/lfw/raw ~/datasets/lfw/alligned --image_size 160 --margin 32 --random_order --gpu_memory_fraction 0.25 & done~~
####DLIB
for N in {1..4}; do python3 tmp/align_dataset.py ~/datasets/lfw/raw ~/datasets/lfw/alligned_dlib --image_size 160 --margin 0.25 --dlib_face_predictor ~/models/dlib/shape_predictor_68_face_landmarks.dat & done

###Natrenovanie classifikatora:
~~####MTCNN~~
~~python3 src/classifier.py TRAIN ~/datasets/lfw/alligned ~/models/facenet/20170512-110547.pb  ~/models/opencv/lfw_classifier_opencv.yml --labels_filename  --batch_size 100 --min_nrof_images_per_class 40 --nrof_train_images_per_class 35 --use_split_dataset~~
####DLIB
python3 src/classifier.py TRAIN ~/datasets/lfw/alligned_dlib ~/models/facenet/20170512-110547.pb  ~/models/opencv/lfw_classifier_opencv.yml --labels_filename ~/models/opencv/classifier_pairs.txt --batch_size 100 --min_nrof_images_per_class 40 --nrof_train_images_per_class 35 --use_split_dataset

### Testovanie classifikatora
python3 src/classifier.py CLASSIFY ~/datasets/lfw/alligned_dlib ~/models/facenet/20170512-110547.pb  ~/models/opencv/lfw_classifier_opencv.yml --batch_size 100 --min_nrof_images_per_class 40 --nrof_train_images_per_class 36 --use_split_dataset


## Analyza a optimalizacia facenet modelu
postup cca podla https://www.tensorflow.org/mobile/prepare_models

#### Benchmark model
bazel build -c opt tensorflow/tools/benchmark:benchmark_model --jobs=1
bazel-bin/tensorflow/tools/benchmark/benchmark_model \
 --graph=/home/jan/models/facenet/20170512-110547.pb \
 --input_layer="input:0,phase_train,batch_size" \
 --input_layer_shape="1,160,160,3::1" \
 --input_layer_type="float,bool,int32" \
 --output_layer="embeddings:0" \
 --show_run_order=false \
 --show_time=false \
 --show_memory=false \
 --show_summary=true \
 --show_flops=true

#### Sumarizacia graphu (inputy, outputy)
bazel build tensorflow/tools/graph_transforms:summarize_graph
bazel-bin/tensorflow/tools/graph_transforms/summarize_graph --in_graph=/home/jan/models/facenet/20170512-110547.pb

#### Stripnutie nepouzivanych nodov - velkost modelu je potom 4xmensia
bazel build tensorflow/tools/graph_transforms:transform-graph
bazel-bin/tensorflow/tools/graph_transforms/transform_graph --in_graph=/home/jan/models/facenet/20170512-110547.pb --out_graph=/home/jan/models/facenet/20170512-110547-optimized.pb --inputs='input:0,phase_train' --outputs='embeddings:0' --transforms='quantize_weights'



## Dalsie kroky:

- [x] Natrenovanie classfikatora a ulozenie modelu cez pytho
- [x] Nacitanie modelu classfikatora androide
- [x] FORK Facenet a zmena submodules aby bolo mozne don zapisovat
- [ ] Oprava povoleni na citanie/zapis z externeho storage
- [ ] Optimalizacia facenet modelu (podla navodu na tensorflow stranke)
- [ ] Automaticke stiahnutie modelov
- [x] Vratenie RGB face chipu z jni - hotovo
- [x] Pouzivanie OpenCV Mat objektov namiesto vracania bytovych poli z JNI
- [x] Kontrola ci rovnaky image vrati rovnake embeddings
- [x] Kontrola ci rovnake embeddings vracaju rovnake classy
- [x] Align tvare cez dlib aj vo facenete
- [ ] Kontrola ci mame obrazok v Extractore v RGB ale BGR
- [x] Prewhiten
- [x] Ulozenie classnamov
- [x] Vypisanie rospoznanej triedy
- [ ] Pouzivat grayScale od detekcie az po extrakciu
- [ ] Viac tvari naraz klasifikovat
- [ ] Text pri stvorceku
- [x] Zmensit model facenetu podla navodu na stranke tf



## Poznatky zlych alebo zdlhavych krokov:
- Nepouzitie kniznice na spracovanie obrazu kamery: S kniznicou Fotoapparat to bolo za chvilu so vsetkym
- Snaha o nativny build tensorflow a jeho pouzivanie bez Javy: Vsetky priklady ukazuju na pouzivanie AAR tensorflow a - automaticke JNI prevolavanie
- Nastavanie OpenCV bolo tiez zdlhave - hned pouzit cez module a pouzivat java interface kde sa da - namiesto pokusu o pouzitie v native
- Vytvorenie SVM modelu cez Tensorflow Estimator: Podarilo sa natrenovat aj ulozit, android nepodporuje nacitanie takehoto modelu - https://github.com/tensorflow/tensorflow/issues/13079
- LibSVM - Api pre android nie je velmi pekne - funguje na zaklade vyskladavanie strinu commandovat a prepinacov a jednym z argumentov je subor ktory ma spracovat
- Dlib classfikator v pythone pre multiclass - nenasli sme vhodny priklad
- Pri konverzii framu z YUV formatu sa objavili signed hodnoty v obrazku
- Neskontrolovali sme hned na zaciatku cast po casti ci davaju rovnake vysledky (napr. rovnake formaty obrazkov, vsetky vykonane operacie (prewhiten))
- Pri analyze modelu sme dostali chybupri stahovani nasm balicka - riesenie je tu https://github.com/tensorflow/tensorflow/issues/16862#issuecomment-368534763
