/*
 * Tencent is pleased to support the open source community by making vap available.
 *
 * Copyright (C) 2020 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.qgame.playerproj

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.permissionx.guolindev.PermissionX
import com.tencent.qgame.animplayer.util.ALog
import com.tencent.qgame.animplayer.util.IALog
import com.tencent.qgame.playerproj.player.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn1.setOnClickListener {
            startActivity(Intent(this, AnimSimpleDemoActivity::class.java))
        }
        btn2.setOnClickListener {
            startActivity(Intent(this, AnimVapxDemoActivity::class.java))
        }
        btn3.setOnClickListener {
            startActivity(Intent(this, AnimActiveDemoActivity::class.java))
        }
        btn4.setOnClickListener {
            startActivity(Intent(this, AnimSpecialSizeDemoActivity::class.java))
        }
        btn5.setOnClickListener {
            startActivity(Intent(this, AnimRangeActivity::class.java))
        }
        // 初始化日志
        initLog()
        requestPermission()
    }

    private fun requestPermission() {
        PermissionX.init(this).permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .request{ allGranted, _, _ ->
                if (!allGranted) Toast.makeText(this, "你拒绝了外部存储权限", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initLog() {
        ALog.isDebug = true
        ALog.log = object : IALog {
            override fun i(tag: String, msg: String) {
                Log.i(tag, msg)
            }

            override fun d(tag: String, msg: String) {
                Log.d(tag, msg)
            }

            override fun e(tag: String, msg: String) {
                Log.e(tag, msg)
            }

            override fun e(tag: String, msg: String, tr: Throwable) {
                Log.e(tag, msg, tr)
            }
        }
    }

}
