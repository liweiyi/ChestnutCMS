<template>
    <div class="home-user-info-container">
      <el-card class="box-card" shadow="hover">
        <div>
          <el-row :gutter="20">
            <el-col :span="2">
              <div class="div-avatar">
                <el-image :src="userInfo.avatar" class="home-user-avatar">
                  <div slot="error" class="image-slot">
                    <img src="@/assets/images/default_home_avatar.png" class="home-user-avatar" />
                  </div>
                </el-image>
              </div>
            </el-col>
            <el-col :span="22">
              <div class="user-info-header">{{ $t("Home.Welcome") }}<router-link to="/user/profile">{{ userInfo.nickName }}</router-link></div>
              <div class="user-info-detail">
                <i class="el-icon-time"> {{ $t("Home.LastLoginTime") }}<span>{{ userInfo.lastLoginTime }}</span></i>
                <i class="el-icon-map-location"> {{ $t("Home.LastLoginIP") }}<span>{{ userInfo.lastLoginIp }} [ {{ userInfo.lastLoginAddr }} ]</span></i>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
  </div>
</template>
<script>
import { getHomeUserInfo } from "@/api/system/user";

export default {
  name: "UserInfoDashboard",
  data () {
    return {
      userInfo: {}
    };
  },
  created() {
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      getHomeUserInfo().then(response => {
        this.userInfo = response.data;
        if (this.userInfo.avatar && this.userInfo.avatar != '') {
          this.userInfo.avatar = process.env.VUE_APP_BASE_API + this.userInfo.avatar;
        }
      })
    }
  }
};
</script>
<style>
.home-user-info-container {
  padding: 10px 0;
}
.home-user-info-container .el-card__body {
  padding: 10px 10px 5px 10px;
}
.home-user-info-container .div-avatar {
  padding: 5px;
  text-align: center;
}
.home-user-info-container .home-user-avatar {
	width: 60px;
	height: 60px;
	border-radius: 50%;
	overflow: hidden;
}
.home-user-info-container .user-info-header {
  font-size: 16px;
  padding: 12px 0;
  color: #5a5e66;
}
.home-user-info-container .user-info-header a {
  color: #409eff;
  padding-left: 10px;
}
.home-user-info-container .user-info-detail {
  display: block;
  font-size: 12px;
  color: #6a6b6e;
}
.home-user-info-container .user-info-detail i {
  margin-right: 15px;
}
.home-user-info-container .user-info-detail i span {
  color: #909399;
  padding-left: 5px;
}
</style>