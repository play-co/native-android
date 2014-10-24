# Guide for modifying the dylibs

In order to support Android 2.2 and phones without OpenSSL we ship with
OpenSSL.  However, the Android runtime linker will look for libssl from the
system location FIRST.  This causes incompatibilities with older versions of
Android that do not have some of the exports from the latest libssl.

So to fix this problem we rename libssl to "libgcl" and libcrypto to
"libgcypto".  The names need to be the same length as the original so that we
can apply the following fix:

If you need to update the dylibs (such as libssl) or add new ones with
dependencies on libssl, you will need to *hex edit* the .so file to depend on
"libgcl" instead of "libssl".

This can be accomplished easily on Mac with [Hex
Fiend](http://ridiculousfish.com/hexfiend/).

With that tool you can find "libssl" and replace all instances of it with
"libgcl".

Otherwise your apps will likely fail to start.

Sorry this ended up being so complicated but we definitely do not want to use
the system OpenSSL library when we need to ship with it anyway.

