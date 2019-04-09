package com.rainday.handler

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.FIND_APP_BYNAME
import com.rainday.`val`.QUERY_APP_BYNAME
import com.rainday.`val`.VERTICLE_INFO
import com.rainday.application.AppVerticle
import com.rainday.ext.toJsonObject
import com.rainday.ext.toJsonString
import com.rainday.model.Action
import com.rainday.model.Application
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Created by wyd on 2019/3/1 11:28:33.
 */

/**
 * 部署app
 */
fun deployApp(rc: RoutingContext) {
    val vertx = rc.vertx()
    val application = rc.bodyAsJson.mapTo(Application::class.java)
    //deploy之前先查询，如果已经存在则直接返回
    vertx.eventBus().send<JsonObject>(FIND_APP_BYNAME, application.appName) {
        if (it.succeeded() && it.result().body() == null) {
            //不存在继续deploy
            val deployOption = DeploymentOptions().setConfig(application.toJsonObject())
            vertx.deployVerticle(AppVerticle::class.java.name, deployOption) {
                if (it.succeeded()) {
                    //设置deployId
                    application.deployId = it.result()

                    val json = JsonObject.mapFrom(application)
                    vertx.eventBus().send(EB_APP_DEPLOY, json)
                    rc.response().setStatusCode(HttpResponseStatus.OK.code())
                        .setStatusMessage(HttpResponseStatus.OK.reasonPhrase())
                        .putHeader("location", "/apps/${application.deployId}")
                        .end()
                } else {
                    rc.response().end("部署失败.原因：${it.cause().message}")
                }
            }
        } else {
            rc.response().setStatusCode(HttpResponseStatus.CONFLICT.code())
                .setStatusMessage(HttpResponseStatus.CONFLICT.reasonPhrase())
                .end(it.result().body().toString())
        }
    }
}

/**
 * 更新APP状态，根据action执行不同逻辑
 */
fun updateApp(rc: RoutingContext) {
    val request = rc.request()
    val action = Action.valueOf(request.getHeader("action") ?: "UNKNOWN")
    when (action) {
        //修改APP名称
        Action.UPDATE_APPNAME -> updateAppName(rc)
        //修改APP描述
        Action.UPDATE_APPDESCRIPTION -> updateAppDescription(rc)

        //修改relay中的可热更新字段(允许修改的参数为：outUrl,outMethod,transmission, paramPairs(参数对应关系))
        Action.UPDATE_RELAY -> updateRelay(rc)
        //新增一个relay
        Action.ADD_RELAY -> addRelay(rc)
        //删除一个relay(删除一个route，以及这个route绑定的转发信息)
        Action.DELETE_RELAY -> disableRelay(rc)
        //启用一个relay
        Action.ENABLE_RELAY -> enableRelay(rc)
        //禁用一个relay
        Action.DISABLE_RELAY -> disableRelay(rc)

        //激活一个APP(deploy这个verticle)
        Action.ACTIVE_APP -> activeApp(rc)
        //关闭一个APP(undeploy这个verticle)
        Action.INACTIVE_APP -> inactiveApp(rc)
        //未知操作
        Action.UNKNOWN -> rc.response()
            .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
            .setStatusMessage(HttpResponseStatus.BAD_REQUEST.reasonPhrase())
            .end("未知的非法操作")
    }
}

/**
 * 删除APP
 */
fun deleteApp(rc: RoutingContext) {
    val vertx = rc.vertx()
    rc.queryParams()
    rc.request().params()
    //todo 这里校验被删除的APP必须是inactive的
    val deployId = rc.pathParam("deployId")
    //todo 这里删除projectInfoVerticle里的APP数据
//    vertx.eventBus().send("")
}

/**
 * 根据条件查询APP
 */
fun queryApp(rc: RoutingContext) {
    rc.vertx().eventBus().send<String>(QUERY_APP_BYNAME, "") {
        rc.response().end(it.result().body())
    }
}

/**
 * 查找某个确定的APP
 */
fun findApp(rc: RoutingContext) {
    val deployId = rc.pathParam("deployId")
    rc.vertx().eventBus().send<String>(FIND_APP_BYNAME, deployId) {
        rc.response().end(it.result().body())
    }
}

/**
 * app唯一性校验
 */
fun appUniqueCheck(rc: RoutingContext) {
    val vertx = rc.vertx()
    val appName: String by rc.pathParams()
    vertx.eventBus().send<Application>(EB_APP_DEPLOY, appName) {
        println("echo received deploy info ${it.result().body()}")
        val app = it.result().body()
        it.result().body().deployId
    }

}

/**
 * 查询所有verticle
 */
fun showVerticles(rc: RoutingContext) {
    val vertx = rc.vertx()
    if (vertx.isClustered) {
        vertx.sharedData().getClusterWideMap<String, JsonObject>(VERTICLE_INFO) {
            rc.response().end(it.result().toJsonString())
        }
    } else {
        val localMap = vertx.sharedData().getLocalMap<String, JsonObject>(VERTICLE_INFO)
        rc.response().end(localMap.toJsonString())
    }
}

/**
 * 修改APP名称
 */
fun updateAppName(rc: RoutingContext) {
    val appName = rc.bodyAsJson.getString("appName", "")
    val deployId = rc.pathParam("deployId")
    rc.vertx().eventBus().send<String>(deployId + Action.UPDATE_APPNAME, appName) {
        if (it.succeeded()) {
            rc.response().end (it.result().body())
        }
    }
}

/**
 * 修改APP描述
 */
fun updateAppDescription(rc: RoutingContext) {
    val description = rc.bodyAsJson.getString("description", "")
    val deployId = rc.pathParam("deployId")
    rc.vertx().eventBus().send<String>(deployId + Action.UPDATE_APPDESCRIPTION, description) {
        if (it.succeeded()) {
            rc.response().end (it.result().body())
        }
    }
}

/**
 * 激活APP
 */
fun activeApp(rc: RoutingContext) {

}

/**
 * 停止APP
 */
fun inactiveApp(rc: RoutingContext) {

}

/**
 * 修改一个中继规则
 */
fun updateRelay(rc: RoutingContext) {

}

/**
 * 新增一个中继规则
 */
fun addRelay(rc: RoutingContext) {

}

/**
 * 更新一个relay中的部分设置(参考${Action}的说明)。
 */
fun enableRelay(rc: RoutingContext) {

}

/**
 * 更新一个relay中的部分设置(参考${Action}的说明)。
 */
fun disableRelay(rc: RoutingContext) {


}