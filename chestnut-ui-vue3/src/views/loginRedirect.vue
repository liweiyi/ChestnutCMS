<template>
  <div class="login">
    {{ $t('System.User.LoginRedirectTip') }}
  </div>
</template>
<script setup name="LoginRedirect">
import useUserStore from '@/store/modules/user'
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

function loginRedirect() {
  userStore.tokenLogin(route.query.token);
  router.push({ path: "/" });
}
loginRedirect();
</script>
