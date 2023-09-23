<template>
  <div class="dashboard-editor-container">
    <el-row :gutter="10">
      <el-col :span="24">
        <user-info></user-info>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="12">
        <cms-site-visit-stat></cms-site-visit-stat>
      </el-col>
      <el-col :span="12">
        <shortcut></shortcut>
        <cms-site-data-stat></cms-site-data-stat>
        <el-card shadow="hover" class="mt10">
          <div slot="header" class="clearfix">
            <span>算上一卦</span>
          </div>
          <div class="body">
            <div style="display: inline-block; height: 30px; line-height: 30px">起卦前净手焚香，心中默念问卦之事！！！</div>
            <div>
              <el-button 
              type="primary"
              size="mini"
              plain
              :loading="loadingQiGua"
              @click="handleQiGua"><svg-icon icon-class="taiji" /> 起卦</el-button>
            </div>
            <el-row :gutter="20" v-if="guaXiang.benGua">
              <el-col :span="12">
                <div>本卦</div>
                <div class="yao_yang" v-if="guaXiang.benGua.yao6=='ShaoYang'||guaXiang.benGua.yao6=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao6 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao6 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.benGua.yao5=='ShaoYang'||guaXiang.benGua.yao5=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao5 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao5 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.benGua.yao4=='ShaoYang'||guaXiang.benGua.yao4=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao4 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao4 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.benGua.yao3=='ShaoYang'||guaXiang.benGua.yao3=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao3 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao3 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.benGua.yao2=='ShaoYang'||guaXiang.benGua.yao2=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao2 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao2 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.benGua.yao1=='ShaoYang'||guaXiang.benGua.yao1=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao1 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.benGua.yao1 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <el-row>
                  {{ guaXiang.benGuaCi.name }} - {{ guaXiang.benGuaCi.alias }}
                </el-row>
                <el-divider content-position="left">卦辞</el-divider>
                <el-row>
                  {{ guaXiang.benGuaCi.description }}
                </el-row>
                <el-row>
                 《彖》曰：{{ guaXiang.benGuaCi.tuan }}
                </el-row>
                <el-row>
                 《象》曰：{{ guaXiang.benGuaCi.tuan }}
                </el-row>
                <el-divider content-position="left">爻辞</el-divider>
                <el-row>
                  <div v-for="yaoCi in guaXiang.benGuaYaoCi" :key="yaoCi.name">
                    {{ yaoCi.name }}：{{ yaoCi.desc }}
                  </div>
                </el-row>
                <el-divider content-position="left"> 爻辞《象》</el-divider>
                <el-row>
                  <div v-for="yaoCi in guaXiang.bianGuaYaoCiXiang" :key="yaoCi.name">
                    {{ yaoCi.name }}：{{ yaoCi.desc }}
                  </div>
                </el-row>
              </el-col>
              <el-col :span="12">
                <div>变卦</div>
                <div class="yao_yang" v-if="guaXiang.bianGua.yao6=='ShaoYang'||guaXiang.bianGua.yao6=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao6 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao6 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.bianGua.yao5=='ShaoYang'||guaXiang.bianGua.yao5=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao5 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao5 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.bianGua.yao4=='ShaoYang'||guaXiang.bianGua.yao4=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao4 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao4 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.bianGua.yao3=='ShaoYang'||guaXiang.bianGua.yao3=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao3 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao3 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.bianGua.yao2=='ShaoYang'||guaXiang.bianGua.yao2=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao2 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao2 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yang" v-if="guaXiang.bianGua.yao1=='ShaoYang'||guaXiang.bianGua.yao1=='LaoYang'">
                  <span class="yao_yang_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao1 == 'LaoYang'"><i class="el-icon-refresh" /></span>
                </div>
                <div class="yao_yin" v-else>
                  <span class="yao_yin_b"></span><span class="yao_yin_w"></span><span class="yao_yin_b"></span><span class="yao_lao" v-if="guaXiang.bianGua.yao1 == 'LaoYin'"><i class="el-icon-refresh" /></span>
                </div>
                <el-row>
                  {{ guaXiang.bianGuaCi.name }} - {{ guaXiang.bianGuaCi.alias }}
                </el-row>
                <el-divider content-position="left">卦辞</el-divider>
                <el-row>
                  {{ guaXiang.bianGuaCi.description }}
                </el-row>
                <el-row>
                 《彖》曰：{{ guaXiang.bianGuaCi.tuan }}
                </el-row>
                <el-row>
                 《象》曰：{{ guaXiang.bianGuaCi.tuan }}
                </el-row>
                <el-divider content-position="left">爻辞</el-divider>
                <el-row>
                  <div v-for="yaoCi in guaXiang.bianGuaYaoCi" :key="yaoCi.name">
                    {{ yaoCi.name }}：{{ yaoCi.desc }}
                  </div>
                </el-row>
                <el-divider content-position="left"> 爻辞《象》</el-divider>
                <el-row>
                  <div v-for="yaoCi in guaXiang.bianGuaYaoCiXiang" :key="yaoCi.name">
                    {{ yaoCi.name }}：{{ yaoCi.desc }}
                  </div>
                </el-row>
              </el-col>
            </el-row>
          </div>
        </el-card>
        <el-card shadow="hover" class="mt10">
          <div slot="header" class="clearfix">
            <span>广告位招租</span>
          </div>
          <div class="body">
            <div style="display: inline-block; height: 30px; line-height: 30px">演示站后台首页广告位招租~（1000RMB/月，月登陆次数1500左右，具体请参考用户登录日志）</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import SysUserInfo from '@/views/system/dashboard/userInfo'
