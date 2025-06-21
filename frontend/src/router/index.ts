import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Home from '../views/Home.vue'
import Snippet from '../views/Snippet.vue'
import AuthPage from '../views/AuthPage.vue'
import SettingsPage from '../views/SettingsPage.vue'
import authService from '../services/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Auth',
    component: AuthPage
  },
  {
    path: '/settings',
    name: 'Settings',
    component: SettingsPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/snippets/:id',
    name: 'Snippet',
    component: Snippet,
    props: true
  },
  {
    path: '/share/:id',
    name: 'Share',
    component: Snippet,
    props: true
  },
  {
    path: '/s/:id',
    name: 'ShortShare',
    component: Snippet,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !authService.isAuthenticated()) {
    next('/login')
  } else if (to.name === 'Auth' && authService.isAuthenticated()) {
    next('/')
  } else {
    next()
  }
})

export default router
