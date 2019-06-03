
cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(src/main/cpp/librtmp)
add_library(
        pusher
        SHARED
        src/main/cpp/native-lib.cpp)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L$-L${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}")

include_directories(src/main/cpp/include)

find_library(
        log-lib
        log)

target_link_libraries(
        pusher
        rtmp
        x264
        ${log-lib})