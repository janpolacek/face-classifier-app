Postup:

nahradit kniznice v thirdparty ak chceme novsie

Skontrolovat OPENCV_DIR a DLIB_DIR v CMakeLists.txt, tak aby smerovali spravne (v pripade pouzitia novsich verzii)

Skopirovat priecinok libs z OPENCV_DIR/sdk/native/libs do src/main/jniLibs

Pridat modul do Android Studia cez import module -> pridat opencv-3.3.1-android-sdk/sdk/java (v pripade pridania novej verzie opencv-sdk),
a nazov noveho modulu dat do build.gradle

