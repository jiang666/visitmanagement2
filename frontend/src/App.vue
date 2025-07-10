<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
userStore.initUser()
</script>

<template>
  <router-view />
</template>
