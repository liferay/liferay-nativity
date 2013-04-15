cmake_minimum_required(VERSION 2.8)
project(liferayplugin)

find_package(GTK2 REQUIRED)

#add_definitions(-DGETTEXT_PACKAGE="liferay-plugin")
add_definitions(-DENABLE_LOG)

include_directories(/usr/lib/i386-linux-gnu/glib-2.0/include)
include_directories(/usr/include/nautilus)
include_directories(/usr/include/glib-2.0)
include_directories(/usr/include/gtk-3.0)
include_directories(/usr/include/gdk-pixbuf-2.0)
include_directories(/usr/include/pango-1.0)
include_directories(/usr/include/cairo)
include_directories(/usr/include/atk-1.0)

set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--no-undefined")

find_library(JSONCPP_LIBRARY NAMES libjson_linux-gcc-4.6_libmt.so libjsoncpp.so PATHS /usr/lib /usr/local/lib)

add_library(liferayplugin SHARED plugin.cpp handlers.cpp logger.cpp socket.cpp requests.cpp content.cpp sync.cpp)

target_link_libraries(liferayplugin gobject-2.0 nautilus-extension glib-2.0 pthread ${GTK2_LIBRARIES} ${JSONCPP_LIBRARY})