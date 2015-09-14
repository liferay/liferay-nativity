cmake_minimum_required(VERSION 2.8)

project(liferaynativity)

find_package(GTK2 REQUIRED)

add_definitions(-DENABLE_LOG)
add_definitions(-DJSON_IS_AMALGAMATION)

include_directories(/usr/include/glib-2.0 /usr/lib/glib-2.0/include /usr/lib64/glib-2.0/include /usr/lib/x86_64-linux-gnu/glib-2.0/include /usr/lib/i386-linux-gnu/glib-2.0/include)
include_directories(/usr/include/nautilus)
include_directories(/usr/include/glib-2.0)
include_directories(/usr/include/gtk-3.0)
include_directories(/usr/include/gdk-pixbuf-2.0)
include_directories(/usr/include/pango-1.0)
include_directories(/usr/include/cairo)
include_directories(/usr/include/atk-1.0)
include_directories(${CMAKE_SOURCE_DIR} ${CMAKE_SOURCE_DIR}/jsoncpp ${CMAKE_SOURCE_DIR}/jsoncpp/json)

set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--no-undefined")

add_library(liferaynativity SHARED plugin.cpp handlers.cpp logger.cpp socket.cpp requests.cpp content.cpp sync.cpp jsoncpp/jsoncpp.cpp)

target_link_libraries(liferaynativity gobject-2.0 nautilus-extension glib-2.0 pthread ${GTK2_LIBRARIES})
