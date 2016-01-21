//
// Created by Joe Wilm on 9/16/15.
//

#include <jni.h>
#include "tealeaf.h"


extern "C" JNIEXPORT void Java_com_weebygames_BubblePang_BubblePangActivity_nativeInitialize(
        JNIEnv *jni,
        jobject activityObject,
        jlong tl0)
{
    tealeaf_t *tealeaf = (tealeaf_t*)tl0;

    struct tl_app_options opts;
    opts.name = "resources";
    opts.splash = NULL;

    opts.url = "resources";
    opts.origin = TL_APP_ORIGIN_LOCAL_ASSETS_UNPACKED;

    tl_app_t *app = tl_app_from_options(tealeaf, &opts);

    tl_app_load(app);
    tl_app_run(app);
}
