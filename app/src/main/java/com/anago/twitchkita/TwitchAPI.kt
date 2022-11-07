package com.anago.twitchkita

import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class TwitchAPI {
    companion object {
        private val okHttpClient = OkHttpClient()

        private fun getRequest(query: String): Request {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = RequestBody.create(mediaType, "{\"query\": \"$query\"}")
            val request: Request = Request.Builder().apply {
                post(requestBody)
                url("https://gql.twitch.tv/gql")
                addHeader("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko")
                addHeader("Cache-Control", "no-cache")
            }.build()
            return request
        }

        fun getUserInfo(streamer: String, callback: Callback) {
            val query = """
            query { 
                user(login: \"__displayName__\") { 
                    id 
                    login 
                    displayName 
                    stream { 
                        id 
                        title 
                        createdAt 
                        game { 
                            name 
                        } 
                    } 
                } 
            }
            """.replace("__displayName__", streamer).replace("    ", "").replace("\n", "")
            val request = getRequest(query)
            okHttpClient.newCall(request).enqueue(callback)
        }

        fun getProfileURL(streamer: String, callback: Callback) {
            val query = """
            query { 
                user(login: \"__displayName__\") { 
                    profileImageURL(width: 70) 
                } 
            }
            """.replace("__displayName__", streamer).replace("    ", "").replace("\n", "")
            val request = getRequest(query)
            okHttpClient.newCall(request).enqueue(callback)
        }
    }
}