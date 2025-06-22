<template>  <div class="home">
    <CompilerInterface 
      @theme-changed="$emit('theme-changed', $event)" 
      @show-notification="$emit('show-notification', $event)"
    />
  </div>
</template>

<script>
import CompilerInterface from '../components/CompilerInterface.vue'

export default {
  name: 'Home',
  components: {
    CompilerInterface
  }
}
</script>
