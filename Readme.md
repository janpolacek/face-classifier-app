Postup:

nahradit kniznice v thirdparty ak chceme novsie
Skontrolovat OPENCV_DIR a DLIB_DIR v CMakeLists.txt, tak aby smerovali spravne (v pripade pouzitia novsich verzii)
Skopirovat priecinok libs z OPENCV_DIR/sdk/native/libs do src/main/jniLibs
Zbuildovat kniznicu pre tensorflow a tiez skopirovat do main/jniLibs


