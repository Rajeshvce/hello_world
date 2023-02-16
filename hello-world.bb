DESCRIPTION = "Hello World Recipe"
LICENSE     = "MIT"

inherit dpkg

FILESPATH_prepend = "${THISDIR}/files:"

SRC_URI = "file://src"

S = "${WORKDIR}/src"

PROVIDES = "hello-world"
