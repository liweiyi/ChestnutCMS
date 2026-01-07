<template>
  <div class="login">
    登录成功，正在跳转...
  </div>
</template>
<script setup name="LoginRedirect">
import useUserStore from '@/store/modules/user'
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

function loginRedirect() {
  console.log("loginRedirect", route.query.token);
  userStore.tokenLogin(route.query.token);
  router.push({ path: "/" });
}
console.log("loginRedirect2", route.query.token);
loginRedirect();
</script>