import SysShortcut from '@/views/system/dashboard/shortcut'
import CmsSiteVisitStat from '@/views/cms/dashboard/siteVisitStat'
import CmsSiteDataStat from '@/views/cms/dashboard/siteDataStat'

import { getGuaXiang } from "@/api/xyz/bagua";

export default {
  name: 'Index',
  components: {
    'user-info': SysUserInfo,
    'shortcut': SysShortcut,
    'cms-site-visit-stat': CmsSiteVisitStat,
    'cms-site-data-stat': CmsSiteDataStat
  },
  data() {
    return {
      loadingQiGua: false,
      guaXiang: {}
    }
  },
  created () {
  },
  methods: {
    handleQiGua() {
      this.loadingQiGua = true;
      getGuaXiang().then(res => {
        this.loadingQiGua = false;
        this.guaXiang = res.data;
        this.guaXiang.benGuaYaoCi = [];
        const benGuaYaoCi = JSON.parse(this.guaXiang.benGuaCi.yaoCi);
        Object.keys(benGuaYaoCi).forEach(item => {
          if (item == 1) {
            if (this.guaXiang.benGua.yao1 == "ShaoYang" || this.guaXiang.benGua.yao1 == "LaoYang") {
              this.guaXiang.benGuaYaoCi.push({ "name": "初九", "desc": benGuaYaoCi[item] })
            } else {
              this.guaXiang.benGuaYaoCi.push({ "name": "初六", "desc": benGuaYaoCi[item] })
            }
          } else if (item == 2) {
            if (this.guaXiang.benGua.yao2 == "ShaoYang" || this.guaXiang.benGua.yao2 == "LaoYang") {
              this.guaXiang.benGuaYaoCi.push({ "name": "九二", "desc": benGuaYaoCi[item] })
            } else {
              this.guaXiang.benGuaYaoCi.push({ "name": "六二", "desc": benGuaYaoCi[item] })
            }
          } else if (item == 3) {
            if (this.guaXiang.benGua.yao3 == "ShaoYang" || this.guaXiang.benGua.yao3 == "LaoYang") {
              this.guaXiang.benGuaYaoCi.push({ "name": "九三", "desc": benGuaYaoCi[item] })
            } else {
              this.guaXiang.benGuaYaoCi.push({ "name": "六三", "desc": benGuaYaoCi[item] })
            }
          } else if (item == 4) {
            if (this.guaXiang.benGua.yao4 == "ShaoYang" || this.guaXiang.benGua.yao4 == "LaoYang") {
              this.guaXiang.benGuaYaoCi.push({ "name": "九四", "desc": benGuaYaoCi[item] })
            } else {
              this.guaXiang.benGuaYaoCi.push({ "name": "六四", "desc": benGuaYaoCi[item] })
            }
          } else if (item == 5) {
            if (this.guaXiang.benGua.yao5 == "ShaoYang" || this.guaXiang.benGua.yao5 == "LaoYang") {
              this.guaXiang.benGuaYaoCi.push({ "name": "九五", "desc": benGuaYaoCi[item] })
            } else {
              this.guaXiang.benGuaYaoCi.push({ "name": "六五", "desc": benGuaYaoCi[item] })
            }
          } else if (item == 6) {
            if (this.guaXiang.benGua.yao6 == "ShaoYang" || this.guaXiang.benGua.yao6 == "LaoYang") {
              this.guaXiang.benGuaYaoCi.push({ "name": "上九", "desc": benGuaYaoCi[item] })
            } else {
              this.guaXiang.benGuaYaoCi.push({ "name": "上六", "desc": benGuaYaoCi[item] })
            }
          }
        });
        this.guaXiang.bianGuaYaoCi = [];
        const bianGuaYaoCi = JSON.parse(this.guaXiang.bianGuaCi.yaoCi);
        Object.keys(bianGuaYaoCi).forEach(item => {
          if (item == 1) {
            if (this.guaXiang.bianGua.yao1 == "ShaoYang" || this.guaXiang.bianGua.yao1 == "LaoYang") {
              this.guaXiang.bianGuaYaoCi.push({ "name": "初九", "desc": bianGuaYaoCi[item] })
            } else {
              this.guaXiang.bianGuaYaoCi.push({ "name": "初六", "desc": bianGuaYaoCi[item] })
            }
          } else if (item == 2) {
            if (this.guaXiang.bianGua.yao2 == "ShaoYang" || this.guaXiang.bianGua.yao2 == "LaoYang") {
              this.guaXiang.bianGuaYaoCi.push({ "name": "九二", "desc": bianGuaYaoCi[item] })
            } else {
              this.guaXiang.bianGuaYaoCi.push({ "name": "六二", "desc": bianGuaYaoCi[item] })
            }
          } else if (item == 3) {
            if (this.guaXiang.bianGua.yao3 == "ShaoYang" || this.guaXiang.bianGua.yao3 == "LaoYang") {
              this.guaXiang.bianGuaYaoCi.push({ "name": "九三", "desc": bianGuaYaoCi[item] })
            } else {
              this.guaXiang.bianGuaYaoCi.push({ "name": "六三", "desc": bianGuaYaoCi[item] })
            }
          } else if (item == 4) {
            if (this.guaXiang.bianGua.yao4 == "ShaoYang" || this.guaXiang.bianGua.yao4 == "LaoYang") {
              this.guaXiang.bianGuaYaoCi.push({ "name": "九四", "desc": bianGuaYaoCi[item] })
            } else {
              this.guaXiang.bianGuaYaoCi.push({ "name": "六四", "desc": bianGuaYaoCi[item] })
            }
          } else if (item == 5) {
            if (this.guaXiang.bianGua.yao5 == "ShaoYang" || this.guaXiang.bianGua.yao5 == "LaoYang") {
              this.guaXiang.bianGuaYaoCi.push({ "name": "九五", "desc": bianGuaYaoCi[item] })
            } else {
              this.guaXiang.bianGuaYaoCi.push({ "name": "六五", "desc": bianGuaYaoCi[item] })
            }
          } else if (item == 6) {
            if (this.guaXiang.bianGua.yao6 == "ShaoYang" || this.guaXiang.bianGua.yao6 == "LaoYang") {
              this.guaXiang.bianGuaYaoCi.push({ "name": "上九", "desc": bianGuaYaoCi[item] })
            } else {
              this.guaXiang.bianGuaYaoCi.push({ "name": "上六", "desc": bianGuaYaoCi[item] })
            }
          }
        });
        this.guaXiang.benGuaYaoCiXiang = [];
        const benGuaYaoCiXiang = JSON.parse(this.guaXiang.benGuaCi.yaoCiXiang);
        Object.keys(benGuaYaoCiXiang).forEach(item => {
          if (item == 1) {
            if (this.guaXiang.benGua.yao1 == "ShaoYang" || this.guaXiang.benGua.yao1 == "LaoYang") {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "初九", "desc": benGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "初六", "desc": benGuaYaoCiXiang[item] })
            }
          } else if (item == 2) {
            if (this.guaXiang.benGua.yao2 == "ShaoYang" || this.guaXiang.benGua.yao2 == "LaoYang") {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "九二", "desc": benGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "六二", "desc": benGuaYaoCiXiang[item] })
            }
          } else if (item == 3) {
            if (this.guaXiang.benGua.yao3 == "ShaoYang" || this.guaXiang.benGua.yao3 == "LaoYang") {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "九三", "desc": benGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "六三", "desc": benGuaYaoCiXiang[item] })
            }
          } else if (item == 4) {
            if (this.guaXiang.benGua.yao4 == "ShaoYang" || this.guaXiang.benGua.yao4 == "LaoYang") {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "九四", "desc": benGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "六四", "desc": benGuaYaoCiXiang[item] })
            }
          } else if (item == 5) {
            if (this.guaXiang.benGua.yao5 == "ShaoYang" || this.guaXiang.benGua.yao5 == "LaoYang") {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "九五", "desc": benGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "六五", "desc": benGuaYaoCiXiang[item] })
            }
          } else if (item == 6) {
            if (this.guaXiang.benGua.yao6 == "ShaoYang" || this.guaXiang.benGua.yao6 == "LaoYang") {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "上九", "desc": benGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.benGuaYaoCiXiang.push({ "name": "上六", "desc": benGuaYaoCiXiang[item] })
            }
          }
        });
        this.guaXiang.bianGuaYaoCiXiang = [];
        const bianGuaYaoCiXiang = JSON.parse(this.guaXiang.bianGuaCi.yaoCiXiang);
        Object.keys(bianGuaYaoCiXiang).forEach(item => {
          if (item == 1) {
            if (this.guaXiang.bianGua.yao1 == "ShaoYang" || this.guaXiang.bianGua.yao1 == "LaoYang") {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "初九", "desc": bianGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "初六", "desc": bianGuaYaoCiXiang[item] })
            }
          } else if (item == 2) {
            if (this.guaXiang.bianGua.yao2 == "ShaoYang" || this.guaXiang.bianGua.yao2 == "LaoYang") {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "九二", "desc": bianGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "六二", "desc": bianGuaYaoCiXiang[item] })
            }
          } else if (item == 3) {
            if (this.guaXiang.bianGua.yao3 == "ShaoYang" || this.guaXiang.bianGua.yao3 == "LaoYang") {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "九三", "desc": bianGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "六三", "desc": bianGuaYaoCiXiang[item] })
            }
          } else if (item == 4) {
            if (this.guaXiang.bianGua.yao4 == "ShaoYang" || this.guaXiang.bianGua.yao4 == "LaoYang") {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "九四", "desc": bianGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "六四", "desc": bianGuaYaoCiXiang[item] })
            }
          } else if (item == 5) {
            if (this.guaXiang.bianGua.yao5 == "ShaoYang" || this.guaXiang.bianGua.yao5 == "LaoYang") {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "九五", "desc": bianGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "六五", "desc": bianGuaYaoCiXiang[item] })
            }
          } else if (item == 6) {
            if (this.guaXiang.bianGua.yao6 == "ShaoYang" || this.guaXiang.bianGua.yao6 == "LaoYang") {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "上九", "desc": bianGuaYaoCiXiang[item] })
            } else {
              this.guaXiang.bianGuaYaoCiXiang.push({ "name": "上六", "desc": bianGuaYaoCiXiang[item] })
            }
          }
        });
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 10px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }

  .yao_yang {
    display: block;
    height: 16px;
    width: 120px;
    margin-bottom: 5px;
  }

  .yao_yang_b {
    background: #000;
    display: inline-block;
    height: 16px;
    width: 100px;
  }

  .yao_yin {
    display: block;
    height: 16px;
    width: 120px;
    margin-bottom: 5px;
  }

  .yao_yin_b {
    background: #000;
    display: inline-block;
    height: 16px;
    width: 40px;
  }

  .yao_yin_w {
    background: #fff;
    display: inline-block;
    height: 16px;
    width: 20px;
  }

  .yao_lao {
    display: inline-block;
    height: 16px;
    width: 20px;
  }
}

@media (max-width:1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}
</style>