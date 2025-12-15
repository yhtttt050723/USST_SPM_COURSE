<template>
  <div class="profile-page">
    <div class="profile">
      <div class="title-out">个人信息</div>
      <div class="avatar-box">
        <div class="title-in">头像 :</div>
         <el-avatar class="avatar" :size="80" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
      </div>
      <div class="item">
        <div class="title-in">用户名 :</div>
        <el-input class="input" v-model="user.name" disabled placeholder=""/>
      </div>
      <div class="item">
        <div class="title-in">学号 :</div>
        <el-input class="input" v-model="user.studentNo" disabled placeholder=""/>
      </div>
    </div>

    <!-- 密码逻辑需要完善 后端是否需要修改密码功能？ -->
    <div class="safe">
      <div class="title-out">账户与安全</div>
      <div class="item">
        <div class="title-in">原密码 :</div>
        <el-input class="input" type="password" v-model="oldPassword" placeholder="" show-password autocomplete="new-password"/>
      </div>
      <div class="item">
        <div class="title-in">新密码 :</div>
        <el-input class="input" type="password" v-model="newPassword" placeholder="" show-password/>
      </div>
      <div class="item">
        <div class="title-in">确认新密码 :</div>
        <el-input class="input" type="password" v-model="confirmNewPassword" placeholder="" show-password/>
      </div>
    </div>

    <div class="button">
      <div class="in">
        <el-button type="primary" size="middle" @click="resetForm" round>重置</el-button>
        <el-button type="primary" size="middle" round>修改</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useUserStore } from '@/stores/useUserStore';

const oldPassword = ref('');
const newPassword = ref('');
const confirmNewPassword = ref('');
const currentUser = useUserStore();
const user =  computed(() => currentUser.currentUser);

const resetForm = () => {
  oldPassword.value = '';
  newPassword.value = '';
  confirmNewPassword.value = '';
};

const isRightOldPassword = () => {
  return (user.value.password === oldPassword.value);
};
const updatePassword = () => {

};
</script>

<style scoped>
.profile-page {
  padding: 20px;
  background-color: white;
  margin: 10px;
  border-radius: 25px;
}
.title-out {
  font-size: 24px;
  font-weight: bold;
  color: #334155;
  margin-bottom: 20px;
}
.profile, .safe {
  padding: 20px;
}
.title-in {
  display: inline-block;
  width: 85px;
  font-size: 15px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 10px;
}
.input {
  width: 300px;
  margin-left: 20px;
}
.avatar {
  margin: 20px;
}
.avatar-box .title-in {
  display: inline-block;
  line-height: 80px;
}
.item {
  margin-bottom: 10px;
}
.button {
  display: flex;
}
.button .in {
  margin-left: 20%;
}
</style>
