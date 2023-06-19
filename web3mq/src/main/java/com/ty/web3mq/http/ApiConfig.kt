package com.ty.web3mq.http

import com.ty.web3mq.http.ApiConfig

object ApiConfig {
    object Headers {
        const val DATE_TIME = "DateTime"
        const val REQUEST_ID = "RequestId"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val PUB_KEY = "web3mq-request-pubkey"
        const val DID_KEY = "didkey"
        const val AUTHORIZATION = "Authorization"
        const val JSON_CONTENT_TYPE = "application/json; charset=utf-8"
        const val API_VERSION = "api-version"
    }

    //    https://testnet-us-west-1-1.web3mq.com
    //    https://testnet-us-west-1-2.web3mq.com
    //    https://testnet-ap-jp-1.web3mq.com
    //    https://testnet-ap-jp-2.web3mq.com
    //    https://testnet-ap-singapore-1.web3mq.com
    //    https://testnet-ap-singapore-2.web3mq.com
    //    String BASE_URL ="https://testnet-ap-jp-1.web3mq.com";
    const val BASE_URL = "https://dev-ap-jp-1.web3mq.com"

    //    String USER_LOGIN = BASE_URL + "/api/user_login/";
    const val USER_REGISTER = BASE_URL + "/api/user_register_v2/"
    const val USER_RESET_PWD = BASE_URL + "/api/user_reset_password_v2/"
    const val PING = BASE_URL + "/api/ping/"
    const val CHANGE_NOTIFICATION_STATUS = BASE_URL + "/api/notification/status/"
    const val USER_LOGIN = BASE_URL + "/api/user_login_v2/"
    const val GET_CHAT_LIST = BASE_URL + "/api/chats/"
    const val UPDATE_MY_CHAT = BASE_URL + "/api/chats/"
    const val GET_CONTACT_LIST = BASE_URL + "/api/contacts/"
    const val POST_FRIEND_REQUEST = BASE_URL + "/api/contacts/add_friends/"
    const val GET_SENT_FRIEND_REQUEST_LIST = BASE_URL + "/api/contacts/add_friends_list/"
    const val HANDLE_FRIEND_REQUEST = BASE_URL + "/api/contacts/friend_requests/"
    const val GET_RECEIVE_FRIEND_REQUEST_LIST = BASE_URL + "/api/contacts/friend_requests_list/"
    const val GET_MY_PROFILE = BASE_URL + "/api/my_profile/"
    const val GET_USER_PUBLIC_PROFILE = BASE_URL + "/api/get_user_public_profile/"
    const val POST_MY_PROFILE = BASE_URL + "/api/my_profile/"
    const val GET_USER_INFO = BASE_URL + "/api/get_user_info/"
    const val SEARCH_USERS = BASE_URL + "/api/users/search/"
    const val GROUP_CREATE = BASE_URL + "/api/groups/"
    const val GROUP_INVITATION = BASE_URL + "/api/group_invitation/"
    const val GET_GROUP_LIST = BASE_URL + "/api/groups/"
    const val GET_GROUP_MEMBERS = BASE_URL + "/api/group_members/"
    const val CHANGE_MESSAGE_STATUS = BASE_URL + "/api/messages/status/"
    const val GET_MESSAGE_HISTORY = BASE_URL + "/api/messages/history/"
    const val GET_NOTIFICATION_HISTORY = BASE_URL + "/api/notification/history/"
    const val CREATE_TOPIC = BASE_URL + "/api/create_topic/"
    const val GET_MY_CREATE_TOPIC_LIST = BASE_URL + "/api/my_create_topic_list/"
    const val GET_MY_SUBSCRIBE_TOPIC_LIST = BASE_URL + "/api/my_subscribe_topic_list/"
    const val PUBLISH_TOPIC_MESSAGE = BASE_URL + "/api/publish_topic_message/"
    const val SUBSCRIBE_TOPIC_MESSAGE = BASE_URL + "/api/subscribe_topic/"
    const val GET_MY_FOLLOWERS = BASE_URL + "/api/user_followers/"
    const val GET_MY_FOLLOWING = BASE_URL + "/api/user_following/"
    const val GET_FOLLOWERS_AND_FOLLOWING = BASE_URL + "/api/user_follow_contacts/"
    const val POST_FOLLOW = BASE_URL + "/api/following/"
    const val ADD_FRIENDS = BASE_URL + "/api/contacts/add_friends/"
    const val GET_USER_PERMISSIONS = BASE_URL + "/api/get_target_user_permissions/"
    const val UPDATE_USER_PERMISSION = BASE_URL + "/api/update_user_permissions/"
}