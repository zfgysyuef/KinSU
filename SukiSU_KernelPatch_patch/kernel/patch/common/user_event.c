/* SPDX-License-Identifier: GPL-2.0-or-later */
/* 
 * Copyright (C) 2024 bmax121. All Rights Reserved.
 */

#include <user_event.h>
#include <baselib.h>
#include <log.h>

// SukiSU/FollKernel: userd (APatch daemon) is not used, stub out the calls
#ifdef ANDROID
// Provide stubs for APatch userd functions that are not needed in SukiSU fork
static int load_ap_package_config(void) { return 0; }
static int refresh_trusted_manager_state(void) { return 0; }
#endif

int report_user_event(const char *event, const char *args)
{
    const char *safe_event = event ? event : "";
    const char *safe_args = args ? args : "";

    #ifdef ANDROID
    if (lib_strcmp(safe_event, "post-fs-data") == 0) {
        log_boot("post-fs-data: loading ap package config ...\n");
        load_ap_package_config();
    }
    if (lib_strcmp(safe_event, "boot-completed") == 0) {

    }
    if (lib_strcmp(safe_event, "uid_listener") == 0 && lib_strcmp(safe_args, "package-list-updated") == 0) {
        int trust_rc = refresh_trusted_manager_state();
        log_boot("boot-completed: trusted manager refresh rc=%d\n", trust_rc);
    }
    #endif
    logki("user report event: %s, args: %s\n", safe_event, safe_args);
    return 0;
}
