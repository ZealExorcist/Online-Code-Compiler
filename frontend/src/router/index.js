import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Snippet from '../views/Snippet.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/snippets/:id',
      name: 'Snippet',
      component: Snippet,
      props: true
    }
  ]
})

export default router
