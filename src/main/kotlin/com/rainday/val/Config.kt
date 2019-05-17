package com.rainday.`val`

import java.util.*

/**
 * Created by wyd on 2019/3/1 13:35:50.
 */

//eventbus address
val EB_APP_DEPLOY = "app-management-deploy"
val EB_APP_UNDEPLOY = "app-management-undeploy"
val EB_APP_DELETE = "app-management-delete"
val EB_APP_UPDATEDEPLOYID = "app-management-updatedeployid"
val EB_APP_UPDATEAPPLICATION = "app-management-updateapplication"

val QUERY_APP_BYNAME = UUID.randomUUID().toString()
val FIND_RELAY_ALL = UUID.randomUUID().toString()
val FIND_APP_BYNAME = UUID.randomUUID().toString()
val FIND_APP_BYKEY = UUID.randomUUID().toString()
val FIND_APP_BYID = UUID.randomUUID().toString()

val DEFAULT_USERAGENT = "TAGW/1.0.0"

val VERTICLE_INFO = "verticleinfo"
val APPLICATION_INFO = "applicationinfo"
val ROUTE_INFO = "routeinfo"